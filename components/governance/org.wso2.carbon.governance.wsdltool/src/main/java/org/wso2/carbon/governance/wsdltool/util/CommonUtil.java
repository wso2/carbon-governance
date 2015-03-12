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
package org.wso2.carbon.governance.wsdltool.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.service.RegistryService;

import javax.servlet.http.HttpServletRequest;

import com.predic8.soamodel.Difference;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wsdl.diff.WsdlDiffGenerator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Class for utils methods
 */
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

    public static UserRegistry getRootSystemRegistry() throws RegistryException {
        if (registryService == null) {
            return null;
        } else {
            return registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        }
    }

    public static UserRegistry getRootSystemRegistry(int tenantId) throws RegistryException {
        if (registryService == null) {
            return null;
        } else {
            return registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, tenantId);
        }
    }

    /**
     * method for get InputStream of a registry resource
     *
     * @param registry Registry
     * @param resourcePath registry resource path
     * @return InputStream of the resource
     * @throws RegistryException
     */
    private static InputStream getResourceInputStream(Registry registry, String resourcePath) throws RegistryException {

        try {
            InputStream resourceInputStream = null;
            // Derive registry resource
            Resource resource = registry.get(resourcePath);
            // Convert registry resource content into byte array
            byte[] content = (byte[]) resource.getContent();
            if (content != null) {
                // Get registry resource InputStream
                resourceInputStream = new ByteArrayInputStream(content);
            }
            resource.discard();
            return resourceInputStream;
        } catch (RegistryException e) {
            String msg = "Could not get the InputStream of the resource at " +
                    resourcePath + ".";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

    }

    /**
     * method for get descriptive wsdl diff using membrane soa-model libs
     *
     * @param registry Registry
     * @param resource1Path path of resource 1
     * @param resource2Path path of resource 2
     * @param type result type
     * @return String[] of membrane diff results
     * @throws RegistryException
     */
    public static String[] getMembraneDiffArrayResult(Registry registry, String resource1Path,
                                                      String resource2Path, String type) throws RegistryException {
        try {
            // Final result diffArray
            String[] diffArray = new String[0];
            WSDLParser parser = new WSDLParser();
            // Get InputStream of the registry resource in resource1Path
            InputStream resource1InputStream = getResourceInputStream(registry, resource1Path);
            // Get InputStream of the registry resource in resource2Path
            InputStream resource2InputStream = getResourceInputStream(registry, resource2Path);
            // Generate diff
            Definitions resource1 = parser.parse(resource1InputStream);
            Definitions resource2 = parser.parse(resource2InputStream);
            WsdlDiffGenerator diffGen = new WsdlDiffGenerator(resource1, resource2);
            // Get Difference list
            List<Difference> lst = diffGen.compare();
            for (Difference diff : lst) {
                dumpDiff(diff, "");
                // Get the diff size
                int diffSize = diff.getDiffs().size();
                if (diffSize > 0) {
                    diffArray = new String[diffSize];
                    for (int i = 0; i < diffSize; i++) {
                        if (type.equals("key")) {
                            // Get the diff value
                            diffArray[i] = String.valueOf(diff.getDiffs().get(i));
                        }
                        if (type.equals("value")) {
                            // Get the diff dump value
                            diffArray[i] = String.valueOf(diff.getDiffs().get(i).dump());
                        }
                    }
                }
            }
            return diffArray;
        } catch (IncompatibleClassChangeError e) {
            String msg = "ASM library conflict Error : ";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } catch (Exception e) {
            String msg = "Error in retrieving membrane descriptive Diff : ";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

    }

    private static void dumpDiff(Difference diff, String level) {
        System.out.println(level + diff.getDescription());
        for (Difference localDiff : diff.getDiffs()){
            dumpDiff(localDiff, level + "  ");
        }
    }

    /**
     * method for validating resources to compare
     *
     * @param registry Registry
     * @param resourcePath1 path of resource 1
     * @param resourcePath2 path of resource 2
     * @return String of diff view type is membrane or codemirror
     * @throws RegistryException
     */
    public static String getDiffViewType(Registry registry, String resourcePath1, String resourcePath2)  throws RegistryException {
        //TODO change the below appending
        resourcePath1 = "/_system/governance/trunk" + resourcePath1;
        resourcePath2 = "/_system/governance/trunk" + resourcePath2;

        Resource resource1 = registry.get(resourcePath1);
        Resource resource2 = registry.get(resourcePath2);
        if (resource1 != null && resource1.getMediaType() != null && resource2 != null && resource2.getMediaType() != null) {
            if (resource1.getMediaType().equals(resource2.getMediaType())) {
                if (resource1.getMediaType().equals(RegistryConstants.WSDL_MEDIA_TYPE) || resource1.getMediaType().equals(RegistryConstants.XSD_MEDIA_TYPE)) {
                    return "membrane.diff";
                } else { //TODO check for invalid media types
                    return "codemirror.diff";
                }
            } else {
                //TODO return i18n resource keys
                return "Incompatible resource types";
            }
        } else {
            return "No resource information available";
        }
    }

}
