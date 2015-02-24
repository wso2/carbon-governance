/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.lcm.services;

import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.lcm.beans.DurationBean;
import org.wso2.carbon.governance.lcm.beans.LifecycleBean;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.governance.lcm.util.LifecycleBeanPopulator;
import org.wso2.carbon.governance.lcm.util.LifecycleStateDurationUtils;
import org.wso2.carbon.registry.admin.api.governance.ILifecycleManagementService;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

public class LifeCycleManagementService extends RegistryAbstractAdmin implements ILifecycleManagementService {
    public String getLifecyclesCollectionLocation() throws Exception  {
        return CommonUtil.getContextRoot();
    }

    public void setLifecyclesCollectionLocation(String location) throws Exception {
        CommonUtil.setContextRoot(location);
    }

    public String[] getLifecycleList() throws Exception  {
        return CommonUtil.getLifecycleList(getConfigSystemRegistry());
    }

    public LifecycleBean getLifecycleBean(String name) throws Exception  {
        throw new UnsupportedOperationException("This operation is no longer supported");
    }

    public String getLifecycleConfiguration(String name) throws Exception  {
        return CommonUtil.getLifecycleConfiguration(name, getConfigSystemRegistry());
    }

    public boolean createLifecycle(String configuration) throws Exception {
        RegistryUtils.recordStatistics(configuration);
        return CommonUtil.createLifecycle(configuration, getConfigSystemRegistry(), getRootRegistry());
    }

    public boolean updateLifecycle(String oldName, String configuration) throws Exception {
        RegistryUtils.recordStatistics(oldName, configuration);
        return CommonUtil.updateLifecycle(oldName, configuration, getConfigSystemRegistry(), getRootRegistry());
    }

    public boolean deleteLifecycle(String name) throws Exception {
        RegistryUtils.recordStatistics(name);
        return CommonUtil.deleteLifecycle(name, getConfigSystemRegistry(), getRootRegistry());
    }

    public boolean isLifecycleNameInUse(String name) throws Exception {
        return CommonUtil.isLifecycleNameInUse(name, getConfigSystemRegistry(), getRootRegistry());
    }

    public boolean parseConfiguration(String configuration) throws Exception {
        return LifecycleBeanPopulator.deserializeLifecycleBean(configuration,getRootRegistry());
    }
    public String getLifecycleConfigurationVersion(String name) throws Exception{
        return CommonUtil.getLifecycleConfigurationVersion(name, getConfigSystemRegistry());
    }

    /**
     * This method provides the lifecycle current state duration.
     *
     * @param registryPathToResource    registry path to the lifecycle associated resource.
     * @param lifecycleName             name of the lifecycle associated to the resource. In multiple lifecycle
     *                                  scenario this service is called once at a time.
     * @return                          duration timestamp of the lifecycle current state respective to the latest state
     *                                  update.
     * @throws GovernanceException      Throws when an GovernanceException is thrown from method
     *                                  LifecycleStateDurationUtils.getCurrentLifecycleStateDuration.
     */
    public DurationBean getLifecycleCurrentStateDuration(String registryPathToResource, String lifecycleName)
            throws GovernanceException {
        return LifecycleStateDurationUtils
                .getCurrentLifecycleStateDuration(registryPathToResource, lifecycleName, getRootRegistry());
    }
}
