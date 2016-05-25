/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.taxonomy.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.util.SecurityManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.governance.taxonomy.internal.ServiceHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/***
 * This class is use to populate category list from admin defined XML document
 */
public class TaxonomyCategoryParser {
    public static final String INPUT_XML = "/taxonomy/taxonomy.xml";
    private static final Log log = LogFactory.getLog(TaxonomyCategoryParser.class);
    private static Stack<HashMap<String, String>> elementStack = new Stack<>();
    private static JSONObject jsonPaths = new JSONObject();
    private static final int ENTITY_EXPANSION_LIMIT = 0;

    /***
     * This method is use to generate the path from the stack of element names
     */
    private static void addPathToCategories() throws JSONException {
        StringBuilder path = new StringBuilder();
        String leafNodeDisplayName = null;
        for (HashMap<String, String> elements : elementStack) {
            if (path.length() > 0) {
                path.append("/");
            }
            for (Map.Entry<String, String> entry : elements.entrySet()) {
                path.append(entry.getKey());
                if (entry.getValue() != null) {
                    leafNodeDisplayName = entry.getValue();
                } else {
                    leafNodeDisplayName = path.toString();
                }
            }

        }

        jsonPaths.put(path.toString(), leafNodeDisplayName);
    }

    /***
     * This method is use to go through xml DOM and push the elements names into a stack
     *
     * @param childNodes
     */
    private static void loopNodes(NodeList childNodes) throws JSONException {
        for (int i = 0; i < childNodes.getLength(); ++i) {
            HashMap<String, String> tempHashMap = new HashMap<>();
            Node node = childNodes.item(i);
            String nodeName = node.getNodeName();
            if (!"#text".equals(nodeName)) {

                Attr attr = (Attr) node.getAttributes().getNamedItem("displayname");
                String attribute = null;
                if (attr != null) {
                    attribute = attr.getValue();
                }

                if (attribute != null) {
                    tempHashMap.put(node.getNodeName(), attribute);
                    elementStack.push(tempHashMap);
                } else {
                    tempHashMap.put(node.getNodeName(), null);
                    elementStack.push(tempHashMap);
                }

                if (node.hasChildNodes()) {
                    loopNodes(node.getChildNodes());
                } else {
                    addPathToCategories();
                }

                if (elementStack.size() > 0) {
                    elementStack.pop();
                }

            }
        }

    }

    /***
     * This method is use to populate the paths from XML document
     *
     * @return List of Strings
     */
    public static JSONObject getPathCategories() throws RegistryException, JSONException, IOException, SAXException {
        Registry registry = ServiceHolder.getRegistryService().getGovernanceSystemRegistry();
        try {
            DocumentBuilderFactory factory = getSecuredDocumentBuilder();
            Document doc = null;
            if (Utils.getTaxonomyService() == null) {
                doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(registry.get(INPUT_XML).
                        getContentStream());

                TaxonomyStorageService ins = new TaxonomyStorageService();
                ins.addParseDocument(doc);
                Utils.setTaxonomyService(ins);
            } else {
                doc = Utils.getTaxonomyService().getParsedDocument();
            }

            NodeList childNodes = doc.getChildNodes();
            loopNodes(childNodes);

        } catch (ParserConfigurationException e) {
            log.error("Error occur while parsing the xml document ", e);
        }

        return jsonPaths;
    }

    /**
     * Returns a secured DocumentBuilderFactory instance
     * @return DocumentBuilderFactory
     */
    public static DocumentBuilderFactory getSecuredDocumentBuilder() {

        org.apache.xerces.impl.Constants Constants = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (ParserConfigurationException e) {
            log.error(
                    "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                    Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        org.apache.xerces.util.SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;
    }

}


