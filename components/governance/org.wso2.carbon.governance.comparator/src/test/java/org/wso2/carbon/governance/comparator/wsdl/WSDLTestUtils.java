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

package org.wso2.carbon.governance.comparator.wsdl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.governance.comparator.utils.WSDLComparisonUtils;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WSDLTestUtils {

    private static final Log log = LogFactory.getLog(WSDLTestUtils.class);

    public static Definition getWSDLDefinition() throws WSDLException {
        return WSDLComparisonUtils.getWSDLDefinition();
    }

    public static Definition getWSDLDefinitionWithDetails() throws WSDLException {
        Definition wsdl = getWSDLDefinition();
        wsdl.addNamespace("original", "http://www.original.com/wsdl");
        wsdl.setDocumentBaseURI("http://www.documentbaseuri.com");
        wsdl.setQName(new QName("qname","http://qnamens.com"));
        wsdl.setTargetNamespace("http://www.tns.com/tns");
        wsdl.setDocumentationElement(WSDLTestUtils.createComment("This is original comment"));
        return wsdl;
    }



    public static Element createComment(String comment) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element commentEle = document.createElement("Comment");
            commentEle.setTextContent("Original comment");
            return commentEle;
        } catch (ParserConfigurationException e) {
            log.error(e);
        }
        return null;
    }

    public static Definition loadWSDL(String path) throws WSDLException {
        URL resourceUrl = WSDLTestUtils.class.getResource(path);
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        return reader.readWSDL(resourceUrl.getPath());
    }
}
