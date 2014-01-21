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
package org.wso2.carbon.governance.custom.lifecycles.checklist.util;

public class CheckListItem implements Comparable{
    private String lifeCycleStatus;
    private String name;
    private String value;
    private String order;
    private String propertyName;
    private String isVisible;

    public String getVisible() {
        return isVisible;
    }

    public void setVisible(String visible) {
        isVisible = visible;
    }

    private static final Object HASH_CODE_OBJECT = new Object();

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getLifeCycleStatus() {
        return lifeCycleStatus;
    }

    public void setLifeCycleStatus(String lifeCycleStatus) {
        this.lifeCycleStatus = lifeCycleStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public CheckListItem(String lifeCycleStatus, String name, String value, String order) {
        this.lifeCycleStatus = lifeCycleStatus;
        this.name = name;
        this.value = value;
        this.order = order;
    }

    public CheckListItem() {

    }

    public boolean matchLifeCycleStatus(String status, boolean ignoreCase) {
        if ((lifeCycleStatus == null) || (status == null)) {
            return false;
        }

        if (ignoreCase)
            return lifeCycleStatus.equalsIgnoreCase(status);
        else
            return lifeCycleStatus.equals(status);
    }

    public boolean matchLifeCycleStatus(String status) {
        return matchLifeCycleStatus(status, true);
    }

    public int hashCode() {
        int hashCode = HASH_CODE_OBJECT.hashCode();
        if (order != null) {
            hashCode &= order.hashCode();
        }
        if (name != null) {
            hashCode &= name.hashCode();
        }
        if (value != null) {
            hashCode &= value.hashCode();
        }
        if (lifeCycleStatus != null) {
            hashCode &= lifeCycleStatus.hashCode();
        }
        if (propertyName != null) {
            hashCode &= propertyName.hashCode();
        }
        return hashCode;
    }

    public boolean equals(Object obj) {
        if (obj instanceof CheckListItem) {
            CheckListItem item = (CheckListItem)obj;
            return (this.order != null && this.order.equals(item.order) ||
                    (this.order == null && item.order == null)) &&
                    (this.lifeCycleStatus != null &&
                            this.lifeCycleStatus.equals(item.lifeCycleStatus) ||
                    (this.lifeCycleStatus == null && item.lifeCycleStatus == null)) &&
                    (this.name != null && this.name.equals(item.name) ||
                    (this.name == null && item.name == null)) &&
                    (this.value != null && this.value.equals(item.value) ||
                    (this.value == null && item.value == null)) &&
                    (this.propertyName != null && this.propertyName.equals(item.propertyName) ||
                    (this.propertyName == null && item.propertyName == null));

        }
        return false;
    }

    public int compareTo(Object anotherItem) {
        if (equals(anotherItem)) {
            return 0;
        }
        CheckListItem item = (CheckListItem)anotherItem;
        int otherItemOrder = Integer.parseInt(item.getOrder());
        int itemOrder = Integer.parseInt(order);

        return itemOrder - otherItemOrder;
    }
}