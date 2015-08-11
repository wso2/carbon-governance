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
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.DetachedGenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
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

public class DiscoveryAgentExecutorTask extends DiscoveryAgentExecutorSupport implements Task {

    private final Log log = LogFactory.getLog(DiscoveryAgentExecutorTask.class);


    private final String CONFIG_FILE_LOCATION = "repository/components/org.wso2.carbon.governance/discoveryagents/";

    private String serverIdPropertyFilePath;


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
                String seqNo = getSequenceNo();
                Map<String, List<String>>  feedback = persistDiscoveredArtifacts(govRegistry, newArtifacts,
                                                                              serverArtifact, seqNo, originProperty);
                printFeedback(feedback);
                handleOrphanArtifacts(govRegistry, newArtifacts, seqNo, originProperty);
            }
            log.info("DiscoveryAgentExecutorTask completed ....");
        } catch (DiscoveryAgentException | RegistryException | IOException e) {
            log.error("Exception occurred running DiscoveryAgentExecutorTask ", e);
        }
    }

    private void printFeedback(Map<String, List<String>> feedback) {
        //TODO
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
                String[] servers = serverStr.split(SERVER_ID_SEPARATOR);
                return Arrays.asList(servers);
            }
        }
        return Collections.emptyList();
    }

    protected void setServerDiscoverOptions(Map<String, List<String>> options, String serverId) {
        if (serverId != null) {
            int index = serverId.indexOf(NAME_VERSION_SEPARATOR);
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

    protected void setConfigFileLocation(Map<String, String> properties) {
        String configFile = properties.get(CONFIG_FILE_PARAMETER);
        if (configFile != null) {
            serverIdPropertyFilePath = CONFIG_FILE_LOCATION.concat(configFile);
        } else {
            serverIdPropertyFilePath = CONFIG_FILE_LOCATION.concat(DEFAULT_CONFIG_FILE);
        }
    }

}
