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
import org.wso2.carbon.governance.api.wsdls.WsdlFilter;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;

public class FilterWSDL extends FilterStrategy {


    public FilterWSDL(String criteria, Registry governanceRegistry, String artifactKey) {
        super(criteria, governanceRegistry, artifactKey);
    }

    @Override
    public GovernanceArtifact[] getArtifacts() throws GovernanceException {
        WsdlManager wsdlManager = new WsdlManager(this.getGovernanceRegistry());
        if (this.getCriteria() != null && !"".equals(this.getCriteria())) {
            return wsdlManager.findWsdls(new WsdlFilter() {
                public boolean matches(Wsdl wsdl) throws GovernanceException {
                    String wsdlName = wsdl.getAttribute("registry.wsdl.Name");
                    if (getCriteria() != null
                            && wsdlName != null
                            && wsdlName.contains(getCriteria())) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        } else {
            return wsdlManager.getAllWsdls();
        }
    }

}
