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
import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

public class WSDLServicesComparator extends AbstractWSDLComparator {

    private final Log log = LogFactory.getLog(WSDLServicesComparator.class);

    @Override
    public void compareInternal(Definition base, Definition changed, DefaultComparison comparison)
            throws ComparisonException {
        compareServices(base, changed, comparison);
    }

    protected void compareServices(Definition base, Definition changed, DefaultComparison comparison) {
        DefaultComparison.DefaultSection section = null;
        Map<QName, Service> baseService = base.getAllServices();
        Map<QName, Service> changedService = changed.getAllServices();
        MapDifference<QName, Service> mapDiff = Maps.difference(baseService, changedService);

        //If both side services are equal, return
        if (mapDiff.areEqual()) {
            return;
        }

        Map<QName, Service> additions = mapDiff.entriesOnlyOnRight();
        if (section == null && additions.size() > 0) {
            section = comparison.newSection();
        }
        processAdditions(section, additions, changed);

        Map<QName, Service> removals = mapDiff.entriesOnlyOnLeft();
        if (section == null && removals.size() > 0) {
            section = comparison.newSection();
        }
        processRemovals(section, removals, base);

        Map<QName, MapDifference.ValueDifference<Service>> changes = mapDiff.entriesDiffering();
        section = processChanges(section, comparison, changes, base, changed);

        if (section != null) {
            comparison.addSection(ComparatorConstants.WSDL_SERVICE, section);
        }
    }

    private void processAdditions(DefaultComparison.DefaultSection section, Map<QName, Service> additions,
            Definition definition) {
        if (additions.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_ADDITION, ComparatorConstants.NEW_SERVICE);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getServiceOnly(additions.values(), definition));
            section.addContent(Comparison.SectionType.CONTENT_ADDITION, content);
        }
    }

    private void processRemovals(DefaultComparison.DefaultSection section, Map<QName, Service> removals,
            Definition definition) {
        if (removals.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_REMOVAL, ComparatorConstants.REMOVE_SERVICE);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getServiceOnly(removals.values(), definition));
            section.addContent(Comparison.SectionType.CONTENT_REMOVAL, content);
        }
    }

    private DefaultComparison.DefaultSection processChanges(DefaultComparison.DefaultSection section,
            DefaultComparison comparison, Map<QName, MapDifference.ValueDifference<Service>> changes, Definition base,
            Definition changed) {
        if (changes.size() > 0) {
            List<Service> left = new ArrayList<>();
            List<Service> right = new ArrayList<>();
            for (MapDifference.ValueDifference<Service> diff : changes.values()) {
                left.add(diff.leftValue());
                right.add(diff.rightValue());
            }
            if (left.size() > 0) {
                String originalServices = getServiceOnly(left, base);
                String changedServices = getServiceOnly(right, changed);
                if (!originalServices.equals(changedServices)) {
                    if (section == null) {
                        section = comparison.newSection();
                    }
                    section.addSectionSummary(Comparison.SectionType.CONTENT_CHANGE,
                            ComparatorConstants.CHANGED_SERVICE);
                    DefaultComparison.DefaultSection.DefaultTextChangeContent content = section.newTextChangeContent();
                    DefaultComparison.DefaultSection.DefaultTextChange change = section.newTextChange();
                    change.setOriginal(originalServices);
                    change.setChanged(changedServices);
                    content.setContent(change);
                    section.addContent(Comparison.SectionType.CONTENT_CHANGE, content);
                }
            }
        }
        return section;
    }

    private String getServiceOnly(Collection<Service> services, Definition definition) {
        try {
            Definition tempDefinition = WSDLComparisonUtils.getWSDLDefinition();
            for (Service service : services) {
                tempDefinition.addService(service);
            }
            WSDLComparisonUtils.copyNamespaces(definition, tempDefinition);
            return WSDLComparisonUtils.getWSDLWithoutDeclaration(tempDefinition);
        } catch (WSDLException e) {
            log.error(e);
            return "";
        }
    }
}
