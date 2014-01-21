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

package org.wso2.carbon.governance.gadgetsource.util;

import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.gadgetsource.beans.LifecycleInfoBean;
import org.wso2.carbon.governance.gadgetsource.beans.LifecycleStageInfoBean;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Populator {

    public static LifecycleInfoBean[] populateLifecycle(Registry registry) throws RegistryException {

        ServiceManager serviceManager = new ServiceManager(registry);
//        Service[] services = serviceManager.getAllServices();
//        List<String> paths = new ArrayList<String>();
//
//        if (services != null) {
//            for (Service service : services) {
//                if (service.getPath() != null) {
//                    paths.add(service.getPath());
//                }
//            }
//        }
        String[] servicePaths = serviceManager.getAllServicePaths();


        Map<String, LifecycleInfoBean> lifecycleInfoBeanMap = new HashMap<String, LifecycleInfoBean>();
        Map<String, List<LifecycleStageInfoBean>> lifecycleStagesMap = new HashMap<String, List<LifecycleStageInfoBean>>();
        for (String servicePath: servicePaths) {
//                Resource serviceResource = GovernanceUtils.getGovernanceSystemRegistry(registry).get(servicePath);
                  Resource serviceResource = registry.get(servicePath);
                String lifecycleName = serviceResource.getProperty("registry.Aspects");
            if (lifecycleName != null) {
                lifecycleName = lifecycleName.replaceAll("\\s", "");
                LifecycleInfoBean lifecycleInfoBean = lifecycleInfoBeanMap.get(lifecycleName);
                List<LifecycleStageInfoBean> lifecycleStages = lifecycleStagesMap.get(lifecycleName);
                if (lifecycleInfoBean == null) {
                    lifecycleInfoBean = new LifecycleInfoBean();
                    lifecycleInfoBean.setName(lifecycleName);
                    lifecycleInfoBeanMap.put(lifecycleName, lifecycleInfoBean);

                    // initializing lifecycle stages
                    lifecycleStages = new ArrayList<LifecycleStageInfoBean>();
                    lifecycleStagesMap.put(lifecycleName, lifecycleStages);
                }
                String lifecycleStageKey = "registry.lifecycle." + lifecycleName + ".state";
                String lifecycleStageValue = serviceResource.getProperty(lifecycleStageKey);

                LifecycleStageInfoBean lifecycleStageInfoBean = null;
                
                // iterate and find the correct stage object
                for (int i = 0; i < lifecycleStages.size(); i ++) {
                    LifecycleStageInfoBean stageInfoBeanIt  = lifecycleStages.get(i);
                    if (stageInfoBeanIt.getName().equals(lifecycleStageValue)) {
                        lifecycleStageInfoBean = stageInfoBeanIt;
                    }
                }

                if (lifecycleStageInfoBean == null) {
                    // create a new life cycle stage
                    lifecycleStageInfoBean =  new LifecycleStageInfoBean();
                    lifecycleStageInfoBean.setName(lifecycleStageValue);
                    lifecycleStageInfoBean.setServiceCount(1);
                    lifecycleStages.add(lifecycleStageInfoBean);
                }
                else {
                    // increment the current service count
                    lifecycleStageInfoBean.setServiceCount(lifecycleStageInfoBean.getServiceCount() + 1);
                }

                LifecycleStageInfoBean[] lifecycleStageInfoBeans = lifecycleStages.toArray(
                            new LifecycleStageInfoBean[lifecycleStages.size()]);
                lifecycleInfoBean.setStages(lifecycleStageInfoBeans);
            }
        }

        // now fill all the beans
        LifecycleInfoBean[] lifecycleInfoBeans = lifecycleInfoBeanMap.values().toArray(
                            new LifecycleInfoBean[lifecycleInfoBeanMap.size()]);
        return lifecycleInfoBeans;
    }


    public static LifecycleInfoBean[] populateLifecycleDummyData() throws RegistryException {
         /* creating dummy data */
        LifecycleInfoBean[] lifecycles = new LifecycleInfoBean[1];
        lifecycles[0] = new LifecycleInfoBean();
        lifecycles[0].setName("No Data Available");
        // application/vnd.wso2-mex+xml
        LifecycleStageInfoBean[] stages = new LifecycleStageInfoBean[1];
        stages[0] = new LifecycleStageInfoBean();
        stages[0].setName("Life Cycle Data Not Available.");
        stages[0].setServiceCount(0);
        /*stages[1] = new LifecycleStageInfoBean();
        stages[1].setName("Lifecycle");
        stages[1].setServiceCount(6);
        stages[2] = new LifecycleStageInfoBean();
        stages[2].setName("Associated");
        stages[2].setServiceCount(2);
        stages[3] = new LifecycleStageInfoBean();
        stages[3].setName("Services");
        stages[3].setServiceCount(3);*/
        lifecycles[0].setStages(stages);

        return lifecycles;
    }
}
