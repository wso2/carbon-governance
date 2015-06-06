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

package org.wso2.carbon.governance.comparator.wsdl;

import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.comparator.Comparison;
import org.wso2.carbon.governance.comparator.ComparisonException;
import org.wso2.carbon.governance.comparator.common.DefaultComparison;
import org.wso2.carbon.governance.comparator.utils.ComparatorConstants;
import org.wso2.carbon.governance.comparator.utils.WSDLComparisonUtils;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.util.Set;

public class WSDLPortTypeComparator extends AbstractWSDLComparator {

    private final Log log = LogFactory.getLog(WSDLImportsComparator.class);

    @Override
    void compareInternal(Definition base, Definition changed, DefaultComparison comparison) throws ComparisonException {
        comparePortTypes(base, changed, comparison);
    }

    protected void comparePortTypes(Definition base, Definition changed, DefaultComparison comparison) {
        //Here we only compare additions and removals only.
        DefaultComparison.DefaultSection section = null;

        Set<QName> basePortTypeNames = base.getPortTypes().keySet();
        Set<QName> ChangedPortTypeNames = changed.getPortTypes().keySet();

        Set<QName> additionKeys = Sets.difference(ChangedPortTypeNames, basePortTypeNames);
        if (section == null && additionKeys.size() > 0) {
            section = comparison.newSection();
        }
        processAdditions(section, additionKeys, changed);

        Set<QName> removalKeys = Sets.difference(basePortTypeNames, ChangedPortTypeNames);
        if (section == null && removalKeys.size() > 0) {
            section = comparison.newSection();
        }
        processRemovals(section, removalKeys, base);

        if (section != null) {
            comparison.addSection(ComparatorConstants.WSDL_IMPORTS, section);
        }


    }

    protected void processAdditions(DefaultComparison.DefaultSection section,
                                    Set<QName> additionKeys, Definition changed) {
        if (additionKeys.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_ADDITION, ComparatorConstants.NEW_PORTTYPES);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getPortTypesOnly(additionKeys, changed));
            section.addContent(Comparison.SectionType.CONTENT_ADDITION, content);
        }

    }

    protected void processRemovals(DefaultComparison.DefaultSection section,
                                   Set<QName> removalKeys, Definition base) {
        if (removalKeys.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_REMOVAL, ComparatorConstants.REMOVE_PORTTYPES);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getPortTypesOnly(removalKeys, base));
            section.addContent(Comparison.SectionType.CONTENT_REMOVAL, content);
        }

    }

    private String getPortTypesOnly(Set<QName> names, Definition definition) {
        try {
            Definition tempDefinition = WSDLComparisonUtils.getWSDLDefinition();
            for (QName name : names) {
                tempDefinition.addPortType(definition.getPortType(name));
            }
            WSDLComparisonUtils.copyNamespaces(definition, tempDefinition);
            return WSDLComparisonUtils.getWSDLWithoutDeclaration(tempDefinition);
        } catch (WSDLException e) {
            log.error(e);
        }
        return null;
    }
}
