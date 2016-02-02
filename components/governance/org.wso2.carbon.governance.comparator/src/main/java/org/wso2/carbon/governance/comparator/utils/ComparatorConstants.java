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

package org.wso2.carbon.governance.comparator.utils;

public class ComparatorConstants {

    // Media types
    public static final String WSDL_MEDIA_TYPE = "application/wsdl+xml";
    public static final String WADL_MEDIA_TYPE = "application/wadl+xml";
    public static final String SCHEMA_MEDIA_TYPE = "application/xsd+xml";
    public static final String TEXT_PLAIN_MEDIA_TYPE = "text/plain";


    // WSDL constants
    public static final String WSDL_DECLARATION = "wsdl_declaration";
    public static final String WSDL_IMPORTS = "wsdl_imports";
    public static final String WSDL_MESSAGES = "wsdl_messages";
    public static final String WSDL_PORTTYPES = "wsdl_porttype";
    public static final String WSDL_OPERATIONS = "wsdl_operations";

    public static final String WSDL_DECLARATION_START_ELEMENT = "<wsdl:definitions";
    public static final String WSDL_DECLARATION_START = "<wsdl:definitions xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\">";
    public static final String WSDL_DECLARATION_END_ELEMENT = "</wsdl:definitions>";
    public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    //Text messages
    public static final String TEXT_CHANGE = "";
    public static final String XML = "xml";
    public static final String JSON = "json";
    public static final String XML_INDENT_AMOUNT = "{http://xml.apache.org/xslt}indent-amount";
    public static final String TWO = "2";
    public static final String YES = "yes";
    public static final String UTF_8 = "UTF-8";


    //WSDL messages
    public static final String DECLARATION_HAS_CHANGED = "Declaration section of the WSDL Definition has changed";
    public static final String NEW_IMPORTS = "There are new WSDL Imports";
    public static final String REMOVE_IMPORTS = "There are WSDL Import removals";
    public static final String CHANGED_IMPORTS = "There are WSDL Import modifications";
    public static final String NEW_MESSAGES = "There are new WSDL Messages";
    public static final String REMOVE_MESSAGES = "There are WSDL Message removals";
    public static final String CHANGED_MESSAGES = "There are WSDL Message modifications";
    public static final String NEW_PORTTYPES = "There are new WSDL Porttypes ";
    public static final String REMOVE_PORTTYPES = "There are WSDL Porttype removals";
    public static final String NEW_OPERATIONS = "There are new WSDL Operations ";
    public static final String REMOVE_OPERATIONS = "There are WSDL Operation removals";
    public static final String CHANGED_OPERATIONS = "There are WSDL Operation modifications";
    public static final String NEW_BINDING = "There are new WSDL Bindings ";
    public static final String REMOVE_BINDING = "There are WSDL Binding removals";
    public static final String CHANGED_BINDING = "There are WSDL Binding modifications";


}
