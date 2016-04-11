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

package org.wso2.carbon.governance.api.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.extensions.services.TaxonomyStorageService;
import org.wso2.carbon.registry.extensions.services.Utils;
import org.xml.sax.SAXException;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

public class TaxonomyTreeAPI {
    public static final String INPUT_XML = "/taxonomy/taxonomy.xml";
    public static final String ELEMENT_NAME = "elementName";
    public static final String DISPLAY_NAME = "displayName";
    public static final String TEXT = "text";
    public static final String CHILDREN = "children";
    private static final Log log = LogFactory.getLog(TaxonomyTreeAPI.class);

    public TaxonomyTreeAPI() {
    }

    public static JSONArray getNodes(String query, int startNode, int endNode)
            throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, JSONException,
            RegistryException {
        boolean loopCount = false;
        UserRegistry registry = RegistryCoreServiceComponent.getRegistryService().getGovernanceSystemRegistry();
        Document doc = null;
        if (Utils.getTaxonomyService() == null) {

            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(registry.get(INPUT_XML).getContentStream());
            TaxonomyStorageService xPath = new TaxonomyStorageService();
            xPath.addParseDocument(doc);
            Utils.setTaxonomyService(xPath);
        } else {
            doc = Utils.getTaxonomyService().getParsedDocument();
        }

        XPath xPathInstance = XPathFactory.newInstance().newXPath();
        XPathExpression exp = xPathInstance.compile(query);
        NodeList nodeList = (NodeList) exp.evaluate(doc, XPathConstants.NODESET);
        new JSONObject();
        new JSONObject();
        JSONArray jary = new JSONArray();
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

        int i;
        JSONObject jobject;
        if (!query.equals("/*") && !query.equals("*")) {
            for (i = startNode; i < nodeCount; ++i) {
                jobject = new JSONObject();
                new JSONObject();
                jobject.put(ELEMENT_NAME, nodeList.item(i).getNodeName());
                if (((Element) nodeList.item(i)).hasAttribute(DISPLAY_NAME)) {
                    jobject.put(TEXT, ((Element) nodeList.item(i)).getAttribute(DISPLAY_NAME));
                } else {
                    jobject.put(TEXT, "");
                }

                jobject.put(CHILDREN, nodeList.item(i).hasChildNodes());
                mainArray.put(jobject);
            }
        } else {
            itemRelativeRoot.put(CHILDREN, jary);
            mainArray.put(itemRelativeRoot);

            for (i = startNode; i < nodeCount; ++i) {
                jobject = new JSONObject();
                new JSONObject();
                jobject.put(ELEMENT_NAME, nodeList.item(i).getNodeName());
                if (((Element) nodeList.item(i)).hasAttribute(DISPLAY_NAME)) {
                    jobject.put(TEXT, ((Element) nodeList.item(i)).getAttribute(DISPLAY_NAME));
                } else {
                    jobject.put(TEXT, "");
                }

                jobject.put(CHILDREN, nodeList.item(i).hasChildNodes());
                jary.put(jobject);
            }
        }

        return mainArray;
    }
}
