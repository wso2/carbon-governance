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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.taxonomy.beans.RXTBean;
import org.wso2.carbon.governance.taxonomy.beans.TaxonomyBean;
import org.wso2.carbon.governance.taxonomy.exception.TaxonomyException;
import org.wso2.carbon.governance.taxonomy.internal.ServiceHolder;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.xml.sax.SAXException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static org.wso2.carbon.governance.taxonomy.util.TaxonomyConstants.TAXONOMY_CONFIGURATION_PATH;

public class CommonUtils {
    private static final String ELEMENT_ID = "id";
    private static final String PREVIOUS_ELEMENT_PATH = "previousSibling";
    private static final String CURRENT_ELEMENT_PATH = "currentElement";
    private static final String NEXT_ELEMENT_PATH = "nextSibling";
    private static final String DISPLAY_NAME = "displayName";
    // two different usage of id constant
    private static final String ID = "id";
    private static final String TEXT = "label";
    private static final String CHILDREN = "children";

    /**
     * This method will return list of rxt bean objects with taxonomy meta data
     *
     * @return rxt bean objects
     * @throws UserStoreException
     * @throws RegistryException
     */
    public static List<RXTBean> getRxtTaxonomies() throws UserStoreException, RegistryException {

        List<RXTBean> rxtBeenList = new ArrayList<>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String adminName = ServiceHolder.getRealmService().getTenantUserRealm(tenantId).getRealmConfiguration()
                .getAdminUserName();
        Registry registry = ServiceHolder.getRegistryService().getGovernanceUserRegistry(adminName, tenantId);
        RealmService realmService = registry.getRegistryContext().getRealmService();

        String configurationPath = RegistryConstants.GOVERNANCE_COMPONENT_PATH + "/types/";
        if (realmService.getTenantUserRealm(realmService.getTenantManager().getTenantId(tenantDomain))
                .getAuthorizationManager().isUserAuthorized(adminName, configurationPath, ActionConstants.GET)) {

            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            List<GovernanceArtifactConfiguration> configurations = GovernanceUtils.
                    findGovernanceArtifactConfigurations(registry);
            for (GovernanceArtifactConfiguration governanceArtifactConfiguration : configurations) {
                RXTBean rxtBean = new RXTBean();

                if (governanceArtifactConfiguration.getTaxonomy() != null) {
                    rxtBean.setTaxonomy(governanceArtifactConfiguration.getTaxonomy());
                }
                rxtBean.setRxtName(governanceArtifactConfiguration.getKey());
                rxtBeenList.add(rxtBean);
            }

        }
        return rxtBeenList;

    }

    /**
     * This method will process the rest api request and return json array
     *
     * @param query        user query request
     * @param updatedQuery updated query
     * @param startNode    start node
     * @param endNode      end node
     * @param nodeList     xml node list
     * @return json array
     * @throws TaxonomyException
     * @throws JSONException
     */
    public static JSONArray toJson(String query, String updatedQuery, int startNode, int endNode, NodeList nodeList)
            throws TaxonomyException, JSONException {
        JSONArray childrenArray = new JSONArray();
        JSONArray mainArray = new JSONArray();
        JSONObject itemRelativeRoot = new JSONObject();

        if (nodeList.getLength() == 0) {
            throw new TaxonomyException("No results for provided query : " + query + ".Please provide valid query.");
        }

        itemRelativeRoot.put(ELEMENT_ID, ((Element) nodeList.item(0).getParentNode()).getAttribute(ID));
        itemRelativeRoot.put(TEXT, ((Element) nodeList.item(0).getParentNode()).getAttribute(DISPLAY_NAME));

        int nodeCount;
        if (nodeList.getLength() > endNode) {
            nodeCount = endNode;
        } else {
            nodeCount = nodeList.getLength();
        }

        JSONObject dataArray;
        if (!updatedQuery.equals("/taxonomy/root/*")) {
            // this will execute when there is long path
            for (int i = startNode; i < nodeCount; ++i) {
                dataArray = new JSONObject();

                String currentId = ((Element) nodeList.item(i)).getAttribute(ID);

                dataArray.put(ELEMENT_ID, currentId);
                //                dataArray.put(ID, currentId);
                dataArray.put(TEXT, ((Element) nodeList.item(i)).getAttribute(DISPLAY_NAME));
                dataArray.put(CHILDREN, nodeList.item(i).hasChildNodes());

                if (nodeCount == 1) {
                    dataArray.put(PREVIOUS_ELEMENT_PATH, "");
                    dataArray.put(CURRENT_ELEMENT_PATH, query.substring(0, query.lastIndexOf("/")) + "/" + currentId);
                    dataArray.put(NEXT_ELEMENT_PATH, "");
                } else if (i == 0) {
                    dataArray.put(PREVIOUS_ELEMENT_PATH, "");
                    dataArray.put(CURRENT_ELEMENT_PATH, query.substring(0, query.lastIndexOf("/")) + "/" + currentId);
                    dataArray.put(NEXT_ELEMENT_PATH,
                            query.substring(0, query.lastIndexOf("/")) + "/" + ((Element) nodeList.item(i + 1))
                                    .getAttribute(ID));
                } else if (i == nodeCount - 1) {
                    dataArray.put(PREVIOUS_ELEMENT_PATH,
                            query.substring(0, query.lastIndexOf("/")) + "/" + ((Element) nodeList.item(i - 1))
                                    .getAttribute(ID));
                    dataArray.put(CURRENT_ELEMENT_PATH, query.substring(0, query.lastIndexOf("/")) + "/" + currentId);
                    dataArray.put(NEXT_ELEMENT_PATH, "");
                } else {
                    dataArray.put(PREVIOUS_ELEMENT_PATH,
                            query.substring(0, query.lastIndexOf("/")) + "/" + ((Element) nodeList.item(i - 1))
                                    .getAttribute(ID));
                    dataArray.put(CURRENT_ELEMENT_PATH, query.substring(0, query.lastIndexOf("/")) + "/" + currentId);
                    dataArray.put(NEXT_ELEMENT_PATH,
                            query.substring(0, query.lastIndexOf("/")) + "/" + ((Element) nodeList.item(i + 1))
                                    .getAttribute(ID));

                }

                mainArray.put(dataArray);
            }
        } else {
            itemRelativeRoot.put(CHILDREN, childrenArray);
            mainArray.put(itemRelativeRoot);
            String rootId = ((Element) nodeList.item(0).getParentNode()).getAttribute(ID);
            for (int i = startNode; i < nodeCount; ++i) {
                dataArray = new JSONObject();
                new JSONObject();
                String currentId = ((Element) nodeList.item(i)).getAttribute(ID);

                dataArray.put(ELEMENT_ID, currentId);
                dataArray.put(TEXT, ((Element) nodeList.item(i)).getAttribute(DISPLAY_NAME));
                dataArray.put(CHILDREN, nodeList.item(i).hasChildNodes());

                if (nodeCount == 1) {
                    dataArray.put(PREVIOUS_ELEMENT_PATH, "");
                    dataArray.put(CURRENT_ELEMENT_PATH, rootId + "/" + currentId);
                    dataArray.put(NEXT_ELEMENT_PATH, "");
                } else if (i == 0) {
                    dataArray.put(PREVIOUS_ELEMENT_PATH, "");
                    dataArray.put(CURRENT_ELEMENT_PATH, rootId + "/" + currentId);
                    dataArray.put(NEXT_ELEMENT_PATH, rootId + "/" + ((Element) nodeList.item(i + 1)).getAttribute(ID));
                } else if (i == nodeCount - 1) {
                    dataArray.put(PREVIOUS_ELEMENT_PATH,
                            rootId + "/" + ((Element) nodeList.item(i - 1)).getAttribute(ID));
                    dataArray.put(CURRENT_ELEMENT_PATH, rootId + "/" + currentId);
                    dataArray.put(NEXT_ELEMENT_PATH, "");
                } else {
                    dataArray.put(PREVIOUS_ELEMENT_PATH,
                            rootId + "/" + ((Element) nodeList.item(i - 1)).getAttribute(ID));
                    dataArray.put(CURRENT_ELEMENT_PATH, rootId + "/" + currentId);
                    dataArray.put(NEXT_ELEMENT_PATH, rootId + "/" + ((Element) nodeList.item(i + 1)).getAttribute(ID));
                }

                childrenArray.put(dataArray);
            }
        }

        return mainArray;
    }

    /**
     * This method will build OMElement
     *
     * @param payload String payload
     * @return OMElement
     * @throws RegistryException
     */
    public static OMElement buildOMElement(String payload) throws RegistryException {
        OMElement element;
        try {
            element = AXIOMUtil.stringToOM(payload);
            element.build();
        } catch (Exception e) {
            String message = "Unable to parse the XML configuration. Please validate the XML configuration";
            throw new RegistryException(message, e);
        }
        return element;
    }

    public static TaxonomyBean documentBeanBuilder(String payload)
            throws RegistryException, ParserConfigurationException, IOException, SAXException {

        OMElement element = buildOMElement(payload);
        String name = element.getAttributeValue(new QName("name"));
        String global = element.getAttributeValue(new QName("global"));
        InputStream inputStream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);

        TaxonomyBean documentBean = new TaxonomyBean();
        documentBean.setTaxonomyName(name);
        documentBean.setDocument(document);
        documentBean.setPath(getCompletePath(name));
        documentBean.setGlobal(Boolean.valueOf(global));
        documentBean.setPayload(payload);

        return documentBean;
    }

    /**
     * This will return complete path of taxonomy configuration
     *
     * @param name taxonomy name
     * @return complete path
     */
    private static String getCompletePath(String name) {
        String path = "/_system/governance/" + TAXONOMY_CONFIGURATION_PATH;
        if (!name.startsWith(path)) {
            name = path + name;
        }
        return name;
    }

}
