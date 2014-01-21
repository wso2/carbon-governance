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

<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.registry.relations.ui.clients.RelationServiceClient" %>
<%@ page import="org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.Utils" %>

<%
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        boolean hasDependencies;
        String _path = request.getParameter("path");
        try {
            RelationServiceClient relationClient = new RelationServiceClient(cookie,config,session);
            DependenciesBean dependenciesBean = relationClient.getDependencies(request);
            hasDependencies = Utils.hasDependencies(dependenciesBean,_path);
            String res = "{" + Utils.getResourceDownloadURL(request, _path)+"**"+ String.valueOf(hasDependencies) + "}";
             %><%=res.trim()%> <%
        }
         catch (Exception e) {
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



