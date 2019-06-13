/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.taxonomy.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.governance.taxonomy.services.ITaxonomyServices;
import org.wso2.carbon.governance.taxonomy.services.TaxonomyManager;
import org.wso2.carbon.governance.taxonomy.services.TaxonomyServicesImpl;
import org.wso2.carbon.governance.taxonomy.services.TenantLoginStorageService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.xml.sax.SAXException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.governance.taxonomy", 
         immediate = true)
public class TaxonomyServiceComponent {

    private static final Log log = LogFactory.getLog(TaxonomyServiceComponent.class);

    /**
     * Method to activate bundle.
     *
     * @param context OSGi component context.
     */
    @Activate
    protected void activate(ComponentContext context) throws UserStoreException, RegistryException, ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        BundleContext bundleContext = context.getBundleContext();
        TenantLoginStorageService tenantLoginStorageService = new TenantLoginStorageService();
        ServiceRegistration tenantMgtListenerSR = bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), tenantLoginStorageService, null);
        if (tenantMgtListenerSR != null) {
            if (log.isDebugEnabled()) {
                log.debug("Governance Taxonomy Management - TenantLoginStorageService registered");
            }
        } else {
            log.error("Governance Taxonomy Management - TenantLoginStorageService could not be registered");
        }
        BundleContext taxonomyBundleContext = context.getBundleContext();
        ServiceRegistration taxonomyManagementService = taxonomyBundleContext.registerService(ITaxonomyServices.class.getName(), new TaxonomyServicesImpl(), null);
        if (taxonomyManagementService != null) {
            if (log.isDebugEnabled()) {
                log.debug("Governance Taxonomy Management - Manager services registered");
            }
        } else {
            log.error("Governance Taxonomy Management - Manager services could not be registered");
        }
        if (log.isDebugEnabled()) {
            log.debug("Governance Taxonomy Management Service bundle is activated");
        }
        try {
            TaxonomyManager taxonomyManager = new TaxonomyManager();
            taxonomyManager.initTaxonomyStorage();
            if (log.isDebugEnabled()) {
                log.debug("Governance Taxonomy Management map initialization is success");
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while getting RealmConfigurations when activating osgi service", e);
        } catch (RegistryException e) {
            log.error("Error occurred while getting taxonomy files from registry when activating osgi service", e);
        } catch (IOException e) {
            log.error("Error occurred while parsing taxonomy xml when activating osgi service", e);
        } catch (ParserConfigurationException e) {
            log.error("Error occurred while building new document for taxonomy when activating osgi service", e);
        } catch (SAXException e) {
            log.error("Error occurred parsing taxonomy content stream when activating osgi service", e);
        }
    }

    /**
     * Method to deactivate bundle.
     *
     * @param context OSGi component context.
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.info("taxonomy bundle is deactivated");
        }
    }

    /**
     * Method to set registry service.
     *
     * @param registryService service to get tenant data.
     */
    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting RegistryService for taxonomy");
        }
        ServiceHolder.setRegistryService(registryService);
    }

    /**
     * Method to unset registry service.
     *
     * @param registryService service to get registry data.
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Unset Registry service");
        }
        ServiceHolder.setRegistryService(null);
    }

    /**
     * Method to set realm service.
     *
     * @param realmService service to get tenant data.
     */
    @Reference(
             name = "realm.service", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        log.debug("Setting RealmService for WSO2 Governance Registry migration");
        ServiceHolder.setRealmService(realmService);
    }

    /**
     * Method to unset realm service.
     *
     * @param realmService service to get tenant data.
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unset Realm service");
        }
        ServiceHolder.setRealmService(null);
    }

    /**
     * Method to set tenant registry loader
     *
     * @param tenantRegLoader tenant registry loader
     */
    @Reference(
             name = "tenant.registryloader", 
             service = org.wso2.carbon.registry.core.service.TenantRegistryLoader.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetTenantRegistryLoader")
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        log.debug("Setting TenantRegistryLoader for WSO2 Governance Registry migration");
        ServiceHolder.setTenantRegLoader(tenantRegLoader);
    }

    /**
     * Method to unset tenant registry loader
     *
     * @param tenantRegLoader tenant registry loader
     */
    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        log.debug("Unset Tenant Registry Loader");
        ServiceHolder.setTenantRegLoader(null);
    }
}

