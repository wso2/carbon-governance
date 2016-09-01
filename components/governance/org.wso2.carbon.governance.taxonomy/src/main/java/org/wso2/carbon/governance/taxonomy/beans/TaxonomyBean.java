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

package org.wso2.carbon.governance.taxonomy.beans;

import org.w3c.dom.Document;

/**
 * This class will use to keep taxonomy meta data
 */
public class TaxonomyBean {
    private String registryPath;
    private Document document;
    private boolean isGlobal;
    private String taxonomyName;
    private String payload;

    /**
     * This method will return the taxonomy xml file content in Taxonomy Bean object
     *
     * @return String taxonomy file content
     */
    public String getPayload() {
        return payload;
    }

    /**
     * This method will set the taxonomy payload content in Taxonomy Bean object
     *
     * @param payload String payload content
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }

    /**
     * This method will return the taxonomy name of Taxonomy Bean object
     *
     * @return String taxonomy name
     */
    public String getTaxonomyName() {
        return taxonomyName;
    }

    /**
     * This method will set the taxonomy name of Taxonomy Bean object
     *
     * @param taxonomyName String taxonomy name
     */
    public void setTaxonomyName(String taxonomyName) {
        this.taxonomyName = taxonomyName;
    }

    /**
     * This method will set the registry path of Taxonomy Bean object
     *
     * @param registryPath String registry path
     */
    public void setPath(String registryPath) {
        this.registryPath = registryPath;
    }

    /**
     * This method will set the parsed taxonomy content as document in Taxonomy Bean object
     *
     * @param document parsed document type taxonomy content
     */
    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * This method will return the path to registry in Taxonomy Bean object
     *
     * @return String path to registry
     */
    public String getPath() {
        return this.registryPath;
    }

    /**
     * This method will return the parsed taxonomy content in Taxonomy Bean object
     *
     * @return taxonomy document model
     */
    public Document getDocument() {
        return this.document;
    }

    /**
     * This method will set the taxonomy is global or not in Taxonomy Bean object
     *
     * @param taxonomyType Boolean value of taxonomy globalism
     */
    public void setGlobal(boolean taxonomyType) {
        this.isGlobal = taxonomyType;
    }

    /**
     * This method will return the taxonomy globalism in Taxonomy Bean object
     *
     * @return Boolean value of taxonomy globalism
     */
    public boolean isTaxonomyGlobal() {
        return this.isGlobal;
    }

}
