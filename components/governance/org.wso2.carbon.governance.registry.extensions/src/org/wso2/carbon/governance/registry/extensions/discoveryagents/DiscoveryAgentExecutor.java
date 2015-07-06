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
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.DetachedGenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.common.GovernanceConfiguration;
import org.wso2.carbon.governance.common.utils.GovernanceUtils;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsDataHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class DiscoveryAgentExecutor {

    public static final String SERVER_RXT_OVERVIEW_TYPE = "overview_type";
    private final Log log = LogFactory.getLog(DiscoveryAgent.class);

    private static DiscoveryAgentExecutor executor = new DiscoveryAgentExecutor();

    private Map<String, DiscoveryAgent> agentMap;

    public static DiscoveryAgentExecutor getInstance() {
        return executor;
    }

    public Map<String, List<DetachedGenericArtifact>> executeDiscoveryAgent(GenericArtifact serverArtifact)
            throws DiscoveryAgentException {
        if (!isAgentMapInitialized()) {
            initializeAgentMap();
        }
        String serverTypeId = getServerTypeId(serverArtifact);
        DiscoveryAgent agent = findDiscoveryAgent(serverTypeId);
        return agent.discoverArtifacts(serverArtifact);
    }

    private boolean isAgentMapInitialized() {
        if (agentMap != null) {
            return true;
        } else {
            return false;
        }
    }

    protected void initializeAgentMap() {
        agentMap = new HashMap<>();
        GovernanceConfiguration configuration = GovernanceRegistryExtensionsDataHolder.getInstance().getGovernanceConfiguration();
        for (Map.Entry<String, String> configEntry : configuration.getDiscoveryAgentConfigs().entrySet()) {
            String serverType = configEntry.getKey();
            String agentClass = configEntry.getValue();
            DiscoveryAgent thisAgent = loadDiscoveryAgent(agentClass);
            if (thisAgent != null) {
                thisAgent.init(getAgentProperties(serverType));
                agentMap.put(serverType, thisAgent);
            }

        }
    }

    private Properties getAgentProperties(String serverTypeId) {
        String file = getAgentPropertyFile(serverTypeId);
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
        } catch (IOException e) {
            log.warn("Can't find property file for agent " + serverTypeId, e);
        }
        return properties;
    }


    private String getAgentPropertyFile(String serverTypeId) {
        StringBuilder builder = new StringBuilder();
        builder.append(GovernanceUtils.getCarbonConfigDirPath());
        builder.append(File.separator);
        builder.append(serverTypeId);
        builder.append(".properties");
        return builder.toString();
    }

    private DiscoveryAgent loadDiscoveryAgent(String agentClass) {
        try {
            Class clazz = getClass().getClassLoader().loadClass(agentClass);
            return (DiscoveryAgent) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            log.error("Can't load DiscoveryAgent class " + agentClass);
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Can't Instantiate DiscoveryAgent class " + agentClass);
        }
        return null;
    }

    private DiscoveryAgent findDiscoveryAgent(String serverTypeId) throws DiscoveryAgentException {
        DiscoveryAgent agent = agentMap.get(serverTypeId);
        if (agent == null) {
            throw new DiscoveryAgentException("Can't find DiscoveryAgent associated to server type :" + serverTypeId);
        }
        return agent;
    }

    private String getServerTypeId(GenericArtifact serverArtifact) throws DiscoveryAgentException {
        String serverTypeId;
        try {
            serverTypeId = serverArtifact.getAttribute(SERVER_RXT_OVERVIEW_TYPE);
            if (serverTypeId == null) {
                throw new DiscoveryAgentException("ServerTypeId value is null, can't proceed");
            }
            return serverTypeId;
        } catch (GovernanceException e) {
            throw new DiscoveryAgentException("ServerTypeId issue", e);
        }

    }
}
