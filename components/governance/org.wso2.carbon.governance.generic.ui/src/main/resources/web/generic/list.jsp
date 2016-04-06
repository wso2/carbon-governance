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

<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.governance.generic.stub.beans.xsd.ArtifactBean" %>
<%@ page import="org.wso2.carbon.governance.generic.stub.beans.xsd.ArtifactsBean" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.GenericUIGenerator" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.UIGeneratorConstants" %>
<%@ page import="org.wso2.carbon.governance.lcm.ui.clients.LifeCycleManagementServiceClient" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.utils.NetworkUtils" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.wso2.carbon.registry.core.pagination.PaginationContext" %>

<%@ page import="java.util.Comparator" %>
<%@ page import="java.util.Arrays" %>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>
<link type="text/css" rel="stylesheet" href="../resources/css/registry.css"/>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../generic/js/genericpagi.js"></script>
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
    String key = request.getParameter("key");
    String breadcrumb = request.getParameter("breadcrumb");
    String lc_name = request.getParameter("lc_name");
    String lc_state = request.getParameter("lc_state");
    String lc_in_out = request.getParameter("lc_in_out");
    String lc_state_in_out = request.getParameter("lc_state_in_out");
    String sortOrder = request.getParameter("sortOrder");
    String sortBy = request.getParameter("sortBy");
    String searchvalule = request.getParameter("searchValue");
    String filterBy = request.getParameter("filterBy");

    if(sortBy==null){
        sortBy = "";
    }

    if(sortOrder == null){
        sortOrder = "ASC";
    }

    String queryTrailer = "&key=" + key + "&breadcrumb=" + breadcrumb;
    String dataName = request.getParameter("dataName");
    if (dataName == null) {
        dataName = "metadata";
    } else {
        queryTrailer += "&dataName=" + dataName;
    }
    String dataNamespace = request.getParameter("dataNamespace");
    if (dataNamespace == null) {
        dataNamespace = UIGeneratorConstants.DATA_NAMESPACE;
    } else {
        queryTrailer += "&dataNamespace=" + dataNamespace;
    }
    String singularLabel = request.getParameter("singularLabel");
    if (singularLabel == null) {
        singularLabel = "Artifact";
    } else {
        queryTrailer += "&singularLabel=" + singularLabel;
    }
    String pluralLabel = request.getParameter("pluralLabel");
    if (pluralLabel == null) {
        pluralLabel = "Artifacts";
    } else {
        queryTrailer += "&pluralLabel=" + pluralLabel;
    }
    String criteria = null;
    boolean filter = request.getParameter("filter") != null;
    if (filter) {
        criteria = (String) session.getAttribute("criteria");
    }
    ArtifactsBean bean = null;
    String region = request.getParameter("region");
    String item = request.getParameter("item");
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String[] temp = null;
    LifeCycleManagementServiceClient LCClient;
    try{
        LCClient = new LifeCycleManagementServiceClient(cookie, config, session);
        temp = LCClient.getLifeCycleList(request);

    } catch (Exception e){
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
        PaginationContext.init(start, count, sortOrder, sortBy, Integer.MAX_VALUE);
        ManageGenericArtifactServiceClient client = new ManageGenericArtifactServiceClient(config, session);

        if(client != null) {
            if (lc_name == null && lc_state == null) {
                bean = client.listArtifacts(key, criteria);
            } else {
                bean = client.listArtifactsByLC(key, lc_name, lc_state, lc_in_out, lc_state_in_out);
            }
        }
    } catch (Exception e) {
        if (filter) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("<%=Encode.forJavaScript(e.getMessage())%>", function() {
        location.href = "../generic/list.jsp?region=<%=Encode.forUriComponent(region)%>&item=
        <%=Encode.forUriComponent(item)%><%=Encode.forUriComponent(queryTrailer)%>";
        return;
    });

</script>
<%
} else {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("<%=Encode.forJavaScript(e.getMessage())%>", function() {
        location.href = "../admin/index.jsp";
        return;
    });

</script>
<%
        }
        return;
    }finally {
            PaginationContext.destroy();
        }
%>
<fmt:bundle basename="org.wso2.carbon.governance.generic.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.JSResources"
            request="<%=request%>" namespace="org.wso2.carbon.governance.generic.ui"/>
    <carbon:breadcrumb
            label="<%=Encode.forHtml(request.getParameter("breadcrumb"))%>"
            topPage="true"
            request="<%=request%>"/>
    <br/>

    <script type="text/javascript">

         function downloadDependencies(path) {
            sessionAwareFunction(function() {
                new Ajax.Request('../generic/download_util_ajaxprocessor.jsp',
                        {
                            method:'post',
                            parameters: {path: path},

                            onSuccess: function(transport) {
                                var str = jQuery.trim(transport.responseText);
                                var resp = jQuery.trim(str.substring(str.indexOf('{')+1).split('}')[0]);
                                var url = resp.split('**')[0];
                                var hasDependencies = resp.split('**')[1];
                                downloadWithDependencies(url,hasDependencies);
                            },

                            onFailure: function() {
                                CARBON.showErrorDialog(transport.responseText);
                            }
                        });

            }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
        }

        function submitFilterForm() {
            sessionAwareFunction(function() {
                var field = $('filterByList').value;

                if(field!=0 && field !=1){
                    var value = $('id_Search_Val').value;
                    document.getElementById('searchVal').name = toPascalCase(field).substring(0, field.length);
                    document.getElementById('searchVal').value = value;
                    document.getElementById('searchvalule').value = value;
                    document.getElementById('filterBy').value = field;
                    submitToAdvanceFilter();
                }else if(field==1){
                    var lcname = $('lifeCycleList').value;
                    var state = $('stateList').value;
                    var lcinout =  $('inoutListLC').value;
                    var lcstateinout =  $('inoutListLCState').value;
                    document.getElementById('searchVal2').value = lcname;
                    if(state!="0"){
                        document.getElementById('searchVal3').value = state;
                        document.getElementById('searchVal4').value = lcinout;
                        document.getElementById('searchVal5').value = lcstateinout;
                    }else{
                        document.getElementById('searchVal3').value = "";
                        document.getElementById('searchVal4').value = lcinout;
                        document.getElementById('searchVal5').value = "";
                    }
                    if(lcname!= "Select"){
                        submitToLCFilter()
                    }
                }else if(field==0){
                    loadPagedList(1);
                }
            }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
        }

        function submitToLCFilter(){
            sessionAwareFunction(function() {
                var advancedSearchForm = $('filterLCForm');
                advancedSearchForm.submit();
            }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
        }

        function submitToAdvanceFilter(){
            sessionAwareFunction(function() {
                var advancedSearchForm = $('filterForm');
                advancedSearchForm.submit();
            }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
        }
    </script>
    <%
            String hostName = "localhost";
            String port = "9443";
            try {
                hostName = NetworkUtils.getMgtHostName();
                port =  System.getProperty("mgt.transport.https.port");
            } catch (Exception ignored) {
            }
     %>
     <table width="100%" style="margin: 0px 0px 5px 0px;">
        <tr>
            <td class="deprecate-warning">From version 5.1.0 onwards, performing governance operations are deprecated from the management console. Please use the publisher app(<a href='https://<%=hostName%>:<%=port%>/store'>https://<%=hostName%>:<%=port%>/store</a>) instead.</td>
        </tr>
    </table>
    <div id="middle">
        <h2><fmt:message key="artifact.list"><fmt:param value="<%=Encode.forHtml(singularLabel)%>"/><</fmt:message></h2>

        <div id="workArea">
            <%if ((bean.getArtifacts() != null && bean.getArtifacts().length != 0) || filter ) {%>
            <p style="padding:5px">

                    <%--This is a hidden form that is filled by the scripts when user search by any feild other than LC
          Will fill this form and will sent to the advance filter--%>
            <form id="filterForm" action="basic_filter_ajaxprocessor.jsp"
                  onsubmit="return submitToAdvanceFilter();" method="post">
                <input type="hidden" name="dataName" value="<%=Encode.forHtml(dataName)%>"/>
                <input type="hidden" name="singularLabel" value="<%=Encode.forHtml(singularLabel)%>"/>
                <input type="hidden" name="pluralLabel" value="<%=Encode.forHtml(pluralLabel)%>"/>
                <input type="hidden" name="dataNamespace" value="<%=Encode.forHtml(dataNamespace)%>">
                <input type="hidden" name="key" value="<%=Encode.forHtml(key)%>">
                <input type="hidden" name="region" value="<%=Encode.forHtml(region)%>">
                <input type="hidden" name="item" value="<%=Encode.forHtml(item)%>">
                <input type="hidden" name="breadcrumb" value="<%=Encode.forHtml(breadcrumb)%>">
                <input id="filterBy" type="hidden" name="filterBy" value="<%=Encode.forHtml(filterBy)%>">
                <input id="searchvalule" type="hidden" name="searchvalule" value="<%=Encode.forHtml(searchvalule)%>">
                <input id="searchVal" type="hidden" name="" value="">

            </form>

                <%--This is a hidden form that is filled by the scripts when user search by LC. Will fill
       the form and will be sent to the LC filter--%>
            <form id="filterLCForm" action="filter_lc_ajaxprocessor.jsp"
                  onsubmit="return submitToLCFilter();" method="post">
                <input type="hidden" name="dataName" value="<%=Encode.forHtml(dataName)%>"/>
                <input type="hidden" name="singularLabel" value="<%=Encode.forHtml(singularLabel)%>"/>
                <input type="hidden" name="pluralLabel" value="<%=Encode.forHtml(pluralLabel)%>"/>
                <input type="hidden" name="dataNamespace" value="<%=Encode.forHtml(dataNamespace)%>">
                <input type="hidden" name="key" value="<%=Encode.forHtml(key)%>">
                <input type="hidden" name="region" value="<%=Encode.forHtml(region)%>">
                <input type="hidden" name="item" value="<%=Encode.forHtml(item)%>">
                <input type="hidden" name="breadcrumb" value="<%=Encode.forHtml(breadcrumb)%>">
                <input id="searchVal2" type="hidden" name="lc_name" value="<%=Encode.forHtml(lc_name)%>">
                <input id="searchVal3" type="hidden" name="lc_state" value="<%=Encode.forHtml(lc_state)%>">
                <input id="searchVal4" type="hidden" name="lc_in_out" value="<%=Encode.forHtml(lc_in_out)%>">
                <input id="searchVal5" type="hidden" name="lc_state_in_out" value="<%=Encode.forHtml(lc_state_in_out)%>">
            </form>

            <form id="tempFilterForm" onKeydown="Javascript: if (event.keyCode==13) {submitFilterForm(); return false;}"
                  onsubmit="return submitFilterForm();" method="post">


                <table id="#_innerTable" style="width:100%">
                    <tr id="buttonRow">
                        <td nowrap="nowrap" style="line-height:25px;padding-right:10px;width:50px;"><fmt:message key="filter.by.name"/></td>
                        <td style="width:1px;">
                            <select id="filterByList" onchange="changeVisibility()">
                                 <option value="1" <%= ((request.getParameter("filterBy")==null||(request.getParameter("filterBy").equals("1")))?" selected ":"") %>>LifeCycle</option>
                                <%
                                    GenericUIGenerator gen = new GenericUIGenerator();
                                    ManageGenericArtifactServiceClient client = new ManageGenericArtifactServiceClient(config,session);
                                    OMElement uiconfig = gen.getUIConfiguration(client.getArtifactUIConfiguration(request.getParameter("key")),request,config,session);
                                    String[] keyList = gen.getKeyList(uiconfig, bean.getKeys());

                                %>
                                <%

                                    for (String field : keyList) {
                                        int lastIndex = field.lastIndexOf("_");
                                        String name = gen.getLabelValue(uiconfig,field);
                                        if (name == null) {
                                            name = field.substring(lastIndex+1);
                                        }
                                %>

                                <option <%= ((request.getParameter("filterBy")!=null && (request.getParameter("filterBy").equals(field)))?" selected ":"") %> value="<%=field%>"><%=name%></option>
                                <%

                                    }
                                %>

                            </select>
                        </td>

                        <td style="width:1px;">
                            <input id="id_Search_Val"
                                   type="text" name="search_val" style="width:200px;margin-bottom:10px;display:none;" value="<%= Encode.forHtml(((searchvalule!=null)?searchvalule:"")) %>">
                        </td>

                        <td style="width:1px;">
                            <select id="inoutListLC" onchange="changeInOutListLC()">
                                <option  value="in">Is</option>
                                <option <%= ((request.getParameter("lc_in_out")!=null&&(request.getParameter("lc_in_out").equals("out")))?" selected ":"") %>  value="out">Is Not</option>
                            </select>
                        </td>

                        <td style="width:1px;">
                            <select id="lifeCycleList" onchange="changeLC()">
                                <option value="Any">Any</option>
                                <%
                                    boolean once = true;
                                    for (String next:temp) {
                                        if(once){
                                %>
                                <option value="<%=Encode.forHtml(next)%>" <%= ((request.getParameter
                                ("lc_name")==null||((request.getParameter("lc_name").equals(next))||
                                (request.getParameter("lc_name").equals(""))))?" selected ":"") %> >
                                <%=Encode.forHtml(next)%></option>
                                <%
                                    once = false;
                                }else{
                                %>
                                <option <%= ((request.getParameter("lc_name")!=
                                null&&(request.getParameter("lc_name").equals(next)))?" selected ":"")
                                %> value="<%=Encode.forHtml(next)%>"><%=Encode.forHtml(next)%></option>
                                <%
                                        }
                                    }

                                %>
                            </select>
                        </td>

                        <td style="width:1px;">
                            <select id="inoutListLCState">
                                <option <%= ((request.getParameter("lc_state_in_out")!=null&&(request.getParameter("lc_state_in_out").equals("in")))?" selected ":"") %> value="in">In</option>
                                <option <%= ((request.getParameter("lc_state_in_out")!=null&&(request.getParameter("lc_state_in_out").equals("out")))?" selected ":"") %> value="out">Not In</option>
                            </select>
                        </td>

                        <td style="width:1px;">
                            <select id="stateList">
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
                                    <td style="vertical-align:middle;padding-left:10px;padding-right:5px;"> |</td>
                                    <td style="vertical-align:middle;padding-left:10px;padding-right:5px;">
                                        <a class="icon-link" style="background-image:url(../search/images/search-top.png);"
                                        href="../generic/filter.jsp?list_region=<%=Encode.forUriComponent(region)%>
                                        &list_item=<%=Encode.forUriComponent(item)%>&dataNamespace=
                                        <%=Encode.forUriComponent(dataNamespace)%>&dataName=
                                        <%=Encode.forUriComponent(dataName)%>&singularLabel=
                                        <%=Encode.forUriComponent(singularLabel)%>&pluralLabel=
                                        <%=Encode.forUriComponent(pluralLabel)%>&key=
                                        <%=Encode.forUriComponent(key)%>&list_breadcrumb=
                                        <%=Encode.forUriComponent(breadcrumb)%>"><fmt:message
                                                key="filter.artifact.message"><fmt:param
                                                value="<%=Encode.forHtml(singularLabel)%>"/></fmt:message></a>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </table>
            </form>
            </p>
            <br>
            <%}%>
            <form id="profilesEditForm">
                <table class="styledLeft" id="customTable">
                    <%if (bean.getArtifacts() == null || bean.getArtifacts().length == 0) {%>
                    <thead>
                    <tr>
                        <%
                            if (filter) {
                        %>
                        <th><fmt:message key="no.artifact.matches.filter"><fmt:param
                                value="<%=Encode.forHtml(singularLabel)%>"/></fmt:message></th>
                        <% } else { %>
                        <th><fmt:message key="no.artifacts"><fmt:param value="<%=Encode.forHtml(pluralLabel)%>"/></fmt:message></th>
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
                   boolean isBrowseAuthorized = CarbonUIUtil.isUserAuthorized(request,
                                "/permission/admin/manage/resources/browse");
                        boolean isLCAvailable = false;
                        for (int j = 0; j <bean.getArtifacts().length; j++) {
                            if (bean.getArtifacts()[j].getLCName() != null && !bean.getArtifacts()[j].getLCName().equals("")) {
                                isLCAvailable = true;
                                break;
                            }
                        }
                    %>
                    <thead>
                    <tr>
                        <%
                           String[] artifactKeys = bean.getKeys();
                           String[] artifactNames = bean.getNames();

                            String creationDateArtifactName = null ;
                            boolean displayingCreationDate = false ;
                            int creationDateIgnoreIndex = -1 ;

                            String lastUpdatedDateArtifactName = null ;
                            boolean displayingLastUpdatedDate = false ;
                            int lastUpdatedDateIgnoreIndex = -1 ;

                            String createdByArtifactName = null ;
                            boolean displayingCreatedBy = false ;
                            int createdByIgnoreIndex = -1 ;

                            String lastUpdatedByArtifactName = null ;
                            boolean displayinglastUpdatedBy = false ;
                            int lastUpdatedByIgnoreIndex = -1 ;

                            for (int i=0;i <artifactNames.length;i++) {
                                String displayStr;
                                String imgType;
                                if (request.getParameter("sortBy") !=null &&
                                        request.getParameter("sortBy").equals(artifactKeys[i])) {
                                    displayStr = "display:'';margin-top:4px;margin-right:2px;";
                                } else {
                                    displayStr = "display:none;";
                                }
                                if(sortOrder.equals("DES")){
                                    imgType ="../admin/images/down-arrow.gif";
                                } else {
                                    imgType ="../admin/images/up-arrow.gif";
                                }

                                if(artifactKeys[i].equalsIgnoreCase("meta_created_date")) {
                                    creationDateArtifactName = artifactNames[i] ;
                                    displayingCreationDate = true ;
                                    creationDateIgnoreIndex = i ;
                                    continue;
                                }

                                if(artifactKeys[i].equalsIgnoreCase("meta_last_updated_date")) {
                                    lastUpdatedDateArtifactName = artifactNames[i] ;
                                    displayingLastUpdatedDate = true ;
                                    lastUpdatedDateIgnoreIndex = i ;
                                    continue;
                                }

                                if(artifactKeys[i].equalsIgnoreCase("meta_created_by")) {
                                    createdByArtifactName = artifactNames[i] ;
                                    displayingCreatedBy = true ;
                                    createdByIgnoreIndex = i ;
                                    continue;
                                }

                                if(artifactKeys[i].equalsIgnoreCase("meta_last_updated_by")) {
                                    lastUpdatedByArtifactName = artifactNames[i] ;
                                    displayinglastUpdatedBy = true ;
                                    lastUpdatedByIgnoreIndex = i ;
                                    continue;
                                }
            %>
            <th id="<%=artifactKeys[i]%>">
                <a onclick="sortAndOrder(
                        '<%=pageNumber%>',
                        '<%="ASC".equals(request.getParameter("sortOrder")) ? "DES" : "ASC" %>',
                        '<%=artifactKeys[i]%>')" title="Sort By <%=artifactNames[i]%>">

                                 <img  src="<%=imgType%>" border="0" align="right" style="<%=displayStr%>"
                                       id="<%=artifactKeys[i]%>" alt="up">
                                 <%=artifactNames[i]%></a></th>
                        <%
                            }
                        %>
            <%
                String imgType;
                if(sortOrder.equals("DES")){
                    imgType ="../admin/images/down-arrow.gif";
                } else {
                    imgType ="../admin/images/up-arrow.gif";
                }

                if(displayingCreationDate) {
            %>

            <th id="CreatedDate">
                <a onclick="sortAndOrder(
                        '<%=pageNumber%>',
                        '<%="ASC".equals(request.getParameter("sortOrder")) ? "DES" : "ASC" %>',
                        'meta_created_date')" title="Sort By Creation Date">

                    <img  src="<%=imgType%>" border="0" align="right" style="display:'';margin-top:4px;margin-right:2px;"
                          id="Created Date" alt="up">
                    <%=creationDateArtifactName%>
                </a>
            </th>

            <% } if(displayingLastUpdatedDate) { %>

            <th id="LastUpdatedDate">
                <a onclick="sortAndOrder(
                        '<%=pageNumber%>',
                        '<%="ASC".equals(request.getParameter("sortOrder")) ? "DES" : "ASC" %>',
                        'meta_last_updated_date')" title="Sort By Last Updated Date">

                    <img  src="<%=imgType%>" border="0" align="right" style="display:'';margin-top:4px;margin-right:2px;"
                          id="LastUpdatedDate" alt="up">
                    <%=lastUpdatedDateArtifactName%>
                </a>
            </th>

            <% } if(displayingCreatedBy) { %>
            <th><%=createdByArtifactName%></th>
            <% } if(displayinglastUpdatedBy) { %>
            <th><%=lastUpdatedByArtifactName%></th>
            <% } %>
                        <% if (isLCAvailable) {%><th><fmt:message key="lifecycle.info"/></th><%} %>
                        <%
                            if (isBrowseAuthorized) {%>
                        <th><fmt:message key="actions"/></th>
                        <%} %>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (int j = 0;j < bean.getArtifacts().length; j++) {
                            ArtifactBean artifact = bean.getArtifacts()[j];

                    %>
                    <tr>
                        <%
                            if (isBrowseAuthorized) {
                                for (int i = 0; i < bean.getNames().length; i++) {
                                    if(creationDateIgnoreIndex == i || lastUpdatedDateIgnoreIndex == i || createdByIgnoreIndex == i || lastUpdatedByIgnoreIndex == i) {
                                        continue ;
                                    }
                                    if (bean.getTypes()[i].equals("path")) {
                        %>
                        <td>
                            <a href="../resources/resource.jsp?region=region3&item=resource_browser_menu&path=<%=URLEncoder.encode(artifact.getValuesB()[i], "UTF-8")%>"><%= artifact.getValuesA()[i] != null ? artifact.getValuesA()[i] : "" %>
                            </a></td>
                        <%
                        } else if (bean.getTypes()[i].equals("link")) {
                        %>
                            <td>
                                <a target="_blank" href="<%=artifact.getValuesB()[i]%>"><%= artifact.getValuesA()[i] != null ? artifact.getValuesA()[i] : "" %>
                                </a>
                            </td>
                        <%
                        } else {
                        %>
                        <td><%= artifact.getValuesA()[i] != null ? artifact.getValuesA()[i] : "" %>
                        </td>
                        <%
                                }
                            }
                        %>
                        <%
                            String createdDate = artifact.getCreatedDate();
                            String lastUpdatedDate = artifact.getLastUpdatedDate();
                            String createdBy = artifact.getCreatedBy();
                            String lastUpdatedBy = artifact.getLastUpdatedBy();

                            String LCState = "";
                            if (isLCAvailable && artifact.getLCName() != null && !artifact.getLCName().equals("")) {
                                LCState = artifact.getLCName() + " / " + artifact.getLCState();
                            }

                            if(displayingCreationDate) {
                        %>

                        <td><%=createdDate%></td>
                        <% } if(displayingLastUpdatedDate) {%>
                        <td><%=lastUpdatedDate%></td>
                        <% } if(displayingCreatedBy) {%>
                        <td><%=createdBy%></td>
                        <% } if(displayinglastUpdatedBy) {%>
                        <td><%=lastUpdatedBy%></td>
                        <% } %>

                        <% if (isLCAvailable) {%><td><%=LCState%></td><%} %>
                        <td><% if (artifact.getCanDelete()) { %><a title="<fmt:message key="delete"/>"
                                                                   onclick="deleteArtifact('<%=Encode.forUri(artifact.getPath())%>','/','../generic/list.jsp?region=<%=URLEncoder.encode(region)%>&item=<%=URLEncoder.encode(item)%><%=Encode.forUri(queryTrailer)%>')"
                                                                   href="#" class="icon-link registryWriteOperation"
                                                                   style="background-image:url(../admin/images/delete.gif);"><fmt:message
                                key="delete"/></a><% } else {%><a class="icon-link registryWriteOperation"
                                                                  style="background-image:url(../generic/images/delete-desable.gif);color:#aaa !important;cursor:default;"><fmt:message
                                key="delete"/></a><% } %>

                            <a onclick="downloadDependencies('<%=artifact.getPath()%>')"  href="#"
                                class="icon-link registryWriteOperation" style="background-image:url(../resources/images/icon-download.jpg);"><fmt:message key="download"/></a>
                        </td>
                        <%
                        } else {
                            for (int i = 0; i < bean.getNames().length; i++) {
                        %>
                        <td><%=artifact.getValuesA()[i]%>
                        </td>
                        <%
                                }
                            }
                        %>
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

        function loadPagedList(page) {

        if(document.getElementById('filterBy').value == "null"||document.getElementById('filterBy').value == "1"){

                window.location = '<%="../generic/list.jsp?" + ((request.getParameter("lc_name")!=null)?"lc_name=" +
                Encode.forUriComponent(request.getParameter("lc_name")):"") + ((request.getParameter("lc_state")!=null)?"&lc_state=" +
                Encode.forUriComponent(request.getParameter("lc_state")):"") + ((request.getParameter("lc_in_out")!=null)?"&lc_in_out=" +
                Encode.forUriComponent(request.getParameter("lc_in_out")):"") + ((request.getParameter("lc_state_in_out")!=null)?"&lc_state_in_out=" +
                Encode.forUriComponent(request.getParameter("lc_state_in_out")):"") + "&region=" + Encode.forUriComponent(request.getParameter("region")) + "&item=" +
                Encode.forUriComponent(request.getParameter("item")) + "&dataName=" + Encode.forUriComponent(request.getParameter("dataName")) + "&singularLabel=" +
                Encode.forUriComponent(request.getParameter("singularLabel")) + "&pluralLabel=" + Encode.forUriComponent(request.getParameter("pluralLabel")) + "&dataNamespace=" +
                Encode.forUriComponent(request.getParameter("dataNamespace")) + "&key=" + Encode.forUriComponent(request.getParameter("key")) + "&breadcrumb=" +
                Encode.forUriComponent(request.getParameter("breadcrumb")) + (filter ? "&filter=filter" : "") +"&sortOrder=" + Encode.forUriComponent(sortOrder) + "&sortBy=" +
                Encode.forUriComponent(sortBy)+ "&page=" %>' + page;

         }else{

                window.location = '<%="../generic/list.jsp?" + ((request.getParameter("filterBy")!=null)?"filterBy=" +
                Encode.forUriComponent(request.getParameter("filterBy")):"") + ((request.getParameter("searchValue")!=null)?"&searchValue=" +
                Encode.forUriComponent(request.getParameter("searchValue")):"") + "&region=" + Encode.forUriComponent(request.getParameter("region")) + "&item=" +
                Encode.forUriComponent(request.getParameter("item")) + "&dataName=" + Encode.forUriComponent(request.getParameter("dataName")) + "&singularLabel=" +
                Encode.forUriComponent(request.getParameter("singularLabel")) + "&pluralLabel=" + Encode.forUriComponent(request.getParameter("pluralLabel")) + "&dataNamespace=" +
                Encode.forUriComponent(request.getParameter("dataNamespace")) + "&key=" + Encode.forUriComponent(request.getParameter("key")) + "&breadcrumb=" +
                Encode.forUriComponent(request.getParameter("breadcrumb")) + (filter ? "&filter=filter" : "") +"&sortOrder=" + Encode.forUriComponent(sortOrder) + "&sortBy=" +
                Encode.forUriComponent(sortBy)+ "&page=" %>' + page;
         }
     }

        function sortAndOrder(page,sortOrder,sortBy ) {

            if(document.getElementById('filterBy').value == "null"||document.getElementById('filterBy').value == "1"){

                window.location = '<%="../generic/list.jsp?" + ((request.getParameter("lc_name")!=null)?"lc_name=" +
                Encode.forUriComponent(request.getParameter("lc_name")):"") + ((request.getParameter("lc_state")!=null)?"&lc_state=" +
                Encode.forUriComponent(request.getParameter("lc_state")):"") + ((request.getParameter("lc_in_out")!=null)?"&lc_in_out=" +
                Encode.forUriComponent(request.getParameter("lc_in_out")):"") + ((request.getParameter("lc_state_in_out")!=null)?"&lc_state_in_out=" +
                Encode.forUriComponent(request.getParameter("lc_state_in_out")):"") + "&region=" + Encode.forUriComponent(request.getParameter("region")) + "&item=" +
                Encode.forUriComponent(request.getParameter("item")) + "&dataName=" + Encode.forUriComponent(request.getParameter("dataName")) + "&singularLabel=" +
                Encode.forUriComponent(request.getParameter("singularLabel")) + "&pluralLabel=" + Encode.forUriComponent(request.getParameter("pluralLabel")) + "&dataNamespace=" +
                Encode.forUriComponent(request.getParameter("dataNamespace")) + "&key=" + Encode.forUriComponent(request.getParameter("key")) + "&breadcrumb=" +
                Encode.forUriComponent(request.getParameter("breadcrumb")) + (filter ? "&filter=filter" : "") + "&page=" %>'+ page +
                '<%="&sortOrder="%>' +sortOrder+ '<%="&sortBy="%>' +sortBy;

            }else{
                window.location = '<%="../generic/list.jsp?" + ((request.getParameter("filterBy")!=null)?"filterBy=" +
                Encode.forUriComponent(request.getParameter("filterBy")):"") + ((request.getParameter("searchValue")!=null)?"&searchValue=" +
                Encode.forUriComponent(request.getParameter("searchValue")):"") + "&region=" + Encode.forUriComponent(request.getParameter("region")) + "&item=" +
                Encode.forUriComponent(request.getParameter("item")) + "&dataName=" + Encode.forUriComponent(request.getParameter("dataName")) + "&singularLabel=" +
                Encode.forUriComponent(request.getParameter("singularLabel")) + "&pluralLabel=" + Encode.forUriComponent(request.getParameter("pluralLabel")) + "&dataNamespace=" +
                Encode.forUriComponent(request.getParameter("dataNamespace")) + "&key=" + Encode.forUriComponent(request.getParameter("key")) + "&breadcrumb=" +
                Encode.forUriComponent(request.getParameter("breadcrumb")) + (filter ? "&filter=filter" : "") + "&page=" %>'+ page +
                '<%="&sortOrder="%>' +sortOrder+ '<%="&sortBy="%>' +sortBy;
            }
        }
    </script>
    <script type="text/javascript">
        //        call after page loaded
        window.onload=changeLC;
    </script>
</fmt:bundle>
