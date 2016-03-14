/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.comparator.Comparison;
import org.wso2.carbon.governance.comparator.ComparisonException;
import org.wso2.carbon.governance.comparator.common.DefaultComparison;

import javax.wsdl.Definition;
import javax.wsdl.Types;

public class WSDLTypesComparator extends AbstractWSDLComparator {

    private final Log log = LogFactory.getLog(WSDLTypesComparator.class);

    @Override
    public void init() {

    }

    @Override
    public void compareInternal(Definition base, Definition changed, DefaultComparison comparison)
            throws ComparisonException {
        compareTypes(base, changed, comparison);
    }

    protected void compareTypes (Definition base, Definition changed, DefaultComparison comparison) {
        DefaultComparison.DefaultSection section = null;
        Types baseTypes = base.getTypes();
        baseTypes.
    }
}
