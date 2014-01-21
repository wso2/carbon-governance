/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.dashboardpopulator.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.dashboardpopulator.DashboardPopulatorContext;
import org.wso2.carbon.governance.dashboardpopulator.GadgetPopulator;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * This observer used to to copy default gadget resources to the tenant registry space
 */
public class DashboardPopulatorAxis2ConfigurationContextObserver implements Axis2ConfigurationContextObserver {
    private static final Log log = LogFactory.getLog(DashboardPopulatorAxis2ConfigurationContextObserver.class);
    private static final String REGISTRY_SYSTEM_DASHBOARDS_ROOT = "/_system/config/repository/dashboards";


    String dashboardDiskRoot = System.getProperty(ServerConstants.CARBON_HOME) + File
            .separator + "repository" + File.separator + "resources" + File.separator + "dashboard";

    String dashboardConfigFile = dashboardDiskRoot + File.separator + "dashboard.xml";
    String gadgetsDiskLocation = dashboardDiskRoot + File.separator + "gadgets";

    @Override
    public void creatingConfigurationContext(int i) {

    }

    @Override
    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        try {
            log.debug("Dashboard Populator for Governance - bundle is activated ");
            // Check whether the system dasboard is already available if not populate
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            Registry tenantRegistry = DashboardPopulatorContext.getRegistryService().getRegistry(
                    CarbonConstants.REGISTRY_SYSTEM_USERNAME, tenantId);

            if (!tenantRegistry.resourceExists(REGISTRY_SYSTEM_DASHBOARDS_ROOT)) {
                copyDashboardXml(tenantRegistry);
            }
            // Check whether Gadgets are stored. If not store
            if (!tenantRegistry.resourceExists(GadgetPopulator.SYSTEM_GADGETS_PATH)) {
                copyGadgetResources(tenantRegistry);
            }
        } catch (Exception e) {
            log.debug("Failed to copy default gadgets for for tenant", e);
        }
    }

   /**
     *  Copy default gadget resources (css, js, swf files,...etc)
     * @param tenantRegistry  Registry
     */
    private void copyGadgetResources(Registry tenantRegistry) {

        File gadgetsDir = new File(gadgetsDiskLocation);
        if (gadgetsDir.exists()) {
            try {
                GadgetPopulator.beginFileTansfer(gadgetsDir, tenantRegistry);
            } catch (RegistryException e) {
                e.printStackTrace();
            }
            log.info("Successfully populated the default Gadgets.");
        } else {
            log.info("Couldn't find contents at '" + gadgetsDiskLocation +
                    "'. Giving up.");
        }
    }

    /**
     * Copy default dashboard.xml file
     * @param tenantRegistry  Registry
     */
    private void copyDashboardXml(Registry tenantRegistry) {
        // Creating an OMElement from file
        File dashboardConfigXml = new File(dashboardConfigFile);

        if (dashboardConfigXml.exists()) {
            FileReader dashboardConfigXmlReader;
            try {
                dashboardConfigXmlReader = new FileReader(dashboardConfigXml);
                // Restoring from file
                tenantRegistry.restore(REGISTRY_SYSTEM_DASHBOARDS_ROOT, dashboardConfigXmlReader);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (RegistryException e) {
                e.printStackTrace();
            }
            log.info("Successfully populated the default Dashboards.");

        } else {
            log.info("Couldn't find a Dashboard at '" + dashboardConfigFile + "'. Giving up.");
        }
    }

    @Override
    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {

    }

    @Override
    public void terminatedConfigurationContext(ConfigurationContext configurationContext) {

    }
}
