/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.metadata.test;


import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import org.wso2.carbon.registry.metadata.lifecycle.StateMachineLifecycle;
import org.wso2.carbon.registry.metadata.models.endpoint.HTTPEndpoint;
import org.wso2.carbon.registry.metadata.models.service.HTTPService;
import org.wso2.carbon.registry.metadata.models.version.ServiceVersion;

import java.util.HashMap;
import java.util.Map;

public class JAXRSV1Client {

    public static void main(String[] args) throws MetadataException {

// Create a service
        Registry registry = null;// Obtain a remote/internal registry instance to start with

        HTTPService http1 = new HTTPService(registry, "foo", new ServiceVersion(registry, "1.0.0-SNAPSHOT"));
        http1.setOwner("serviceOwner");
        http1.setProperty("createdDate", "12-12-2012");

// Save the service
        HTTPService.add(registry, http1);

// Update a service
        HTTPService newService = HTTPService.get(registry, http1.getUUID());
        newService.setOwner("newOwner");
        HTTPService.update(registry, newService);

// Fetch all services
        HTTPService[] services = HTTPService.getAll(registry);

// Search services
        Map<String, String> criteria = new HashMap<String, String>();
        criteria.put(HTTPService.KEY_OWNER, "newOwner");
        HTTPService[] results = HTTPService.find(registry, criteria);

//  Create new Version of a service
        ServiceVersion httpV1 = http1.newVersion("1.0.0");
        HTTPEndpoint ep = new HTTPEndpoint(registry,"myep1");
        ep.setUrl("http://test.rest/stockquote");
        httpV1.addEndpoint(ep);
        httpV1.setProperty("isSecured", "true");

//  Save a service version
        ServiceVersion.add(registry, httpV1);

//  Lifecycle operations fora service
        httpV1.attachLifecycle("HTTPServiceLifecycle");
        StateMachineLifecycle lc = httpV1.getLifecycle();
        lc.transfer("Promote");
        StateMachineLifecycle.State currentState = lc.getCurrentState();

//  Delete service version
        ServiceVersion v1 = ServiceVersion.get(registry, httpV1.getUUID());
        HTTPService.delete(registry, v1.getUUID());

    }
}
