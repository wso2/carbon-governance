/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.taxonomy.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.HashMap;
import java.util.Map;

/**
 * This will work as taxonomy.xml parsed doc storage service. It will store parsed doc in map related to tenant
 */
public class TaxonomyStorageService {
    private static final Log log = LogFactory.getLog(TaxonomyStorageService.class);
    private static Map<Integer, Document> tenantMap = new HashMap<Integer, Document>();

    /**
     * This method will retrieve the already parsed document related to specific tenant
     * @return xml parsed document
     */
    public Document getParsedDocument() {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        return tenantMap.get(tenantId);
    }

    /**
     * This method will store parsed  xml document inside tenant specific map.
     * @param doc
     */
    public void addParseDocument(Document doc) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        tenantMap.put(tenantId, doc);
    }

}
