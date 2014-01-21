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

package org.wso2.carbon.governance.custom.lifecycles.checklist.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.LifecycleActions;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.Property;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class LifecycleBean {

    private boolean loggedIn;

    private boolean versionView;

    private boolean putAllowed;

    private String pathWithVersion;

    private String[] aspectsToAdd;

    private LifecycleActions[] availableActions;

    private Property[] lifecycleProperties;
    
    private Property[] lifecycleApproval;

    private boolean link;

    private boolean mounted;

    private boolean showAddDelete;

    private String mediaType;

    private String[] rolesOfUser;

    public String[] getRolesOfUser() {
        return rolesOfUser;
    }

    public void setRolesOfUser(String[] rolesOfUser) {
        this.rolesOfUser = rolesOfUser;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public boolean isVersionView() {
        return versionView;
    }

    public void setVersionView(boolean versionView) {
        this.versionView = versionView;
    }

    public boolean isPutAllowed() {
        return putAllowed;
    }

    public void setPutAllowed(boolean putAllowed) {
        this.putAllowed = putAllowed;
    }

    public String getPathWithVersion() {
        return pathWithVersion;
    }

    public void setPathWithVersion(String pathWithVersion) {
        this.pathWithVersion = pathWithVersion;
    }

    public String[] getAspectsToAdd() {
        return aspectsToAdd;
    }

    public void setAspectsToAdd(String[] aspectsToAdd) {
        this.aspectsToAdd = aspectsToAdd;
    }

    public LifecycleActions[] getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(LifecycleActions[] availableActions) {
        this.availableActions = availableActions;
    }

    public Property[] getLifecycleProperties() {
        return lifecycleProperties;
    }

    public void setLifecycleProperties(Property[] lifecycleProperties) {
        this.lifecycleProperties = lifecycleProperties;
    }

    public boolean isLink() {
        return link;
    }

    public void setLink(boolean link) {
        this.link = link;
    }

    public boolean isMounted() {
        return this.mounted;
    }

    public void setMounted(boolean mounted) {
        this.mounted = mounted;
    }

    public boolean getShowAddDelete() {
        return showAddDelete;
    }

    public void setShowAddDelete(boolean showAddDelete) {
        this.showAddDelete = showAddDelete;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

	public void setLifecycleApproval(Property[] lifecycleApproval) {
		this.lifecycleApproval = lifecycleApproval;
	}

	public Property[] getLifecycleApproval() {
		return lifecycleApproval;
	}
}
