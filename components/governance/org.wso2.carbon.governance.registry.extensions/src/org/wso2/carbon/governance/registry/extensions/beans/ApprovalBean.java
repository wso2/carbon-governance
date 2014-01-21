/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.registry.extensions.beans;

import java.util.ArrayList;
import java.util.List;

public class ApprovalBean implements Comparable  {
	
    private List<String> roles;
    private String forEvent;
    private int votes;

    public ApprovalBean() {
        this.roles = new ArrayList<String>();
        this.forEvent = "";
    }

    public String getForEvent() {
        return forEvent;
    }

    public void setForEvent(String forEvent) {
        this.forEvent = forEvent;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

	public void setVotes(int votes) {
		this.votes = votes;
	}

	public int getVotes() {
		return votes;
	}

	@Override
	public int compareTo(Object anotherItem) {
		if (!(anotherItem instanceof ApprovalBean))
            return 0;
		try {
			ApprovalBean item = (ApprovalBean) anotherItem;
            return this.getForEvent().compareTo(item.getForEvent());
        } catch (Exception e) {
            /* suppressing any parsing errors, since order is not "that" important to consider. */
        }
        return 0;
	}

}
