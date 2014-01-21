/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.list.operations;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.list.operations.util.OperationUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.List;

public class ReadOperation extends AbstractOperation{
    private Log log = LogFactory.getLog(ReadOperation.class);
    private String content;

    public ReadOperation(QName name, Registry governanceSystemRegistry, String mediatype, String namespace) {
        super(name, governanceSystemRegistry, mediatype, namespace);
    }

    @Override
    public void setPayload(OMElement bodyContent) throws XMLStreamException {
        OMFactory factory = bodyContent.getOMFactory();
        OMElement returnElement = factory.createOMElement(new QName(bodyContent.getNamespace().getPrefix() + ":return"));
        returnElement.setText(content);
        bodyContent.addChild(returnElement);
    }

    @Override
    public String getRequestName() {
        return "artifactId";
    }

    @Override
    public String getRequestType() {
        return "xs:string";
    }

    @Override
    public String getResponseType() {
        return "xs:string";
    }

    @Override
    public String getResponseMaxOccurs() {
        return "1";
    }

    public MessageContext process(MessageContext requestMessageContext) throws AxisFault {
        String artifactId = null;
        AXIOMXPath expression;
        try {
            String operation = requestMessageContext.
                    getOperationContext().getAxisOperation().getName().getLocalPart();
            expression = new AXIOMXPath("//ns:" + operation + "/ns:artifactId");
            expression.addNamespace("ns", namespace);

            List elements = expression.selectNodes(requestMessageContext.getEnvelope().getBody());
            if(elements.isEmpty()){
                String msg = "Content of the resource should be in correct format";
                log.error(msg);
                OperationUtil.handleException(msg);
            }
            artifactId = ((OMElement)elements.get(0)).getText().trim();
        } catch (JaxenException e) {
            String msg = "Error occured while reading the content of the SOAP message";
            log.error(msg);
            OperationUtil.handleException(msg, e);
        }

        try {
            GenericArtifactManager artifactManager = new GenericArtifactManager(governanceSystemRegistry, rxtKey);
            GenericArtifact artifact;
            if((artifact = artifactManager.getGenericArtifact(artifactId)) != null){
                Resource resource =  governanceSystemRegistry.get(artifact.getPath());
                content = RegistryUtils.decodeBytes((byte [])resource.getContent());
            } else {
                String msg = "Artifact not found for the artifact id " + artifactId;
                log.error(msg);
                OperationUtil.handleException(msg);
            }

        } catch (RegistryException e) {
            String msg = e.getMessage();
            log.error(msg);
            OperationUtil.handleException(msg, e);
        }

        return getAbstractResponseMessageContext(requestMessageContext);
    }
}
