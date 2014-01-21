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
package org.wso2.carbon.governance.api.policies;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.policies.dataobjects.PolicyImpl;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * This provides the management functionality for policy artifacts stored on the
 * registry.
 */
public class PolicyManager {

    private static final Log log = LogFactory.getLog(PolicyManager.class);
    private Registry registry;

    /**
     * Constructor accepting an instance of the registry to use.
     * 
     * @param registry the instance of the registry.
     */
    public PolicyManager(Registry registry) {
        this.registry = registry;
    }

    /**
     * Create a new Schema based on content either embedded or passed to a service.
     *
     * @param content  the schema content
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public Policy newPolicy(byte[] content) throws RegistryException {
        return newPolicy(content, null);
    }

    /**
     * Create a new Schema based on content either embedded or passed to a service.
     *
     * @param content  the schema content
     * @param name     the schema name
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public Policy newPolicy(byte[] content, String name)
            throws RegistryException {
        String policyId = UUID.randomUUID().toString();
        PolicyImpl policy = new PolicyImpl(policyId, name != null ? "name://" + name : null);
        policy.associateRegistry(registry);
        policy.setPolicyContent(RegistryUtils.decodeBytes(content));
        return policy;
    }

    /**
     * Creates a new policy artifact from the given URL.
     * 
     * @param url the given URL.
     * 
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public Policy newPolicy(String url) throws GovernanceException {
        String policyId = UUID.randomUUID().toString();
        PolicyImpl policy = new PolicyImpl(policyId, url);
        policy.associateRegistry(registry);
        return policy;
    }

    /**
     * Adds the given policy artifact to the registry. Please do not use this method to update an
     * existing artifact use the update method instead. If this method is used to update an existing
     * artifact, all existing properties (such as lifecycle details) will be removed from the
     * existing artifact.
     * 
     * @param policy the policy artifact.
     * 
     * @throws GovernanceException if the operation failed.
     */
    public void addPolicy(Policy policy) throws GovernanceException {
        boolean succeeded = false;
        String url = ((PolicyImpl)policy).getUrl();
        try {
            registry.beginTransaction();
            Resource policyResource = registry.newResource();
            policyResource.setMediaType(GovernanceConstants.POLICY_XML_MEDIA_TYPE);

           policyResource.setUUID(policy.getId());
            // setting the policy content
            setContent(policy, policyResource);
            String tmpPath;
            if (policy.getQName() != null) {
                tmpPath = "/" + policy.getQName().getLocalPart();
            } else if (url != null && !url.startsWith("name://")) {
                tmpPath = RegistryUtils.getResourceName(new URL(url).getFile().replace("~", ""));
            } else if (url != null) {
                tmpPath = url.substring("name://".length());
            } else {
                tmpPath = policy.getId() + ".xml";
            }
            // OK this is a hack to get the UUID of the newly added artifact. This needs to be fixed
            // properly with the fix for UUID support at Kernel-level - Janaka.
//            Resource resource;
            if (url == null || url.startsWith("name://")) {
//                resource = registry.get(registry.put("/" + tmpPath, policyResource));
                registry.put("/" + tmpPath, policyResource);
            } else {
//                resource = registry.get(registry.importResource(tmpPath, url, policyResource));
                registry.importResource(tmpPath, url, policyResource);
            }
//            policy.setId(policyResource.getUUID());
            ((PolicyImpl)policy).updatePath();
            ((PolicyImpl)policy).loadPolicyDetails();
            succeeded = true;
        } catch (RegistryException e) {
            String msg = "Error in adding the Policy. policy id: " + policy.getId() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        } catch (MalformedURLException e) {
            String msg = "Malformed policy url provided. url: " + url + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        } finally {
            if (succeeded) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in committing transactions. Add policy failed: policy id: " +
                                    policy.getId() + ", path: " + policy.getPath() + ".";
                    log.error(msg, e);
                }
            } else {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in rolling back transactions. Add policy failed: policy id: " +
                                    policy.getId() + ", path: " + policy.getPath() + ".";
                    log.error(msg, e);
                }
            }
        }
    }

    /**
     * Updates the given policy artifact on the registry.
     *
     * @param policy the policy artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void updatePolicy(Policy policy) throws GovernanceException {
        if (policy.getPolicyContent() == null) {
            // there won't be any updates
            String msg =
                    "Updates are only accepted if the policy content is available. " +
                            "So no updates will be done. " + "policy id: " + policy.getId() +
                            ", policy path: " + policy.getPath() + ".";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        boolean succeeded = false;
        String url = ((PolicyImpl)policy).getUrl();
        try {
            registry.beginTransaction();

            // getting the old policy.
            Policy oldPolicy = getPolicy(policy.getId());
            if (oldPolicy == null) {
                addPolicy(policy);
                return;
            }
            Resource policyResource = registry.newResource();
            policyResource.setMediaType(GovernanceConstants.POLICY_XML_MEDIA_TYPE);

            // setting the policy content
            setContent(policy, policyResource);

            String tmpPath;
            if (policy.getQName() != null) {
                tmpPath = "/" + policy.getQName().getLocalPart();
            } else if (url != null) {
                tmpPath = RegistryUtils.getResourceName(new URL(url).getFile().replace("~", ""));
            } else {
                String msg =
                        "Error in identifying the name for the policy. State the name for the policy.";
                log.error(msg);
                throw new GovernanceException(msg);
            }
            policyResource.setUUID(policy.getId());
            registry.put(tmpPath, policyResource);
//            policy.setId(policyResource.getUUID());
            ((PolicyImpl)policy).updatePath();

            succeeded = true;
        } catch (RegistryException e) {
            String msg =
                    "Error in updating the policy, policy id: " + policy.getId() +
                            ", policy path: " + policy.getPath() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        } catch (MalformedURLException e) {
            String msg = "Malformed url found, url " + url + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        } finally {
            if (succeeded) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in committing transactions. Update policy failed: " +
                                    "policy id: " + policy.getId() + ", path: " + policy.getPath() +
                                    ".";
                    log.error(msg, e);
                }
            } else {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in rolling back transactions. Update policy failed: " +
                                    "policy id: " + policy.getId() + ", path: " + policy.getPath() +
                                    ".";
                    log.error(msg, e);
                }
            }
        }
    }

    /**
     * Fetches the given policy artifact on the registry.
     * 
     * @param policyId the identifier of the policy artifact.
     * 
     * @return the policy artifact.
     * @throws GovernanceException if the operation failed.
     */
    public Policy getPolicy(String policyId) throws GovernanceException {
        GovernanceArtifact artifact =
                GovernanceUtils.retrieveGovernanceArtifactById(registry, policyId);
        if (artifact != null && !(artifact instanceof Policy)) {
            String msg = "The artifact request is not a policy. id: " + policyId + ".";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        return (Policy) artifact;
    }

    /**
     * Removes the given policy artifact from the registry.
     * 
     * @param policyId the identifier of the policy artifact.
     * 
     * @throws GovernanceException if the operation failed.
     */
    public void removePolicy(String policyId) throws GovernanceException {
        GovernanceUtils.removeArtifact(registry, policyId);
    }

    /**
     * Sets content of the given policy artifact to the given resource on the
     * registry.
     * 
     * @param policy the policy artifact.
     * @param policyResource the content resource.
     * 
     * @throws GovernanceException if the operation failed.
     */
    protected void setContent(Policy policy, Resource policyResource) throws GovernanceException {
        if (policy.getPolicyContent() != null) {
            String policyContent = policy.getPolicyContent();
            try {
                policyResource.setContent(policyContent);
            } catch (RegistryException e) {
                String msg =
                        "Error in setting the content from policy, policy id: " + policy.getId() +
                                ", policy path: " + policy.getPath() + ".";
                log.error(msg, e);
                throw new GovernanceException(msg, e);
            }
        }
        // and set all the attributes as properties.
        String[] attributeKeys = policy.getAttributeKeys();
        if (attributeKeys != null) {
            Properties properties = new Properties();
            for (String attributeKey : attributeKeys) {
                String[] attributeValues = policy.getAttributes(attributeKey);
                if (attributeValues != null) {
                    // The list obtained from the Arrays#asList method is
                    // immutable. Therefore,
                    // we create a mutable object out of it before adding it as
                    // a property.
                    properties.put(attributeKey,
                            new ArrayList<String>(Arrays.asList(attributeValues)));
                }
            }
            policyResource.setProperties(properties);
        }
        policyResource.setUUID(policy.getId());
    }

    /**
     * Finds all policy artifacts matching the given filter criteria.
     * 
     * @param criteria the filter criteria to be matched.
     * 
     * @return the policy artifacts that match.
     * @throws GovernanceException if the operation failed.
     */
    public Policy[] findPolicies(PolicyFilter criteria) throws GovernanceException {
        List<Policy> policies = new ArrayList<Policy>();
        for (Policy policy : getAllPolicies()) {
            if (policy != null) {
                if (criteria.matches(policy)) {
                    policies.add(policy);
                }
            }
        }
        return policies.toArray(new Policy[policies.size()]);
    }

    /**
     * Finds all policy artifacts on the registry.
     * 
     * @return all policy artifacts on the registry.
     * @throws GovernanceException if the operation failed.
     */
    public Policy[] getAllPolicies() throws GovernanceException {
        List<String> policyPaths =
                Arrays.asList(GovernanceUtils.getResultPaths(registry,
                        GovernanceConstants.POLICY_XML_MEDIA_TYPE));
        Collections.sort(policyPaths, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return RegistryUtils.getResourceName(o1).compareToIgnoreCase(
                        RegistryUtils.getResourceName(o2));
            }
        });
        List<Policy> policies = new ArrayList<Policy>();
        for (String policyPath : policyPaths) {
            GovernanceArtifact artifact =
                    GovernanceUtils.retrieveGovernanceArtifactByPath(registry, policyPath);
            policies.add((Policy) artifact);
        }
        return policies.toArray(new Policy[policies.size()]);
    }
}
