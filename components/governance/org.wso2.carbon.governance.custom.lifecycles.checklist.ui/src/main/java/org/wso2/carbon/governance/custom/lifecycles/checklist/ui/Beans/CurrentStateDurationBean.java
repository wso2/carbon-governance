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

package org.wso2.carbon.governance.custom.lifecycles.checklist.ui.Beans;

import org.wso2.carbon.governance.lcm.stub.beans.xsd.CheckpointBean;

/**
 * This class used when getting current lifecycle state duration.
 */
public class CurrentStateDurationBean {

    /**
     * Checkpoint bean used to handle checkpoint details.
     */
    private CheckpointBean checkpointBean;

    /**
     * Duration of the checkpoint.
     */
    private String duration;

    /**
     * Method used to get checkpoint bean.
     *
     * @return                  checkpoint bean which includes checkpoint name, checkpoint duration color and checkpoint
     *                          boundaries.
     */
    public CheckpointBean getCheckpointBean() {
        return checkpointBean;
    }

    /**
     * This method is used to set checkpoint bean.
     *
     * @param checkpointBean    checkpoint bean used to show current lifecycle state details.
     */
    public void setCheckpointBean(CheckpointBean checkpointBean) {
        this.checkpointBean = checkpointBean;
    }

    /**
     * This method is used to get current lifecycle state duration.
     *
     * @return                  current lifecycle state duration.
     */
    public String getDuration() {
        return duration;
    }

    /**
     * This method is used to set duration of the current lifecycle state.
     *
     * @param duration          current lifecycle state duration in dd:hh:mm:ss format.
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }
}
