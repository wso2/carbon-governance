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
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.registry.extensions.interfaces.CustomValidations;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.Map;

public class AttributeExistenceValidator implements CustomValidations {

    private static final Log log = LogFactory.getLog(AttributeExistenceValidator.class);
    private String[] attributes = new String[0];

    public void init(Map parameterMap) {
        if (parameterMap != null) {
            String temp = (String) parameterMap.get("attributes");
            if (temp != null) {
                attributes = temp.split(",");
            }
        }
    }

    public boolean validate(RequestContext context) {
        if (attributes.length == 0) {
            return true;
        }
        String resourcePath = context.getResourcePath().getPath();
        int index = resourcePath.indexOf(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
        if (index < 0) {
            log.warn("Unable to use Validator For Resource Path: " + resourcePath);
            return false;
        }
        index += RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length();
        if (resourcePath.length() <= index) {
            log.warn("Unable to use Validator For Resource Path: " + resourcePath);
            return false;
        }
        resourcePath = resourcePath.substring(index);
        try {
            UserRegistry registry = ((UserRegistry) context.getSystemRegistry());
            if (!registry.resourceExists(resourcePath)) {
                registry = ((UserRegistry) context.getSystemRegistry())
                        .getChrootedRegistry(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
            }
            GovernanceArtifact governanceArtifact =
                    GovernanceUtils.retrieveGovernanceArtifactByPath(registry, resourcePath);
            for (String attribute : attributes) {
                if (!validateAttribute(governanceArtifact, attribute)) {
                    return false;
                }
            }
        } catch (RegistryException e) {
            log.error("Unable to obtain registry instance", e);
        }
        return true;
    }

    protected boolean validateAttribute(GovernanceArtifact governanceArtifact, String attribute)
            throws GovernanceException {
        return (governanceArtifact.getAttribute(attribute) != null);
    }
}
