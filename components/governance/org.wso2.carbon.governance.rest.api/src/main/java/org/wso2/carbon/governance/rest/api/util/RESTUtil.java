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

package org.wso2.carbon.governance.rest.api.util;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RESTUtil {

    public static final String MAXID_QUERY_PARAM = "maxid";
    public static final String COUNT_QUERY_PARAM = "count";

    public static String getResourceName(String shortName) {
        //TODO - handle "s" and "es"
        return shortName.concat("s");
    }

    public static String generateLink(String shortName, String id, String baseURI, boolean formatShortName) {
        String resourceName = formatShortName == Boolean.TRUE ? RESTUtil.getResourceName(shortName) : shortName;
        return baseURI + resourceName + "/" + id;
    }

    public static String generateLink(String shortName, String id, String baseURI) {
        return generateLink(shortName, id, baseURI, true);
    }

    public static String getBaseURL(UriInfo uriInfo) {
        return uriInfo.getBaseUri().toString();
    }

    public String formatKey(String key) {
        if (key.indexOf("_") == -1) {
            //Assume this is belong to "overview_"
            return "overview_" + key;
        }
        return key;
    }

    public static List<String> formatValue(String value) {
        List<String> values = new ArrayList<>();
        values.addAll(Arrays.asList(value.split(",")));
        return values;
    }

    public static String getShortName(String assetType) {
        return assetType.substring(0, assetType.length() - 1);
    }

    public static int getMaxid(MultivaluedMap<String, String> queryParams) {
        if (queryParams.get(MAXID_QUERY_PARAM) != null) {
            String maxid = queryParams.get(MAXID_QUERY_PARAM).get(0);
            if (maxid != null || !"".equals(MAXID_QUERY_PARAM)) {
                return Integer.valueOf(MAXID_QUERY_PARAM);
            }
        }
        return 0;
    }

    public static int getCount(MultivaluedMap<String, String> queryParams) {
        if (queryParams.get(COUNT_QUERY_PARAM) != null) {
            String count = queryParams.get(COUNT_QUERY_PARAM).get(0);
            if (count != null || !"".equals(COUNT_QUERY_PARAM)) {
                return Integer.valueOf(COUNT_QUERY_PARAM);
            }
        }
        return 20;
    }
}
