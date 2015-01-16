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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.services.callback.LoginEvent;
import org.wso2.carbon.core.services.callback.LoginListener;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.LinkedList;
import java.util.List;

public class LifecycleLoader extends AbstractAxis2ConfigurationContextObserver {
    
    private static Log log = LogFactory.getLog(LifecycleLoader.class);

    @Override
    public void createdConfigurationContext(ConfigurationContext configContext) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        RegistryService service = RegistryCoreServiceComponent.getRegistryService();
        try {
            CommonUtil.addDefaultLifecyclesIfNotAvailable(service.getConfigSystemRegistry(tenantId),
                                                          CommonUtil.getRootSystemRegistry(tenantId));
        } catch (Exception e) {
            String msg = "Error in adding the default lifecycles";
            log.error(msg, e);
        }
    }
}
