/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.list.util.filter;

import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyFilter;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class FilterPolicy extends FilterStrategy {


    public FilterPolicy(String criteria, Registry governanceRegistry, String artifactKey) {
        super(criteria, governanceRegistry, artifactKey);
    }

    @Override
    public GovernanceArtifact[] getArtifacts() throws RegistryException {
        PolicyManager policyManager = new PolicyManager(this.getGovernanceRegistry());
        if (this.getCriteria() != null && !"".equals(this.getCriteria())) {
            return policyManager.findPolicies(new PolicyFilter() {
                public boolean matches(Policy policy) throws GovernanceException {
                    String policyName = getPolicyName(policy);
                    if (getCriteria() != null
                            && !"".equals(getCriteria())
                            && policyName != null
                            && policyName.contains(getCriteria())) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        } else {
            return policyManager.getAllPolicies();
        }
    }

    private static String getPolicyName(Policy policy) {
        String local = policy.getQName().getLocalPart();
        if (local != null && !"".equals(local)) {
            if (local.contains("\\.")) {
                return local.substring(0, local.lastIndexOf("\\."));
            } else {
                return local;
            }
        }
        return local;
    }

}

