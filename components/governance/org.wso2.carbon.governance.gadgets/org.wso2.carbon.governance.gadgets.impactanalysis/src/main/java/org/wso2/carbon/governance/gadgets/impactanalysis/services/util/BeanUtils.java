/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.gadgets.impactanalysis.services.util;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.gadgets.impactanalysis.beans.ImpactBean;
import org.wso2.carbon.governance.gadgets.impactanalysis.beans.PolicyBean;
import org.wso2.carbon.governance.gadgets.impactanalysis.beans.SchemaBean;
import org.wso2.carbon.governance.gadgets.impactanalysis.beans.ServiceBean;
import org.wso2.carbon.governance.gadgets.impactanalysis.beans.WSDLBean;

public class BeanUtils {
	private static final Log log = LogFactory.getLog(BeanUtils.class);
	public static ImpactBean populateImpactBean(ServiceManager manager) throws GovernanceException {
		
		ImpactBean impactBean = new ImpactBean(); 
		impactBean.setServiceBean(populateServicesBean(manager));
		return impactBean;
	}

	public static ServiceBean[] populateServicesBean(ServiceManager manager) throws GovernanceException  {
		String[] serviceIds = manager.getAllServiceIds();
		if (serviceIds == null) return null;
		
		ServiceBean[] serviceBeans = new ServiceBean[serviceIds.length];
		for (int i = 0; i < serviceIds.length; i++) {
			serviceBeans[i] = new ServiceBean();
			if (serviceIds[i] != null) {
				Service service = manager.getService(serviceIds[i]);
                                if(service != null ) {
				    serviceBeans[i].setWsdlBeans(populateWSDLBean(service.getAttachedWsdls()));
				    serviceBeans[i].setSchemaBeans(populateSchemaBean(service.getAttachedSchemas()));
				    serviceBeans[i].setPolicyBeans(populatePolicyBean(service.getAttachedPolicies()));
				    serviceBeans[i].setId(service.getId());
				    serviceBeans[i].setPath(service.getPath());
				    serviceBeans[i].setqName(getName(service.getQName()));
                                } 
			}
		}
		return serviceBeans;
	}
	
	public static WSDLBean[] populateWSDLBean(Wsdl[] wsdls) throws GovernanceException {
		if (wsdls == null) return null;
        WSDLBean[] wsdlBean = new WSDLBean[wsdls.length];
        for (int j = 0; j < wsdls.length; j++) {
			wsdlBean[j] = new WSDLBean();
			if (wsdls[j] != null) {
				wsdlBean[j].setId(wsdls[j].getId());
				wsdlBean[j].setPath(wsdls[j].getPath());
				wsdlBean[j].setqName(getName(wsdls[j].getQName()));
				wsdlBean[j].setAttachedSchemas(populateSchemaBean(wsdls[j].getAttachedSchemas()));
			}
		}
		return wsdlBean;
	}
	
	public static PolicyBean[] populatePolicyBean(Policy[] policies) {
		if (policies == null) return null;
        PolicyBean[] policyBean = new PolicyBean[policies.length];
        for (int j = 0; j < policies.length; j++) {
			policyBean[j] = new PolicyBean();
			if (policies[j] != null) {
				policyBean[j].setId(policies[j].getId());
				    try {
                        policyBean[j].setPath(policies[j].getPath());
                    } catch (GovernanceException e) {
                        String msg = "Error in getting the path from the policy.";
                        log.error(msg, e);
                    }
				policyBean[j].setqName(getName(policies[j].getQName()));
			}
		}
		return policyBean;
	}
	
	public static SchemaBean[] populateSchemaBean(Schema[] schemas) {
		if (schemas == null) return null;
        SchemaBean[] schemaBean = new SchemaBean[schemas.length];
        for (int j = 0; j < schemas.length; j++) {
			schemaBean[j] = new SchemaBean();
			if (schemas[j] != null) {
				schemaBean[j].setId(schemas[j].getId());
				try {
                    schemaBean[j].setPath(schemas[j].getPath());
                } catch (GovernanceException e) {
                    String msg = "Error in getting the path.";
                    log.error(msg);
                }
				schemaBean[j].setqName(getName(schemas[j].getQName()));
			}
		}
		return schemaBean;
	}
	
	private static String getName(QName qName) {
		return qName.getLocalPart() + " - " + qName.getNamespaceURI();
	}
	
}
