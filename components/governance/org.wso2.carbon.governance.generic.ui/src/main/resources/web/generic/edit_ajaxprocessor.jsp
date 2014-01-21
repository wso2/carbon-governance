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
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.GenericUtil" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.UIGeneratorConstants" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="javax.xml.namespace.QName" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../generic/js/collapsible_menu_util.js"></script>

<fmt:bundle basename="org.wso2.carbon.governance.generic.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.governance.generic.ui.i18n.JSResources"
        request="<%=request%>" namespace="org.wso2.carbon.governance.generic.ui"/>
<%
    boolean isBrowseOnly = false;
    if(!CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/govern/generic/add")){
        if(CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/browse")){
            isBrowseOnly=true;
        }else{
            return;
        }
    }
    String dataName = request.getParameter("dataName");
    if (dataName == null) {
        dataName = "metadata";
    }
    String dataNamespace = request.getParameter("dataNamespace");
    if (dataNamespace == null) {
        dataNamespace = UIGeneratorConstants.DATA_NAMESPACE;
    }
    String breadcrumb = request.getParameter("breadcrumb");
    if (breadcrumb == null) {
        breadcrumb = "Artifact";
    }
    String type = request.getParameter("type");
    ManageGenericArtifactServiceClient
            client = new ManageGenericArtifactServiceClient(config,session);

    String path = request.getParameter("path");
    String relativePath = path;
    if (relativePath.startsWith(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)) {
        relativePath =
                relativePath.substring(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length());
    }
    boolean isVersionedPage = false;
    String disabledAttr = "";
    String saveButtonClass = "button registryWriteOperation";
    if (relativePath.contains(";version")) {
        isVersionedPage = true;
        disabledAttr = " disabled=\"1\"";
        saveButtonClass = "button registryNonWriteOperation";
    }

    String content = client.getArtifactContent(relativePath);
    if(content ==null){
        return;
    }
    OMElement data = GenericUtil.loadAddedServiceContent(content);
    GenericUIGenerator gen = new GenericUIGenerator(dataName, dataNamespace);
    OMElement uiconfig = gen.getUIConfiguration(client.getArtifactUIConfiguration(
            request.getParameter("key")),request,config,session);
    request.setAttribute("content",data);
    Iterator widgets = uiconfig.getChildrenWithName(new QName(null, UIGeneratorConstants.WIDGET_ELEMENT));
    StringBuffer table = new StringBuffer();
    while(widgets.hasNext()){
        OMElement widget = (OMElement)widgets.next();
        String widgetText = gen.printWidgetWithValues(widget, data, false, true, true, request, config).replace("\n", "<!--LF-->").replace("\r", "<!--CR-->");
        table.append(widgetText.replace("<!--LF-->", "\n").replace("<!--CR-->", "\r"));
    }

    List<Map> validatationAttributes = gen.getValidationAttributes(uiconfig);
    String[] mandatory = gen.getMandatoryIdList(uiconfig);
    String[] name = gen.getMandatoryNameList(uiconfig);
    String[] unboundedNameList = gen.getUnboundedNameList(uiconfig);
    String[] unboundedWidgetList = gen.getUnboundedWidgetList(uiconfig);
    String[] unboundedTooltipList = gen.getUnboundedTooltipList(uiconfig);
    String[][] unboundedValues = gen.getUnboundedValues(uiconfig, request, config);
    String[][] dateIdAndNameList = gen.getDateIdAndNameList(uiconfig, data, true);
%>

<br/>
<script type="text/javascript">

    jQuery(document).ready(
            function() {
                initCollapseMenu("#_addEditTable");
            }
    );
</script>

<script type="text/javascript">
    function addEditArtifact(){
        sessionAwareFunction(function() {
            getArtifactName();
            var reason = "";
        <%for(int i=0;i<mandatory.length;i++){%>
            reason += validateEmpty(document.getElementById('<%=mandatory[i]%>'),
                        "<%=name[i]%>");
        <%}%>

            <%
                //validate date fields
                for (int i=0; i<dateIdAndNameList.length; ++i) { %>
                if (document.getElementById('<%=dateIdAndNameList[i][0]%>') != null) {
            reason
                    += validateDate(document.getElementById('<%=dateIdAndNameList[i][0]%>'),
                    "<%=dateIdAndNameList[i][1]%>");
                }
            <%}
            %>

            var eleArr = null, ele = null;
            <%
                for (int i=0; i<validatationAttributes.size(); i++) {
                    Map<String, Object> map = validatationAttributes.get(i);
                    String prop = (String)map.get("properties");
                    String eleName = (String)map.get("name");
                    List<String> eleIds = (List<String>)map.get("ids");
                    String regexp = StringEscapeUtils.escapeJavaScript((String)map.get("regexp"));

                    if (prop != null && "unbounded".equals(prop)) {  %>

            for (var i= 0, len = parseInt(document.getElementsByName('<%=eleName.replaceAll(" ",
            "-") + UIGeneratorConstants.COUNT%>')[0].value); i<len; ++i) {
                eleArr = new Array(); <%
                for (int j=0; j<eleIds.size(); ++j) { %>
                ele = document.getElementById('<%=eleIds.get(j)%>' + (i+1));
                if (ele == undefined || ele == null) {
                    ele = document.getElementsByName('<%=eleIds.get(j)%>' + (i+1))[0];
                }
                eleArr.push(ele);
            <%} %>
                reason += validateRegex("<%=regexp%>", eleArr, "<%=eleName%>" + (i+1));
            }

            <%  } else { %>
            eleArr = new Array(); <%
                for (int j=0; j<eleIds.size(); ++j) { %>
            ele = document.getElementById('<%=eleIds.get(j)%>');
            if (ele == undefined || ele == null) {
                ele = document.getElementsByName('<%=eleIds.get(j)%>')[0];
            }
            eleArr.push(ele);
            <%} %>
            reason += validateRegex("<%=regexp%>", eleArr, "<%=eleName%>");

            <%}
               }
            %>

            var CustomUIForm=document.getElementById('CustomUIForm');
            var waitMessage = document.getElementById('waitMessage');
            var buttonRow = document.getElementById('buttonRow');
            if(reason!=""){
                CARBON.showWarningDialog(reason);
            }else{
                buttonRow.style.display = "none";
                waitMessage.style.display = "";
                CustomUIForm.submit();
            }
        }, org_wso2_carbon_governance_generic_ui_jsi18n["session.timed.out"]);
    }
    function getArtifactName(){
        var artifactLoader= document.getElementById('artifactLoader');
        artifactLoader.innerHTML='<img src="images/ajax-loader.gif" align="left" hspace="20"/><fmt:message key="please.wait.saving.details.for"/> '+'<%=breadcrumb%>'+'...';
    }

    jQuery(document).ready(function() {
    <%
    // date fields are loaded with jquery datepicks on JS page load
    for (int i=0; i<dateIdAndNameList.length; ++i) { %>
        jQuery('#<%=dateIdAndNameList[i][0]%>').datepicker();
    <%}%>
    });

    <%
       if(unboundedNameList != null && unboundedWidgetList != null && unboundedValues != null){
       for(int i=0;i<unboundedNameList.length;i++){%>

        function delete<%=unboundedNameList[i].replaceAll(" ", "")%>_<%=unboundedWidgetList[i].replaceAll(" ", "")%>(index) {
            var endpointMgt = document.getElementById('<%=unboundedNameList[i].replaceAll(" ", "")%>Mgt');
            endpointMgt.parentNode.style.display = "";
            endpointMgt.parentNode.deleteRow(index);

            var table = endpointMgt.parentNode;
            var rows = table.getElementsByTagName("input");

            if (rows != null & rows.length == 0) {
                endpointMgt.parentNode.style.display = "none";
            }
        }
        function add<%=unboundedNameList[i].replaceAll(" ", "")%>_<%=unboundedWidgetList[i].replaceAll(" ", "")%>(inputParam,startWith){
           
        <%String[] valuelist = unboundedValues[i];%>
            var epOptions = '<%for(int j=0;j<valuelist.length;j++){%><option value="<%=valuelist[j]%>"><%=valuelist[j]%></option><%}%>';
            var endpointMgt = document.getElementById('<%=unboundedNameList[i]%>Mgt');
            endpointMgt.parentNode.style.display = "";
            var table = endpointMgt.parentNode;
            var rows = table.getElementsByTagName("input");

            if (rows.length > 0) {
                for (var i = 0; i < rows.length; i++) {
                    var endpoint = rows[i];
                    if (endpoint != null & endpoint.value == "") {
                        return;
                    }
                }
            }
            var epCountTaker = document.getElementById('<%=unboundedNameList[i].replaceAll(" ", "")%>CountTaker');
            var <%=unboundedNameList[i].replaceAll(" ", "")%>Count = parseInt(epCountTaker.value);
            <%=unboundedNameList[i].replaceAll(" ", "")%>Count++;
            epCountTaker.value = "" + <%=unboundedNameList[i].replaceAll(" ", "")%>Count;
            var theTr = document.createElement("TR");
            var theTd1 = document.createElement("TD");
            var theTd2 = document.createElement("TD");
            var theTd3 = document.createElement("TD");
            var td1Inner = '<select name="<%=(unboundedWidgetList[i].replaceAll(" ","") + "_" + unboundedNameList[i].replaceAll(" ",""))%>'+<%=unboundedNameList[i].replaceAll(" ", "")%>Count+
                    '" title="<%=unboundedTooltipList[i]%>" >' + epOptions + '</select>';
            var selectResource = "";
            if (inputParam == "path") {
            	if(startWith !=null){
            		selectResource = ' <input type="button" class="button" value=".." title="<fmt:message key="select.path"/>" onclick="showGovernanceResourceTreeWithCustomPath(\'id_<%=unboundedWidgetList[i].replaceAll(" ","") + "_" + unboundedNameList[i].replaceAll(" ","")%>' + <%=unboundedNameList[i].replaceAll(" ", "")%>Count + "'" +  "," + "'" + startWith + '\');"/>';
            	} else {
            		selectResource = ' <input type="button" class="button" value=".." title="<fmt:message key="select.path"/>" onclick="showGovernanceResourceTree(\'id_<%=unboundedWidgetList[i].replaceAll(" ","") + "_" + unboundedNameList[i].replaceAll(" ","")%>'+<%=unboundedNameList[i].replaceAll(" ", "")%>Count+'\');"/>';
            	}
                
            }
            var td2Inner = '<input id="id_<%=unboundedWidgetList[i].replaceAll(" ","") + "_" + unboundedNameList[i].replaceAll(" ","")%>'+<%=unboundedNameList[i].replaceAll(" ", "")%>Count+'" type="text" name="<%=unboundedWidgetList[i].replaceAll(" ","") + UIGeneratorConstants.TEXT_FIELD + "_" + unboundedNameList[i].replaceAll(" ","")%>'+<%=unboundedNameList[i].replaceAll(" ", "")%>Count
                    +'" style="width:400px" title="<%=unboundedTooltipList[i]%>"/>' +
                    selectResource;
            var td3Inner = '<a class="icon-link" title="delete" onclick="delete<%=unboundedNameList[i].replaceAll(" ", "")%>_<%=unboundedWidgetList[i].replaceAll(" ", "")%>(this.parentNode.parentNode.rowIndex)" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

            theTd1.innerHTML = td1Inner;
            theTd2.innerHTML = td2Inner;
            <%--Setting the default width to fix alignment problems--%>
            theTd2.width="500px";
            theTd3.innerHTML = td3Inner;

            theTr.appendChild(theTd1);
            theTr.appendChild(theTd2);
            theTr.appendChild(theTd3);

            endpointMgt.appendChild(theTr);


        }
<%      }

   }%>
   
   <%=gen.getUnboundedWidgets(uiconfig, request, config)%>

</script>

<div id="middle">


    <div id="workArea">

        <div id="activityReason" style="display: none;"></div>
        <form id="CustomUIForm" action="../generic/add_ajaxprocessor.jsp" method="post">
            <input type="hidden" name="add_edit_operation" value="edit">
            <input type="hidden" name="path" value="<%=path%>">
                <input type="hidden" name="dataName" value="<%=dataName%>"/>
                <input type="hidden" name="dataNamespace" value="<%=dataNamespace%>"/>
                <input type="hidden" name="region" value="<%=request.getParameter("add_edit_region")%>"/>
                <input type="hidden" name="item" value="<%=request.getParameter("add_edit_item")%>"/>
                <input type="hidden" name="key" value="<%=request.getParameter("key")%>"/>
                <input type="hidden" name="breadcrumb" value="<%=request.getParameter("add_edit_breadcrumb")%>"/>
                <input type="hidden" name="lifecycleAttribute" value="<%=request.getParameter("lifecycleAttribute")%>"/>
                <input type="hidden" name="currentPath" value="<%=(request.getParameter("path") != null ? request.getParameter("path") : "")%>"/>
            <table class="styledLeft" id="#_addEditTable">
                <tr><td>
                    <%=table.toString()%>
                </td></tr>
                <% if(client.canChange(path)){%>
                <tr id="buttonRow">
                    <td colspan="3" class="buttonRow">
                        <%
                            if ((type == null || type.equals("collection"))) {
                                if(!isBrowseOnly){
                        %>
                        <input class="<%=saveButtonClass%>" type="button" <%=disabledAttr%>
                               value="<fmt:message key="save.artifact"><fmt:param value="<%=breadcrumb%>"/></fmt:message>" onclick="addEditArtifact()"/>
                        <%
                                }
                            }
                        %>
                    </td>
                </tr>
                <tr id="waitMessage" style="display:none">
                    <td colspan="3">
                        <div style="font-size:13px !important;margin-top:10px;margin-bottom:10px;" id="artifactLoader">
                        </div>
                    </td>
                </tr>
            <%}%>
            </table>
        </form>
        <br/>

        <div id="AddArtifact">
        </div>
    </div>
</div>
</fmt:bundle>
