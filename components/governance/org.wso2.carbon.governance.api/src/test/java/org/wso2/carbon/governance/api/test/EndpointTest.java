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
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.test.utils.BaseTestCase;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class EndpointTest extends BaseTestCase {
    
    public void testAddEndpoint() throws Exception {
        // first add an endpoint, get it delete it, simply stuff like that.
        EndpointManager endpointManager = new EndpointManager(registry);

        Endpoint endpoint1 = endpointManager.newEndpoint("http://localhost/papapa/booom");
        endpoint1.addAttribute("status", "QA");

        endpointManager.addEndpoint(endpoint1);
        assertEquals("/endpoints/localhost/papapa/ep-booom", endpoint1.getPath());

        // now get the endpoint back.
        Endpoint endpoint2 = endpointManager.getEndpoint(endpoint1.getId());
        assertEquals("http://localhost/papapa/booom", endpoint2.getUrl());
        assertEquals("QA", endpoint1.getAttribute("status"));

        // so we will be deleting the endpoint
        endpointManager.removeEndpoint(endpoint2.getId());
        assertTrue(true);

        endpoint2 = endpointManager.getEndpoint(endpoint2.getId());

        assertNull(endpoint2);
    }

    public void testAddWsdlWithEndpoints() throws Exception {
        WsdlManager wsdlManager = new WsdlManager(registry);

        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
        wsdlManager.addWsdl(wsdl);

        Endpoint[] endpoints = wsdl.getAttachedEndpoints();
        assertEquals(1, endpoints.length);

        assertEquals("http://localhost:8080/axis2/services/BizService", endpoints[0].getUrl());
        assertEquals(1, endpoints[0].getAttributeKeys().length);
        assertEquals("true", endpoints[0].getAttribute(CommonConstants.SOAP11_ENDPOINT_ATTRIBUTE));

        // now we are trying to remove the endpoint
        EndpointManager endpointManager = new EndpointManager(registry);

        try {
            endpointManager.removeEndpoint(endpoints[0].getId());
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
        GovernanceArtifact[] artifacts = wsdl.getDependents();
        // delete the wsdl
        wsdlManager.removeWsdl(wsdl.getId());

        ServiceManager serviceManager = new ServiceManager(registry);

        for (GovernanceArtifact artifact: artifacts) {
            if (artifact instanceof Service) {
                // getting the service.
                Service service2 = (Service)artifact;
                serviceManager.removeService(service2.getId());
            }
        }        

        // now try to remove the endpoint
        endpointManager.removeEndpoint(endpoints[0].getId());
        assertTrue(true);
    }

    public void testAddServiceWithEndpoints() throws Exception {
        ServiceManager serviceManager = new ServiceManager(registry);
        Service service = serviceManager.newService(new QName("http://wso2.com/test/xxx", "myService"));

        service.addAttribute("endpoints_entry", ":http://endpoint1");
        service.addAttribute("endpoints_entry", "QA:http://endpoint2");

        serviceManager.addService(service);

        Endpoint[] endpoints = service.getAttachedEndpoints();
        assertEquals(2, endpoints.length);

        assertEquals("http://endpoint1", endpoints[0].getUrl());
        assertEquals(0, endpoints[0].getAttributeKeys().length);

        assertEquals("http://endpoint2", endpoints[1].getUrl());
        assertEquals(1, endpoints[1].getAttributeKeys().length);
        assertEquals("QA", endpoints[1].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR));

        // now update the endpoints in the service
        service.setAttributes("endpoints_entry", new String[] {
                "Dev:http://endpoint3",
                "Production:http://endpoint4",
                "QA:http://endpoint2",
        });
        serviceManager.updateService(service);

        endpoints = getAttachedEndpointsFromService(service);
//        endpoints = service.getAttachedEndpoints();
        assertEquals(3, endpoints.length);


        assertEquals("http://endpoint3", endpoints[0].getUrl());
        assertEquals(1, endpoints[0].getAttributeKeys().length);
        assertEquals("Dev", endpoints[0].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR));

        assertEquals("http://endpoint4", endpoints[1].getUrl());
        assertEquals(1, endpoints[1].getAttributeKeys().length);
        assertEquals("Production", endpoints[1].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR));

        assertEquals("http://endpoint2", endpoints[2].getUrl());
        assertEquals(1, endpoints[2].getAttributeKeys().length);
        assertEquals("QA", endpoints[2].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR));       
    }

    // add endpoints as attachments
    public void testAttachEndpointsToService() throws Exception {
        ServiceManager serviceManager = new ServiceManager(registry);
        Service service = serviceManager.newService(new QName("http://wso2.com/test234/xxxxx", "myServicxcde"));
        serviceManager.addService(service);

        EndpointManager endpointManager = new EndpointManager(registry);
        Endpoint ep1 = endpointManager.newEndpoint("http://endpoint1xx");
        endpointManager.addEndpoint(ep1);

        Endpoint ep2 = endpointManager.newEndpoint("http://endpoint2xx");
        ep2.setAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR, "QA");
        endpointManager.addEndpoint(ep2);

        service.attachEndpoint(ep1);
        service.attachEndpoint(ep2);

        Endpoint[] endpoints = service.getAttachedEndpoints();
        assertEquals(2, endpoints.length);

        assertEquals("http://endpoint1xx", endpoints[0].getUrl());
        assertEquals(0, endpoints[0].getAttributeKeys().length);

        assertEquals("http://endpoint2xx", endpoints[1].getUrl());
        assertEquals(1, endpoints[1].getAttributeKeys().length);
        assertEquals("QA", endpoints[1].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR));

        service.detachEndpoint(ep1.getId());
        endpoints = service.getAttachedEndpoints();
        assertEquals(1, endpoints.length);
        
        assertEquals("http://endpoint2xx", endpoints[0].getUrl());
        assertEquals(1, endpoints[0].getAttributeKeys().length);
        assertEquals("QA", endpoints[0].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR));


        // now update the endpoints in the service
        service.setAttributes("endpoints_entry", new String[] {
                "Dev:http://endpoint3",
                "Production:http://endpoint4",
                "QA:http://endpoint2xx",
        });
        serviceManager.updateService(service);

       endpoints = getAttachedEndpointsFromService(service);
//        endpoints = service.getAttachedEndpoints();
        assertEquals(3, endpoints.length);


        assertEquals("http://endpoint3", endpoints[0].getUrl());
        assertEquals(1, endpoints[0].getAttributeKeys().length);
        assertEquals("Dev", endpoints[0].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR));

        assertEquals("http://endpoint4", endpoints[1].getUrl());
        assertEquals(1, endpoints[1].getAttributeKeys().length);
        assertEquals("Production", endpoints[1].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR));

        assertEquals("http://endpoint2xx", endpoints[2].getUrl());
        assertEquals(1, endpoints[2].getAttributeKeys().length);
        assertEquals("QA", endpoints[2].getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR));

        Endpoint ep5 = endpointManager.getEndpointByUrl("http://endpoint2");
        assertEquals("QA", ep5.getAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR));
    }

    public void testAssociatingEndpoints() throws Exception {
        ServiceManager serviceManager = new ServiceManager(registry);
        Service service = serviceManager.newService(new QName("http://done.ding/dong/doodo", "bangService343"));

        serviceManager.addService(service);

        EndpointManager endpointManager = new EndpointManager(registry);
        Endpoint endpoint = endpointManager.newEndpoint("http://dos.dis/doos/safdsf/ppeekk");
        endpointManager.addEndpoint(endpoint);
        service.attachEndpoint(endpoint);

        // retrieve the service
        Service service2 = serviceManager.getService(service.getId());
        Endpoint[] endpoints = service2.getAttachedEndpoints();
        assertEquals(1, endpoints.length);

        assertEquals(":" + endpoints[0].getUrl(), service2.getAttribute("endpoints_entry"));
    }


    public void testServiceAddingEndpointsWithWsdl() throws Exception {
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
        assertEquals(newService.getAttribute("endpoints_entry"),
                ":http://localhost:8080/axis2/services/BizService");

        // now we just add an endpoints
        WsdlManager wsdlManager = new WsdlManager(registry);
        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/MyChangedBizService.wsdl");
        wsdl.addAttribute("boom", "hahahaha");

        wsdlManager.addWsdl(wsdl);

        GovernanceArtifact[] artifacts = wsdl.getDependents();

        for (GovernanceArtifact artifact: artifacts) {
            if (artifact instanceof Service) {
                // getting the service.
                Service service2 = (Service)artifact;
                Endpoint[] endpoints = service2.getAttachedEndpoints();
                assertEquals(1, endpoints.length);
                assertEquals("http://localhost:8080/axis2/services/BizService-my-changes", endpoints[0].getUrl());
            }
        }
    }

    // detach endpoints
    public void testDetachEndpointsToService() throws Exception {
        ServiceManager serviceManager = new ServiceManager(registry);
        Service service = serviceManager.newService(new QName("http://wso2.com/test234/xxxxxx", "_myServicxcde"));
        serviceManager.addService(service);

        EndpointManager endpointManager = new EndpointManager(registry);
        Endpoint ep1 = endpointManager.newEndpoint("http://endpoint1");
        endpointManager.addEndpoint(ep1);

        Endpoint ep2 = endpointManager.newEndpoint("http://endpoint2");
        ep2.setAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR, "QA");
        endpointManager.addEndpoint(ep2);

        service.attachEndpoint(ep1);
        service.attachEndpoint(ep2);

        Endpoint[] endpoints = service.getAttachedEndpoints();
        assertEquals(2, endpoints.length);

        // get the updated service endpoints
        service = serviceManager.getService(service.getId());

        String[] endpointValues = service.getAttributes("endpoints_entry");

        assertEquals(2, endpointValues.length);
    }

    // add non http endpoints as attachments
    public void testNonHttpEndpointsToService() throws Exception {
        ServiceManager serviceManager = new ServiceManager(registry);
        Service service = serviceManager.newService(new QName("http://wso2.com/doadf/spidf", "myServisdfcxcde"));
        serviceManager.addService(service);

        EndpointManager endpointManager = new EndpointManager(registry);
        Endpoint ep1 = endpointManager.newEndpoint("jms:/Service1");
        endpointManager.addEndpoint(ep1);

        Endpoint ep2 = endpointManager.newEndpoint("jms:/Service2");
        ep2.setAttribute(CommonConstants.ENDPOINT_ENVIRONMENT_ATTR, "QA");
        endpointManager.addEndpoint(ep2);

        service.attachEndpoint(ep1);
        service.attachEndpoint(ep2);

        Endpoint[] endpoints = service.getAttachedEndpoints();
        assertEquals(2, endpoints.length);

        // get the updated service endpoints
        service = serviceManager.getService(service.getId());

        String[] endpointValues = service.getAttributes("endpoints_entry");

        assertEquals(2, endpointValues.length);
    }

    private  Endpoint[] getAttachedEndpointsFromService(Service service) throws GovernanceException {
        List<Endpoint> endpoints =new ArrayList<Endpoint>();
        try {
            String[] endpointValues = service.getAttributes("endpoints_entry");
            EndpointManager endpointManager = new EndpointManager(registry);
            for(String ep:endpointValues) {
                endpoints.add(endpointManager.getEndpointByUrl(getFilteredEPURL(ep)));
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Exception occurred while geting endpoints ");
        }
     return endpoints.toArray(new Endpoint[endpoints.size()]);
    }

    private String getFilteredEPURL(String ep){
//        Dev:http://endpoint3
      if(!ep.startsWith("http")){
      return ep.substring(ep.indexOf(":") + 1, ep.length());
      } else {
       return ep;
      }
    }

}
