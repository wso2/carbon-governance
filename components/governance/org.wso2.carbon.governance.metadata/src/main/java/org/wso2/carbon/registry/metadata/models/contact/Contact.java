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

package org.wso2.carbon.registry.metadata.models.contact;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.exception.MetadataException;
import java.util.List;
import java.util.Map;


public class Contact extends Base {

    public static final String CONTACT_FIRST_NAME = "firstname";
    public static final String CONTACT_LAST_NAME = "lastname";
    public static final String CONTACT_TYPE_PHONE = "phone";
    public static final String CONTACT_TYPE_EMAIL = "description";
    public static final String CONTACT_ORGANIZATION = "organization";
    public static final String CONTACT_ADDRESS = "address";
    public static final String CONTACT_COUNTRY = "coutry";
    public static final String CONTACT_TITLE = "title";

    //  Variables defined for the internal implementation
    private static final Log log = LogFactory.getLog(Contact.class);
    private static final String mediaType = "vnd.wso2.contact/+xml;version=1";

    public Contact(Registry registry, String name) throws MetadataException {
        super(mediaType, name, registry);
    }

    public Contact (Registry registry, String name, String uuid, Map<String, List<String>> propertyBag, Map<String, List<String>> attributeMap) throws MetadataException {
        super(mediaType,name, uuid, propertyBag,attributeMap, registry);
    }

    public String getFirstName() {
        return getSingleValuedAttribute(CONTACT_FIRST_NAME);
    }

    public String getLastName() {
        return getSingleValuedAttribute(CONTACT_LAST_NAME);
    }


    public String getContactPhone() {
        return getSingleValuedAttribute(CONTACT_TYPE_PHONE);
    }


    public String getContactEmail() {
        return getSingleValuedAttribute(CONTACT_TYPE_EMAIL);
    }


    public String getContactTitle() {
        return getSingleValuedAttribute(CONTACT_TITLE);
    }


    public String getContactOrganization() {
        return getSingleValuedAttribute(CONTACT_ORGANIZATION);
    }


    public String getContactAddress() {
        return getSingleValuedAttribute(CONTACT_ADDRESS);
    }


    public String getContactCountry() {
        return getSingleValuedAttribute(CONTACT_COUNTRY);
    }


    public static Contact [] find(Registry registry, Map<String, String> criteria) throws MetadataException{
        List<Base> list = find(registry, criteria, mediaType);
        return list.toArray(new Contact[list.size()]);
    }

    public static Contact get(Registry registry, String uuid) throws MetadataException {
        return (Contact) get(registry, uuid, mediaType);
    }

    private String getSingleValuedAttribute(String key){
        List<String> value = attributeMap.get(key);
        return value != null ? value.get(0) : null;
    }
}
