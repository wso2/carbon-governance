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

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTaxonomyName() {
        return taxonomyName;
    }

    public void setTaxonomyName(String taxonomyName) {
        this.taxonomyName = taxonomyName;
    }

    public void setPath(String registryPath) {
        this.registryPath = registryPath;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getPath() {
        return this.registryPath;
    }

    public Document getDocument() {
        return this.document;
    }

    public void setGlobal(boolean taxonomyType) {
        this.isGlobal = taxonomyType;
    }

    public boolean isTaxonomyGlobal() {
        return this.isGlobal;
    }

}
