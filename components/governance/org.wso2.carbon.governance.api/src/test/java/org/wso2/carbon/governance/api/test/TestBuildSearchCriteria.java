/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.governance.api.test;

import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.test.utils.BaseTestCase;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class TestBuildSearchCriteria extends BaseTestCase {

    public void testBuildSearchCriteria() throws Exception {

        String val = getStringFromInputStream(this.getClass().getClassLoader().getResourceAsStream("uri.rxt"));
        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("uri.rxt"));
        registry.put("/uri", resource);
        GovernanceArtifactConfiguration governanceArtifactConfiguration =
                GovernanceUtils.getGovernanceArtifactConfiguration(val);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);

        try {
            GovernanceUtils.findGovernanceArtifacts(
                    "name=SampleURI2&version=1.2" +
                            ".3&tags=123&updater!=admin&author!=admin&mediaType!=admin&associationDest=admin&comments" +
                            "=123",
                    registry, governanceArtifactConfiguration.getMediaType());
        } catch (GovernanceException e) {
            assertEquals("Attribute Search Service not Found", e.getMessage());
            assertNull(e.getCause());
        }

    }

    public void testBuildSearchCriteria2() throws Exception {

        String val = getStringFromInputStream(this.getClass().getClassLoader().getResourceAsStream("uri.rxt"));
        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("uri.rxt"));
        registry.put("/uri", resource);
        GovernanceArtifactConfiguration governanceArtifactConfiguration =
                GovernanceUtils.getGovernanceArtifactConfiguration(val);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);

        try {
            GovernanceUtils.findGovernanceArtifacts("name:(SampleURI2 OR SampleURI1)", registry,
                                                    governanceArtifactConfiguration.getMediaType());
        } catch (GovernanceException e) {
            assertEquals("Attribute Search Service not Found", e.getMessage());
            assertNull(e.getCause());
        }

    }

    public void testBuildSearchCriteriaTaxonomy() throws Exception {

        String val = getStringFromInputStream(this.getClass().getClassLoader().getResourceAsStream("uri.rxt"));
        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("uri.rxt"));
        registry.put("/uri", resource);
        GovernanceArtifactConfiguration governanceArtifactConfiguration =
                GovernanceUtils.getGovernanceArtifactConfiguration(val);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);

        try {
            GovernanceUtils.findGovernanceArtifacts("taxonomy=(SampleURI2 OR SampleURI1)", registry,
                                                    governanceArtifactConfiguration.getMediaType());
        } catch (GovernanceException e) {
            assertEquals("Attribute Search Service not Found", e.getMessage());
            assertNull(e.getCause());
        }

    }

    public void testBuildSearchCriteriaSubPart() throws Exception {

        String val = getStringFromInputStream(this.getClass().getClassLoader().getResourceAsStream("uri.rxt"));
        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("uri.rxt"));
        registry.put("/uri", resource);
        GovernanceArtifactConfiguration governanceArtifactConfiguration =
                GovernanceUtils.getGovernanceArtifactConfiguration(val);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);

        try {
            GovernanceUtils.findGovernanceArtifacts("overview:name=(SampleURI2 OR SampleURI1)", registry,
                                                    governanceArtifactConfiguration.getMediaType());
        } catch (GovernanceException e) {
            assertEquals("Attribute Search Service not Found", e.getMessage());
            assertNull(e.getCause());
        }

    }
}
