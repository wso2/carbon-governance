/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.governance.api.test;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.io.IOUtils;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.test.utils.BaseTestCase;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlFilter;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class WSDLTest extends BaseTestCase {
    public void testAddWSDL() throws Exception {
        WsdlManager wsdlManager = new WsdlManager(registry);

        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
        wsdl.addAttribute("creator", "it is me");
        wsdl.addAttribute("version", "0.01");
        wsdlManager.addWsdl(wsdl);

        Wsdl newWsdl = wsdlManager.getWsdl(wsdl.getId());
        assertEquals(wsdl.getWsdlElement().toString(), newWsdl.getWsdlElement().toString());
        assertEquals("it is me", newWsdl.getAttribute("creator"));
        assertEquals("0.01", newWsdl.getAttribute("version"));

        // change the target namespace and check
        String oldWSDLPath = newWsdl.getPath();
        assertEquals(oldWSDLPath, "/wsdls/com/foo/0.01/BizService.wsdl");
        assertTrue(registry.resourceExists("/wsdls/com/foo/0.01/BizService.wsdl"));

        OMElement wsdlElement = newWsdl.getWsdlElement();
        wsdlElement.addAttribute("targetNamespace", "http://ww2.wso2.org/test", null);
        wsdlElement.declareNamespace("http://ww2.wso2.org/test", "tns");
        wsdlManager.updateWsdl(newWsdl);

        assertEquals("/wsdls/org/wso2/ww2/test/0.01/BizService.wsdl", newWsdl.getPath());
        //assertFalse(registry.resourceExists("/wsdls/http/foo/com/BizService.wsdl"));

        // doing an update without changing anything.
        wsdlManager.updateWsdl(newWsdl);

        assertEquals("/wsdls/org/wso2/ww2/test/0.01/BizService.wsdl", newWsdl.getPath());
        assertEquals("0.01", newWsdl.getAttribute("version"));

        newWsdl = wsdlManager.getWsdl(wsdl.getId());
        assertEquals("it is me", newWsdl.getAttribute("creator"));
        assertEquals("0.01", newWsdl.getAttribute("version"));

        // adding a new schema to the wsdl.
        wsdlElement = newWsdl.getWsdlElement();
        OMElement schemaElement = evaluateXPathToElement("//xsd:schema", wsdlElement);

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement importElement = factory.createOMElement(
                new QName("http://www.w3.org/2001/XMLSchema", "import"));
        importElement.addAttribute("schemaLocation",
                "http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/xsd/purchasing_dup.xsd", null);
        schemaElement.addChild(importElement);
        importElement.addAttribute("namespace", "http://bar.org/purchasing_dup", null);

        wsdlManager.updateWsdl(newWsdl);

        Schema[] schemas = newWsdl.getAttachedSchemas();
        assertEquals("/schemas/org/bar/purchasing_dup/0.01/purchasing_dup.xsd",
                schemas[schemas.length - 1].getPath());


        Wsdl[] wsdls = wsdlManager.findWsdls(new WsdlFilter() {
            public boolean matches(Wsdl wsdl) throws GovernanceException {
                Schema[] schemas = wsdl.getAttachedSchemas();
                for (Schema schema: schemas) {
                    if (schema.getPath().equals("/schemas/org/bar/purchasing_dup/0.01/purchasing_dup.xsd")) {
                        return true;
                    }
                }
                return false;
            }
        });
        assertEquals(1, wsdls.length);
        assertEquals(newWsdl.getId(), wsdls[0].getId());

        // deleting the wsdl
        wsdlManager.removeWsdl(newWsdl.getId());
        Wsdl deletedWsdl = wsdlManager.getWsdl(newWsdl.getId());
        assertNull(deletedWsdl);

        wsdlManager = new WsdlManager(registry);
        // add again
        Wsdl anotherWsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
        anotherWsdl.addAttribute("creator", "it is not me");
        anotherWsdl.addAttribute("version", "0.02");
        wsdlManager.addWsdl(anotherWsdl);

        // and delete the wsdl
        wsdlManager.removeWsdl(anotherWsdl.getId());
        assertNull(wsdlManager.getWsdl(anotherWsdl.getId()));

    }
//TODO fix these test cases properly
/*
    public void testEditWSDL() throws Exception {
        WsdlManager wsdlManager = new WsdlManager(registry);

        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
        wsdl.addAttribute("creator2", "it is me");
        wsdl.addAttribute("version2", "0.01");
        wsdlManager.addWsdl(wsdl);

        // now edit the wsdl
        OMElement contentElement = wsdl.getWsdlElement();
        OMElement addressElement = evaluateXPathToElement("//soap:address", contentElement);
        addressElement.addAttribute("location", "http://my-custom-endpoint/hoooo", null);
        wsdl.setWsdlElement(contentElement);

        // now do an update.
        wsdlManager.updateWsdl(wsdl);

        // now get the wsdl and check the update is there.
        Wsdl wsdl2 = wsdlManager.getWsdl(wsdl.getId());
        assertEquals("it is me", wsdl2.getAttribute("creator2"));
        assertEquals("0.01", wsdl2.getAttribute("version2"));
        OMElement contentElement2 = wsdl.getWsdlElement();
        OMElement addressElement2 = evaluateXPathToElement("//soap:address", contentElement2);

        assertEquals("http://my-custom-endpoint/hoooo", addressElement2.getAttributeValue(new QName("location")));
    }
*/

    private static OMElement evaluateXPathToElement(String expression,
                                                           OMElement root) throws Exception {
        List<OMElement> nodes = GovernanceUtils.evaluateXPathToElements(expression, root);
        if (nodes == null || nodes.size() == 0) {
            return null;
        }
        return nodes.get(0);
    }

}
