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
 * This bean class maps to checkpoint configurations in lifecycle states.
 */
public class CheckpointBean {

    /**
     * Checkpoint name.
     */
    private String name;

    /**
     * Checkpoint duration display color of lifecycle current state.
     */
    private String durationColour;

    /**
     * Checkpoint boundaries which includes lower and upper boundaries.
     */
    private BoundaryBean checkpointBoundaries;

    /**
     * This method gives the checkpoint name.
     *
     * @return      Checkpoint name.
     */
    public String getName() {
        return name;
    }

    /**
     * This method sets checkpoint name.
     *
     * @param name  Checkpoint name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method gives the duration display color of lifecycle current state.
     *
     * @return      current lifecycle state duration display color.
     */
    public String getDurationColour() {
        return durationColour;
    }

    /**
     * This method sets current lifecycle state duration display color.
     *
     * @param durationColour    duration color.
     */
    public void setDurationColour(String durationColour) {
        this.durationColour = durationColour;
    }

    /**
     * This method gives the checkpoint boundaries.
     *
     * @return      Checkpoint boundaries.
     */
    public BoundaryBean getCheckpointBoundaries() {
        return checkpointBoundaries;
    }

    /**
     * This method sets checkpoint boundaries.
     *
     * @param checkpointBoundaries  Checkpoint boundaries.
     */
    public void setCheckpointBoundaries(BoundaryBean checkpointBoundaries) {
        this.checkpointBoundaries = checkpointBoundaries;
    }
}
