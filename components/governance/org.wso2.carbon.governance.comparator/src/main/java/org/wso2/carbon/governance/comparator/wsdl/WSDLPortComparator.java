/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.comparator.Comparison;
import org.wso2.carbon.governance.comparator.ComparisonException;
import org.wso2.carbon.governance.comparator.common.DefaultComparison;
import org.wso2.carbon.governance.comparator.utils.ComparatorConstants;
import org.wso2.carbon.governance.comparator.utils.WSDLComparisonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

public class WSDLPortComparator extends AbstractWSDLComparator {

    private final Log log = LogFactory.getLog(WSDLPortComparator.class);

    @Override
    public void compareInternal(Definition base, Definition changed, DefaultComparison comparison)
            throws ComparisonException {
        //Here we only compare ports belong to common Services
        comparePorts(base, changed, comparison);
    }

    protected void comparePorts(Definition base, Definition changed, DefaultComparison comparison) {
        DefaultComparison.DefaultSection section = null;
        Set<QName> commonKeys = Sets.intersection(base.getAllServices().keySet(), changed.getAllServices().keySet());
        if (commonKeys.size() > 0) {
            for (QName service : commonKeys) {
                Map<QName, Port> basePorts = base.getService(service).getPorts();
                Map<QName, Port> changedPorts = changed.getService(service).getPorts();
                MapDifference<QName, Port> mapDiff = Maps.difference(basePorts, changedPorts);

                if (!mapDiff.areEqual()) {
                    Map<QName, Port> additions = mapDiff.entriesOnlyOnRight();
                    if (section == null && additions.size() > 0) {
                        section = comparison.newSection();
                    }
                    processAdditions(section, additions, changed);

                    Map<QName, Port> removals = mapDiff.entriesOnlyOnLeft();
                    if (section == null && removals.size() > 0) {
                        section = comparison.newSection();
                    }
                    processRemovals(section, removals, base);

                    Map<QName, MapDifference.ValueDifference<Port>> changes = mapDiff.entriesDiffering();
                    section = processChanges(section, comparison, changes, base, changed);
                }
            }
        }

        if (section != null) {
            comparison.addSection(ComparatorConstants.WSDL_PORTS, section);
        }
    }

    private void processAdditions(DefaultComparison.DefaultSection section, Map<QName, Port> additions,
            Definition definition) {
        if (additions.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_ADDITION, ComparatorConstants.NEW_PORTS);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getPortsOnly(additions.values(), definition));
            section.addContent(Comparison.SectionType.CONTENT_ADDITION, content);
        }
    }

    private void processRemovals(DefaultComparison.DefaultSection section, Map<QName, Port> removals,
            Definition definition) {
        if (removals.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_REMOVAL, ComparatorConstants.REMOVE_PORTS);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getPortsOnly(removals.values(), definition));
            section.addContent(Comparison.SectionType.CONTENT_REMOVAL, content);
        }
    }

    private DefaultComparison.DefaultSection processChanges(DefaultComparison.DefaultSection section,
            DefaultComparison comparison, Map<QName, MapDifference.ValueDifference<Port>> changes, Definition base,
            Definition changed) {
        if (changes.size() > 0) {
            List<Port> left = new ArrayList<>();
            List<Port> right = new ArrayList<>();
            for (MapDifference.ValueDifference<Port> diff : changes.values()) {
                left.add(diff.leftValue());
                right.add(diff.rightValue());
            }
            if (left.size() > 0) {
                String originalPorts = getPortsOnly(left, base);
                String changedPorts = getPortsOnly(right, changed);
                if (!originalPorts.equals(changedPorts)) {
                    if (section == null) {
                        section = comparison.newSection();
                    }
                    section.addSectionSummary(Comparison.SectionType.CONTENT_CHANGE, ComparatorConstants.CHANGED_PORTS);
                    DefaultComparison.DefaultSection.DefaultTextChangeContent content = section.newTextChangeContent();
                    DefaultComparison.DefaultSection.DefaultTextChange change = section.newTextChange();
                    change.setOriginal(originalPorts);
                    change.setChanged(changedPorts);
                    content.setContent(change);
                    section.addContent(Comparison.SectionType.CONTENT_CHANGE, content);
                }
            }
        }
        return section;
    }

    private String getPortsOnly(Collection<Port> ports, Definition definition) {
        try {
            Definition tempDefinition = WSDLComparisonUtils.getWSDLDefinition();
            Service service = tempDefinition.createService();
            service.setQName(new QName(definition.getTargetNamespace(), "temp"));
            for (Port port : ports) {
                service.addPort(port);
            }
            tempDefinition.addService(service);
            WSDLComparisonUtils.copyNamespaces(definition, tempDefinition);
            String content = WSDLComparisonUtils.getWSDLWithoutDeclaration(tempDefinition);
            content = content.substring(content.indexOf(">") + 2);
            return content.replace("</wsdl:service>", "");
        } catch (WSDLException e) {
            log.error(e);
            return "";
        }
    }
}