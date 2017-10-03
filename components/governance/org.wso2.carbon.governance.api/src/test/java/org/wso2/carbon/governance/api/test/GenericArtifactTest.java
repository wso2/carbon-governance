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
import org.wso2.carbon.governance.api.exception.GovernanceException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericArtifactTest extends BaseTestCase {

    public void testCheckValueOfArtifactConfiguration() throws RegistryException {
        //Add RXT
        String val = getStringFromInputStream(this.getClass().getClassLoader().getResourceAsStream("uri.rxt"));
        Resource resource = registry.newResource();
        resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
        resource.setContentStream(this.getClass().getClassLoader().getResourceAsStream("uri.rxt"));
        registry.put("/uri", resource);
        GovernanceArtifactConfiguration governanceArtifactConfiguration =
                GovernanceUtils.getGovernanceArtifactConfiguration(val);
        assertNotNull(governanceArtifactConfiguration);

        //Validate RXT values
        assertEquals(3, governanceArtifactConfiguration.getKeysOnListUI().length);
        assertEquals(3, governanceArtifactConfiguration.getExpressionsOnListUI().length);
        assertEquals(3, governanceArtifactConfiguration.getNamesOnListUI().length);
        assertEquals(3, governanceArtifactConfiguration.getTypesOnListUI().length);
        assertEquals(0, governanceArtifactConfiguration.getRelationshipDefinitions().length);
        assertEquals("URI", governanceArtifactConfiguration.getSingularLabel());
        assertEquals("/uris/@{overview_type}/@{name}", governanceArtifactConfiguration.getPathExpression());

        assertEquals("application/vnd.wso2-uri+xml", governanceArtifactConfiguration.getMediaType());
        assertEquals("uri", governanceArtifactConfiguration.getKey());
        assertEquals("overview_name", governanceArtifactConfiguration.getArtifactNameAttribute());
        assertNull(governanceArtifactConfiguration.getArtifactNamespaceAttribute());
        assertEquals("metadata", governanceArtifactConfiguration.getArtifactElementRoot());
        assertNotNull(governanceArtifactConfiguration.getContentDefinition());


        assertEquals("URIs", governanceArtifactConfiguration.getPluralLabel());
        assertEquals("URI", governanceArtifactConfiguration.getSingularLabel());
        assertEquals(11, governanceArtifactConfiguration.getIconSet());
        assertNull(governanceArtifactConfiguration.getLifecycle());
        assertEquals(2, governanceArtifactConfiguration.getUniqueAttributes().size());

        assertEquals(2, governanceArtifactConfiguration.getUniqueAttributes().size());
        assertEquals(2, governanceArtifactConfiguration.getValidationAttributes().size());
        assertEquals(4, governanceArtifactConfiguration.getUIPermissions().length);

        assertNull(governanceArtifactConfiguration.getContentURL());
        assertNull(governanceArtifactConfiguration.getExtension());
        assertFalse(governanceArtifactConfiguration.hasNamespace());
        assertNull(governanceArtifactConfiguration.getGroupingAttribute());
        assertEquals("http://www.wso2.org/governance/metadata",
                     governanceArtifactConfiguration.getArtifactElementNamespace());
        assertNotNull(governanceArtifactConfiguration.getUIConfigurations());
        assertNull(governanceArtifactConfiguration.getTaxonomy());

        //Set Lifecycle and Taxonomy
        governanceArtifactConfiguration.setLifecycle("SampleLifecycle");
        governanceArtifactConfiguration.setTaxonomy("taxonomy", false, false);

        assertEquals("SampleLifecycle", governanceArtifactConfiguration.getLifecycle());
        assertEquals(1, governanceArtifactConfiguration.getTaxonomy().size());


        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);

        assertEquals("uri", GovernanceUtils.findGovernanceArtifactConfigurations(registry).get(0).getKey());
        assertEquals("uri", GovernanceUtils
                .getArtifactConfigurationByMediaType(registry, governanceArtifactConfiguration.getMediaType())
                .getKey());
        assertEquals("uri", GovernanceUtils
                .findGovernanceArtifactConfiguration(governanceArtifactConfiguration.getKey(), registry).getKey());

        //Create generic artifact manager using added RXT

        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(registry,
                                                                                   governanceArtifactConfiguration
                                                                                           .getKey());

        //Create asset instance using eneric artifact manager
        GenericArtifact artifact = genericArtifactManager.newGovernanceArtifact(new QName("SampleURI1"));
        artifact.setAttribute("overview_name", "SampleURI1");
        artifact.setAttribute("overview_uri", "http://napagoda.com");
        artifact.setAttribute("overview_type", "ValueType");
        //Validate Mandatory Fields
        GovernanceUtils.CheckMandatoryFields(registry, governanceArtifactConfiguration.getKey(), artifact);
        // add a asset
        genericArtifactManager.addGenericArtifact(artifact);

        //Update above asset
        genericArtifactManager.updateGenericArtifact(artifact);

        // Check media type is same
        assertEquals(governanceArtifactConfiguration.getMediaType(), artifact.getMediaType());

        GovernanceArtifact governanceArtifact = artifact;

        assertNotNull(artifact.toString());

        assertNull("Life Cycle is not attached yet", governanceArtifact.getLifecycleName());

        assertEquals("Life Cycle is not attached yet", 0, governanceArtifact.getLifecycleNames().length);

        assertNull("Life Cycle is not attached yet", governanceArtifact.getLifecycleState());

        assertNull("Life Cycle is not attached yet", governanceArtifact.getLifecycleState("sample"));


        GenericArtifact artifact2 = genericArtifactManager.newGovernanceArtifact(new QName("SampleURI2"));
        artifact2.setAttribute("overview_name", "SampleURI2");
        artifact2.setAttribute("overview_uri", "http://napagoda.com");
        artifact2.setAttribute("overview_type", "ValueType");
        GovernanceUtils.CheckMandatoryFields(registry, governanceArtifactConfiguration.getKey(), artifact2);
        genericArtifactManager.addGenericArtifact(artifact2);

        governanceArtifact.addAssociation("usedby", artifact2);

        assertEquals("application/vnd.wso2-uri+xml", governanceArtifact.getMediaType());

        governanceArtifact.getDependencies();
        governanceArtifact.getDependents();


        assertEquals(2, GovernanceUtils
                .findGovernanceArtifacts(governanceArtifactConfiguration.getMediaType(), registry).length);


        genericArtifactManager.removeGenericArtifact(artifact);
        genericArtifactManager.removeGenericArtifact(artifact2.getId());


        try {
            GovernanceUtils.findGovernanceArtifacts("name=SampleURI2", registry,
                                                    governanceArtifactConfiguration.getMediaType());
        } catch (GovernanceException e) {
            assertEquals("Attribute Search Service not Found", e.getMessage());
            assertNull(e.getCause());
        }

        try {
            Map<String, List<String>> maps = new HashMap<>();
            List list = new ArrayList();
            list.add("SampleURI2");
            maps.put("name", list);
            GovernanceUtils.findGovernanceArtifacts(maps, registry, governanceArtifactConfiguration.getMediaType());
        } catch (GovernanceException e) {
            assertEquals("Attribute Search Service not Found", e.getMessage());
            assertNull(e.getCause());
        }
    }

}
