/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
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
import org.wso2.carbon.ntask.core.Task;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public class DiscoveryAgentExecutorTask implements Task {

    public static final String RESOURCE_SOURCE_PROPERTY = "resource.source";
    public static final String SOURCE_GREG_DISCOVERY = "greg-discovery";
    public static final String RESOURCE_ORIGIN_PROPERTY = "resource.origin";
    public static final String RESOURCE_DISCOVERY_SEQNO_PROPERTY = "resource.discovery_seqno";
    public static final String NAME_VERSION_SEPARATER = ":";
    private final Log log = LogFactory.getLog(DiscoveryAgentExecutorTask.class);

    public static final String ON_ORPHAN_ARTIFACT_PROPERTY = "onOrphanArtifact";
    public static final String ON_EXIST_ARTIFACT_PROPERTY = "onExistArtifact";

    public static final String CONFIG_FILE_PARAMETER = "configFile";
    public static final String DEFAULT_CONFIG_FILE = "discoveryagent.properties";
    public static final String SERVERS_PROPERTY = "servers";
    //TODO - load mediaType from RXT itself.
    public static final String SERVER_RXT_SHORT_NAME = "server";
    public static final String SERVER_RXT_OVERVIEW_NAME = "overview_name";
    private static final String SERVER_RXT_OVERVIEW_VERSION = "overview_version";
    public static final String SERVER_ID_SEPARATER = ",";
    private final String CONFIG_FILE_LOCATION = "repository/components/org.wso2.carbon.governance/discoveryagents/";
    private String serverIdPropertyFilePath;
    private OrphanArtifactStrategy onOrphanArtifactStrategy;
    private ExistArtifactStrategy onExistArtifactStrategy;


    @Override
    public void setProperties(Map<String, String> properties) {
        setConfigFileLocation(properties);
        setOrphanArtifactStrategy(properties);
        setExistArtifactStrategy(properties);
    }

    @Override
    public void init() {
    }

    @Override
    public void execute() {
        log.info("DiscoveryAgentExecutorTask started ....");
        try {
            Registry govRegistry = getGovRegistry();
            DiscoveryAgentExecutor discoveryAgentExecutor = DiscoveryAgentExecutor.getInstance();
            for (GenericArtifact serverArtifact : loadServersToDiscover(govRegistry)) {
                log.info("Started to discover new governance artifacts on " + serverArtifact.getQName() + " server");
                Map<String, List<DetachedGenericArtifact>> newArtifacts = discoveryAgentExecutor.executeDiscoveryAgent(serverArtifact);
                String originProperty = getOriginProperty(serverArtifact);
                String seqNo = getSequnceNo();
                persistDiscoveredArtifacts(govRegistry, newArtifacts, serverArtifact, seqNo, originProperty);
                //TODO
                //handleOrphanArtifacts(govRegistry, newArtifacts.keySet(), seqNo, originProperty);
            }
            log.info("DiscoveryAgentExecutorTask completed ....");
        } catch (DiscoveryAgentException | RegistryException | IOException e) {
            log.error("Exception occurred running DiscoveryAgentExecutorTask ", e);
        }
    }

    protected List<GenericArtifact> loadServersToDiscover(Registry registry)
            throws RegistryException, IOException {
        List<GenericArtifact> serverArtifacts = new ArrayList<>();
        GenericArtifactManager artifactManager = getGenericArtifactManager(registry, SERVER_RXT_SHORT_NAME);
        for (String serverId : getServersToDiscover(getConfigRegistry())) {
            Map<String, List<String>> options = new HashMap<>();
            setServerDiscoverOptions(options, serverId);
            GenericArtifact[] serverArtifactArray = artifactManager.findGenericArtifacts(options);
            serverArtifacts.addAll(Arrays.asList(serverArtifactArray));
        }
        return serverArtifacts;
    }

    protected List<String> getServersToDiscover(Registry registry) throws RegistryException, IOException {
        Resource resource = registry.get(serverIdPropertyFilePath);
        if (resource != null) {
            Properties serverProperties = new Properties();
            serverProperties.load(resource.getContentStream());
            String serverStr = serverProperties.getProperty(SERVERS_PROPERTY);
            if (serverStr != null) {
                String[] servers = serverStr.split(SERVER_ID_SEPARATER);
                return Arrays.asList(servers);
            }
        }
        return Collections.emptyList();
    }

    protected void setServerDiscoverOptions(Map<String, List<String>> options, String serverId) {
        if (serverId != null) {
            int index = serverId.indexOf(NAME_VERSION_SEPARATER);
            if (index > 0) {
                String serverName = serverId.substring(0, index);
                String serverVersion = serverId.substring(index);
                options.put(SERVER_RXT_OVERVIEW_NAME, Arrays.asList(serverName));
                options.put(SERVER_RXT_OVERVIEW_VERSION, Arrays.asList(serverVersion));
            } else {
                options.put(SERVER_RXT_OVERVIEW_NAME, Arrays.asList(serverId));
            }
        }
    }

    private void updateMaintenanceInfo(GenericArtifact artifact, String seqNo, String originProperty)
            throws GovernanceException {
        artifact.addAttribute(RESOURCE_SOURCE_PROPERTY, SOURCE_GREG_DISCOVERY);
        artifact.addAttribute(RESOURCE_ORIGIN_PROPERTY, originProperty);
        artifact.addAttribute(RESOURCE_DISCOVERY_SEQNO_PROPERTY, seqNo);
    }

    private void persistDiscoveredArtifacts(Registry registry, Map<String, List<DetachedGenericArtifact>> newArtifacts,
                                            GenericArtifact serverArtifact, String seqNo,
                                            String originProperty)
            throws RegistryException {
        for (Map.Entry<String, List<DetachedGenericArtifact>> artifactEntry : newArtifacts.entrySet()) {
            String shortName = artifactEntry.getKey();
            List<DetachedGenericArtifact> artifacts = artifactEntry.getValue();
            GenericArtifactManager artifactManager = getGenericArtifactManager(registry, shortName);
            for (DetachedGenericArtifact artifact : artifacts) {
                persistNewArtifact(artifactManager, artifact, serverArtifact, seqNo, originProperty);
            }
        }

    }

    private void persistNewArtifact(GenericArtifactManager artifactManager, DetachedGenericArtifact artifact,
                                    GenericArtifact server, String seqNo,
                                    String originProperty)
            throws GovernanceException {
        if (artifactManager.isExists(artifact)) {
            switch (onExistArtifactStrategy) {
                case IGNORE:
                    log.info("Ignored already existing artifact" + artifact);
                    break;
                case REMOVE:
                    //If artifact is already exists, delete and then add new artifact.
                    artifactManager.removeGenericArtifact(artifact);
                    log.info("Removed already existing artifact" + artifact);
                    addNewGenericArtifact(artifactManager, artifact, server, seqNo, originProperty);
                    break;
                case CUSTOM:
                    customizeExistArtifactStrategy(artifactManager, artifact, seqNo, originProperty);
                    break;
            }
        } else {
            addNewGenericArtifact(artifactManager, artifact, server, seqNo, originProperty);
        }
    }

    private GenericArtifact addNewGenericArtifact(GenericArtifactManager artifactManager,
                                                  DetachedGenericArtifact artifact, GenericArtifact server,
                                                  String seqNo, String originProperty)
            throws GovernanceException {
        GenericArtifact newArtifact = artifact.makeRegistryAware(artifactManager);
        updateMaintenanceInfo(newArtifact, seqNo, originProperty);
        artifactManager.addGenericArtifact(newArtifact);
        addServerToArtifactAssociation(newArtifact, server);
        return newArtifact;
    }

    private void addServerToArtifactAssociation(GenericArtifact source,
                                                GenericArtifact destination) throws GovernanceException {
        source.addBidirectionalAssociation("avialbleOn", "contains", destination);
    }


    protected void handleOrphanArtifacts(Registry registry, Set<String> shortNames, String seqNo, String originProperty)
            throws RegistryException {
        switch (onOrphanArtifactStrategy) {
            case IGNORE:
                log.info("Ignored handling orphan artifact");
                break;
            case REMOVE:
                for (String shortName : shortNames) {
                    removeOrphanArtifacts(getGenericArtifactManager(registry, shortName), seqNo, originProperty);
                }
                break;
            case CUSTOM:
                for (String shortName : shortNames) {
                    customizeOrphanArtifactStrategy(getGenericArtifactManager(registry, shortName), seqNo, originProperty);
                }
                break;
        }
    }

    protected void removeOrphanArtifacts(GenericArtifactManager genericArtifactManager, String seqNo,
                                         String originProperty) throws RegistryException {
        for (GenericArtifact artifact : findOrphanArtifacts(genericArtifactManager, seqNo, originProperty)) {
            removeDerivedAssociations(artifact);
            genericArtifactManager.removeGenericArtifact(artifact.getId());
            log.info("Removed orphan artifact belong to " + originProperty + " server : " + artifact);
        }
    }

    private void removeDerivedAssociations(GenericArtifact artifact) {
        //TODO
    }

    private List<GenericArtifact> findOrphanArtifacts(GenericArtifactManager genericArtifactManager, String seqNo,
                                                      String originProperty) throws GovernanceException {
        List<GenericArtifact> orphanArtifacts = new ArrayList<>();
        Map<String, List<String>> options = new HashMap<>();
        options.put("propertyName", Arrays.asList("resource.origin"));
        options.put("rightOp", Arrays.asList("eq"));
        options.put("rightPropertyValue", Arrays.asList(originProperty));
        Registry govRegistry = null;
        try {
            govRegistry = getGovRegistry();
        } catch (RegistryException e) {
            throw new GovernanceException(e);
        }
        if (govRegistry != null) {
            for (GenericArtifact artifact : genericArtifactManager.findGenericArtifacts(options)) {
                Resource resource = null;
                try {
                    resource = govRegistry.get(artifact.getPath());
                } catch (RegistryException e) {
                    //We still have to check other artifacts so continue...
                }
                if (resource != null) {
                    String currentSeqNo = resource.getProperty("resource.discovery_seqno");
                    if (currentSeqNo == null || !seqNo.equals(currentSeqNo)) {
                        orphanArtifacts.add(artifact);
                    }
                }
            }
        }
        return orphanArtifacts;
    }

    private void customizeOrphanArtifactStrategy(GenericArtifactManager genericArtifactManager, String seqNo,
                                                 String originProperty) {
        throw new UnsupportedOperationException("Override customizeOrphanArtifactStrategy method in a subclass of" +
                                                " DiscoveryAgentExecutorTask  ");
    }

    protected void customizeExistArtifactStrategy(GenericArtifactManager artifactManager, GenericArtifact artifact,
                                                  String seqNo, String originProperty) throws GovernanceException {

        throw new UnsupportedOperationException("Override customizeExistArtifactStrategy method in a subclass of" +
                                                " DiscoveryAgentExecutorTask  ");
    }

    private void setExistArtifactStrategy(Map<String, String> properties) {
        String existProperty = properties.get(ON_EXIST_ARTIFACT_PROPERTY);
        onExistArtifactStrategy = ExistArtifactStrategy.valueOf(existProperty.toUpperCase());
    }

    private void setOrphanArtifactStrategy(Map<String, String> properties) {
        String orphanProperty = properties.get(ON_ORPHAN_ARTIFACT_PROPERTY);
        onOrphanArtifactStrategy = OrphanArtifactStrategy.valueOf(orphanProperty.toUpperCase());
    }

    private void setConfigFileLocation(Map<String, String> properties) {
        String configFile = properties.get(CONFIG_FILE_PARAMETER);
        if (configFile != null) {
            serverIdPropertyFilePath = CONFIG_FILE_LOCATION.concat(configFile);
        } else {
            serverIdPropertyFilePath = CONFIG_FILE_LOCATION.concat(DEFAULT_CONFIG_FILE);
        }
    }

    private String getOriginProperty(GenericArtifact serverArtifact) throws GovernanceException {
        String serverName = serverArtifact.getAttribute(SERVER_RXT_OVERVIEW_NAME);
        String serverVersion = serverArtifact.getAttribute(SERVER_RXT_OVERVIEW_VERSION);
        if (serverVersion != null) {
            serverName = serverName.concat(serverVersion);
        }
        return serverName;
    }

    private Registry getConfigRegistry() throws RegistryException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        return GovernanceRegistryExtensionsDataHolder.getInstance().getRegistryService().getConfigSystemRegistry();
    }

    private Registry getGovRegistry() throws RegistryException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        return GovernanceRegistryExtensionsDataHolder.getInstance().getRegistryService().getGovernanceSystemRegistry();
    }

    private String getSequnceNo() {
        return UUID.randomUUID().toString();
    }

    private GenericArtifactManager getGenericArtifactManager(Registry registry, String mediaType)
            throws RegistryException {
        return new GenericArtifactManager(registry, mediaType);
    }
}
