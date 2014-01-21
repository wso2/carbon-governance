/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.lcm.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.services.callback.LoginEvent;
import org.wso2.carbon.core.services.callback.LoginListener;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.LinkedList;
import java.util.List;

public class LifecycleLoader implements LoginListener {
    
    private static Log log = LogFactory.getLog(LifecycleLoader.class);

    private List<Integer> initializedTenants = new LinkedList<Integer>();

    public void onLogin(Registry configRegistry, LoginEvent loginEvent) {
        try {
            boolean initialized = initializedTenants.contains(loginEvent.getTenantId());
            if (!initialized) {
                initializedTenants.add(loginEvent.getTenantId());
            }
            PrivilegedCarbonContext.startTenantFlow();
            try {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(loginEvent.getTenantDomain());
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(loginEvent.getTenantId());
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(loginEvent.getUsername());
                CommonUtil.addDefaultLifecyclesIfNotAvailable(configRegistry,
                        CommonUtil.getRootSystemRegistry(loginEvent.getTenantId()), initialized);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } catch (Exception e) {
            String msg = "Error in adding the default lifecycles";
            log.error(msg, e);
        }
    }
}
