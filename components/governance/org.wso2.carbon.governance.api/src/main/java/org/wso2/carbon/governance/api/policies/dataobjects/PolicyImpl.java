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
package org.wso2.carbon.governance.api.policies.dataobjects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifactImpl;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * This represents a policy artifact stored on the Registry. Policy artifacts are created as a
 * result of importing or uploading a policy.
 */
public class PolicyImpl extends GovernanceArtifactImpl implements Policy {

    private static final Log log = LogFactory.getLog(PolicyImpl.class);

    private String name;
    private String url;
    private String policyContent;

    /**
     * Constructor accepting resource path, identifier and a registry instance.
     * This constructor should be used only when the policy already saved in the registry.
     *
     * @param path     the resource path.
     * @param id       the resource identifier.
     * @param registry the registry instance.
     *
     * @throws GovernanceException if the construction fails.
     */
    public PolicyImpl(String id, Registry registry) throws GovernanceException {
        super(id);
        associateRegistry(registry);
        loadPolicyDetails();
    }

    /**
     * Constructor accepting resource identifier and the policy URL.
     *
     * @param id  the resource identifier.
     * @param url the policy URL.
     */
    public PolicyImpl(String id, String url) {
        super(id);
        this.url = url;
    }

    public QName getQName() {
        if (name == null) {
            return null;
        }
        // for the policy QName there are no
        return new QName(name);
    }

    /**
     * Method to obtain the policy element of this policy artifact.
     *
     * @return the policy element.
     */
    @Override
    public String getPolicyContent() {
        return policyContent;
    }

    /**
     * Method to set the policy element of this policy artifact.
     *
     * @param policyContent the policy element.
     */
    @Override
    @SuppressWarnings("unused")
    public void setPolicyContent(String policyContent) {
        this.policyContent = policyContent;
    }

    /**
     * Method to obtain the policy URL.
     *
     * @return the policy URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Method to set the name of the policy.
     *
     * @param name the name of the policy.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method to load the policy details into this artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void loadPolicyDetails() throws GovernanceException {
        checkRegistryResourceAssociation();
        Registry registry = getAssociatedRegistry();
        String path = getPath();
        String id = getId();
        Resource resource;
        try {
            resource = registry.get(path);
            byte[] content = (byte[]) resource.getContent();
            policyContent = RegistryUtils.decodeBytes(content);

        } catch (RegistryException e) {
            String msg =
                    "Error in getting the content for the artifact. artifact id: " + id + ", " +
                            "path: " + path + ".";
            log.error(msg);
            throw new GovernanceException(msg, e);
        }
        // get the target namespace.

        name = RegistryUtils.getResourceName(path);

        // and then iterate all the properties and add.
        Properties properties = resource.getProperties();
        if (properties != null) {
            Set keySet = properties.keySet();
            if (keySet != null) {
                for (Object keyObj : keySet) {
                    String key = (String) keyObj;
//                    if (key.equals(GovernanceConstants.ARTIFACT_ID_PROP_KEY)) {
                        // it is not a property.
//                        continue;
//                    }
                    List values = (List) properties.get(key);
                    if (values != null) {
                        for (Object valueObj : values) {
                            String value = (String) valueObj;
                            addAttribute(key, value);
                        }
                    }
                }
            }
        }
    }

    /**
     * Method to set the qualified name of this service artifact.
     *
     * @param qName the qualified name.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void setQName(QName qName) throws GovernanceException {
        // the path will be synced with the qualified name
        this.name = qName.getLocalPart();
    }
}
