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
package org.wso2.carbon.governance.generic.ui.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Method to populate the list of lifecycle names.
 */
public class LifecycleListPopulator implements DropDownDataPopulator {

    private static final Log log = LogFactory.getLog(LifecycleListPopulator.class);

    /**
     * Method to obtain the list of strings to be displayed in ascending order.
     *
     * @param request The HTTP request that was made.
     * @param config  The HTTP servlet configuration.
     * @return the list of strings.
     */
    public String[] getList(HttpServletRequest request, ServletConfig config) {
        try {
            ManageGenericArtifactServiceClient client =
                    new ManageGenericArtifactServiceClient(config, request.getSession());
            String[] lifeCycleList = client.getAvailableAspects();
            if (lifeCycleList != null) {
                List<String> output = new ArrayList<String>(Arrays.asList(lifeCycleList));
                output.add(0, "None");
                return output.toArray(new String[output.size()]);
            }
        } catch (Exception e) {
            log.error("An error occurred while obtaining the list of lifecycles.", e);
        }
        return new String[0];
    }

    /**
     * Method to obtain the list of strings to be displayed and this method only used in publisher.
     *
     * @param uuid UUID of the resource.
     * @param path  The HTTP servlet configuration.
     * @param registry The Registry instance
     * @return the list of strings.
     */
    public String[] getList(String uuid, String path, Registry registry) {
        try {
            String[] lifeCycleList = GovernanceUtils.getAvailableAspects();
            if (lifeCycleList != null) {
                List<String> output = new ArrayList<String>(Arrays.asList(lifeCycleList));
                output.add(0, "None");
                return output.toArray(new String[output.size()]);
            }
        } catch (RegistryException e) {
            log.error("An error occurred while obtaining the list of lifecycles.", e);
        }
        return new String[0];
    }
}
