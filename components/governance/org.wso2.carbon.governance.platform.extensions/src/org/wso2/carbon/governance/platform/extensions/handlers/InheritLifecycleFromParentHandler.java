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
package org.wso2.carbon.governance.platform.extensions.handlers;

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Handler that makes it possible for resources to inherit lifecycle definitions of their parent
 * collections.
 */
@SuppressWarnings("unused")
public class InheritLifecycleFromParentHandler extends Handler {

    private static final Pattern REGISTRY_LC_PROPERTY_REG_EX =
            Pattern.compile("^registry[.]([d]?lc[m]?|.*lifecycle|aspects).*$");
    private String basePath = RegistryConstants.ROOT_PATH;

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void put(RequestContext requestContext) throws RegistryException {
        String parentPath = requestContext.getResourcePath().getPath();
        Registry registry = requestContext.getRegistry();
        Resource parent = null;

        while (!parentPath.equals(basePath)) {
            parentPath = RegistryUtils.getParentPath(parentPath);
            if (registry.resourceExists(parentPath)) {
                parent = registry.get(parentPath);
                break;
            } else {
                registry.put(parentPath, registry.newCollection());
            }
        }
        if (parent != null) {
            Resource resource = requestContext.getResource();
            Properties properties = parent.getProperties();
            for (Object key : properties.keySet()) {
                String keyStr = (String) key;
                if (REGISTRY_LC_PROPERTY_REG_EX.matcher(keyStr.toLowerCase()).matches()) {
                    resource.setProperty(keyStr, parent.getPropertyValues(keyStr));
                }
            }
        }
    }

    public String copy(RequestContext requestContext) throws RegistryException {
        String output = null;
        if (!CommonUtil.isUpdateLockAvailable()) {
            return output;
        }
        CommonUtil.acquireUpdateLock();
        try {
            String sourcePath = requestContext.getSourcePath();
            String targetPath = requestContext.getTargetPath();
            Registry registry = requestContext.getRegistry();
            output = registry.copy(sourcePath, targetPath);
            recursivePut(registry, targetPath);
            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
        return output;
    }

    public String move(RequestContext requestContext) throws RegistryException {
        String output = null;
        if (!CommonUtil.isUpdateLockAvailable()) {
            return output;
        }
        CommonUtil.acquireUpdateLock();
        try {
            String sourcePath = requestContext.getSourcePath();
            String targetPath = requestContext.getTargetPath();
            Registry registry = requestContext.getRegistry();
            output = registry.move(sourcePath, targetPath);
            recursivePut(registry, targetPath);
            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
        return output;
    }

    public void recursivePut(Registry registry, String path) throws RegistryException {
        if (!registry.resourceExists(path)) {
            return;
        }
        Resource resource = registry.get(path);
        registry.put(path, resource);
        if (resource instanceof Collection) {
            Collection collection = (Collection) resource;
            String[] children = collection.getChildren();
            if (children != null) {
                for (String child : children) {
                    recursivePut(registry, child);
                }
            }
        }
    }
}
