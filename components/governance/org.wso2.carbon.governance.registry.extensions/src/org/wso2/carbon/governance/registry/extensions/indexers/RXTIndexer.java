/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.registry.extensions.indexers;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrException;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifactImpl;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.IndexingManager;
import org.wso2.carbon.registry.indexing.indexer.Indexer;
import org.wso2.carbon.registry.indexing.indexer.XMLIndexer;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RXTIndexer extends XMLIndexer implements Indexer {

    public static final Log log = LogFactory.getLog(RXTIndexer.class);

    public IndexDocument getIndexedDocument(AsyncIndexer.File2Index fileData) throws SolrException, RegistryException {
        IndexDocument indexedDocument = super.getIndexedDocument(fileData);
        try {
            Registry registry =
                    GovernanceUtils.getGovernanceSystemRegistry(IndexingManager.getInstance().getRegistry(fileData.tenantId));
            GovernanceArtifactConfiguration configuration =
                    GovernanceUtils.findGovernanceArtifactConfigurationByMediaType(fileData.mediaType, registry);
            if (configuration == null) {
                return indexedDocument;
            }
            GenericArtifactManager manager = new GenericArtifactManager(registry, configuration.getKey());
            String xmlAsStr = RegistryUtils.decodeBytes(fileData.data);
            IndexableGovernanceArtifact artifact = new IndexableGovernanceArtifact((GovernanceArtifactImpl)
                    manager.newGovernanceArtifact(AXIOMUtil.stringToOM(xmlAsStr)));
            Map<String, List<String>> fields = indexedDocument.getFields();
            setAttributesToLowerCase(fields);
            Map<String, List<String>> attributes = artifact.getAttributes();
            //Content artifact (policy, wsdl, schema ...etc) doesn't contains the attributes.
            if (fileData.mediaType.matches("application/vnd.(.)+\\+xml") && attributes.size() > 0) {
                setAttributesToLowerCase(attributes);
                fields.putAll(attributes);
            } else {
                fields.put("overview_name", Arrays.asList(RegistryUtils.getResourceName(fileData.path).toLowerCase()));
            }
            indexedDocument.setFields(fields);
            if (log.isDebugEnabled()) {
                log.debug("Registry RXT Indexer is running");
            }
            
        } catch (XMLStreamException e) {
            log.error("Unable to parse XML", e);
        }

        return indexedDocument;

    }

    private void setAttributesToLowerCase(Map<String, List<String>> attributes){
        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            List<String> list = entry.getValue();
            if (list == null) continue;
            for (int i = 0; i < list.size(); i++) {
                list.set(i, list.get(i) != null ? list.get(i).toLowerCase() : list.get(i));
            }
        }
    }

    private class IndexableGovernanceArtifact extends GovernanceArtifactImpl {

        private IndexableGovernanceArtifact(GovernanceArtifactImpl artifact) {
            super(artifact);
        }

        public QName getQName() {
            throw new UnsupportedOperationException();
        }

        public Map<String, List<String>> getAttributes() {
            return attributes;
        }
    }
}
