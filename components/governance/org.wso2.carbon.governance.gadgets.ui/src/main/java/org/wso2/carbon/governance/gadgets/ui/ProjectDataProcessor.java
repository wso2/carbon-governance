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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
//import org.wso2.carbon.user.mgt.common.FlaggedName;
import org.wso2.carbon.user.mgt.ui.UserAdminClient;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

public class ProjectDataProcessor {

    private static final Log log = LogFactory.getLog(ProjectDataProcessor.class);
    private Registry registry;
    private UserAdminClient userManager;

    public ProjectDataProcessor(HttpServletRequest request, ServletConfig config) {
        HttpSession session = request.getSession();
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        try {
            this.registry = GovernanceUtils.getGovernanceUserRegistry(
                    new WSRegistryServiceClient(backendServerURL, cookie),
                    (String) session.getAttribute("logged-user"));
        } catch (RegistryException e) {
            log.error("Unable to obtain an instance of the registry.", e);
        }
        try {
            this.userManager = new UserAdminClient(cookie, backendServerURL,
                    configContext);
        } catch (Exception e) {
            log.error("Unable to create connection to User Manager", e);
        }
    }

    public String getJSONTree() throws Exception {
        String key = "projects";
        GenericArtifactManager manager =
                new GenericArtifactManager(registry, key);
        GenericArtifact[] projects = manager.getAllGenericArtifacts();
        List<Map> projectList = new LinkedList<Map>();
        Map projectsMap = Collections.singletonMap("projects",
                Collections.singletonMap("project", projectList));
        if (projects != null) {
            for (GenericArtifact project : projects) {
                Map<String, Object> projectMap = new LinkedHashMap<String, Object>();
                projectMap.put("name", project.getAttribute("overview_name"));
                String projectManager = project.getAttribute("overview_projectManager");
                projectMap.put("manager", projectManager);
                String timeline_startDate = project.getAttribute("timeline_startDate");
                if (timeline_startDate != null) {
                    projectMap.put("startdate", timeline_startDate);
                }
                String timeline_endDate = project.getAttribute("timeline_endDate");
                if (timeline_endDate != null) {
                    projectMap.put("enddate", timeline_endDate);
                }
                String cost_actual = project.getAttribute("cost_actual");
                if (cost_actual != null) {
                    projectMap.put("cost", cost_actual);
                }
                String plan_issueTracker = project.getAttribute("plan_issueTracker");
                if (plan_issueTracker != null) {
                    projectMap.put("jira", plan_issueTracker);
                }
                List<Map> roleList = new LinkedList<Map>();
                projectMap.put("roles", Collections.singletonMap("role", roleList));
                String members_roles = project.getAttribute("members_roles");
                if (members_roles != null) {
                    String[] roles = members_roles.split(",");
                    for (String role : roles) {
                        Map<String, Object> roleMap = new LinkedHashMap<String, Object>();
                        roleMap.put("name", role);
                        List<Map> memberList = new LinkedList<Map>();
                        roleMap.put("member", memberList);
                        FlaggedName[] usersOfRole = userManager.getUsersOfRole(role, "*", -1);
                        for (FlaggedName flaggedName : usersOfRole) {
                            if (flaggedName.getSelected()) {
                                memberList.add(Collections.singletonMap("name",
                                        flaggedName.getItemName()));
                            }
                        }
                        roleList.add(roleMap);
                    }
                }
                Map<String, Object> roleMap = new LinkedHashMap<String, Object>();
                roleMap.put("name", "manager");
                Map<String, Object> memberMap = new LinkedHashMap<String, Object>();
                roleMap.put("member", Collections.singletonMap("name", projectManager));
                roleList.add(roleMap);

                String[] assets = project.getAttributes("assets_entry");
                if (assets != null) {
                    List<String> policies = new LinkedList<String>();
                    List<String> services = new LinkedList<String>();
                    for (String asset : assets) {
                        String[] temp = asset.split(":");
                        if (temp.length == 2) {
                            if (temp[0].equals("Service")) {
                                services.add(temp[1]);
                            } else if (temp[0].equals("Policy")) {
                                policies.add(RegistryUtils.getResourceName(temp[1]));
                            }
                        }
                    }
                    List<Map> policyList = new LinkedList<Map>();
                    projectMap.put("policies", Collections.singletonMap("policy", policyList));
                    for (String policy : policies) {
                        Map<String, Object> policyMap = new LinkedHashMap<String, Object>();
                        policyMap.put("name", policy);
                        policyList.add(policyMap);
                    }
                    List<Map> serviceList = new LinkedList<Map>();
                    Map<String, List<Map>> versionMap = new LinkedHashMap<String, List<Map>>();
                    for (String service : services) {
                        Service artifact =
                                (Service) GovernanceUtils.retrieveGovernanceArtifactByPath(
                                        registry, service);
                        Policy[] attachedPolicies = artifact.getAttachedPolicies();
                        List<String> enforcedPolicies = new LinkedList<String>();
                        if (attachedPolicies != null && attachedPolicies.length >= 0 &&
                                policies.size() > 0) {
                            for (Policy policy : attachedPolicies) {
                                String policyName = RegistryUtils.getResourceName(
                                        policy.getPath());
                                if (policies.contains(policyName)) {
                                    enforcedPolicies.add(policyName);
                                    if (enforcedPolicies.size() == policies.size()) {
                                        break;
                                    }
                                }
                            }
                        }
                        String name = artifact.getQName().getLocalPart();
                        String version = artifact.getAttribute("overview_version");
                        List<String> nonEnforcedPolicies = new LinkedList<String>(policies);
                        nonEnforcedPolicies.removeAll(enforcedPolicies);
                        List<Map> servicePolicies = new LinkedList<Map>();
                        for (String entry : enforcedPolicies) {
                            Map<String, String> enforcement = new HashMap<String, String>();
                            enforcement.put("name", entry);
                            enforcement.put("policyEnforcement", "yes");
                            servicePolicies.add(enforcement);
                        }
                        for (String entry : nonEnforcedPolicies) {
                            Map<String, String> enforcement = new HashMap<String, String>();
                            enforcement.put("name", entry);
                            enforcement.put("policyEnforcement", "no");
                            servicePolicies.add(enforcement);
                        }
                        Map<String, Object> temp = new LinkedHashMap<String, Object>();
                        temp.put("name", version);
                        temp.put("policy", servicePolicies);
                        if (versionMap.containsKey(name)) {
                            versionMap.get(name).add(temp);
                        } else {
                            LinkedList<Map> value = new LinkedList<Map>();
                            value.add(temp);
                            versionMap.put(name, value);
                        }
                    }
                    projectMap.put("services", Collections.singletonMap("service", serviceList));
                    for (Map.Entry<String, List<Map>> e : versionMap.entrySet()) {
                        Map<String, Object> temp = new HashMap<String, Object>();
                        temp.put("name", e.getKey());
                        temp.put("version", e.getValue());
                        serviceList.add(temp);
                    }
                }

                String[] products = project.getAttributes("products_entry");
                if (products != null) {
                    List<String> applications = new LinkedList<String>();
                    List<String> processes = new LinkedList<String>();
                    for (String product : products) {
                        String[] temp = product.split(":");
                        if (temp.length == 2) {
                            if (temp[0].equals("Application")) {
                                applications.add(RegistryUtils.getResourceName(
                                        RegistryUtils.getParentPath(temp[1])));
                            } else if (temp[0].equals("Process")) {
                                processes.add(RegistryUtils.getResourceName(
                                        RegistryUtils.getParentPath(temp[1])));
                            }
                        }
                    }
                    Map<String, Map> productMap = new HashMap<String, Map>();
                    projectMap.put("production", productMap);

                    List<Map> applicationList = new LinkedList<Map>();
                    productMap.put("applications", Collections.singletonMap("application",
                            applicationList));
                    for (String application : applications) {
                        Map<String, Object> applicationMap = new LinkedHashMap<String, Object>();
                        applicationMap.put("name", application);
                        applicationList.add(applicationMap);
                    }

                    List<Map> processList = new LinkedList<Map>();
                    productMap.put("processes", Collections.singletonMap("process", processList));
                    for (String process : processes) {
                        Map<String, Object> processMap = new LinkedHashMap<String, Object>();
                        processMap.put("name", process);
                        processList.add(processMap);
                    }
                }
                projectList.add(projectMap);
            }
        }
        JSONObject object = new JSONObject(projectsMap);
        return object.toString();
	}


}
