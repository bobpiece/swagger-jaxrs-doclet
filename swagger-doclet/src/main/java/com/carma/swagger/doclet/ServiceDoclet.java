package com.carma.swagger.doclet;

import java.util.HashMap;
import java.util.Map;

import com.carma.swagger.doclet.parser.JaxRsAnnotationParser;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;

public class ServiceDoclet {

	/**
	 * Generate documentation here.
	 * This method is required for all doclets.
	 * @param doc The root doc
	 * @return true on success.
	 */
	public static boolean start(RootDoc doc) {
		DocletOptions options = DocletOptions.parse(doc.options());
		return new JaxRsAnnotationParser(options, doc).run();
	}

	/**
	 * Check for doclet-added options. Returns the number of
	 * arguments you must specify on the command line for the
	 * given option. For example, "-d docs" would return 2.
	 * <p/>
	 * This method is required if the doclet contains any options. If this method is missing, Javadoc will print an invalid flag error for every option.
	 * @param option The option to check
	 * @return number of arguments on the command line for an option
	 *         including the option name itself. Zero return means
	 *         option not known. Negative value means error occurred.
	 */
	public static int optionLength(String option) {
		Map<String, Integer> options = new HashMap<String, Integer>();
		options.put("-d", 2);
		options.put("-docBasePath", 2);
		options.put("-apiBasePath", 2);
		options.put("-apiVersion", 2);

		options.put("-genericWrapperTypes", 2);

		options.put("-fileParameterAnnotations", 2);
		options.put("-fileParameterTypes", 2);
		options.put("-formParameterAnnotations", 2);
		options.put("-formParameterTypes", 2);
		options.put("-stringTypePrefixes", 2);

		options.put("-compositeParamAnnotations", 2);
		options.put("-compositeParamTypes", 2);

		options.put("-responseTypeTags", 2);
		options.put("-inputTypeTags", 2);

		options.put("-defaultErrorTypeTags", 2);
		options.put("-responseMessageTags", 2);

		options.put("-resourceTags", 2);
		options.put("-operationNotesTags", 2);
		options.put("-operationSummaryTags", 2);
		options.put("-fieldDescriptionTags", 2);
		options.put("-fieldMinTags", 2);
		options.put("-fieldMaxTags", 2);
		options.put("-fieldDefaultTags", 2);

		options.put("-requiredParamsTags", 2);
		options.put("-optionalParamsTags", 2);
		options.put("-paramsMinValueTags", 2);
		options.put("-paramsMaxValueTags", 2);
		options.put("-paramsDefaultValueTags", 2);
		options.put("-paramsNameTags", 2);
		options.put("-parameterNameAnnotations", 2);

		options.put("-requiredFieldTags", 2);
		options.put("-optionalFieldTags", 2);

		options.put("-apiAuthorizationsFile", 2);
		options.put("-apiInfoFile", 2);
		options.put("-unauthOperationTags", 2);
		options.put("-authOperationTags", 2);
		options.put("-unauthOperationTagValues", 2);
		options.put("-authOperationScopes", 2);
		options.put("-operationScopeTags", 2);

		options.put("-swaggerUiZipPath", 2); // kept for backward compatibility
		options.put("-swaggerUiPath", 2);
		// supports turning off copy of swagger ui, useful for tests and also for
		// people that use their own swagger ui files
		options.put("-disableCopySwaggerUi", 1);
		options.put("-skipUiFiles", 1);

		options.put("-disableModels", 1);
		options.put("-modelFieldsRequiredByDefault", 1);

		// supports removing certain methods from the docs, e.g. for hidden/private
		// endpoints
		options.put("-typesToTreatAsOpaque", 2); // this is kept for backward compatibility
		options.put("-excludeModelPrefixes", 2);
		options.put("-excludeResourcePrefixes", 2);

		options.put("-excludeParamAnnotations", 2);
		options.put("-excludeClassTags", 2);
		options.put("-excludeOperationTags", 2);
		options.put("-excludeFieldTags", 2);
		options.put("-excludeParamsTags", 2);

		// flags params as multiple
		options.put("-csvParamsTags", 2);

		// legacy no longer needed
		options.put("-crossClassResources", 1);

		// control deprecation exclusion
		options.put("-disableDeprecatedResourceClassExclusion", 1);
		options.put("-disableDeprecatedModelClassExclusion", 1);
		options.put("-disableDeprecatedOperationExclusion", 1);
		options.put("-disableDeprecatedFieldExclusion", 1);
		options.put("-disableDeprecatedParamExclusion", 1);

		// sort options
		options.put("-sortResourcesByPath", 1);
		options.put("-sortResourcesByPriority", 1);
		options.put("-sortApisByPath", 1);

		Integer value = options.get(option);
		if (value != null) {
			return value;
		} else {
			return 0;
		}
	}

	/**
	 * Return the version of the Java Programming Language supported
	 * by this doclet.
	 * <p/>
	 * This method is required by any doclet supporting a language version newer than 1.1.
	 * @return the language version supported by this doclet.
	 * @since 1.5
	 */
	public static LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}

}
