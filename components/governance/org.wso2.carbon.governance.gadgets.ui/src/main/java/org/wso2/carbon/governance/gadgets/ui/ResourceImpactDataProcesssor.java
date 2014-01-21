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
package org.wso2.carbon.governance.gadgets.ui;

import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyFilter;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaFilter;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlFilter;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.gadgets.stub.beans.xsd.AssociationBean;
import org.wso2.carbon.governance.gadgets.ui.beans.AssociationBeanLocal;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

public class ResourceImpactDataProcesssor {

	ResourceImpactAdminClient client;
	Registry registry;
    boolean reverse = false;

	public ResourceImpactDataProcesssor(ServletConfig config,
			HttpSession session, HttpServletRequest request) throws Exception {
		client =  new ResourceImpactAdminClient(config, session, request);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        registry = GovernanceUtils.getGovernanceUserRegistry(
                new WSRegistryServiceClient(backendServerURL, cookie),
                (String) session.getAttribute("logged-user"));
	}

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public String getResourceImpactJSONTree(String path) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append(buildOpenJSONForNameandChildren("Assets"));
        Set<String> processedPathList = new HashSet<String>();
        if (path.length() == 0 || path.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            addChildPathsToBuilder(path, builder, true, processedPathList);
        } else {
            final String referenceValue = path;
            boolean first = true;
            Service[] services = new ServiceManager(registry).findServices(new ServiceFilter() {
                public boolean matches(Service service) throws GovernanceException {
                    String realValue = service.getQName().getLocalPart();
                    try {
                        return realValue.contains(referenceValue) ||
                                realValue.matches(referenceValue);
                    } catch (Exception e) {
                        throw new GovernanceException("Error in performing the matches for: " +
                                referenceValue + ".", e);
                    }
                }
            });
            for (Service service : services) {
                if (first) {
                    first = false;
                } else {
                    builder.append(" , \n");
                }
                addChildPathsToBuilder(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        service.getPath(), builder, true, processedPathList);
            }
            Wsdl[] wsdls = new WsdlManager(registry).findWsdls(new WsdlFilter() {
                public boolean matches(Wsdl wsdl) throws GovernanceException {
                    String realValue = wsdl.getQName().getLocalPart();
                    try {
                        return realValue.contains(referenceValue) ||
                                realValue.matches(referenceValue);
                    } catch (Exception e) {
                        throw new GovernanceException("Error in performing the matches for: " +
                                referenceValue + ".", e);
                    }
                }
            });
            for (Wsdl wsdl : wsdls) {
                if (first) {
                    first = false;
                } else {
                    builder.append(" , \n");
                }
                addChildPathsToBuilder(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        wsdl.getPath(), builder, true, processedPathList);
            }
            Schema[] schemas = new SchemaManager(registry).findSchemas(new SchemaFilter() {
                public boolean matches(Schema schema) throws GovernanceException {
                    String realValue = schema.getQName().getLocalPart();
                    try {
                        return realValue.contains(referenceValue) ||
                                realValue.matches(referenceValue);
                    } catch (Exception e) {
                        throw new GovernanceException("Error in performing the matches for: " +
                                referenceValue + ".", e);
                    }
                }
            });
            for (Schema schema : schemas) {
                if (first) {
                    first = false;
                } else {
                    builder.append(" , \n");
                }
                addChildPathsToBuilder(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        schema.getPath(), builder, true, processedPathList);
            }
            Policy[] policies = new PolicyManager(registry).findPolicies(new PolicyFilter() {
                public boolean matches(Policy policy) throws GovernanceException {
                    String realValue = policy.getQName().getLocalPart();
                    try {
                        return realValue.contains(referenceValue) ||
                                realValue.matches(referenceValue);
                    } catch (Exception e) {
                        throw new GovernanceException("Error in performing the matches for: " +
                                referenceValue + ".", e);
                    }
                }
            });
            for (Policy policy : policies) {
                if (first) {
                    first = false;
                } else {
                    builder.append(" , \n");
                }
                addChildPathsToBuilder(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
                        policy.getPath(), builder, true, processedPathList);
            }
        }
        builder.append(buildCloseJSONForNameandChildren());
        return builder.toString();
	}

    private boolean addChildPathsToBuilder(String path, StringBuilder builder,
                                        boolean addEmptyMessage, Set<String> processedPaths)
            throws Exception {
        ArrayList<AssociationBeanLocal> associationBeans = getBean(path, processedPaths, true);
        return processBean(path, builder, addEmptyMessage, processedPaths, associationBeans);
    }

    private boolean processBean(String path, StringBuilder builder, boolean addEmptyMessage,
                                Set<String> processedPaths,
                                ArrayList<AssociationBeanLocal> associationBeans) throws Exception {
        associationBeans.trimToSize();
        if (!associationBeans.isEmpty()) {
            builder.append(buildOpenJSONForNameandChildren(trimPath(path)));
            int k = 1;
            for (AssociationBeanLocal bean : associationBeans) {
                String currentType = bean.getAssociationType();
                builder.append(buildOpenJSONForNameandChildren(currentType));
                ArrayList<AssociationBeanLocal.DestinationPath> paths = bean.getDestinationPaths();
                paths.trimToSize();
                int i = 1;
                for (AssociationBeanLocal.DestinationPath destinationPath : paths) {
                    String destination = trimPath(destinationPath.getPath());
                    builder.append(buildOpenJSONForNameandChildren(destination));
                    if (destinationPath.getAssociations().size() > 0) {
                        addChildPathsToBuilder(destinationPath.getPath(), builder, false,
                                processedPaths);
                    }
                    builder.append(buildCloseJSONForNameandChildren());

                    if (i != paths.size()) {
                        builder.append(" , \n");
                    }
                    i++;
                }
                builder.append("\n");
                builder.append(buildCloseJSONForNameandChildren());
                if (k != associationBeans.size()) {
                    builder.append(" , \n");
                }
                k++;
            }
            builder.append(buildCloseJSONForNameandChildren());
            return true;
        } else {
            if (addEmptyMessage) {
                builder.append(
                        buildOpenJSONForNameandChildren(""));
            }
            builder.append(buildCloseJSONForNameandChildren());
            return false;
        }
    }

    private String trimPath(String originalPath) {
		String[] parts = originalPath.split("/");
        int index = parts.length - 1;
        String part = parts[index];
        if (index == 0) {
            return part;
        } else if (part.equals("service")) {
            index--;
            part = parts[index];
        }
        if (index != 0) {
            String regEx = CommonConstants.SERVICE_VERSION_REGEX.replace("$", "(-[a-zA-Z0-9]+)?$");
            if (part.matches(regEx)) {
                return parts[index - 1] + " - " + part;
            } else if (parts[index - 1].matches(regEx)) {
                return part + " - " + parts[index - 1];
            }
        }
        return part;
	}


	private ArrayList<AssociationBeanLocal> getBean(String path,
                                                    Set<String> processedList,
                                                    boolean expandPaths) throws Exception {
		AssociationBean[] associations = client.getResourceAssociations(path, reverse);

		ArrayList<String> associationTypeList = new ArrayList<String>();
		ArrayList<AssociationBeanLocal> beanList = new ArrayList<AssociationBeanLocal>();
        Map<String, AssociationBeanLocal> pathMap =
                new LinkedHashMap<String, AssociationBeanLocal>();
		if (associations != null) {
			for (int i = 0; i < associations.length; i++) {
				String currentType = associations[i].getAssociationType();
				String currentDestPath = associations[i].getDestinationPath();
				String currentSourcePath = associations[i].getSourcePath();
				String pathToAdd = (currentDestPath.equals(path)) ? (currentSourcePath) : (currentDestPath);
				if (!associationTypeList.contains(currentType)) {
					associationTypeList.add(currentType);
					AssociationBeanLocal assoBean = new AssociationBeanLocal();
					assoBean.setAssociationType(currentType);
                    if (expandPaths) {
					    pathMap.put(pathToAdd, assoBean);
                    }
					beanList.add(assoBean);
				} else if (expandPaths) {
					for (AssociationBeanLocal bean : beanList) {
						if (bean.getAssociationType().equals(currentType)) {
                            pathMap.put(pathToAdd, bean);
						}
					}
				}
			}
            for (Map.Entry<String, AssociationBeanLocal> e : pathMap.entrySet()) {
                String key = e.getKey();
                if (processedList.contains(e.getKey())) {
                    e.getValue().setDestinationPaths(key, new ArrayList<AssociationBeanLocal>());
                } else {
                    processedList.add(key);
                    e.getValue().setDestinationPaths(key, getBean(key, processedList, true));
                }
            }
		}
		return beanList;
	}
	private String buildCloseJSONForNameandChildren() {
		StringBuilder builder = new StringBuilder();
		builder.append("] } \n");
		return builder.toString();
	}
	
	private String buildOpenJSONForNameandChildren(String name) {
		StringBuilder builder = new StringBuilder();
		builder.append("{ ");
		builder.append(buildJSONforBeanAttributes(UUID.randomUUID().toString(), name, "") +
                appendChildrenJSON());
		return builder.toString();
	}
	
	private String appendChildrenJSON() {
		return ", \"children\" : [";
	}
	
	
	private String buildJSONforBeanAttributes(String id, String name, String path) {
		StringBuilder builder = new StringBuilder();
		builder.append(buildJSONNameValuePair("id", id) + "," + buildJSONNameValuePair("name", name) + "," + buildJSONNameValuePair("data", "{}"));
		return builder.toString();
	}
	
	private String buildJSONwithNoChildren(String name) {
		StringBuilder builder = new StringBuilder();
		builder.append("{ ").append(buildJSONforBeanAttributes(UUID.randomUUID().toString(), name, ""));
		builder.append(", ").append(buildJSONNameValuePair("children", "[]")).append("}");
		return builder.toString();		
	}
	
	private String buildJSONNameValuePair(String name, String value) {
		if (value.contentEquals("[]") || value.contentEquals("{}")) return "\"" + name + "\" : " + value + " ";
		return "\"" + name + "\" : \"" + value + "\" ";
		
	}
}
