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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.platform.extensions.util.Utils;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Handler to monitor policy enforcement done through WSO2 ESB.
 */
@SuppressWarnings("unused")
public class PolicyEnforcementHandler extends Handler {

    private static final Log log = LogFactory.getLog(PolicyEnforcementHandler.class);

    private String sequencePath = null;
    private String registryURL = "https://localhost:9443/registry";
    private String username = null;
    private String password = null;
    private String enforcedPoliciesParameter = "security_enforcedPolicies";
    private String associationType = "depends";
    private String xacmlPolicyKey = "registry.xacml.policy";

    public void setXacmlPolicyKey(String xacmlPolicyKey) {
        this.xacmlPolicyKey = xacmlPolicyKey;
    }

    public void setAssociationType(String associationType) {
        this.associationType = associationType;
    }

    public void setRegistryURL(String registryURL) {
        this.registryURL = registryURL;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEnforcedPoliciesParameter(String enforcedPoliciesParameter) {
        this.enforcedPoliciesParameter = enforcedPoliciesParameter;
    }

    public void setSequencePath(String sequencePath) {
        this.sequencePath = sequencePath;
    }

    public void put(RequestContext requestContext) throws RegistryException {
        if (username == null || password == null) {
            throw new RegistryException("This handler configuration requires that you specify " +
                    "the username and password.");
        }

        if (sequencePath == null) {
            throw new RegistryException("The sequence path has not been defined");
        }
        OMElement proxyContent = Utils.extractPayload(requestContext.getResource());
        OMNamespace namespace = proxyContent.getNamespace();
        String namespaceURI = namespace.getNamespaceURI();
        OMElement target = proxyContent.getFirstChildWithName(new QName(namespaceURI, "target"));
        OMElement inSequence =
                target.getFirstChildWithName(new QName(namespaceURI, "inSequence"));
        Iterator sequenceIterator =
                inSequence.getChildrenWithName(new QName(namespaceURI, "sequence"));
        boolean hasEntitlementPolicies = false;
        while (sequenceIterator.hasNext()) {
            OMElement sequence = (OMElement) sequenceIterator.next();
            String key = sequence.getAttributeValue(new QName("key"));
            Registry registry = requestContext.getRegistry();
            String location = sequencePath + RegistryConstants.PATH_SEPARATOR + key;
            if (key != null && registry.resourceExists(location)) {
                Resource resource = registry.get(location);
                OMElement sequenceContent = Utils.extractPayload(resource);
                String sequenceNSURI = sequenceContent.getNamespace().getNamespaceURI();
                if (sequenceContent.getFirstChildWithName(new QName(sequenceNSURI,
                        "entitlementService")) != null) {
                    hasEntitlementPolicies = true;
                    break;
                }
            }
        }
        final String serviceName = proxyContent.getAttributeValue(new QName("name"));
        Iterator parameters =
                proxyContent.getChildrenWithName(new QName(namespaceURI, "parameter"));
        List<String> policyNames = new LinkedList<String>();
        while (parameters.hasNext()) {
            OMElement parameter = (OMElement) parameters.next();
            if (parameter.getAttributeValue(new QName("name")).equals(
                    "secPolicyRegistryPath")) {
                String[] split = parameter.getText().split(":");
                String policyName = RegistryUtils.getResourceName(split[split.length - 1]);
                policyNames.add(policyName);
            }
        }
        RemoteRegistry registry;
        try {
            registry = new RemoteRegistry(registryURL, username, password);
        } catch (MalformedURLException e) {
            String msg = "Unable to create connection to remote registry server";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        Registry governanceRegistry =
                GovernanceUtils.getGovernanceUserRegistry(registry, username);
        ServiceManager manager = new ServiceManager(governanceRegistry);
        Service[] services = manager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                return service.getQName().getLocalPart().equals(serviceName);
            }
        });
        for (Service service : services) {
            String path = service.getPath();
            String enforcedPolicies = "";
            StringBuilder enforcedPoliciesBuilder = new StringBuilder();
            Association[] dependencies = governanceRegistry.getAssociations(path, associationType);
            for (Association dependency : dependencies) {
                if (dependency.getSourcePath().equals(path)) {
                    String destinationPath = dependency.getDestinationPath();
                    String destinationName = RegistryUtils.getResourceName(
                            destinationPath);
                    boolean doContinue = false;
                    for (String policyName : policyNames) {
                        if (destinationName.equals(policyName)) {
                            enforcedPoliciesBuilder.append(destinationName).append(";");
                            doContinue = true;
                        }
                    }
                    if (!doContinue && hasEntitlementPolicies &&
                            Boolean.toString(true).equals(
                                    governanceRegistry.get(destinationPath).getProperty(
                                            xacmlPolicyKey))) {
                        enforcedPoliciesBuilder.append(destinationName).append(";");
                    }
                }
            }
            enforcedPolicies = enforcedPoliciesBuilder.toString();
            if (enforcedPolicies.length() > 0) {
                service.setAttribute(enforcedPoliciesParameter, enforcedPolicies.substring(0,
                        enforcedPolicies.length() - 1));
                manager.updateService(service);
            }
        }
    }
}
