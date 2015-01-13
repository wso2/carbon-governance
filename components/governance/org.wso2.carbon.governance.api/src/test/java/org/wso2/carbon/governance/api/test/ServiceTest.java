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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.test.utils.BaseTestCase;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;

public class ServiceTest extends BaseTestCase {

    public void testSearchServiceWithNoServices() throws Exception{
        ServiceManager serviceManager = new ServiceManager(registry);
        Service[] allServices = serviceManager.getAllServices();

        assertEquals("invalid service count received", 0, allServices.length);
    }
    public void testAddService() throws Exception {
        ServiceManager serviceManager = new ServiceManager(registry);

        Service service = serviceManager.newService(new QName("http://bang.boom.com/mnm/beep", "MyService"));
        service.addAttribute("testAttribute", "somevalue");
        serviceManager.addService(service);

        String serviceId = service.getId();
        Service newService = serviceManager.getService(serviceId);

        assertEquals(newService.getAttribute("testAttribute"), "somevalue");

        service.addAttribute("testAttribute", "somevalue2");
        serviceManager.updateService(service);

        newService = serviceManager.getService(serviceId);

        String[] values = newService.getAttributes("testAttribute");

        assertEquals(values.length, 2);
    }

    public void testNewServiceContentXMLInvalid() throws RegistryException,
            XMLStreamException {
        ServiceManager serviceManager = new ServiceManager(registry);
        String content = "<metadata xmlns=\"http://www.wso2.org/governance/metadata\"><overview><namespace>UserA</namespace></overview></metadata>";
        OMElement XMLContent = AXIOMUtil.stringToOM(content);
        try {
            serviceManager.newService(XMLContent);
        } catch (GovernanceException e) {
            assertEquals("Unable to compute QName from given XML payload, " +
                    "please ensure that the content passed in matches the configuration.",
                    e.getMessage());
            return;
        }
        fail("An exception was expected to be thrown, but did not.");
    }

    public void testServiceSearch() throws Exception {
        File file = new File("src/test/resources/service.metadata.xml"); 
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileContents = new byte[(int)file.length()];
        fileInputStream.read(fileContents);

        OMElement contentElement = GovernanceUtils.buildOMElement(fileContents);

        ServiceManager serviceManager = new ServiceManager(registry);                                       
        Service service = serviceManager.newService(contentElement);

        service.addAttribute("custom-attribute", "custom-value");
        serviceManager.addService(service);


        // so retrieve it back
        String serviceId = service.getId();
        Service newService = serviceManager.getService(serviceId);

        assertEquals(newService.getAttribute("custom-attribute"),  "custom-value");
        assertEquals(newService.getAttribute("interface_wsdlURL"),
                "/_system/governance/wsdls/com/foo/abc.wsdl");
        assertEquals(service.getQName(), newService.getQName());

        Service service2 = serviceManager.newService(new QName("http://baps.paps.mug/done", "meep"));
        service2.addAttribute("custom-attribute", "custom-value2");
        serviceManager.addService(service2);

        Service service3 =  serviceManager.newService(new QName("http://baps.paps.mug/jug", "peem"));
        service3.addAttribute("custom-attribute", "not-custom-value");
        serviceManager.addService(service3);

        Service service4 =  serviceManager.newService(new QName("http://baps.dadan.mug/jug", "doon"));
        service4.addAttribute("not-custom-attribute", "custom-value3");
        serviceManager.addService(service4);

        Service[] services = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("custom-attribute");
                if (attributeVal != null && attributeVal.startsWith("custom-value")) {
                    return true;
                }
                return false;
            }
        });

        assertEquals(services.length, 2);
        assertTrue(services[0].getQName().equals(service.getQName()) ||
                services[0].getQName().equals(service2.getQName()));
        assertTrue(services[1].getQName().equals(service.getQName()) ||
                services[1].getQName().equals(service2.getQName()));

        newService = serviceManager.getService(service2.getId());
        assertEquals(service2.getAttribute("custom-attribute"), "custom-value2");


        // do the test again
        services = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("custom-attribute");
                if (attributeVal != null && attributeVal.startsWith("custom-value")) {
                    return true;
                }
                return false;
            }
        });

        assertEquals(services.length, 2);
        assertTrue(services[0].getQName().equals(service.getQName()) ||
                services[0].getQName().equals(service2.getQName()));
        assertTrue(services[1].getQName().equals(service.getQName()) ||
                services[1].getQName().equals(service2.getQName()));
    }

    public void testServiceAttachments() throws Exception {
        // first put a WSDL
        WsdlManager wsdlManager = new WsdlManager(registry);

        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
         wsdlManager.addWsdl(wsdl);

        ServiceManager serviceManager = new ServiceManager(registry);
        Service service = serviceManager.newService(new QName("http://test/org/bang", "myservice"));
        serviceManager.addService(service);

        service.attachWSDL(wsdl);

        Wsdl[] wsdls = service.getAttachedWsdls();

        assertEquals(wsdls.length, 1);
        assertEquals(wsdls[0].getQName(), new QName("http://foo.com", "BizService.wsdl"));

        Schema[] schemas = wsdls[0].getAttachedSchemas();

        assertEquals(schemas.length, 1);
        assertEquals(schemas[0].getQName(), new QName("http://bar.org/purchasing", "purchasing.xsd"));
        assertNotNull(schemas[0].getId());

        // now add a policy.
        PolicyManager policyManager = new PolicyManager(registry);

        Policy policy = policyManager.newPolicy(
                "http://svn.wso2.org/repos/wso2/tags/wsf/php/2.1.0/samples/security/" +
                        "complete/policy.xml");
        policyManager.addPolicy(policy);

        service.attachPolicy(policy);

        Policy[] policies = service.getAttachedPolicies();
        assertEquals(policies.length, 1);
        assertEquals(policies[0].getQName(), new QName("policy.xml"));

        File file = new File("src/test/resources/service.metadata.xml");
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileContents = new byte[(int)file.length()];
        fileInputStream.read(fileContents);

        OMElement contentElement = GovernanceUtils.buildOMElement(fileContents);

        service = serviceManager.newService(contentElement);
        //serviceManager.addService(service);

        String[] serviceIds = serviceManager.getAllServiceIds();
        for (String serviceId: serviceIds) {
            Service servicex = serviceManager.getService(serviceId);
            Policy[] policiesx = servicex.getAttachedPolicies();
            if (policiesx != null && policiesx.length != 0) {
                assertEquals(policiesx[0].getQName(), new QName("policy.xml"));
            }
        }

    }

    public void testServiceDelete() throws Exception {
        ServiceManager serviceManager = new ServiceManager(registry);

        Service service = serviceManager.newService(new QName("http://banga.doom.queek/blaa", "lmnop"));
        serviceManager.addService(service);

        Service newService = serviceManager.getService(service.getId());
        assertNotNull(newService);

        serviceManager.removeService(newService.getId());
        newService = serviceManager.getService(service.getId());
        assertNull(newService);


        service = serviceManager.newService(new QName("http://banga.bang.queek/blaa", "basss"));
        serviceManager.addService(service);

        newService = serviceManager.getService(service.getId());
        assertNotNull(newService);

        registry.delete(newService.getPath());
        newService = serviceManager.getService(service.getId());
        assertNull(newService);
    }
}
