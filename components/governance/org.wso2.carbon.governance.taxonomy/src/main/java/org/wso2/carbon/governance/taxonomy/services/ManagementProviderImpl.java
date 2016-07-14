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

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.taxonomy.beans.TaxonomyBean;
import org.wso2.carbon.governance.taxonomy.clustering.ClusterMessage;
import org.wso2.carbon.governance.taxonomy.clustering.ClusteringUtil;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;

import static org.wso2.carbon.governance.taxonomy.util.CommonUtils.buildOMElement;
import static org.wso2.carbon.governance.taxonomy.util.TaxonomyConstants.TAXONOMY_CONFIGURATION_PATH;
import static org.wso2.carbon.governance.taxonomy.util.TaxonomyConstants.TAXONOMY_MEDIA_TYPE;

class ManagementProviderImpl extends RegistryAbstractAdmin implements IManagementProvider {

    /**
     * This method will add user defined taxonomy content into the registry
     *
     * @param storageProvider storage provider instance
     * @param taxonomyBean    taxonomy bean instance which contains taxonomy meta data
     * @return
     * @throws RegistryException
     */
    @Override
    public boolean addTaxonomy(IStorageProvider storageProvider, TaxonomyBean taxonomyBean)
            throws RegistryException {
        Registry registry = getGovernanceUserRegistry();
        Resource resource;
        String name;
        OMElement element = null;
        element = buildOMElement(taxonomyBean.getPayload());
        name = element.getAttributeValue(new QName("name"));

        if (!getGovernanceUserRegistry().resourceExists(TAXONOMY_CONFIGURATION_PATH + name)) {
            resource = new ResourceImpl();
            resource.setMediaType(TAXONOMY_MEDIA_TYPE);
            resource.setContent(taxonomyBean.getPayload());
            registry.put(TAXONOMY_CONFIGURATION_PATH + name, resource);
            storageProvider.addTaxonomy(taxonomyBean);
        } else {
            return false;
        }
        return true;
    }

    /**
     * This method will delete the given taxonomy file from registry
     *
     * @return boolean state of the action
     * @throws RegistryException
     */
    @Override
    public boolean deleteTaxonomy(IStorageProvider storageProvider, String taxonomyName)
            throws RegistryException {
        Registry registry = getGovernanceUserRegistry();

        if (getGovernanceUserRegistry().resourceExists(TAXONOMY_CONFIGURATION_PATH + taxonomyName)) {
            registry.delete(TAXONOMY_CONFIGURATION_PATH + taxonomyName);
            storageProvider.removeTaxonomy(taxonomyName);
            invalidateCache(taxonomyName);
            return true;
        } else {
            return false;
        }

    }

    private void invalidateCache(String taxonomyName) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ClusterMessage message = new ClusterMessage(tenantId, taxonomyName);
        ClusteringUtil.sendClusterMessage(message);
    }

    /**
     * This method will update the taxonomy file from the registry
     *
     * @param storageProvider Storage provider
     * @param oldName         String old name
     * @param taxonomyBean    Taxonomy bean taxonomy bean
     * @return boolean state of the action
     * @throws RegistryException
     */
    @Override
    public boolean updateTaxonomy(IStorageProvider storageProvider, String oldName, TaxonomyBean taxonomyBean)
            throws RegistryException {
        Registry registry = getGovernanceUserRegistry();

        String newName = null;
        OMElement element = null;
        element = buildOMElement(taxonomyBean.getPayload());

        if (element != null) {
            newName = element.getAttributeValue(new QName("name"));
        }

        if (newName == null || newName.equals("")) {
            return false; // invalid configuration
        }

        // add new resource with new name
        if (oldName == null || oldName.equals("")) {
            Resource resource;
            resource = new ResourceImpl();
            resource.setContent(taxonomyBean.getPayload());
            registry.put(TAXONOMY_CONFIGURATION_PATH + newName, resource);
            storageProvider.addTaxonomy(taxonomyBean);
            return true;
        }

        if (oldName.equals(newName)) {
            // add resource with same old name
            Resource resource = registry.get(TAXONOMY_CONFIGURATION_PATH + oldName);
            resource.setContent(taxonomyBean.getPayload());
            registry.put(TAXONOMY_CONFIGURATION_PATH + oldName, resource);
            storageProvider.updateTaxonomy(oldName, taxonomyBean);
            invalidateCache(oldName);
            return true;
        } else {
            // add new resource and remove old one
            Resource resource;
            resource = new ResourceImpl();
            resource.setContent(taxonomyBean.getPayload());
            registry.put(TAXONOMY_CONFIGURATION_PATH + newName, resource);
            storageProvider.addTaxonomy(taxonomyBean);
            storageProvider.removeTaxonomy(oldName);
            registry.delete(TAXONOMY_CONFIGURATION_PATH + oldName);
            invalidateCache(oldName);
            return true;

        }
    }

    /**
     * This method will retrieve the given taxonomy data from registry
     *
     * @return String type data
     * @throws RegistryException
     */
    @Override
    public String getTaxonomy(IStorageProvider storageProvider, String name) throws RegistryException {
        Registry registry = getGovernanceUserRegistry();

        if (getGovernanceUserRegistry().resourceExists(TAXONOMY_CONFIGURATION_PATH + name)) {
            Resource resource;
            resource = registry.get(TAXONOMY_CONFIGURATION_PATH + name);
            return RegistryUtils.decodeBytes((byte[]) resource.getContent());
        } else {
            return null;
        }
    }

    /**
     * This method will return all taxonomy file list inside given path of the registry
     *
     * @return String[] array
     * @throws RegistryException
     */
    @Override
    public String[] getTaxonomyList(IStorageProvider storageProvider) throws RegistryException {
        Collection collection;
        Registry registry = getGovernanceUserRegistry();

        if (registry.resourceExists(TAXONOMY_CONFIGURATION_PATH)) {
            collection = (Collection) registry.get(TAXONOMY_CONFIGURATION_PATH);

            if (collection.getChildCount() == 0) {
                return null;
            }

            String[] childrenList = collection.getChildren();
            String[] taxonomyNameList = new String[collection.getChildCount()];
            for (int i = 0; i < childrenList.length; i++) {
                String path = childrenList[i];
                taxonomyNameList[i] = path.substring(path.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
            }
            return taxonomyNameList;
        }
        return null;
    }

}
