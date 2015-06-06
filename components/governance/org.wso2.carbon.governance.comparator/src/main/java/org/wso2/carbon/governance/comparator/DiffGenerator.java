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

import org.wso2.carbon.governance.comparator.common.DefaultComparison;
import org.wso2.carbon.governance.comparator.wsdl.WSDLComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLBindingsComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLDeclarationComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLImportsComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLMessagesComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLOperationComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLPortTypeComparator;

import java.util.ArrayList;
import java.util.List;

public class DiffGenerator {

    private List<Comparator<?>> comparators = new ArrayList<>();

    public DiffGenerator(){

    }
    public DiffGenerator(List<Comparator<?>> comparators) {
        this.comparators.addAll(comparators);
    }

    public Comparison compare(Object base, Object changed, String mediaType) throws ComparisonException {
        Comparison comparison = new DefaultComparison();
        return compare(base, changed, mediaType, comparison);
    }

    protected Comparison compare(Object base, Object changed, String mediaType, Comparison comparison) throws ComparisonException {
        for (Comparator comparator : comparators) {
            if (comparator.isSupportedMediaType(mediaType)) {
                comparator.compare(base, changed, comparison);
            }
        }
        return comparison;
    }

    protected List<Comparator<?>> getComparators() {
        return comparators;
    }

    protected void addComparator(Comparator<?> comparator) {
        this.comparators.add(comparator);
    }



}
