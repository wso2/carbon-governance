/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyFilter;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.test.utils.BaseTestCase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class PolicyTest extends BaseTestCase {
    public void testAddPolicy() throws Exception {
        PolicyManager policyManager = new PolicyManager(registry);

        Policy policy = policyManager.newPolicy("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/policy/policy.xml");
        policy.addAttribute("creator", "it is me");
        policy.addAttribute("version", "0.01");
        policyManager.addPolicy(policy);

        Policy newPolicy = policyManager.getPolicy(policy.getId());
        assertEquals(policy.getPolicyContent(), newPolicy.getPolicyContent());
        assertEquals("it is me", newPolicy.getAttribute("creator"));
        assertEquals("0.01", newPolicy.getAttribute("version"));

        // change the target namespace and check
        String oldPolicyPath = newPolicy.getPath();
        assertEquals(oldPolicyPath, "/policies/policy.xml");
        assertTrue(registry.resourceExists("/policies/policy.xml"));

//        newPolicy.setName("my-policy.xml");
//        policyManager.updatePolicy(newPolicy);
//
//        assertEquals("/policies/my-policy.xml", newPolicy.getPath());
//        assertFalse(registry.resourceExists("/policies/policy.xml"));

        // doing an update without changing anything.
        policyManager.updatePolicy(newPolicy);

        assertEquals("/policies/policy.xml", newPolicy.getPath());
        assertEquals("0.01", newPolicy.getAttribute("version"));

        newPolicy = policyManager.getPolicy(policy.getId());
        assertEquals("it is me", newPolicy.getAttribute("creator"));
        assertEquals("0.01", newPolicy.getAttribute("version"));

        Policy[] policies = policyManager.findPolicies(new PolicyFilter() {
            public boolean matches(Policy policy) throws GovernanceException {
                if (policy.getAttribute("version").equals("0.01")) {
                    return true;
                }
                return false;
            }
        });
        assertEquals(1, policies.length);
        assertEquals(newPolicy.getId(), policies[0].getId());

        // deleting the policy
        policyManager.removePolicy(newPolicy.getId());
        Policy deletedPolicy = policyManager.getPolicy(newPolicy.getId());
        assertNull(deletedPolicy);
    }

    public void testAddPolicyFromContent() throws Exception {
        PolicyManager policyManager = new PolicyManager(registry);
        byte[] bytes = null;
        try {
            InputStream inputStream = new URL("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/policy/policy.xml").openStream();
            try {
                bytes = IOUtils.toByteArray(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            fail("Unable to read WSDL content");
        }
        Policy policy = policyManager.newPolicy(bytes, "newPolicy.xml");
        policy.addAttribute("creator", "it is me");
        policy.addAttribute("version", "0.01");
        policyManager.addPolicy(policy);

        Policy newPolicy = policyManager.getPolicy(policy.getId());
        assertEquals(policy.getPolicyContent(), newPolicy.getPolicyContent());
        assertEquals("it is me", newPolicy.getAttribute("creator"));
        assertEquals("0.01", newPolicy.getAttribute("version"));

        // change the target namespace and check
        String oldPolicyPath = newPolicy.getPath();
        assertEquals(oldPolicyPath, "/policies/newPolicy.xml");
        assertTrue(registry.resourceExists("/policies/newPolicy.xml"));
    }

    public void testAddPolicyFromContentNoName() throws Exception {
        PolicyManager policyManager = new PolicyManager(registry);
        byte[] bytes = null;
        try {
            InputStream inputStream = new URL("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/policy/policy.xml").openStream();
            try {
                bytes = IOUtils.toByteArray(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            fail("Unable to read WSDL content");
        }
        Policy policy = policyManager.newPolicy(bytes);
        policy.addAttribute("creator", "it is me");
        policy.addAttribute("version", "0.01");
        policyManager.addPolicy(policy);

        Policy newPolicy = policyManager.getPolicy(policy.getId());
        assertEquals(policy.getPolicyContent(), newPolicy.getPolicyContent());
        assertEquals("it is me", newPolicy.getAttribute("creator"));
        assertEquals("0.01", newPolicy.getAttribute("version"));

        // change the target namespace and check
        String oldPolicyPath = newPolicy.getPath();
        assertEquals(oldPolicyPath, "/policies/"+ policy.getId() + ".xml");
        assertTrue(registry.resourceExists("/policies/"+ policy.getId() + ".xml"));
    }
}
