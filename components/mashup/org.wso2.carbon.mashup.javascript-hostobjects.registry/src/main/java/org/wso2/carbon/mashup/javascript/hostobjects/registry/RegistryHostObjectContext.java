/**
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.mashup.javascript.hostobjects.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.user.core.UserStoreException;


public class RegistryHostObjectContext {

    private static Log log = LogFactory.getLog(RegistryHostObjectContext.class);

    private static RegistryService registryService = null;

    public static void setRegistryService(RegistryService registryService) {
        RegistryHostObjectContext.registryService = registryService;
    }

    public static Registry getUserRegistry(String mashupAuthor, int tenantId) throws CarbonException {
        if (registryService == null) {
            throw new CarbonException("Registry is null");
        }
        try {
            if(registryService.getUserRealm(tenantId).getUserStoreManager().isExistingUser(mashupAuthor)) {
                return registryService.getGovernanceUserRegistry(mashupAuthor, tenantId);
            } else {
                throw new CarbonException("Unable to access Registry, mashup author is not an active user");
            }
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        }catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        }
    }
}
