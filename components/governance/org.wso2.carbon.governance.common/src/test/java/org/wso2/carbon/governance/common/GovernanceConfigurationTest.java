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

import java.util.Map;

public class GovernanceConfigurationTest extends TestCase {

    private static Log log = LogFactory.getLog(GovernanceConfigurationTest.class);


    public void testGetGovernanceConfiguration() throws GovernanceConfigurationException {
        GovernanceConfiguration configuration = GovernanceUtils.getGovernanceConfiguration();
        log.info("====== configuration === "+ configuration);
        assertNotNull(configuration);
        Map<String, String> map =  configuration.getDiscoveryAgentConfigs();
        assertNotNull(map);
        assertTrue(map.containsKey("ESB"));
        assertTrue(map.containsKey("BPS"));
        assertNotNull(map.get("ESB"));
        assertNotNull(map.get("BPS"));
        assertEquals("ESBClass", map.get("ESB"));
        assertEquals("BPSClass", map.get("BPS"));

    }
}
