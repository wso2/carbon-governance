/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.comparator.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.governance.common.GovernanceConfigurationService;


/**
 * @scr.component name="org.wso2.carbon.governance.comparator.services" immediate="true"
 * @scr.reference name="governanceconfiguration.service"
 * interface="org.wso2.carbon.governance.common.GovernanceConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setGovernanceConfigurationService" unbind="unsetGovernanceConfiguration"
 */
public class GovernanceComparatorComponent {

    private static final Log log = LogFactory.getLog(GovernanceComparatorComponent.class);
    private GovernanceComparatorDataHolder dataHolder = GovernanceComparatorDataHolder.getInstance();

    protected void activate(ComponentContext componentContext) {
    }

    protected void setGovernanceConfigurationService(GovernanceConfigurationService govConfigService) {
        dataHolder.setGovernanceConfiguration(govConfigService.getGovernanceConfiguration());
    }

    protected void unsetGovernanceConfiguration(GovernanceConfigurationService govConfigService) {
        dataHolder.setGovernanceConfiguration(null);
    }
}
