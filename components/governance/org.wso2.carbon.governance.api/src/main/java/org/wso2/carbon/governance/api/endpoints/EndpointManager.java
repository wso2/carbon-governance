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
package org.wso2.carbon.governance.api.endpoints;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.endpoints.dataobjects.EndpointImpl;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.handlers.utils.EndpointUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import java.util.*;

/**
 * This provides the management functionality for endpoint artifacts stored on
 * the registry.
 */
public class EndpointManager {

    private static final Log log = LogFactory.getLog(EndpointManager.class);
    private Registry registry;

    /**
     * Constructor accepting an instance of the registry to use.
     * 
     * @param registry the instance of the registry.
     */
    public EndpointManager(Registry registry) {
        this.registry = registry;
    }

    /**
     * Creates a new endpoint artifact from the given URL.
     * 
     * @param url the given URL.
     * 
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public Endpoint newEndpoint(String url) throws GovernanceException {
        String endpointId = UUID.randomUUID().toString();
        EndpointImpl endpoint = new EndpointImpl(url, endpointId);
        endpoint.associateRegistry(registry);
        return endpoint;
    }

    /**
     * Adds the given endpoint artifact to the registry. Please do not use this method to update an
     * existing artifact use the update method instead. If this method is used to update an existing
     * artifact, all existing properties (such as lifecycle details) will be removed from the
     * existing artifact.
     *
     * @param endpoint the endpoint artifact.
     * 
     * @throws GovernanceException if the operation failed.
     */
    public void addEndpoint(Endpoint endpoint) throws GovernanceException {
        boolean succeeded = false;
        try {
            registry.beginTransaction();
            Resource endpointResource = registry.newResource();
            endpointResource.setMediaType(GovernanceConstants.ENDPOINT_MEDIA_TYPE);
            setContent(endpoint, endpointResource);
            String tmpPath = "/" + GovernanceUtils.getNameFromUrl(((EndpointImpl)endpoint).getUrl());
            endpointResource.setUUID(endpoint.getId());

//            Resource resource = registry.get(registry.put(tmpPath, endpointResource));
            registry.put(tmpPath, endpointResource);
//            endpoint.setId(endpointResource.getUUID());
            ((EndpointImpl)endpoint).updatePath();
            succeeded = true;
        } catch (RegistryException e) {
            String msg =
                    "Add endpoint failed: endpoint id: " + endpoint.getId() + ", path: " +
                            endpoint.getPath() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        } finally {
            if (succeeded) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in committing transactions. Add endpoint failed: " +
                                    "endpoint id: " + endpoint.getId() + ", path: " +
                                    endpoint.getPath() + ".";
                    log.error(msg, e);
                }
            } else {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in rolling back transactions. Add endpoint failed: " +
                                    "endpoint id: " + endpoint.getId() + ", path: " +
                                    endpoint.getPath() + ".";
                    log.error(msg, e);
                }
            }
        }
    }

    /**
     * Updates the given endpoint artifact on the registry.
     * 
     * @param endpoint the endpoint artifact.
     * 
     * @throws GovernanceException if the operation failed.
     */
    @SuppressWarnings("unused")
    public void updateEndpoint(Endpoint endpoint) throws GovernanceException {
        if (((EndpointImpl)endpoint).getUrl() == null) {
            // there won't be any updates
            String msg =
                    "Updates are only accepted if the url is available. " +
                            "So no updates will be done. " + "endpoint id: " + endpoint.getId() +
                            ", endpoint path: " + endpoint.getPath() + ".";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        boolean succeeded = false;
        try {
            registry.beginTransaction();

            // getting the old endpoint.
//            Endpoint oldEndpoint = getEndpoint(endpoint.getId());
//            if (oldEndpoint != null) {
//                // we are expecting only the OMElement to be different.
//                String oldPath = oldEndpoint.getPath();
//                registry.delete(oldPath);
//            }
            
            addEndpoint(endpoint);
            succeeded = true;
        } catch (RegistryException e) {
            String msg =
                    "Error in updating the endpoint, endpoint id: " + endpoint.getId() +
                            ", endpoint path: " + endpoint.getPath() + ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        } finally {
            if (succeeded) {
                try {
                    registry.commitTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in committing transactions. Update endpoint failed: " +
                                    "endpoint id: " + endpoint.getId() + ", path: " +
                                    endpoint.getPath() + ".";
                    log.error(msg, e);
                }
            } else {
                try {
                    registry.rollbackTransaction();
                } catch (RegistryException e) {
                    String msg =
                            "Error in rolling back transactions. Update endpoint failed: " +
                                    "endpoint id: " + endpoint.getId() + ", path: " +
                                    endpoint.getPath() + ".";
                    log.error(msg, e);
                }
            }
        }
    }

    /**
     * Fetches the given endpoint artifact on the registry.
     * 
     * @param endpointId the identifier of the endpoint artifact.
     * 
     * @return the endpoint artifact.
     * @throws GovernanceException if the operation failed.
     */
    public Endpoint getEndpoint(String endpointId) throws GovernanceException {
        GovernanceArtifact artifact =
                GovernanceUtils.retrieveGovernanceArtifactById(registry, endpointId);
        if (artifact != null && !(artifact instanceof Endpoint)) {
            String msg = "The artifact request is not an endpoint. id: " + endpointId + ".";
            log.error(msg);
            throw new GovernanceException(msg);
        }
        return (Endpoint) artifact;
    }

    /**
     * Removes the given endpoint artifact from the registry.
     * 
     * @param endpointId the identifier of the endpoint artifact.
     * 
     * @throws GovernanceException if the operation failed.
     */
    public void removeEndpoint(String endpointId) throws GovernanceException {
        GovernanceUtils.removeArtifact(registry, endpointId);
    }

    /**
     * Sets content of the given endpoint artifact to the given resource on the
     * registry.
     * 
     * @param endpoint the endpoint artifact.
     * @param endpointResource the content resource.
     * 
     * @throws GovernanceException if the operation failed.
     */
    public void setContent(Endpoint endpoint, Resource endpointResource) throws GovernanceException {
        // set the endpoint url
        String url = ((EndpointImpl)endpoint).getUrl();
        try {
            String content = EndpointUtils.getEndpointContentWithOverview(url, EndpointUtils.deriveEndpointFromUrl(url),
                    EndpointUtils.deriveEndpointNameFromUrl(url), CommonConstants.ENDPOINT_VERSION_DEFAULT_VALUE);
            endpointResource.setContent(content);
        } catch (RegistryException e) {
            String msg =
                    "Error in setting the resource content for endpoint. path: " +
                            endpoint.getPath() + ", " + "id: " + endpoint.getId() + ".";
            log.error(msg);
            throw new GovernanceException(msg, e);
        }
        // and set all the attributes as properties.
        String[] attributeKeys = endpoint.getAttributeKeys();
        if (attributeKeys != null) {
            Properties properties = new Properties();
            for (String attributeKey : attributeKeys) {
                String[] attributeValues = endpoint.getAttributes(attributeKey);
                if (attributeValues != null) {
                    // The list obtained from the Arrays#asList method is
                    // immutable. Therefore,
                    // we create a mutable object out of it before adding it as
                    // a property.
                    properties.put(attributeKey,
                            new ArrayList<String>(Arrays.asList(attributeValues)));
                }
            }
            endpointResource.setProperties(properties);
        }
        endpointResource.setUUID(endpoint.getId());
    }

    /**
     * Finds the endpoint artifact that matches the given URL.
     * 
     * @param url the URL.
     * 
     * @return the endpoint artifact that corresponds.
     * @throws GovernanceException if the operation failed.
     */
    public Endpoint getEndpointByUrl(String url) throws GovernanceException {
        String path = CommonUtil.getEndpointPathFromUrl(url);
        Resource r;
        try {
            if (registry.resourceExists(path)) {
                r = registry.get(path);
            } else {
                return null;
            }
        } catch (RegistryException e) {
            String msg =
                    "Error in retrieving the endpoint resource. url:" + url + ", path:" + path +
                            ".";
            log.error(msg, e);
            throw new GovernanceException(msg, e);
        }
        String artifactId = r.getUUID();
        if (artifactId != null) {
            return getEndpoint(artifactId);
        }
        return null;
    }


    /**
     * Finds all Endpoint artifacts on the registry.
     *
     * @return all Endpoint artifacts on the registry.
     * @throws GovernanceException if the operation failed.
     */
    public Endpoint[] getAllEndpoints() throws GovernanceException {
        List<String> endpointPaths =
                Arrays.asList(GovernanceUtils.getResultPaths(registry,
                        GovernanceConstants.ENDPOINT_MEDIA_TYPE));
        Collections.sort(endpointPaths, new Comparator<String>() {
            public int compare(String o1, String o2) {
                // First order by name
                int result = RegistryUtils.getResourceName(o1).compareToIgnoreCase(
                        RegistryUtils.getResourceName(o2));
                if (result != 0) {
                    return result;
                }
                // Then order by namespace
                return o1.compareToIgnoreCase(o2);
            }
        });
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        for (String endpointPath : endpointPaths) {
            GovernanceArtifact artifact =
                    GovernanceUtils.retrieveGovernanceArtifactByPath(registry, endpointPath);
            endpoints.add((Endpoint) artifact);
        }
        return endpoints.toArray(new Endpoint[endpoints.size()]);
    }
}
