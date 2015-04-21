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
package org.wso2.carbon.registry.metadata.models.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import org.wso2.carbon.registry.metadata.models.version.ServiceVersion;
import org.wso2.carbon.registry.metadata.VersionBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HTTPService extends Base {

//  Type specific attributes goes here
    public static final String KEY_OWNER = "owner";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_TYPE = "type";
    public static final String KEY_REPOSITORY_TYPE = "repositoryType";


    //  Variables defined for the internal implementation
    private static final Log log = LogFactory.getLog(HTTPService.class);
    private static final String mediaType = "vnd.wso2.service/http+xml;version=1";

    public HTTPService(Registry registry, String name) throws MetadataException {
        super(mediaType, name, registry);
    }

    public HTTPService(Registry registry, String name, VersionBase version) throws MetadataException {
        super(mediaType, name, registry,version);
    }

    public HTTPService(Registry registry, String name, String uuid, Map<String, List<String>> propertyBag, Map<String, List<String>> attributeMap) throws MetadataException {
        super(mediaType,name, uuid, propertyBag,attributeMap, registry);
    }

    public ServiceVersion newVersion(String key) throws MetadataException {
        ServiceVersion v = new ServiceVersion(registry, key);
        v.setBaseUUID(uuid);
        v.setBaseName(name);
        return v;
    }

    public void setOwner(String owner) {
       setAttribute(KEY_OWNER,owner);
    }

    public void setDescription(String decs) {
        setAttribute(KEY_DESCRIPTION,decs);
    }

    public void setType(String type) {
        setAttribute(KEY_TYPE,type);
    }

    public void setRepositoryType(String type) {
        setAttribute(KEY_REPOSITORY_TYPE,type);
    }

    public String getOwner() {
       return getSingleValuedAttribute(KEY_OWNER);
    }

    public String getDescription() {
        return getSingleValuedAttribute(KEY_DESCRIPTION);
    }

    public String getType() {
        return getSingleValuedAttribute(KEY_TYPE);
    }

    public String getRepositoryType() {
        return getSingleValuedAttribute(KEY_REPOSITORY_TYPE);
    }

    public static void add(Registry registry, Base metadata) throws MetadataException {
        if (((HTTPService) metadata).baseVersion == null) {
            add(registry, metadata,
                    generateMetadataStoragePath(metadata.getName(), metadata.getRootStoragePath()));
        } else {
            add(registry, metadata,
                    generateMetadataStoragePath(metadata.getName(), metadata.getRootStoragePath()));
            ServiceVersion.add(registry, ((HTTPService) metadata).baseVersion);
        }
    }

    public static void update(Registry registry, Base metadata) throws MetadataException {
        update(registry, metadata,
                generateMetadataStoragePath(metadata.getName(), metadata.getRootStoragePath()));
    }

    /**
     * @return all meta data instances and their children that denotes from this particular media type
     */
    public static HTTPService[] getAll(Registry registry) throws MetadataException {
        List<Base> list = getAll(registry, mediaType);
        return list.toArray(new HTTPService[list.size()]);
    }

    /**
     * Search all meta data instances of this particular type with the given search attributes
     *
     * @param criteria Key value map that has search attributes
     * @return
     */
    public static HTTPService[] find(Registry registry, Map<String, String> criteria) throws MetadataException {
        List<Base> list = find(registry, criteria, mediaType);
        return list.toArray(new HTTPService[list.size()]);
    }

    /**
     * Returns the meta data instance that can be identified by the given UUID
     *
     * @param uuid - UUID of the metadata insatnce
     * @return meta data from the UUID
     */
    public static HTTPService get(Registry registry, String uuid) throws MetadataException {
        return (HTTPService) get(registry, uuid, mediaType);
    }

    private void setAttribute(String key,String val){
        List<String> value = new ArrayList<String>();
        value.add(val);
        attributeMap.put(key,value);
    }

    private String getSingleValuedAttribute(String key){
        List<String> value = attributeMap.get(key);
        return value != null ? value.get(0) : null;
    }

}
