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
package org.wso2.carbon.governance.registry.extensions.validators.aggregators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.registry.extensions.interfaces.CustomValidations;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAggregator implements CustomValidations {

    private static final Log log = LogFactory.getLog(AbstractAggregator.class);

    protected CustomValidations generateValidator(String input) {
        if (input != null) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                String[] temp = input.split(",[ ]?parameters[:]");
                String className = temp[0].substring("class:".length());
                CustomValidations customValidations =
                        (CustomValidations) Class.forName(className, true, loader).newInstance();
                if (temp.length == 2) {
                    String[] parameterArray = temp[1].split("},");
                    String lastParameter = parameterArray[parameterArray.length - 1];
                    if (lastParameter.endsWith("}")) {
                        parameterArray[parameterArray.length - 1] = lastParameter.substring(0,
                                lastParameter.length() - 1);
                    }

                    Map<String, String> parameterMap = new HashMap<String, String>();
                    for (String parameter : parameterArray) {
                        String keyValuePair = parameter.substring(1);
                        int separatorIndex = keyValuePair.indexOf(":");
                        parameterMap.put(keyValuePair.substring(0, separatorIndex),
                                    keyValuePair.substring(separatorIndex + 1, keyValuePair.length()));
                    }
                    customValidations.init(parameterMap);
                }
                return customValidations;
            } catch (InstantiationException e) {
                log.error("Unable to instantiate validation class", e);
            } catch (IllegalAccessException e) {
                log.error("Unable to instantiate validation class", e);
            } catch (ClassNotFoundException e) {
                log.error("Unable to instantiate validation class", e);
            } catch (ClassCastException e) {
                log.error("Unable to instantiate validation class", e);
            }
        }
        return null;
    }
}
