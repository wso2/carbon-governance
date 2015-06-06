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
import javax.wsdl.Message;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WSDLMessagesComparator extends AbstractWSDLComparator {

    private final Log log = LogFactory.getLog(WSDLMessagesComparator.class);

    @Override
    public void init() {

    }

    @Override
    public void compareInternal(Definition base, Definition changed, DefaultComparison comparison)
            throws ComparisonException {
        compareMessages(base, changed, comparison);
    }

    protected void compareMessages(Definition base, Definition changed, DefaultComparison comparison) {

        DefaultComparison.DefaultSection section = null;

        Set<QName> baseKeys = base.getMessages().keySet();
        Set<QName> changedKeys = changed.getMessages().keySet();

        Set<QName> additionKeys = Sets.difference(changedKeys, baseKeys);
        if (section == null && additionKeys.size() > 0) {
            section = comparison.newSection();
        }
        processAdditions(section, additionKeys, changed);

        Set<QName> removalKeys = Sets.difference(baseKeys, changedKeys);
        if (section == null && removalKeys.size() > 0) {
            section = comparison.newSection();
        }
        processRemovals(section, removalKeys, base);


        Set<QName> commonKeys = Sets.intersection(baseKeys, changedKeys);
        if (section == null && commonKeys.size() > 0) {
            section = comparison.newSection();
        }
        processChanges(section, commonKeys, base, changed);


        if (section != null) {
            comparison.addSection(ComparatorConstants.WSDL_IMPORTS, section);
        }


    }

    protected void processChanges(DefaultComparison.DefaultSection section,
                                  Set<QName> commonKeys, Definition base, Definition changed) {
        Map<String, Message> baseMessages = base.getMessages();
        Map<String, Message> changedMessages = changed.getMessages();
        List<Message> leftMessages = new ArrayList<>();
        List<Message> rightMessages = new ArrayList<>();
        if (commonKeys.size() > 0) {
            for (QName key : commonKeys) {
                Message left = baseMessages.get(key);
                Message right = changedMessages.get(key);
                if (isDiffrent(left, right)) {
                    leftMessages.add(left);
                    rightMessages.add(right);
                }
            }
            if (leftMessages.size() > 0) {
                section.addSectionSummary(Comparison.SectionType.CONTENT_CHANGE, ComparatorConstants.CHANGED_MESSAGES);
                DefaultComparison.DefaultSection.DefaultTextChangeContent content = section.newTextChangeContent();
                DefaultComparison.DefaultSection.DefaultTextChange textChange = section.newTextChange();
                textChange.setOriginal(getMessagesOnly(leftMessages, base));
                textChange.setChanged(getMessagesOnly(rightMessages, changed));
                content.setContent(textChange);
                section.addContent(Comparison.SectionType.CONTENT_CHANGE, content);

            }


        }

    }

    protected void processAdditions(DefaultComparison.DefaultSection section,
                                    Set<QName> additionKeys, Definition changed) {
        if (additionKeys.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_ADDITION, ComparatorConstants.NEW_MESSAGES);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getMessagesOnly(additionKeys, changed));
            section.addContent(Comparison.SectionType.CONTENT_ADDITION, content);
        }

    }

    protected void processRemovals(DefaultComparison.DefaultSection section,
                                   Set<QName> removalKeys, Definition base) {
        if (removalKeys.size() > 0) {
            section.addSectionSummary(Comparison.SectionType.CONTENT_REMOVAL, ComparatorConstants.REMOVE_MESSAGES);
            DefaultComparison.DefaultSection.DefaultTextContent content = section.newTextContent();
            content.setContent(getMessagesOnly(removalKeys, base));
            section.addContent(Comparison.SectionType.CONTENT_REMOVAL, content);
        }

    }

    private String getMessagesOnly(Set<QName> removalKeys, Definition definition) {
        try {
            Definition tempDefinition = WSDLComparisonUtils.getWSDLDefinition();
            Map<QName, Message> messages = definition.getMessages();
            for (QName key : removalKeys) {
                Message message = messages.get(key);
                tempDefinition.addMessage(message);
            }
            WSDLComparisonUtils.copyNamespaces(definition, tempDefinition);
            return WSDLComparisonUtils.getWSDLWithoutDeclaration(tempDefinition);
        } catch (WSDLException e) {
            log.error(e);
        }
        return null;
    }

    private String getMessagesOnly(List<Message> messages, Definition definition) {
        try {
            Definition tempDefinition = WSDLComparisonUtils.getWSDLDefinition();
            for (Message message : messages) {
                tempDefinition.addMessage(message);
            }
            WSDLComparisonUtils.copyNamespaces(definition, tempDefinition);
            return WSDLComparisonUtils.getWSDLWithoutDeclaration(tempDefinition);
        } catch (WSDLException e) {
            log.error(e);
        }
        return null;
    }

    private boolean isDiffrent(Message left, Message right) {
        return WSDLComparisonUtils.isDiffrentMessages(left, right);
    }

}
