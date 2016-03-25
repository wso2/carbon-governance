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

import org.apache.commons.io.IOUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * This message body writer is to handle special case where
 * a content artifact type consist of application/json data
 */

@Provider
@Consumes({"application/json"})
public class ContentArtifactMessageBodyWriter implements MessageBodyWriter<ByteArrayInputStream> {
    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return ByteArrayInputStream.class.isAssignableFrom(aClass);
    }

    @Override
    public long getSize(ByteArrayInputStream byteArrayInputStream, Class<?> aClass, Type type, Annotation[] annotations,
                        MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(ByteArrayInputStream byteArrayInputStream, Class<?> aClass, Type type, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> stringObjectMultivaluedMap,
                        OutputStream outputStream) throws IOException, WebApplicationException {
        writeContentArtifact(byteArrayInputStream, outputStream);
    }

    private void writeContentArtifact(ByteArrayInputStream byteArrayInputStream, OutputStream outputStream)
            throws IOException {
        IOUtils.copy(byteArrayInputStream, outputStream);
        byteArrayInputStream.close();
        outputStream.close();
    }
}
