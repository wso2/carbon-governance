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

/**
 * This class will store taxonomy query meta data
 */
public class QueryBean {

    private String taxonomyName;
    private String query;
    private String assetType;

    /**
     * This method will return the asset type of query bean object
     *
     * @return String asset type
     */
    public String getAssetType() {
        return assetType;
    }

    /**
     * This method will set the asset type of query bean object
     *
     * @param assetType String asset type
     */
    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    /**
     * This method will return the taxonomy name of query bean object
     *
     * @return String taxonomy name
     */
    public String getTaxonomyName() {
        return taxonomyName;
    }

    /**
     * This method will set the taxonomy name of query bean object
     *
     * @param taxonomyName String taxonomy name
     */
    public void setTaxonomyName(String taxonomyName) {
        this.taxonomyName = taxonomyName;
    }

    /**
     * This method will return the query of query bean object; for example path to a specific node (root/nodeA1/nodeB2)
     *
     * @return String query of query bean object
     */
    public String getQuery() {
        return query;
    }

    /**
     * This method will set the query of query bean object; for example path to a specific node (root/nodeA1/nodeB2)
     *
     * @param query String query
     */
    public void setQuery(String query) {
        this.query = query;
    }

}
