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
package org.wso2.carbon.governance.platform.extensions.handlers;

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A handler that can be used to filter out resources belonging to a particular lifecycle state from
 * a search result (ex:- Removing Abandoned services from the service list).
 *
 * <pre>
 *     &lt;handler class="org.wso2.carbon.governance.platform.extensions.handlers.ExcludeServicesByStateFromListHandler"
 *              methods="EXECUTE_QUERY"&gt;
 *          &lt;property name="lifecycleName"&gt;ServiceLifeCycle&lt;/property&gt;
 *          &lt;property name="stateName"&gt;Development&lt;/property&gt;
 *          &lt;filter class="org.wso2.carbon.governance.platform.extensions.filters.ExecuteQueryForAllPathsFilter"/&gt;
 *     &lt;/handler&gt;
 * </pre>
 */
public class ExcludeServicesByStateFromListHandler extends Handler {

    private String stateName;
    private String lifecycleName;

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public void setLifecycleName(String lifecycleName) {
        this.lifecycleName = lifecycleName;
    }

    public Collection executeQuery(RequestContext requestContext)
            throws RegistryException {
        if (CommonUtil.isUpdateLockAvailable()) {
            CommonUtil.acquireUpdateLock();
            try {
                Map queryParameters = requestContext.getQueryParameters();
                if (queryParameters != null) {
                    if (queryParameters.values().contains(RegistryConstants.SERVICE_MEDIA_TYPE)) {
                        // We now have a query which lists services.
                        String resourcePath = null;
                        if (requestContext.getResourcePath() != null) {
                            resourcePath = requestContext.getResourcePath().getPath();
                        }
                        Registry registry = requestContext.getRegistry();
                        Collection collection = registry.executeQuery(resourcePath, queryParameters);
                        List<String> fixedPaths = new LinkedList<String>();
                        for (String path : collection.getChildren()) {
                            try {
                                if (!stateName.equals(registry.get(path).getProperty(
                                        "registry.lifecycle." + lifecycleName + ".state"))) {
                                    fixedPaths.add(path);
                                }
                            } catch (RegistryException ignored) {
                            }
                        }
                        collection.setChildren(fixedPaths.toArray(new String[fixedPaths.size()]));
                        requestContext.setProcessingComplete(true);
                        return collection;
                    }
                }
            } finally {
                CommonUtil.releaseUpdateLock();
            }
        }
        return null;
    }
}
