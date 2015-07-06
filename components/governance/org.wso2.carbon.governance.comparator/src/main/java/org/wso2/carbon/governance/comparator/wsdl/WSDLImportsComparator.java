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

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.WSDLException;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

public class WSDLImportsComparator extends AbstractWSDLComparator {

    private final Log log = LogFactory.getLog(WSDLImportsComparator.class);

    @Override
    public void init() {

    }

    @Override
    public void compareInternal(Definition base, Definition changed, DefaultComparison comparison)
            throws ComparisonException {
        compareImports(base.getImports(), changed.getImports(), comparison);
    }


    protected void compareImports(Map<String, Vector<Import>> base, Map<String, Vector<Import>> changed,
                                  DefaultComparison comparison) {
        DefaultComparison.DefaultSection section = null;
        MapDifference<String, Vector<Import>> mapDiff = Maps.difference(base, changed);

        //If both side imports are equal, return
        if (mapDiff.areEqual()) {
            return;
        }

        Map<String, Vector<Import>> additions = mapDiff.entriesOnlyOnRight();
        if (section == null && additions.size() > 0) {
            section = comparison.newSection();
        }
        processAdditions(comparison, section, additions);

        Map<String, Vector<Import>> removals = mapDiff.entriesOnlyOnLeft();
        if (section == null && removals.size() > 0) {
            section = comparison.newSection();
        }
        processRemovals(comparison, section, removals);

        Map<String, MapDifference.ValueDifference<Vector<Import>>> changes = mapDiff.entriesDiffering();
        if (section == null && changes.size() > 0) {
            section = comparison.newSection();
        }
        processChanges(comparison, section, changes);


        if (section != null) {
            comparison.addSection(ComparatorConstants.WSDL_IMPORTS, section);
        }

    }

    private void processChanges(DefaultComparison comparison, DefaultComparison.DefaultSection section,
                                Map<String, MapDifference.ValueDifference<Vector<Import>>> changes) {
        if (changes.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_CHANGE, ComparatorConstants.CHANGED_IMPORTS);
            DefaultComparison.DefaultSection.DefaultTextChangeContent content = section.newTextChangeContent();
            DefaultComparison.DefaultSection.DefaultTextChange change = section.newTextChange();
            Vector<Import> left = new Vector<>();
            Vector<Import> right = new Vector<>();
            for (MapDifference.ValueDifference<Vector<Import>> diff : changes.values()) {
                left.add(diff.leftValue().firstElement());
                right.add(diff.rightValue().firstElement());
            }
            change.setOriginal(getImportsOnly(left));
            change.setChanged(getImportsOnly(right));
            content.setContent(change);
            section.addContent(Comparison.SectionType.CONTENT_CHANGE, content);
        }

    }

    private void processAdditions(DefaultComparison comparison, DefaultComparison.DefaultSection section,
                                  Map<String, Vector<Import>> additions) {
        if (additions.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_ADDITION, ComparatorConstants.NEW_IMPORTS);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getImportsOnly(additions));
            section.addContent(Comparison.SectionType.CONTENT_ADDITION, content);
        }
    }

    private void processRemovals(DefaultComparison comparison, DefaultComparison.DefaultSection section,
                                 Map<String, Vector<Import>> removals) {
        if (removals.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_REMOVAL, ComparatorConstants.REMOVE_IMPORTS);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getImportsOnly(removals));
            section.addContent(Comparison.SectionType.CONTENT_REMOVAL, content);
        }
    }

    private String getImportsOnly(Map<String, Vector<Import>> additions) {
        try {
            Definition tempDefinition = WSDLComparisonUtils.getWSDLDefinition();
            for (Collection<Import> addition : additions.values()) {
                for (Import importItem : addition) {
                    tempDefinition.addImport(importItem);
                }
            }
            return WSDLComparisonUtils.getWSDLWithoutDeclaration(tempDefinition);
        } catch (WSDLException e) {
            log.error(e);
        }
        return null;
    }

    private String getImportsOnly(Vector<Import> importz) {
        try {
            Definition tempDefinition = WSDLComparisonUtils.getWSDLDefinition();
            for (Import importItem : importz) {
                tempDefinition.addImport(importItem);
            }
            return WSDLComparisonUtils.getWSDLWithoutDeclaration(tempDefinition);
        } catch (WSDLException e) {
            log.error(e);
        }
        return null;
    }

    private boolean detectImportsChange(Map<String, String> base, Map<String, String> changed) {
        //TODO Fix me

        return true;
    }
}
