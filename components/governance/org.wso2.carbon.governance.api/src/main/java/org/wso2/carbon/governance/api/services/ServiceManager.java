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
package org.wso2.carbon.governance.api.services;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.governance.api.cache.ArtifactCache;
import org.wso2.carbon.governance.api.cache.ArtifactCacheManager;
import org.wso2.carbon.governance.api.common.GovernanceArtifactManager;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.services.dataobjects.ServiceImpl;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * This provides the management functionality for service artifacts stored on the registry.
 */
public class ServiceManager {
    private GovernanceArtifactManager manager;
    private Registry registry;
    /**
     * Constructor accepting an instance of the registry to use.
     *
     * @param registry the instance of the registry.
     */
    public ServiceManager(Registry registry) throws RegistryException {
        this(registry, GovernanceConstants.SERVICE_MEDIA_TYPE);

    }

    /**
     * Constructor accepting an instance of the registry, and also details on the type of manager.
     *
     * @param registry  the instance of the registry.
     * @param mediaType the media type of resources being saved or fetched.
     */
    protected ServiceManager(Registry registry, String mediaType) throws RegistryException {
        GovernanceArtifactConfiguration configuration =
                            GovernanceUtils.findGovernanceArtifactConfiguration(
                                    GovernanceConstants.SERVICE_ARTIFACT_KEY, registry);
        this.manager = new GovernanceArtifactManager(
                registry, mediaType,
                configuration.getArtifactNameAttribute(),
                configuration.getArtifactNamespaceAttribute(),
                configuration.getArtifactElementRoot(),
                GovernanceConstants.SERVICE_ELEMENT_NAMESPACE,
                configuration.getPathExpression(), new Association[0]);
        this.registry = registry;

    }

    /**
     * Creates a new service artifact from the given qualified name.
     *
     * @param qName the qualified name of this service.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public Service newService(QName qName) throws GovernanceException {
        ServiceImpl service = new ServiceImpl(manager.newGovernanceArtifact()) {};
        service.setQName(qName);
        return service;
    }

    /**
     * Creates a new service artifact from the given content.
     *
     * @param content the service content.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public Service newService(OMElement content) throws GovernanceException {
        ServiceImpl service = new ServiceImpl(manager.newGovernanceArtifact(content)) {};
        String serviceName = CommonUtil.getServiceName(content);
        String serviceNamespace = CommonUtil.getServiceNamespace(content);
        if (serviceName != null && !serviceName.equals("")) {
            service.setQName(new QName(serviceNamespace, serviceName));
        } else {
            throw new GovernanceException("Unable to compute QName from given XML payload, " +
                    "please ensure that the content passed in matches the configuration.");
        }
        return service;
    }

    /**
     * Adds the given service artifact to the registry. Please do not use this method to update an
     * existing artifact use the update method instead. If this method is used to update an existing
     * artifact, all existing properties (such as lifecycle details) will be removed from the
     * existing artifact.
     *
     * @param service the service artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void addService(Service service) throws GovernanceException {
        manager.addGovernanceArtifact(service);
//            GovernanceUtils.writeOwnerAssociations(registry, service);
//            GovernanceUtils.writeConsumerAssociations(registry, service);
    }

    /**
     * Updates the given service artifact on the registry.
     *
     * @param service the service artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void updateService(Service service) throws GovernanceException {
        manager.updateGovernanceArtifact(service);
        ArtifactCache artifactCache =
                ArtifactCacheManager.getCacheManager().getTenantArtifactCache(((UserRegistry)registry).getTenantId());
        if (artifactCache != null) {
                artifactCache.addArtifact(service.getPath(),service);
        }
    }

    /**
     * Fetches the given service artifact on the registry.
     *
     * @param serviceId the identifier of the service artifact.
     *
     * @return the service artifact.
     * @throws GovernanceException if the operation failed.
     */
    public Service getService(String serviceId) throws GovernanceException {
        GovernanceArtifact governanceArtifact;
        String path = GovernanceUtils.getArtifactPath(registry, serviceId);
        ArtifactCache cache = ArtifactCacheManager.getCacheManager().getTenantArtifactCache(((UserRegistry)registry).getTenantId());
        if (cache != null) {
            governanceArtifact = cache.getArtifact(path);
            if (governanceArtifact != null) {
                return new ServiceImpl(governanceArtifact) {};
            }
        }
        governanceArtifact = manager.getGovernanceArtifact(serviceId);
        if (governanceArtifact == null) {
            return null;
        }
        if (cache != null) {
            cache.addArtifact(governanceArtifact.getPath(), governanceArtifact);
        }
        return new ServiceImpl(governanceArtifact) {};
    }

    /**
     * Removes the given service artifact from the registry.
     *
     * @param serviceId the identifier of the service artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void removeService(String serviceId) throws GovernanceException {
        manager.removeGovernanceArtifact(serviceId);
    }

    /**
     * Finds all service artifacts matching the given filter criteria.
     *
     * @param criteria the filter criteria to be matched.
     *
     * @return the service artifacts that match.
     * @throws GovernanceException if the operation failed.
     */
    public Service[] findServices(ServiceFilter criteria) throws GovernanceException {
        List<Service> services = new ArrayList<Service>();
        for (Service service : getAllServices()) {
            if (service != null) {
                if (criteria.matches(service)) {
                    services.add(service);
                }
            }
        }
        return services.toArray(new Service[services.size()]);
    }
         /**
     * Finds all the service path and used to list services
     *
     * @return all the service paths
     * @throws GovernanceException if the operation failed
     */
    public String[] getAllServicePaths() throws GovernanceException {
        return GovernanceUtils.getResultPaths(registry,
                GovernanceConstants.SERVICE_MEDIA_TYPE);
    }

    /**
     * Finds all service artifacts on the registry.
     *
     * @return all service artifacts on the registry.
     * @throws GovernanceException if the operation failed.
     */
    public Service[] getAllServices() throws GovernanceException {
        return getServices(manager.getAllGovernanceArtifacts());
    }

    // Method to obtain services from governance artifacts.
    private Service[] getServices(GovernanceArtifact[] governanceArtifacts) {
        Service[] services = new Service[governanceArtifacts.length];
        for (int i = 0; i < governanceArtifacts.length; i++) {
            services[i] = new ServiceImpl(governanceArtifacts[i]) {};
        }
        return services;
    }

    /**
     * Finds all identifiers of the service artifacts on the registry.
     *
     * @return an array of identifiers of the service artifacts.
     * @throws GovernanceException if the operation failed.
     */
    public String[] getAllServiceIds() throws GovernanceException {
        return manager.getAllGovernanceArtifactIds();
    }

    /**
     * Retrieve all Services which associated with the given lifecycle
     *
     * @param lcName Name of the lifecycle
     *
     * @return Service array
     * @throws GovernanceException if the operation failed.
     */
    public Service[] getAllServicesByLifecycle(String lcName) throws GovernanceException {
        return (Service[]) manager.getAllGovernanceArtifactsByLifecycle(lcName);
    }

    /**
     * Retrieve all Services which associated with the given lifecycle in the given lifecycle state
     *
     * @param lcName  Name of the lifecycle
     * @param lcState Name of the current lifecycle state
     *
     * @return Service array
     * @throws GovernanceException if the operation failed.
     */
    public Service[] getAllServicesByLifecycleStatus(String lcName, String lcState) throws GovernanceException {
        return (Service[]) manager.getAllGovernanceArtifactsByLIfecycleStatus(lcName, lcState);
    }

}
