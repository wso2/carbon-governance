/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.taxonomy.services;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.xml.sax.SAXException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class will work as tenant login event detector .
 */
public class TenantLoginStorageService extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(TenantLoginStorageService.class);

    /**
     * when any tenant login into the publisher or store except supper tenant this method will invoke
     *
     * @param configContext configuration details
     */
    @Override
    public void createdConfigurationContext(ConfigurationContext configContext) {

        try {
            TaxonomyManager taxonomyManager = new TaxonomyManager();
            taxonomyManager.initTaxonomyStorage();
        } catch (UserStoreException e) {
            log.error("Error occurred while getting RealmConfigurations when initializing taxonomy storage service.",
                    e);
        } catch (RegistryException e) {
            log.error("Error occurred while getting taxonomy files from registry when initializing taxonomy storage "
                    + "service", e);
        } catch (IOException e) {
            log.error("Error occurred while parsing taxonomy xml when initializing taxonomy storage service", e);
        } catch (ParserConfigurationException e) {
            log.error(
                    "Error occurred while building new document for taxonomy when initializing taxonomy storage service"
                            + e);
        } catch (SAXException e) {
            log.error("Error occurred parsing taxonomy content stream when initializing taxonomy storage service", e);
        }

    }

}
