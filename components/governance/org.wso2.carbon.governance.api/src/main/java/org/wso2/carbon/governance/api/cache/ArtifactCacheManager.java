/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.api.cache;

import java.util.HashMap;
import java.util.Map;

public class ArtifactCacheManager {

    private static Map<Integer, ArtifactCache> cacheMap;
    private static ArtifactCacheManager thisInstance = new ArtifactCacheManager();
    private static final Object lock = new Object();
    private static ArtifactCache clientCache = null;

    public static void enableClientCache() {
        clientCache = new ArtifactCache();
    }

    public static ArtifactCacheManager getCacheManager() {
        return thisInstance;
    }

    private ArtifactCacheManager() {
        cacheMap = new HashMap<Integer, ArtifactCache>();
    }

    public void addTenantArtifactCache(ArtifactCache cache,int tenantId) {
        synchronized (lock) {
            cacheMap.put(tenantId, cache);
        }
    }

    public ArtifactCache getTenantArtifactCache(int tenantId) {
        if (clientCache != null) {
            return clientCache;
        }
        synchronized (lock) {
            return cacheMap.get(tenantId);
        }
    }

    public void removeTenantArtifactCache(int tenantId) {
        synchronized (lock) {
            cacheMap.get(tenantId).invalidateCache();
            cacheMap.remove(tenantId);
        }
    }

}
