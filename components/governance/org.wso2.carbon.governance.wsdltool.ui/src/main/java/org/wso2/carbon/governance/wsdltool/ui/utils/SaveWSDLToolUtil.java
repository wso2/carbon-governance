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
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.wso2.carbon.registry.common.ui.UIException;
import org.wso2.carbon.registry.resource.ui.clients.CustomUIServiceClient;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

public class SaveWSDLToolUtil {

    private static final Log log = LogFactory.getLog(SaveWSDLToolUtil.class);

    public static void saveEndpointBean(
            String path, HttpServletRequest request, ServletConfig config, HttpSession session) throws UIException {

        try {
            CustomUIServiceClient customUIServiceClient =
                    new CustomUIServiceClient(config, session);

            String name = request.getParameter("name");
            String uri = request.getParameter("uri");
            String format = request.getParameter("format");
            String optimize = request.getParameter("optimize");
            String sd = request.getParameter("sd");

            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMElement endpointElement = fac.createOMElement("endpoint", null);
            endpointElement.addAttribute("name", name, null);

            OMElement addressElement = fac.createOMElement("address", null);
            addressElement.addAttribute("uri", uri, null);
            addressElement.addAttribute("format", format, null);
            addressElement.addAttribute("optimize", optimize, null);
            endpointElement.addChild(addressElement);

            OMElement sdElement = fac.createOMElement("suspendDurationOnFailure", null);
            sdElement.setText(sd);
            addressElement.addChild(sdElement);

            String content = endpointElement.toString();

            customUIServiceClient.updateTextContent(path, content);

        } catch (Exception e) {
            String msg = "Failed to get end point details. " + e.getMessage();
            log.error(msg, e);
            throw new UIException(msg, e);
        }
    }
}
