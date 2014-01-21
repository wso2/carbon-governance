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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.HashMap;
import java.util.Map;

public class InvokeAspectUtil {

    private static final Log log = LogFactory.getLog(InvokeAspectUtil.class);

    public static void invokeAspect(String path, String aspect, String action, String[] items,
                                    Registry registry, Map<String, String> parameters)
            throws Exception {

        try {
            if(parameters.size() == 0){
                parameters = new HashMap<String, String>();
            }
            
            String itemType;
            if ("voteClick".equals(action)) {
            	itemType = ".vote";
            }else {
            	itemType = ".item";
            }
            
            for (int i = 0; i < items.length; i++) {
                parameters.put(i + itemType, items[i]);
            }

            if (parameters.size() > 0) {
                registry.invokeAspect(path, aspect, action, parameters);
            } else {
                registry.invokeAspect(path, aspect, action);
            }

        } catch (RegistryException e) {
            String msg = "Failed to invoke action " + action + " of aspect " + aspect +
                    " on resource " + path + ". " + e.getMessage();
            log.error(msg, e);
            // We need to preserve the incoming message from the lower layer, so that custom
            // exceptions are properly related at the UI-level.
            throw e;
        }
    }
}
