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
 * This bean class maps to boundary configurations in lifecycle checkpoints.
 */
public class BoundaryBean {

    /**
     * Lower boundary of the checkpoint.
     */
    private String min;

    /**
     * Upper boundary of the checkpoint.
     */
    private String max;

    /**
     * This method gives the lower boundary of the checkpoint.
     *
     * @return String   Lower boundary.
     */
    public String getMin() {
        return min;
    }

    /**
     * This method sets the upper boundary of the checkpoint.
     *
     * @param min   Upper boundary timestamp.
     */
    public void setMin(String min) {
        this.min = min;
    }

    /**
     * This method gives the upper boundary of the checkpoint
     *
     * @return String   Upper boundary
     */
    public String getMax() {
        return max;
    }

    /**
     * This method sets the upper boundary of the checkpoint.
     *
     * @param max   Upper boundary timestamp.
     */
    public void setMax(String max) {
        this.max = max;
    }
}
