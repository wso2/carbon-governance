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

package org.wso2.carbon.governance.taxonomy.services;

import org.wso2.carbon.governance.taxonomy.beans.TaxonomyBean;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * This interface will provide methods for basic taxonomy operations which can be use to manage registry operations
 */
public interface IManagementProvider {

    boolean addTaxonomy(IStorageProvider storageProvider, TaxonomyBean taxonomyBean) throws RegistryException;

    boolean deleteTaxonomy(IStorageProvider storageProvider, String name) throws RegistryException;

    boolean updateTaxonomy(IStorageProvider storageProvider, String oldName, TaxonomyBean taxonomyBean)
            throws RegistryException;

    String getTaxonomy(IStorageProvider storageProvider, String name) throws RegistryException;

    String[] getTaxonomyList(IStorageProvider storageProvider) throws RegistryException;
}
