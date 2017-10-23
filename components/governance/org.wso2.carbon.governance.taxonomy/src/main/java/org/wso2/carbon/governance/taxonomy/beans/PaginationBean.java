/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.governance.taxonomy.beans;

/**
 * This bean class will store taxonomy pagination meta data
 */
public class PaginationBean {

    private int startNode;
    private int endNode;

    /**
     * This method will return the last node index of pagination bean object
     *
     * @return Integer end node index value
     */
    public int getEndNode() {
        return endNode;
    }

    /**
     * This method will set the end node's index value in bean object
     *
     * @param endNode Integer end node index value
     */
    public void setEndNode(int endNode) {
        this.endNode = endNode;
    }

    /**
     * This method will return the start node index of pagination object
     *
     * @return Integer start node index value
     */
    public int getStartNode() {
        return startNode;
    }

    /**
     * This method will set the start node index value of pagination object
     *
     * @param startNode Integer start node index value
     */
    public void setStartNode(int startNode) {
        this.startNode = startNode;
    }

}
