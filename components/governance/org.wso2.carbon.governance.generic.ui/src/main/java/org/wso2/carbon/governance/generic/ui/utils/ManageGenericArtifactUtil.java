/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.generic.ui.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient;
import org.wso2.carbon.registry.common.ui.UIException;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceStub;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageGenericArtifactUtil {
    private static final Log log = LogFactory.getLog(ManageGenericArtifactUtil.class);

    public static String addArtifactContent(
            OMElement info, HttpServletRequest request, ServletConfig config, HttpSession session,
            String dataName, String dataNamespace, String currentPath) throws UIException {

        try {
            ManageGenericArtifactServiceClient
                    serviceClient = new ManageGenericArtifactServiceClient(config, session);
            OMElement filledService = new GenericUIGenerator(
                    dataName, dataNamespace).getDataFromUI(info, request);
            String operation = request.getParameter("add_edit_operation");
            if (operation != null) {
                if (operation.equals("add")) {
                    return serviceClient.addArtifact(request.getParameter("key"),
                            filledService.toString(),
                            request.getParameter("lifecycleAttribute"));
                } else if (operation.equals("edit")) {
                    return serviceClient.editArtifact(currentPath, request.getParameter("key"),
                            filledService.toString(),
                            request.getParameter("lifecycleAttribute"));
                }
            }
            return null;
        } catch (Exception e) {
            String msg = "Failed to add/edit artifact details. " + e.getMessage();
            log.error(msg, e);
            throw new UIException(e);
        }
    }

  /**
     *  This method specifically creates a search service stub to do search by mediaType as the existing admin service uses a http
   *    request parameters to obtain the media type. But in this particular case the page gets directed from the component xml and
   *    hence cannot add pre defined request params there. And to keep the search admin service structure as it is, thus this bundle creates
   *    a separate client to access the search stub.
     */

    public static List<InstalledRxt> getInstalledRxts(String cookie, ServletConfig config, HttpSession session) throws Exception {
       String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
       ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
       String epr = backendServerURL + "SearchAdminService";
       SearchAdminServiceStub stub;
        try {
            stub = new SearchAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate search service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new Exception(msg, axisFault);
        }

        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        ArrayOfString arr = new ArrayOfString();
        arr.setArray(new String[]{"mediaType", GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE});
        ArrayOfString[] paramList = new ArrayOfString[]{arr};
        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = stub.getAdvancedSearchResults(searchQuery);
        ResourceData [] results = result.getResourceDataList();
        List<InstalledRxt> listInstalledRxts = new ArrayList<InstalledRxt>();

        if (results != null && results.length > 0) {
            for(ResourceData data:results) {
                String path = data.getResourcePath();
                if(path != null && path.contains("/")) {
                  String rxt =  path.substring(path.lastIndexOf("/") + 1).split("\\.")[0];
                  InstalledRxt rxtObj = new InstalledRxt();
                  rxtObj.setRxt(rxt);
                  if(data.getDeleteAllowed()) {
                      rxtObj.setDeleteAllowed();
                  }
                  listInstalledRxts.add(rxtObj);
                }
            }
        }

        return listInstalledRxts;
    }

}
