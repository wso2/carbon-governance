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
package org.wso2.carbon.governance.api.test.utils;

import junit.framework.TestCase;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.cache.ArtifactCache;
import org.wso2.carbon.governance.api.cache.ArtifactCacheFactory;
import org.wso2.carbon.governance.api.cache.ArtifactCacheManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.InMemoryEmbeddedRegistryService;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


public class BaseTestCase extends TestCase {

    protected static RegistryContext ctx;
    protected InMemoryEmbeddedRegistryService registryService;
 /* protected RemoteRegistryService registryService;*/
    protected Registry registry; // an admin registry


    public void setUp() throws Exception {
        if (System.getProperty("carbon.home") == null) {
            File file = new File("src/test/resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }

        // The line below is responsible for initializing the cache.
        CarbonContext.getThreadLocalCarbonContext();

        InputStream is;
        try {
            is = new FileInputStream("src/test/resources/registry.xml");
            /*is = new FileInputStream("/home/lahiru/trunk/carbon-platform/products/greg/modules/distribution/target/wso2greg-3.5.0-SNAPSHOT/repository/conf/registry.xml");*/
        } catch (Exception e) {
            is = null;
        }
        /*
        RealmService realmService = new InMemoryRealmService();
        RegistryContext registryContext  = RegistryContext.getBaseInstance(is, realmService);
        registryContext.setSetup(true);
        registryContext.selectDBConfig("h2-db");

        Code to use to test Remote Registry
        System.setProperty("javax.net.ssl.trustStore", "/home/lahiru/trunk/carbon-platform/products/greg/modules/distribution/target/wso2greg-3.5.0-SNAPSHOT/resources/security/client-truststore.jks");
         System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
         System.setProperty("javax.net.ssl.trustStoreType","JKS");
         registryService = new RemoteRegistryService("http://localhost:9763/registry", "admin", "admin");
         registry = registryService.getGovernanceUserRegistry("admin","admin");*/
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        registryService = new InMemoryEmbeddedRegistryService(is);
        registry = registryService.getGovernanceUserRegistry("admin", MultitenantConstants.SUPER_TENANT_ID);
        RegistryCoreServiceComponent component = new RegistryCoreServiceComponent() {
            {
                setRealmService(registryService.getRealmService());
            }
        };
        component.registerBuiltInHandlers(registryService);
        ArtifactCache cache = ArtifactCacheFactory.createArtifactCache();
        ArtifactCacheManager.getCacheManager().addTenantArtifactCache(cache, MultitenantConstants.SUPER_TENANT_ID);
    }
}
