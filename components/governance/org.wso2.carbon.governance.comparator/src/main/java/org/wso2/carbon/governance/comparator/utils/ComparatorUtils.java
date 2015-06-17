/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.governance.comparator.utils;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.comparator.Comparison;
import org.wso2.carbon.governance.comparator.ComparisonException;
import org.wso2.carbon.governance.comparator.DiffGenerator;
import org.wso2.carbon.governance.comparator.DiffGeneratorFactory;
import org.wso2.carbon.governance.comparator.GovernanceDiffGeneratorFactory;
import org.wso2.carbon.governance.comparator.TextDiffGeneratorFactory;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.xml.sax.InputSource;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

/**
 * This Util class includes Comparator util methods.
 */
public class ComparatorUtils extends RegistryAbstractAdmin {

    /**
     * This method is used to get the text difference of two strings.
     *
     * @param resourcePathOne   resource path one.
     * @param resourcePathTwo   resource path two.
     * @return                  Comparison object which includes the difference parameters.
     * @throws ComparisonException
     * @throws WSDLException
     * @throws RegistryException
     * @throws UnsupportedEncodingException
     */
    public Comparison getArtifactTextDiff(String resourcePathOne, String resourcePathTwo)
            throws ComparisonException, WSDLException, RegistryException, UnsupportedEncodingException {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        Registry registry = RegistryCoreServiceComponent.getRegistryService().getSystemRegistry();
        Resource resourceOne = registry.get(resourcePathOne);
        Resource resourceTwo = registry.get(resourcePathTwo);

        DiffGeneratorFactory factory = new TextDiffGeneratorFactory();
        DiffGenerator flow = factory.getDiffGenerator();

        String resourceOneText = new String((byte[]) resourceOne.getContent(), "UTF-8");
        String resourceTwoText = new String((byte[]) resourceTwo.getContent(), "UTF-8");
        return flow.compare(resourceOneText, resourceTwoText, ComparatorConstants.TEXT_PLAIN_MEDIA_TYPE);
    }

    /**
     * This method is used to get a details difference of two resource while considering the media type.
     *
     * @param resourcePathOne   resource path one
     * @param resourcePathTwo   resource path two
     * @param mediaType         media type
     * @return                  Comparison object which includes the difference parameters.
     * @throws ComparisonException
     * @throws WSDLException
     * @throws RegistryException
     * @throws UnsupportedEncodingException
     */
    public Comparison getArtifactDetailDiff(String resourcePathOne, String resourcePathTwo, String mediaType)
            throws ComparisonException, WSDLException, RegistryException, UnsupportedEncodingException {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        Registry registry = RegistryCoreServiceComponent.getRegistryService().getSystemRegistry();
        Resource resourceOne = registry.get(resourcePathOne);
        Resource resourceTwo = registry.get(resourcePathTwo);

        switch (mediaType) {
        case ComparatorConstants.WSDL_MEDIA_TYPE:
            return getWSDLComparison(resourceOne, resourceTwo);
        default:
            return null;
        }
    }

    /**
     * This method is used to get wsdl difference comparison.
     *
     * @param WSDLOne   wsdl one.
     * @param WSDLTwo   wsdl two.
     * @return          Comparison object which includes the difference parameters.
     * @throws ComparisonException
     * @throws WSDLException
     * @throws RegistryException
     * @throws UnsupportedEncodingException
     */
    private Comparison getWSDLComparison(Resource WSDLOne, Resource WSDLTwo)
            throws ComparisonException, WSDLException, RegistryException, UnsupportedEncodingException {
        GovernanceDiffGeneratorFactory diffGeneratorFactory = new GovernanceDiffGeneratorFactory();
        DiffGenerator flow = diffGeneratorFactory.getDiffGenerator();
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();

        InputSource inputSourceOne = new InputSource(new ByteArrayInputStream((byte[]) WSDLOne.getContent()));
        Definition originalWSDL = wsdlReader.readWSDL(null, inputSourceOne);

        InputSource inputSourceTwo = new InputSource(new ByteArrayInputStream((byte[]) WSDLTwo.getContent()));
        Definition changedWSDL = wsdlReader.readWSDL(null, inputSourceTwo);

        return flow.compare(originalWSDL, changedWSDL, ComparatorConstants.WSDL_MEDIA_TYPE);
    }
}
