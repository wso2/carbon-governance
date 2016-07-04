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

package org.wso2.carbon.governance.taxonomy.ui.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.taxonomy.stub.TaxonomyServicesStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This class will handle all jsp method callings. And this will send all function calls to OSGI service through
 * taxonomy stub
 */
public class TaxonomyManagementClient {
    private TaxonomyServicesStub stub;

    public TaxonomyManagementClient(String cookie, ServletConfig config, HttpSession session)
            throws Exception {
        String endpointURL;
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        endpointURL = backendServerURL + "TaxonomyServices";

        try {
            stub = new TaxonomyServicesStub(configContext, endpointURL);
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate taxonomy service client. " + axisFault.getMessage();
            throw new Exception(msg, axisFault);
        }
    }

    /**
     * This method will call addTaxonomy method in taxonomy OSGI service
     *
     * @param request User request
     * @return boolean
     * @throws Exception
     */
    public boolean newTaxonomy(HttpServletRequest request)
            throws  Exception {
        return stub.addTaxonomy(request.getParameter("payload"));
    }

    /**
     * This method will return content of the taxonomy file
     *
     * @param request user request which contains taxonomy file name
     * @return String content
     * @throws Exception
     */
    public String getTaxonomy(HttpServletRequest request)
            throws  Exception {
        return stub.getTaxonomy(request.getParameter("taxonomyName"));
    }

    /**
     * This method will call updateTaxonomy method in taxonomy OSGI service
     *
     * @param request user request
     * @return boolean
     * @throws Exception
     */
    public boolean updateTaxonomy(HttpServletRequest request)
            throws  Exception {
        return stub.updateTaxonomy(request.getParameter("taxonomyName"), request.getParameter("payload"));
    }

    /**
     * This method will call deleteTaxonomy method in taxonomy OSGI service
     *
     * @param request user request
     * @return boolean
     * @throws Exception
     */
    public boolean deleteTaxonomy(HttpServletRequest request)
            throws  Exception {
        return stub.deleteTaxonomy(request.getParameter("taxonomyName"));
    }

    /**
     * This method will return array of taxonomies which in the registry for a specific tenant
     *
     * @param request user request
     * @return String array of taxonomy files
     * @throws Exception
     */

    public String[] getTaxonomyList(HttpServletRequest request)
            throws  Exception {
        return stub.getTaxonomyList();
    }

}
