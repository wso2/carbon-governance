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

package org.wso2.carbon.governance.dashboardpopulator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;


public class GadgetPopulator {

    private static final Log log = LogFactory.getLog(GadgetPopulator.class);

    public static final String SYSTEM_GADGETS_PATH = "/_system/config/repository/dashboards/gadgets";

    public static void beginFileTansfer(File rootDirectory,Registry tenantRegistry ) throws RegistryException {

        // Storing the root path for future reference
        String rootPath = rootDirectory.getAbsolutePath();
        // Creating the default gadget collection resource
        Collection defaultGadgetCollection = tenantRegistry.newCollection();

        // Set permission for annonymous read
//            AuthorizationManager accessControlAdmin =
//                    tenantRegistry.getUserRealm().getAuthorizationManager();
//
//            if (!accessControlAdmin.isUserAuthorized(RegistryConstants.ANONYMOUS_USER,
//                                             SYSTEM_GADGETS_PATH, ActionConstants.GET))  {
//                accessControlAdmin.authorizeUser(RegistryConstants.ANONYMOUS_USER,
//                                             SYSTEM_GADGETS_PATH, ActionConstants.GET);
//            }
        try {
            tenantRegistry.beginTransaction();
            tenantRegistry.put(SYSTEM_GADGETS_PATH, defaultGadgetCollection);

            transferDirectoryContentToRegistry(rootDirectory, tenantRegistry, rootPath);
            tenantRegistry.commitTransaction();
        } catch (Exception e) {
            tenantRegistry.rollbackTransaction();
            log.error(e);
        }
    }

    private static void transferDirectoryContentToRegistry(File rootDirectory, Registry tenantRegistry,
                                                           String rootPath)
            throws FileNotFoundException {

        try {


            File[] filesAndDirs = rootDirectory.listFiles();
            List<File> filesDirs = Arrays.asList(filesAndDirs);

            for (File file : filesDirs) {

                if (!file.isFile()) {
                    // This is a Directory add a new collection
                    // This path is used to store the file resource under registry
                    String directoryRegistryPath =
                            SYSTEM_GADGETS_PATH + file.getAbsolutePath()
                                    .substring(rootPath.length()).replaceAll("[/\\\\]+", "/");
                    Collection newCollection = tenantRegistry.newCollection();
                    tenantRegistry.put(directoryRegistryPath, newCollection);

                    // recurse
                    transferDirectoryContentToRegistry(file, tenantRegistry, rootPath);
                } else {
                    // Add this to registry
                    addToRegistry(rootPath, file, tenantRegistry);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }

    }


    private static void addToRegistry(String rootPath, File file, Registry tenantRegistry) {
        try {

            // This path is used to store the file resource under registry
            String fileRegistryPath =
                    SYSTEM_GADGETS_PATH + file.getAbsolutePath().substring(rootPath.length())
                            .replaceAll("[/\\\\]+", "/");

            // Adding the file to the Registry             
            Resource fileResource = tenantRegistry.newResource();
            fileResource.setMediaType(new MimetypesFileTypeMap().getContentType(file));
            fileResource.setContentStream(new FileInputStream(file));
            tenantRegistry.put(fileRegistryPath, fileResource);

        } catch (RegistryException e) {
            log.error(e);
        } catch (FileNotFoundException e) {
            log.error(e);
        }
    }
}
