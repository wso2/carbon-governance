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

package org.wso2.carbon.governance.comparator;

import java.util.List;
import java.util.Map;

public interface Comparison {

    public Map<String, Section> getSections();


    interface Section {

        public Map<SectionType, List<String>> getSummary();

        public Map<SectionType, List<Content>> getContent();

        interface Content<T> {
            public T getContent();
        }

        interface TextContent extends Content<String> {
        }

        interface TextChangedContent extends Content<TextChange> {
        }

        interface TextChange {
            public String getOriginal();

            public String getChanged();

            public String getDiff();
        }


    }

    enum SectionType {
        CONTENT_TEXT,
        CONTENT_ADDITION,
        CONTENT_REMOVAL,
        CONTENT_CHANGE
    }
}
