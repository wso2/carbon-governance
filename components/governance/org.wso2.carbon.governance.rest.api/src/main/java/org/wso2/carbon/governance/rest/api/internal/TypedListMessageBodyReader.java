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

import com.google.gson.stream.JsonReader;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TypedListMessageBodyReader extends JSONMessageBodyReader implements MessageBodyReader<GenericArtifact> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (GenericArtifact.class.getName().equals(type.getName())) {
            if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType) || MediaType.APPLICATION_XML_TYPE.equals(mediaType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public GenericArtifact readFrom(Class<GenericArtifact> type, Type genericType, Annotation[] annotations,
                                    MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                                    InputStream entityStream) throws IOException, WebApplicationException {
        JsonReader reader = new JsonReader(new InputStreamReader(entityStream, "UTF-8"));
        Map<String, Object> map = new HashMap<>();
        reader.setLenient(true);
        handleJSON(reader, map);
        return createGenericArtifact(map);
    }


    private GenericArtifact createGenericArtifact(Map<String, Object> map) {
        String name = (String) map.get("name");
        String type = (String) map.get("type");
        if (!name.isEmpty() && !type.isEmpty()) {
            GenericArtifact artifact = GenericArtifactManager.newDetachedGovernanceArtifact(new QName(name), type);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                if (key.indexOf("_") == -1) {
                    key = "overview_".concat(key);
                }
                try {
                    artifact.addAttribute(key, String.valueOf(entry.getValue()));
                } catch (GovernanceException e) {
                    e.printStackTrace();
                    break;
                }
            }
            return artifact;
        }
        return null;
    }


}
