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

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.rest.api.model.TypedList;
import org.wso2.carbon.governance.rest.api.util.RESTUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class GenericArtifactJSONWriter {

    public static final String ASSETS = "assets";
    public static final String INDENT = "  ";
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String OVERVIEW = "overview_";
    public static final String LINK = "link";

    public void writeTo(TypedList<GenericArtifact> typedList, OutputStream entityStream, String baseURI)
            throws IOException, GovernanceException {
        PrintWriter printWriter = new PrintWriter(entityStream);
        JsonWriter writer = new JsonWriter(printWriter);
        writer.setIndent(INDENT);

        writer.beginObject();
        writer.name(ASSETS);
        if (typedList.hasData()) {
            writer.beginArray();

            for (Map.Entry<String, List<GenericArtifact>> entry : typedList.getArtifacts().entrySet()) {
                String shortName = entry.getKey();
                for (GenericArtifact artifact : entry.getValue()) {
                    writeGenericArtifact(writer, shortName, artifact, baseURI);
                }
            }
            writer.endArray();
        } else {
            writer.nullValue();
        }
         //TODO support pagination here.
//        writer.name("links");
//        writer.beginObject();
//        writer.name("self").value(baseURI);
//        writer.name("prev").value(baseURI);
//        writer.name("next").value(baseURI);
//        writer.endObject();
        writer.endObject();

        writer.flush();
        writer.close();
        printWriter.flush();
        printWriter.close();
    }

    private void writeGenericArtifact(JsonWriter writer, String shortName, GenericArtifact artifact, String baseURI)
            throws IOException, GovernanceException {

        writer.beginObject();
        writer.name(NAME).value(artifact.getQName().getLocalPart());
        writer.name(ID).value(artifact.getId());
        writer.name(TYPE).value(shortName);
        for (String key : artifact.getAttributeKeys()) {
            //TODO value can be something else not alweys String value
            String value = artifact.getAttribute(key);
            if (key.indexOf(OVERVIEW) > -1) {
                key = key.replace(OVERVIEW, "");
            }
            if (!NAME.equals(key) && value != null) {
                writer.name(key).value(value);
            }
        }
        //Add links
        writer.name(LINK).value(RESTUtil.generateLink(shortName, artifact.getId(), baseURI));
        writer.endObject();
    }

    private void writeToJSON(TypedList<?> typedList, OutputStream entityStream) {
        PrintWriter printWriter = new PrintWriter(entityStream);
        JsonObject jObject = convertToJSON(typedList);
        printWriter.write(jObject.toString());
        printWriter.flush();
    }

    private JsonObject convertToJSON(TypedList<?> typedList) {
        JsonObject jObject = new JsonObject();
        jObject.addProperty(ASSETS, 1);
        if (GenericArtifact.class.isAssignableFrom(typedList.getType())) {
            // return converGenericArtifactToJSON((List<GenericArtifact>) typedList.getArtifacts());
        }
        return null;
    }

}
