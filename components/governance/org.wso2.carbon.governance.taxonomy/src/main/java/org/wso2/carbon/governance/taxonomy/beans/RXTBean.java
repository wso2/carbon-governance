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

import java.util.HashMap;
import java.util.Map;

/**
 * This is bean class for keeping rxt meta data
 */
public class RXTBean {
    private String rxtName;
    private Map<String, Map<String, Boolean>> taxonomies = new HashMap<>();

    /**
     * This method will set the RXT name for RXT Bean object
     *
     * @param setRxtName String RXT name
     */
    public void setRxtName(String setRxtName) {
        this.rxtName = setRxtName;
    }

    /**
     * This method will set the taxonomy map details for RXT Bean object
     *
     * @param taxonomies Map contain (taxonomy name , (global value, is enable ))
     */
    public void setTaxonomy(Map<String, Map<String, Boolean>> taxonomies) {
        this.taxonomies = taxonomies;
    }

    /**
     * This method will return the rxt name of RXT Bean object
     *
     * @return String RXT name
     */
    public String getRxtName() {
        return rxtName;
    }

    /**
     * This method will return the taxonomy map
     *
     * @return Map contain (taxonomy name , (global value, is enable ))
     */
    public Map<String, Map<String, Boolean>> getTaxonomy() {
        return taxonomies;
    }

}
