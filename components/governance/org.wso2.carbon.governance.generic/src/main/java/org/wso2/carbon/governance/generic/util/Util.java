/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.governance.generic.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.File;
import java.io.StringReader;

public class Util {

    private static final Log log = LogFactory.getLog(Util.class);

    private static Validator serviceSchemaValidator = null;
    private static final int ENTITY_EXPANSION_LIMIT = 0;

    //    The methods have been duplicated in several places because there is no common bundle to place them.
//    We have to keep this inside different bundles so that users will not run in to problems if they uninstall some features
    public static boolean validateOMContent(OMElement omContent, Validator validator) {
        try {
            InputStream is = new ByteArrayInputStream(omContent.toString().getBytes("utf-8"));
            Source xmlFile = new StreamSource(is);
            if (validator != null) {
                validator.validate(xmlFile);
            }
        } catch (SAXException e) {
            log.error("Unable to validate the given xml configuration ",e);
            return false;
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported content");
            return false;
        } catch (IOException e) {
            log.error("Unable to validate the given file");
            return false;
        }
        return true;
    }

    public static Validator getSchemaValidator(String schemaPath) {

        if (serviceSchemaValidator == null) {
            try {
                SchemaFactory schemaFactory = SchemaFactory
                        .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new File(schemaPath));
                serviceSchemaValidator = schema.newValidator();
            } catch (SAXException e) {
                log.error("Unable to get a schema validator from the given file path : " + schemaPath);
            }
        }
        return serviceSchemaValidator;
    }

    public static String getServicesSchemaLocation() {
        return CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources"
                + File.separator + "service-ui-config.xsd";
    }

    public static void validateOMContent(OMElement element) throws RegistryException {
        if (!validateOMContent(element, getSchemaValidator(getServicesSchemaLocation()))) {
            String message = "Unable to validate the xml configuration";
            log.error(message);
            throw new RegistryException(message);
        }
    }

    public static OMElement buildOMElement(String payload) throws RegistryException {
        OMElement element = Util.getOMElementFromString(payload);
        element.build();
        return element;
    }

    /**
     * This method is used parse the String XML payload and get the corresponding OMElement.
     *
     * @param payload String XML payload
     * @return OMElement
     * @throws RegistryException If an error occurs while parsing.
     */
    public static OMElement getOMElementFromString(String payload) throws RegistryException {
        try {
            DocumentBuilderFactory dbf = getSecuredDocumentBuilder();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(payload)));
            return XMLUtils.toOM((document).getDocumentElement());
        } catch (Exception e) {
            String message = "Unable to parse the XML configuration. Please validate the XML configuration";
            log.error(message, e);
            throw new RegistryException(message, e);
        }
    }

    public static DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (ParserConfigurationException e) {
            log.error(
                    "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or "
                            + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or "
                            + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);
        return dbf;
    }

}
