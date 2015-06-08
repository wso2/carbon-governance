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
import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.comparator.Comparison;
import org.wso2.carbon.governance.comparator.ComparisonException;
import org.wso2.carbon.governance.comparator.common.DefaultComparison;
import org.wso2.carbon.governance.comparator.utils.ComparatorConstants;
import org.wso2.carbon.governance.comparator.utils.WSDLComparisonUtils;

import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WSDLOperationComparator extends AbstractWSDLComparator {

    private final Log log = LogFactory.getLog(WSDLImportsComparator.class);

    @Override
    void compareInternal(Definition base, Definition changed, DefaultComparison comparison) throws ComparisonException {
        //Here we only compare operations belong to common PortTypes
        compareOperations(base, changed, comparison);
    }

    protected void compareOperations(Definition base, Definition changed, DefaultComparison comparison) {
        DefaultComparison.DefaultSection section = null;
        Set<QName> commonKeys = Sets.intersection(base.getPortTypes().keySet(), changed.getPortTypes().keySet());
        List<Operation> additions = new ArrayList<>();
        List<Operation> removeals = new ArrayList<>();
        List<Operation> leftChanges = new ArrayList<>();
        List<Operation> rightChanges = new ArrayList<>();
        if (commonKeys.size() > 0) {
            for (QName portTypeName : commonKeys) {
                PortType leftPortType = base.getPortType(portTypeName);
                PortType rightPortType = changed.getPortType(portTypeName);
                List<Operation> leftOperations = leftPortType.getOperations();
                List<Operation> rightOperations = rightPortType.getOperations();

                for (Operation left : leftOperations) {
                    Operation right = rightPortType.getOperation(left.getName(), null, null);
                    if (right != null) {
                        if (isDifferent(left, right)) {
                            leftChanges.add(left);
                            rightChanges.add(right);
                        }
                        rightOperations.remove(right);
                    } else if (right == null) {
                        removeals.add(left);
                    }
                }
                additions = rightOperations;
            }
        }

        if (section == null && additions.size() > 0) {
            section = comparison.newSection();
        }
        processAdditions(section, additions, changed);

        if (section == null && removeals.size() > 0) {
            section = comparison.newSection();
        }
        processRemovals(section, removeals, base);

        boolean different = false;
        for (int i = 0; i < leftChanges.size(); i++) {
            if (isDifferent(leftChanges.get(i), rightChanges.get(i))) {
                different = true;
                break;
            }
        }

        if (section == null && different) {
            section = comparison.newSection();
        }
        if (different) {
            processChanges(section, leftChanges, rightChanges, base, changed);
        }

        if (section != null) {
            comparison.addSection(ComparatorConstants.WSDL_IMPORTS, section);
        }
    }


    protected void processAdditions(DefaultComparison.DefaultSection section,
                                    List<Operation> additions, Definition changed) {
        if (additions.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_ADDITION, ComparatorConstants.NEW_OPERATIONS);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getOperationOnly(additions, changed));
            section.addContent(Comparison.SectionType.CONTENT_ADDITION, content);
        }

    }

    protected void processRemovals(DefaultComparison.DefaultSection section,
                                   List<Operation> removeals, Definition base) {
        if (removeals.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_REMOVAL, ComparatorConstants.REMOVE_OPERATIONS);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getOperationOnly(removeals, base));
            section.addContent(Comparison.SectionType.CONTENT_REMOVAL, content);
        }

    }

    protected void processChanges(DefaultComparison.DefaultSection section,
                                  List<Operation> leftChanges, List<Operation> rightChanges, Definition base,
                                  Definition changed) {
        section.addSectionSummary(Comparison.SectionType.CONTENT_CHANGE, ComparatorConstants.CHANGED_OPERATIONS);
        DefaultComparison.DefaultSection.DefaultTextChangeContent content = section.newTextChangeContent();
        DefaultComparison.DefaultSection.DefaultTextChange textChange = section.newTextChange();
        textChange.setOriginal(getOperationOnly(leftChanges, base));
        textChange.setChanged(getOperationOnly(rightChanges, changed));
        content.setContent(textChange);
        section.addContent(Comparison.SectionType.CONTENT_CHANGE, content);
    }

    private String getOperationOnly(List<Operation> operations, Definition definition) {
        try {
            Definition tempDefinition = WSDLComparisonUtils.getWSDLDefinition();
            PortType portType = tempDefinition.createPortType();
            portType.setUndefined(false);
            portType.setQName(new QName(definition.getTargetNamespace(), "temp"));
            for (Operation operation : operations) {
                portType.addOperation(operation);
            }
            tempDefinition.addPortType(portType);
            WSDLComparisonUtils.copyNamespaces(definition, tempDefinition);
            String content = WSDLComparisonUtils.getWSDLWithoutDeclaration(tempDefinition);
            content = content.replace("<wsdl:portType name=\"temp\">", "");
            content = content.substring(content.indexOf(">"));
            return content.replace("</wsdl:portType>", "");

        } catch (WSDLException e) {
            log.error(e);
        }
        return null;
    }

    private boolean isDifferent(Operation left, Operation right) {
        if (isDifferent(left.getInput(), right.getInput())) {
            return true;
        }
        if (isDifferent(left.getOutput(), right.getOutput())) {
            return true;
        }
        if (isDifferent(left.getFaults(), right.getFaults())) {
            return true;
        }
        return false;
    }

    private boolean isDifferent(Map<String, Fault> left, Map<String, Fault> right) {
        if (left != null && right != null && left.size() != right.size()) {
            return true;
        } else {
            MapDifference<String, Fault> mapDiff = Maps.difference(left, right);
            if (!mapDiff.areEqual()) {
                return true;
            } else {
                for (String name : mapDiff.entriesInCommon().keySet()) {
                    if (isDifferent(left.get(name), right.get(name))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isDifferent(Fault left, Fault right) {
        if (left != null && right == null) {
            return true;
        } else if (right != null && left == null) {

        } else {
            if (left.getName() != null && right.getName() != null && !left.getName().equals(right.getName())) {
                return true;
            }
            if (left.getMessage() != null && right.getMessage() != null && WSDLComparisonUtils.isDiffrentMessages(left.getMessage(), right.getMessage())) {
                return true;
            }
        }
        return false;
    }

    private boolean isDifferent(Output left, Output right) {
        if (left != null && right == null) {
            return true;
        } else if (right != null && left == null) {

        } else {
            if (left.getName() != null && right.getName() != null && !left.getName().equals(right.getName())) {
                return true;
            }
            if (left.getMessage() != null && right.getMessage() != null && WSDLComparisonUtils.isDiffrentMessages(left.getMessage(), right.getMessage())) {
                return true;
            }
        }
        return false;
    }

    private boolean isDifferent(Input left, Input right) {
        if (left != null && right == null) {
            return true;
        } else if (right != null && left == null) {
            return true;
        } else {
            if (left.getName() != null && right.getName() != null && !left.getName().equals(right.getName())) {
                return true;
            }
            if (left.getMessage() != null && right.getMessage() != null && WSDLComparisonUtils.isDiffrentMessages(left.getMessage(), right.getMessage())) {
                return true;
            }
        }
        return false;
    }
}
