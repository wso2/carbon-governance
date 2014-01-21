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
package org.wso2.carbon.governance.wsdltool.ui.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.wsdltool.stub.beans.xsd.ServiceInfoBean;
import org.wso2.carbon.governance.wsdltool.ui.clients.WSDLToolServiceClient;
import org.wso2.carbon.registry.common.ui.UIException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AddWSDLToolUtil {

    private static final Log log = LogFactory.getLog(AddWSDLToolUtil.class);

    public static void addMEXBean(
            HttpServletRequest request, ServletConfig config, HttpSession session) throws UIException {

        try {
            String parentPath = request.getParameter("parentPath");          
            String wsdlURL = request.getParameter("wsdlURL");
            String ownerName = request.getParameter("ownerName");
            String ownerEmail = request.getParameter("ownerEmail");
            String ownerTelephone = request.getParameter("ownerTelephone");
            String ownerAddress = request.getParameter("ownerAddress");
            String description = request.getParameter("description");

            ServiceInfoBean serviceInfoBean = new ServiceInfoBean();
            serviceInfoBean.setWsdlURL(wsdlURL);
            serviceInfoBean.setOwnerName(ownerName);
            serviceInfoBean.setOwnerEmail(ownerEmail);
            serviceInfoBean.setOwnerAddress(ownerAddress);
            serviceInfoBean.setOwnerTelephone(ownerTelephone);
            serviceInfoBean.setDescription(description);

            WSDLToolServiceClient serviceClient = new WSDLToolServiceClient(config, session);
            serviceClient.addMEXService(parentPath + "/dummy.wsdl", serviceInfoBean);

        } catch (Exception e) {
            String msg = "Failed to get service details. " + e.getMessage();
            log.error(msg, e);
            throw new UIException(msg, e);
        }
    }
}
