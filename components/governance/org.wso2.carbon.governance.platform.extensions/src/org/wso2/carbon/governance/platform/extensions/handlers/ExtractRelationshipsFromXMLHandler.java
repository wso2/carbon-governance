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
package org.wso2.carbon.governance.platform.extensions.handlers;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.governance.platform.extensions.util.Utils;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * A handler used to create dependencies between XML-artifacts. This handler can be used to create
 * a dependency hierarchy between ESB artifacts. For example, here's how you can build a 
 * relationship structure between Proxy Services and dependent Sequences and Endpoints.
 * <pre>   
 *   &lt;handler class="org.wso2.carbon.governance.platform.extensions.handlers.ExtractRelationshipsFromXMLHandler"&gt;
 *       &lt;property name="testExistence"&gt;false&lt;/property&gt;
 *       &lt;property name="configuration" type="xml"&gt;
 *           &lt;relationship xpath="//ns:target/@faultSequence | //ns:target/@inSequence | //ns:target/@outSequence | //ns:sequence/@key"
 *                   destination="/_system/config/repository/synapse/default/sequences" &gt;
 *               &lt;namespace prefix="ns" uri="http://ws.apache.org/ns/synapse" /&gt;
 *           &lt;/relationship&gt;
 *           &lt;relationship xpath="//ns:target/@endpoint | //ns:endpoint/@key"
 *                   destination="/_system/config/repository/synapse/default/endpoints" &gt;
 *               &lt;namespace prefix="ns" uri="http://ws.apache.org/ns/synapse" /&gt;
 *           &lt;/relationship&gt;
 *       &lt;/property&gt;
 *       &lt;filter class="org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher"&gt;
 *           &lt;property name="pattern"&gt;/_system/config/repository/synapse/default/proxy-services/.*&lt;/property&gt;
 *       &lt;/filter&gt;
 *   &lt;/handler&gt;
 * </pre>
 */
@SuppressWarnings("unused")
public class ExtractRelationshipsFromXMLHandler extends Handler {

    private static final Log log = LogFactory.getLog(ExtractRelationshipsFromXMLHandler.class);

    private static final String CONFIG_REGISTRY_PREFIX = "conf";
    private static final String GOVERNANCE_REGISTRY_PREFIX = "gov";

    private List<Relationship> relationshipList = new LinkedList<Relationship>();
    private Set<String> types = new HashSet<String>();
    private boolean testExistence = true;

    public void setTestExistence(String testExistence) {
        this.testExistence = Boolean.toString(true).equals(testExistence);
    }

    public void setConfiguration(OMElement configuration) {
        Iterator relationships =
                configuration.getChildrenWithName(new QName("relationship"));
        while (relationships.hasNext()) {
            OMElement element = (OMElement) relationships.next();
            String type = element.getAttributeValue(new QName("type"));
            if (type == null) {
                type = "depends";
            }
            types.add(type);
            String[] xpath = element.getAttributeValue(new QName("xpath")).split(",");
            List<QName> namespaceList = new LinkedList<QName>();
            Iterator namespaces = element.getChildrenWithName(new QName("namespace"));
            while (namespaces.hasNext()) {
                OMElement namespace = (OMElement) namespaces.next();
                namespaceList.add(new QName(namespace.getAttributeValue(new QName("uri")), "dummy",
                        namespace.getAttributeValue(new QName("prefix"))));
            }
            List<AXIOMXPath> xpathList = new LinkedList<AXIOMXPath>();
            if (xpath != null) {
                for (String temp : xpath) {
                    temp = temp.trim();
                    if (temp.endsWith("/")) {
                        temp = temp.substring(0, temp.length() - 1);
                    }
                    try {
                        AXIOMXPath expression = new AXIOMXPath(temp);
                        for (QName namespace : namespaceList) {
                            expression.addNamespace(namespace.getPrefix(),
                                    namespace.getNamespaceURI());
                        }
                        xpathList.add(expression);
                    } catch (JaxenException e) {
                        log.error("Unable to compile xpath for expression", e);
                    }
                }
            }
            String[] destination =
                    element.getAttributeValue(new QName("destination")).split(",");
            List<String> destinationList = new LinkedList<String>();
            if (destination != null) {
                for (String temp : destination) {
                    destinationList.add(temp.trim());
                }
            }
            relationshipList.add(
                    new Relationship(xpathList.toArray(new AXIOMXPath[xpathList.size()]),
                            destinationList.toArray(new String[destinationList.size()]), type));
        }
    }

    public void put(RequestContext requestContext) throws RegistryException {
        String source = requestContext.getResourcePath().getPath();
        Registry registry = requestContext.getRegistry();

        // first get rid of all outward associations from this artifact belonging to the types for
        // which relationships are defined.
        Association[] associations = registry.getAllAssociations(source);
        if (associations != null) {
            for (Association association : associations) {
                String sourcePath = association.getSourcePath();
                String destinationPath = association.getDestinationPath();
                String associationType = association.getAssociationType();
                if (sourcePath.equals(source) && types.contains(associationType)) {
                    registry.removeAssociation(sourcePath, destinationPath, associationType);
                }
            }
        }

        // then compute associations that are to be added as per handler configuration.
        OMElement content = Utils.extractPayload(requestContext.getResource());
        for (Relationship relationship : relationshipList) {
            List<String> destinations = new LinkedList<String>();
            for (AXIOMXPath xpath : relationship.getXpathExpressions()) {
                try {
                    Object results = xpath.evaluate(content);
                    if (results != null) {
                        for (Object result : (ArrayList)results) {
                            if (result instanceof OMAttribute) {
                                destinations.add(((OMAttribute)result).getAttributeValue());
                            } else if (result instanceof OMElement) {
                                destinations.add(((OMElement)result).getText());
                            }
                        }
                    }
                } catch (JaxenException ignored) {
                    // any failure of a known reason is not our concern.
                } catch (ClassCastException ignored) {
                    // any failure of a known reason is not our concern.
                }
            }

            // create required associations for each defined relationship.
            if (destinations.size() > 0) {
                List<String> paths = new LinkedList<String>();
                for (String parent : relationship.getDestinations()) {
                    for (String child : destinations) {
                        paths.add(computePath(parent, child));
                    }
                }
                String type = relationship.getType();
                for (String target : paths) {
                    if (!testExistence || registry.resourceExists(target)) {
                        registry.addAssociation(source, target, type);
                    }
                }
            }
        }
    }

    private String computePath(String parent, String child) {
        if (child.indexOf(":") > 0) {
            String[] temp = child.split(":");
            if (CONFIG_REGISTRY_PREFIX.equals(temp[0])) {
                parent = RegistryConstants.CONFIG_REGISTRY_BASE_PATH;
            } else if (GOVERNANCE_REGISTRY_PREFIX.equals(temp[0])) {
                parent = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH;
            }
            return parent + temp[1];
        } else {
            return parent + RegistryConstants.PATH_SEPARATOR + child;
        }
    }

    private static class Relationship {
        private AXIOMXPath[] xpathExpressions;
        private String[] destinations;
        private String type;

        private Relationship(AXIOMXPath[] xpathExpressions, String[] destinations, String type) {
            this.xpathExpressions = xpathExpressions;
            this.destinations = destinations;
            this.type = type;
        }

        public List<AXIOMXPath> getXpathExpressions() {
            return Arrays.asList(xpathExpressions);
        }

        public List<String> getDestinations() {
            return Arrays.asList(destinations);
        }

        public String getType() {
            return type;
        }
    }
}
