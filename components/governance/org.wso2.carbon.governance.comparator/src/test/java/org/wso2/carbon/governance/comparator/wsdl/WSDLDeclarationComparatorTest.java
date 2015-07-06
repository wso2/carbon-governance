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

import junit.framework.TestCase;
import org.wso2.carbon.governance.comparator.Comparator;
import org.wso2.carbon.governance.comparator.Comparison;
import org.wso2.carbon.governance.comparator.ComparisonException;
import org.wso2.carbon.governance.comparator.common.DefaultComparison;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

public class WSDLDeclarationComparatorTest extends TestCase {

    public void test1(){
    }

    public void testCompare() throws WSDLException, ComparisonException {

        Comparator<Definition> comparator = new WSDLDeclarationComparator();
        Definition originalWSDL = WSDLTestUtils.getWSDLDefinition();
        Definition changedWSDL = WSDLTestUtils.getWSDLDefinitionWithDetails();




//        originalWSDL.setExtensionAttribute();

        //        originalWSDL.setTypes();

//      originalWSDL.addBinding();
//      originalWSDL.addImport();
//        originalWSDL.addMessage();
//        originalWSDL.addBinding();
//        originalWSDL.addPortType();
//        originalWSDL.addService();
//        originalWSDL.addExtensibilityElement();

        originalWSDL.getAllBindings();
        originalWSDL.getAllPortTypes();
        originalWSDL.getAllServices();
        originalWSDL.getDocumentBaseURI();
        originalWSDL.getImports();
        originalWSDL.getMessages();
        originalWSDL.getNamespaces();
        originalWSDL.getPortTypes();
        originalWSDL.getQName();
        originalWSDL.getTargetNamespace();
        originalWSDL.getTypes();
        originalWSDL.getDocumentationElement();
        originalWSDL.getExtensibilityElements();
        //TODO Fix me
        originalWSDL.getExtensionAttributes();

        Comparison defaultComparison = new DefaultComparison();
        comparator.compare(originalWSDL, changedWSDL, defaultComparison);
        System.out.println(defaultComparison);
        assertNotNull(defaultComparison);

    }
}
