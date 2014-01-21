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

import org.apache.axis2.AxisFault;
import org.wso2.carbon.governance.gadgets.stub.GovernanceException;
import org.wso2.carbon.governance.gadgets.stub.governance.gadgets.impactanalysis.beans.xsd.*;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;


public class GovImpactAnalysisDataProcessor {

    private GovImpactAdminServiceClient client;
    private String serverURL;

    public GovImpactAnalysisDataProcessor(ServletConfig config,
                                          HttpSession session, HttpServletRequest request) throws AxisFault {
        client =  new GovImpactAdminServiceClient(config, session, request);
        serverURL = CarbonUIUtil.getAdminConsoleURL(
                CarbonUIUtil.getServerConfigurationProperty("WebContextRoot"));
    }

    public String getImpactAnalysisinJSON() throws RemoteException, GovernanceException {
        ImpactBean bean = client.getImpactAnalysis();

//		if (bean.getServiceBean() == null) return "";
        return buildImpactJSONTree(bean.getServiceBean());
    }

    private String getResourceURL(String path) {
        return serverURL + "resources/resource.jsp?region=region3&item=resource_browser_menu&path=" + "/_system/governance" + path;
    }

    private Map<String, Map<String, ServiceBean>> buildServiceVersionHierarchy(
            ServiceBean[] serviceBeans) {
        Map<String, Map<String, ServiceBean>> hierarchy =
                new LinkedHashMap<String, Map<String, ServiceBean>>();
        if (serviceBeans == null) {
            return hierarchy;
        }
        for (ServiceBean serviceBean : serviceBeans) {
            if (serviceBean != null) {
                String path = RegistryUtils.getParentPath(serviceBean.getPath());
                String version = RegistryUtils.getResourceName(path);
                if (!version.replace("-SNAPSHOT", "").matches("^\\d+[.]\\d+[.]\\d+$")) {
                    version = "SNAPSHOT";
                    serviceBean.setPath(serviceBean.getPath());
                } else {
                    serviceBean.setPath(RegistryUtils.getParentPath(path));
                }
                Map<String, ServiceBean> versionMap;
                if (hierarchy.containsKey(serviceBean.getQName())) {
                    versionMap = hierarchy.get(serviceBean.getQName());
                } else {
                    versionMap = new LinkedHashMap<String, ServiceBean>();
                    hierarchy.put(serviceBean.getQName(), versionMap);
                }
                versionMap.put(version, serviceBean);
            }
        }
        return hierarchy;
    }

    private String buildImpactJSONTree(ServiceBean[] serviceBeans) {
        StringBuilder builder = new StringBuilder();
        builder.append("{ " + buildJSONforNoEdgeAttributes(UUID.randomUUID().toString(), "Services", null) + appendChildrenJSON());
        int count = 0;
        Map<String, Map<String, ServiceBean>> serviceVersionHierarchy = buildServiceVersionHierarchy(serviceBeans);
        for (Map.Entry<String, Map<String, ServiceBean>> e : serviceVersionHierarchy.entrySet()) {
            Map<String, ServiceBean> versionMap = e.getValue();
            count++;
            int countInner = 0;
            for (Map.Entry<String, ServiceBean> versionEntry : versionMap.entrySet()) {
                countInner++;
                String key = versionEntry.getKey();
                ServiceBean serviceBean = versionEntry.getValue();
                if (countInner == 1) {
                    builder.append("{" + buildJSONforNoEdgeAttributes(serviceBean.getId(), serviceBean.getQName(), getResourceURL(serviceBean.getPath())) + appendChildrenJSON() + "\n");
                }
                String uuid = UUIDGenerator.generateUUID();
                String servicePath;
                if (key.equals("SNAPSHOT")) {
                    servicePath = serviceBean.getPath();
                } else {
                    String name = serviceBean.getQName();
                    name = name.substring(0,name.lastIndexOf("-") -1).trim();
                    servicePath = serviceBean.getPath() + "/" + key + "/" + name;
                }
                builder.append("{" + buildOpenJSONWithEdges(uuid, key, serviceBean.getId(), "", getResourceURL(
                        servicePath)) + appendChildrenJSON() + "\n");
                    PolicyBean[] policyBeans = serviceBean.getPolicyBeans();
                    WSDLBean[] wsdlBeans = serviceBean.getWsdlBeans();
                    SchemaBean[] schemaBeans = serviceBean.getSchemaBeans();
                    if (policyBeans != null) {
                        for (int j = 0; j < policyBeans.length; j++) {
                            if (policyBeans[j] != null) {
                                builder.append("{" + buildJSONWithEdgeNoChildren(policyBeans[j].getId(), policyBeans[j].getQName(), uuid, "Policy",
                                        getResourceURL(policyBeans[j].getPath())) + "} \n");
                                if (j != (policyBeans.length - 1))
                                    builder.append(",");
                                builder.append("\n");
                            }
                        }
                        builder.append(buildCloseJSONForNameandChildren());
                        if (wsdlBeans != null || schemaBeans != null) {
                            builder.append(", \n");
                        }
                    }
                    if (wsdlBeans != null) {
                        for (int j = 0; j < wsdlBeans.length; j++) {
                            if (wsdlBeans[j] != null) {
                                builder.append("{" + buildOpenJSONWithEdges(wsdlBeans[j].getId(), wsdlBeans[j].getQName(), uuid, "WSDL",
                                        getResourceURL(wsdlBeans[j].getPath())) + appendChildrenJSON() +" \n");
                                SchemaBean[] attachedSchemas = wsdlBeans[j].getAttachedSchemas();
                                if (attachedSchemas != null) {
                                    for (int k = 0; k < attachedSchemas.length; k++) {
                                        if (attachedSchemas[k] != null) {
                                            builder.append("{" + buildJSONWithEdgeNoChildren(attachedSchemas[k].getId(), attachedSchemas[k].getQName(), wsdlBeans[j].getId(), "Schema",
                                                    getResourceURL(attachedSchemas[k].getPath())) + "} \n");
                                            if (j != (attachedSchemas.length - 1)) {
                                                builder.append(",");
                                            }
                                            builder.append("\n");
                                        }
                                    }
                                }
                                builder.append("] \n");
                                builder.append("} \n");
                                if (j != (wsdlBeans.length - 1))
                                    builder.append(",");
                                builder.append("\n");
                            }
                        }
                        if (schemaBeans != null) {
                            builder.append(", \n");
                        }
                    }
                    if (schemaBeans != null) {
                        for (int j = 0; j < schemaBeans.length; j++) {
                            if (schemaBeans[j] != null) {
                                builder.append("{" + buildJSONWithEdgeNoChildren(schemaBeans[j].getId(), schemaBeans[j].getQName(), uuid, "Schema",
                                        getResourceURL(schemaBeans[j].getPath())) + "} \n");
                                if (j != (schemaBeans.length - 1))
                                    builder.append(",");
                                builder.append("\n");
                            }
                        }
                    }
                    builder.append("] \n");
                    builder.append("} \n");
                    if (countInner != (versionMap.size())) {
                        builder.append(", ");
                    }
                    builder.append("\n");
            }
            builder.append("] \n");
            builder.append("} \n");
            if (count != (serviceVersionHierarchy.size())) {
                builder.append(", ");
            }
            builder.append("\n");
        }
        builder.append("] \n");
        builder.append("} \n");

        return builder.toString();

    }

    private String buildJSONTree(ServiceBean[] serviceBeans) {
        StringBuilder builder = new StringBuilder();

        builder.append(buildOpenJSONForNameandChildren("Services"));
        if (serviceBeans != null) {
            for (int i = 0; i < serviceBeans.length; i++) {
                if (serviceBeans[i] != null) {
                    //				builder.append("\"service\" : \n");
                    builder.append("{" + buildJSONforNoEdgeAttributes(serviceBeans[i].getId(), serviceBeans[i].getQName(), serviceBeans[i].getPath()) + appendChildrenJSON() + "\n");
                    PolicyBean[] policyBeans = serviceBeans[i].getPolicyBeans();
                    WSDLBean[] wsdlBeans = serviceBeans[i].getWsdlBeans();
                    SchemaBean[] schemaBeans = serviceBeans[i].getSchemaBeans();
                    //				if (wsdlBeans != null || schemaBeans != null || policyBeans != null) {
                    //					builder.append(", \n");
                    //				}
                    if (policyBeans != null) {
                        builder.append(buildOpenJSONForNameandChildren("Policies"));
                        for (int j = 0; j < policyBeans.length; j++) {
                            if (policyBeans[j] != null) {
                                builder.append("{"
                                        + buildJSONwithNoChildren(policyBeans[j]
                                        .getId(),
                                        policyBeans[j].getQName(),
                                        policyBeans[j].getPath()) + "} ");
                                if (j != (policyBeans.length - 1))
                                    builder.append(",");
                                builder.append("\n");
                            }
                        }
                        builder.append(buildCloseJSONForNameandChildren());
                        if (wsdlBeans != null || schemaBeans != null) {
                            builder.append(", \n");
                        }
                    }

                    if (wsdlBeans != null) {
                        builder.append(buildOpenJSONForNameandChildren("WSDLs"));

                        for (int j = 0; j < wsdlBeans.length; j++) {
                            if (wsdlBeans[j] != null) {
                                builder.append("{"
                                        + buildJSONforNoEdgeAttributes(wsdlBeans[j]
                                        .getId(), wsdlBeans[j].getQName(),
                                        wsdlBeans[j].getPath()) + appendChildrenJSON() + "\n" );
                                SchemaBean[] attachedSchemas = wsdlBeans[j].getAttachedSchemas();
                                if (attachedSchemas != null) {
                                    builder.append(buildOpenJSONForNameandChildren("Schemas"));
                                    for (int k = 0;k < attachedSchemas.length; k++) {
                                        if (attachedSchemas[k] != null) {
                                            builder.append("{"
                                                    + buildJSONwithNoChildren(attachedSchemas[k]
                                                    .getId(),
                                                    attachedSchemas[k].getQName(),
                                                    attachedSchemas[k].getPath()) + "} ");
                                            if (k != (attachedSchemas.length - 1))
                                                builder.append(",");
                                            builder.append("\n");
                                        }
                                    }
                                    builder.append(buildCloseJSONForNameandChildren());
                                }
                                builder.append("] \n");
                                builder.append("} \n");
                                if (j != (wsdlBeans.length - 1))
                                    builder.append(",");
                                builder.append("\n");
                            }
                        }
                        builder.append(buildCloseJSONForNameandChildren());
                        if (schemaBeans != null) {
                            builder.append(", \n");
                        }
                    }

                    if (schemaBeans != null) {
                        builder.append(buildOpenJSONForNameandChildren("Schemas"));
                        for (int j = 0; j < schemaBeans.length; j++) {
                            if (schemaBeans[j] != null) {
                                builder.append("{"
                                        + buildJSONwithNoChildren(schemaBeans[j]
                                        .getId(),
                                        schemaBeans[j].getQName(),
                                        schemaBeans[j].getPath()) + "} ");
                                if (j != (schemaBeans.length - 1))
                                    builder.append(",");
                                builder.append("\n");
                            }
                        }
                        builder.append(buildCloseJSONForNameandChildren());
                    }
                    builder.append("] \n");
                    builder.append("} \n");
                    if (i != (serviceBeans.length - 1)) {
                        builder.append(", ");
                    }
                    builder.append("\n");
                }
            }
        }
        builder.append(buildCloseJSONForNameandChildren());

        return builder.toString();
    }

    private String buildOpenJSONWithEdges (String id, String name, String parentId, String edgeName, String url) {
        StringBuilder builder = new StringBuilder();
        builder.append(buildJSONNameValuePair("id", id) + "," + buildJSONNameValuePair("name", name) + "," + "\"data\" : { " +
                buildJSONEdge(parentId, id, edgeName) + "," + buildJSONNameValuePair("url", url) + "} ");
        return builder.toString();
    }

    private String buildJSONEdge(String parentId, String id, String edgeName) {
        StringBuilder builder = new StringBuilder();
        builder.append("\"edges\" : [ { " + buildJSONNameValuePair("id", parentId + "---" + id) + "," + buildJSONNameValuePair("name", edgeName) + "} ] ");
        return builder.toString();
    }

    private String buildJSONWithEdgeNoChildren (String id, String name, String parentId, String edgeName, String url) {
        StringBuilder builder = new StringBuilder();
        builder.append(buildOpenJSONWithEdges(id, name, parentId, edgeName, url));
        builder.append(", " + buildJSONNameValuePair("children", "[]"));
        return builder.toString();
    }

    @Deprecated
    private String buildCloseJSONForNameandChildren() {
        StringBuilder builder = new StringBuilder();
        builder.append("] } \n");
        return builder.toString();
    }

    @Deprecated
    private String buildOpenJSONForNameandChildren(String name) {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        builder.append(buildJSONforNoEdgeAttributes(UUID.randomUUID().toString(), name, "") + appendChildrenJSON());
        return builder.toString();
    }

    private String appendChildrenJSON() {
        return ", \"children\" : [";
    }

    @Deprecated
    private String buildJSONobject(ServiceBean[] serviceBeans) {
        StringBuilder builder = new StringBuilder();
        builder.append("{ \n");
        for (int i = 0; i < serviceBeans.length; i++) {
            if (serviceBeans[i] != null) {
                builder.append("\"service\" : \n");
                builder.append("{" + buildJSONforNoEdgeAttributes(serviceBeans[i].getId(), serviceBeans[i].getQName(), serviceBeans[i].getPath()) + "\n");
                PolicyBean[] policyBeans = serviceBeans[i].getPolicyBeans();
                WSDLBean[] wsdlBeans = serviceBeans[i].getWsdlBeans();
                SchemaBean[] schemaBeans = serviceBeans[i].getSchemaBeans();
                if (wsdlBeans != null || schemaBeans != null || policyBeans != null) {
                    builder.append(", \n");
                }
                if (policyBeans != null) {
                    builder.append("\"policies\" : [\n");
                    for (int j = 0; j < policyBeans.length; j++) {
                        if (policyBeans[j] != null) {
                            builder.append("{"
                                    + buildJSONforNoEdgeAttributes(policyBeans[j]
                                    .getId(),
                                    policyBeans[j].getQName(),
                                    policyBeans[j].getPath()) + "} ");
                            if (j != (policyBeans.length - 1))
                                builder.append(",");
                            builder.append("\n");
                        }
                    }
                    builder.append("] \n");
                    if (wsdlBeans != null || schemaBeans != null) {
                        builder.append(", \n");
                    }
                }

                if (wsdlBeans != null) {
                    builder.append("\"wsdls\" : [\n");

                    for (int j = 0; j < wsdlBeans.length; j++) {
                        if (wsdlBeans[j] != null) {
                            builder.append("{"
                                    + buildJSONforNoEdgeAttributes(wsdlBeans[j]
                                    .getId(), wsdlBeans[j].getQName(),
                                    wsdlBeans[j].getPath()) + "} ");
                            if (j != (wsdlBeans.length - 1))
                                builder.append(",");
                            builder.append("\n");
                        }
                    }
                    builder.append("] \n");
                    if (schemaBeans != null) {
                        builder.append(", \n");
                    }
                }

                if (schemaBeans != null) {
                    builder.append("\"schemas\" : [\n");
                    for (int j = 0; j < schemaBeans.length; j++) {
                        if (schemaBeans[j] != null) {
                            builder.append("{"
                                    + buildJSONforNoEdgeAttributes(schemaBeans[j]
                                    .getId(),
                                    schemaBeans[j].getQName(),
                                    schemaBeans[j].getPath()) + "} ");
                            if (j != (schemaBeans.length - 1))
                                builder.append(",");
                            builder.append("\n");
                        }
                    }
                    builder.append("] \n");
                }
                builder.append("} \n");
                if (i != (serviceBeans.length - 1)) {
                    builder.append(", ");
                }
                builder.append("\n");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    @Deprecated
    private String buildJSONforBeanAttributes(String id, String name, String path, String dataType, String parentId) {
        StringBuilder builder = new StringBuilder();
        builder.append(buildJSONNameValuePair("id", id) + "," + buildJSONNameValuePair("name", name) + ",");
        builder.append(buildJSONNameValuePair("data", "{}"));
        return builder.toString();
    }

    private String buildJSONforNoEdgeAttributes(String id, String name, String url) {
        StringBuilder builder = new StringBuilder();
        builder.append(buildJSONNameValuePair("id", id) + "," + buildJSONNameValuePair("name", name) + ",");
        if (url == null || url.equals("")) {
            builder.append(buildJSONNameValuePair("data", "{}"));
        } else {
            builder.append("\"data\" : { " + buildJSONNameValuePair("url", url) +" }");
        }
        return builder.toString();
    }

    @Deprecated
    private String buildJSONwithNoChildren(String id, String name, String path) {
        StringBuilder builder = new StringBuilder();
        builder.append(buildJSONforNoEdgeAttributes(id, name, path));
        builder.append(", ").append(buildJSONNameValuePair("children", "[]"));
        return builder.toString();
    }

    private String buildJSONNameValuePair(String name, String value) {
        if (value.contentEquals("[]") || value.contentEquals("{}")) return "\"" + name + "\" : " + value + " ";
        return "\"" + name + "\" : \"" + value + "\" ";

    }
}	
