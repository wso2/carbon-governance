/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.registry.extensions.tasks;

import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.ntask.core.Task;

/**
 * AbstractGovernanceTask is a convenient abstract class for RegistryTask implementers who wish to access
 * tenant-qualified tenant-qualified GenericArtifactManager. Additionally this class provides single
 * execute(Map<String, String> properties) method to resolve design ambiguities of Carbon Task interface
 * (Refer - https://wso2.org/jira/browse/TS-10 ) so that users have to implement only one method.
 *
 * @since 5.0.0
 */
public abstract class AbstractGovernanceTask implements Task {

    
    protected Registry getConfigSystemRegistry() throws RegistryException {
        throw new UnsupportedOperationException("Not allow to access ConfigurationRegistry here");
    }

    protected GenericArtifactManager getGenericArtifactManager(String artifactShortName) throws GovernanceException {
        throw new UnsupportedOperationException("Not allow to access ConfigurationRegistry here");
    }

}
