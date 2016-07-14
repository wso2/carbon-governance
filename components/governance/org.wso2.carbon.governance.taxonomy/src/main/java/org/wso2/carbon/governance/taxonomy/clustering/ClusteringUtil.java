/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.governance.taxonomy.clustering;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Hashtable;

public class ClusteringUtil {

    private static final Log log = LogFactory.getLog(ClusteringUtil.class);

    /**
     * Sends a cluster message to other members of the cluster.If message transmission fails it will
     * create a thread which will attempt retry transmission for a predefined amount of retry attempts.
     *
     * @param message The message to be transmitted
     */
    public static void sendClusterMessage(ClusteringMessage message) {
        ClusteringAgent agent = createClusteringAgent();
        if (agent == null) {
            //log.error("Unable to send the clustering message as a clustering agent was not obtained.");
            if (log.isDebugEnabled()) {
                log.debug(String.format("Failed to send cluster message :%s ", message));
            }
            return;
        }
        try {
            agent.sendMessage(message, true);
            if (log.isDebugEnabled()) {

                log.debug(String.format("Successfully transmitted cluster message :%s", message));
            }
        } catch (ClusteringFault e) {
            if (log.isDebugEnabled()) {
                log.error("Unable to send the clustering message.The system will now attempt to retry " +
                        "sending the message", e);
            }
        }
    }

    public static ClusteringAgent createClusteringAgent() {
        ClusteringAgent agent = null;
        try {
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            ConfigurationContextService configContextSvc = (ConfigurationContextService) privilegedCarbonContext.
                    getOSGiService(Class.forName("org.wso2.carbon.utils.ConfigurationContextService"),
                            new Hashtable<String, String>());
            agent = configContextSvc.getServerConfigContext().getAxisConfiguration().getClusteringAgent();
        } catch (ClassNotFoundException e) {
            log.error("Unable to create clustering agent", e);
        }
        return agent;
    }
}
