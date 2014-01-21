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
package org.wso2.carbon.governance.platform.extensions.handlers;

import org.wso2.carbon.governance.platform.extensions.util.Utils;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

/**
 * Handler to identify XACML policies added to WSO2 IS.
 */
@SuppressWarnings("unused")
public class XACMLPolicyHandler extends Handler {

    private String xacmlNamespace = "urn:oasis:names:tc:xacml:2.0:policy:schema:os";
    private String xacmlPolicyKey = "registry.xacml.policy";

    public void setXacmlNamespace(String xacmlNamespace) {
        this.xacmlNamespace = xacmlNamespace;
    }

    public void setXacmlPolicyKey(String xacmlPolicyKey) {
        this.xacmlPolicyKey = xacmlPolicyKey;
    }

    public void put(RequestContext requestContext) throws RegistryException {
        Resource resource = requestContext.getResource();
        String namespaceURI = Utils.extractPayload(resource).getNamespace().getNamespaceURI();
        resource.setProperty(xacmlPolicyKey, Boolean.toString(namespaceURI.equals(xacmlNamespace)));
    }
}
