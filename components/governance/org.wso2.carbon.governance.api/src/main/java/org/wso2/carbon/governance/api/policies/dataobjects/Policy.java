/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.api.policies.dataobjects;

import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;


public interface Policy extends GovernanceArtifact {
    /**
     * Method to obtain the policy element of this policy artifact.
     *
     * @return the policy element.
     */
    String getPolicyContent();

    /**
     * Method to set the policy element of this policy artifact.
     *
     * @param policyContent the policy element.
     */
    @SuppressWarnings("unused")
    void setPolicyContent(String policyContent);
}
