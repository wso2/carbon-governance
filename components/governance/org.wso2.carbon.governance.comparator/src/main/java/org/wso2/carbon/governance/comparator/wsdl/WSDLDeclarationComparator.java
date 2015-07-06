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
import org.wso2.carbon.governance.comparator.Comparison;
import org.wso2.carbon.governance.comparator.ComparisonException;
import org.wso2.carbon.governance.comparator.common.DefaultComparison;
import org.wso2.carbon.governance.comparator.utils.ComparatorConstants;
import org.wso2.carbon.governance.comparator.utils.WSDLComparisonUtils;

import javax.wsdl.Definition;
import java.util.Map;

public class WSDLDeclarationComparator extends AbstractWSDLComparator {


    @Override
    public void compareInternal(Definition base, Definition changed, DefaultComparison comparison)
            throws ComparisonException {
        validate(base, changed);
        Comparison.Section section = compareDeclarations(base, changed, comparison);
        if (section != null) {
            comparison.addSection(ComparatorConstants.WSDL_DECLARATION, section);
        }
    }


    protected Comparison.Section compareDeclarations(Definition base, Definition changed,
                                                     DefaultComparison comparison) {
        DefaultComparison.DefaultSection section = null;
        if (detectDeclarationChange(base, changed)) {
            section = comparison.newSection();
            section.addSectionSummary(Comparison.SectionType.CONTENT_CHANGE, ComparatorConstants.DECLARATION_HAS_CHANGED);
            DefaultComparison.DefaultSection.DefaultTextChangeContent content = section.newTextChangeContent();
            DefaultComparison.DefaultSection.DefaultTextChange textChange = section.newTextChange();
            textChange.setOriginal(WSDLComparisonUtils.getWSDLDeclarationOnly(base));
            textChange.setChanged(WSDLComparisonUtils.getWSDLDeclarationOnly(changed));
            content.setContent(textChange);
            section.addContent(Comparison.SectionType.CONTENT_CHANGE, content);
        }
        return section;
    }

    private boolean detectDeclarationChange(Definition base, Definition changed) {

        // 1. Check Namespaces
        Map<String, String> baseNamespaces = base.getNamespaces();
        Map<String, String> changedNamespaces = changed.getNamespaces();
        MapDifference<String, String> mapDiff = Maps.difference(baseNamespaces, changedNamespaces);
        if (!mapDiff.areEqual()) {
            return true;
        }

        // 2. Check QNames
        if (isNotEqualAndNotNull(base.getQName(), changed.getQName())) {
            return true;
        }

        // 3. Check TNS
        if (isNotEqualAndNotNull(base.getTargetNamespace(), changed.getTargetNamespace())) {
            return true;
        }


        // 4. Check documentation
        if (isNotEqualAndNotNull(base.getDocumentationElement(), changed.getDocumentationElement())) {
            return true;
        }

        // 4. Check baseURI
        //TODO It seems comparing baseURI is not correct, check again.
//        if (isNotEqualAndNotNull(base.getDocumentBaseURI(), changed.getDocumentBaseURI())) {
//            return true;
//        }


        return false;

    }

    private boolean isNotEqualAndNotNull(Object left, Object right) {
        if (left != null && right != null && !left.equals(right)) {
            return true;
        }
        return false;
    }

    protected void validate(Definition base, Definition changed) throws ComparisonException {
        if (base == null || changed == null) {
            throw new ComparisonException("WSDL Definition can not be null ");
        }
    }

}
