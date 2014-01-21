/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.gadgets.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.gadgets.stub.governance.gadgetsource.beans.xsd.LifecycleInfoBean;
import org.wso2.carbon.governance.gadgets.stub.governance.gadgetsource.beans.xsd.LifecyclePiechartGadgetBean;
import org.wso2.carbon.governance.gadgets.stub.governance.gadgetsource.beans.xsd.LifecycleStageInfoBean;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

public class GadgetSourceServiceDataProcessor {

    GadgetSourceServiceClient client;

    public GadgetSourceServiceDataProcessor(ServletConfig config, HttpSession session,
                                            HttpServletRequest request)
            throws AxisFault {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext()
                        .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        client = new GadgetSourceServiceClient(cookie, backendServerURL, configContext,
                                               request.getLocale());

    }

    public String getLifeCycles() {
    	LifecyclePiechartGadgetBean gadgetBean = client.getLifecyclePiechartGadgetData();
    	StringBuilder lifeCycleNames = new StringBuilder();
    	if (gadgetBean != null) {
    		LifecycleInfoBean[] lifecycles = gadgetBean.getLifecycles();
    		for (LifecycleInfoBean lifecycle : lifecycles) {
                lifeCycleNames.append(lifecycle.getName()).append(";");
            }
    	}
    	return lifeCycleNames.toString();
    	
    }
    
    public String getLifeCyleDataforPieChart(String lifeCycleName) {
        /* &title=Pie+Chart,{font-size:18px; color: #d01f3c}&
        &x_axis_steps=1&
        &y_ticks=5,10,5&
        &line=3,#87421F&
        &y_min=0&
        &y_max=20&
        &pie=60,#505050,{font-size: 12px; color: #404040;&
        &values=8,9,14,9,8&
        &pie_labels=IE,Firefox,Opera,Wii,Other&
        &colours=#d01f3c,#356aa0,#C79810&
        &links=&
        &tool_tip=%23val%23%25&*/

        String data = "";


        LifecyclePiechartGadgetBean gadgetBean = client.getLifecyclePiechartGadgetData();
        if (gadgetBean != null) {
            LifecycleInfoBean[] lifecycles = gadgetBean.getLifecycles();
            LifecycleInfoBean currentLIBean = null;
            for (LifecycleInfoBean infoBean : lifecycles) {
            	if (infoBean.getName().equals(lifeCycleName)) {
            		currentLIBean = infoBean;
            	}
            }
            
            String pieChartLables = "";
            String pieChartValues = "";

            // Getting the lifecycle stages stored in this bean
            LifecycleStageInfoBean[] stages = currentLIBean.getStages();
            for (int y = 0; y < stages.length; y++) {
                LifecycleStageInfoBean currentStage = stages[y];
                if (y == 0) {
                    pieChartLables = currentStage.getName();
                    pieChartValues = String.valueOf(currentStage.getServiceCount());
                } else {
                    pieChartLables = pieChartLables + "," + currentStage.getName();
                    pieChartValues = pieChartValues + "," + currentStage.getServiceCount();
                }
            }

            data =  "&bg_colour=#ffffff&\n" +
                    "&x_axis_steps=1&\n" +
                    "&y_ticks=5,10,5&\n" +
                    "&line=3,#87421F&\n" +
                    "&y_min=0&\n" +
                    "&y_max=20&\n" +
                    "&pie=60,#505050,{font-size: 12px; color: #404040;&\n" +
                    "&values=" + pieChartValues + "&\n" +
                    "&pie_labels=" + pieChartLables + "&\n" +
                    "&colours=#d01f3c,#356aa0,#C78810,#66CC66,#999999,#CC66CC,#99CC33&\n" +
                    "&links=&\n" +
                    "&tool_tip=%23val%23&";
        }else{
            // Notify the user
            data =  "&bg_colour=#ffffff&\n" +
                    "&x_axis_steps=1&\n" +
                    "&y_ticks=5,10,5&\n" +
                    "&line=3,#87421F&\n" +
                    "&y_min=0&\n" +
                    "&y_max=20&\n" +
                    "&pie=60,#505050,{font-size: 12px; color: #404040;&\n" +
                    "&values=0&\n" +
                    "&pie_labels=Life Cycle Data Not Available.&\n" +
                    "&colours=#d01f3c,#356aa0,#C78810,#66CC66,#999999,#CC66CC,#99CC33&\n" +
                    "&links=&\n" +
                    "&tool_tip=%23val%23&";
        }


        return data;
    }
}
