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

import org.json.JSONArray;
import org.wso2.carbon.governance.taxonomy.beans.PaginationBean;
import org.wso2.carbon.governance.taxonomy.beans.QueryBean;
import org.wso2.carbon.governance.taxonomy.beans.TaxonomyBean;
import org.wso2.carbon.governance.taxonomy.exception.TaxonomyException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import java.util.Map;

/**
 * This interface will provide methods to manage all taxonomy operations
 * As well as this will expose to outside as OSGI service
 */
public interface ITaxonomyServices {

    public boolean addTaxonomy(String payload) throws Exception;

    public boolean deleteTaxonomy(String name) throws Exception;

    public boolean updateTaxonomy(String oldName, String payload) throws Exception;

    public String getTaxonomy(String name) throws Exception;

    public TaxonomyBean getTaxonomyBean (QueryBean taxonomyQueryBean) throws RegistryException;

    public String[] getTaxonomyList() throws Exception;

    public JSONArray query(QueryBean taxonomyQueryBean, PaginationBean paginationBean) throws TaxonomyException;

    public JSONArray getTaxonomyName(QueryBean taxonomyQueryBean) throws TaxonomyException;

    public Map<String, TaxonomyBean>  getTaxonomyBeanMap ();

}
