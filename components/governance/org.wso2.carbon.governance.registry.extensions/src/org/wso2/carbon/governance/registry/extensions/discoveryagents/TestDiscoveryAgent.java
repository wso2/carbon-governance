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

import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.DetachedGenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDiscoveryAgent extends AbstractDiscoveryAgent {

    @Override
    public Map<String, List<DetachedGenericArtifact>> discoverArtifacts(GenericArtifact server)
            throws DiscoveryAgentException {
        return this.discoverArtifactsInternal(server);
    }


    private Map<String, List<DetachedGenericArtifact>> discoverArtifactsInternal(GenericArtifact server)
            throws DiscoveryAgentException {
        Map<String, List<DetachedGenericArtifact>> map = new HashMap<>();
        List<DetachedGenericArtifact> restArtifacts = new ArrayList<>();
        List<DetachedGenericArtifact> soapArtifacts = new ArrayList<>();

        for (int i = 1; i < 11; i++) {
            DetachedGenericArtifact artifact = (DetachedGenericArtifact) GenericArtifactManager
                    .newDetachedGovernanceArtifact(new QName("TestRESTService" + i),
                                                   "application/vnd.wso2-restservice+xml");
            try {
                artifact.setAttribute("overview_name", "TestRESTService" + i);
                artifact.setAttribute("overview_provider", "admin");
                artifact.setAttribute("overview_context", "/test2");
                artifact.setAttribute("overview_version", "1.0.0");
            } catch (GovernanceException e) {
                throw new DiscoveryAgentException("Exception setting requred attributes", e);
            }
            restArtifacts.add(artifact);
        }
        for (int i = 1; i < 7; i++) {
            DetachedGenericArtifact artifact = (DetachedGenericArtifact) GenericArtifactManager
                    .newDetachedGovernanceArtifact(new QName("TestSOAPService" + i),
                                                   "application/vnd.wso2-soapservice+xml");
            try {
                artifact.setAttribute("overview_name", "TestSOAPService" + i);
                artifact.setAttribute("overview_version", "1.0.0");
            } catch (GovernanceException e) {
                throw new DiscoveryAgentException("Exception setting requred attributes", e);
            }
            soapArtifacts.add(artifact);
        }
        map.put("restservice", restArtifacts);
        map.put("soapservice", soapArtifacts);

        return map;
    }
}
