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
import org.wso2.carbon.registry.core.config.RegistryConfiguration;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.InMemoryEmbeddedRegistryService;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.extensions.aspects.DefaultLifecycle;
import org.wso2.carbon.user.core.service.RealmService;

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
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        // The line below is responsible for initializing the cache.
        CarbonContext.getThreadLocalCarbonContext();

        String carbonHome = System.getProperty("carbon.home");
        System.out.println("carbon home " + carbonHome);
        String carbonXMLPath = carbonHome + File.separator + "repository"
                + File.separator + "conf" + File.separator + "carbon.xml";
        RegistryConfiguration regConfig = new RegistryConfiguration(carbonXMLPath);
        RegistryCoreServiceComponent.setRegistryConfig(regConfig);

        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        InputStream is;

        try {
            is = this.getClass().getClassLoader().getResourceAsStream(
                    "registry.xml");
        } catch (Exception e) {
            is = null;
        }
        ctx = RegistryContext.getBaseInstance(is, realmService);
        //RegistryConfigurationProcessor.populateRegistryConfig(is, ctx);
        ctx.setSetup(true);
        ctx.selectDBConfig("h2-db");
        ctx.addAspect("DefaultLifecycle", new DefaultLifecycle(), MultitenantConstants.SUPER_TENANT_ID);
        EmbeddedRegistryService embeddedRegistry = ctx.getEmbeddedRegistryService();
        new RegistryCoreServiceComponent().registerBuiltInHandlers(embeddedRegistry);
        registry = embeddedRegistry.getGovernanceUserRegistry("admin", "admin");

        ArtifactCache cache = ArtifactCacheFactory.createArtifactCache();
        ArtifactCacheManager.getCacheManager().addTenantArtifactCache(cache, MultitenantConstants.SUPER_TENANT_ID);
    }
}
