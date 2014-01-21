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

package org.wso2.carbon.governance.custom.lifecycles.checklist.ui.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.custom.lifecycles.checklist.ui.clients.LifecycleServiceClient;
import org.wso2.carbon.registry.core.RegistryConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class InvokeAspectProcessor {

    private static final Log log = LogFactory.getLog(InvokeAspectProcessor.class);

     public static void invokeAspect(HttpServletRequest request, ServletConfig config) throws Exception {
         LifecycleServiceClient lifecycleServiceClient = new LifecycleServiceClient(config, request.getSession());

         String path = request.getParameter("path");
         String aspect = request.getParameter("aspect");
         String action = request.getParameter("action");
         String[] items = request.getParameterValues("items");   /* "true, false, true, false"*/
         String versionString = request.getParameter("parameterString");

         if (!versionString.trim().equals("")) {
             String[] keySetWithValues = versionString.split("\\^\\|\\^");
             String[][] resourceVersionArray = new String[keySetWithValues.length][2];

             for (int i = 0; i < keySetWithValues.length; i++) {
                 String keySetWithValue = keySetWithValues[i];
                 String[] keyAndValue = keySetWithValue.split("\\^\\^");
                 resourceVersionArray[i][0] = keyAndValue[0];
                 resourceVersionArray[i][1] = keyAndValue[1];
             }

             lifecycleServiceClient.invokeAspectWithParams(path, aspect, action, items,resourceVersionArray);
         }
         else{
             lifecycleServiceClient.invokeAspect(path, aspect, action, items);
         }
     }

    public static String[] getAllDependencies(HttpServletRequest request, ServletConfig config) throws Exception {
        String path = request.getParameter("path");

        LifecycleServiceClient lifecycleServiceClient = new LifecycleServiceClient(config,request.getSession());

        String[] dependencies = lifecycleServiceClient.getAllDependencies(path);
        List<String> filteredDependencies = new ArrayList<String>();
        for (String dependency : dependencies) {
            if (dependency.startsWith("/")) {
                filteredDependencies.add(dependency);
            } else {
                log.warn("Dependency " + dependency + " of " + path.substring(
                        path.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1) + " ignored when promoting");
            }
        }
        return filteredDependencies.toArray(new String[filteredDependencies.size()]);
    }
}
