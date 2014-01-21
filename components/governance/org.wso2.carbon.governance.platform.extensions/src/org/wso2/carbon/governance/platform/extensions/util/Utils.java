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
package org.wso2.carbon.governance.platform.extensions.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.stream.XMLStreamException;

public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    public static OMElement extractPayload(Resource resource) throws RegistryException {
        Object content = resource.getContent();
        OMElement proxyContent;
        try {
            if (content instanceof String) {
                proxyContent = AXIOMUtil.stringToOM((String) content);
            } else {
                proxyContent = AXIOMUtil.stringToOM(RegistryUtils.decodeBytes((byte[]) content));
            }
        } catch (XMLStreamException e) {
            String msg = "Unable to parse the provided payload";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return proxyContent;
    }

}
