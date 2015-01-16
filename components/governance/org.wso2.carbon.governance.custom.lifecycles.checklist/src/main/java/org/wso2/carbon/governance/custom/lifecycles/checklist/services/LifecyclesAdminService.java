/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.custom.lifecycles.checklist.services;

import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.custom.lifecycles.checklist.beans.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.CommonUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.InvokeAspectUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.LifecycleBeanPopulator;
import org.wso2.carbon.registry.admin.api.governance.IChecklistLifecycleService;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LifecyclesAdminService extends RegistryAbstractAdmin implements IChecklistLifecycleService {

    public LifecycleBean getLifecycleBean(String path) throws Exception {
        return LifecycleBeanPopulator.getLifecycleBean(path, (UserRegistry)getRootRegistry(),
                getConfigSystemRegistry());
    }

    public void addAspect(String path, String aspect) throws Exception {
        RegistryUtils.recordStatistics(path, aspect);
        GovernanceUtils.associateAspect(path, aspect, getRootRegistry());
    }

    public void invokeAspect(String path, String aspect, String action, String[] items) throws Exception {
        RegistryUtils.recordStatistics(path, aspect, action, items);
        InvokeAspectUtil.invokeAspect(path, aspect, action, items, getRootRegistry(),
                Collections.<String, String>emptyMap());
    }

    public void invokeAspectWithParams(String path, String aspect, String action, String[] items,
                                       String[][] parameters) throws Exception {
        RegistryUtils.recordStatistics(path, aspect, action, items, parameters);
        Map<String, String> paramMap = new HashMap<String, String>();
        for (String[] strings : parameters) {
            if (strings != null && strings.length == 2) {
                paramMap.put(strings[0], strings[1]);
            }
        }
        InvokeAspectUtil.invokeAspect(path, aspect, action, items, getRootRegistry(), paramMap);
    }

    public void removeAspect(String path, String aspect) throws Exception {
        RegistryUtils.recordStatistics(path, aspect);
        GovernanceUtils.removeAspect(path, aspect, getRootRegistry());
    }

    @SuppressWarnings("unused")
    public String[] getAllDependencies(String path) throws Exception{
        return CommonUtil.getAllDependencies(path,getRootRegistry());
    }

    @SuppressWarnings("unused")
    public void setDefaultAspect(String path, String aspect) throws Exception {
        RegistryUtils.recordStatistics(path, aspect);
        GovernanceUtils.setDefaultLifeCycle(path, aspect, getRootRegistry());
    }
}
