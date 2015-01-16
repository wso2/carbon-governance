/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.governance.api.test;

import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.test.utils.BaseTestCase;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.registry.core.Association;

public class GenericTest extends BaseTestCase {
    public void testSearchGenericArtifactsBeforeAdding() throws Exception {
        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(registry, "application/vnd" +
                                                                                             ".wso2-test+xml",
                                                                                   "test", "org.wso2.samples.test",
                                                                                   null, GovernanceConstants
                .SERVICE_ELEMENT_NAMESPACE, "/@{name}", new Association[0]);
        GenericArtifact[] genericArtifacts = genericArtifactManager.getAllGenericArtifacts();

        assertEquals("Invalid artifact count", 0, genericArtifacts.length);
    }
}
