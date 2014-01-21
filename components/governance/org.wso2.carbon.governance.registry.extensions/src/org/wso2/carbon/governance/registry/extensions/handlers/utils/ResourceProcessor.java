package org.wso2.carbon.governance.registry.extensions.handlers.utils;

import org.apache.axiom.om.OMElement;
import org.jaxen.JaxenException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class ResourceProcessor {
    private Registry registry;
    private String wadlPath;

    public ResourceProcessor(Registry registry, String wadlPath){
        this.registry = registry;
        this.wadlPath = wadlPath;
    }

    public void saveResources(OMElement resourcesElement, String basePath)
            throws JaxenException, RegistryException {
        Iterator<OMElement> resources = resourcesElement.getChildrenWithLocalName("resource");

        while (resources.hasNext()){
            String resourcePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                    addResource(resources.next(), basePath);
            if (resourcePath != null){
                addDependency(wadlPath, resourcePath);
            }
        }
    }

    public String addResource(OMElement resourceElement, String basePath)
            throws JaxenException, RegistryException {
        String path;
        if((path = resourceElement.getAttribute(new QName("path")).
                getAttributeValue()).startsWith("/")){
            path = path.substring(1);
        }

        Registry governanceSystemRegistry =
                RegistryCoreServiceComponent.getRegistryService().getGovernanceSystemRegistry();
        GenericArtifactManager genericArtifactManager =
                new GenericArtifactManager(governanceSystemRegistry, "wadlresource");

        if(path.matches("^\\{([^~!@#;%^*+=\\{\\}\\(\\)\\|\\\\<>;,]+)\\}$")) {
            path = path.replaceAll("\\{(.*)\\}$", "$1");
            path = "exp-" + path;
        } else if(!path.matches("[^~!@#;%^*+=\\{\\}\\(\\)\\|\\\\<>;,]+")) {
            throw new RegistryException
                    ("Invalid resource: " + path);
        }

        GenericArtifact resourceArtifact = genericArtifactManager.newGovernanceArtifact(
                new QName(path));
        resourceArtifact.addAttribute("overview_name", path);
        resourceArtifact.addAttribute("overview_base", basePath);

        Iterator<OMElement> methods = resourceElement.getChildrenWithLocalName("method");
        while(methods.hasNext()){
            OMElement method = methods.next();
            if(method.getAttribute(new QName("name"))!=null &&
                    method.getAttribute(new QName("id"))!=null){
                resourceArtifact.addAttribute("methods_entry",
                        method.getAttribute(new QName("name")).getAttributeValue() + ":" +
                                method.getAttribute(new QName("id")).getAttributeValue());

            }

        }
        genericArtifactManager.addGenericArtifact(resourceArtifact);

        saveResources(resourceElement, basePath + path);
        return resourceArtifact.getPath();
    }

    private void addDependency(String source, String target) throws RegistryException {
        registry.addAssociation(source, target, CommonConstants.DEPENDS);
        registry.addAssociation(target, source, CommonConstants.USED_BY);
    }
}
