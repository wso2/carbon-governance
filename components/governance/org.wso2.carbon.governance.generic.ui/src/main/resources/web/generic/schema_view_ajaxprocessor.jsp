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

<%@ page import="org.wso2.carbon.governance.generic.ui.utils.SchemaTreeNodeBuilder" %>
<%@ page import="org.wso2.carbon.registry.common.ui.utils.TreeNode" %>
<%@ page import="org.wso2.carbon.registry.common.ui.utils.UIUtil" %>
<%@ page import="org.wso2.carbon.registry.resource.stub.beans.xsd.ContentBean" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<jsp:include page="../registry_common/registry_common-i18n-ajaxprocessor.jsp"/>
<fmt:bundle basename="org.wso2.carbon.governance.generic.ui.i18n.Resources">
<%
    if(!CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/resources/govern/generic/list")){
        return;
    }
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ResourceServiceClient client;
    ContentBean cb;
    String textContent = null;
    String path = request.getParameter("path");
    TreeNode rootNode = null;
    try {
        client = new ResourceServiceClient(cookie, config, session);
        cb = client.getContent(request);
        if (cb.getCollection()) {
            throw new Exception("The resource custom ui doesn't support wsdl content as a collection.");            
        }
        textContent = client.getTextContent(request);
        SchemaTreeNodeBuilder treeNodeBuilder = new SchemaTreeNodeBuilder(path, textContent);
        treeNodeBuilder.setActualSchemaPath(client.getProperty(path, "registry.actualpath"));
        rootNode = treeNodeBuilder.buildTree();
        
    } catch (Exception e) {
%>
Custom UI doesn't support some of the constructs in your Schema.
Click on the <a onclick="viewStandardContentSection('<%=path%>')">Standard view</a> to view, edit, download or <a onclick="visualizeXML('<%=path%>', 'xsd')">visualize</a> the schema.
<%
        return;
    }
    // now iterate each key and show the values
%>

<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>

<script type="text/javascript" src="../yui/build/treeview/treeview-min.js"></script>

<link rel="stylesheet" type="text/css" href="../generic/css/treeview.css" />
        <div class="yui-skin-sam">

            <!-- markup for expand/contract links -->
            <p style="margin: 10px !important;">
                Click on the <a onclick="viewStandardContentSection('<%=path%>')">Standard view</a> to view, edit, download or <a onclick="visualizeXML('<%=path%>', 'xsd')">visualize</a> the schema.
            </p>
            <br/>
            <div id="expandcontractdiv" style="margin-bottom:10px;">
                <a id="expand" href="#"><img src="../generic/images/expandall.gif" align="top" /> Expand all</a>
                <a id="collapse" href="#" style="margin-left:20px"><img src="../generic/images/contractall.gif" align="top" /> Collapse all</a>
            </div>

            <div id="treeDiv1"></div>
            <p style="margin: 10px !important;">
                Click on the <a onclick="viewStandardContentSection('<%=path%>')">Standard view</a> to view, edit, download or <a onclick="visualizeXML('<%=path%>', 'xsd')">visualize</a> the schema.
            </p>
            <br/>
        </div>


    <script type="text/javascript" src="../generic/js/TaskNode.js"></script>


    <script type="text/javascript">

        var tree;
        var nodes = [];
        var nodeIndex;

        function treeInit() {
            buildSchemaNodeTree();
        }

        //handler for expanding all nodes
        YAHOO.util.Event.on("expand", "click", function(e) {
            tree.expandAll();
            YAHOO.util.Event.preventDefault(e);
        });

        //handler for collapsing all nodes
        YAHOO.util.Event.on("collapse", "click", function(e) {
            tree.collapseAll();
            YAHOO.util.Event.preventDefault(e);
        });
        function buildSchemaNodeTree() {

           //instantiate the tree:
           tree = new YAHOO.widget.TreeView("treeDiv1");
           tree.checked = true;


         <% UIUtil.printNodesOfTree(rootNode, "tree.getRoot()", 0, out); %>

          // Expand and collapse happen prior to the actual expand/collapse,
          // and can be used to cancel the operation
          tree.subscribe("expand", function(node) {// return false to cancel the expand
              });

          tree.subscribe("collapse", function(node) { });
          tree.draw();
       }

        YAHOO.util.Event.onDOMReady(treeInit);
     </script>


</fmt:bundle>
