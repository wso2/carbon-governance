/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.governance.rest.api.model.AssetAssociateModel;

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

public class AssetAssocMessageBodyReader extends JSONMessageBodyReader implements MessageBodyReader<AssetAssociateModel> {

    private final Log log = LogFactory.getLog(AssetAssocMessageBodyReader.class);
    private static final String UTF_8 = "UTF-8";
    private static final String DESTINATION_TYPE = "destAssetType";
    private static final String DESTINATION_ID = "destAssetID";
    private static final String SOURCE_ASSOC_TYPE = "sourceAssocType";
    private static final String DESTINATION_ASSOC_TYPE = "destAssocType";

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (AssetAssociateModel.class.getName().equals(type.getName())) {
            if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AssetAssociateModel readFrom(Class<AssetAssociateModel> aClass, Type type, Annotation[] annotations,
                                        MediaType mediaType, MultivaluedMap<String, String> multivaluedMap,
                                        InputStream inputStream) throws IOException, WebApplicationException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, UTF_8));
        Map<String, Object> map = new HashMap<>();
        reader.setLenient(true);
        handleJSON(reader, map);
        try {
            return createAssetAssociation(map);
        } catch (GovernanceException e) {
            log.error(e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    private AssetAssociateModel createAssetAssociation(Map<String, Object> map) throws GovernanceException {
        String destAssetType = (String) map.get(DESTINATION_TYPE);
        String destAssetID = (String) map.get(DESTINATION_ID);
        String sourceAssocType = (String) map.get(SOURCE_ASSOC_TYPE);
        String destAssocType = (String) map.get(DESTINATION_ASSOC_TYPE);

        if (destAssetType != null && destAssetID != null && sourceAssocType != null && destAssocType != null) {
            AssetAssociateModel assetAssociateModel = new AssetAssociateModel();
            assetAssociateModel.setDestAssocType(destAssocType);
            assetAssociateModel.setDestAssetID(destAssetID);
            assetAssociateModel.setDestAssetType(destAssetType);
            assetAssociateModel.setSourceAssocType(sourceAssocType);
            return assetAssociateModel;
        }
        throw new GovernanceException("Can't create asset associate model");
    }
}
