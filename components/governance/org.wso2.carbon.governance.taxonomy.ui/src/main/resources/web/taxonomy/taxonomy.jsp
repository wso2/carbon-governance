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
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ page import="org.wso2.carbon.governance.taxonomy.ui.clients.TaxonomyManagementClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.governance.taxonomy.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.governance.taxonomy.ui"/>
<script type="text/javascript" src="js/taxonomy.js"></script>
<%

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String[] temp = null;
    TaxonomyManagementClient client;
    try {
        client = new TaxonomyManagementClient(cookie, config, session);
        temp = client.getTaxonomyList(request);
    } catch (Exception e) {
        response.setStatus(500);
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp?<%=e.getMessage()%>"/>
<%
        return;
    }
%>
<fmt:bundle basename="org.wso2.carbon.governance.taxonomy.ui.i18n.Resources">
    <carbon:breadcrumb
            label="taxonomy.source"
            resourceBundle="org.wso2.carbon.governance.taxonomy.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <div id="middle">

        <h2>
            <fmt:message key="governance.taxonomy.menu"/>
        </h2>
        <div id="workArea">
            <%
                if (temp == null || temp.length == 0 || temp[0] == null) {
            %>
            <div class="registryWriteOperation">
                <fmt:message
                        key="no.taxonomy.are.currently.defined.click.add.taxonomy.to.create.a.new.taxonomy"/>
            </div>
            <div class="registryNonWriteOperation">
                <fmt:message
                        key="no.taxonomy.are.currently.defined"/>
            </div>
            <%
            } else {
            %>
            <table class="styledLeft" cellspacing="1" id="lcmTable">
                <thead>
                <tr>
                    <th>
                        <fmt:message key="name"/>
                    </th>
                    <th>
                        <fmt:message key="actions"/>
                    </th>
                </tr>
                </thead>
                <tbody>

                <%
                    for (String next : temp) {
                %>
                <tr>
                    <td>
                        <%=next%>
                    </td>
                    <td>
                        <a href="#" onclick="editTaxonomy('<%=next%>')" class="icon-link"
                           style="background-image:url(../admin/images/edit.gif);"><fmt:message key="edit"/></a>
                        <a href="#" onclick="deleteTaxonomy('<%=next%>')" class="icon-link registryWriteOperation"
                           style="background-image:url(../admin/images/delete.gif);"><fmt:message key="delete"/></a>
                    </td>

                </tr>
                <%
                    }
                %>
                </tbody>
            </table>
            <%
                }
            %>
            <script type="text/javascript">
                alternateTableRows('lcmTable', 'tableEvenRow', 'tableOddRow');
            </script>

            <div class="registryWriteOperation" style="height:25px;">
                <a class="icon-link" style="background-image: url(../admin/images/add.gif);" href="source_taxonomy.jsp"><fmt:message
                        key="add.taxonomy"/></a>
            </div>

        </div>


    </div>
</fmt:bundle>