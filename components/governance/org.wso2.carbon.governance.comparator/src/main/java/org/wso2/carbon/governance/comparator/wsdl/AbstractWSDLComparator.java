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

package org.wso2.carbon.governance.comparator.wsdl;

import org.wso2.carbon.governance.comparator.Comparator;
import org.wso2.carbon.governance.comparator.Comparison;
import org.wso2.carbon.governance.comparator.ComparisonException;
import org.wso2.carbon.governance.comparator.common.DefaultComparison;
import org.wso2.carbon.governance.comparator.utils.ComparatorConstants;

import javax.wsdl.Definition;

public abstract class AbstractWSDLComparator implements Comparator<Definition> {

    @Override
    public void init() {

    }

    @Override
    public boolean isSupportedMediaType(String mediaType) {
        return ComparatorConstants.WSDL_MEDIA_TYPE == mediaType ? true : false;
    }

    @Override
    public void compare(Definition base, Definition changed, Comparison comparison) throws ComparisonException {
        compareInternal(base, changed, (DefaultComparison) comparison);
    }

    abstract void compareInternal(Definition base, Definition changed, DefaultComparison comparison)
            throws ComparisonException;
}
