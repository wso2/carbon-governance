/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.governance.registry.extensions.indexers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrException;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.IndexingConstants;
import org.wso2.carbon.registry.indexing.IndexingManager;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JSONIndexer implements Indexer {

    public static final Log log = LogFactory.getLog(JSONIndexer.class);

    @Override
    public IndexDocument getIndexedDocument(AsyncIndexer.File2Index fileData) throws SolrException, RegistryException {

        if (log.isDebugEnabled()) {
            log.debug("Registry JSON Indexer is running");
        }

        IndexDocument indexedDocument = getPreProcessedDocument(fileData);
        Registry registry =
                GovernanceUtils.getGovernanceSystemRegistry(IndexingManager.getInstance().getRegistry(fileData.tenantId));
        GovernanceArtifactConfiguration configuration =
                GovernanceUtils.findGovernanceArtifactConfigurationByMediaType(fileData.mediaType, registry);
        if (configuration == null) {
            return indexedDocument;
        }
        Map<String, List<String>> fields = indexedDocument.getFields();
        setAttributesToLowerCase(fields);
        fields.put("overview_name", Arrays.asList(RegistryUtils.getResourceName(fileData.path).toLowerCase()));
        indexedDocument.setFields(fields);

        return indexedDocument;
    }

    private IndexDocument getPreProcessedDocument(AsyncIndexer.File2Index fileData) throws RegistryException {
        // we register both the content as it is and only text content
        String jsonAsStr = RegistryUtils.decodeBytes(fileData.data);

        final StringBuffer contentOnly = new StringBuffer();

        IndexDocument indexDocument = new IndexDocument(fileData.path, jsonAsStr,
                contentOnly.toString());
        Map<String, List<String>> attributes = new HashMap<>();
        if (fileData.mediaType != null) {
            attributes.put(IndexingConstants.FIELD_MEDIA_TYPE, Arrays.asList(fileData.mediaType));
        }
        if (fileData.lcState != null) {
            attributes.put(IndexingConstants.FIELD_LC_STATE, Arrays.asList(fileData.lcState));
        }
        if (fileData.lcName != null) {
            attributes.put(IndexingConstants.FIELD_LC_NAME, Arrays.asList(fileData.lcName));
        }
        indexDocument.setFields(attributes);
        return indexDocument;
    }

    private void setAttributesToLowerCase(Map<String, List<String>> attributes) {
        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            List<String> list = entry.getValue();
            if (list == null) continue;
            for (int i = 0; i < list.size(); i++) {
                list.set(i, list.get(i) != null ? list.get(i).toLowerCase() : list.get(i));
            }
        }
    }
}


