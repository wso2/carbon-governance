/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.governance.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.governance.common.GovernanceConfiguration;
import org.wso2.carbon.governance.common.GovernanceConfigurationException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GovernanceUtils {

    private static Log log = LogFactory.getLog(GovernanceUtils.class);


    private static boolean isConfigInitialized = false;

    public static GovernanceConfiguration getGovernanceConfiguration() throws GovernanceConfigurationException{
        GovernanceConfiguration govConfig = GovernanceConfiguration.getInstance();
        if (!isConfigInitialized) {
            String governanceXML = getGovernanceXML();
            File governanceXMLFile = new File(governanceXML);
            InputStream in = null;
            try {
                in = new FileInputStream(governanceXMLFile);
                initGovernanceConfiguration(in, govConfig);
                isConfigInitialized = true;
                if(log.isDebugEnabled()){
                    log.debug(govConfig);
                }
            } catch (IOException e) {
                throw new GovernanceConfigurationException("Cannot read file " + governanceXML, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.warn("Cannot close file " + governanceXML, e);
                    }
                }
            }
        }
        return govConfig;
    }

    private static void initGovernanceConfiguration(InputStream in, GovernanceConfiguration govConfig)
            throws GovernanceConfigurationException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(in);
            readChildElements(document.getDocumentElement(), govConfig);
            isConfigInitialized = true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.fatal("Problem in parsing governance configuration file ", e);
            throw new GovernanceConfigurationException(e);
        }
    }

    private static String getGovernanceXML() {
            return getCarbonConfigDirPath() + File.separator + "governance.xml";
        }


    private static void readChildElements(Element config,
                                          GovernanceConfiguration govConfig) {
        readDiscoveryAgents(config, govConfig);
    }

    private static void readDiscoveryAgents(Element config,
                                            GovernanceConfiguration govConfig) {
        Element agentsElement = getFirstElement(config, "DiscoveryAgents");
        NodeList agents = agentsElement.getElementsByTagName("DiscoveryAgent");
        for (int i = 0; i < agents.getLength(); i++) {
            Element agent = (Element) agents.item(i);
            String serverType = getFirstElementContent(agent, "ServerTypeId");
            String agentClass = getFirstElementContent(agent, "AgentClass");
            govConfig.addDiscoveryAgentConfig(serverType, agentClass);
        }
    }

    private static Element getFirstElement(Element element, String childName) {
        return (Element) element.getElementsByTagName(childName).item(0);
    }

    private static String getFirstElementContent(Element element, String childName) {
        return element.getElementsByTagName(childName).item(0).getTextContent();
    }

    public static String getCarbonConfigDirPath() {
        /* if user set the system property telling where is the configuration directory*/
        String carbonConfigDir =
                System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        if (carbonConfigDir == null) {
            carbonConfigDir =  CarbonUtils.getCarbonConfigDirPath();
        }
        return carbonConfigDir;
    }
}

