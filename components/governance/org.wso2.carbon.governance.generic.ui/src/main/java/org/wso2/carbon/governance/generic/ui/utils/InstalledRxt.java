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

package org.wso2.carbon.governance.generic.ui.utils;

import java.util.Comparator;

public class InstalledRxt {
    private String rxt;
    private boolean isDeleteAllowed = false;

    public void setRxt(String rxt) {
        this.rxt = rxt;
    }

    public String getRxt() {
        return rxt;
    }

    public void setDeleteAllowed() {
        isDeleteAllowed = true;
    }

    public boolean isDeleteAllowed() {
        return isDeleteAllowed;
    }

    public static Comparator<InstalledRxt> installedRxtComparator = new Comparator<InstalledRxt>() {
        public int compare(InstalledRxt s1, InstalledRxt s2) {
            return s1.getRxt().compareTo(s2.getRxt());
        }
    };

}