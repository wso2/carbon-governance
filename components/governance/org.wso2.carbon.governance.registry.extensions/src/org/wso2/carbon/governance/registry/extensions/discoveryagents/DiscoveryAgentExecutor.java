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
import org.wso2.carbon.governance.common.GovernanceConfiguration;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsDataHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DiscoveryAgentExecutor {

    public static final String SERVER_RXT_OVERVIEW_TYPE = "overview_type";
    public static final String DISCOVERY_STATUS = "discovery_status";
    public static final String DISCOVERY_STATUS_EXISTING = "existing";
    public static final String DISCOVERY_STATUS_NEW = "new";
    public static final String AGENT_CLASS = "AgentClass";
    private final Log log = LogFactory.getLog(DiscoveryAgentExecutor.class);

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
        Map<String, List<DetachedGenericArtifact>> discoveredArtifacts = agent.discoverArtifacts(serverArtifact);
        try {
            markExistingArtifacts(discoveredArtifacts, getGovRegistry());
        } catch (RegistryException e) {
            throw new DiscoveryAgentException("Exception occurred accessing Registry", e);
        }
        return discoveredArtifacts;
    }

    private void markExistingArtifacts(Map<String, List<DetachedGenericArtifact>> artifacts, Registry registry)
            throws RegistryException {
        for(Map.Entry<String,  List<DetachedGenericArtifact>> entry : artifacts.entrySet()){
            String shortName = entry.getKey();
            GenericArtifactManager manager  = new GenericArtifactManager(registry, shortName);
            for(GenericArtifact artifact : entry.getValue()){
               if(manager.isExists(artifact)){
                   artifact.addAttribute(DISCOVERY_STATUS, DISCOVERY_STATUS_EXISTING);
               } else {
                   artifact.addAttribute(DISCOVERY_STATUS, DISCOVERY_STATUS_NEW);
               }
            }
        }
    }

    private boolean isAgentMapInitialized() {
        return agentMap != null;
    }

    protected void initializeAgentMap() {
        agentMap = new HashMap<>();
        GovernanceConfiguration configuration = GovernanceRegistryExtensionsDataHolder.getInstance().getGovernanceConfiguration();
        for (Map.Entry<String, Map<String, String>> configEntry : configuration.getDiscoveryAgentConfigs().entrySet()) {
            String serverType = configEntry.getKey();
            Map<String, String> properties = configEntry.getValue();
            String agentClass = properties.get(AGENT_CLASS);
            DiscoveryAgent thisAgent = loadDiscoveryAgent(agentClass);
            if (thisAgent != null) {
                thisAgent.init(properties);
                agentMap.put(serverType, thisAgent);
            }

        }
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

    private Registry getGovRegistry() throws RegistryException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        return GovernanceRegistryExtensionsDataHolder.getInstance().getRegistryService().getGovernanceSystemRegistry();
    }
}
