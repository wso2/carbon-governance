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

import org.w3c.dom.NodeList;
import org.wso2.carbon.governance.taxonomy.beans.QueryBean;
import org.wso2.carbon.governance.taxonomy.beans.RXTBean;
import org.wso2.carbon.governance.taxonomy.beans.TaxonomyBean;
import org.wso2.carbon.governance.taxonomy.util.CommonUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.*;

/**
 * This class will implements the methods for query operations
 */
class QueryProviderImpl implements IQueryProvider {
    /**
     * This method will return list of nodes for a given query
     *
     * @param taxonomyQueryBean taxonomy query meta data
     * @return NodeList (XML) for specific taxonomy file
     * @throws XPathExpressionException
     */
    @Override
    public NodeList query(QueryBean taxonomyQueryBean) throws XPathExpressionException {
        TaxonomyManager taxonomyManager = new TaxonomyManager();
        XPath xPathInstance = XPathFactory.newInstance().newXPath();
        XPathExpression exp = xPathInstance.compile(getUpdatedQuery(taxonomyQueryBean));
        return (NodeList) exp
                .evaluate(taxonomyManager.getTaxonomy(taxonomyQueryBean).getDocument(), XPathConstants.NODESET);
    }

    /**
     * This method will update the given query to xpath compilable query
     *
     * @param taxonomyQueryBean String front end rest api provided query
     * @return String update query
     */
    @Override
    public String getUpdatedQuery(QueryBean taxonomyQueryBean) {
        StringBuilder updatedStringBuilder = new StringBuilder();
        String query = taxonomyQueryBean.getQuery();
        // this will execute long queries with /children
        if (query.contains("/") && !query.contains("/*")) {
            String[] listIds = query.split("/");
            updatedStringBuilder.append("/taxonomy/root[@id='").append(listIds[0]).append("']");
            for (int ids = 1; ids < listIds.length; ids++) {
                if (!(ids == listIds.length - 1 && listIds[ids].equals("children"))) {
                    updatedStringBuilder.append("/node[@id='").append(listIds[ids]).append("']");
                }
            }
            if (query.contains("children")) {
                updatedStringBuilder.append("/*");
            }

        } else {
            updatedStringBuilder.append("/taxonomy/root/*");
        }
        return updatedStringBuilder.toString();
    }

    @Override
    public List<String> getTaxonomiesByRXT(String artifactType) {
        return null;
    }

    /**
     * This method will return taxonomy name related to given taxonomy id
     * (this method will execute when only store page refresh and publisher edit view)
     * @param taxonomyQueryBean Query bean object
     * @return taxonomy name
     * @throws UserStoreException
     * @throws RegistryException
     * @throws XPathExpressionException
     */
    @Override
    public String getTaxonomyNameById(QueryBean taxonomyQueryBean)
            throws UserStoreException, RegistryException, XPathExpressionException {

        List<RXTBean> rxtBeanList = CommonUtils.getRxtTaxonomies();

        for (RXTBean rxtBean : rxtBeanList) {
            if (rxtBean.getRxtName().equals(taxonomyQueryBean.getAssetType())) {
                Map<String, Map<String, Boolean>> objTaxonomies = rxtBean.getTaxonomy();
                if (objTaxonomies != null) {
                    if (objTaxonomies.size() > 0) {
                        for (Map.Entry<String, Map<String, Boolean>> entry : objTaxonomies.entrySet()) {
                            taxonomyQueryBean.setTaxonomyName(entry.getKey());
                            TaxonomyManager taxonomyManager = new TaxonomyManager();
                            XPath xPathInstance = XPathFactory.newInstance().newXPath();
                            XPathExpression exp = xPathInstance
                                    .compile("/taxonomy/root[@id='" + taxonomyQueryBean.getQuery() + "']");
                            NodeList nodeList = (NodeList) exp
                                    .evaluate(taxonomyManager.getTaxonomy(taxonomyQueryBean).getDocument(),
                                            XPathConstants.NODESET);
                            if (nodeList.getLength() > 0) {
                                return entry.getKey();
                            }

                        }
                    } else {
                        String entry = getGlobalTaxonomyName(taxonomyQueryBean);
                        if (entry != null) {
                            return entry;
                        }
                    }
                }
            } else {
                String entry = getGlobalTaxonomyName(taxonomyQueryBean);
                if (entry != null) {
                    return entry;
                }
            }

        }

        return null;
    }

    /**
     * This method will return global taxonomy name
     * @param taxonomyQueryBean Query bean
     * @return taxonomy name
     * @throws XPathExpressionException
     */
    private String getGlobalTaxonomyName(QueryBean taxonomyQueryBean) throws XPathExpressionException {
        TaxonomyManager taxonomyManager = new TaxonomyManager();
        Map<String, TaxonomyBean> beanMap = taxonomyManager.getTaxonomyBeanMap();
        for (Map.Entry<String, TaxonomyBean> entry : beanMap.entrySet()) {
            if (entry.getValue().isTaxonomyGlobal()) {
                taxonomyQueryBean.setTaxonomyName(entry.getKey());
                XPath xPathInstance = XPathFactory.newInstance().newXPath();
                XPathExpression exp = xPathInstance
                        .compile("/taxonomy/root[@id='" + taxonomyQueryBean.getQuery() + "']");
                NodeList nodeList = (NodeList) exp.evaluate(taxonomyManager.getTaxonomy(taxonomyQueryBean).getDocument(),
                        XPathConstants.NODESET);
                if (nodeList.getLength() > 0) {
                    return entry.getKey();
                }
            }

        }
        return null;
    }
}
