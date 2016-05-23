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
import com.google.gson.stream.JsonToken;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class JSONMessageBodyReader {

    protected void handleJSON(JsonReader reader, Map<String, Object> map) throws IOException {
        handleObject(reader, map, null, false);
    }

    /**
     * Traverses through a json object and maps the keys and values to a {@link Map}
     *
     * @param reader        {@link JsonReader}
     * @param map           map that the values to be added.
     * @param token         {@link JsonToken}
     * @param isArray       whether the object is inside a json array
     * @throws IOException  If unable to parse the json object
     */
    protected void handleObject(JsonReader reader, Map<String, Object> map, JsonToken token, boolean isArray)
            throws IOException {
        String key = null;
        while (true) {
            if (token == null) {
                token = reader.peek();
            }

            if (JsonToken.BEGIN_OBJECT.equals(token)) {
                reader.beginObject();

            } else if (JsonToken.END_OBJECT.equals(token)) {
                reader.endObject();

            } else if (JsonToken.NAME.equals(token)) {
                key = reader.nextName();

            } else if (JsonToken.STRING.equals(token)) {
                String value = reader.nextString();
                handleValue(key, value, map, isArray);
                key = null;

            } else if (JsonToken.NUMBER.equals(token)) {
                Double value = reader.nextDouble();
                handleValue(key, value, map, isArray);
                key = null;

            } else if (token.equals(JsonToken.BEGIN_ARRAY)) {
                Map<String, Object> values = handleArray(reader);
                if (key != null) {
                    map.put(key, values);
                }

            } else {
                reader.skipValue();
            }

            if (reader.hasNext()) {
                token = reader.peek();
            } else {
                break;
            }
        }
    }

    /**
     * Traverse through a json array and maps the array elements to a {@link Map}
     *
     * @param reader        {@link JsonReader}
     * @return              map with json array values mapped to key value pairs.
     * @throws IOException  If unable to parse the json array.
     */
    private Map<String, Object> handleArray(JsonReader reader) throws IOException {
        Map<String, Object> values = new HashMap<>();
        reader.beginArray();
        JsonToken token = reader.peek();
        while (token != null) {
            if (token.equals(JsonToken.END_ARRAY)) {
                reader.endArray();
                return values;
            } else {
                handleObject(reader, values, token, true);
            }
            token = reader.peek();
        }
        return values;
    }

    /**
     * Inserts json values to the given map. Handles multi valued attributes such as unbounded attributes
     * and assigns them to a {@link Map}.
     *
     * @param key       key to be added.
     * @param value     value to be added.
     * @param map       the map that the value being added.
     * @param isArray   whether the value is included inside a json array.
     */
    private void handleValue(String key, Object value, Map<String, Object> map, boolean isArray) {
        /*
         * Fix for REGISTRY-3613
         * Enabling multiple values with the same key to be added as attributes in order to save
         * unbounded elements such as endpoints.
         */
        if (isArray) {
            List values;
            if (StringUtils.isNotBlank(key)) {
                if (map.containsKey(key) && map.get(key) instanceof List) {
                    values = (List) map.get(key);
                } else {
                    values = new ArrayList<>();
                }
                values.add(value);
                map.put(key, values);
            }
        } else {
            map.put(key, String.valueOf(value));
        }
    }
}
