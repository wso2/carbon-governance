/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.governance.taxonomy.clustering;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.taxonomy.services.TaxonomyManager;

public class ClusterMessage extends ClusteringMessage {

    private static final Log log = LogFactory.getLog(ClusterMessage.class);

    private String taxaName;
    private int tenantId;

    public ClusterMessage(int tenantId, String taxaName) {
        super();
        this.taxaName = taxaName;
        this.tenantId = tenantId;
    }

    @Override
    public ClusteringCommand getResponse() {
        return null;
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        if (log.isDebugEnabled()) {
            log.debug("Cluster message received " + taxaName);
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            TaxonomyManager taxonomyManager = new TaxonomyManager();
            taxonomyManager.getStorageProvider().removeTaxonomy(taxaName);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }


    }

    public String toString() {
        return String.format(taxaName, tenantId);
    }
}
