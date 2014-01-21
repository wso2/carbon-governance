/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.registry.extensions.validators;

import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.registry.extensions.interfaces.CustomValidations;

import java.util.Collections;
import java.util.Map;

public class AttributeValueValidator extends AttributeExistenceValidator
        implements CustomValidations {

    private String pattern = null;
    private boolean atLeastOne = false;

    public void init(Map parameterMap) {
        if (parameterMap != null) {
            pattern = (String) parameterMap.get("pattern");
            atLeastOne = Boolean.toString(true).equals(parameterMap.get("atLeastOne"));
            super.init(Collections.singletonMap("attributes",
                    (String) parameterMap.get("attribute")));
        }
    }

    protected boolean validateAttribute(GovernanceArtifact governanceArtifact, String attribute)
            throws GovernanceException {
        boolean output = true;
        if (pattern != null) {
            String[] values = governanceArtifact.getAttributes(attribute);
            if (values != null) {
                for (String value : values) {
                    if (value.matches(pattern)) {
                        if (atLeastOne) {
                            return true;
                        }
                    } else {
                        output = false;
                    }
                }
            } else {
                output = false;
            }
        }
        return output;
    }
}
