package org.wso2.carbon.governance.generic.util;

/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.governance.list.util.CommonUtil;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

import javax.cache.Cache;
import javax.xml.namespace.QName;


public class GenericArtifactUtil {

    private static final Log log = LogFactory.getLog(GenericArtifactUtil.class);

    public final static String REL_RXT_BASE_PATH ;

    static {
       REL_RXT_BASE_PATH = GovernanceConstants.RXT_CONFIGS_PATH.
                          split(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)[1];
    }

    public static boolean addRXTResource(String path,String rxtConfig, Registry registry) throws RegistryException {

        String rxtName = null;
        Resource resource = null;
        String rxtStoragePath = null;

        try {
            if(rxtConfig == null || rxtConfig.equals("")){
                log.error("Failed to add RXT resource , because RXT content is null or empty");
                return false;
            }

        OMElement element = buildOMElement(rxtConfig);
        if (!CommonUtil.validateXMLConfigOnSchema(
                RegistryUtils.decodeBytes(rxtConfig.getBytes()), "rxt-ui-config")) {
            throw new RegistryException("Violation of RXT definition in" +
                    " configuration file, follow the schema correctly..!!");
        }
        if (element != null) {
            rxtName = element.getAttributeValue(new QName("shortName"));
        }
        if (rxtName == null || rxtName.equals("")) {
            return false; // invalid configuration
        }

            if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
                return false;
            }


                if(path == null) {
                    resource = registry.newResource();
                    rxtStoragePath = getCalculatedRXTPath(rxtName);
                } else if (registry.resourceExists(getGovernanceRelativePath(path))){
                  resource = registry.get(getGovernanceRelativePath(path));
                  rxtStoragePath = getGovernanceRelativePath(path);
                }
            Cache<String,Boolean> rxtConfigCache = GovernanceUtils.getRXTConfigCache(GovernanceConstants.RXT_CONFIG_CACHE_ID);

            resource.setContent(rxtConfig.getBytes());
            resource.setMediaType(CommonConstants.RXT_MEDIA_TYPE);
            registry.beginTransaction();
            registry.put(rxtStoragePath, resource);
            registry.commitTransaction();

            if(rxtConfigCache.containsKey(rxtStoragePath)){
                rxtConfigCache.put(rxtStoragePath,rxtConfigCache.get(rxtStoragePath)^true);
            }else{
                rxtConfigCache.put(rxtStoragePath,true);
            }

        } catch (RegistryException e) {
            registry.rollbackTransaction();
            log.error("Error occurred while installing the RXT configuration " + rxtName, e);
            throw new RegistryException("Unable to store the rxt resource ", e);
        } catch (Exception e) {
            registry.rollbackTransaction();
            log.error("Error occurred while installing the RXT configuration " + rxtName, e);
            throw new RegistryException("Unable to store the rxt resource ", e);
        }
        return true;
    }


    public static OMElement buildOMElement(String payload) throws RegistryException {
        OMElement element;
        try {
            element = AXIOMUtil.stringToOM(payload);
            element.build();
        } catch (Exception e) {
            String message = "Unable to parse the XML configuration. Please validate the XML configuration";
            log.error(message, e);
            throw new RegistryException(message, e);
        }
        return element;
    }

    public static String getRXTKeyFromContent(String payload) throws RegistryException {
        OMElement element = buildOMElement(payload);
        return element.getAttributeValue(new QName("shortName"));

    }

    public static String getArtifactUIContentFromConfig(String payload) throws RegistryException {
        OMElement element = buildOMElement(payload);
        OMElement content = element.getFirstChildWithName(new QName("content"));
        if(content != null) {
          return content.toString();
        } else {
          return null;
        }
    }

    private static String getRXTNameFromShortName(String shortName) {
        return shortName + ".rxt";
    }

    private static String getGovernanceRelativePath(String absPath){
      return absPath.split(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)[1];
    }

    private static String getCalculatedRXTPath(String rxtName) {
        getRXTNameFromShortName(rxtName);
        StringBuilder storage = new StringBuilder();
        storage.append(REL_RXT_BASE_PATH).append("/").append(rxtName).append(".rxt");
       return storage.toString();
    }

    public static String getArtifactViewRequestParams(String payload) throws Exception {
        String shortName = "";
        String singuLarLabel = "";
        String pluralLabel = "";
        OMElement element = buildOMElement(payload);
        if (element != null) {
            shortName = element.getAttributeValue(new QName("shortName"));
            singuLarLabel = element.getAttributeValue(new QName("singularLabel"));
            pluralLabel = element.getAttributeValue(new QName("pluralLabel"));
        }

        String item = "governance_" + shortName + "_config_menu";
        String addEditItem = "governance_add_" + shortName + "_menu";

        return new StringBuilder("region=region1").append("&").append("item=").append(item).append("&").
                append("key=").append(shortName).append("&").append("breadcrumb=").append(pluralLabel.
                replaceAll(" ", "%20")).append("&").append("add_edit_region=region3").append("&").
                append("add_edit_item=").append(addEditItem).append("&").append("lifecycleAttribute=null").
                append("&").append("add_edit_breadcrumb=").append(singuLarLabel.replaceAll(" ", "%20")).
                append("&").append("singularLabel=").append(singuLarLabel.replaceAll(" ", "%20")).append("&").
                append("pluralLabel=").append(pluralLabel.replaceAll(" ", "%20")).toString();
    }

}

