/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.governance.comparator.utils;

import junit.framework.TestCase;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.wso2.carbon.governance.comparator.wsdl.WSDLTestUtils;

import javax.validation.constraints.AssertTrue;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WSDLComparisonUtilsTest extends TestCase {

    public void testGetWSDLDefinition() throws WSDLException {
        Definition definition = WSDLComparisonUtils.getWSDLDefinition();
        assertNotNull(definition);
    }

    public void testGetWSDLDeclarationOnly() throws WSDLException {
        String definition = WSDLComparisonUtils.getWSDLDeclarationOnly(getNewnDefinition());
        assertNotNull(definition);
    }

    public void testGetWSDLDeclarationOnlyDetaild() throws WSDLException {
        //TODO in fact send a definition with some binding, operation etc
        String definition = WSDLComparisonUtils.getWSDLDeclarationOnly(WSDLTestUtils.getWSDLDefinitionWithDetails());
        assertNotNull(definition);
        assertTrue(definition.contains("http://www.original.com/wsdl"));
        assertTrue(definition.contains("http://qnamens.com"));
        assertTrue(definition.contains("http://www.tns.com/tns"));
        assertTrue(definition.contains("Original comment"));
        assertTrue(definition.contains("http://schemas.xmlsoap.org/wsdl/"));

    }

    public void testSerializeOutputStream() throws WSDLException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WSDLComparisonUtils.serialize(getNewnDefinition(), baos);
        assertTrue(new String(baos.toByteArray()).length() > 0);
    }

    public void testSerializeWriter() throws WSDLException {
        StringWriter writer = new StringWriter();
        WSDLComparisonUtils.serialize(getNewnDefinition(), writer);
        assertTrue(writer.toString().length() > 0);
    }

    public void testGetWSDLWithoutDeclaration() throws WSDLException {
        Definition changedWSDL = WSDLTestUtils.getWSDLDefinition();
        Import imports = changedWSDL.createImport();
        imports.setLocationURI("http://www.importlocation.com");
        imports.setNamespaceURI("http://www.importnamespace.com");
        changedWSDL.addImport(imports);
        String results = WSDLComparisonUtils.getWSDLWithoutDeclaration(changedWSDL);
        assertTrue(results.contains("<wsdl:import namespace=\"http://www.importnamespace.com\" location=\"http://www.importlocation.com\">"));
        assertFalse(results.contains(ComparatorConstants.XML_DECLARATION));
    }

    public void testLoadWSDL() throws WSDLException, URISyntaxException {
        Definition definition = WSDLTestUtils.loadWSDL("/wsdl/tempconvert.wsdl");
        assertNotNull(definition);

    }


    private Definition getNewnDefinition() throws WSDLException {
        WSDLFactory factory = WSDLFactory.newInstance();
        return factory.newDefinition();
    }


}
