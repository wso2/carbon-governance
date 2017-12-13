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

package org.wso2.carbon.governance.rest.api.internal;

import com.google.gson.stream.JsonWriter;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.rest.api.model.TypedList;
import org.wso2.carbon.governance.rest.api.util.Util;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GenericArtifactJSONWriter {

    public static final String ASSETS = "assets";
    public static final String INDENT = "  ";
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String OVERVIEW = "overview_";
    public static final String SELF_LINK = "self-link";
    public static final String CONTENT_LINK = "content-link";
    public static final String LINKS = "links";
    public static final String SELF = "self";
    public static final String PREV = "prev";
    public static final String NEXT = "next";
    public static final String BELONG_TO = "belong-to";
    public static final String ENTRY = "entry";

    private final Log log = LogFactory.getLog(GenericArtifactJSONWriter.class);

    public void writeTo(TypedList<GovernanceArtifact> typedList, OutputStream entityStream, String baseURI)
            throws IOException, GovernanceException {
        PrintWriter printWriter = new PrintWriter(entityStream);
        JsonWriter writer = new JsonWriter(printWriter);
        writer.setIndent(INDENT);

        writer.beginObject();
        writer.name(ASSETS);
        String shortName = null;
        if (typedList.hasData()) {
            writer.beginArray();

            try {
                for (Map.Entry<String, List<GovernanceArtifact>> entry : typedList.getArtifacts().entrySet()) {
                    shortName = entry.getKey();
                    GovernanceArtifactConfiguration artifactConfiguration = null;

                    for (GovernanceArtifact artifact : entry.getValue()) {
                        if (artifactConfiguration == null) {
                            artifactConfiguration = GovernanceUtils
                                    .findGovernanceArtifactConfigurationByMediaType(artifact.getMediaType(),
                                            getUserRegistry());
                        }
                        writeGenericArtifact(writer, shortName, artifact, baseURI, artifactConfiguration);
                    }
                }
            } catch (RegistryException e) {
                throw new GovernanceException("Error while finding artifact configuration.", e);
            }
            writer.endArray();
        } else {
            writer.nullValue();
        }

        TypedList.Pagination pagination = typedList.getPagination();
        if (pagination != null) {
            String nextURI = null;
            String prevURI = null;
            String selfURI = generateLink(shortName, baseURI, pagination.getQuery(), pagination.getSelfStart(),
                                          pagination.getCount(), pagination.getTenant());
            if (pagination.getNextStart() != null) {
                nextURI = generateLink(shortName, baseURI, pagination.getQuery(), pagination.getNextStart(),
                                       pagination.getCount(), pagination.getTenant());
            }
            if (pagination.getPreviousStart() != null) {
                prevURI = generateLink(shortName, baseURI, pagination.getQuery(), pagination.getPreviousStart(),
                                       pagination.getCount(), pagination.getTenant());
            }
            writer.name(LINKS);
            writer.beginObject();
            writer.name(SELF).value(selfURI);
            if (prevURI != null) {
                writer.name(PREV).value(prevURI);
            }
            if (nextURI != null) {
                writer.name(NEXT).value(nextURI);
            }
            writer.endObject();
        }

        writer.endObject();
        writer.flush();
        writer.close();
        printWriter.flush();
        printWriter.close();
    }

    private String generateLink(String shortName, String baseURI, String query, int start, int count, String tenant) {
        if (query != null && !query.isEmpty()) {
            query = query + "&";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(Util.generateLink(shortName, baseURI, true));
        builder.append("?");
        builder.append(query);
        builder.append(PaginationInfo.PAGINATION_PARAM_START);
        builder.append("=");
        builder.append(start);
        builder.append("&");
        builder.append(PaginationInfo.PAGINATION_PARAM_COUNT);
        builder.append("=");
        builder.append(count);
        if (tenant != null) {
            builder.append("&");
            builder.append(PaginationInfo.PAGINATION_PARAM_TENANT);
            builder.append("=");
            builder.append(tenant);
        }
        return builder.toString();
    }

    private void writeGenericArtifact(JsonWriter writer, String shortName, GovernanceArtifact artifact, String baseURI,
            GovernanceArtifactConfiguration artifactConfiguration) throws IOException, GovernanceException {

        writer.beginObject();
        writer.name(NAME).value(artifact.getQName().getLocalPart());
        writer.name(ID).value(artifact.getId());
        writer.name(TYPE).value(shortName);
        String belongToLink = Util.generateBelongToLink(artifact, baseURI);
        for (String key : artifact.getAttributeKeys()) {
            //TODO value can be something else not a String value
            // Get all attributes.
            String[] value = artifact.getAttributes(key);
            if (key.indexOf(OVERVIEW) > -1) {
                key = key.replace(OVERVIEW, "");
            }
            // If the attributes are more than one.
            if (!NAME.equals(key) && value != null)
                if (value.length > 1) {
                    if (key.endsWith(ENTRY)) {

                        String optionTextTableName = key.split("_", 2)[0];

                        //Get the table name as a upper camel string.
                        optionTextTableName =
                                optionTextTableName.substring(0, 1).toUpperCase() + optionTextTableName.substring(1)
                                        .toLowerCase();

                        List subheadings = evaluateXpath(artifactConfiguration.getContentDefinition(),
                                "/artifactType/content/table[@name='" + optionTextTableName +
                                        "']/subheading/heading",
                                null);

                        List<String> headings = new ArrayList<>();

                        for (Object subheadingObject : subheadings) {
                            OMElement subheadingElement = (OMElement) subheadingObject;
                            headings.add(subheadingElement.getText());
                        }

                        if (headings.size() > 0) {
                            writer.name(key);
                            writer.beginArray();
                            writer.setIndent("    ");
                            for (int i = 0; i < value.length; i++) {
                                writer.beginObject();
                                // Setting the key and empty string map in JSON for empty values.
                                if (value[i] == null) {
                                    value[i] = "";
                                }

                                String[] optionValues = value[i].split(":", headings.size());

                                if (optionValues.length > 0) {
                                    for (int j = 0; j < optionValues.length; j++) {
                                        writer.name(headings.get(j)).value(optionValues[j]);
                                    }
                                }
                                writer.endObject();
                            }
                            writer.endArray();
                        }
                    } else {
                        writer.name(key);
                        writer.beginArray();
                        for (int i = 0; i < value.length; i++) {
                            if (value[i] == null) {
                                value[i] = "";
                            }
                            writer.value(value[i]);
                        }
                        writer.endArray();
                    }
                    // If only one attribute is received.
                } else if (value.length == 1) {
                    writer.name(key).value(value[0]);
                } else {
                    writer.name(key).nullValue();
                }
        }
        String self_link = Util.generateLink(shortName, artifact.getId(), baseURI);
        writer.name(SELF_LINK).value(self_link);
        writer.name(CONTENT_LINK).value(self_link + "/content");
        if (belongToLink != null) {
            writer.name(BELONG_TO).value(belongToLink);
        }
        writer.endObject();
    }

    /**
     * This method is used to get the user registry.
     *
     * @return  user registry object.
     * @throws RegistryException
     */
    private Registry getUserRegistry() throws RegistryException {
        CarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RegistryService registryService = (RegistryService) carbonContext.
                getOSGiService(RegistryService.class, null);
        return registryService.getGovernanceUserRegistry(carbonContext.getUsername(), carbonContext.getTenantId());

    }


    /**
     * This method is used to evaluate an xpath.
     *
     * @param contentElement OM element that the xpath is bean evaluated.
     * @param xpathString    xPath
     * @param nsPrefix       namespace prefix
     * @return
     */
    public List evaluateXpath(OMElement contentElement, String xpathString, String nsPrefix) {
        List resultsList = new ArrayList();
        try {
            AXIOMXPath xpath = new AXIOMXPath(xpathString);

            Iterator nsIterator = contentElement.getAllDeclaredNamespaces();
            if (nsIterator.hasNext()) {
                while (nsIterator.hasNext()) {
                    OMNamespace next = (OMNamespace) nsIterator.next();
                    xpath.addNamespace(nsPrefix, next.getNamespaceURI());
                    resultsList.addAll(xpath.selectNodes(contentElement));
                }
            } else if (contentElement.getDefaultNamespace() != null) {
                xpath.addNamespace(nsPrefix, contentElement.getDefaultNamespace().getNamespaceURI());
                resultsList.addAll(xpath.selectNodes(contentElement));
            } else if (nsPrefix != null) {
                xpathString = xpathString.replace(nsPrefix + ":", "");
                xpath = new AXIOMXPath(xpathString);
                resultsList.addAll(xpath.selectNodes(contentElement));
            } else {
                xpath = new AXIOMXPath(xpathString);
                resultsList.addAll(xpath.selectNodes(contentElement));
            }
            return resultsList;
        } catch (JaxenException e) {
            log.error("Error while evaluating xPath: '" + xpathString + "'.", e);
        }
        return null;
    }

}
