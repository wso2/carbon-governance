/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.registry.extensions.aspects.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class LifecycleCheckpointUtils {

    private static final Log log = LogFactory.getLog(LifecycleCheckpointUtils.class);

    /**
     * This method is used to evaluate an xpath.
     *
     * @param contentElement OM element that the xpath is bean evaluated.
     * @param xpathString    xPath
     * @param nsPrefix       namespace prefix
     * @return
     */
    public static List evaluateXpath(OMElement contentElement, String xpathString, String nsPrefix) {
        List resultsList = new ArrayList();
        try {
            AXIOMXPath xpath = new AXIOMXPath(xpathString);

            Iterator nsIterator = contentElement.getAllDeclaredNamespaces();
            if (nsIterator.hasNext()) {
                while (nsIterator.hasNext()) {
                    OMNamespace next = (OMNamespace) nsIterator.next();
                    xpath.addNamespace(nsPrefix, next.getNamespaceURI());
                    resultsList.addAll(xpath.selectNodes(contentElement));
                }
            } else if (contentElement.getDefaultNamespace() != null) {
                xpath.addNamespace(nsPrefix, contentElement.getDefaultNamespace().getNamespaceURI());
                resultsList.addAll(xpath.selectNodes(contentElement));
            } else if (nsPrefix != null) {
                xpathString = xpathString.replace(nsPrefix + ":", "");
                xpath = new AXIOMXPath(xpathString);
                resultsList.addAll(xpath.selectNodes(contentElement));
            } else {
                xpath = new AXIOMXPath(xpathString);
                resultsList.addAll(xpath.selectNodes(contentElement));
            }
            return resultsList;
        } catch (JaxenException e) {
            log.error("Error while evaluating xPath: '" + xpathString + "'.", e);
        }
        return null;
    }

    /**
     * This method is used to get lifecycle initial state name.
     *
     * @return
     */
    public static String getLCInitialStateId(OMElement configurationElement) {
        if (configurationElement == null) {
            return null;
        }
        String xpathString = LifecycleConstants.XPATH_STATE_ID;
        List checkpoints = evaluateXpath(configurationElement, xpathString, null);
        if (!checkpoints.isEmpty()) {
            OMElement initialStateElement = (OMElement) checkpoints.get(0);
            return initialStateElement.getAttributeValue(new QName("id"));
        } else {
            return null;
        }
    }

    /**
     * This method is used to current time
     *
     * @return String  current time in  yyyy-MM-dd HH:mm:ss.SSS format.
     */
    public static String getCurrentTime() {
        Date currentTimeStamp = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(LifecycleConstants.HISTORY_ITEM_TIME_STAMP_FORMAT);
        return dateFormat.format(currentTimeStamp);
    }
}
