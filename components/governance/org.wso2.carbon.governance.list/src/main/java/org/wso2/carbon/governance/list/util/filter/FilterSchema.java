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
import org.wso2.carbon.governance.api.schema.SchemaFilter;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.registry.core.Registry;

public class FilterSchema extends FilterStrategy {


    public FilterSchema(String criteria, Registry governanceRegistry, String artifactKey) {
        super(criteria, governanceRegistry, artifactKey);
    }

    @Override
    public GovernanceArtifact[] getArtifacts() throws GovernanceException {
        SchemaManager schemaManager = new SchemaManager(this.getGovernanceRegistry());
        if (this.getCriteria() != null && !"".equals(this.getCriteria())) {
            return schemaManager.findSchemas(new SchemaFilter() {
                public boolean matches(Schema schema) throws GovernanceException {
                    String schemaName = getSchemaName(schema);
                    return getCriteria() != null
                            && !"".equals(getCriteria())
                            && schemaName != null
                            && schemaName.contains(getCriteria());
                }
            });
        } else {
            return schemaManager.getAllSchemas();
        }
    }

    private static String getSchemaName(Schema schema) {
        String local = schema.getQName().getLocalPart();
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
