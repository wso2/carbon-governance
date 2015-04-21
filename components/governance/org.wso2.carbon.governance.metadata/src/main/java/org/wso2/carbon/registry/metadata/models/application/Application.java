package org.wso2.carbon.registry.metadata.models.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.exception.MetadataException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Application extends Base {

    public static final String APPLICATION_LANGUAGE = "language";
    public static final String APPLICATION_OWNER = "owner";
    public static final String APPLICATION_TYPE = "apptype";

    //  Variables defined for the internal implementation
    private static final Log log = LogFactory.getLog(Application.class);
    private static final String mediaType = "vnd.wso2.application/+xml;version=1";

    public Application(String mediaType, String name, Registry registry) throws MetadataException {
        super(mediaType, name, registry);
    }

    public String getApplicationLanguage() {
        return getSingleValuedAttribute(APPLICATION_LANGUAGE);
    }

    public String getApplicationOwner() {
        return getSingleValuedAttribute(APPLICATION_OWNER);
    }

    public String getApplicationType() {
        return getSingleValuedAttribute(APPLICATION_TYPE);
    }

    public static Application [] find(Registry registry, Map<String, String> criteria) throws MetadataException{
        List<Base> list = find(registry, criteria, mediaType);
        return list.toArray(new Application[list.size()]);
    }

    public static Application get(Registry registry, String uuid) throws MetadataException {
        return (Application) get(registry, uuid, mediaType);
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
