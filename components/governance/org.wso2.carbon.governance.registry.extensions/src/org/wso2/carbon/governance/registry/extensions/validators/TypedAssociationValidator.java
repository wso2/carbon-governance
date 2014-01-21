/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.registry.extensions.validators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.registry.extensions.interfaces.CustomValidations;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class TypedAssociationValidator extends PropertyValueValidator implements CustomValidations {

    private static final Log log = LogFactory.getLog(TypedAssociationValidator.class);
    private String associationType = null;
    private String associationDirection = null;
    private String associatedMediaType = null;

    public void init(Map parameterMap) {
        if (parameterMap != null) {
            associationType = (String) parameterMap.get("associationType");
            associationDirection = (String) parameterMap.get("associationDirection");
            associatedMediaType = (String) parameterMap.get("associatedMediaType");
            super.init(parameterMap);
        }
    }

    public boolean validate(RequestContext context) {
        try {
            String path = context.getResourcePath().getPath();
            Registry systemRegistry = context.getSystemRegistry();
            Association[] associations = systemRegistry.getAssociations(
                    path, associationType);
            if (associations == null || associations.length == 0) {
                return false;
            }
            for (Association association : associations) {
                if ("url".equals(associatedMediaType)) {
                    try {
                        new URL(association.getDestinationPath());
                        // if there was a valid URL as the destination, we'll go for that.
                        return true;
                    } catch (MalformedURLException ignore) {
                    }
                } else if ("outward".equals(associationDirection) &&
                        association.getSourcePath().equals(path)) {
                    String destinationPath = association.getDestinationPath();
                    if (systemRegistry.resourceExists(destinationPath)) {
                        Resource resource = systemRegistry.get(destinationPath);
                        if (associatedMediaType == null ||
                                associatedMediaType.equals(resource.getMediaType())) {
                            if (validatePropertyOfResource(resource)) {
                                return true;
                            }
                        }
                    }
                } else if ("inward".equals(associationDirection) &&
                        association.getDestinationPath().equals(path)) {
                    Resource resource = systemRegistry.get(association.getSourcePath());
                    if (associatedMediaType == null ||
                            associatedMediaType.equals(resource.getMediaType())) {
                        if (validatePropertyOfResource(resource)) {
                            return true;
                        }
                    }
                } else if (association.getSourcePath().equals(path)) {
                    Resource resource = systemRegistry.get(association.getDestinationPath());
                    if (associatedMediaType == null ||
                            associatedMediaType.equals(resource.getMediaType())) {
                        if (validatePropertyOfResource(resource)) {
                            return true;
                        }
                    }
                } else {
                    Resource resource = systemRegistry.get(association.getSourcePath());
                    if (associatedMediaType == null ||
                            associatedMediaType.equals(resource.getMediaType())) {
                        if (validatePropertyOfResource(resource)) {
                            return true;
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Unable to obtain registry instance", e);
        }
        return false;
    }
}
