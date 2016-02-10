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

package org.wso2.carbon.governance.rest.api.internal;

public class PaginationInfo {

    public static final int PAGINATION_DEFAULT_START = 0;
    public static final int PAGINATION_DEFAULT_COUNT = 10;
    public static final int PAGINATION_DEFAULT_LIMIT = 100;
    public static final String PAGINATION_PARAM_START = "start";
    public static final String PAGINATION_PARAM_COUNT = "count";
    public static final String PAGINATION_PARAM_LIMIT = "limit";

    public static final String PAGINATION_PARAM_SORT_ORDER = "sort";
    public static final String PAGINATION_PARAM_SORT_BY = "sortby";
    public static final String PAGINATION_SORT_ORDER_ASCENDING = "asc";
    public static final String PAGINATION_SORT_ORDER_DESCENDING = "des";
    public static final String PAGINATION_SORT_BY_NAME = "overview_name";
    public static final String OVERVIEW_PREFIX = "overview_";
    public static final String PAGINATION_PARAM_TENANT = "tenant";

    private int start;
    private int count;
    private int limit;
    private String sortOrder;
    private String sortBy;
    private String query;
    private boolean morePages = false;
    private String tenant;


    public PaginationInfo() {
        this.start = PAGINATION_DEFAULT_START;
        this.count = PAGINATION_DEFAULT_COUNT;
        this.limit = PAGINATION_DEFAULT_LIMIT;
        this.sortOrder = PAGINATION_SORT_ORDER_ASCENDING;
        this.sortBy = PAGINATION_SORT_BY_NAME;
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

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        if (sortOrder != null && PAGINATION_SORT_ORDER_DESCENDING.equals(sortOrder)) {
            this.sortOrder = PAGINATION_SORT_ORDER_DESCENDING.toUpperCase();
        }
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        if (sortBy != null && !sortBy.isEmpty() && sortBy.indexOf(OVERVIEW_PREFIX) == -1) {
            this.sortBy = OVERVIEW_PREFIX.concat(sortBy);
        }
        this.sortBy = sortBy;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isMorePages() {
        return morePages;
    }

    public void setMorePages(boolean morePages) {
        this.morePages = morePages;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    @Override
    public String toString() {
        return "PaginationInfo{" +
               "start=" + start +
               ", count=" + count +
               ", limit=" + limit +
               ", sortOrder='" + sortOrder + '\'' +
               ", sortBy='" + sortBy + '\'' +
               ", query='" + query + '\'' +
               ", morePages=" + morePages +
               ", tenant='" + tenant + '\'' +
               '}';
    }


}
