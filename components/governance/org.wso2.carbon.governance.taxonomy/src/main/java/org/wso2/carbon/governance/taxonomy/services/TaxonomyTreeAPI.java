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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.governance.taxonomy.internal.ServiceHolder;
import org.wso2.carbon.governance.taxonomy.util.TaxonomyStorageService;
import org.wso2.carbon.governance.taxonomy.util.Utils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.xml.sax.SAXException;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

/**
 * This class is use to populate taxonomy json response related to REST API calls
 */
public class TaxonomyTreeAPI implements TaxonomyService {
    public static final String INPUT_XML = "/taxonomy/taxonomy.xml";
    public static final String ELEMENT_NAME = "elementName";
    public static final String DISPLAY_NAME = "displayName";
    public static final String TEXT = "text";
    public static final String CHILDREN = "children";
    private static final Log log = LogFactory.getLog(TaxonomyTreeAPI.class);

    /***
     * This method will return a json array related to REST API user's input parameters
     *
     * @param query     user query to select specific branch of the tree
     * @param startNode start node of tree branch
     * @param endNode   end node of tree branch
     * @return JSON array array of child nodes
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     * @throws JSONException
     * @throws RegistryException
     */
    @Override
    public JSONArray getNodes(String query, int startNode, int endNode)
            throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, JSONException,
            RegistryException {
        Registry registry = ServiceHolder.getRegistryService().getGovernanceSystemRegistry();
        Document doc = null;
        // First time execute , this will parse taxonomy.xml file and store inside a map which is tenant specific
        if (Utils.getTaxonomyService() == null) {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(registry.get(INPUT_XML).getContentStream());
            TaxonomyStorageService xPath = new TaxonomyStorageService();
            xPath.addParseDocument(doc);
            Utils.setTaxonomyService(xPath);
        } else {
            // If this is not first time, then read the tenant specific map and retrieve from there
            doc = Utils.getTaxonomyService().getParsedDocument();
        }

        XPath xPathInstance = XPathFactory.newInstance().newXPath();
        XPathExpression exp = xPathInstance.compile(query.replace(CHILDREN, "*"));
        NodeList nodeList = (NodeList) exp.evaluate(doc, XPathConstants.NODESET);
        JSONArray childrenArray = new JSONArray();
        JSONArray mainArray = new JSONArray();
        JSONObject itemRelativeRoot = new JSONObject();
        if (!query.equals("/*") && !query.equals("*")) {
            itemRelativeRoot.put(ELEMENT_NAME, nodeList.item(0).getParentNode().getNodeName());
            if (((Element) nodeList.item(0).getParentNode()).hasAttribute(DISPLAY_NAME)) {
                itemRelativeRoot.put(TEXT, ((Element) nodeList.item(0).getParentNode()).getAttribute(DISPLAY_NAME));
            } else {
                itemRelativeRoot.put(TEXT, "");
            }
        } else {
            exp = xPathInstance.compile("/" + nodeList.item(0).getNodeName() + "/" + "*");
            nodeList = (NodeList) exp.evaluate(doc, XPathConstants.NODESET);
            itemRelativeRoot.put(ELEMENT_NAME, nodeList.item(0).getParentNode().getNodeName());
            if (((Element) nodeList.item(0).getParentNode()).hasAttribute(DISPLAY_NAME)) {
                itemRelativeRoot.put(TEXT, ((Element) nodeList.item(0).getParentNode()).getAttribute(DISPLAY_NAME));
            } else {
                itemRelativeRoot.put(TEXT, "");
            }
        }

        int nodeCount;
        if (nodeList.getLength() > endNode) {
            nodeCount = endNode;
        } else {
            nodeCount = nodeList.getLength();
        }

        JSONObject dataArray;
        if (!query.equals("/*") && !query.equals("*")) {
            for (int i = startNode; i < nodeCount; ++i) {
                dataArray = new JSONObject();
                new JSONObject();
                dataArray.put(ELEMENT_NAME, nodeList.item(i).getNodeName());
                if (((Element) nodeList.item(i)).hasAttribute(DISPLAY_NAME)) {
                    dataArray.put(TEXT, ((Element) nodeList.item(i)).getAttribute(DISPLAY_NAME));
                } else {
                    dataArray.put(TEXT, "");
                }

                dataArray.put(CHILDREN, nodeList.item(i).hasChildNodes());
                mainArray.put(dataArray);
            }
        } else {
            itemRelativeRoot.put(CHILDREN, childrenArray);
            mainArray.put(itemRelativeRoot);

            for (int i = startNode; i < nodeCount; ++i) {
                dataArray = new JSONObject();
                new JSONObject();
                dataArray.put(ELEMENT_NAME, nodeList.item(i).getNodeName());
                if (((Element) nodeList.item(i)).hasAttribute(DISPLAY_NAME)) {
                    dataArray.put(TEXT, ((Element) nodeList.item(i)).getAttribute(DISPLAY_NAME));
                } else {
                    dataArray.put(TEXT, "");
                }

                dataArray.put(CHILDREN, nodeList.item(i).hasChildNodes());
                childrenArray.put(dataArray);
            }
        }

        return mainArray;
    }

    /***
     * This method returns the taxonomy.xml file availability
     *
     * @return String taxonomy.xml availability
     * @throws RegistryException
     */
    @Override
    public Boolean getTaxonomyAvailability() throws RegistryException {
        Registry registry = ServiceHolder.getRegistryService().getGovernanceSystemRegistry();

        if (registry.resourceExists(INPUT_XML)) {
            return  true;
        } else {
            return  false;
        }
    }

    /***
     * This method returns the last modified time of given resource.
     *
     * @return String last updated time as string
     * @throws RegistryException
     */
    @Override
    public String getLastModifiedTime() throws RegistryException {
        Registry registry = ServiceHolder.getRegistryService().getGovernanceSystemRegistry();
        return registry.get(INPUT_XML).getLastModified().toString();
    }
}
