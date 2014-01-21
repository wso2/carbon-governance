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
package org.wso2.carbon.governance.list.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.dataobjects.Service;

public class ListServiceFilter implements ServiceFilter {
    private static final Log log = LogFactory.getLog(ListServiceFilter.class);
    private Service referenceService; // the service to match with
    public ListServiceFilter(Service referenceService) {
        this.referenceService = referenceService;    
    }
    public boolean matches(Service service) throws GovernanceException {
        if (referenceService == null) {
            return true;
        }
        String[] keys = referenceService.getAttributeKeys();

        for (String key: keys) {
            if ("operation".equals(key)) {
                // this is a special case
                continue;
            }
            if (key.toLowerCase().contains("count")) {
                // we ignore the count.
                continue;
            }
            String[] referenceValues = referenceService.getAttributes(key);
            if (referenceValues == null) {
                continue;
            }
            // all the valid keys should be satisfied..
            boolean satisfied = false; // either one of value should be satisfied.
            String[] realValues = service.getAttributes(key);
            if(realValues == null){
                return false;
            }
            else{
                for (String referenceValue: referenceValues) {
                    for (String realValue: realValues) {
                        if (realValue.contains(referenceValue)) {
                            satisfied = true;
                            break;
                        }
                        // the reference value can be regular expression
                        try {
                            if (realValue.matches(referenceValue)) {
                                satisfied = true;
                                break;
                            }
                        } catch (Exception e) {
                            String msg = "Error in performing the regular expression matches for: " +
                                    referenceValue + ".";
                            throw new GovernanceException(msg, e);
                        }
                    }
                    if (satisfied) {
                        break;
                    }
                }
            }
            if (!satisfied) {
                if (log.isDebugEnabled()) {
                    String msg = "key: " + key + " is not satisfied by the service: " +
                            service.getQName() + ".";
                    log.debug(msg);
                }
                return false;
            }
        }
        return true;
    }
}
