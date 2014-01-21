/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.governance.registry.extensions.executors;

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.HashMap;
import java.util.Map;

public class TransformAndCopyExecutor extends CopyExecutor {

    private static final String MAPPINGS_RESOURCE = "mappingsResource";
    private static final String STARTING_INDEX = "startingIndex";

    protected void doCopy(RequestContext requestContext, String resourcePath, String newPath)
            throws RegistryException {
        if (requestContext.getResource() instanceof Collection) {
            Registry registry = requestContext.getRegistry();
            Object content = registry.get(
                    (String) parameterMap.get(MAPPINGS_RESOURCE)).getContent();
            String contentString = getContentString(content);
            int startingIndex = Integer.parseInt((String) parameterMap.get(STARTING_INDEX));
            Map<String, String> conversions = new HashMap<String, String>();
            for (String mapping : contentString.split("\n")) {
                String[] mappings = mapping.split("\r")[0].split(",");
                conversions.put(mappings[startingIndex - 1].trim(), mappings[startingIndex].trim());
            }
            registry.put(newPath, registry.get(resourcePath));
            for (String path : ((Collection)requestContext.getResource()).getChildren()) {
                String temp = RegistryConstants.PATH_SEPARATOR +
                        RegistryUtils.getResourceName(path);
                Resource resource = registry.get(resourcePath + temp);
                String string = getContentString(resource.getContent());
                for (Map.Entry<String, String> e : conversions.entrySet()) {
                    string = string.replace(e.getKey(), e.getValue());
                }
                resource.setContent(string);
                registry.put(newPath + temp, resource);
            }
        }
    }

    private String getContentString(Object content) throws RegistryException {
        String contentString;
        if (content instanceof String) {
            contentString = (String) content;
        } else {
            contentString = RegistryUtils.decodeBytes((byte[])content);
        }
        return contentString;
    }
}
