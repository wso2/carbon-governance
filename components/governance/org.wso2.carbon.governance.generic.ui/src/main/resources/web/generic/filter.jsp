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
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.GenericUIGenerator" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.UIGeneratorConstants" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.Iterator" %>
<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<fmt:bundle basename="org.wso2.carbon.governance.generic.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.JSResources"
            request="<%=request%>" namespace="org.wso2.carbon.governance.generic.ui"/>
    <carbon:breadcrumb
            label="filter.list"
            resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>" />
<script type="">
  sessionAwareFunction(null, null, "<fmt:message key="session.timed.out"/>");
</script>      
<%
    GenericUIGenerator gen = new GenericUIGenerator();
    ManageGenericArtifactServiceClient client = new ManageGenericArtifactServiceClient(config,session);
    OMElement uiconfig = gen.getUIConfiguration(client.getArtifactUIConfiguration(request.getParameter("key")),request,config,session);
    Iterator widgets = uiconfig.getChildrenWithName(new QName(null,UIGeneratorConstants.WIDGET_ELEMENT));
    StringBuffer table = new StringBuffer();
    while(widgets.hasNext()){
        OMElement widget = (OMElement)widgets.next();
        table.append(gen.printWidgetWithValues(widget, null, true, false, false, request, config));
    }

    String[] unboundedNameList = gen.getUnboundedNameList(uiconfig);
    String[] unboundedWidgetList = gen.getUnboundedWidgetList(uiconfig);
    String[][] unboundedValues = gen.getUnboundedValues(uiconfig, request, config);
    String[][] dateIdAndNameList = gen.getDateIdAndNameList(uiconfig, null, false);

%>
<br/>
<script type="text/javascript">

    function submitFilterForm() {
        sessionAwareFunction(function() {
            var advancedSearchForm = $('filterForm');
            advancedSearchForm.submit();
        }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
    }

    function clearAll(){
        var table = $('#_innerTable');
        var Inputrows = table.getElementsByTagName('input');

        for (var i = 0; i < Inputrows.length; i++) {
            if (Inputrows[i].type == "text") {
                Inputrows[i].value = "";
            } else if (Inputrows[i].type == "checkbox") {
                Inputrows[i].checked = false;
            }
        }

        var TextAreas = table.getElementsByTagName('textarea');
        for (var i = 0; i < TextAreas.length; i++) {
            TextAreas[i].value = "";
        }
        var SelectAreas = table.getElementsByTagName('select');
        for (var i = 0; i < SelectAreas.length; i++) {
            SelectAreas[i].selectedIndex = 0;
        }
    }
    
    jQuery(document).ready(function() {
        <%
            // date fields are loaded with jquery datepicks on JS page load
        for (int i=0; i<dateIdAndNameList.length; ++i) { %>
            jQuery('#<%=dateIdAndNameList[i][0]%>').datepicker();
        <%}
        %>
    });

    <%
       if(unboundedNameList != null && unboundedWidgetList != null && unboundedValues != null){
       for(int i=0;i<unboundedNameList.length;i++){%>
        <%=unboundedNameList[i]%>Count = 0;
        jQuery(document).ready(function() {
            var countTracker = document.getElementById("<%=unboundedNameList[i]%>CountTaker");
            if (countTracker != null && countTracker.value) {
                <%=unboundedNameList[i]%>Count = parseInt(countTracker.value);
            }
        });

        function add<%=unboundedNameList[i]%>_<%=unboundedWidgetList[i]%>(inputParam){
           
        <%String[] valueList = unboundedValues[i];%>
            var epOptions = '<%for(int j=0;j<valueList.length;j++){%><option value="<%=valueList[j]%>"><%=valueList[j]%></option><%}%>';
            var endpointMgt = document.getElementById('<%=unboundedNameList[i]%>Mgt');
            endpointMgt.parentNode.style.display = "";
            if(<%=unboundedNameList[i]%>Count >0){
                for(var i=1;i<=<%=unboundedNameList[i]%>Count;i++){
                    var endpoint = document.getElementById('id_<%=unboundedWidgetList[i].replaceAll(" ","_") + "_" + unboundedNameList[i].replaceAll(" ","-")%>' + i);
                    if(endpoint == null || endpoint.value == ""){
                        return;
                    }
                }
            }
            <%=unboundedNameList[i]%>Count++;
            var epCountTaker = document.getElementById('<%=unboundedNameList[i]%>CountTaker');
            epCountTaker.value = <%=unboundedNameList[i]%>Count;
            var theTr = document.createElement("TR");
            var theTd1 = document.createElement("TD");
            var theTd2 = document.createElement("TD");
            var td1Inner = '<select name="<%=(unboundedWidgetList[i].replaceAll(" ","_") + "_" + unboundedNameList[i].replaceAll(" ","-"))%>'+<%=unboundedNameList[i]%>Count+'">' + epOptions + '</select>';
            var selectResource = "";
            if (inputParam == "path") {
                selectResource = ' <input type="button" class="button" value=".." title="<fmt:message key="select.path"/>" onclick="showGovernanceResourceTree(\'' + <%=unboundedNameList[i]%>Count + '\');"/>';
            }
            var td2Inner = '<input id="id_<%=unboundedWidgetList[i].replaceAll(" ","_") + "_" + unboundedNameList[i].replaceAll(" ","-")%>'+<%=unboundedNameList[i]%>Count+'" type="text" name="<%=unboundedWidgetList[i].replaceAll(" ","-") + UIGeneratorConstants.TEXT_FIELD + "_" + unboundedNameList[i].replaceAll(" ","-")%>'+<%=unboundedNameList[i]%>Count+'" style="width:400px"/>' + selectResource;

            theTd1.innerHTML = td1Inner;
            theTd2.innerHTML = td2Inner;

            theTr.appendChild(theTd1);
            theTr.appendChild(theTd2);

            endpointMgt.appendChild(theTr);


        }
<%      }

   }%>
   
   <%=gen.getUnboundedWidgets(uiconfig, request,config)%>
  
</script>
<div id="middle">
<h2><fmt:message key="filter.artifacts"><fmt:param value="<%=request.getParameter("pluralLabel")%>"/></fmt:message></h2>
<div id="workArea">
            <p style="padding:5px">
                <fmt:message key="filter.artifacts.description"><fmt:param value="<%=request.getParameter("singularLabel")%>"/></fmt:message>
            </p>
            <div id="activityReason" style="display: none;"></div>
            <form id="filterForm" action="filter_ajaxprocessor.jsp"
                  onsubmit="return submitFilterForm();" method="post">
                <input type="hidden" name="dataName" value="<%=request.getParameter("dataName")%>"/>
                <input type="hidden" name="singularLabel" value="<%=request.getParameter("singularLabel")%>"/>
                <input type="hidden" name="pluralLabel" value="<%=request.getParameter("pluralLabel")%>"/>
                <input type="hidden" name="dataNamespace" value="<%=request.getParameter("dataNamespace")%>">
                <input type="hidden" name="key" value="<%=request.getParameter("key")%>">
                <input type="hidden" name="region" value="<%=request.getParameter("list_region")%>">
                <input type="hidden" name="item" value="<%=request.getParameter("list_item")%>">
                <input type="hidden" name="breadcrumb" value="<%=request.getParameter("list_breadcrumb")%>">
                <table class="styledLeft" id="#_innerTable">
                    <tr><td>
                        <%=table.toString()%>
                    </td></tr>
                    <tr id="buttonRow">
                        <td class="buttonRow">
                            <input class="button" type="button"
                                   onclick="submitFilterForm()"
                                   value="<fmt:message key="filter.list"/>" />
                            <input type="button" id="#_1" value="<fmt:message key="clear"/>" class="button"
                   onclick="clearAll()"/>
                        </td>
                    </tr>
                    <tr id="waitMessage" style="display:none">
                        <td>
                            <div style="font-size:13px !important;margin-top:10px;margin-bottom:10px;margin-left:5px !important" id="artifactLoader" class="ajax-loading-message">
                            </div>
                        </td>
                    </tr>

                </table>
            </form>
            <br/>
</div>
</div>
</fmt:bundle>
