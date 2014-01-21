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
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.governance.services.ui.utils.AddServicesUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.governance.services.ui.utils.AddServiceUIGenerator" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.governance.services.ui.utils.UIGeneratorConstants" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="org.wso2.carbon.governance.services.ui.clients.AddServicesServiceClient" %>
<script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<jsp:include page="../list/list-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../list/js/list.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<fmt:bundle basename="org.wso2.carbon.governance.list.ui.i18n.Resources">
<carbon:breadcrumb
            label="filter.services.menu.text"
            resourceBundle="org.wso2.carbon.governance.list.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>" />
<script type="">
  sessionAwareFunction(null, null, "<fmt:message key="session.timed.out"/>");
</script>      
<%
    String error = "Wrong configuration in " + RegistryConstants.GOVERNANCE_SERVICES_CONFIG_PATH;
    AddServiceUIGenerator gen = new AddServiceUIGenerator();
    AddServicesServiceClient client = new AddServicesServiceClient(config,session);
    OMElement head = gen.getUIConfiguration(client.getServiceConfiguration(),request,config,session);

    OMElement uiconfig = gen.getUIConfiguration(client.getServiceConfiguration(),request,config,session);
    Iterator widgets = head.getChildrenWithName(new QName(null,UIGeneratorConstants.WIDGET_ELEMENT));
    StringBuffer table = new StringBuffer();
    while(widgets.hasNext()){
        OMElement widget = (OMElement)widgets.next();
        table.append(gen.printWidgetWithValues(widget, null, true, false, false, request, config));
    }

    String[] addname = gen.getUnboundedNameList(uiconfig);
    String[] addwidget = gen.getUnboundedWidgetList(uiconfig);
    String[][] selectvaluelist = gen.getUnboundedValues(uiconfig, request, config);

%>
<br/>
<script type="text/javascript">

    <%
       if(addname != null && addwidget != null && selectvaluelist != null){
       for(int i=0;i<addname.length;i++){%>
        <%=addname[i]%>Count = 0;
        jQuery(document).ready(function() {
            var countTracker = document.getElementById("<%=addname[i]%>CountTaker");
            if (countTracker != null && countTracker.value) {
                <%=addname[i]%>Count = parseInt(countTracker.value);
            }
        });

        function add<%=addname[i]%>_<%=addwidget[i]%>(inputParam){
        <%String[] valuelist = selectvaluelist[i];%>
            var epOptions = '<%for(int j=0;j<valuelist.length;j++){%><option value="<%=valuelist[j]%>"><%=valuelist[j]%></option><%}%>';
            var endpointMgt = document.getElementById('<%=addname[i]%>Mgt');
            endpointMgt.parentNode.style.display = "";
            if(<%=addname[i]%>Count >0){
                for(var i=1;i<=<%=addname[i]%>Count;i++){
                    var endpoint = document.getElementById('id_<%=addwidget[i].replaceAll(" ","_") + "_" + addname[i].replaceAll(" ","-")%>' + i);
                    if(endpoint == null || endpoint.value == ""){
                        return;
                    }
                }
            }
            <%=addname[i]%>Count++;
            var epCountTaker = document.getElementById('<%=addname[i]%>CountTaker');
            epCountTaker.value = <%=addname[i]%>Count;
            var theTr = document.createElement("TR");
            var theTd1 = document.createElement("TD");
            var theTd2 = document.createElement("TD");
            var td1Inner = '<select name="<%=(addwidget[i].replaceAll(" ","_") + "_" + addname[i].replaceAll(" ","-"))%>'+<%=addname[i]%>Count+'">' + epOptions + '</select>';
            var selectResource = "";
            if (inputParam == "path") {
                selectResource = ' <input type="button" class="button" value=".." title="<fmt:message key="select.path"/>" onclick="showGovernanceResourceTree(\''+ <%=addname[i]%>Count +'\');"/>';
            }
            var td2Inner = '<input id="id_<%=addwidget[i].replaceAll(" ","_") + "_" + addname[i].replaceAll(" ","-")%>'+<%=addname[i]%>Count+'" type="text" name="<%=addwidget[i].replaceAll(" ","-") + UIGeneratorConstants.TEXT_FIELD + "_" + addname[i].replaceAll(" ","-")%>'+<%=addname[i]%>Count+'" style="width:400px"/>' + selectResource;

            theTd1.innerHTML = td1Inner;
            theTd2.innerHTML = td2Inner;

            theTr.appendChild(theTd1);
            theTr.appendChild(theTd2);

            endpointMgt.appendChild(theTr);


        }
<%      }

   }%>
  
</script>

<script type="text/javascript">
    function isEnter(e) {
     e = e || window.event || {};
     var charCode = e.charCode || e.keyCode || e.which;
        if (charCode == 13) {
            submitFilterForm();  
        }
    }
</script>

<div id="middle">
<h2><fmt:message key="filter.services"/></h2>
<div id="workArea">
<%
    if(table != null){
%>
            <p style="padding:5px">
                <fmt:message key="filter.services.description"/>
            </p>
            <div id="activityReason" style="display: none;"></div>
            <form id="filterForm" action="service_filter_ajaxprocessor.jsp"
                  onsubmit="return submitFilterForm();" method="post" onkeydown="isEnter(event);">
                <input type="hidden" name="operation" value="Add"/>
                <input type="hidden" name="currentname" value="">
                <input type="hidden" name="currentnamespace" value="">
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
                            <div style="font-size:13px !important;margin-top:10px;margin-bottom:10px;margin-left:5px !important" id="serviceLoader" class="ajax-loading-message">
                            </div>
                        </td>
                    </tr>

                </table>
            </form>
            <br/>

    <%}%>
</div>
</div>
</fmt:bundle>
