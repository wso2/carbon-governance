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

import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.rest.api.internal.PaginationInfo;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {

    public static final String TEMP_BELONG_TO_ASSET_ID = "_temp_belongToAssetID";
    public static final String TEMP_BELONG_TO_ASSET_SHORT_NAME = "_temp_belongToAssetShortName";

     // TODO - for SOAPService/RESTService -> Endpoint use cases introduce new associations as follows.
//   public static final String ENDPOINT_ASSOCIATION_USE = "use";
//   public static final String ENDPOINT_ASSOCIATION_BELONG_TO = "belongTo";
    public static final String ENDPOINT_ASSOCIATION_USE = "usedBy";
    public static final String ENDPOINT_ASSOCIATION_BELONG_TO = "depends";



    public static String getResourceName(String shortName) {
        //TODO - handle "s" and "es"
        return shortName.concat("s");
    }

    public static String generateLink(String shortName, String id, String baseURI, boolean formatShortName) {
        String resourceName = formatShortName == Boolean.TRUE ? Util.getResourceName(shortName) : shortName;
        if (id != null) {
            return baseURI + resourceName + "/" + id;
        }
        return baseURI + resourceName;
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

    public static PaginationInfo getPaginationInfo(MultivaluedMap<String, String> queryParams, String tenantHeader) {
        PaginationInfo paginationInfo = new PaginationInfo();

        Integer start = getFirstIntValue(queryParams.get(PaginationInfo.PAGINATION_PARAM_START));
        if (start != null) {
            paginationInfo.setStart(start);
        }

        Integer count = getFirstIntValue(queryParams.get(PaginationInfo.PAGINATION_PARAM_COUNT));
        if (count != null) {
            paginationInfo.setCount(count);
        }

        Integer limit = getFirstIntValue(queryParams.get(PaginationInfo.PAGINATION_PARAM_LIMIT));
        if (limit != null) {
            paginationInfo.setLimit(limit);
        }

        String sort = getFirstStringValue(queryParams.get(PaginationInfo.PAGINATION_PARAM_SORT_ORDER));
        if (sort != null) {
            paginationInfo.setSortOrder(sort);
        }

        String sortBy = getFirstStringValue(queryParams.get(PaginationInfo.PAGINATION_PARAM_SORT_BY));
        if (sortBy != null) {
            paginationInfo.setSortBy(sortBy);
        }

        String tenant = getFirstStringValue(queryParams.get(PaginationInfo.PAGINATION_PARAM_TENANT));
        if (tenant != null) {
            paginationInfo.setTenant(tenant);
        } else if (tenantHeader != null) {
            paginationInfo.setTenant(tenantHeader);
        }

        return paginationInfo;
    }

    public static void excludePaginationParameters(MultivaluedMap<String, String> queryParams) {
        queryParams.remove(PaginationInfo.PAGINATION_PARAM_START);
        queryParams.remove(PaginationInfo.PAGINATION_PARAM_COUNT);
        queryParams.remove(PaginationInfo.PAGINATION_PARAM_LIMIT);
        queryParams.remove(PaginationInfo.PAGINATION_PARAM_SORT_ORDER);
        queryParams.remove(PaginationInfo.PAGINATION_PARAM_SORT_BY);
    }

    private static String getFirstStringValue(List<String> values) {
        if (values != null && values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    private static Integer getFirstIntValue(List<String> values) {
        if (values != null && values.size() > 0) {
            /*
            Special case where user provided incorrect values for start, count or limit parameters.
            No point to throw NumberFormatException instead fallback to use default values for above
            parameters and continue with the query.
             */
            try {
                return Integer.valueOf(values.get(0));
            } catch (NumberFormatException e) {
                //ignore it.
            }
        }
        return null;
    }


    public static String generateLink(String shortName, String baseURI, boolean formatShortName) {
        return generateLink(shortName, null, baseURI, formatShortName);
    }

    public static String generateBelongToLink(GovernanceArtifact artifact, String baseURI) throws GovernanceException {
        String  id = artifact.getAttribute(Util.TEMP_BELONG_TO_ASSET_ID);
        String shortName = artifact.getAttribute(Util.TEMP_BELONG_TO_ASSET_SHORT_NAME);
        if(id != null && shortName != null){
            artifact.removeAttribute(Util.TEMP_BELONG_TO_ASSET_SHORT_NAME);
            artifact.removeAttribute(Util.TEMP_BELONG_TO_ASSET_ID);
          return generateLink(shortName, id, baseURI);
        }
        return null;
    }
}
