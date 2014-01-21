<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.governance.list.ui.clients.ListMetadataServiceClient" %>
<%@ page import="org.wso2.carbon.governance.list.stub.beans.xsd.PolicyBean" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.registry.extensions.utils.CommonConstants" %>
<%@ page import="java.net.URLEncoder" %>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>
<link type="text/css" rel="stylesheet" href="../resources/css/registry.css"/>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../list/list-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../list/js/list.js"></script>
<%

    PolicyBean bean;
    String policyFilterKey = null;
    boolean filter = request.getParameter("filter") != null;
    if (filter) {
        policyFilterKey = request.getParameter("policykey");
    }

    try {
        if(!filter) {
            ListMetadataServiceClient listservice = new ListMetadataServiceClient(config, session);
            bean = listservice.listpolicies();
        } else {
            ListMetadataServiceClient listservice = new ListMetadataServiceClient(config, session);
            bean = listservice.listPoliciesByName(policyFilterKey);
        }

    } catch (Exception e) {

%>
<script type="text/javascript">
      CARBON.showErrorDialog("<%=e.getMessage()%>",function(){
          location.href="../admin/index.jsp";
          return;
      });

</script>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.governance.list.ui.i18n.Resources">
<carbon:breadcrumb
            label="list.policies.menu.text"
            resourceBundle="org.wso2.carbon.governance.list.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>" />
<br/>

<script type="text/javascript">
    function submitFilterByNameForm() {
        sessionAwareFunction(function() {
            var advancedSearchForm = $('filterByNameForm');
            advancedSearchForm.submit();
        }, org_wso2_carbon_governance_list_ui_jsi18n["session.timed.out"]);
    }
</script>

<div id="middle">
    <h2><fmt:message key="policy.list"/></h2>
    <div id="workArea">

   <%if(bean.getSize() != 0 || filter){%>
       <p style="padding:5px">
       <form id="filterByNameForm" action="policy_name_filter_ajaxprocessor.jsp"
             onsubmit="return submitFilterByNameForm();" method="post">

           <table id="#_innerTable" style="width:100%">
               <tr id="buttonRow">
                   <td nowrap="nowrap" style="line-height:25px;padding-right:10px;width:150px;">Filter by Policy Name:</td>
                   <td nowrap="nowrap" style="width:200px">
                       <input id="id_Policy_Name" onkeypress="if (event.keyCode == 13) {submitFilterByNameForm(); }"
                              type="text" name="Policy_Name" style="width:200px;margin-bottom:10px;">
                   </td>
                   <td>
                       <table style="*width:430px !important;">
                   <tbody>
                             <tr>
                       <td>
                          <a class="icon-link" href="#" style="background-image: url(../search/images/search.gif);" onclick="submitFilterByNameForm(); return false;" alt="Search"></a>
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
            <%if(bean.getSize()==0){%>
                 <thead>
                    <tr>
                        <%
                        if (filter) {
                        %>
                        <th><fmt:message key="no.policies.matches.filter"/></th>
                        <% } else { %>
                        <th><fmt:message key="no.policies"/></th>
                        <% } %>
                    </tr>
                </thead>
        <%} else{
            int pageNumber;
            String pageStr = request.getParameter("page");
            if (pageStr != null) {
                pageNumber = Integer.parseInt(pageStr);
            } else {
                pageNumber = 1;
            }
            int itemsPerPage = (int)(RegistryConstants.ITEMS_PER_PAGE * 1.5);
            int numberOfPages;
            if (bean.getName().length % itemsPerPage == 0) {
                numberOfPages = bean.getName().length / itemsPerPage;
            } else {
                numberOfPages = bean.getName().length / itemsPerPage + 1;
            }
            boolean isBrowseAuthorized = CarbonUIUtil.isUserAuthorized(request,
                    "/permission/admin/manage/resources/browse");
            boolean isLCAvailable = false;
            for(int i=(pageNumber - 1) * itemsPerPage;i<pageNumber * itemsPerPage && i<bean.getName().length;i++) {
                if (bean.getLCName()[i]!=null && !bean.getLCName()[i].equals("")) {
                    isLCAvailable = true;
                    break;
                }
            }
        %>
            <thead>
            <tr>
                    <th><fmt:message key="policy.name"/></th>
                    <th><fmt:message key="version"/></th>
                    <% if (isLCAvailable) {%><th><fmt:message key="policy.LC.info"/></th><%} %>
                    <% if (isBrowseAuthorized) {%><th><fmt:message key="actions"/></th><%} %>
                </tr>
            </thead>
            <tbody>
                    <%
              for(int i=(pageNumber - 1) * itemsPerPage;i<pageNumber * itemsPerPage && i<bean.getName().length;i++) {
                  String tempPath = bean.getPath()[i];
                  String completePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + tempPath;
                  try {
                      tempPath = URLEncoder.encode(tempPath, "UTF-8");
                  } catch (Exception ignore) {}
                  String urlCompletePath = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + tempPath;
                 %>
                <tr>
                    <%
                        String policyName = bean.getName()[i];
                        String LCState = "";
                        if(isLCAvailable && bean.getLCName()[i]!=null && !bean.getLCName()[i].equals("")){
                            LCState = bean.getLCName()[i] + " / " + bean.getLCState()[i];
                        }
                        String version = "";
                        if (RegistryUtils.getResourceName(RegistryUtils.getParentPath(completePath)).replace(
                                "-SNAPSHOT", "").matches(CommonConstants.SERVICE_VERSION_REGEX)) {
                            version = RegistryUtils.getResourceName(RegistryUtils.getParentPath(completePath));
                        }
                        if (isBrowseAuthorized) { %>
                    <td><a href="../resources/resource.jsp?region=region3&item=resource_browser_menu&path=<%=urlCompletePath%>"><%=policyName%></a></td>
                    <td><%=version%></td>
                    <% if (isLCAvailable) {%><td><%=LCState%></td><%} %>
                    <td>
                        <%if (bean.getCanDelete()[i])  { %>
                            <a title="<fmt:message key="delete"/>" onclick="deleteService('<%=completePath%>','/','../listPolicy/policy.jsp?region=region3&item=governance_list_policy_menu')" href="#" class="icon-link registryWriteOperation" style="background-image:url(../admin/images/delete.gif);"><fmt:message key="delete"/></a>
                         <%} else { %>
                            <a class="icon-link registryWriteOperation" style="background-image:url(./images/delete-desable.gif);color:#aaa !important;cursor:default;"><fmt:message key="delete"/></a>
                         <%} %>
                        <a onclick="downloadDependencies('<%=completePath%>')"  href="#"
                                   class="icon-link registryWriteOperation" style="background-image:url(../resources/images/icon-download.jpg);"><fmt:message key="download"/></a>

                    </td>
                    <% } else { %>
                    <td><%=policyName%></td>
                    <td><%=version%></td>
                    <% if (isLCAvailable) {%><td><%=LCState%></td><%} %>
                    <% } %>
                </tr>

                    <%
             }
             %>
            </tbody>
        </table>
        <table width="100%" style="text-align:center; padding-top: 10px; margin-bottom: -10px">
            <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                      resourceBundle="org.wso2.carbon.governance.list.ui.i18n.Resources"
                                      nextKey="next" prevKey="prev"
                                      paginationFunction="loadPagedList({0}, false, 'listPolicy', 'policy', 'policy')" />
        <%}%>
        </table>
    </form>
    </div>
    </div>
        <script type="text/javascript">
        alternateTableRows('customTable','tableEvenRow','tableOddRow');
</script>
</fmt:bundle>    
