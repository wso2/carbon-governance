package org.wso2.carbon.governance.list.util;

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

import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyFilter;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaFilter;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.wsdls.WsdlFilter;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;

import java.util.ArrayList;
import java.util.List;

public class GovernanceArtifactListUtil {

    public static String[] filterWsdlByName(Registry governanceRegistry, final String refWsdlName) throws Exception {
        List<String> wsdls = new ArrayList<String>();

        WsdlManager wsdlManager = new WsdlManager(governanceRegistry);
        Wsdl[] filteredWsdls = wsdlManager.findWsdls(new WsdlFilter() {
            public boolean matches(Wsdl wsdl) throws GovernanceException {
                String wsdlName = wsdl.getAttribute("registry.wsdl.Name");
                if (refWsdlName != null
                        && wsdlName != null
                        && wsdlName.contains(refWsdlName)) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        for (Wsdl wsdl : filteredWsdls) {
            String path = wsdl.getPath();
            if (path != null) {
                wsdls.add(path);
            }
        }

        return wsdls.toArray(new String[wsdls.size()]);
    }


    public static String[] filterSchemaByName(Registry governanceRegistry, final String refSchemaName) throws Exception {
        List<String> schemas = new ArrayList<String>();

        SchemaManager schemaManager = new SchemaManager(governanceRegistry);
        Schema[] filteredSchemas = schemaManager.findSchemas(new SchemaFilter() {
            public boolean matches(Schema schema) throws GovernanceException {
                String schemaName = getSchemaName(schema);
                if (refSchemaName != null
                        && !"".equals(refSchemaName)
                        && schemaName != null
                        && schemaName.contains(refSchemaName)) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        for (Schema schema : filteredSchemas) {
            String path = schema.getPath();
            if (path != null) {
                schemas.add(path);
            }
        }
        return schemas.toArray(new String[schemas.size()]);
    }


    public static String[] filterPolicyByName(Registry governanceRegistry, final String refPolicyName) throws Exception {
        List<String> policies = new ArrayList<String>();

        PolicyManager policyManager = new PolicyManager(governanceRegistry);
        Policy[] filteredPolicies = policyManager.findPolicies(new PolicyFilter() {

            public boolean matches(Policy policy) throws GovernanceException {
                String policyName = getPolicyName(policy);
                if (refPolicyName != null
                        && !"".equals(refPolicyName)
                        && policyName != null
                        && policyName.contains(refPolicyName)) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        for (Policy policy : filteredPolicies) {
            String path = policy.getPath();
            if (path != null) {
                policies.add(path);
            }
        }
        return policies.toArray(new String[policies.size()]);
    }

    private static String getSchemaName(Schema schema){
     String local = schema.getQName().getLocalPart();
        if(local != null && !"".equals(local)){
           if(local.contains("\\.")){
             return local.substring(0,local.lastIndexOf("\\."));
           } else {
             return local;
           }
        }
     return local;
    }

    private static String getPolicyName(Policy policy){
     String local = policy.getQName().getLocalPart();
        if(local != null && !"".equals(local)){
           if(local.contains("\\.")){
             return local.substring(0,local.lastIndexOf("\\."));
           } else {
             return local;
           }
        }
     return local;
    }

}
