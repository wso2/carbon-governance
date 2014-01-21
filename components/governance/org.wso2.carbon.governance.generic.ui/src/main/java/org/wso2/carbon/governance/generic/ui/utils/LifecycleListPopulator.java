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
import org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient;

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
     * {@inheritDoc}
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
}
