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

import org.wso2.carbon.governance.registry.extensions.interfaces.CustomValidations;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.HashMap;
import java.util.Map;

public class CheckItemTickedValidator extends PropertyValueValidator implements CustomValidations {

    private int itemIndex;
    public void init(Map parameterMap) {
        if (parameterMap != null) {
            itemIndex = Integer.parseInt((String) parameterMap.get("itemIndex"));
            boolean checked = !Boolean.toString(false).equals(parameterMap.get("checked"));
            Map<String, String> temp = new HashMap<String, String>();
            temp.put("propertyName", "registry.custom_lifecycle.checklist.option." + itemIndex +
                    ".item");
            temp.put("propertyValue", "value:" + Boolean.toString(checked));
            temp.put("isMultiValued", Boolean.toString(true));
            super.init(temp);
        }
    }

    public boolean validate(RequestContext context) {
        try {
            Resource resource = context.getResource();
            return validatePropertyOfResource(resource);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error in lifecycle configuration. Checklist item not found for index " + itemIndex);
        }
    }
}
