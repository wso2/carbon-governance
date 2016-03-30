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
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.rest.api.model.TypedList;
import org.wso2.carbon.governance.rest.api.util.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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

            for (Map.Entry<String, List<GovernanceArtifact>> entry : typedList.getArtifacts().entrySet()) {
                shortName = entry.getKey();
                for (GovernanceArtifact artifact : entry.getValue()) {
                    writeGenericArtifact(writer, shortName, artifact, baseURI);
                }
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

    private void writeGenericArtifact(JsonWriter writer, String shortName, GovernanceArtifact artifact, String baseURI)
            throws IOException, GovernanceException {

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
            if (!NAME.equals(key) && value != null) {
                // If the attributes are more than one.
                if (value.length > 1) {
                    StringWriter stringWriter = new StringWriter();
                    JsonWriter jsonWriter = new JsonWriter(stringWriter);
                    jsonWriter.beginObject();
                    for (int i = 0; i < value.length; i++) {
                        // Setting the key and empty string map in JSON for empty values.
                        if (value[i] == null) {
                            value[i] = "";
                        }
                        jsonWriter.name(Integer.toString(i)).value(value[i]);
                    }
                    jsonWriter.endObject();
                    jsonWriter.flush();
                    jsonWriter.close();
                    writer.name(key).value(stringWriter.toString());
                    stringWriter.flush();
                    stringWriter.close();
                    // If only one attribute is received.
                } else if (value.length == 1) {
                    writer.name(key).value(value[0]);
                } else {
                    writer.name(key).nullValue();
                }
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
}
