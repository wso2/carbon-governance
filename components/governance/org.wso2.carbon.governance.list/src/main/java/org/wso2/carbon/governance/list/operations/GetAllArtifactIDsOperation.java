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
