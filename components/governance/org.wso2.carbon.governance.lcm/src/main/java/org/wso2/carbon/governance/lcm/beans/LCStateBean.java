/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.lcm.beans;

import java.util.List;

public class LCStateBean {

    private String lifeCycleName;

    private String lifeCycleState;

    private List<LifeCycleApprovalBean> lifeCycleApprovalBeanList;

    private List<LifeCycleCheckListItemBean> lifeCycleCheckListItemBeans;

    private LifeCycleActionsBean lifeCycleActionsBean;

    public List<LifeCycleApprovalBean> getLifeCycleApprovalBeanList() {
        return lifeCycleApprovalBeanList;
    }

    public void setLifeCycleApprovalBeanList(List<LifeCycleApprovalBean> lifeCycleApprovalBeanList) {
        this.lifeCycleApprovalBeanList = lifeCycleApprovalBeanList;
    }

    public List<LifeCycleCheckListItemBean> getLifeCycleCheckListItemBeans() {
        return lifeCycleCheckListItemBeans;
    }

    public void setLifeCycleCheckListItemBeans(List<LifeCycleCheckListItemBean> lifeCycleCheckListItemBeans) {
        this.lifeCycleCheckListItemBeans = lifeCycleCheckListItemBeans;
    }

    public LifeCycleActionsBean getLifeCycleActionsBean() {
        return lifeCycleActionsBean;
    }

    public void setLifeCycleActionsBean(LifeCycleActionsBean lifeCycleActionsBean) {
        this.lifeCycleActionsBean = lifeCycleActionsBean;
    }

    public String getLifeCycleName() {
        return lifeCycleName;
    }

    public void setLifeCycleName(String lifeCycleName) {
        this.lifeCycleName = lifeCycleName;
    }

    public String getLifeCycleState() {
        return lifeCycleState;
    }

    public void setLifeCycleState(String lifeCycleState) {
        this.lifeCycleState = lifeCycleState;
    }
}
