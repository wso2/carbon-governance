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
import java.util.HashMap;
import java.util.Map;

public class GovernanceUtils {

    public static final String DISCOVERY_AGENTS = "DiscoveryAgents";
    public static final String DISCOVERY_AGENT = "DiscoveryAgent";
    public static final String SERVER_TYPE_ID = "ServerTypeId";
    public static final String AGENT_CLASS = "AgentClass";
    public static final String PROPERTY = "property";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String GOVERNANCE_CONFIG_FILE = "governance.xml";
    public static final String COMPARATORS = "Comparators";
    public static final String COMPARATOR = "Comparator";
    public static final String CLASS_ATTR = "class";
    public static final String ENDPOINT_STATE_MANAGEMENT = "EndpointStateManagement";
    public static final String ENDPOINT_STATE_MANAGEMENT_ENABLED = "enabled";
    public static final String DEFAULT_ENDPOINT_ACTIVE_DURATION = "DefaultEndpointActiveDuration";
    private static Log log = LogFactory.getLog(GovernanceUtils.class);


    private static boolean isConfigInitialized = false;

    public static GovernanceConfiguration getGovernanceConfiguration() throws GovernanceConfigurationException {
        GovernanceConfiguration govConfig = GovernanceConfiguration.getInstance();
        if (!isConfigInitialized) {
            String governanceXML = getGovernanceXML();
            File governanceXMLFile = new File(governanceXML);
            InputStream in = null;
            try {
                in = new FileInputStream(governanceXMLFile);
                initGovernanceConfiguration(in, govConfig);
                isConfigInitialized = true;
                if (log.isDebugEnabled()) {
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
        return getCarbonConfigDirPath() + File.separator + GOVERNANCE_CONFIG_FILE;
    }


    private static void readChildElements(Element config,
                                          GovernanceConfiguration govConfig) {
        readDiscoveryAgents(config, govConfig);
        readComparators(config, govConfig);
        readEndpointStateManagement(config, govConfig);
    }

    private static void readDiscoveryAgents(Element config,
                                            GovernanceConfiguration govConfig) {
        Element agentsElement = getFirstElement(config, DISCOVERY_AGENTS);
        if (agentsElement != null) {
            NodeList agents = agentsElement.getElementsByTagName(DISCOVERY_AGENT);
            for (int i = 0; i < agents.getLength(); i++) {
                Element agent = (Element) agents.item(i);
                String serverType = getFirstElementContent(agent, SERVER_TYPE_ID);
                String agentClass = getFirstElementContent(agent, AGENT_CLASS);
                Map<String, String> properties = getProperties(agent);
                properties.put(AGENT_CLASS, agentClass);
                govConfig.addDiscoveryAgentConfig(serverType, properties);
            }
        }

    }

    private static void readComparators(Element config,
                                        GovernanceConfiguration govConfig) {
        Element comparatorsEle = getFirstElement(config, COMPARATORS);
        if (comparatorsEle != null) {
            NodeList comparatorsElements = comparatorsEle.getElementsByTagName(COMPARATOR);
            for (int i = 0; i < comparatorsElements.getLength(); i++) {
                Element comparatorEle = (Element) comparatorsElements.item(i);
                String comparatorClass = comparatorEle.getAttribute(CLASS_ATTR);
                if (comparatorClass != null && !comparatorClass.isEmpty()) {
                    govConfig.addComparator(comparatorClass);
                }
            }
        }

    }


    private static void readEndpointStateManagement(Element config, GovernanceConfiguration govConfig) {
        Element endpointStateManagementEle = getFirstElement(config, ENDPOINT_STATE_MANAGEMENT);
        if (endpointStateManagementEle != null) {
            String enabled = endpointStateManagementEle.getTextContent();
            if (enabled != null && ENDPOINT_STATE_MANAGEMENT_ENABLED.equals(enabled.toLowerCase())) {
                govConfig.setEndpointStateManagementEnabled(true);
            }
        }

        Element DefaultEndpointActiveEle = getFirstElement(config, DEFAULT_ENDPOINT_ACTIVE_DURATION);
        if (DefaultEndpointActiveEle != null) {
            String durationStr = DefaultEndpointActiveEle.getTextContent();
            if (durationStr != null) {
                long duration = Long.valueOf(durationStr);
                govConfig.setDefaultEndpointActiveDuration(duration);
            }
        }
    }

    private static Map<String, String> getProperties(Element agentEle) {
        NodeList propertyNodes = agentEle.getElementsByTagName(PROPERTY);
        Map<String, String> properties = new HashMap<>();
        for (int i = 0; i < propertyNodes.getLength(); i++) {
            Element propertyELe = (Element) propertyNodes.item(i);
            String propertyKey = propertyELe.getAttribute(NAME);
            String propertyValue = propertyELe.getAttribute(VALUE);
            if (propertyKey != null && propertyValue != null && !propertyKey.isEmpty() && !propertyValue.isEmpty()) {
                properties.put(propertyKey, propertyValue);
            }
        }
        return properties;
    }

    private static Element getFirstElement(Element element, String childName) {
        if (element.getElementsByTagName(childName) != null) {
            return (Element) element.getElementsByTagName(childName).item(0);
        }
        return null;
    }

    private static String getFirstElementContent(Element element, String childName) {
        return element.getElementsByTagName(childName).item(0).getTextContent();
    }

    public static String getCarbonConfigDirPath() {
        /* if user set the system property telling where is the configuration directory*/
        String carbonConfigDir =
                System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        if (carbonConfigDir == null) {
            carbonConfigDir = CarbonUtils.getCarbonConfigDirPath();
        }
        return carbonConfigDir;
    }
}

