package com.carma.swagger.doclet.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.carma.swagger.doclet.DocletOptions;
import com.carma.swagger.doclet.Recorder;
import com.carma.swagger.doclet.ServiceDoclet;
import com.carma.swagger.doclet.model.Api;
import com.carma.swagger.doclet.model.ApiDeclaration;
import com.carma.swagger.doclet.model.ResourceListing;
import com.carma.swagger.doclet.model.ResourceListingAPI;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

public class JaxRsAnnotationParser {

	private static final String SWAGGER_VERSION = "1.2";
	private static final String SWAGGER_UI_VERSION = "2.1.0";

	private final DocletOptions options;
	private final RootDoc rootDoc;

	public JaxRsAnnotationParser(DocletOptions options, RootDoc rootDoc) {
		this.options = options;
		this.rootDoc = rootDoc;
	}

	public boolean run() {
		try {

			// filter the classes to process
			Collection<ClassDoc> docletClasses = new ArrayList<ClassDoc>();
			for (ClassDoc classDoc : this.rootDoc.classes()) {

				// see if deprecated
				if (this.options.isExcludeDeprecatedResourceClasses() && ParserHelper.isDeprecated(classDoc)) {
					continue;
				}

				// see if excluded via a tag
				if (ParserHelper.hasTag(classDoc, this.options.getExcludeClassTags())) {
					continue;
				}

				// see if excluded via its FQN
				boolean excludeResource = false;
				if (this.options.getExcludeResourcePrefixes() != null && !this.options.getExcludeResourcePrefixes().isEmpty()) {
					for (String prefix : this.options.getExcludeResourcePrefixes()) {
						String className = classDoc.qualifiedName();
						if (className.startsWith(prefix)) {
							excludeResource = true;
							break;
						}
					}
				}
				if (excludeResource) {
					continue;
				}

				docletClasses.add(classDoc);
			}

			List<ApiDeclaration> declarations = null;

			// parse with the v2 parser that supports endpoints of the same resource being spread across resource files
			Map<String, ApiDeclaration> resourceToDeclaration = new HashMap<String, ApiDeclaration>();
			for (ClassDoc classDoc : docletClasses) {
				CrossClassApiParser classParser = new CrossClassApiParser(this.options, classDoc, docletClasses, SWAGGER_VERSION, this.options.getApiVersion(),
						this.options.getApiBasePath());
				classParser.parse(resourceToDeclaration);
			}
			Collection<ApiDeclaration> declarationColl = resourceToDeclaration.values();

			declarations = new ArrayList<ApiDeclaration>(declarationColl);

			// clear any empty models
			for (ApiDeclaration api : declarations) {
				if (api.getModels() != null && api.getModels().isEmpty()) {
					api.setModels(null);
				}
			}

			// sort the api declarations if needed
			if (this.options.isSortResourcesByPriority()) {

				Collections.sort(declarations, new Comparator<ApiDeclaration>() {

					public int compare(ApiDeclaration dec1, ApiDeclaration dec2) {
						return Integer.valueOf(dec1.getPriority()).compareTo(dec2.getPriority());
					}

				});

			} else if (this.options.isSortResourcesByPath()) {
				Collections.sort(declarations, new Comparator<ApiDeclaration>() {

					public int compare(ApiDeclaration dec1, ApiDeclaration dec2) {
						if (dec1 == null || dec1.getResourcePath() == null) {
							return 1;
						}
						if (dec2 == null || dec2.getResourcePath() == null) {
							return -1;
						}
						return dec1.getResourcePath().compareTo(dec2.getResourcePath());
					}

				});
			}

			// sort apis of each declaration
			if (this.options.isSortApisByPath()) {
				for (ApiDeclaration dec : declarations) {
					if (dec.getApis() != null) {
						Collections.sort(dec.getApis(), new Comparator<Api>() {

							public int compare(Api o1, Api o2) {
								if (o1 == null || o1.getPath() == null) {
									return -1;
								}
								return o1.getPath().compareTo(o2.getPath());
							}
						});
					}
				}
			}

			writeApis(declarations);
			// Copy swagger-ui into the output directory.
			if (this.options.isIncludeSwaggerUi()) {
				copyUi();
			}
			return true;
		} catch (IOException e) {
			System.err.println("Failed to write api docs, err msg: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	private void writeApis(Collection<ApiDeclaration> apis) throws IOException {
		List<ResourceListingAPI> resources = new LinkedList<ResourceListingAPI>();
		File outputDirectory = this.options.getOutputDirectory();
		Recorder recorder = this.options.getRecorder();
		for (ApiDeclaration api : apis) {
			String resourcePath = api.getResourcePath();
			if (!Strings.isNullOrEmpty(resourcePath)) {
				String resourceName = resourcePath.replaceFirst("/", "").replaceAll("/", "_").replaceAll("[\\{\\}]", "");
				resources.add(new ResourceListingAPI("/" + resourceName + ".{format}", api.getDescription()));
				File apiFile = new File(outputDirectory, resourceName + ".json");
				recorder.record(apiFile, api);
			}
		}

		// write out json for api
		ResourceListing listing = new ResourceListing(SWAGGER_VERSION, this.options.getApiVersion(), this.options.getDocBasePath(), resources,
				this.options.getApiAuthorizations(), this.options.getApiInfo());
		File docFile = new File(outputDirectory, "service.json");
		recorder.record(docFile, listing);

	}

	private void copyUi() throws IOException {
		File outputDirectory = this.options.getOutputDirectory();
		if (outputDirectory == null) {
			outputDirectory = new File(".");
		}
		Recorder recorder = this.options.getRecorder();
		String uiPath = this.options.getSwaggerUiPath();

		if (uiPath == null) {
			// default inbuilt zip
			copyZip(recorder, null, outputDirectory);
		} else {
			// zip or dir
			File uiPathFile = new File(uiPath);
			if (uiPathFile.isDirectory()) {
				System.out.println("Using swagger dir from: " + uiPathFile.getAbsolutePath());
				copyDirectory(recorder, uiPathFile, uiPathFile, outputDirectory);
			} else if (!uiPathFile.exists()) {
				File f = new File(".");
				System.out.println("SwaggerDoclet working directory: " + f.getAbsolutePath());
				System.out.println("-swaggerUiPath not set correctly as it did not exist: " + uiPathFile.getAbsolutePath());
				throw new RuntimeException("-swaggerUiPath not set correctly as it did not exist: " + uiPathFile.getAbsolutePath());
			} else {
				copyZip(recorder, uiPathFile, outputDirectory);
			}
		}
	}

	private void copyZip(Recorder recorder, File uiPathFile, File outputDirectory) throws IOException {
		ZipInputStream swaggerZip = null;
		try {
			if (uiPathFile == null) {
				swaggerZip = new ZipInputStream(ServiceDoclet.class.getResourceAsStream("/swagger-ui-" + SWAGGER_UI_VERSION + ".zip"));
				System.out.println("Using default swagger-ui.zip file from SwaggerDoclet jar file");
			} else {
				swaggerZip = new ZipInputStream(new FileInputStream(uiPathFile));
				System.out.println("Using swagger-ui.zip file from: " + uiPathFile.getAbsolutePath());
			}

			ZipEntry entry = swaggerZip.getNextEntry();
			while (entry != null) {
				final File swaggerFile = new File(outputDirectory, entry.getName());
				if (entry.isDirectory()) {
					if (!swaggerFile.isDirectory() && !swaggerFile.mkdirs()) {
						throw new RuntimeException("Unable to create directory: " + swaggerFile);
					}
				} else {

					FileOutputStream outputStream = null;
					try {
						outputStream = new FileOutputStream(swaggerFile);
						ByteStreams.copy(swaggerZip, outputStream);
						outputStream.flush();
					} finally {
						if (outputStream != null) {
							outputStream.close();
						}
					}

				}

				entry = swaggerZip.getNextEntry();
			}

		} finally {
			if (swaggerZip != null) {
				swaggerZip.close();
			}
		}
	}

	private void copyDirectory(Recorder recorder, File uiPathFile, File sourceLocation, File targetLocation) throws IOException {
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				if (!targetLocation.mkdirs()) {
					throw new IOException("Failed to create the dir: " + targetLocation.getAbsolutePath());
				}
			}

			String[] children = sourceLocation.list();
			for (String element : children) {
				copyDirectory(recorder, uiPathFile, new File(sourceLocation, element), new File(targetLocation, element));
			}
		} else {

			InputStream in = null;
			OutputStream out = null;
			try {
				in = new FileInputStream(sourceLocation);
				out = new FileOutputStream(targetLocation);
				ByteStreams.copy(in, out);
				out.flush();

			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException ex) {
						// ignore
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException ex) {
						// ignore
					}
				}
			}
		}
	}

}
