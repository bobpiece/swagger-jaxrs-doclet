/*
 *  Copyright 2014 ADVA Optical Networking SE. All rights reserved.
 *
 *  Owner: rpieczonka
 *
 *  $Id$
 */

package com.carma.swagger.doclet.apidocs;

import com.carma.swagger.doclet.DocletOptions;
import com.carma.swagger.doclet.Recorder;
import com.carma.swagger.doclet.model.ApiDeclaration;
import com.carma.swagger.doclet.model.ResourceListing;
import com.carma.swagger.doclet.parser.JaxRsAnnotationParser;
import com.sun.javadoc.RootDoc;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.carma.swagger.doclet.apidocs.FixtureLoader.loadFixture;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SameRootResourceTest {

  private Recorder recorderMock;
  private DocletOptions options;

  @Before
  public void setup() {
    this.recorderMock = mock(Recorder.class);
    this.options = new DocletOptions().setRecorder(this.recorderMock).setIncludeSwaggerUi(false).setExcludeDeprecatedParams(false);
  }

  @Test
  public void testStart() throws IOException {
    final RootDoc rootDoc = RootDocLoader.fromPath("src/test/resources", "fixtures.samperootresource");

    boolean parsingResult = new JaxRsAnnotationParser(this.options, rootDoc).run();
    assertThat("JavaDoc generation failed", parsingResult, equalTo(true));

    final ResourceListing expectedListing = loadFixture("/fixtures/samperootresource/service.json", ResourceListing.class);
    verify(this.recorderMock).record(any(File.class), eq(expectedListing));

    final ApiDeclaration expectedDeclaration = loadFixture("/fixtures/samperootresource/foo.json", ApiDeclaration.class);
    verify(this.recorderMock).record(any(File.class), eq(expectedDeclaration));
  }

}
