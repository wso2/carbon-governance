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
package org.wso2.carbon.governance.registry.extensions.handlers;

import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

/**
 * Handler for validating the format of a version collection. This is useful to enforce that all
 * versions created on the testing branch should comply to the format 1.2.3-(ALPHA|BETA|RC) and that
 * all versions created on the production branch should comply to the format 1.2.3, as a best
 * practice.
 */
@SuppressWarnings("unused")
public class VersionValidationHandler extends Handler {

    private String expression;
    private String exampleFormat;

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setExampleFormat(String exampleFormat) {
        this.exampleFormat = exampleFormat;
    }

    public void put(RequestContext requestContext) throws RegistryException {
        String path = requestContext.getResourcePath().getPath();
        String[] pathFragments = path.split(RegistryConstants.PATH_SEPARATOR);
        for (String pathFragment : pathFragments) {
            if (pathFragment.matches(CommonConstants.SERVICE_VERSION_REGEX.replace("$",
                    "(-[a-zA-Z0-9]+)?$")) && !pathFragment.matches(expression)) {
                throw new RegistryException("The version " + pathFragment + " is not valid. " +
                        "Please specify a version in the format " + exampleFormat + ".");
            }
        }
    }
}
