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

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.comparator.Comparison;
import org.wso2.carbon.governance.comparator.ComparisonException;
import org.wso2.carbon.governance.comparator.common.DefaultComparison;
import org.wso2.carbon.governance.comparator.utils.ComparatorConstants;
import org.wso2.carbon.governance.comparator.utils.WSDLComparisonUtils;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Map;

public class WSDLBindingsComparator extends AbstractWSDLComparator {

    private Log log = LogFactory.getLog(WSDLImportsComparator.class);

    @Override
    public void compareInternal(Definition base, Definition changed, DefaultComparison comparison)
            throws ComparisonException {
        compareBindings(base, changed, comparison);
    }

    protected void compareBindings(Definition base, Definition changed,
                                   DefaultComparison comparison) {

        Map<QName, Binding> baseBinding = base.getAllBindings();
        Map<QName, Binding> changedBinding = changed.getAllBindings();
        DefaultComparison.DefaultSection section = null;
        MapDifference<QName, Binding> mapDiff = Maps.difference(baseBinding, changedBinding);

        //If both side imports are equal, return
        if (mapDiff.areEqual()) {
            return;
        }

        Map<QName, Binding> additions = mapDiff.entriesOnlyOnRight();
        if (section == null && additions.size() > 0) {
            section = comparison.newSection();
        }
        processAdditions(section, additions, changed);

        Map<QName, Binding> removals = mapDiff.entriesOnlyOnLeft();
        if (section == null && removals.size() > 0) {
            section = comparison.newSection();
        }
        processRemovals(section, removals, changed);

        Map<QName, MapDifference.ValueDifference<Binding>> changes = mapDiff.entriesDiffering();
        if (section == null && changes.size() > 0) {
            section = comparison.newSection();
        }
        processChanges(section, additions, changes);

        if (section != null) {
            comparison.addSection(ComparatorConstants.WSDL_IMPORTS, section);
        }
    }


    private void processAdditions(DefaultComparison.DefaultSection section, Map<QName, Binding> additions,
                                  Definition definition) {
        if (additions.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_ADDITION, ComparatorConstants.NEW_BINDING);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getBindingsOnly(additions.values(), definition));
            section.addContent(Comparison.SectionType.CONTENT_ADDITION, content);
        }
    }


    private void processRemovals(DefaultComparison.DefaultSection section, Map<QName, Binding> removals,
                                 Definition definition) {
        if (removals.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_REMOVAL, ComparatorConstants.REMOVE_BINDING);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getBindingsOnly(removals.values(), definition));
            section.addContent(Comparison.SectionType.CONTENT_REMOVAL, content);
        }
    }

    private void processChanges(DefaultComparison.DefaultSection comparison, Map<QName, Binding> section,
                                Map<QName, MapDifference.ValueDifference<Binding>> changes) {
        if (changes.size() > 0) {
//            section.addSectionSummary(Comparison.SectionType.CONTENT_CHANGE, ComparatorConstants.CHANGED_IMPORTS);
//            DefaultComparison.DefaultSection.DefaultTextChangeContent content = section.newTextChangeContent();
//            DefaultComparison.DefaultSection.DefaultTextChange change = section.newTextChange();
//            Vector<Import> left = new Vector<>();
//            Vector<Import> right = new Vector<>();
//            for(MapDifference.ValueDifference<Binding> diff : changes.values()){
//                left.add(diff.leftValue());
//                right.add(diff.rightValue().firstElement());
//            }
//            change.setOriginal(getBindingsOnly(left));
//            change.setChanged(getBindingsOnly(right));
//            content.setContent(change);
//            section.addContent(Comparison.SectionType.CONTENT_CHANGE, content);
        }

    }

    private String getBindingsOnly(Collection<Binding> bindings, Definition definition) {
        try {
            Definition tempDefinition = WSDLComparisonUtils.getWSDLDefinition();
            for (Binding binding : bindings) {
                tempDefinition.addBinding(binding);
            }
            WSDLComparisonUtils.copyNamespaces(definition, tempDefinition);
            return WSDLComparisonUtils.getWSDLWithoutDeclaration(tempDefinition);
        } catch (WSDLException e) {
            log.error(e);
        }
        return null;

    }
}
