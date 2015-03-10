/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.wsdltool.services;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.governance.wsdltool.beans.ServiceInfoBean;
import org.wso2.carbon.governance.wsdltool.util.CommonUtil;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

/**
 * This class act as the service class for wsdltool server component
 */
public class WSDLToolService extends RegistryAbstractAdmin {

    /**
     * Method for get membrane diff
     * @param resource1RegPath registry path of the resource 1
     * @param resource2RegPath registry path of the resource 2
     * @param type result type
     * @return resources diff array
     * @throws Exception
     */
    public String[] getMembraneDiffArrayResult(String resource1RegPath, String resource2RegPath, String type) throws Exception {
        // Get the root registry.
        Registry registry = (UserRegistry) getRootRegistry();
        return CommonUtil.getMembraneDiffArrayResult(registry, resource1RegPath, resource2RegPath, type);
    }

    /**
     * method for validating resources to compare
     *
     * @param resourcePath1 path of resource 1
     * @param resourcePath2 path of resource 2
     * @return String of diff view type is membrane or codemirror
     * @throws RegistryException
     */
    public String getDiffViewType(String resourcePath1, String resourcePath2)  throws RegistryException {
        // Get the root registry.
        Registry registry = (UserRegistry) getRootRegistry();
        return CommonUtil.getDiffViewType(registry, resourcePath1, resourcePath2);
    }

    public void addMEXService(String path, ServiceInfoBean serviceInfo) throws Exception {
 
        Registry registry = getGovernanceRegistry();
        Resource resource = registry.newResource();
        //String parentPath = RegistryUtils.getParentPath(path);
        //String wsdlURL = serviceInfo.getWsdlURL();

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement serviceInfoElement = fac.createOMElement("wsdltool", null);

        if (serviceInfo.getOwnerName() != null) {
            OMElement childElement = fac.createOMElement("wsdlurl", null);
            childElement.setText(serviceInfo.getWsdlURL());
            serviceInfoElement.addChild(childElement);
        }

        if (serviceInfo.getOwnerName() != null) {
            OMElement childElement = fac.createOMElement("ownername", null);
            childElement.setText(serviceInfo.getOwnerName());
            serviceInfoElement.addChild(childElement);
        }

        if (serviceInfo.getOwnerAddress() != null) {
            OMElement childElement = fac.createOMElement("owneraddress", null);
            childElement.setText(serviceInfo.getOwnerAddress());
            serviceInfoElement.addChild(childElement);
        }

        if (serviceInfo.getOwnerTelephone() != null) {
            OMElement childElement = fac.createOMElement("ownertelephone", null);
            childElement.setText(serviceInfo.getOwnerTelephone());
            serviceInfoElement.addChild(childElement);
        }

        if (serviceInfo.getOwnerEmail() != null) {
            OMElement childElement = fac.createOMElement("owneremail", null);
            childElement.setText(serviceInfo.getOwnerEmail());
            serviceInfoElement.addChild(childElement);
        }

        if (serviceInfo.getDescription() != null) {
            OMElement childElement = fac.createOMElement("description", null);
            childElement.setText(serviceInfo.getDescription());
            serviceInfoElement.addChild(childElement);
        }

        String content = serviceInfoElement.toString();
        resource.setContent(RegistryUtils.encodeString(content));
        resource.setMediaType(RegistryConstants.MEX_MEDIA_TYPE);
        registry.put(path, resource);
    }
}
