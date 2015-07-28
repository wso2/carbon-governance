/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.governance.comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.comparator.internal.GovernanceComparatorDataHolder;

import java.util.ArrayList;
import java.util.List;

public class GovernanceDiffGeneratorFactory implements DiffGeneratorFactory {

    private final static Log log = LogFactory.getLog(GovernanceDiffGeneratorFactory.class);

    private DiffGenerator diffGenerator;

    @Override
    public DiffGenerator getDiffGenerator() {
        if (diffGenerator == null) {
            List<String> comparatorClasses = GovernanceComparatorDataHolder.getInstance().getGovernanceConfiguration()
                    .getComparators();
            List<Comparator<?>> comparators = getcomparators(comparatorClasses);

        }
        return diffGenerator;
    }

    private List<Comparator<?>> getcomparators(List<String> comparatorClasses) {
        List<Comparator<?>> comparators = new ArrayList<>();
        for (String comparatorClass : comparatorClasses) {
            try {
                Comparator<?> comparator = (Comparator<?>) getClass().getClassLoader().loadClass(comparatorClass).newInstance();
                comparators.add(comparator);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                log.error("Error instantiating  Comparator class " + comparatorClass);
            }
        }
        return comparators;
    }
}
