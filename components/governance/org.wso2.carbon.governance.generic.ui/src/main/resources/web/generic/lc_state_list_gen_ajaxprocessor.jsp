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
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>

<%
    ManageGenericArtifactServiceClient client = new ManageGenericArtifactServiceClient(config, session);
    String LCName;
    LCName = request.getParameter("LCName");
    String State = request.getParameter("LCState");
    String[] LCStates;
    LCStates = client.getAllLifeCycleState(LCName);
    String statesHtml = "<option " + ((State == null || State.equals("0")) ? "selected" : "") + " value=\"0\">Any State</option>";

    for (String state : LCStates) {
        statesHtml = statesHtml + "<option " + ((State != null && State.equals(state)) ? "selected" : "") + "  value=\"" + Encode.forHtml(state) + "\">" + Encode.forHtml(state) + "</option>";
    }
    out.write(statesHtml);
%>