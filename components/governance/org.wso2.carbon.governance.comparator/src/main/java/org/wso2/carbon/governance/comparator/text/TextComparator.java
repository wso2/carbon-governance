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

package org.wso2.carbon.governance.comparator.text;

import org.wso2.carbon.governance.comparator.Comparator;
import org.wso2.carbon.governance.comparator.Comparison;
import org.wso2.carbon.governance.comparator.ComparisonException;
import org.wso2.carbon.governance.comparator.common.DefaultComparison;
import org.wso2.carbon.governance.comparator.utils.ComparatorConstants;

public class TextComparator implements Comparator<String> {

    @Override
    public void init() {

    }

    @Override
    public void compare(String base, String changed, Comparison comparison) throws ComparisonException {
        DefaultComparison.DefaultSection section = ((DefaultComparison) comparison).newSection();
        section.addSectionSummary(Comparison.SectionType.CONTENT_TEXT, ComparatorConstants.TEXT_CHANGE);
        DefaultComparison.DefaultSection.DefaultTextChangeContent content = section.newTextChangeContent();
        DefaultComparison.DefaultSection.DefaultTextChange textChange = section.newTextChange();
        textChange.setOriginal(base);
        textChange.setChanged(changed);
        content.setContent(textChange);
        section.addContent(Comparison.SectionType.CONTENT_TEXT, content);
        ((DefaultComparison) comparison).addSection(ComparatorConstants.TEXT_CHANGE, section);
    }

    @Override
    public boolean isSupportedMediaType(String mediaType) {
        if (ComparatorConstants.TEXT_PLAIN_MEDIA_TYPE.equals(mediaType)) {
            return true;
        }
        return false;
    }
}
