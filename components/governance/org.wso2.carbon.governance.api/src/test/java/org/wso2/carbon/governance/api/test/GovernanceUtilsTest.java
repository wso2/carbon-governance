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
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.test.utils.BaseTestCase;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import javax.xml.namespace.QName;

public class GovernanceUtilsTest extends BaseTestCase {

    public void testGetDirectArtifactPath() throws Exception {
        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(registry, "application/vnd" +
                ".wso2-test+xml",
                                                                                   "test", "org.wso2.samples.test",
                                                                                   "/_system/governance",
                                                                                   GovernanceConstants
                                                                                           .SERVICE_ELEMENT_NAMESPACE,
                                                                                   "/@{name}", new Association[0]);
        GenericArtifact artifact = genericArtifactManager.newGovernanceArtifact(
                new QName(GovernanceConstants.SERVICE_ELEMENT_NAMESPACE, "MyService1"));
        artifact.setAttribute("name", "Chandana");
        artifact.setAttribute("namespace", "Chandana");
        artifact.setAttribute("overview_namespace", "Chandana");
        genericArtifactManager.addGenericArtifact(artifact);

        assertTrue(genericArtifactManager.isExists(artifact));

        assertEquals("/MyService1", GovernanceUtils.getDirectArtifactPath(registry, artifact.getId()));

        genericArtifactManager.removeGenericArtifact(artifact.getId());
    }

    public void testFindGovernanceArtifacts() throws RegistryException {
        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(registry, "application/vnd" +
                ".wso2-test+xml",
                                                                                   "test", "org.wso2.samples.test",
                                                                                   "/_system/governance",
                                                                                   GovernanceConstants
                                                                                           .SERVICE_ELEMENT_NAMESPACE,
                                                                                   "/@{name}", new Association[0]);
        GenericArtifact artifact = genericArtifactManager.newGovernanceArtifact(
                new QName(GovernanceConstants.SERVICE_ELEMENT_NAMESPACE, "MyService2"));
        artifact.setAttribute("name", "Chandana");
        artifact.setAttribute("namespace", "Chandana");
        artifact.setAttribute("overview_namespace", "Chandana");
        genericArtifactManager.addGenericArtifact(artifact);

        assertTrue(genericArtifactManager.isExists(artifact));
        assertEquals(1, GovernanceUtils.findGovernanceArtifacts("application/vnd.wso2-test+xml", registry).length);
        System.out.println(GovernanceUtils.getArtifactPath(registry, artifact.getId()));
        genericArtifactManager.removeGenericArtifact(artifact.getId());
    }

    public void testFindGovernanceArtifactConfigurationByMediaType() throws RegistryException {
        GenericArtifactManager genericArtifactManager = null;
        GenericArtifact artifact = null;
        try {
            genericArtifactManager = new GenericArtifactManager(registry, "application/vnd" +
                    ".wso2-test+xml",
                                                                "test", "org.wso2.samples.test",
                                                                "/_system/governance", GovernanceConstants
                                                                        .SERVICE_ELEMENT_NAMESPACE, "/@{name}",
                                                                new Association[0]);
            artifact = genericArtifactManager.newGovernanceArtifact(
                    new QName(GovernanceConstants.SERVICE_ELEMENT_NAMESPACE, "MyService3"));
            artifact.setAttribute("name", "Chandana");
            artifact.setAttribute("namespace", "Chandana");
            artifact.setAttribute("overview_namespace", "Chandana");
            genericArtifactManager.addGenericArtifact(artifact);

            assertTrue(genericArtifactManager.isExists(artifact));
            GovernanceUtils.findGovernanceArtifactConfigurationByMediaType("application/vnd.wso2-test+xml", registry);
        } catch (RegistryException e) {
            fail("not expecting exception");
        } finally {
            genericArtifactManager.removeGenericArtifact(artifact.getId());
        }
    }


    public void testGetResultPaths() throws RegistryException {
        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(registry, "application/vnd" +
                ".wso2-test+xml",
                                                                                   "test", "org.wso2.samples.test",
                                                                                   "/_system/governance",
                                                                                   GovernanceConstants
                                                                                           .SERVICE_ELEMENT_NAMESPACE,
                                                                                   "/@{name}", new Association[0]);
        GenericArtifact artifact = genericArtifactManager.newGovernanceArtifact(
                new QName(GovernanceConstants.SERVICE_ELEMENT_NAMESPACE, "MyService4"));
        artifact.setAttribute("name", "Chandana");
        artifact.setAttribute("namespace", "Chandana");
        artifact.setAttribute("overview_namespace", "Chandana");
        genericArtifactManager.addGenericArtifact(artifact);

        assertTrue(genericArtifactManager.isExists(artifact));
        assertEquals(1, GovernanceUtils.getResultPaths(registry, "application/vnd.wso2-test+xml").length);

        genericArtifactManager.removeGenericArtifact(artifact.getId());
    }


    public void testGetArtifactConfigurationByMediaType() throws RegistryException {
        GenericArtifactManager genericArtifactManager = null;
        GenericArtifact artifact = null;
        try {
            genericArtifactManager = new GenericArtifactManager(registry, "application/vnd" +
                    ".wso2-test+xml",
                                                                "test", "org.wso2.samples.test",
                                                                "/_system/governance", GovernanceConstants
                                                                        .SERVICE_ELEMENT_NAMESPACE, "/@{name}",
                                                                new Association[0]);
            artifact = genericArtifactManager.newGovernanceArtifact(
                    new QName(GovernanceConstants.SERVICE_ELEMENT_NAMESPACE, "MyService5"));
            artifact.setAttribute("name", "Chandana");
            artifact.setAttribute("namespace", "Chandana");
            artifact.setAttribute("overview_namespace", "Chandana");
            genericArtifactManager.addGenericArtifact(artifact);

            assertTrue(genericArtifactManager.isExists(artifact));
            GovernanceUtils.getArtifactConfigurationByMediaType(registry, "application/vnd.wso2-test+xml");
        } catch (RegistryException e) {
            fail("not expecting exception");
        } finally {
            genericArtifactManager.removeGenericArtifact(artifact.getId());
        }
    }

    public void testLoadGovernanceArtifacts() throws GovernanceException {
        GenericArtifactManager genericArtifactManager = null;
        GenericArtifact artifact = null;
        try {
            genericArtifactManager = new GenericArtifactManager(registry, "application/vnd" +
                    ".wso2-test+xml",
                                                                "test", "org.wso2.samples.test",
                                                                "/_system/governance", GovernanceConstants
                                                                        .SERVICE_ELEMENT_NAMESPACE, "/@{name}",
                                                                new Association[0]);
            artifact = genericArtifactManager.newGovernanceArtifact(
                    new QName(GovernanceConstants.SERVICE_ELEMENT_NAMESPACE, "MyService6"));
            artifact.setAttribute("name", "Chandana");
            artifact.setAttribute("namespace", "Chandana");
            artifact.setAttribute("overview_namespace", "Chandana");
            genericArtifactManager.addGenericArtifact(artifact);

            assertTrue(genericArtifactManager.isExists(artifact));
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
        } catch (RegistryException e) {
            fail("not expecting exception");
        } finally {
            genericArtifactManager.removeGenericArtifact(artifact.getId());
        }
    }


    public void testGetGovernanceSystemRegistry() throws RegistryException {
        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(registry, "application/vnd" +
                ".wso2-test+xml",
                                                                                   "test", "org.wso2.samples.test",
                                                                                   "/_system/governance",
                                                                                   GovernanceConstants
                                                                                           .SERVICE_ELEMENT_NAMESPACE,
                                                                                   "/@{name}", new Association[0]);
        GenericArtifact artifact = genericArtifactManager.newGovernanceArtifact(
                new QName(GovernanceConstants.SERVICE_ELEMENT_NAMESPACE, "MyService7"));
        artifact.setAttribute("name", "Chandana");
        artifact.setAttribute("namespace", "Chandana");
        artifact.setAttribute("overview_namespace", "Chandana");
        genericArtifactManager.addGenericArtifact(artifact);

        assertTrue(genericArtifactManager.isExists(artifact));
        assertNull(GovernanceUtils.getGovernanceSystemRegistry(registry));

        genericArtifactManager.removeGenericArtifact(artifact.getId());


    }

    public void testGetAllArtifactPathsByLifecycle() throws RegistryException {
        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(registry, "application/vnd" +
                ".wso2-test+xml",
                                                                                   "test", "org.wso2.samples.test",
                                                                                   "/_system/governance",
                                                                                   GovernanceConstants
                                                                                           .SERVICE_ELEMENT_NAMESPACE,
                                                                                   "/@{name}", new Association[0]);
        GenericArtifact artifact = genericArtifactManager.newGovernanceArtifact(
                new QName(GovernanceConstants.SERVICE_ELEMENT_NAMESPACE, "MyService8"));
        artifact.setAttribute("name", "Chandana");
        artifact.setAttribute("namespace", "Chandana");
        artifact.setAttribute("overview_namespace", "Chandana");
        genericArtifactManager.addGenericArtifact(artifact);

        assertTrue(genericArtifactManager.isExists(artifact));
        assertNotNull(GovernanceUtils.getAllArtifactPathsByLifecycle(registry, "Test",
                                                                     "application/vnd.wso2-test+xml").length);

        genericArtifactManager.removeGenericArtifact(artifact);
    }


    public void testAddEmptyRXT() throws RegistryException {
        assertNull(GovernanceUtils.getGovernanceArtifactConfiguration(null));
    }


}
