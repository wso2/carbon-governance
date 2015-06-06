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

package org.wso2.carbon.governance.comparator.text;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.comparator.Comparator;
import org.wso2.carbon.governance.comparator.Comparison;
import org.wso2.carbon.governance.comparator.ComparisonException;
import org.wso2.carbon.governance.comparator.DiffGenerator;
import org.wso2.carbon.governance.comparator.DiffGeneratorFactory;
import org.wso2.carbon.governance.comparator.TestDiffGeneratorFactory;
import org.wso2.carbon.governance.comparator.utils.ComparatorConstants;
import org.wso2.carbon.governance.comparator.utils.WSDLComparisonUtils;
import org.wso2.carbon.governance.comparator.wsdl.WSDLBindingsComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLDeclarationComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLImportsComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLMessagesComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLOperationComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLPortTypeComparator;
import org.wso2.carbon.governance.comparator.wsdl.WSDLTestUtils;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import java.util.ArrayList;
import java.util.List;

public class PlainTextTest extends TestCase {

    private final Log log = LogFactory.getLog(PlainTextTest.class);

    public void testCompare() throws WSDLException, ComparisonException {
        DiffGeneratorFactory factory = new TestPlainTextDiffGeneratorFactory();
        DiffGenerator flow = factory.getDiffGenerator();
        String base = "Text content 1";
        String change = "Text content 2";
        Comparison comparison = flow.compare(base, change, ComparatorConstants.TEXT_PLAIN_MEDIA_TYPE);
        WSDLComparisonUtils.print(comparison);
        assertNotNull(comparison);
    }


    private class TestPlainTextDiffGeneratorFactory implements DiffGeneratorFactory {
        @Override
        public DiffGenerator getDiffGenerator() {
            List<Comparator<?>> comparators = new ArrayList<>();
            comparators.add(new TextComparator());
            return new DiffGenerator(comparators);
        }
    }
}
