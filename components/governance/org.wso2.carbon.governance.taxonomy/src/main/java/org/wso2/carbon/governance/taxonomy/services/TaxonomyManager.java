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
import org.json.JSONException;
import org.w3c.dom.NodeList;
import org.wso2.carbon.governance.taxonomy.beans.PaginationBean;
import org.wso2.carbon.governance.taxonomy.beans.QueryBean;
import org.wso2.carbon.governance.taxonomy.beans.TaxonomyBean;
import org.wso2.carbon.governance.taxonomy.exception.TaxonomyException;
import org.wso2.carbon.governance.taxonomy.util.CommonUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.xml.sax.SAXException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

/**
 * This is the main class which manage all taxonomy operations including map storage and registry operations
 */
public class TaxonomyManager {

    private IStorageProvider storageProvider;
    private IQueryProvider queryProvider;
    private IManagementProvider managementProvider;

    /**
     * Constructor will initialize new object instances for above attributes
     */
    public TaxonomyManager() {
        this.storageProvider = new StorageProviderImpl();
        this.queryProvider = new QueryProviderImpl();
        this.managementProvider = new ManagementProviderImpl();
    }

    /**
     * This method will invoke add taxonomy methods for management provider and storage provider
     *
     * @param taxonomyBean Taxonomy document bean object
     * @return return state of the operation
     * @throws RegistryException
     */
    boolean addTaxonomy(TaxonomyBean taxonomyBean) throws RegistryException {
        return managementProvider.addTaxonomy(storageProvider, taxonomyBean);
    }

    /**
     * This method will invoke delete taxonomy methods for management provider and storage provider
     *
     * @param taxonomyName of the taxonomy file
     * @return return state of the operation
     * @throws RegistryException
     */
    boolean deleteTaxonomy(String taxonomyName) throws RegistryException {
        return managementProvider.deleteTaxonomy(storageProvider, taxonomyName);
    }

    /**
     * This method will invoke update taxonomy methods for management provider and storage provider
     *
     * @param oldName      String value of old name
     * @param taxonomyBean Taxonomy meta data contained object
     * @return return state of the operation
     * @throws RegistryException
     */
    boolean updateTaxonomy(String oldName, TaxonomyBean taxonomyBean) throws RegistryException {
        return managementProvider.updateTaxonomy(storageProvider, oldName, taxonomyBean);
    }

    /**
     * This method will return text content of given taxonomy
     *
     * @param taxonomyName taxonomy file taxonomyName
     * @return String content of taxonomy file taxonomyName
     * @throws RegistryException
     */
    String getTaxonomy(String taxonomyName) throws RegistryException {
        return managementProvider.getTaxonomy(storageProvider, taxonomyName);
    }

    /**
     * This method will return taxonomyBean object for a given query bean
     *
     * @param taxonomyQueryBean Taxonomy query bean
     * @return taxonomy bean object
     */
    TaxonomyBean getTaxonomy(QueryBean taxonomyQueryBean) {
        return storageProvider.getTaxonomy(taxonomyQueryBean);
    }

    /**
     * This method will return all available taxonomy file list in specific registry location
     *
     * @return String array of taxonomy file names
     * @throws RegistryException
     */
    String[] getTaxonomyList() throws RegistryException {
        return managementProvider.getTaxonomyList(storageProvider);
    }

    /**
     * This method will retrieve the results for rest api queries
     *
     * @return Json array with processed results
     * @throws XPathExpressionException
     * @throws JSONException
     * @throws TaxonomyException
     */
    JSONArray query(QueryBean taxonomyQueryBean, PaginationBean taxonomyPaginationBean)
            throws XPathExpressionException, JSONException, TaxonomyException {
        NodeList nodeList = queryProvider.query(taxonomyQueryBean);
        return CommonUtils.toJson(taxonomyQueryBean.getQuery(), queryProvider.getUpdatedQuery(taxonomyQueryBean),
                taxonomyPaginationBean.getStartNode(), taxonomyPaginationBean.getEndNode(), nodeList);
    }

    public void initTaxonomyStorage()
            throws UserStoreException, RegistryException, ParserConfigurationException, SAXException, IOException {
        storageProvider.initTaxonomyStorage();
    }
}
