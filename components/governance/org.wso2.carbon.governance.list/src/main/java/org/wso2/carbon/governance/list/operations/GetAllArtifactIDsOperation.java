package org.wso2.carbon.governance.list.operations;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class GetAllArtifactIDsOperation extends AbstractOperation{
    private Log log = LogFactory.getLog(GetAllArtifactIDsOperation.class);
    private String[] artifactIDs;

    public GetAllArtifactIDsOperation(QName name, Registry governanceSystemRegistry, String mediatype, String namespace) {
        super(name, governanceSystemRegistry, mediatype, namespace);
    }

    @Override
    public void setPayload(OMElement bodyContent) throws XMLStreamException {
        OMFactory factory = bodyContent.getOMFactory();
        for(String artifactId : artifactIDs){
            OMElement returnElement = factory.createOMElement(new QName(bodyContent.getNamespace().getPrefix() + ":return"));
            returnElement.setText(artifactId);
            bodyContent.addChild(returnElement);
        }
    }

    @Override
    public String getRequestName() {
        return "";
    }

    @Override
    public String getRequestType() {
        return "";
    }

    @Override
    public String getResponseType() {
        return "xs:string";
    }

    @Override
    public String getResponseMaxOccurs() {
        return "unbounded";
    }

    public MessageContext process(MessageContext requestMessageContext) throws AxisFault {
        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(governanceSystemRegistry, rxtKey);
            artifactIDs = artifactManager.getAllGenericArtifactIds();
        } catch (RegistryException e) {
            String msg = e.getMessage();
            log.error(msg);
            throw new AxisFault(msg, e);
        }

        return getAbstractResponseMessageContext(requestMessageContext);
    }
}
