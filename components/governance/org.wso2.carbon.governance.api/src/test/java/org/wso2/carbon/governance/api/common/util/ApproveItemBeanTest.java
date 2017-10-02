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
package org.wso2.carbon.governance.api.common.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class ApproveItemBeanTest extends TestCase {

    private ApproveItemBean approveItemBean;

    public void setUp() throws Exception {
        approveItemBean = new ApproveItemBean();
        approveItemBean.setValue(true);
        approveItemBean.setName("default");
        approveItemBean.setOrder(0);
        approveItemBean.setRequiredVotes(0);
        approveItemBean.setVoters(null);
        approveItemBean.setStatus("default");
        approveItemBean.setVotes(0);

    }

    public void testGetName() throws Exception {
        assertEquals("default", approveItemBean.getName());
        approveItemBean.setName("updatedValue");
        assertEquals("updatedValue", approveItemBean.getName());
    }

    public void testGetOrder() throws Exception {
        assertEquals(0, approveItemBean.getOrder());
        approveItemBean.setOrder(1);
        assertEquals(1, approveItemBean.getOrder());
    }

    public void testGetStatus() throws Exception {
        assertEquals("default", approveItemBean.getStatus());
        approveItemBean.setStatus("updatedStatus");
        assertEquals("updatedStatus", approveItemBean.getStatus());
    }

    public void testGetRequiredVotes() throws Exception {
        assertEquals(0, approveItemBean.getRequiredVotes());
        approveItemBean.setRequiredVotes(10);
        assertEquals(10, approveItemBean.getRequiredVotes());
    }

    public void testGetVotes() throws Exception {

        assertEquals(0, approveItemBean.getVotes());
        approveItemBean.setVotes(5);
        assertEquals(5, approveItemBean.getVotes());
    }

    public void testGetVoters() throws Exception {
        assertNull(approveItemBean.getVoters());
        List<String> voters = new ArrayList<>();
        voters.add("Chandana");
        voters.add("Kishanthan");
        approveItemBean.setVoters(voters);
        assertEquals(2, approveItemBean.getVoters().size());
    }

    public void testGetValue() throws Exception {
        assertTrue(approveItemBean.getValue());
        approveItemBean.setValue(false);
        assertFalse(approveItemBean.getValue());
    }

}