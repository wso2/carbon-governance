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

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.test.utils.BaseTestCase;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.AttributeSearchService;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.governance.api.util.GovernanceUtils.findGovernanceArtifacts;

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
            findGovernanceArtifacts(
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
            findGovernanceArtifacts("name:(SampleURI2 OR SampleURI1)", registry,
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
            findGovernanceArtifacts("taxonomy=(SampleURI2 OR SampleURI1)", registry,
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
            findGovernanceArtifacts("overview:name=(SampleURI2 OR SampleURI1)", registry,
                                                    governanceArtifactConfiguration.getMediaType());
        } catch (GovernanceException e) {
            assertEquals("Attribute Search Service not Found", e.getMessage());
            assertNull(e.getCause());
        }

    }

    /**
     * This test case tests the behavior of search with the property.
     */
    @SuppressWarnings("unchecked")
    public void testBuildSearchCriteriaProperty() throws RegistryException {
        registry.put("/uri", createResource());
        registry.put("/uri2", createResource());
        final GovernanceArtifactConfiguration governanceArtifactConfiguration = GovernanceUtils
                .getGovernanceArtifactConfiguration(
                        getStringFromInputStream(this.getClass().getClassLoader().getResourceAsStream("uri.rxt")));
        governanceArtifactConfiguration.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        GovernanceUtils
                .loadGovernanceArtifacts((UserRegistry) registry, new ArrayList<GovernanceArtifactConfiguration>() {{
                    add(governanceArtifactConfiguration);
                }});
        AttributeSearchService attributeSearchService = Mockito.mock(AttributeSearchService.class);
        PaginationContext.init(0, 10, "ASC", "nameAttribute", 100);

        // Mocking the search method to return both of the resources as a result.
        Mockito.doAnswer(new Answer() {
            private int count = 0;
            public Object answer(InvocationOnMock invocation) {
                if (count == 0) {
                    count++;
                    return new ResourceData[0];
                } else {
                    ResourceData resourceData = new ResourceData();
                    resourceData.setResourcePath("/_system/governance/uri");
                    ResourceData resourceDataCopy = new ResourceData();
                    resourceDataCopy.setResourcePath("/_system/governance/uri2");
                    return new ResourceData[] { resourceData, resourceDataCopy };
                }
            }
        }).when(attributeSearchService).search(Mockito.any(Map.class));
        GovernanceUtils.setAttributeSearchService(attributeSearchService);
        List<GovernanceArtifact> governanceArtifacts = GovernanceUtils
                .findGovernanceArtifacts("name=(SampleURI2 OR " + "SampleURI1)&publisher_roles=(admin)", registry,
                        governanceArtifactConfiguration.getMediaType());
        assertEquals(2, governanceArtifacts.size());
    }

    /**
     * To create a registry resource for testing purpose.
     *
     * @return Resource.
     * @throws RegistryException Registry Exception.
     */
    private Resource createResource() throws RegistryException {
        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("uri.rxt"));
        resource.setProperty("publisher_roles", "admin");
        return resource;
    }
}
