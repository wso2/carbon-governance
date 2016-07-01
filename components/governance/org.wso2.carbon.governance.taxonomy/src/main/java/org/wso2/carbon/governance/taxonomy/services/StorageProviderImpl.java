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

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.taxonomy.beans.QueryBean;
import org.wso2.carbon.governance.taxonomy.beans.TaxonomyBean;
import org.wso2.carbon.governance.taxonomy.internal.ServiceHolder;
import org.wso2.carbon.governance.taxonomy.util.CommonUtils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import static org.wso2.carbon.registry.core.RegistryConstants.GOVERNANCE_COMPONENT_PATH;

/**
 * this class will implements methods to manage tenant specific taxonomy data map
 */
class StorageProviderImpl implements IStorageProvider {
    private static Map<Integer, Map<String, TaxonomyBean>> tenantTaxonomyMap;
    private int tenantId;

    StorageProviderImpl() {
        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    /**
     * This method will return taxonomy document bean object for a given taxonomy taxonomyName from the map
     *
     * @param taxonomyName String taxonomyName of the taxonomy (this must be unique for a tenant)
     * @return Taxonomy document bean which contains taxonomy meta data
     */
    @Override
    public TaxonomyBean getTaxonomy(String taxonomyName) {
        if (tenantTaxonomyMap != null) {
            if (tenantTaxonomyMap.containsKey(tenantId)) {
                return tenantTaxonomyMap.get(tenantId).get(taxonomyName);
            }
        }
        return null;
    }

    /**
     * This method will return taxonomy document bean object for a given taxonomy taxonomyName from the map
     *
     * @param taxonomyQueryBean taxonomy query meta data
     * @return Taxonomy document bean which contains taxonomy meta data
     */
    @Override
    public TaxonomyBean getTaxonomy(QueryBean taxonomyQueryBean) {
        if (tenantTaxonomyMap != null) {
            if (tenantTaxonomyMap.containsKey(tenantId)) {
                return tenantTaxonomyMap.get(tenantId).get(taxonomyQueryBean.getTaxonomyName());
            }
        }
        return null;
    }

    /**
     * This method will remove taxonomy document bean object from the map for a given taxonomy name
     *
     * @param taxonomyName String taxonomy name
     */
    @Override
    public void removeTaxonomy(String taxonomyName) {
        if (tenantTaxonomyMap != null) {
            if (tenantTaxonomyMap.containsKey(tenantId)) {
                tenantTaxonomyMap.get(tenantId).remove(taxonomyName);
            }
        }
    }

    /**
     * This method will add taxonomy document bean object to the map for a given taxonomy name
     *
     * @param taxonomyBean Taxonomy document bean which contains taxonomy meta data
     */
    @Override
    public void addTaxonomy(TaxonomyBean taxonomyBean) {
        if (tenantTaxonomyMap != null) {
            Map<String, TaxonomyBean> taxonomyMaps = tenantTaxonomyMap.get(tenantId);
            if (taxonomyMaps != null) {
                taxonomyMaps.put(taxonomyBean.getTaxonomyName(), taxonomyBean);
                tenantTaxonomyMap.put(tenantId, taxonomyMaps);
            } else {
                Map<String, TaxonomyBean> tempTaxonomyMap = new HashMap<>();
                tempTaxonomyMap.put(taxonomyBean.getTaxonomyName(), taxonomyBean);
                tenantTaxonomyMap.put(tenantId, tempTaxonomyMap);
            }
        } else {
            tenantTaxonomyMap = new HashMap<>();
            Map<String, TaxonomyBean> tempTaxonomyMap = new HashMap<>();
            tempTaxonomyMap.put(taxonomyBean.getTaxonomyName(), taxonomyBean);
            tenantTaxonomyMap.put(tenantId, tempTaxonomyMap);
        }
    }

    /**
     * This method will update taxonomy document bean object in the map for a given taxonomy name
     *
     * @param oldName      String name of the existing name
     * @param taxonomyBean Taxonomy document bean object
     */
    @Override
    public void updateTaxonomy(String oldName, TaxonomyBean taxonomyBean) {
        if (tenantTaxonomyMap != null) {
            Map<String, TaxonomyBean> taxonomyMaps = tenantTaxonomyMap.get(tenantId);

            if (taxonomyMaps.containsKey(oldName)) {
                taxonomyMaps.remove(oldName);
                taxonomyMaps.put(taxonomyBean.getTaxonomyName(), taxonomyBean);
                tenantTaxonomyMap.put(tenantId, taxonomyMaps);

            }

        }
    }

    /**
     * This method will initialize taxonomy maps with all taxonomy data.
     *
     * @throws UserStoreException           throws while getting RealmConfigurations
     * @throws RegistryException            throws getting files from registry
     * @throws IOException                  throws when reading file
     * @throws SAXException                 throws when parsing content stream
     * @throws ParserConfigurationException
     */
    @Override
    public void initTaxonomyStorage()
            throws UserStoreException, RegistryException, IOException, SAXException, ParserConfigurationException {
        String TAXONOMY_PATH = GOVERNANCE_COMPONENT_PATH + "/taxonomy";
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String adminName = ServiceHolder.getRealmService().getTenantUserRealm(tenantId).getRealmConfiguration()
                .getAdminUserName();
        Registry registry = ServiceHolder.getRegistryService().getGovernanceUserRegistry(adminName, tenantId);
        if (registry.resourceExists(TAXONOMY_PATH)) {
            Collection collection = (Collection) registry.get(TAXONOMY_PATH);
            String[] childrenList = collection.getChildren();
            for (String child : childrenList) {
                String myString = IOUtils.toString(registry.get(child).getContentStream(), "UTF-8");
                TaxonomyBean taxonomyDocumentBean = CommonUtils.documentBeanBuilder(myString);
                addTaxonomy(taxonomyDocumentBean);
            }
        }

    }

    @Override
    public List<String> getTaxonomiesByRXT(String name) {
        return null;
    }
}
