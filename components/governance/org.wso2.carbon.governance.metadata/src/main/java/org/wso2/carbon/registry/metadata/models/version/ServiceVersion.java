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
package org.wso2.carbon.registry.metadata.models.version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.api.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.Util;
import org.wso2.carbon.registry.metadata.VersionBase;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import org.wso2.carbon.registry.metadata.models.endpoint.HTTPEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceVersion extends VersionBase {


//  Variables defined for internal implementation purpose
    private static String mediaType = "vnd.wso2.version/service+xml;version=1";
    private static final Log log = LogFactory.getLog(ServiceVersion.class);

    public ServiceVersion(Registry registry, String name) throws MetadataException {
        super(mediaType,name,registry);
        this.name = name;
    }

    public ServiceVersion(Registry registry, String name, String uuid, String baseName, String baseUUID, Map<String, List<String>> propertyBag, Map<String, List<String>> attributeMap) throws MetadataException {
        super(mediaType,name, uuid, baseName,baseUUID,propertyBag,attributeMap, registry);
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getMediaType() {
        return mediaType;
    }


    public void addEndpoint(HTTPEndpoint httpEndpoint) throws MetadataException {
        HTTPEndpoint.add(registry, httpEndpoint);
        Util.createAssociation(registry, uuid, httpEndpoint.getUUID(), Constants.ASSOCIATION_ENDPOINT);
        Util.createAssociation(registry, httpEndpoint.getUUID(), uuid, Constants.ASSOCIATION_ENDPOINT_OF);
    }

    public HTTPEndpoint[] getEndpoints(HTTPEndpoint httpEndpoint) throws MetadataException {
        ArrayList<HTTPEndpoint> list = new ArrayList<HTTPEndpoint>();
        try {
            for (Association as : Util.getAssociations(registry, uuid, Constants.ASSOCIATION_ENDPOINT)) {
                if (registry.resourceExists(as.getDestinationPath())) {
                    Resource r = registry.get(as.getDestinationPath());
                    list.add((HTTPEndpoint)Util.getBaseProvider(httpEndpoint.getMediaType()).get(r, registry));
                }
            }
        } catch (RegistryException e) {
            throw new MetadataException(e.getMessage(), e);
        }
        return list.toArray(new HTTPEndpoint[list.size()]);
    }

    public void removeEndpoint(String uuid) throws MetadataException {
        try {
            registry.removeAssociation(Util.getMetadataPath(this.uuid,registry),Util.getMetadataPath(uuid,registry),Constants.ASSOCIATION_ENDPOINT);
            registry.removeAssociation(Util.getMetadataPath(uuid,registry), Util.getMetadataPath(this.uuid,registry),Constants.ASSOCIATION_ENDPOINT_OF);

        } catch (RegistryException e) {
            log.error("Error occurred while removing associated endpoints from service " + this.uuid);
            throw new MetadataException(e.getMessage(),e);
        }
    }

    public static void add(Registry registry, VersionBase metadata) throws MetadataException {
        add(registry, metadata, generateMetadataStoragePath(
                metadata.getBaseName()
                , metadata.getName()
                , metadata.getRootStoragePath()));

    }

    public static void update(Registry registry, VersionBase metadata) throws MetadataException {
        update(registry, metadata, generateMetadataStoragePath(
                metadata.getBaseName()
                , metadata.getName()
                , metadata.getRootStoragePath()));
    }


    /**
     * @return all meta data instances and their children that denotes from this particular media type
     */
    public static ServiceVersion[] getAll(Registry registry) throws MetadataException {
        List<VersionBase> list = getAll(registry, mediaType);
        return list.toArray(new ServiceVersion[list.size()]);
    }

    /**
     * Search all meta data instances of this particular type with the given search attributes
     *
     * @param criteria Key value map that has search attributes
     * @return
     */
    public static ServiceVersion[] find(Registry registry, Map<String, String> criteria) throws MetadataException {
        List<VersionBase> list = find(registry, criteria, mediaType);
        return list.toArray(new ServiceVersion[list.size()]);
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     *
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    public static ServiceVersion get(Registry registry, String uuid) throws MetadataException {
        return (ServiceVersion) get(registry, uuid, mediaType);
    }

}
