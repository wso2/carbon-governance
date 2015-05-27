package org.wso2.carbon.governance.common;
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

import org.wso2.carbon.base.CarbonBaseUtils;

import java.util.HashMap;
import java.util.Map;

public class GovernanceConfiguration {

    private Map<String, String> discoveryAgentConfigs = new HashMap<String, String>();

    private static GovernanceConfiguration instance = new GovernanceConfiguration();

    private GovernanceConfiguration() {
    }

    public static GovernanceConfiguration getInstance() {
        // Need permissions in order to instantiate ServerConfiguration
        CarbonBaseUtils.checkSecurity();
        return instance;
    }

    public void addDiscoveryAgentConfig(String serverTypeId, String agentCalssName) {
        discoveryAgentConfigs.put(serverTypeId, agentCalssName);
    }

    public Map<String, String> getDiscoveryAgentConfigs() {
        return discoveryAgentConfigs;
    }

    @Override
    public String toString() {
        return "GovernanceConfiguration{" +
               "discoveryAgentConfigs=" + discoveryAgentConfigs +
               '}';
    }
}


