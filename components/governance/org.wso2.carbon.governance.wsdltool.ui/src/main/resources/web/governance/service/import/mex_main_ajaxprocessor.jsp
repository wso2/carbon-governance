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
<%@ page import="org.wso2.carbon.registry.samples.ui.custom.topics.beans.EndpointBean" %>
<%@ page import="org.wso2.carbon.registry.samples.ui.custom.topics.utils.GetEndpointUtil" %>
<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>
<%@ page import="org.wso2.carbon.registry.common.ui.UIException" %>
<%
    String cPath = RegistryUtil.getPath(request);
    EndpointBean bean;
    try {
        bean = GetEndpointUtil.getEndpointBean(cPath, config, session);
    } catch (UIException e) {
        %>Error occured while retrieving endpoint details<%
        return;
    }
%>

<br/>

<h3>Endpoint</h3>

<table width="50%">
    <tr>
        <td>Name</td>
        <td><%=bean.getName()%></td>
    </tr>
    <tr>
        <td>URI</td>
        <td><%=bean.getUri()%></td>
    </tr>
    <tr>
        <td>Format</td>
        <td><%=bean.getFormat()%></td>
    </tr>
    <tr>
        <td>Optimization method</td>
        <td><%=bean.getOptimize()%></td>
    </tr>
    <tr>
        <td>Duration to suspend this endpoint on failure</td>
        <td><%=bean.getSuspendDurationOnFailure()%></td>
    </tr>
</table>

<a onclick="loadViewUI('../registry/custom/endpoint/epr_edit_ajaxprocessor.jsp', '<%=cPath%>')">Edit endpoint</a>

<br/>