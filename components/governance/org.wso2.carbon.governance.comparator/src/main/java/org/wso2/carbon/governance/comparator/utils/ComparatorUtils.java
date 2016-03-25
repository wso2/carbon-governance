/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.governance.comparator.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.comparator.Comparison;
import org.wso2.carbon.governance.comparator.ComparisonException;
import org.wso2.carbon.governance.comparator.DiffGenerator;
import org.wso2.carbon.governance.comparator.DiffGeneratorFactory;
import org.wso2.carbon.governance.comparator.GovernanceDiffGeneratorFactory;
import org.wso2.carbon.governance.comparator.TextDiffGeneratorFactory;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

/**
 * This Util class includes Comparator util methods.
 */
public class ComparatorUtils extends RegistryAbstractAdmin {

    private static final Log log = LogFactory.getLog(ComparatorUtils.class);

    /**
     * This method is used to get the text difference of two strings.
     *
     * @param resourcePathOne   resource path one.
     * @param resourcePathTwo   resource path two.
     * @return                  Comparison object which includes the difference parameters.
     * @throws ComparisonException
     * @throws WSDLException
     * @throws RegistryException
     * @throws UnsupportedEncodingException
     */
    public Comparison getArtifactTextDiff(String resourcePathOne, String resourcePathTwo)
            throws ComparisonException, WSDLException, RegistryException, UnsupportedEncodingException {

        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Registry registry = RegistryCoreServiceComponent.getRegistryService().getRegistry(username, tenantId);
        Resource resourceOne = registry.get(resourcePathOne);
        Resource resourceTwo = registry.get(resourcePathTwo);

        DiffGeneratorFactory factory = new TextDiffGeneratorFactory();
        DiffGenerator flow = factory.getDiffGenerator();

        String resourceOneText = new String((byte[]) resourceOne.getContent(), "UTF-8");
        String resourceTwoText = new String((byte[]) resourceTwo.getContent(), "UTF-8");

        String resourceOneFormattedText = prettyFormatText(resourceOneText, resourceOne.getMediaType());
        String resourceTwoFormattedText = prettyFormatText(resourceTwoText, resourceTwo.getMediaType());
        return flow.compare(resourceOneFormattedText, resourceTwoFormattedText, ComparatorConstants
                .TEXT_PLAIN_MEDIA_TYPE);
    }

    /**
     * This method is used to get a details difference of two resource while considering the media type.
     *
     * @param resourcePathOne   resource path one
     * @param resourcePathTwo   resource path two
     * @param mediaType         media type
     * @return                  Comparison object which includes the difference parameters.
     * @throws ComparisonException
     * @throws WSDLException
     * @throws RegistryException
     * @throws UnsupportedEncodingException
     */
    public Comparison getArtifactDetailDiff(String resourcePathOne, String resourcePathTwo, String mediaType)
            throws ComparisonException, WSDLException, RegistryException, UnsupportedEncodingException {

        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Registry registry = RegistryCoreServiceComponent.getRegistryService().getRegistry(username, tenantId);
        Resource resourceOne = registry.get(resourcePathOne);
        Resource resourceTwo = registry.get(resourcePathTwo);

        switch (mediaType) {
        case ComparatorConstants.WSDL_MEDIA_TYPE:
            return getWSDLComparison(resourceOne, resourceTwo);
        default:
            return null;
        }
    }

    /**
     * This method is used to get wsdl difference comparison.
     *
     * @param WSDLOne   wsdl one.
     * @param WSDLTwo   wsdl two.
     * @return          Comparison object which includes the difference parameters.
     * @throws ComparisonException
     * @throws WSDLException
     * @throws RegistryException
     * @throws UnsupportedEncodingException
     */
    private Comparison getWSDLComparison(Resource WSDLOne, Resource WSDLTwo)
            throws ComparisonException, WSDLException, RegistryException, UnsupportedEncodingException {
        GovernanceDiffGeneratorFactory diffGeneratorFactory = new GovernanceDiffGeneratorFactory();
        DiffGenerator flow = diffGeneratorFactory.getDiffGenerator();
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();

        InputSource inputSourceOne = new InputSource(new ByteArrayInputStream((byte[]) WSDLOne.getContent()));
        Definition originalWSDL = wsdlReader.readWSDL(null, inputSourceOne);

        InputSource inputSourceTwo = new InputSource(new ByteArrayInputStream((byte[]) WSDLTwo.getContent()));
        Definition changedWSDL = wsdlReader.readWSDL(null, inputSourceTwo);

        return flow.compare(originalWSDL, changedWSDL, ComparatorConstants.WSDL_MEDIA_TYPE);
    }

    /**
     * This method is used to format Texts.
     *
     * @param input                 input String
     * @param mediaType             input resource mediaType
     * @return                      formatted text
     * @throws ComparisonException  Exception will occur if an error happens when formatting the text.
     */
    public String prettyFormatText(String input, String mediaType) throws ComparisonException {
        if (mediaType.contains(ComparatorConstants.XML)) {
            return prettyFormatXML(input);
        } else if (mediaType.contains(ComparatorConstants.JSON)) {
            return prettyFormatJSON(input);
        } else {
            return input;
        }
    }

    /**
     * This method is used to format XML Strings
     * @param input                 input xml String
     * @return                      formatted xml String
     */
    private String prettyFormatXML(String input) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new ByteArrayInputStream(input.getBytes(ComparatorConstants.UTF_8))));

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xPath
                    .evaluate("//text()[normalize-space()='']", document, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, ComparatorConstants.UTF_8);
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, ComparatorConstants.YES);
            transformer.setOutputProperty(OutputKeys.INDENT, ComparatorConstants.YES);
            transformer.setOutputProperty(ComparatorConstants.XML_INDENT_AMOUNT, ComparatorConstants.TWO);

            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);

            transformer.transform(new DOMSource(document), streamResult);
            return stringWriter.toString();
        } catch (TransformerException | SAXException | ParserConfigurationException | XPathExpressionException |
                IOException e) {
            log.warn("Error occurred while formatting the xml content.", e);
            return input;
        }
    }

    /**
     * This method is used to format JSON Strings.
     *
     * @param input                 JSON Strong
     * @return                      Formatted JSON Strong
     */
    private String prettyFormatJSON(String input) {
        JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = parser.parse(input);
        return gson.toJson(jsonElement);
    }
}
