/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.governance.taxonomy.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.governance.taxonomy.util.TaxonomyStorageService;
import org.wso2.carbon.governance.taxonomy.util.Utils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import org.xml.sax.SAXException;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/***
 * This handler will use to parse taxonomy.xml and store it inside tenant specific map.
 */
public class TaxonomyResourceHandler extends Handler {
    private static final Log log = LogFactory.getLog(
            org.wso2.carbon.governance.taxonomy.handler.TaxonomyResourceHandler.class);
    public static final String TAXONOMY_XML = "/_system/governance/taxonomy/taxonomy.xml";

    /**
     * This method will execute when user update the taxonomy.xml
     * @param requestContext
     * @throws RegistryException
     */
    public void put(RequestContext requestContext) throws RegistryException {
        try {
            if (requestContext.getResourcePath().toString().equals(TAXONOMY_XML)) {

                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(requestContext.getResource().getContentStream());
                TaxonomyStorageService ins = new TaxonomyStorageService();
                ins.addParseDocument(doc);
                Utils.setTaxonomyService(ins);
            }
        } catch (SAXException e) {
            log.error("SAX Exception occurred while parsing taxonomy.xml doc", e);
            throw new RegistryException("SAX Exception occurred while parsing taxonomy.xml doc", e);
        } catch (IOException e) {
            log.error("IO Exception occurred while reading taxonomy.xml", e);
            throw new RegistryException("IO Exception occurred while reading taxonomy.xml", e);
        } catch (ParserConfigurationException e) {
            log.error("Parser configuration Exception occurred while parsing taxonomy.xml", e);
            throw new RegistryException("Parser configuration Exception occurred while parsing taxonomy.xml", e);
        }

    }
}
