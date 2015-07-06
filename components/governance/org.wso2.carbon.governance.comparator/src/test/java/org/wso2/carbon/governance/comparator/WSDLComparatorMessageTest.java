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

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.comparator.utils.ComparatorConstants;
import org.wso2.carbon.governance.comparator.utils.WSDLComparisonUtils;
import org.wso2.carbon.governance.comparator.wsdl.WSDLTestUtils;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

public class WSDLComparatorMessageTest extends TestCase {

    private final Log log = LogFactory.getLog(WSDLComparatorMessageTest.class);


    public void testMessagesSameWSDLV1() throws WSDLException, ComparisonException {
        TestDiffGeneratorFactory factory = new TestDiffGeneratorFactory();
        DiffGenerator flow = factory.getDiffGenerator();
        Definition originalWSDL = WSDLTestUtils.loadWSDL("/wsdl/tempconvert.wsdl");
        Definition changedWSDL = WSDLTestUtils.loadWSDL("/wsdl/tempconvert.wsdl");
        Comparison comparison = flow.compare(originalWSDL, changedWSDL, ComparatorConstants.WSDL_MEDIA_TYPE);
        WSDLComparisonUtils.print(comparison);
        assertNotNull(comparison);
    }

    public void testMessagesSameWSDLV2() throws WSDLException, ComparisonException {
        TestDiffGeneratorFactory factory = new TestDiffGeneratorFactory();
        DiffGenerator flow = factory.getDiffGenerator();
        Definition originalWSDL = WSDLTestUtils.loadWSDL("/wsdl/tempconvert-changed.wsdl");
        Definition changedWSDL = WSDLTestUtils.loadWSDL("/wsdl/tempconvert-changed.wsdl");
        Comparison comparison = flow.compare(originalWSDL, changedWSDL, ComparatorConstants.WSDL_MEDIA_TYPE);
        WSDLComparisonUtils.print(comparison);
        assertNotNull(comparison);
    }

    public void testMessagesAddition() throws WSDLException, ComparisonException {
        TestDiffGeneratorFactory factory = new TestDiffGeneratorFactory();
        DiffGenerator flow = factory.getDiffGenerator();
        Definition originalWSDL = WSDLTestUtils.loadWSDL("/wsdl/tempconvert.wsdl");
        Definition changedWSDL = WSDLTestUtils.loadWSDL("/wsdl/tempconvert-changed.wsdl");
        Comparison comparison = flow.compare(originalWSDL, changedWSDL, ComparatorConstants.WSDL_MEDIA_TYPE);
        WSDLComparisonUtils.print(comparison);
        assertNotNull(comparison);
    }

    public void testMessagesRemovalAndChanges() throws WSDLException, ComparisonException {
        TestDiffGeneratorFactory factory = new TestDiffGeneratorFactory();
        DiffGenerator flow = factory.getDiffGenerator();
        Definition originalWSDL = WSDLTestUtils.loadWSDL("/wsdl/tempconvert.wsdl");
        Definition changedWSDL = WSDLTestUtils.loadWSDL("/wsdl/tempconvert-changed.wsdl");
        Comparison comparison = flow.compare(originalWSDL, changedWSDL, ComparatorConstants.WSDL_MEDIA_TYPE);
        WSDLComparisonUtils.print(comparison);
        assertNotNull(comparison);
    }

    public void testMessagesRemovalAndChangesReverse() throws WSDLException, ComparisonException {
        TestDiffGeneratorFactory factory = new TestDiffGeneratorFactory();
        DiffGenerator flow = factory.getDiffGenerator();
        Definition originalWSDL = WSDLTestUtils.loadWSDL("/wsdl/tempconvert.wsdl");
        Definition changedWSDL = WSDLTestUtils.loadWSDL("/wsdl/tempconvert-changed.wsdl");
        Comparison comparison = flow.compare(changedWSDL, originalWSDL, ComparatorConstants.WSDL_MEDIA_TYPE);
        WSDLComparisonUtils.print(comparison);
        assertNotNull(comparison);
    }


}
