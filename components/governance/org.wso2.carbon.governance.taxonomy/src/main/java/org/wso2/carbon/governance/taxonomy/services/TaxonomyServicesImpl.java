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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.wso2.carbon.governance.taxonomy.beans.TaxonomyBean;
import org.wso2.carbon.governance.taxonomy.beans.PaginationBean;
import org.wso2.carbon.governance.taxonomy.beans.QueryBean;
import org.wso2.carbon.governance.taxonomy.exception.TaxonomyException;
import org.wso2.carbon.governance.taxonomy.util.CommonUtils;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

public class TaxonomyServicesImpl extends RegistryAbstractAdmin implements ITaxonomyServices {
    private static final Log log = LogFactory.getLog(TaxonomyServicesImpl.class);
    /**
     * This method will add user defined taxonomy into taxonomy.xml
     *
     * @param payload String
     * @return boolean
     * @throws Exception
     */
    @Override public boolean
    addTaxonomy(String payload) throws Exception {
        TaxonomyManager taxonomyManager = new TaxonomyManager();
        try {
            TaxonomyBean documentBean = CommonUtils.documentBeanBuilder(payload);
            return taxonomyManager.addTaxonomy(documentBean);
        } catch (ParserConfigurationException e) {
            log.error("Error occurred while parsing payload ", e);
            throw (e);
        } catch (IOException e) {
            log.error("Error occurred while building document ", e);
            throw (e);
        } catch (SAXException e) {
            log.error("Error occurred while creating document instance ", e);
            throw (e);
        }
    }

    /**
     * This method will delete the taxonomy.xml file
     *
     * @return boolean
     * @throws Exception
     */
    @Override
    public boolean deleteTaxonomy(String name) throws Exception{
        TaxonomyManager taxonomyManager = new TaxonomyManager();
        try {
            return taxonomyManager.deleteTaxonomy(name);
        } catch (RegistryException e) {
            log.error("Error occurred while deleting taxonomy from registry ", e);
            throw (e);
        }
    }

    /**
     * This method will update the taxonomy.xml file with user defined data
     *
     * @param payload String
     * @return boolean
     * @throws Exception
     */
    @Override
    public boolean updateTaxonomy(String oldName, String payload) throws Exception{
        TaxonomyManager taxonomyManager = new TaxonomyManager();
        try {
            TaxonomyBean documentBean = CommonUtils.documentBeanBuilder(payload);
            return taxonomyManager.updateTaxonomy(oldName, documentBean);
        } catch (ParserConfigurationException e) {
            log.error("Error occurred while parsing payload ", e);
            throw (e);
        } catch (IOException e) {
            log.error("Error occurred while building document ", e);
            throw (e);
        } catch (SAXException e) {
            log.error("Error occurred while creating document instance ", e);
            throw (e);
        } catch (RegistryException e) {
            log.error("Error occurred while converting payload to a DOM instance ", e);
            throw (e);
        }
    }

    /**
     * This method will return text content of given taxonomy name (This will be a registry call)
     *
     * @return String type data
     * @throws Exception
     */
    @Override
    public String getTaxonomy(String name) throws Exception {
        try {
            TaxonomyManager taxonomyManager = new TaxonomyManager();
            return taxonomyManager.getTaxonomy(name);
        } catch (RegistryException e) {
            log.error("Error occurred while reading taxonomy from registry ", e);
            throw (e);
        }
    }

    /**
     * This method will return taxonomyBean object for a given query bean (This will return from map)
     * (cost will be low comparing to registry call)
     * @param queryBean query bean object
     * @return TaxonomyBean object
     * @throws RegistryException
     */
    @Override
    public TaxonomyBean getTaxonomyBean(QueryBean queryBean) throws RegistryException {
        TaxonomyManager taxonomyManager = new TaxonomyManager();
        return taxonomyManager.getTaxonomy(queryBean);
    }

    /**
     * This method will retrieve all taxonomy file list
     *
     * @return Array of Strings which contains taxonomy file list
     * @throws Exception
     */
    public String[] getTaxonomyList() throws Exception {
        try {
            TaxonomyManager taxonomyManager = new TaxonomyManager();
            return taxonomyManager.getTaxonomyList();
        } catch (RegistryException e) {
            log.error("Error occurred while reading taxonomy list from registry ", e);
            throw (e);
        }
    }

    /**
     * This method will return processed results for
     *
     * @param taxonomyQueryBean Taxonomy meta data bean object
     * @param paginationBean pagination meta data
     * @return json array of results
     * @throws TaxonomyException
     */
    @Override
    public JSONArray query(QueryBean taxonomyQueryBean, PaginationBean paginationBean)
            throws TaxonomyException {
        TaxonomyManager taxonomyManager = new TaxonomyManager();
        try {
            return taxonomyManager.query(taxonomyQueryBean, paginationBean);
        } catch (XPathExpressionException e) {
            throw new TaxonomyException("Error occurred while compiling xpath ", e);
        } catch (JSONException e) {
            throw new TaxonomyException("Error occurred while parsing to json ", e);
        }
    }

    @Override
    public JSONArray getTaxonomyName(QueryBean taxonomyQueryBean) throws TaxonomyException {
        TaxonomyManager taxonomyManager = new TaxonomyManager();
        try {
            return taxonomyManager.query(taxonomyQueryBean);
        } catch (XPathExpressionException e) {
            throw new TaxonomyException("Error occurred while compiling xpath ", e);
        } catch (JSONException e) {
            throw new TaxonomyException("Error occurred while parsing to json ", e);
        } catch (UserStoreException e) {
            throw new TaxonomyException("Error occurred while user authenticating ", e);
        } catch (RegistryException e) {
            throw new TaxonomyException("Error occurred while reading rxt list for tenant ", e);
        }
    }

    @Override
    /**
     * This method will return all available taxonomies for a specific tenant
     * @return  Map of taxonomy bean objects
     */
    public Map<String, TaxonomyBean> getTaxonomyBeanMap () {
        TaxonomyManager taxonomyManager = new TaxonomyManager();
        return taxonomyManager.getTaxonomyBeanMap();
    }

}
