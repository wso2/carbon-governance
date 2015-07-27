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
import org.wso2.carbon.governance.api.generic.dataobjects.DetachedGenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCompositDiscoveryAgent extends AbstractDiscoveryAgent {

    private static final Log log = LogFactory.getLog(AbstractCompositDiscoveryAgent.class);


    private List<DiscoveryAgent> discoveryAgents;


    @Override
    public void init(Map properties) {
        super.init(properties);
        if (discoveryAgents == null) {
            initializeDiscoveryAgents();
        }
    }

    private void initializeDiscoveryAgents() {
        discoveryAgents = new ArrayList<>();
        for (Class<DiscoveryAgent> agentClass : getDiscoveryAgentClasses()) {
            DiscoveryAgent agent = loadDiscoveryAgent(agentClass);
            discoveryAgents.add(agent);
        }
    }

    @Override
    public void close(Map properties) {
        super.close(properties);
    }

    @Override
    public Map<String, List<DetachedGenericArtifact>> discoverArtifacts(GenericArtifact server) {
        Map<String, List<GenericArtifact>> genericArtifact = new HashMap<>();
        return merge();
    }

    private Map<String, List<DetachedGenericArtifact>> merge() {
        //TODO - implemant
        return null;
    }

    abstract List<Class<DiscoveryAgent>> getDiscoveryAgentClasses();

    private DiscoveryAgent loadDiscoveryAgent(Class<DiscoveryAgent> agentClass) {
        try {
            return agentClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Can't Instantiate DiscoveryAgent class " + agentClass.getName());
        }
        return null;
    }
}
