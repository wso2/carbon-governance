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

package org.wso2.carbon.governance.rest.api.model;


import org.wso2.carbon.governance.rest.api.internal.PaginationInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypedList<T> {

    private Class<T> type;
    private Map<String, List<T>> artifacts = new HashMap();
    private Pagination pagination;

    public TypedList(Class<T> type, Map<String, List<T>> artifacts) {
        this.type = type;
        this.artifacts = artifacts;
    }

    public TypedList(Class<T> type) {
        this.type = type;
    }

    public TypedList(Class<T> genericArtifactClass, String assetType, List<T> artifactList,
                     PaginationInfo paginationInfo) {
        this.type = type;
        this.artifacts.put(assetType, artifactList);
        if(paginationInfo != null) {
            this.pagination = new Pagination(paginationInfo);
        }

    }

    public Class<T> getType() {
        return type;
    }

    public Map<String, List<T>> getArtifacts() {
        return artifacts;
    }

    public void addArtifacts(String shortName, List<T> artifacts) {
        getArtifacts().put(shortName, artifacts);
    }

    public boolean hasData() {
        return artifacts.size() > 0 ? true : false;

    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    @Override
    public String toString() {
        return "TypedList{" +
               "type=" + type +
               ", artifacts=" + artifacts +
               '}';
    }

    public class Pagination {

        private Integer count;
        private Integer selfStart;
        private Integer nextStart;
        private Integer previousStart;
        private String query;

        public Pagination() {
        }

        public Pagination(PaginationInfo info) {
            this.count = info.getCount();
            this.selfStart = info.getStart();
            if (info.isMorePages()) {
                this.nextStart = selfStart + count;
            }
            if (selfStart - count >= 0) {
                this.previousStart = selfStart - count;
            }
            this.query = info.getQuery();
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public Integer getSelfStart() {
            return selfStart;
        }

        public void setSelfStart(Integer selfStart) {
            this.selfStart = selfStart;
        }

        public Integer getNextStart() {
            return nextStart;
        }

        public void setNextStart(Integer nextStart) {
            this.nextStart = nextStart;
        }

        public Integer getPreviousStart() {
            return previousStart;
        }

        public void setPreviousStart(Integer previousStart) {
            this.previousStart = previousStart;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public class Entry {
            private int start;
            private int count;

            public Entry() {
            }

            public Entry(int start, int count) {
                this.start = start;
                this.count = count;
            }

            public int getStart() {
                return start;
            }

            public void setStart(int start) {
                this.start = start;
            }

            public int getCount() {
                return count;
            }

            public void setCount(int count) {
                this.count = count;
            }
        }

    }


}
