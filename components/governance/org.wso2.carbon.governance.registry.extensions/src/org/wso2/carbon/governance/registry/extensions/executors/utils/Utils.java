/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.wso2.carbon.governance.registry.extensions.executors.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);

    public static String formatPath(String path){
        if(path.startsWith(RegistryConstants.PATH_SEPARATOR)){
            path = path.substring(1);
        }
        if(path.endsWith(RegistryConstants.PATH_SEPARATOR)){
            path = path.substring(0,path.length() -1);
        }
        return path;
    }
    public static void addNewId(Registry registry, Resource newResource, String newPath) throws RegistryException {
        String artifactID = UUID.randomUUID().toString();
        newResource.setUUID(artifactID);
//        CommonUtil.addGovernanceArtifactEntryWithAbsoluteValues(registry, artifactID, newPath);
    }

    public static String getResourceContent(Resource tempResource) throws RegistryException {
        if (tempResource.getContent() instanceof String) {
            return (String) tempResource.getContent();
        } else if (tempResource.getContent() instanceof byte[]) {
            return RegistryUtils.decodeBytes((byte[]) tempResource.getContent());
        }
        return null;
    }

    public static OMElement getServiceOMElement(Resource newResource) {
        try {
            String content = getResourceContent(newResource);
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(
                    new StringReader(content));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement serviceElement = builder.getDocumentElement();
            serviceElement.build();
            return serviceElement;
        } catch (Exception e) {
            log.error("Error in parsing the resource content");
        }
        return null;
    }

    public static boolean populateParameterMap(RequestContext requestContext, Map<String, String> currentParameterMap) {
        Set parameterMapKeySet = (Set) requestContext.getProperty("parameterNames");
        if (parameterMapKeySet == null) {
            log.warn("No parameters where found");
            return true;
        }
        for (Object entry : parameterMapKeySet) {
            String key = (String) entry;
            if (!key.equals("preserveOriginal")) {
                currentParameterMap.put(key, (String) requestContext.getProperty(key));
            }
        }
        return true;
    }

    public static void copyAssociations(Registry registry, String newPath, String path) throws RegistryException {
        Association[] associations = registry.getAllAssociations(path);
        for (Association association : associations) {
            if (!association.getAssociationType().equals(CommonConstants.DEPENDS)) {
                if (association.getSourcePath().equals(path)) {
                    registry.addAssociation(newPath,
                            association.getDestinationPath(), association.getAssociationType());
                } else {
                    registry.addAssociation(association.getSourcePath(), newPath,
                            association.getAssociationType());
                }
            }
        }
    }

    public static void copyRatings(Registry registry, String newPath, String path) throws RegistryException {
        float averageRating = registry.getAverageRating(path);
        registry.rateResource(newPath,
                new Float(averageRating).intValue());
    }

    public static void copyTags(Registry registry, String newPath, String path) throws RegistryException {
        Tag[] tags = registry.getTags(path);
        for (Tag tag : tags) {
            registry.applyTag(newPath, tag.getTagName());
        }
    }

    public static void copyComments(Registry registry, String newPath, String path) throws RegistryException {
        Comment[] comments = registry.getComments(path);
        for (Comment comment : comments) {
            registry.addComment(newPath, comment);
        }
    }
    /*
   * This method creates the associations between the new resource and its new dependant resource.
   * */
    public static void makeDependencies(RequestContext requestContext, Map<String, String> parameterMap
            , Map<String, String> oldPathNewPathMap) throws RegistryException {

        Registry registry = requestContext.getRegistry();

        if (!CommonUtil.isAddingAssociationLockAvailable()) {
            return;
        }
        CommonUtil.acquireAddingAssociationLock();
        try {
            for (Map.Entry<String, String> entry : oldPathNewPathMap.entrySet()) {
                Association[] associations = registry.getAllAssociations(entry.getValue());
                for (Association association : associations) {
                    if (!(oldPathNewPathMap.containsValue(association.getSourcePath()))
                            || !(oldPathNewPathMap.containsValue(association.getDestinationPath()))) {
                        registry.removeAssociation(association.getSourcePath(), association.getDestinationPath()
                                , association.getAssociationType());
                    }
                }
            }
            for (Map.Entry<String, String> keyValueSet : parameterMap.entrySet()) {
                Association[] associations = registry.getAllAssociations(keyValueSet.getKey());
                for (Association association : associations) {
                    if (oldPathNewPathMap.containsKey(association.getDestinationPath())
                            && oldPathNewPathMap.containsKey(association.getSourcePath())) {
                        registry.addAssociation(
                                oldPathNewPathMap.get(association.getSourcePath())
                                , oldPathNewPathMap.get(association.getDestinationPath())
                                , association.getAssociationType());
                    }
                }
            }
        } finally {
            CommonUtil.releaseAddingAssociationLock();
        }
    }

    public static void makeOtherDependencies(RequestContext requestContext, Map<String, String> oldPathNewPathMap
            , List<String> otherDependencies) throws RegistryException {

        Registry registry = requestContext.getRegistry();

        for (Map.Entry<String, String> entry : oldPathNewPathMap.entrySet()) {
            Association[] associations = registry.getAllAssociations(entry.getKey());

            for (Association association : associations) {
                for (String dependency : otherDependencies) {
                    if (association.getDestinationPath().equals(dependency)) {
                        registry.addAssociation(entry.getValue(), dependency, association.getAssociationType());
                    }
                    if (association.getSourcePath().equals(dependency)) {
                        registry.addAssociation(dependency, entry.getValue(), association.getAssociationType());
                    }

                }
            }
        }
    }
    public static List evaluateXpath(OMElement contentElement, String xpathString) {
        List ret = new ArrayList();
        try {
            AXIOMXPath xpath = new AXIOMXPath(xpathString);

            Iterator nsIterator = contentElement.getAllDeclaredNamespaces();
            while (nsIterator.hasNext()) {
                OMNamespace next = (OMNamespace) nsIterator.next();
                xpath.addNamespace("x", next.getNamespaceURI());
                ret.addAll(xpath.selectNodes(contentElement));
            }
            return ret;
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

	/**
	 * Authenticate to API Manager
	 *
	 * @param httpContext HTTP context.
	 */
	public static void authenticateAPIM(HttpContext httpContext, String apimEndpoint, String apimUsername,
	                                    String apimPassword) throws RegistryException {
		String loginEP = apimEndpoint + ExecutorConstants.APIM_LOGIN_URL;
		try {
			// create a post request to addAPI.
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(loginEP);
			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<NameValuePair>(3);

			params.add(new BasicNameValuePair(ExecutorConstants.API_ACTION, ExecutorConstants.API_LOGIN_ACTION));
			params.add(new BasicNameValuePair(ExecutorConstants.API_USERNAME, apimUsername));
			params.add(new BasicNameValuePair(ExecutorConstants.API_PASSWORD, apimPassword));
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

			HttpResponse response = httpclient.execute(httppost, httpContext);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException(" Authentication with API Manager failed: HTTP error code : " +
				                           response.getStatusLine().getStatusCode());
			}

		} catch (ClientProtocolException e) {
			throw new RegistryException("", e);
		} catch (UnsupportedEncodingException e) {
			throw new RegistryException("", e);
		} catch (IOException e) {
			throw new RegistryException("", e);
		}
	}


}
