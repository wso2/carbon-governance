/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.metadata.provider;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import org.wso2.carbon.registry.metadata.models.contact.Contact;
import org.wso2.carbon.registry.metadata.provider.util.Util;

import javax.xml.namespace.QName;
import java.util.*;

public class ContactProvider implements BaseProvider  {

    private final String mediaType;
    private final String versionMediaType;

    public ContactProvider(String mediaType, String versionMediaType) {
        this.mediaType = mediaType;
        this.versionMediaType = versionMediaType;
    }

    @Override
    public String getVersionMediaType() {
        return versionMediaType;
    }

    @Override
    public String getMediaType() {
        return mediaType;
    }

    @Override
    public Resource buildResource(Base metadata, Resource resource) throws MetadataException {
        return null;  // no implementation here since we do not give an API to create contacts from metadata
    }

    @Override
    public Resource updateResource(Base newMetadata, Resource resource) throws MetadataException {
        return null;  // no implementation here since we do not give an API to create contacts from metadata
    }

    @Override
    public Base get(Resource resource, Registry registry) throws MetadataException {
        try {
            byte[] contentBytes = (byte[]) resource.getContent();
            OMElement root = Util.buildOMElement(contentBytes);
            Map<String, List<String>> propBag = Util.getPropertyBag(root);
            return getFilledBean(root, propBag, registry);
        } catch (RegistryException e) {
            throw new MetadataException("Error occurred while obtaining resource metadata content uuid = " + resource.getUUID(), e);
        }
    }

    private Contact getFilledBean(OMElement root, Map<String, List<String>> propBag, Registry registry) throws MetadataException {
        Map<String, List<String>> attributeMap = new HashMap<String, List<String>>();
        OMElement attributes = root.getFirstChildWithName(new QName(Constants.CONTENT_ATTRIBUTE_EL_ROOT_NAME));
        String uuid = attributes.getFirstChildWithName(new QName(Constants.ATTRIBUTE_UUID)).getText();
        String name = attributes.getFirstChildWithName(new QName(Constants.ATTRIBUTE_METADATA_NAME)).getText();
        if(attributeMap != null) {
            Iterator itr = attributes.getChildren();
            while (itr.hasNext()) {
                OMElement el = (OMElement) itr.next();
                String key = el.getLocalName();
                String value = el.getText();
                List<String> valList = new ArrayList<String>();
                valList.add(value);
                attributeMap.put(key, valList);
            }
        }
        return new Contact(registry, name, uuid, propBag, attributeMap);
    }
}
