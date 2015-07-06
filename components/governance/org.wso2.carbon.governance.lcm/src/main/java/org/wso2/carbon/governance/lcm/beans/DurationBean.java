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

/**
 * This bean class maps to lifecycle current state duration configurations.
 */
public class DurationBean {

    /**
     * Lifecycle current state duration.
     */
    private long duration;

    /**
     * Lifecycle current checkpoint.
     */
    private CheckpointBean checkpoint;

    /**
     * This method gives lifecycle current state duration.
     *
     * @return          lifecycle current state duration timestamp.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * This method sets lifecycle current sate duration.
     *
     * @param duration  current state duration.
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * This method gives lifecycle current state checkpoints.
     *
     * @return          lifecycle state checkpoints.
     */
    public CheckpointBean getCheckpoint() {
        return checkpoint;
    }

    /**
     * This method sets lifecycle current state checkpoints.
     *
     * @param checkpoint    lifecycle current state checkpoints.
     */
    public void setCheckpoint(CheckpointBean checkpoint) {
        this.checkpoint = checkpoint;
    }
}
