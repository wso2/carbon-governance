<%--
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
 --%>

<%@page import="org.wso2.carbon.governance.gadgets.ui.ResourceImpactDataProcesssor"%>
<%@ page import="org.wso2.carbon.governance.gadgets.ui.GovImpactAnalysisDataProcessor"%>
<%@ page import="org.wso2.carbon.governance.gadgets.ui.GadgetSourceServiceDataProcessor" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Random" %>
<%@ page import="org.wso2.carbon.governance.gadgets.ui.ProjectDataProcessor" %>

<%
    String funcName = request.getParameter("funcName");
    Random randomGenerator = new Random();

    String DATE_FORMAT = "yyyy-MM-dd";
    java.text.SimpleDateFormat sdf =
            new java.text.SimpleDateFormat(DATE_FORMAT);
    Calendar c1 = Calendar.getInstance();
    c1.add(Calendar.DATE, -1);
    String date = sdf.format(c1.getTime());

    if ("pgauge_data".equals(funcName)) {
        out.print("&value=38");
    } else if ("dgauge_data".equals(funcName)) {
        out.print("&value=60&hist=28");
    } else if ("meter_data".equals(funcName)) {
        int randomInt = randomGenerator.nextInt(1000);
        out.print("&value=" + randomInt + "&range=1000");
    } else if ("iogauge_data".equals(funcName)) {
        int randomInt = randomGenerator.nextInt(500);
        int randomInt2 = randomGenerator.nextInt(500);
        out.print("&in_range=500&out_range=500&in_value=" + randomInt + "&out_value=" + randomInt2 +
                "&avg_in_value=160&avg_out_value=230");
    } else if ("digital_data".equals(funcName)) {
        int randomInt = randomGenerator.nextInt(500);
        int randomInt2 = randomGenerator.nextInt(500);
        out.print("&value_a=" + randomInt + "&value_b=" + randomInt2);
    } else if ("digital_data2".equals(funcName)) {
        int randomInt = 45675;
        int randomInt2 = 54600;
        out.print("&value_a=" + randomInt + "&value_b=" + randomInt2);
    } else if ("status_data".equals(funcName)) {
        out.print("&value=1");
    } else if ("temp_data".equals(funcName)) {
        out.print("&value=75&range=100");
    } else if ("pmeter_data".equals(funcName)) {
        out.print("&value=-45&range=100");
    } else if ("bar_data".equals(funcName)) {
        out.print("&value=70&range=90");
    } else if ("res_data".equals(funcName)) {
        int randomInt = randomGenerator.nextInt(90);
        out.print("&value=" + randomInt + "&range=90");
    } else if ("min_max_data".equals(funcName)) {
        String data =
                "&title=Min Max Processing Times,{font-size:20px; color: #FFFFFF; margin: 5px; background-color: #505050; padding:5px; padding-left: 20px; padding-right: 20px;}&\n" +
                        "&bg_colour=#ffffff&\n" +
                        "&x_axis_steps=1&\n" +
                        "&x_axis_3d=1&\n" +
                        "&y_legend=Time (100x ms),12,#736AFF&\n" +
                        "&y_ticks=5,10,5&\n" +
                        "&x_labels=" + date + "&\n" +
                        "&y_min=0&\n" +
                        "&y_max=10&\n" +
                        "&x_axis_colour=#909090&\n" +
                        "&x_grid_colour=#ADB5C7&\n" +
                        "&y_axis_colour=#909090&\n" +
                        "&y_grid_colour=#ADB5C7&\n" +
                        "&bar_3d=75,#D54C78,Min,10&\n" +
                        "&values=2&\n" +
                        "&bar_3d_2=75,#3334AD,Max,10&\n" +
                        "&values_2=6&";
        out.print(data);
    } else if (funcName.indexOf("lifeCycleNames") > -1) {
        // Retrieving lifecycle data
        GadgetSourceServiceDataProcessor dataProcessor =
                new GadgetSourceServiceDataProcessor(config, session, request);
        String data = dataProcessor.getLifeCycles();
        out.print(data);
    } else if (funcName.indexOf("lifeCyclePieChartData") > -1)	{
    	GadgetSourceServiceDataProcessor dataProcessor =
            new GadgetSourceServiceDataProcessor(config, session, request);
    	String name = request.getParameter("name");
        String data = dataProcessor.getLifeCyleDataforPieChart(name);
        out.print(data);
    
    } else if (funcName.indexOf("lastminuterequestcount") > -1) {
//        int serviceID = Integer.parseInt(request.getParameter("serviceID"));
//
//        BAMDataServiceDataProcessor BAMDataProcessor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = BAMDataProcessor.getLastMinuteRequestCount(serviceID);
//        out.print(data);

    } else if ("getBackendServerUrl".equals(funcName)) {
        String data = CarbonUIUtil.getServerURL(config.getServletContext(), session);

        // Remove Unnecessary stuff
        data = data.split("/services/")[0];

        out.print(data);
    }
//    } else if (funcName.indexOf("lastminuterequestcountsystem") > -1) {
//        out.print("TODO - Dumindu");
////        BAMDataServiceDataProcessor BAMDataProcessor =
////                new BAMDataServiceDataProcessor(config, session, request);
////        String data = BAMDataProcessor.getLastMinuteRequestCountSystem();
////        out.print(data);
//    } else if (funcName.indexOf("getServerList") > -1) {
//        BAMDataServiceDataProcessor BAMDataProcessor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = BAMDataProcessor.getServerList();
//        out.print(data);
//    } else if (funcName.indexOf("getServicesList") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        BAMDataServiceDataProcessor BAMDataProcessor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = BAMDataProcessor.getServicesList(serverID);
//        out.print(data);
//    } else if (funcName.indexOf("getminmaxaverageresptimessystem") > -1) {
//        /*int serverID = Integer.parseInt(request.getParameter("serverID"));
//
//        BAMDataServiceDataProcessor BAMDataProcessor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = BAMDataProcessor.getMinMaxAverageRespTimesSystem(serverID);
//        out.print(data);*/
//    } else if (funcName.indexOf("getminmaxaverageresptimesservice") > -1) {
//        int serviceID = Integer.parseInt(request.getParameter("serviceID"));
//
//        BAMDataServiceDataProcessor BAMDataProcessor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = BAMDataProcessor.getMinMaxAverageRespTimesService(serviceID);
//        out.print(data);
//    } else if (funcName.indexOf("getAvgResponseTime") > -1) {
//        int serviceID = Integer.parseInt(request.getParameter("serviceID"));
//
//        BAMDataServiceDataProcessor BAMDataProcessor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = BAMDataProcessor.getAvgResponseTime(serviceID);
//        out.print(data);
//    } else if (funcName.indexOf("getEndpointInvokeCount") > -1) {
//        //out.print(request.getParameter("serverID"));
//        //out.print(request.getParameter("endpoint"));
///*        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String endpoint = request.getParameter("endpoint");
//
//        BAMDataServiceDataProcessor BAMDataProcessor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = BAMDataProcessor.getEndpointInvokeCount(serverID, endpoint);
//        out.print(data);*/
//    } else if (funcName.indexOf("getAvgResponseTimeSystem") > -1) {
//
////
////        BAMDataServiceDataProcessor BAMDataProcessor =
////                new BAMDataServiceDataProcessor(config, session, request);
////        String data = BAMDataProcessor.getAvgResponseTimeSystem();
////        out.print(data);
//    } else if (funcName.indexOf("getMaxResponseTime") > -1) {
//        int serviceID = Integer.parseInt(request.getParameter("serviceID"));
//
//        BAMDataServiceDataProcessor BAMDataProcessor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = BAMDataProcessor.getMaxResponseTime(serviceID);
//        out.print(data);
////        String serverUrl = request.getParameter("serverUrl");
////        String serviceName = request.getParameter("serviceName");
////
////        BAMDataServiceDataProcessor BAMDataProcessor =
////                new BAMDataServiceDataProcessor(config, session, request);
////        String data = BAMDataProcessor.getMaxResponseTime(serverUrl, serviceName);
////        out.print(data);
//    } else if (funcName.indexOf("getMaxResponseTimeSystem") > -1) {
////
////        BAMDataServiceDataProcessor BAMDataProcessor =
////                new BAMDataServiceDataProcessor(config, session, request);
////        String data = BAMDataProcessor.getMaxResponseTimeSystem();
////        out.print(data);
//    } else if (funcName.indexOf("getMinResponseTime") > -1) {
////        String serverUrl = request.getParameter("serverUrl");
////        String serviceName = request.getParameter("serviceName");
////
////        BAMDataServiceDataProcessor BAMDataProcessor =
////                new BAMDataServiceDataProcessor(config, session, request);
////        String data = BAMDataProcessor.getMinResponseTime(serverUrl, serviceName);
////        out.print(data);
//    } else if (funcName.indexOf("getMinResponseTimeSystem") > -1) {
////
////        BAMDataServiceDataProcessor BAMDataProcessor =
////                new BAMDataServiceDataProcessor(config, session, request);
////        String data = BAMDataProcessor.getMinResponseTimeSystem();
////        out.print(data);
//    }
//// else if (funcName.indexOf("getNoOfCalls") > -1) {
////        String serverUrl = request.getParameter("serverUrl");
////        String serviceName = request.getParameter("serviceName");
////
////        BAMDataServiceDataProcessor BAMDataProcessor =
////                new BAMDataServiceDataProcessor(config, session, request);
////        String data = BAMDataProcessor.getNoOfCalls(serverUrl, serviceName);
////        out.print(data);
////    } else if (funcName.indexOf("getminmaxaverageresptimessystem") > -1) {
////        String serverUrl = request.getParameter("serverUrl");
////
////        BAMDataServiceDataProcessor BAMDataProcessor =
////                new BAMDataServiceDataProcessor(config, session, request);
////        String data = BAMDataProcessor.getMinMaxAverageRespTimesSystem(serverUrl);
////        out.print(data);
////    } else if (funcName.indexOf("getloginsandfailures") > -1) {
////        String serverUrl = request.getParameter("serverUrl");
////
////        BAMDataServiceDataProcessor BAMDataProcessor =
////                new BAMDataServiceDataProcessor(config, session, request);
////        String data = BAMDataProcessor.getLoginsAndFailures(serverUrl);
////        out.print(data);
////    } else if (funcName.indexOf("getfailuresbyuser") > -1) {
////        String serverUrl = request.getParameter("serverUrl");
////        String userName = request.getParameter("userName");
////
////        BAMDataServiceDataProcessor BAMDataProcessor =
////                new BAMDataServiceDataProcessor(config, session, request);
////        String data = BAMDataProcessor.getSuccessFailureLoginsByUser(serverUrl, userName);
////        out.print(data);
////    }
//
//      else if (funcName.indexOf("getLatestAverageResponseTimeForServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestAverageResponseTimeForServer(serverID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestMaximumResponseTimeForServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestMaximumResponseTimeForServer(serverID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestMinimumResponseTimeForServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestMinimumResponseTimeForServer(serverID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestRequestCountForServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestRequestCountForServer(serverID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestResponseCountForServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestResponseCountForServer(serverID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestFaultCountForServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestFaultCountForServer(serverID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestAverageResponseTimeForService") > -1) {
//        int serviceID = Integer.parseInt(request.getParameter("serviceID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestAverageResponseTimeForService(serviceID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestMaximumResponseTimeForService") > -1) {
//        int serviceID = Integer.parseInt(request.getParameter("serviceID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestMaximumResponseTimeForService(serviceID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestMinimumResponseTimeForService") > -1) {
//        int serviceID = Integer.parseInt(request.getParameter("serviceID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestMinimumResponseTimeForService(serviceID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestRequestCountForService") > -1) {
//        int serviceID = Integer.parseInt(request.getParameter("serviceID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestRequestCountForService(serviceID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestResponseCountForService") > -1) {
//        int serviceID = Integer.parseInt(request.getParameter("serviceID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestResponseCountForService(serviceID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestFaultCountForService") > -1) {
//        int serviceID = Integer.parseInt(request.getParameter("serviceID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestFaultCountForService(serviceID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestAverageResponseTimeForOperation") > -1) {
//        int operationID = Integer.parseInt(request.getParameter("operationID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestAverageResponseTimeForOperation(operationID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestMaximumResponseTimeForOperation") > -1) {
//        int operationID = Integer.parseInt(request.getParameter("operationID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestMaximumResponseTimeForOperation(operationID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestMinimumResponseTimeForOperation") > -1) {
//        int operationID = Integer.parseInt(request.getParameter("operationID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestMinimumResponseTimeForOperation(operationID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestRequestCountForOperation") > -1) {
//        int operationID = Integer.parseInt(request.getParameter("operationID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestRequestCountForOperation(operationID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestResponseCountForOperation") > -1) {
//        int operationID = Integer.parseInt(request.getParameter("operationID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestResponseCountForOperation(operationID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestFaultCountForOperation") > -1) {
//        int operationID = Integer.parseInt(request.getParameter("operationID"));
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestFaultCountForOperation(operationID);
//        out.print(data);
//    } else if (funcName.indexOf("getEndpoints") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//
//        BAMDataServiceDataProcessor BAMDataProcessor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = BAMDataProcessor.getEndpoints(serverID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInAverageProcessingTimeForEndpoint") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String endpointName = "EndpointInAvgProcessingTime-" + request.getParameter("endpointName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInAverageProcessingTimeForEndpoint(serverID, endpointName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInMaximumProcessingTimeForEndpoint") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String endpointName = "EndpointInMaxProcessingTime-" + request.getParameter("endpointName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInMaximumProcessingTimeForEndpoint(serverID, endpointName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInMinimumProcessingTimeForEndpoint") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String endpointName = "EndpointInMinProcessingTime-" + request.getParameter("endpointName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInMinimumProcessingTimeForEndpoint(serverID, endpointName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInCumulativeCountForEndpoint") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String endpointName = "EndpointInCumulativeCount-" + request.getParameter("endpointName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInCumulativeCountForEndpoint(serverID, endpointName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInFaultCountForEndpoint") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String endpointName = "EndpointInFaultCount-" + request.getParameter("endpointName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInFaultCountForEndpoint(serverID, endpointName);
//        out.print(data);
//    } else if (funcName.indexOf("getSequences") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//
//        BAMDataServiceDataProcessor BAMDataProcessor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = BAMDataProcessor.getSequences(serverID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInAverageProcessingTimeForSequence") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String sequenceName = "SequenceInAvgProcessingTime-" + request.getParameter("sequenceName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInAverageProcessingTimeForSequence(serverID, sequenceName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInMaximumProcessingTimeForSequence") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String sequenceName = "SequenceInMaxProcessingTime-" + request.getParameter("sequenceName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInMaximumProcessingTimeForSequence(serverID, sequenceName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInMinimumProcessingTimeForSequence") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String sequenceName = "SequenceInMinProcessingTime-" + request.getParameter("sequenceName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInMinimumProcessingTimeForSequence(serverID, sequenceName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInCumulativeCountForSequence") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String sequenceName = "SequenceInCumulativeCount-" + request.getParameter("sequenceName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInCumulativeCountForSequence(serverID, sequenceName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInFaultCountForSequence") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String sequenceName = "SequenceInFaultCount-" + request.getParameter("sequenceName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInFaultCountForSequence(serverID, sequenceName);
//        out.print(data);
//    }  else if (funcName.indexOf("getLatestOutAverageProcessingTimeForSequence") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String sequenceName = "SequenceOutAvgProcessingTime-" + request.getParameter("sequenceName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestOutAverageProcessingTimeForSequence(serverID, sequenceName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestOutMaximumProcessingTimeForSequence") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String sequenceName = "SequenceOutMaxProcessingTime-" +request.getParameter("sequenceName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestOutMaximumProcessingTimeForSequence(serverID, sequenceName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestOutMinimumProcessingTimeForSequence") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String sequenceName = "SequenceOutMinProcessingTime-" + request.getParameter("sequenceName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestOutMinimumProcessingTimeForSequence(serverID, sequenceName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestOutCumulativeCountForSequence") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String sequenceName = "SequenceOutCumulativeCount-" + request.getParameter("sequenceName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestOutCumulativeCountForSequence(serverID, sequenceName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestOutFaultCountForSequence") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String sequenceName = "SequenceOutFaultCount-" + request.getParameter("sequenceName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestOutFaultCountForSequence(serverID, sequenceName);
//        out.print(data);
//    }  else if (funcName.indexOf("getProxyServices") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//
//        BAMDataServiceDataProcessor BAMDataProcessor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = BAMDataProcessor.getProxyServices(serverID);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInAverageProcessingTimeForProxy") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String proxyName = "ProxyInAvgProcessingTime-" + request.getParameter("proxyName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInAverageProcessingTimeForProxy(serverID, proxyName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInMaximumProcessingTimeForProxy") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String proxyName = "ProxyInMaxProcessingTime-" + request.getParameter("proxyName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInMaximumProcessingTimeForProxy(serverID, proxyName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInMinimumProcessingTimeForProxy") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String proxyName = "ProxyInMinProcessingTime-" + request.getParameter("proxyName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInMinimumProcessingTimeForProxy(serverID, proxyName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInCumulativeCountForProxy") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String proxyName = "ProxyInCumulativeCount-" + request.getParameter("proxyName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInCumulativeCountForProxy(serverID, proxyName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestInFaultCountForProxy") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String proxyName = "ProxyInFaultCount-" +  request.getParameter("proxyName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestInFaultCountForProxy(serverID, proxyName);
//        out.print(data);
//    }  else if (funcName.indexOf("getLatestOutAverageProcessingTimeForProxy") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String proxyName = "ProxyOutAvgProcessingTime-" + request.getParameter("proxyName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestOutAverageProcessingTimeForProxy(serverID, proxyName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestOutMaximumProcessingTimeForProxy") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String proxyName = "ProxyOutMaxProcessingTime-" + request.getParameter("proxyName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestOutMaximumProcessingTimeForProxy(serverID, proxyName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestOutMinimumProcessingTimeForProxy") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String proxyName = "ProxyOutMinProcessingTime-" + request.getParameter("proxyName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestOutMinimumProcessingTimeForProxy(serverID, proxyName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestOutCumulativeCountForProxy") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String proxyName = "ProxyOutCumulativeCount-" + request.getParameter("proxyName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestOutCumulativeCountForProxy(serverID, proxyName);
//        out.print(data);
//    } else if (funcName.indexOf("getLatestOutFaultCountForProxy") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String proxyName = "ProxyOutFaultCount-" + request.getParameter("proxyName");
//
//        BAMStatQueryDSClient client =
//                new BAMStatQueryDSClient(config, session, request);
//        String data = client.getLatestOutFaultCountForProxy(serverID, proxyName);
//        out.print(data);
//    } else if (funcName.indexOf("getServiceAvgResponseTimesOfServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = processor.getServiceResponseTimesOfServer(serverID, 0, demoFlag);
//        out.print(data);
//    } else if (funcName.indexOf("getServiceMaxResponseTimesOfServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = processor.getServiceResponseTimesOfServer(serverID, 2, demoFlag);;
//        out.print(data);
//    } else if (funcName.indexOf("getServiceMinResponseTimesOfServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = processor.getServiceResponseTimesOfServer(serverID, 1, demoFlag);
//        out.print(data);
//    } else if (funcName.indexOf("getServerInfo") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = processor.getServerInfo(serverID, demoFlag);
//        out.print(data);
//    } else if (funcName.indexOf("getServiceReqResFaultCountsOfServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = processor.getServiceReqResFaultCountsOfServer(serverID, demoFlag);
//        out.print(data);
//    } else if (funcName.indexOf("getSequenceInAvgProcessingTimesOfServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = processor.getSequenceInAvgProcessingTimesOfServer(serverID, demoFlag);
//        out.print(data);
//    } else if (funcName.indexOf("getEndpointInAvgProcessingTimesOfServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = processor.getEndpointInAvgProcessingTimesOfServer(serverID, demoFlag);
//        out.print(data);
//    } else if (funcName.indexOf("getProxyServiceInAvgProcessingTimesOfServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = processor.getProxyServiceInAvgProcessingTimesOfServer(serverID, demoFlag);
//        out.print(data);
//    } else if (funcName.indexOf("getServerMediationInfo") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = processor.getServerMediationInfo(serverID, demoFlag);
//        out.print(data);
//    } else if (funcName.indexOf("getSequenceReqResFaultCountsOfServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = processor.getSequenceReqResFaultCountsOfServer(serverID, demoFlag);
//        out.print(data);
//    } else if (funcName.indexOf("getProxyServiceReqResFaultCountsOfServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = processor.getProxyServiceReqResFaultCountsOfServer(serverID, demoFlag);
//        out.print(data);
//    } else if (funcName.indexOf("getEndpointReqResFaultCountsOfServer") > -1) {
//        int serverID = Integer.parseInt(request.getParameter("serverID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = processor.getEndpointReqResFaultCountsOfServer(serverID, demoFlag);
//        out.print(data);
//    } else if (funcName
//			.indexOf("getLatestMaximumOperationsForAnActivityID") > -1) {
//		int activityID = Integer.parseInt(request
//				.getParameter("activityID"));
//		String activityName = request.getParameter("activityName");
//
//		BAMStatQueryDSClient client = new BAMStatQueryDSClient(config,
//				session, request);
//
//		String data = client
//				.getLatestMaximumOperationsForAnActivityID(activityID);
//		out.print(data);
//	} else if (funcName.indexOf("getActivityList") > -1) {
//
//		BAMDataServiceDataProcessor BAMDataProcessor = new BAMDataServiceDataProcessor(
//				config, session, request);
//		String data = BAMDataProcessor.getActivityList();
//		out.print(data);
//	} else if (funcName.indexOf("getActivityInfoForActivityID") > -1) {
//        int activityID = Integer.parseInt(request.getParameter("activityID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//	            new BAMDataServiceDataProcessor(config, session, request);
//	    String data = processor.getActivityInfoForActivityID(activityID, demoFlag);
//	    out.print(data);
//    } else if (funcName.indexOf("getActivityInfo") > -1) {
//        int activityID = Integer.parseInt(request.getParameter("activityID"));
//        String demo = request.getParameter("demo");
//        boolean demoFlag = (demo != null);
//
//        BAMDataServiceDataProcessor processor =
//                new BAMDataServiceDataProcessor(config, session, request);
//        String data = processor.getActivityInfo(activityID, demoFlag);
//        out.print(data);
//    } else if (funcName.indexOf("getOperationsOfService") > -1) {
//
//	    int serverID = Integer.parseInt(request.getParameter("serverID"));
//	    int serviceID = Integer.parseInt(request.getParameter("serviceID"));
//	    String demo = request.getParameter("demo");
//	    boolean demoFlag = (demo != null);
//
//	    BAMDataServiceDataProcessor processor =
//	            new BAMDataServiceDataProcessor(config, session, request);
//	    String data = processor.getOperationsOfService(serverID, serviceID, demoFlag);
//	    out.print(data);
//	} else if (funcName.indexOf("getAdminConsoleUrl") > -1) {
//
//	    BAMDataServiceDataProcessor processor =
//	            new BAMDataServiceDataProcessor(config, session, request);
//	    String data = processor.getAdminConsoleUrl(request);
//	    out.print(data);
//    } else if (funcName.indexOf("getServerWithData") > -1) {
//        String func = request.getParameter("function");
//        BAMDataServiceDataProcessor processor =
//	            new BAMDataServiceDataProcessor(config, session, request);
//	    String data = processor.getServerWithData(func);
//	    out.print(data);
//    }
//
//    else if (funcName.indexOf("getJMXMetricsWindow") > -1) {
//     	 int serverID = Integer.parseInt(request.getParameter("serverID"));
//       	 BAMDataServiceDataProcessor processor =
//    	            new BAMDataServiceDataProcessor(config, session, request);
//             String data = processor.getJMXMetricsWindow(serverID);
//             out.print(data);
//        }
    // adding governance gadgets
    else if (funcName.indexOf("getImpactAnalysis") > -1) {
    	GovImpactAnalysisDataProcessor processor = new GovImpactAnalysisDataProcessor(config, session, request);
    	String impactJSONString = processor.getImpactAnalysisinJSON();
    	out.print(impactJSONString);
    }
    else if (funcName.indexOf("getResourceImpact") > -1) {
    	ResourceImpactDataProcesssor processor = new ResourceImpactDataProcesssor(config,session,request);
        processor.setReverse(Boolean.toString(true).equals(request.getParameter("reverse")));
    	String jSON = processor.getResourceImpactJSONTree(request.getParameter("path"));
    	out.print(jSON);
    } else if (funcName.indexOf("getProjects") > -1) {
    	ProjectDataProcessor processor =
                new ProjectDataProcessor(request, config);
        try {
            String jSON = processor.getJSONTree();
            out.print(jSON);
        } catch (Exception e) {
            out.print("{\"projects\":{\"project\":[]}}");
        }
    }
%>

