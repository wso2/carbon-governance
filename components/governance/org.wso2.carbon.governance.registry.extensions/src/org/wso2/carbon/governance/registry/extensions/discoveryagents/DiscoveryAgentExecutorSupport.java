/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.wso2.carbon.governance.registry.extensions.discoveryagents;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.DetachedGenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsDataHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class DiscoveryAgentExecutorSupport {

    public static final String RESOURCE_SOURCE_PROPERTY = "resource.source";
    public static final String SOURCE_GREG_DISCOVERY = "greg-discovery";
    public static final String RESOURCE_ORIGIN_PROPERTY = "resource.origin";
    public static final String RESOURCE_DISCOVERY_SEQNO_PROPERTY = "resource.discovery_seqno";
    public static final String NAME_VERSION_SEPARATOR = ":";

    public static final String ON_ORPHAN_ARTIFACT_PROPERTY = "onOrphanArtifact";
    public static final String ON_EXIST_ARTIFACT_PROPERTY = "onExistArtifact";

    public static final String CONFIG_FILE_PARAMETER = "configFile";
    public static final String DEFAULT_CONFIG_FILE = "discoveryagent.properties";
    public static final String SERVERS_PROPERTY = "servers";
    public static final String SERVER_RXT_SHORT_NAME = "server";
    public static final String SERVER_RXT_OVERVIEW_NAME = "overview_name";
    public static final String SERVER_RXT_OVERVIEW_VERSION = "overview_version";
    public static final String SERVER_ID_SEPARATOR = ",";
    public static final String ARTIFACT_ADDED = "added";
    public static final String ARTIFACT_REMOVED = "removed";
    public static final String ARTIFACT_REPLACED = "replaced";
    public static final String ARTIFACT_IGNORED = "ignored";
    public static final String RESOURCE_DISCOVERY_SEQNO = "resource.discovery_seqno";
    public static final String ASSOCIATION_AVAILABLE_ON = "availableOn";
    public static final String ASSOCIATION_CONTAINS = "contains";

    private final Log log = LogFactory.getLog(DiscoveryAgentExecutorSupport.class);


    private OrphanArtifactStrategy onOrphanArtifactStrategy;
    private ExistArtifactStrategy onExistArtifactStrategy;


    protected void updateMaintenanceInfo(GenericArtifact artifact, String seqNo, String originProperty)
            throws GovernanceException {
        artifact.addAttribute(RESOURCE_SOURCE_PROPERTY, SOURCE_GREG_DISCOVERY);
        artifact.addAttribute(RESOURCE_ORIGIN_PROPERTY, originProperty);
        artifact.addAttribute(RESOURCE_DISCOVERY_SEQNO_PROPERTY, seqNo);
    }

    protected Map<String, List<String>> persistDiscoveredArtifacts(Registry registry,
                                                                   Map<String, List<DetachedGenericArtifact>> newArtifacts,
                                                                   GenericArtifact serverArtifact, String seqNo,
                                                                   String originProperty)
            throws RegistryException {
        Map<String, List<String>> feedback = initFeedbackMap();
        for (Map.Entry<String, List<DetachedGenericArtifact>> artifactEntry : newArtifacts.entrySet()) {
            String shortName = artifactEntry.getKey();
            List<DetachedGenericArtifact> artifacts = artifactEntry.getValue();
            GenericArtifactManager artifactManager = getGenericArtifactManager(registry, shortName);
            for (DetachedGenericArtifact artifact : artifacts) {
                persistNewArtifact(artifactManager, artifact, shortName, serverArtifact, seqNo, originProperty, feedback);
            }
        }
        return feedback;
    }

    protected void persistNewArtifact(GenericArtifactManager artifactManager, DetachedGenericArtifact artifact,
                                      String shortName, GenericArtifact server, String seqNo,
                                      String originProperty, Map<String, List<String>> feedback)
            throws GovernanceException {
        if (isExists(artifactManager, artifact)) {
            switch (onExistArtifactStrategy) {
                case IGNORE:
                    log.info("Ignored already existing artifact" + artifact);
                    feedback.get(ARTIFACT_IGNORED).add(shortName + ":" + artifact.getQName().getLocalPart());
                    break;
                case REMOVE:
                    //If artifact is already exists, delete and then add new artifact.
                    artifactManager.removeGenericArtifact(artifact);
                    log.info("Removed already existing artifact" + artifact);
                    addNewGenericArtifact(artifactManager, artifact, server, seqNo, originProperty);
                    feedback.get(ARTIFACT_REPLACED).add(shortName + ":" + artifact.getQName().getLocalPart());
                    break;
                case CUSTOM:
                    customizeExistArtifactStrategy(artifactManager, artifact, seqNo, originProperty);
                    break;
            }
        } else {
            addNewGenericArtifact(artifactManager, artifact, server, seqNo, originProperty);
            feedback.get(ARTIFACT_ADDED).add(shortName + ":" + artifact.getQName().getLocalPart());
        }
    }

    private boolean isExists(GenericArtifactManager artifactManager, DetachedGenericArtifact artifact)
            throws GovernanceException {
        String status = artifact.getAttribute(DiscoveryAgentExecutor.DISCOVERY_STATUS);
        if (status != null) {
            if (DiscoveryAgentExecutor.DISCOVERY_STATUS_EXISTING.equals(status)) {
                artifact.removeAttribute(DiscoveryAgentExecutor.DISCOVERY_STATUS_EXISTING);
                return true;
            }
        } else {
            return artifactManager.isExists(artifact);
        }
        return false;
    }

    private Map<String, List<String>> initFeedbackMap() {
        Map<String, List<String>> map = new HashMap<>();
        map.put(ARTIFACT_ADDED, new ArrayList<String>());
        map.put(ARTIFACT_REMOVED, new ArrayList<String>());
        map.put(ARTIFACT_REPLACED, new ArrayList<String>());
        map.put(ARTIFACT_IGNORED, new ArrayList<String>());
        return map;
    }

    protected GenericArtifact addNewGenericArtifact(GenericArtifactManager artifactManager,
                                                    DetachedGenericArtifact artifact, GenericArtifact server,
                                                    String seqNo, String originProperty)
            throws GovernanceException {
        GenericArtifact newArtifact = artifact.makeRegistryAware(artifactManager);
        updateMaintenanceInfo(newArtifact, seqNo, originProperty);
        artifactManager.addGenericArtifact(newArtifact);
        addServerToArtifactAssociation(newArtifact, server);
        return newArtifact;
    }

    protected void addServerToArtifactAssociation(GenericArtifact source,
                                                  GenericArtifact destination) throws GovernanceException {
        source.addBidirectionalAssociation(ASSOCIATION_AVAILABLE_ON, ASSOCIATION_CONTAINS, destination);
    }


    protected void handleOrphanArtifacts(Registry registry, Map<String, List<DetachedGenericArtifact>> discovredArtifacts, String seqNo, String originProperty)
            throws RegistryException {
        switch (onOrphanArtifactStrategy) {
            case IGNORE:
                log.info("Ignored handling orphan artifact");
                break;
            case REMOVE:
                for (Map.Entry<String, List<DetachedGenericArtifact>> entry : discovredArtifacts.entrySet()) {
                    removeOrphanArtifacts(registry, entry.getKey(), entry.getValue(), seqNo, originProperty);
                }
                break;
            case CUSTOM:
                for (Map.Entry<String, List<DetachedGenericArtifact>> entry : discovredArtifacts.entrySet()) {
                    customizeOrphanArtifactStrategy(registry, entry.getKey(), entry.getValue(), seqNo, originProperty);
                }
                break;
        }
    }

    protected void removeOrphanArtifacts(Registry registry, String shortName,
                                         List<DetachedGenericArtifact> artifacts, String
            seqNo, String originProperty) throws RegistryException {
        GenericArtifactManager genericArtifactManager = getGenericArtifactManager(registry, shortName);
        for (GenericArtifact artifact : findOrphanArtifacts(genericArtifactManager, seqNo, originProperty)) {
            /*
            NOTE - Though we updated 'seqNo" property in just discovered artifacts immediate Registry search may give
            incorrect results due to the fact that indexer run as an asynchronous job. To skip artifacts which
            are already updated but yet to be updated in indexer, it's required to perform  isDiscoveredArtifact()
            check.
             */
            if (!isCurrentlyDiscoveredArtifact(artifact, artifacts)) {
                genericArtifactManager.removeGenericArtifact(artifact.getId());
                log.info("Removed orphan artifact belong to " + originProperty + " server : " + artifact);
            }
        }
    }

    private boolean isCurrentlyDiscoveredArtifact(GenericArtifact artifact, List<DetachedGenericArtifact> artifacts) {
        for(DetachedGenericArtifact detachedGenericArtifact : artifacts){
            if(artifact.uniqueTo(detachedGenericArtifact)){
                return true;
            }
        }
        return false;
    }

    protected List<GenericArtifact> findOrphanArtifacts(GenericArtifactManager genericArtifactManager, String seqNo,
                                                        String originProperty) throws GovernanceException {
        List<GenericArtifact> orphanArtifacts = new ArrayList<>();
        //resource.origin = originProperty
        String query = RESOURCE_ORIGIN_PROPERTY + "=" + originProperty;
        Registry govRegistry;
        try {
            govRegistry = getGovRegistry();
        } catch (RegistryException e) {
            throw new GovernanceException(e);
        }
        if (govRegistry != null) {
            for (GenericArtifact artifact : genericArtifactManager.findGovernanceArtifacts(query)) {
                Resource resource = null;
                try {
                    resource = govRegistry.get(artifact.getPath());
                } catch (RegistryException e) {
                    //We still have to check other artifacts so continue...
                    log.error(e);
                }
                if (resource != null) {
                    String currentSeqNo = resource.getProperty(RESOURCE_DISCOVERY_SEQNO);
                    if (currentSeqNo == null || !seqNo.equals(currentSeqNo)) {
                        orphanArtifacts.add(artifact);
                    }
                }
            }
        }
        return orphanArtifacts;
    }

    protected void customizeOrphanArtifactStrategy(Registry registry, String shortName,
                                                   List<DetachedGenericArtifact> artifacts, String seqNo,
                                                   String originProperty) {
        throw new UnsupportedOperationException("Override customizeOrphanArtifactStrategy method in a subclass of" +
                                                " DiscoveryAgentExecutorTask  ");
    }

    protected void customizeExistArtifactStrategy(GenericArtifactManager artifactManager, GenericArtifact artifact,
                                                  String seqNo, String originProperty) throws GovernanceException {

        throw new UnsupportedOperationException("Override customizeExistArtifactStrategy method in a subclass of" +
                                                " DiscoveryAgentExecutorTask  ");
    }

    protected void setExistArtifactStrategy(Map<String, String> properties) {
        String existProperty = properties.get(ON_EXIST_ARTIFACT_PROPERTY);
        onExistArtifactStrategy = ExistArtifactStrategy.valueOf(existProperty.toUpperCase());
    }

    protected void setOrphanArtifactStrategy(Map<String, String> properties) {
        String orphanProperty = properties.get(ON_ORPHAN_ARTIFACT_PROPERTY);
        onOrphanArtifactStrategy = OrphanArtifactStrategy.valueOf(orphanProperty.toUpperCase());
    }

    protected void setExistArtifactStrategy(ExistArtifactStrategy existArtifactStrategy) {
        this.onExistArtifactStrategy = existArtifactStrategy;
    }

    protected void setOrphanArtifactStrategy(OrphanArtifactStrategy orphanArtifactStrategy) {
        this.onOrphanArtifactStrategy = orphanArtifactStrategy;
    }


    protected String getOriginProperty(GenericArtifact serverArtifact) throws GovernanceException {
        String serverName = serverArtifact.getAttribute(SERVER_RXT_OVERVIEW_NAME);
        String serverVersion = serverArtifact.getAttribute(SERVER_RXT_OVERVIEW_VERSION);
        if (serverVersion != null) {
            serverName = serverName.concat(serverVersion);
        }
        return serverName;
    }

    protected Registry getConfigRegistry() throws RegistryException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        return GovernanceRegistryExtensionsDataHolder.getInstance().getRegistryService().getConfigSystemRegistry();
    }

    protected Registry getGovRegistry() throws RegistryException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        return GovernanceRegistryExtensionsDataHolder.getInstance().getRegistryService().getGovernanceSystemRegistry();
    }

    protected String getSequenceNo() {
        return UUID.randomUUID().toString();
    }

    protected GenericArtifactManager getGenericArtifactManager(Registry registry, String mediaType)
            throws RegistryException {
        return new GenericArtifactManager(registry, mediaType);
    }


}
