<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.governance.generic.stub.beans.xsd.ContentArtifactsBean" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.governance.lcm.ui.clients.LifeCycleManagementServiceClient" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.registry.extensions.utils.CommonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.utils.NetworkUtils" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.wso2.carbon.registry.core.pagination.PaginationContext" %>

<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>
<link type="text/css" rel="stylesheet" href="../resources/css/registry.css"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../relations/relations-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../relations/js/relations.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../generic/js/generic.js"></script>
<script type="text/javascript" src="js/artifacts-list.js"></script>
<style type="text/css">
    td.deprecate-warning {
        position: absolute;
        top: 135px;
        left: 262px;
        right: 19px;
        background-color: #FEEFB3;
        font-weight: 600;
        font-size: 12px;
        color: #000;
        padding: 10px;
        width: auto;
    }
</style>
<%
    String lc_name = request.getParameter("lc_name");
    String lc_state = request.getParameter("lc_state");
    String lc_in_out = request.getParameter("lc_in_out");
    String lc_state_in_out = request.getParameter("lc_state_in_out");
    String sortOrder = request.getParameter("sortOrder");
    String sortBy = request.getParameter("sortBy");
    String filterBy = request.getParameter("filterBy");
    String searchvalule = request.getParameter("searchValue");

    if (sortBy == null) {
        sortBy = "";
    }
    if (sortOrder == null) {
        sortOrder = "ASC";
    }

    ContentArtifactsBean bean;
    String breadcrumb = request.getParameter("breadcrumb");
    if (breadcrumb == null) {
        breadcrumb = "Artifact";
    }
    String key = request.getParameter("key");
    String singularLabel = request.getParameter("singularLabel");
    String pluralLabel = request.getParameter("pluralLabel");
    String hasNamespaceStr = request.getParameter("hasNamespace");
    String mediaType = request.getParameter("mediaType").replace(" ", "+");
    String item = request.getParameter("item");
    String region = request.getParameter("region");
    String listURL = "list_content.jsp?" + "key=" + Encode.forUriComponent(key) + "&breadcrumb=" + Encode
            .forUriComponent(breadcrumb) +
            "&singularLabel=" + Encode.forUriComponent(singularLabel) + "&pluralLabel=" + Encode
            .forUriComponent(pluralLabel) + "&hasNamespace=" +
            Encode.forUriComponent(hasNamespaceStr) + "&item=" + Encode.forUriComponent(item) + "&region=" + Encode
            .forUriComponent(region) + "&mediaType=" + Encode.forUriComponent(mediaType);

    boolean hasNamespace = Boolean.parseBoolean(hasNamespaceStr);
    String filterKey = null;
    boolean filter = request.getParameter("filter") != null;
    if (filter) {
        filterKey = request.getParameter("artby_name");
        listURL += "&filter=" + Encode.forUriComponent(request.getParameter("filter")) + "&artby_name=" + Encode
                .forUriComponent(filterKey);
    }
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String[] temp = null;
    LifeCycleManagementServiceClient LCClient;
    try {
        LCClient = new LifeCycleManagementServiceClient(cookie, config, session);
        temp = LCClient.getLifeCycleList(request);

    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }

    try {
        String pageStr = request.getParameter("page");

        int start;
        int count = (int) (RegistryConstants.ITEMS_PER_PAGE * 1.5);
        if (pageStr != null) {
            start = (int) ((Integer.parseInt(pageStr) - 1) * (RegistryConstants.ITEMS_PER_PAGE * 1.5));
        } else {
            start = 0;
        }
        PaginationContext.init(start, count, sortOrder, sortBy, 1500);
        ManageGenericArtifactServiceClient client = new ManageGenericArtifactServiceClient(config, session);

        if (!filter) {
            bean = client.listContentArtifacts(mediaType);
        } else if (lc_name == null && lc_state == null) {
            bean = client.listContentArtifactsByName(mediaType, request.getParameter("filter"));
        } else {
            bean = client.listContentArtifactsByLC(mediaType, lc_name, lc_state, lc_in_out, lc_state_in_out);
        }

    } catch (Exception e) {


%>
<script type="text/javascript">
    CARBON.showErrorDialog("<%=e.getMessage()%>", function () {
        location.href = "../admin/index.jsp";
        return;
    });

</script>
<%
        return;
    } finally {
        PaginationContext.destroy();
    }
%>

<fmt:bundle basename="org.wso2.carbon.governance.generic.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.JSResources"
            request="<%=request%>" namespace="org.wso2.carbon.governance.generic.ui"/>
    <carbon:breadcrumb
            label="<%=Encode.forHtml(breadcrumb)%>"
            resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <br/>

    <script type="text/javascript">
        function submitFilterForm() {
            sessionAwareFunction(function () {
                var field = $('filterByList').value;

                if (field != 0 && field != 1) {
                    var value = $('id_Search_Val').value;
                    document.getElementById('searchVal').name = toPascalCase(field).substring(0, field.length);
                    document.getElementById('searchVal').value = value;
                    document.getElementById('searchvalule').value = value;
                    document.getElementById('filterBy').value = field;
                    submitToNameFilter();
                } else if (field == 1) {
                    var lcname = $('lifeCycleList').value;
                    var state = $('stateList').value;
                    var lcinout = $('inoutListLC').value;
                    var lcstateinout = $('inoutListLCState').value;
                    document.getElementById('searchVal2').value = lcname;
                    if (state != "0") {
                        document.getElementById('searchVal3').value = state;
                        document.getElementById('searchVal4').value = lcinout;
                        document.getElementById('searchVal5').value = lcstateinout;
                    } else {
                        document.getElementById('searchVal3').value = "";
                        document.getElementById('searchVal4').value = lcinout;
                        document.getElementById('searchVal5').value = "";
                    }
                    if (lcname != "Select") {
                        submitToLCFilter()
                    }
                } else if (field == 0) {
                    loadPagedList(1);
                }
            }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
        }

        function submitToLCFilter() {
            sessionAwareFunction(function () {
                var advancedSearchForm = $('filterLCForm');
                advancedSearchForm.submit();
            }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
        }

        function submitToNameFilter() {
            sessionAwareFunction(function () {
                var advancedSearchForm = $('filterByNameForm');
                advancedSearchForm.submit();
            }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
        }

        //        function submitFilterByNameForm() {
        //            sessionAwareFunction(function() {
        //                var advancedSearchForm = $('filterByNameForm');
        //                advancedSearchForm.submit();
        //            }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
        //        }

        function downloadDependencies(path) {
            sessionAwareFunction(function () {
                new Ajax.Request('../generic/download_util_ajaxprocessor.jsp',
                        {
                            method: 'post',
                            parameters: {path: path},

                            onSuccess: function (transport) {
                                var str = transport.responseText.trim();
                                var resp = str.substring(str.indexOf('{') + 1).split('}')[0].trim();
                                var url = resp.split('**')[0];
                                var hasDependencies = resp.split('**')[1];
                                downloadWithDependencies(url, hasDependencies);
                            },

                            onFailure: function () {
                                CARBON.showErrorDialog(transport.responseText);
                            }
                        });

            }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
        }
    </script>
    <%
        String hostName = "localhost";
        String port = "9443";
        try {
            hostName = NetworkUtils.getMgtHostName();
            port = System.getProperty("mgt.transport.https.port");
        } catch (Exception ignored) {
        }
    %>
    <table width="100%" style="margin: 0px 0px 5px 0px;">
        <tr>
            <td class="deprecate-warning">From version 5.1.0 onwards, performing governance operations are deprecated
                from the management console. Please use the publisher app(<a
                        href='https://<%=hostName%>:<%=port%>/store'>https://<%=hostName%>:<%=port%>/store</a>) instead.
            </td>
        </tr>
    </table>
    <div id="middle">
        <h2><fmt:message key="artifact.list"><fmt:param value="<%=Encode.forHtml(singularLabel)%>"/></fmt:message></h2>
        <div id="workArea">

            <%if (bean.getSize() != 0 || filter) {%>
            <p style="padding:5px">

                    <%--This is a hidden form that is fillinput type="hidden" name="item"ed by the scripts when user search by any feild other than LC
          Will fill this form and will sent to the advance filter--%>
            <form id="filterByNameForm" action="filter_name_ajaxprocessor.jsp"
                  onsubmit="return submitToNameFilter();" method="post">
                <input type="hidden" name="singularLabel" value="<%=Encode.forHtml(singularLabel)%>"/>
                <input type="hidden" name="pluralLabel" value="<%=Encode.forHtml(pluralLabel)%>"/>
                <input type="hidden" name="key" value="<%=Encode.forHtml(key)%>">
                <input type="hidden" name="region" value="<%=Encode.forHtml(region)%>">
                <input type="hidden" name="item" value="<%=Encode.forHtml(item)%>">
                <input type="hidden" name="breadcrumb" value="<%=Encode.forHtml(breadcrumb)%>">
                <input type="hidden" name="hasNamespace" value="<%=Encode.forHtml(hasNamespaceStr)%>">
                <input type="hidden" name="mediaType" value="<%=Encode.forHtml(mediaType)%>">
                <input type="hidden" name="isContent" value="true">
                <input id="searchVal" type="hidden" name="Name" value="">
                <input id="filterBy" type="hidden" name="filterBy" value="<%=filterBy%>">
                <input id="searchvalule" type="hidden" name="searchvalule" value="<%=Encode.forHtml(searchvalule)%>">
            </form>

                <%--This is a hidden form that is filled by the scripts when user search by LC. Will fill
       the form and will be sent to the LC filter--%>
            <form id="filterLCForm" action="contentfilter_lc_ajaxprocessor.jsp"
                  onsubmit="return submitToLCFilter();" method="post">
                <input type="hidden" name="singularLabel" value="<%=Encode.forHtml(singularLabel)%>"/>
                <input type="hidden" name="pluralLabel" value="<%=Encode.forHtml(pluralLabel)%>"/>
                <input type="hidden" name="key" value="<%=Encode.forHtml(key)%>">
                <input type="hidden" name="region" value="<%=Encode.forHtml(region)%>">
                <input type="hidden" name="item" value="<%=Encode.forHtml(item)%>">
                <input type="hidden" name="breadcrumb" value="<%=Encode.forHtml(breadcrumb)%>">
                <input type="hidden" name="hasNamespace" value="<%=Encode.forHtml(hasNamespaceStr)%>">
                <input type="hidden" name="mediaType" value="<%=Encode.forHtml(mediaType)%>">
                <input type="hidden" name="isContent" value="true">
                <input id="searchVal2" type="hidden" name="lc_name" value="<%=Encode.forHtml(lc_name)%>">
                <input id="searchVal3" type="hidden" name="lc_state" value="<%=Encode.forHtml(lc_state)%>">
                <input id="searchVal4" type="hidden" name="lc_in_out" value="<%=Encode.forHtml(lc_in_out)%>">
                <input id="searchVal5" type="hidden" name="lc_state_in_out"
                       value="<%=Encode.forHtml(lc_state_in_out)%>">
            </form>


            <form id="tempFilterForm" onKeydown="Javascript: if (event.keyCode==13) {submitFilterForm(); return false;}"
                  onsubmit="return submitFilterForm();" method="post">
                <input type="hidden" name="singularLabel" value="<%=Encode.forHtml(singularLabel)%>"/>
                <input type="hidden" name="pluralLabel" value="<%=Encode.forHtml(pluralLabel)%>"/>
                <input type="hidden" name="key" value="<%=Encode.forHtml(key)%>">
                <input type="hidden" name="region" value="<%=Encode.forHtml(region)%>">
                <input type="hidden" name="item" value="<%=Encode.forHtml(item)%>">
                <input type="hidden" name="breadcrumb" value="<%=Encode.forHtml(breadcrumb)%>">
                <input type="hidden" name="hasNamespace" value="<%=Encode.forHtml(hasNamespaceStr)%>">
                <input type="hidden" name="mediaType" value="<%=Encode.forHtml(mediaType)%>">
                <input type="hidden" name="isContent" value="true">
                <table id="#_innerTable" style="width:100%">
                    <tr id="buttonRow">
                        <td nowrap="nowrap" style="line-height:25px;padding-right:10px;width:50px;"><fmt:message
                                key="filter.by.name"/></td>
                        <td style="width:1px;">
                            <select id="filterByList" onchange="changeVisibility()">
                                <option value="1" <%= ((request.getParameter("filterBy") == null || (request
                                        .getParameter("filterBy").equals("1"))) ? " selected " : "") %>>LifeCycle
                                </option>
                                <option <%= ((request.getParameter("filterBy") != null && (request
                                        .getParameter("filterBy").equals("Name"))) ? " selected " : "") %> value="Name">
                                    Name
                                </option>

                            </select>
                        </td>

                        <td style="width:1px;">
                            <input id="id_Search_Val"
                                   type="text" name="search_val" style="width:200px;margin-bottom:10px;display:none;"
                                   value="<%= ((searchvalule!=null)?searchvalule:"") %>">
                        </td>

                        <td style="width:1px;">
                            <select id="inoutListLC" onchange="changeInOutListLC()">
                                <option value="in">Is</option>
                                <option <%= ((request.getParameter("lc_in_out") != null && (request
                                        .getParameter("lc_in_out").equals("out"))) ? " selected " : "") %> value="out">
                                    Is Not
                                </option>
                            </select>
                        </td>

                        <td style="width:1px;">
                            <select id="lifeCycleList" onchange="changeLC()">
                                <option value="Any">Any</option>
                                <%
                                    boolean once = true;
                                    for (String next : temp) {
                                        if (once) {
                                %>
                                <option value="<%=Encode.forHtml(next)%>" <%= ((request.getParameter("lc_name") == null
                                        || ((request.getParameter("lc_name").equals(next)) || (request
                                        .getParameter("lc_name").equals("")))) ? " selected " : "") %> ><%=Encode
                                        .forHtml(next)%>
                                </option>
                                <%
                                    once = false;
                                } else {
                                %>
                                <option <%= ((request.getParameter("lc_name") != null && (request
                                        .getParameter("lc_name").equals(next))) ? " selected " : "") %>
                                        value="<%=Encode.forHtml(next)%>"><%=Encode.forHtml(next)%>
                                </option>

                                <%
                                        }
                                    }

                                %>
                            </select>
                        </td>

                        <td style="width:1px;">
                            <select id="inoutListLCState">
                                <option <%= ((request.getParameter("lc_state_in_out") != null && (request
                                        .getParameter("lc_state_in_out").equals("in"))) ? " selected " : "") %>
                                        value="in">In
                                </option>
                                <option <%= ((request.getParameter("lc_state_in_out") != null && (request
                                        .getParameter("lc_state_in_out").equals("out"))) ? " selected " : "") %>
                                        value="out">Not In
                                </option>
                            </select>
                        </td>

                        <td style="width:1px;">
                            <select id="stateList" style="display: '';">
                                    <%--will be filled out as soon as a LC is selected--%>
                            </select>
                        </td>
                        <td>
                            <table style="*width:430px !important;">
                                <tbody>
                                <tr>
                                    <td>
                                        <a class="icon-link" href="#"
                                           style="background-image: url(../search/images/search.gif);"
                                           onclick="submitFilterForm(); return false;" alt="Search"></a>
                                    </td>
                                    <td style="vertical-align:middle;padding-left:10px;padding-right:5px;"></td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </table>
            </form>
            </p>
            <br>
            <%
                }
            %>

            <form id="profilesEditForm">
                <table class="styledLeft" id="customTable">
                    <%if (bean.getSize() == 0) {%>
                    <thead>
                    <tr>
                        <%
                            if (filter) {
                        %>
                        <th><fmt:message key="no.artifact.matches.filter"><fmt:param
                                value="<%=Encode.forHtml(singularLabel)%>"/></fmt:message></th>
                        <% } else { %>
                        <th><fmt:message key="no.artifacts"><fmt:param
                                value="<%=Encode.forHtml(pluralLabel)%>"/></fmt:message></th>
                        <% } %>
                    </tr>
                    </thead>
                    <%
                    } else {
                        int pageNumber;
                        String pageStr = request.getParameter("page");
                        if (pageStr != null) {
                            pageNumber = Integer.parseInt(pageStr);
                        } else {
                            pageNumber = 1;
                        }
                        int rowCount = Integer.parseInt(session.getAttribute("row_count").toString());
                        int itemsPerPage = (int) (RegistryConstants.ITEMS_PER_PAGE * 1.5);
                        int numberOfPages;
                        if (rowCount % itemsPerPage == 0) {
                            numberOfPages = rowCount / itemsPerPage;
                        } else {
                            numberOfPages = rowCount / itemsPerPage + 1;
                        }
                        boolean isBrowseAuthorized = CarbonUIUtil
                                .isUserAuthorized(request, "/permission/admin/manage/resources/browse");
                        boolean isLCAvailable = false;
                        for (int i = 0; i < bean.getName().length; i++) {
                            if (bean.getLCName()[i] != null && !bean.getLCName()[i].equals("")) {
                                isLCAvailable = true;
                                break;
                            }
                        }
                    %>
                    <thead>

                    <tr>
                        <%
                            String imgType;
                            String displayStr;
                            if (sortOrder.equals("DES")) {
                                imgType = "../admin/images/down-arrow.gif";
                            } else {
                                imgType = "../admin/images/up-arrow.gif";
                            }

                            if (request.getParameter("sortBy") != null && request.getParameter("sortBy")
                                    .equals("overview_name")) {
                                displayStr = "display:'';margin-top:4px;margin-right:2px;";
                            } else {
                                displayStr = "display:none;";
                            }
                        %>
                        <th><a onclick="sortContentList('overview_name',
                                '<%="ASC".equals(request.getParameter("sortOrder")) ? "DES" : "ASC" %>');"
                               title="Sort By <fmt:message key="name"/>"> <fmt:message key="name"/>
                            <img src="<%=imgType%>" border="0" align="right" style="<%=displayStr%>">
                        </a></th>

                        <% if (hasNamespace) {%>
                        <th><fmt:message key="namespace"/></th>
                        <%}%>
                        <th><fmt:message key="version"/></th>
                        <% if (isLCAvailable) {%>
                        <th><fmt:message key="lifecycle.info"/></th>
                        <%} %>
                        <th colspan="2"><fmt:message key="actions"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (int i = 0; i < bean.getName().length; i++) {
                            if (bean.getName()[i] == null) {
                                continue;
                            }
                            String tempPath = bean.getPath()[i];
                            String completePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + tempPath;
                            try {
                                tempPath = URLEncoder.encode(tempPath, "UTF-8");
                            } catch (Exception ignore) {
                            }
                            String urlCompletePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + tempPath;
                    %>
                    <tr>
                        <%
                            String name = bean.getName()[i];
                            String namespace = null;
                            if (hasNamespace) {
                                namespace = bean.getNamespace()[i];
                            }
                            String LCState = "";
                            if (isLCAvailable && bean.getLCName()[i] != null && !bean.getLCName()[i].equals("")) {
                                LCState = bean.getLCName()[i] + " / " + bean.getLCState()[i];
                            }

                            String version = RegistryUtils.getResourceName(RegistryUtils.getParentPath(completePath));
                            
                            if (isBrowseAuthorized) { %>
                        <td>
                            <a href="../resources/resource.jsp?region=region3&item=resource_browser_menu&path=<%=urlCompletePath%>"><%=name%>
                            </a></td>
                        <% if (hasNamespace) {%>
                        <td><%=Encode.forHtml(namespace)%>
                        </td>
                        <%}%>
                        <td><%=version%>
                        </td>
                        <% if (isLCAvailable) {%>
                        <td><%=LCState%>
                        </td>
                        <%} %>
                        <td>
                            <%if (bean.getCanDelete()[i]) { %>
                            <a title="<fmt:message key="delete"/>"
                               onclick="deleteArtifact('<%=completePath%>','/','<%=listURL%>')" href="#"
                               class="icon-link registryWriteOperation"
                               style="background-image:url(../admin/images/delete.gif);"><fmt:message key="delete"/></a>
                            <%} else { %>
                            <a class="icon-link registryWriteOperation"
                               style="background-image:url(./images/delete-desable.gif);color:#aaa !important;cursor:default;"><fmt:message
                                    key="delete"/></a>
                            <%} %>
                            <a onclick="downloadDependencies('<%=completePath%>')" href="#"
                               class="icon-link registryWriteOperation"
                               style="background-image:url(../resources/images/icon-download.jpg);"><fmt:message
                                    key="download"/></a>
                        </td>
                        <td><a title="<fmt:message key="dependency"/>"
                               onclick="showAssociationTree('depends','<%=completePath%>')" href="#" class="icon-link"
                               style="background-image:url(../relations/images/dep-tree.gif);"> <fmt:message
                                key="view.dependency"/></a></td>
                        <% } else { %>
                        <td><%=Encode.forJavaScript(name)%>
                        </td>
                        <% if (hasNamespace) {%>
                        <td><%=Encode.forJavaScript(namespace)%>
                        </td>
                        <%}%>
                        <td><%=Encode.forJavaScript(version)%>
                        </td>
                        <% if (isLCAvailable) {%>
                        <td><%=Encode.forJavaScript(LCState)%>
                        </td>
                        <%} %>
                        <td><% if (isBrowseAuthorized) {%><a title="<fmt:message key="delete"/>"
                                                             onclick="deleteArtifact('<%=completePath%>','/','<%=listURL%>')"
                                                             href="#" class="icon-link registryWriteOperation"
                                                             style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                key="delete"/></a><% }%></td>
                        <td><a title="<fmt:message key="dependency"/>"
                               onclick="CARBON.showWarningDialog('<fmt:message key="not.sufficient.permissions"/>');"
                               href="#" class="icon-link"
                               style="background-image:url(../relations/images/dep-tree.gif);"> <fmt:message
                                key="view.dependency"/></a></td>
                        <% } %>
                    </tr>

                    <%
                        }
                    %>
                    </tbody>
                </table>
                <table width="100%" style="text-align:center; padding-top: 10px; margin-bottom: -10px">
                    <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                              resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.Resources"
                                              nextKey="next" prevKey="prev"
                                              paginationFunction="loadPagedList({0})"/>
                    <%}%>
                </table>
            </form>
        </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('customTable', 'tableEvenRow', 'tableOddRow');
        TODO:js
        to
        forUriComponent
        function loadPagedList(page) {
            window.location = '<%="../generic/"+listURL+
            ((request.getParameter("lc_name")!=null)?"&lc_name=" + Encode.forUriComponent(request.getParameter("lc_name")):"") +
            ((request.getParameter("lc_state")!=null)?"&lc_state=" + Encode.forUriComponent(request.getParameter("lc_state")):"") +
            ((request.getParameter("lc_in_out")!=null)?"&lc_in_out=" + Encode.forUriComponent(request.getParameter("lc_in_out")):"") +
            ((request.getParameter("lc_state_in_out")!=null)?"&lc_state_in_out=" + Encode.forUriComponent(request.getParameter("lc_state_in_out")):"") +
            ((request.getParameter("filterBy")!=null)?"&filterBy=" + Encode.forUriComponent(request.getParameter("filterBy")):"")+
            ((request.getParameter("searchValue")!=null)?"&searchValue=" + Encode.forUriComponent(request.getParameter("searchValue")):"")+
            ((request.getParameter("sortOrder")!=null)?"&sortOrder=" + Encode.forUriComponent(request.getParameter("sortOrder")):"")+
            ((request.getParameter("sortBy")!=null)?"&sortBy=" + Encode.forUriComponent(request.getParameter("sortBy")):"")+
            "&page="%>' + page;
        }

        function sortContentList(sortBy, sortOrder) {
            window.location = '<%="../generic/list_content.jsp?" + ((request.getParameter("lc_name")!=null)?"lc_name=" +
            Encode.forUriComponent(request.getParameter("lc_name")):"") + ((request.getParameter("lc_state")!=null)?"&lc_state=" +
            Encode.forUriComponent(request.getParameter("lc_state")):"") + ((request.getParameter("lc_in_out")!=null)?"&lc_in_out=" +
            Encode.forUriComponent(request.getParameter("lc_in_out")):"") + ((request.getParameter("lc_state_in_out")!=null)?"&lc_state_in_out=" +
            Encode.forUriComponent(request.getParameter("lc_state_in_out")):"") +((request.getParameter("searchValue")!=null)?"&searchValue=" +
            Encode.forUriComponent(request.getParameter("searchValue")):"")+ ((request.getParameter("filter")!=null)?"&filter=" +
            Encode.forUriComponent(request.getParameter("filter")):"") +((request.getParameter("artby_name")!=null)?"&artby_name=" +
            Encode.forUriComponent(request.getParameter("artby_name")):"")+((request.getParameter("filterBy")!=null)?"&filterBy=" +
            Encode.forUriComponent(request.getParameter("filterBy")):"") +"&region=" +Encode.forUriComponent(region) +"&item="+Encode.forUriComponent(item) +
            "&key=" + Encode.forUriComponent(key)+ "&breadcrumb=" + Encode.forUriComponent(breadcrumb) +"&mediaType="+ Encode.forUriComponent(mediaType)+
            "&singularLabel="+ Encode.forUriComponent(singularLabel)+ "&pluralLabel=" + Encode.forUriComponent(pluralLabel) + "&hasNamespace=" + hasNamespace%>' +
                    '<%="&sortOrder="%>' + sortOrder + '<%="&sortBy="%>' + sortBy;
        }

    </script>
    <script type="text/javascript">
        // call after page loaded to generate LC state list.
        window.onload = changeLC;
    </script>
</fmt:bundle>