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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.rest.api.model.AssetStateChange;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class LCStateMessageBodyReader extends JSONMessageBodyReader implements MessageBodyReader<AssetStateChange> {

    private final Log log = LogFactory.getLog(LCStateMessageBodyReader.class);

    public static final String ATTR_LIFECYCLE = "lc";
    public static final String ATTR_ACTION = "action";
    public static final String UTF_8 = "UTF-8";

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (AssetStateChange.class.getName().equals(type.getName())) {
            if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AssetStateChange readFrom(Class<AssetStateChange> type, Type genericType, Annotation[] annotations,
                                  MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                                  InputStream entityStream) throws IOException, WebApplicationException {
        JsonReader reader = new JsonReader(new InputStreamReader(entityStream, UTF_8));
        Map<String, Object> map = new HashMap<>();
        reader.setLenient(true);
        handleJSON(reader, map);
        try {
            return createLCStateChange(map);
        } catch (GovernanceException e) {
            log.error(e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    private AssetStateChange createLCStateChange(Map<String, Object> map) throws GovernanceException {
        String lc = (String) map.get(ATTR_LIFECYCLE);
        String action = (String) map.get(ATTR_ACTION);
        if (lc != null && action != null) {
            AssetStateChange stateChange = new AssetStateChange();
            stateChange.setLifecycle(lc);
            stateChange.setAction(action);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                if (key.contains("item_")) {
                    String itemNumber = key.substring(5);
                    stateChange.addParameter(itemNumber.concat(".item"), (String) entry.getValue());
                }
            }
            return stateChange;
        }
        throw new GovernanceException("Can't create LCState");
    }
}
