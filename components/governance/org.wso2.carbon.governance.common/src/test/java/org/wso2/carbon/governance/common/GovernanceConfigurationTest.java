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

package org.wso2.carbon.governance.common;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.common.utils.GovernanceUtils;

import java.util.List;
import java.util.Map;

public class GovernanceConfigurationTest extends TestCase {

    private static Log log = LogFactory.getLog(GovernanceConfigurationTest.class);


    public void testDiscoveryAgents() throws GovernanceConfigurationException {
        GovernanceConfiguration configuration = GovernanceUtils.getGovernanceConfiguration();
        log.info("configuration ===> "+ configuration);
        assertNotNull(configuration);
        Map<String, Map<String, String>> map =  configuration.getDiscoveryAgentConfigs();
        assertNotNull(map);
        assertTrue(map.containsKey("ESB"));
        assertTrue(map.containsKey("BPS"));
        assertNotNull(map.get("ESB"));
        assertNotNull(map.get("BPS"));
        assertEquals("ESBClass", map.get("ESB").get(GovernanceUtils.AGENT_CLASS));
        assertEquals("BPSClass", map.get("BPS").get(GovernanceUtils.AGENT_CLASS));

    }

    public void testDiscoveryAgentsProperties() throws GovernanceConfigurationException {
        GovernanceConfiguration configuration = GovernanceUtils.getGovernanceConfiguration();
        log.info("configuration ===> "+ configuration);
        assertNotNull(configuration);
        Map<String, Map<String, String>> map =  configuration.getDiscoveryAgentConfigs();
        assertNotNull(map);
        assertTrue(map.containsKey("ESB"));
        assertTrue(map.containsKey("BPS"));
        assertNotNull(map.get("ESB"));
        assertNotNull(map.get("BPS"));
        Map<String, String> properties =  map.get("ESB");
        assertEquals("ESBClass", properties.get(GovernanceUtils.AGENT_CLASS));
        assertEquals("p1-value", properties.get("p1"));
        assertEquals("p2-value", properties.get("p2"));
    }

    public void testComparators() throws GovernanceConfigurationException {
        GovernanceConfiguration configuration = GovernanceUtils.getGovernanceConfiguration();
        log.info("configuration ===> "+ configuration);
        assertNotNull(configuration);
        List<String> list=  configuration.getComparators();
        assertNotNull(list);
        assertTrue(list.contains("TestComparator1"));
        assertTrue(list.contains("TestComparator2"));

    }
}
