/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.api.services;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.test.utils.BaseTestCase;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.namespace.QName;

public class ServiceManagerTest extends BaseTestCase {

    public void testNewService() throws Exception {

        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("service.rxt"));
        registry.put("/service", resource);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        ServiceManager serviceManager = new ServiceManager(registry);
        QName qName = new QName("Sample");
        Service service = serviceManager.newService(qName);
        assertEquals(qName, service.getQName());

        clean();
    }

    public void testNewService1() throws Exception {
        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("service.rxt"));
        registry.put("/service", resource);

        ServiceManager serviceManager = new ServiceManager(registry);
        String content =
                "<metadata xmlns=\"http://www.wso2.org/governance/metadata\"><overview><name>Sample</name><version>1" +
                        ".0.0</version><namespace>UserA</namespace></overview></metadata>";
        OMElement XMLContent = AXIOMUtil.stringToOM(content);
        serviceManager.newService(XMLContent);

        clean();
    }

    public void testAddService() throws Exception {

        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("service.rxt"));
        registry.put("/service", resource);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        ServiceManager serviceManager = new ServiceManager(registry);
        QName qName = new QName("Sample");
        Service service = serviceManager.newService(qName);
        service.setAttribute("overview_name", "Sample");
        service.setAttribute("overview_version", "1.0.0");
        serviceManager.addService(service);

        serviceManager.removeService(service.getId());
        clean();
    }

    public void testUpdateService() throws Exception {

        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("service.rxt"));
        registry.put("/service", resource);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        ServiceManager serviceManager = new ServiceManager(registry);
        QName qName = new QName("Sample");
        Service service = serviceManager.newService(qName);
        service.setAttribute("overview_name", "Sample");
        service.setAttribute("overview_version", "1.0.0");
        service.setAttribute("overview_custom", "oldValue");
        serviceManager.addService(service);

        service.setAttribute("overview_custom", "newValue");
        serviceManager.updateService(service);
        service = serviceManager.getService(service.getId());

        assertEquals("newValue", service.getAttribute("overview_custom"));

        serviceManager.removeService(service.getId());
        clean();
    }

    public void testFindServices() throws Exception {
        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("service.rxt"));
        registry.put("/service", resource);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        ServiceManager serviceManager = new ServiceManager(registry);
        QName qName = new QName("Sample");
        Service service = serviceManager.newService(qName);
        service.setAttribute("overview_name", "Sample");
        service.setAttribute("overview_version", "1.0.0");
        service.setAttribute("overview_custom", "oldValue");
        serviceManager.addService(service);

        Service[] services = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_custom");
                if (attributeVal != null && attributeVal.startsWith("oldValue")) {
                    return true;
                }
                return false;
            }
        });

        assertEquals(1, services.length);

        serviceManager.removeService(service.getId());
        clean();
    }

    public void testGetAllServicePaths() throws Exception {

        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("service.rxt"));
        registry.put("/service", resource);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        ServiceManager serviceManager = new ServiceManager(registry);
        QName qName = new QName("Sample");
        Service service = serviceManager.newService(qName);
        service.setAttribute("overview_name", "Sample");
        service.setAttribute("overview_version", "1.0.0");
        service.setAttribute("overview_custom", "oldValue");
        serviceManager.addService(service);

        assertEquals(1, serviceManager.getAllServicePaths().length);

        serviceManager.removeService(service.getId());
        clean();
    }

    public void testGetAllServices() throws Exception {
        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("service.rxt"));
        registry.put("/service", resource);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        ServiceManager serviceManager = new ServiceManager(registry);
        QName qName = new QName("Sample");
        Service service = serviceManager.newService(qName);
        service.setAttribute("overview_name", "Sample");
        service.setAttribute("overview_version", "1.0.0");
        service.setAttribute("overview_custom", "oldValue");
        service.setAttribute("overview_custom2", "oldValue");
        service.removeAttribute("overview_custom2");
        serviceManager.addService(service);

        Service addedServcie = serviceManager.getService(service.getId());
        assertNull(service.getAttribute("overview_custom2"));

        Service service2 = serviceManager.newService(qName);
        service2.setAttribute("overview_name", "Sample2");
        service2.setAttribute("overview_version", "2.0.0");
        service2.setAttribute("overview_custom", "oldValue");
        serviceManager.addService(service2);

        assertEquals(2, serviceManager.getAllServices().length);

        serviceManager.removeService(service.getId());
        serviceManager.removeService(service2.getId());
        clean();
    }

    public void testGetAllServiceIds() throws Exception {

        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("service.rxt"));
        registry.put("/service", resource);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        ServiceManager serviceManager = new ServiceManager(registry);
        QName qName = new QName("Sample");
        Service service = serviceManager.newService(qName);
        service.setAttribute("overview_name", "Sample");
        service.setAttribute("overview_version", "1.0.0");
        service.setAttribute("overview_custom", "oldValue");
        serviceManager.addService(service);

        Service service2 = serviceManager.newService(qName);
        service2.setAttribute("overview_name", "Sample2");
        service2.setAttribute("overview_version", "2.0.0");
        service2.setAttribute("overview_custom", "oldValue");
        serviceManager.addService(service2);

        assertEquals(2, serviceManager.getAllServiceIds().length);

        serviceManager.removeService(service.getId());
        serviceManager.removeService(service2.getId());
        clean();
    }

    public void testServiceOperation() throws Exception {

        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("service.rxt"));
        registry.put("/service", resource);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        ServiceManager serviceManager = new ServiceManager(registry);
        QName qName = new QName("Sample");
        Service service = serviceManager.newService(qName);
        service.setAttribute("overview_name", "Sample");
        service.setAttribute("overview_version", "1.0.0");
        service.setAttribute("overview_custom", "oldValue");
        serviceManager.addService(service);

        service.activate();
        assertTrue(service.isActive());
        service.deactivate();
        assertFalse(service.isActive());

        assertEquals(0, service.getAttachedEndpoints().length);
        assertEquals(0, service.getAttachedSchemas().length);

        assertEquals(1, serviceManager.getAllServiceIds().length);

        serviceManager.removeService(service.getId());
        clean();
    }


    private void clean() throws RegistryException {
        registry.delete("/service");
    }

}