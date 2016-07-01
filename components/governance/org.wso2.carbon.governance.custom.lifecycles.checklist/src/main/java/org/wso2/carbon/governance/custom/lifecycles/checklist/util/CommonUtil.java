/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.custom.lifecycles.checklist.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.Aspect;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryConfigurationProcessor;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class CommonUtil {

    private static final Log log = LogFactory.getLog(CommonUtil.class);

    private static RegistryService registryService;

    public static synchronized void setRegistryService(RegistryService service) {
        if (registryService == null) {
            registryService = service;
        }
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static String[] getAllDependencies(String path, Registry registry) throws Exception {
        Set<String> retList = getAllDependenciesRecursive(path, registry);
        String[] retString = new String[retList.size()];
        return retList.toArray(retString);
    }

    public static Set<String> getAllDependenciesRecursive(String path, Registry registry) throws Exception{
        Association[] dependencies = registry.getAssociations(path, CommonConstants.ASSOCIATION_TYPE01);

        Set<String> allAssociations = new HashSet<String>();
        for (Association dependency : dependencies) {
            if (dependency.getSourcePath().equals(path)
                    && !dependency.getSourcePath().equals(dependency.getDestinationPath())) {
                allAssociations.addAll(getAllDependenciesRecursive(dependency.getDestinationPath(), registry));
            }
        }
        allAssociations.add(RegistryUtils.getAbsolutePath(registry.getRegistryContext(),path));
        return  allAssociations;
    }
    public static boolean generateAspect(String resourceFullPath, Registry registry) throws RegistryException,
                                                                                            XMLStreamException {
        RegistryContext registryContext = registry.getRegistryContext();
        if (registryContext == null) {
            return false;
        }
        Resource resource = registry.get(resourceFullPath);
        if (resource != null) {
            String content = null;
            if (resource.getContent() != null) {
                content = RegistryUtils.decodeBytes((byte[])resource.getContent());
            }
            if (content != null) {
                OMElement aspect = AXIOMUtil.stringToOM(content);
                if (aspect != null) {
                    OMElement dummy = OMAbstractFactory.getOMFactory().createOMElement("dummy", null);
                    dummy.addChild(aspect);
                    Aspect aspectinstance = RegistryConfigurationProcessor.updateAspects(dummy);
                    Iterator aspectElement = dummy.getChildrenWithName(new QName("aspect"));
                    String name = "";
                    if (aspectElement != null) {
                        OMElement aspectelement = (OMElement) aspectElement.next();
                        name = aspectelement.getAttributeValue(new QName("name"));
                    }
                    registry.addAspect(name,aspectinstance);
                    return true;
                }
            }
        }
        return false;
    }

    /*public static UserRegistry getRegistry() throws RegistryException {

        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext == null) {
            String msg = "Could not get the user's Registry session. Message context not found.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        HttpServletRequest request =
                (HttpServletRequest) messageContext.getProperty("transport.http.servletRequest");

        return getRegistry(request);
    }

    public static UserRegistry getRegistry(HttpServletRequest request) throws RegistryException {

        UserRegistry registry =
                (UserRegistry) request.getSession().getAttribute(RegistryConstants.USER_REGISTRY);

        if (registry == null) {
            String msg = "User's Registry instance is not found. " +
                    "Users have to login to retrieve a registry instance. ";
            log.error(msg);
            throw new RegistryException(msg);
        }

        return registry;
    }*/
}
