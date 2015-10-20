/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.governance.lcm.internal;

import org.wso2.carbon.registry.common.AttributeSearchService;
import org.wso2.carbon.registry.core.service.RegistryService;

public class LifeCycleServiceHolder {

    // Registry service used to do registry operations.
    private RegistryService registryService;

    // Attribute search service used in attribute search operations.
    private AttributeSearchService attributeSearchService;

    private static final LifeCycleServiceHolder INSTANCE = new LifeCycleServiceHolder();

    private LifeCycleServiceHolder() {
    }

    public static LifeCycleServiceHolder getInstance() {
        return INSTANCE;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }
    /**
     * This method is used to get Attribute search service.
     *
     * @return  attribute search service.
     */
    public AttributeSearchService getAttributeSearchService() {
        return attributeSearchService;
    }
    /**
     * This method is used to set attribute indexing service.
     *
     * @param attributeSearchService   Attribute indexing service.
     */
    public void setAttributeSearchService(AttributeSearchService attributeSearchService) {
        this.attributeSearchService = attributeSearchService;
    }
}
