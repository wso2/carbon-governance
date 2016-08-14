<!--
~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.governance.taxonomy.ui.clients.TaxonomyManagementClient" %>

<link type="text/css" rel="stylesheet" href="css/menu.css"/>
<link type="text/css" rel="stylesheet" href="css/style.css"/>

<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<!--EditArea javascript syntax hylighter -->
<script language="javascript" type="text/javascript" src="../editarea/edit_area_full.js"></script>

<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.governance.taxonomy.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.governance.taxonomy.ui"/>
<script type="text/javascript" src="js/taxonomy.js"></script>
<%
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String temp = "";
    boolean isNew = true;

    boolean viewMode = false;
    try {
        TaxonomyManagementClient client = new TaxonomyManagementClient(cookie, config, session);
        viewMode = request.getParameter("view") != null;

        if (request.getParameter("taxonomyName") != null) {
            temp = client.getTaxonomy(request);
            isNew = false;
        } else {
            temp = "<taxonomy id=\"Teams\" name=\"Teams\">\n" + " \n"
                    + "\t<root id=\"wso2Teams\" displayName=\"WSO2 Teams\">\n"
                    + "\t\t<node id=\"sales\" displayName=\"Sales\"></node>\n"
                    + "\t\t<node id=\"marketing\" displayName=\"Marketing\"></node>\n"
                    + "\t\t<node id=\"hR\" displayName=\"HR\"></node>\n"
                    + "\t\t<node id=\"engineering\" displayName=\"Engineering\">\n"
                    + "\t\t\t<node id=\"governanceTG\" displayName=\"Governance TG\">\n"
                    + "\t\t\t\t<node id=\"esGReg\" displayName=\"ES/GReg\"></node>\n"
                    + "\t\t\t\t<node id=\"is\" displayName=\"IS\"></node>\n"
                    + "\t\t\t\t<node id=\"security\" displayName=\"Security\"></node>\n" + "\t\t\t</node>\n"
                    + "\t\t\t<node id=\"platformTG\" displayName=\"Platform TG\">\n"
                    + "\t\t\t\t<node id=\"asCarbon\" displayName=\"AS/Carbon\"></node>\n"
                    + "\t\t\t\t<node id=\"dS\" displayName=\"DS\"></node>\n"
                    + "\t\t\t\t<node id=\"developerStudio\" displayName=\"Developer Studio\"></node>\n"
                    + "\t\t\t\t<node id=\"uiUX\" displayName=\"UI/UX\"></node>\n"
                    + "\t\t\t\t<node id=\"platformExtension\" displayName=\"Platform Extension\"></node>\n"
                    + "\t\t\t</node>\n" + "\t\t\t<node id=\"integrationTG\" displayName=\"Integration TG\">\n"
                    + "\t\t\t\t<node id=\"esbGwLb\" displayName=\"ESB/GW/LB\"></node>\n"
                    + "\t\t\t\t<node id=\"mb\" displayName=\"MB\"></node>\n"
                    + "\t\t\t\t<node id=\"bpsBrs\" displayName=\"BPS/BRS\"></node>\n"
                    + "\t\t\t\t<node id=\"uiUX\" displayName=\"PC \"></node>\n"
                    + "\t\t\t\t<node id=\"platformExtension\" displayName=\"DIS\"></node>\n" + "\t\t\t</node>\n"
                    + "\t\t\t<node id=\"dataTG\" displayName=\"Data TG\">\n"
                    + "\t\t\t\t<node id=\"dasDss\" displayName=\"DAS/DSS\"></node>\n"
                    + "\t\t\t\t<node id=\"cep\" displayName=\"CEP\"></node>\n"
                    + "\t\t\t\t<node id=\"ml\" displayName=\"ML\"></node>\n"
                    + "\t\t\t\t<node id=\"analytics\" displayName=\"Analytics\"></node>\n"
                    + "\t\t\t\t<node id=\"research\" displayName=\"Research\"></node>\n" + "\t\t\t</node>\n"
                    + "\t\t\t<node id=\"apiTG\" displayName=\"API TG\">\n"
                    + "\t\t\t\t<node id=\"apiManager\" displayName=\"API Manager\"></node>\n"
                    + "\t\t\t\t<node id=\"appManager\" displayName=\"App Manager\"></node>\n"
                    + "\t\t\t\t<node id=\"emmIot\" displayName=\"EMM/IOT\"></node>\n" + "\t\t\t</node>\n"
                    + "\t\t\t<node id=\"qaTG\" displayName=\"QA TG\">\n"
                    + "\t\t\t\t<node id=\"qa\" displayName=\"QA\"></node>\n"
                    + "\t\t\t\t<node id=\"qaa\" displayName=\"QAA\"></node>\n" + "\t\t\t</node>\n"
                    + "\t\t\t<node id=\"cloudTG\" displayName=\"Cloud TG\">\n"
                    + "\t\t\t\t<node id=\"appFactory\" displayName=\"AppFactory /SS\"></node>\n"
                    + "\t\t\t\t<node id=\"cloudTeam\" displayName=\"Cloud Team\"></node>\n"
                    + "\t\t\t\t<node id=\"paas\" displayName=\"PaaS\"></node>\n"
                    + "\t\t\t\t<node id=\"devOpsTeam\" displayName=\"DevOps Team\"></node>\n" + "\t\t\t</node>\n"
                    + "\t\t</node>\n" + "\t\t<node id=\"Finance\" displayName=\"Finance\"></node>\n"
                    + "\t\t<node id=\"Admin\" displayName=\"Admin\"></node>\n" + "\t</root>\n" + "</taxonomy>";

        }

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
            topPage="false"
            request="<%=request%>"/>
    <script type="text/javascript">

        function cancelSequence() {
            document.location.href = "taxonomy.jsp?region=region3&item=governance_taxonomy_menu";
        }

        YAHOO.util.Event.onDOMReady(function () {
            editAreaLoader.init({
                id: "payload"        // textarea id
                , syntax: "xml"            // syntax to be uses for highgliting
                , start_highlight: true        // to display with highlight mode on start-up
                , allow_resize: "both"
                , min_height: 250
            });
        });


    </script>


    <div id="middle">
        <h2><fmt:message key="taxonomy.source"/></h2>
        <div id="workArea">
            <form id="life.cycle.source.form" method="post" action="save_taxonomy-ajaxprocessor.jsp">
                <table class="styledLeft" cellspacing="0" cellpadding="0">
                    <thead>
                    <tr>
                        <th>
                            <span style="float: left; position: relative; margin-top: 2px;"><fmt:message
                                    key="taxonomy.source"/></span>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>
                            <textarea id="payload"
                                      style="border: 0px solid rgb(204, 204, 204); width: 99%; height: 275px;
                                      margin-top: 5px;" name="payload" rows="30" class="codepress html linenumbers-on"
                                      wrap="off"><%=temp%></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <% if (!viewMode) { %>
                            <input class="button registryWriteOperation" type="button"
                                   onclick="saveTaxonomy('<%=request.getParameter("taxonomyName")%>',
                                           <%=Boolean.toString(isNew)%>,'false')" value="<fmt:message key="save"/>"/>
                            <input class="button registryNonWriteOperation" type="button" disabled="disabled"
                                   value="<fmt:message key="save"/>"/>
                            <input class="button" type="button" value="<fmt:message key="cancel"/>"
                                   onclick="javascript: cancelSequence(); return false;"/>
                            <% } else { %>
                            <input class="button registryWriteOperation" type="button"
                                   onclick="saveTaxonomy('<%=request.getParameter("taxonomyName")%>',
                                           <%=Boolean.toString(isNew)%>,'false')" value="<fmt:message key="save"/>"/>
                            <input class="button registryNonWriteOperation" type="button" disabled="disabled"
                                   value="<fmt:message key="save"/>"/>
                            <input class="button" type="button" value="<fmt:message key="cancel"/>"
                                   onclick="javascript: cancelSequence(); return false;"/>
                            <% } %>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>

