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

package org.wso2.carbon.governance.comparator.common;

import org.wso2.carbon.governance.comparator.Comparison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultComparison implements Comparison {

    private Map<String, Section> sections = new HashMap<>();

    public DefaultComparison(){
    }


    public DefaultComparison(Map<String, Section> sections){
        this.sections = sections;
    }

    public DefaultSection newSection(){
        return new DefaultSection();
    }

    @Override
    public Map<String, Section> getSections() {
        return sections;
    }

    public void setSections(Map<String, Section> sections){
        this.sections = sections;
    }

    public void addSection(String key, Section section){
        sections.put(key, section);
    }

    public class DefaultSection implements Section {

        private Map<SectionType,List<String>> sectionSummary = new HashMap<>();
        private Map<SectionType,List<Content>> content = new HashMap<>();

        @Override
        public Map<SectionType, List<String>> getSummary() {
            return sectionSummary;
        }

        @Override
        public Map<SectionType, List<Content>> getContent() {
            return content;
        }

        public void setSectionSummary(Map<SectionType, List<String>> sectionSummary){
            this.sectionSummary = sectionSummary;
        }

        public void addSectionSummary(SectionType sectionType, List<String> stringList){
            this.sectionSummary.put(sectionType, stringList);
        }

        public void addSectionSummary(SectionType sectionType, String summaryItem){
            if(sectionSummary.get(sectionType) != null){
                sectionSummary.get(sectionType).add(summaryItem);
            } else {
                List<String> list = new ArrayList<>();
                list.add(summaryItem);
                addSectionSummary(sectionType, list);

            }
        }

        public void setContent(Map<SectionType,List<Content>> content){
            this.content = content;
        }

        public void addContent(SectionType sectionType, List<Content> contentList){
            content.put(sectionType, contentList);
        }

        public void addContent(SectionType sectionType, Content contentItem){
            if(content.get(sectionType) != null){
                content.get(sectionType).add(contentItem);
            } else {
                List<Content> list = new ArrayList<>();
                list.add(contentItem);
                addContent(sectionType, list);
            }
        }

        public DefaultTextContent newTextContent(){
            return new DefaultTextContent();
        }

        public DefaultTextChangeContent newTextChangeContent(){
            return new DefaultTextChangeContent();
        }

        public DefaultTextChange newTextChange(){
            return new DefaultTextChange();
        }

        @Override
        public String toString() {
            return "DefaultSection{" +
                   "sectionSummary=" + sectionSummary +
                   ", content=" + content +
                   '}';
        }

        public class DefaultTextContent implements TextContent {

           private String content;

           @Override
           public String getContent() {
               return content;
           }

           public void setContent(String content){
               this.content = content;
           }

            @Override
            public String toString() {
                return "DefaultTextContent{" +
                       "content='" + content + '\'' +
                       '}';
            }
        }


        public class DefaultTextChangeContent implements TextChangedContent {

            private TextChange content;

            @Override
            public TextChange getContent() {
                return content;
            }

            public void setContent(TextChange content){
                this.content = content;
            }

            @Override
            public String toString() {
                return "DefaultTextChangeContent{" +
                       "content=" + content +
                       '}';
            }
        }

        public class DefaultTextChange implements TextChange {

            private String original;
            private String changed;
            private String diff;

            @Override
            public String getOriginal() {
                return original;
            }

            @Override
            public String getChanged() {
                return changed;
            }

            @Override
            public String getDiff() {
                return diff;
            }

            public void setOriginal(String original) {
                this.original = original;
            }



            public void setChanged(String changed) {
                this.changed = changed;
            }



            public void setDiff(String diff) {
                this.diff = diff;
            }

            @Override
            public String toString() {
                return "DefaultTextChange{" +
                       "original='" + original + '\'' +
                       ", changed='" + changed + '\'' +
                       ", diff='" + diff + '\'' +
                       '}';
            }
        }
    }

    @Override
    public String toString() {
        return "DefaultComparison{" +
               "sections=" + sections +
               '}';
    }
}
