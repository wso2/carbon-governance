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

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.comparator.Comparison;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WSDLComparisonUtils {

    private static final Log log = LogFactory.getLog(WSDLComparisonUtils.class);

    public static Definition getWSDLDefinition() throws WSDLException {
        WSDLFactory factory = WSDLFactory.newInstance();
        return factory.newDefinition();
    }

    public static String getWSDLDeclarationOnly(Definition definition) {
        try {
            Definition declarationOnly = getWSDLDefinition();
            declarationOnly.setDocumentationElement(definition.getDocumentationElement());
            declarationOnly.setTargetNamespace(definition.getTargetNamespace());
            declarationOnly.setDocumentBaseURI(definition.getDocumentBaseURI());
            declarationOnly.setQName(definition.getQName());
            if (definition.getNamespaces() != null) {
                Set<Map.Entry<String, String>> entries = definition.getNamespaces().entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    declarationOnly.addNamespace(entry.getKey(), entry.getValue());
                }
            }
            StringWriter writer = new StringWriter();
            serialize(declarationOnly, writer);
            return writer.toString();
        } catch (WSDLException e) {
            log.error(e);
        }
        return null;
    }

    public static String getWSDLWithoutDeclaration(Definition definition) {
        try {
            StringWriter writer = new StringWriter();
            serialize(definition, writer);
            String content = writer.toString();
            content = content.substring(content.indexOf(ComparatorConstants.WSDL_DECLARATION_START_ELEMENT));
            content = content.substring(content.indexOf(">"));
            content = content.replace(ComparatorConstants.WSDL_DECLARATION_START, "");
            content = content.replace(ComparatorConstants.WSDL_DECLARATION_END_ELEMENT, "");
            return content;
        } catch (WSDLException e) {
            log.error(e);
        }
        return null;
    }

    public static void serialize(Definition definition, OutputStream stream) throws WSDLException {
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLWriter writer = factory.newWSDLWriter();
        writer.writeWSDL(definition, stream);
    }

    public static void serialize(Definition definition, Writer writer) throws WSDLException {
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLWriter wsdlWriter = factory.newWSDLWriter();
        wsdlWriter.writeWSDL(definition, writer);
    }

    public static void copyNamespaces(Definition definition, Definition tempDefinition) {
        Map<String, String> namespaces = definition.getNamespaces();
        for (Map.Entry<String, String> ns : namespaces.entrySet()) {
            tempDefinition.addNamespace(ns.getKey(), ns.getValue());
        }
    }

    public static boolean isDiffrentMessages(Message left, Message right) {
        if (left.getQName().equals(right.getQName())) {
            Map<String, Part> leftParts = left.getParts();
            Map<String, Part> rightParts = right.getParts();
            MapDifference mapDiff = Maps.difference(leftParts, rightParts);
            if (mapDiff.areEqual()) {
                return false;
            } else {
                Map<String, MapDifference.ValueDifference<Part>> difference = mapDiff.entriesDiffering();
                for (MapDifference.ValueDifference<Part> diff : difference.values()) {
                    if (isDiffrentParts(diff.leftValue(), diff.rightValue())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isDiffrentParts(Part left, Part right) {
        if (left.getName().equals(right.getName())) {
            if (left.getElementName() != null && right.getElementName() != null) {
                if (!left.getElementName().equals(right.getElementName())) {
                    return true;
                }
            }
            if (left.getTypeName() != null && right.getTypeName() != null) {
                if (!left.getTypeName().equals(right.getTypeName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void print(Comparison comparison) {
        log.info("************************* Comparison Start ******************");
        for (Map.Entry<String, Comparison.Section> entry : comparison.getSections().entrySet()) {
            String key = entry.getKey();
            Comparison.Section section = entry.getValue();
            log.info("Section : " + key);
            for (Map.Entry<Comparison.SectionType, List<String>> summaryEntry : section.getSummary().entrySet()) {
                log.info(summaryEntry.getKey() + " : " + summaryEntry.getValue());
            }
            for (Map.Entry<Comparison.SectionType, List<Comparison.Section.Content>> contentEntry : section.getContent().entrySet()) {
                log.info(contentEntry.getKey() + " : " + contentEntry.getValue());
            }

        }
        log.info("************************* Comparison End ******************");
    }
}
