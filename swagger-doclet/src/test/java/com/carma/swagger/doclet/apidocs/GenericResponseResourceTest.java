package com.carma.swagger.doclet.apidocs;

import static com.carma.swagger.doclet.apidocs.FixtureLoader.loadFixture;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.carma.swagger.doclet.DocletOptions;
import com.carma.swagger.doclet.Recorder;
import com.carma.swagger.doclet.model.ApiDeclaration;
import com.carma.swagger.doclet.parser.JaxRsAnnotationParser;
import com.sun.javadoc.RootDoc;

/**
 * The GenericResponseResourceTest represents a test for generic classes in response models
 * @version $Id$
 * @author conor.roche
 */
public class GenericResponseResourceTest {

	private Recorder recorderMock;
	private DocletOptions options;

	@Before
	public void setup() {
		this.recorderMock = mock(Recorder.class);
		this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false);
	}

	@Test
	public void testStart() throws IOException {
		final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.genericresponse");
		new JaxRsAnnotationParser(this.options, rootDoc).run();

		final ApiDeclaration api = loadFixture("/fixtures/genericresponse/genericresponse.json", ApiDeclaration.class);
		verify(this.recorderMock).record(any(File.class), eq(api));
	}

}
