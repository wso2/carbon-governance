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

public class CheckListItemBeanTest extends TestCase {

    private CheckListItemBean checkListItemBean;

    @Override
    protected void setUp() throws Exception {
        checkListItemBean = new CheckListItemBean();
        checkListItemBean.setName("default");
        checkListItemBean.setStatus("defaultStatus");
        checkListItemBean.setOrder(1);
        checkListItemBean.setValue(false);
    }

    public void testGetName() throws Exception {

        assertEquals("default", checkListItemBean.getName());
        checkListItemBean.setName("updatedValue");
        assertEquals("updatedValue", checkListItemBean.getName());
    }

    public void testGetStatus() throws Exception {

        assertEquals("defaultStatus", checkListItemBean.getStatus());
        checkListItemBean.setStatus("updatedStatus");
        assertEquals("updatedStatus", checkListItemBean.getStatus());
    }

    public void testGetOrder() throws Exception {

        assertEquals(1, checkListItemBean.getOrder());
        checkListItemBean.setOrder(3);
        assertEquals(3, checkListItemBean.getOrder());
    }

    public void testGetValue() throws Exception {

        assertFalse(checkListItemBean.getValue());
        checkListItemBean.setValue(true);
        assertTrue(checkListItemBean.getValue());
    }

}