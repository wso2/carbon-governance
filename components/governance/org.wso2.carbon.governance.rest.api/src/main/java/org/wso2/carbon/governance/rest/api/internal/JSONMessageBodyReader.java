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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class JSONMessageBodyReader {

    protected void handleJSON(JsonReader reader, Map<String, Object> map) throws IOException {
        handleObject(reader, map, null);
    }


    protected void handleObject(JsonReader reader, Map<String, Object> map, JsonToken token) throws IOException {
        String key = null;
        while (true) {
            if (token == null) {
                token = reader.peek();
            }

            if (JsonToken.BEGIN_OBJECT.equals(token)) {
                reader.beginObject();

            } else if (JsonToken.END_OBJECT.equals(token)) {
                reader.endObject();
                break;

            } else if (JsonToken.NAME.equals(token)) {
                key = reader.nextName();

            } else if (JsonToken.STRING.equals(token)) {
                String value = reader.nextString();
                if (key != null) {
                    map.put(key, value);
                    key = null;
                }

            } else if (JsonToken.NUMBER.equals(token)) {
                Double value = reader.nextDouble();
                if (key != null) {
                    map.put(key, String.valueOf(value));
                    key = null;
                }

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


    private Map<String, Object> handleArray(JsonReader reader) throws IOException {
        Map<String, Object> values = new HashMap<>();
        reader.beginArray();
        while (reader.hasNext()) {
            JsonToken token = reader.peek();
            if (token.equals(JsonToken.END_ARRAY)) {
                reader.endArray();
            } else {
                handleObject(reader, values, token);
            }
        }
        return values;
    }


}
