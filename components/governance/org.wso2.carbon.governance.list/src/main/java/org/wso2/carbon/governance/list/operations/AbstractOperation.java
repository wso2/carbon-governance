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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.wso2.carbon.governance.list.operations.util.OperationsConstants;
import org.wso2.carbon.registry.core.Registry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractOperation extends InOutAxisOperation implements MessageProcessor {
    private Log log = LogFactory.getLog(AbstractOperation.class);
    protected String rxtKey;
    protected Registry governanceSystemRegistry;
    protected String name;
    protected String mediaType;
    protected String namespace;
    protected String singlularName;

    protected AbstractOperation(QName name, Registry governanceSystemRegistry, String mediaType, String namespace) {
        super(name);
        this.governanceSystemRegistry = governanceSystemRegistry;
        this.name = name.getLocalPart();
        this.mediaType = mediaType;
        this.namespace = namespace;
    }

    public AbstractOperation init(String rxtKey, RXTMessageReceiver receiver) {
        this.rxtKey = rxtKey;
        receiver.setMessageProcessor(name, this);
        setMessageReceiver(receiver);
        AxisMessage in = getMessage(OperationsConstants.IN);
        in.setName(name + OperationsConstants.REQUEST);
        in.setElementQName(new QName(namespace, name));
        AxisMessage out = getMessage(OperationsConstants.OUT);
        out.setName(name + OperationsConstants.RESPONSE);
        out.setElementQName(new QName(namespace, name + OperationsConstants.RESPONSE));
        AxisMessage fault = new AxisMessage();
        fault.setName(name + "ServiceGovernanceException");
        fault.setElementQName(new QName(namespace, name + "ServiceGovernanceException"));
        setFaultMessages(fault);
        return this;
    }

    public XmlSchema[] getSchemas(XmlSchemaCollection collection) {
        String str = getCustomSchema();
        return Arrays.asList(collection.read(new StreamSource(new ByteArrayInputStream(str.getBytes())), null)).toArray(
                new XmlSchema[1]);
    }

    public MessageContext getAbstractResponseMessageContext(MessageContext requestMessageContext) throws AxisFault {
        MessageContext outMessageCtx = MessageContextBuilder.createOutMessageContext(requestMessageContext);

        SOAPFactory factory = getSOAPFactory(requestMessageContext);
        AxisOperation operation = requestMessageContext.getOperationContext().getAxisOperation();
        AxisService service = requestMessageContext.getAxisService();

        OMElement bodyContent;
        AxisMessage outMessage = operation.getMessage(OperationsConstants.OUT);

        bodyContent = factory.createOMElement(outMessage.getName(),
                factory.createOMNamespace(namespace,
                        service.getSchemaTargetNamespacePrefix()));
        try {
            setPayload(bodyContent);
        } catch (XMLStreamException e) {
            String msg = "Error in adding the payload to the response message";
            log.error(msg);
            throw new AxisFault(msg, e);
        }

        SOAPEnvelope soapEnvelope = factory.getDefaultEnvelope();
        soapEnvelope.getBody().addChild(bodyContent);
        outMessageCtx.setEnvelope(soapEnvelope);
        return outMessageCtx;
    }

    public SOAPFactory getSOAPFactory(MessageContext msgContext) throws AxisFault {
        String nsURI = msgContext.getEnvelope().getNamespace().getNamespaceURI();
        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP11Factory();
        } else {
            throw new AxisFault(Messages.getMessage("invalidSOAPversion"));
        }
    }

    private String getCustomSchema() {
        OMElement omElement = null;
        try {
            omElement = AXIOMUtil.stringToOM(OperationsConstants.CUSTOM_XSD);
            omElement.addAttribute("targetNamespace", namespace, null);

            AXIOMXPath expression = new AXIOMXPath("xs:element");
            expression.addNamespace("xs", OperationsConstants.XSD_NAMESPACE);
            List<OMElement> elements = expression.selectNodes(omElement);

            OMElement element1 = elements.get(0);
            element1.addAttribute("name", name + "ServiceGovernanceException", null);


            OMElement element2 = elements.get(1);
            element2.addAttribute("name", name, null);

            String requestType;
            expression = new AXIOMXPath("xs:complexType/xs:sequence/xs:element");
            expression.addNamespace("xs", OperationsConstants.XSD_NAMESPACE);
            OMElement ep2 = (OMElement)expression.selectNodes(element2).get(0);
            if(!(requestType = getRequestType()).equals("")) {
                ep2.addAttribute("name", getRequestName(), null);
                ep2.addAttribute("type", requestType, null);
            } else {
                ep2.detach();
            }


            OMElement element3 = elements.get(2);
            element3.addAttribute("name", name + "Response", null);
            expression = new AXIOMXPath("xs:complexType/xs:sequence/xs:element");
            expression.addNamespace("xs", OperationsConstants.XSD_NAMESPACE);
            OMElement ep3 = (OMElement)expression.selectNodes(element3).get(0);
            ep3.addAttribute("type", getResponseType(), null);
            ep3.addAttribute("maxOccurs", getResponseMaxOccurs(), null);
        } catch (Exception e) {
            //Should not throw outside
            log.error("Error while creating the custom Schema");
        }

        return omElement.toString();
    }

    public abstract void setPayload(OMElement bodyContent) throws XMLStreamException;

    public abstract String getRequestName();

    public abstract String getRequestType();

    public abstract String getResponseType();

    public abstract String getResponseMaxOccurs();
}
