/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.list.util.filter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.list.util.GovernanceArtifactFilter;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

public class FilterService extends FilterStrategy {


    public FilterService(String criteria, Registry governanceRegistry, String artifactKey) {
        super(criteria, governanceRegistry, artifactKey);
    }

    @Override
    public GovernanceArtifact[] getArtifacts() throws GovernanceException {
        Service[] service = new Service[0];
	
	    ServiceManager serviceManger;
   	    try {
                serviceManger = new ServiceManager(this.getGovernanceRegistry());
	    } catch (RegistryException e) {
                throw new GovernanceException("Service Manager Initialization Failed", e);
            }

            final Service referenceService;
            if (this.getCriteria() != null && !"".equals(this.getCriteria())) {
                XMLStreamReader reader = null;
                try {
                    reader = XMLInputFactory.newInstance().createXMLStreamReader(
                            new StringReader(this.getCriteria()));
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                OMElement referenceServiceElement = builder.getDocumentElement();
                referenceService = serviceManger.newService(referenceServiceElement);

                ServiceFilter listServiceFilter = new ServiceFilter() {
                GovernanceArtifactFilter filter = new GovernanceArtifactFilter(referenceService);
                public boolean matches(Service service) throws GovernanceException {
                    return filter.matches(service);
                }
            };
                service = serviceManger.findServices(listServiceFilter);
            }else {
                service = serviceManger.getAllServices();
            }

        return service;
    }

}
