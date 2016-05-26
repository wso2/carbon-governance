/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.taxonomy.services;

import org.json.JSONArray;
import org.json.JSONException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.xml.sax.SAXException;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

public interface TaxonomyService {
    public JSONArray getNodes(String query, int startNode, int endNode)
            throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, JSONException,
            RegistryException, UserStoreException;

    public Boolean getTaxonomyAvailability() throws RegistryException, UserStoreException;

    public String getLastModifiedTime() throws RegistryException, UserStoreException;

}
