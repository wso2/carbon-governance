package org.wso2.carbon.governance.generic.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;

public class Util {

    private static final Log log = LogFactory.getLog(Util.class);

    private static Validator serviceSchemaValidator = null;

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

    public static Validator getSchemaValidator(String schemaPath){

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

    public static String getServicesSchemaLocation(){
        return CarbonUtils.getCarbonHome() + File.separator + "repository"+File.separator +"resources"+ File.separator +
                "service-ui-config.xsd";
    }

    public static void validateOMContent(OMElement element) throws RegistryException {
        if(!validateOMContent(element,getSchemaValidator(getServicesSchemaLocation()))){
            String message = "Unable to validate the xml configuration";
            log.error(message);
            throw new RegistryException(message);
        }
    }

    public static OMElement buildOMElement(String payload) throws RegistryException {
        OMElement element;
        try {
            element = AXIOMUtil.stringToOM(payload);
            element.build();
        } catch (Exception e) {
            String message = "Unable to parse the XML configuration. Please validate the XML configuration";
            log.error(message,e);
            throw new RegistryException(message,e);
        }
        return element;
    }

}
