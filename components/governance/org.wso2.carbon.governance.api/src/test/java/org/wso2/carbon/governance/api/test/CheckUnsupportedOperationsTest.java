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

import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.test.utils.BaseTestCase;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.namespace.QName;

public class CheckUnsupportedOperationsTest extends BaseTestCase {


    public void testGetAllArtifactIds() throws Exception {
        try {
            GovernanceUtils.getAllArtifactIds(registry);
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {

        }
    }

    public void testGetAllArtifacts() throws Exception {
        try {
            GovernanceUtils.getAllArtifacts(registry);
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {

        }

    }

    public void testGenericSrtifacts() throws RegistryException {
        String val = GenericArtifactTest.getStringFromInputStream(
                this.getClass().getClassLoader().getResourceAsStream("uri.rxt"));
        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("uri.rxt"));
        registry.put("/uri", resource);
        GovernanceArtifactConfiguration governanceArtifactConfiguration =
                GovernanceUtils.getGovernanceArtifactConfiguration(val);

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);

        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(registry,
                                                                                   governanceArtifactConfiguration
                                                                                           .getKey());
        GenericArtifact artifact = genericArtifactManager.newGovernanceArtifact(new QName("SampleURI1"));
        artifact.setAttribute("overview_name", "SampleURI1");
        artifact.setAttribute("overview_uri", "http://napagoda.com");
        artifact.setAttribute("overview_type", "ValueType");
        genericArtifactManager.addGenericArtifact(artifact);

        GovernanceArtifact governanceArtifact = artifact;

        try {
            governanceArtifact.addAssociation("test", artifact.getId());
            fail("Unsupported Operation Exception expected");
        } catch (UnsupportedOperationException e) {

        }

        try {
            governanceArtifact.getAssociations();
            fail("Unsupported Operation Exception expected");
        } catch (UnsupportedOperationException e) {

        }

        try {
            governanceArtifact.removeAssociation(artifact.getId());
            fail("Unsupported Operation Exception expected");
        } catch (UnsupportedOperationException e) {

        }

        try {
            governanceArtifact.removeAssociation("test", artifact.getId());
            fail("Unsupported Operation Exception expected");
        } catch (UnsupportedOperationException e) {

        }

        try {
            governanceArtifact.getAssociatedArtifactIds();
            fail("Unsupported Operation Exception expected");
        } catch (UnsupportedOperationException e) {

        }


        genericArtifactManager.removeGenericArtifact(artifact.getId());

    }

}
