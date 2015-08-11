/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.registry.extensions.utils;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.governance.registry.extensions.executors.utils.ExecutorConstants.*;

/**
 * Created by a548347 on 05/07/2014.
 * This class will be util class for APIM related stuff.
 */
public final class APIUtils {

    /**
     * This method will authenticate the user name and password mentioned in life cycle against APIM.
     *
     * @param httpContext
     * @param apimEndpoint
     * @param apimUsername
     * @param apimPassword
     */
    public static void authenticateAPIM(HttpContext httpContext, String apimEndpoint, String apimUsername, String apimPassword)  throws GovernanceException {

        String loginEP = apimEndpoint + Constants.APIM_LOGIN_ENDPOINT;
        try {
            // create a post request to addAPI.
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(loginEP);
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(Constants.APIM_PARAMETER_COUNT);

            params.add(new BasicNameValuePair(API_ACTION, API_LOGIN_ACTION));
            params.add(new BasicNameValuePair(API_USERNAME, apimUsername));
            params.add(new BasicNameValuePair(API_PASSWORD, apimPassword));
            httppost.setEntity(new UrlEncodedFormEntity(params, Constants.UTF_8_ENCODE));

            HttpResponse response = httpclient.execute(httppost, httpContext);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, Constants.UTF_8_ENCODE);
            if (response.getStatusLine().getStatusCode() != Constants.SUCCESS_RESPONSE_CODE) {
                throw new RegistryException(" Authentication with APIM failed: HTTP error code : " +
                        response.getStatusLine().getStatusCode());
            }

            Gson gson = new Gson();
            ResponseAPIM responseAPIM = gson.fromJson(responseString, ResponseAPIM.class);
            if (responseAPIM.getError().equalsIgnoreCase("true")) {
                throw new GovernanceException("Error occurred in validating the user. Please check the credentials");
            }

        } catch (Exception e) {
            throw new GovernanceException("Authentication failed with API Manager. Please check the credentials", e);
        }
    }

    /**
     * This method will authenticate the user name and password mentioned in life cycle against APIM 2.0.0.
     *
     * @param httpContext
     * @param apimEndpoint
     * @param apimUsername
     * @param apimPassword
     */
    public static String authenticateAPIM_2(HttpContext httpContext, String apimEndpoint, String apimUsername, String apimPassword)  throws GovernanceException {

        String loginEP = apimEndpoint + Constants.APIM_2_0_0_LOGIN_ENDPOINT;
        try {
            // create a post request to addAPI.
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(loginEP);
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair(API_USERNAME, apimUsername));
            params.add(new BasicNameValuePair(API_PASSWORD, apimPassword));
            httppost.setEntity(new UrlEncodedFormEntity(params, Constants.UTF_8_ENCODE));

            HttpResponse response = httpclient.execute(httppost, httpContext);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, Constants.UTF_8_ENCODE);
            if (response.getStatusLine().getStatusCode() != Constants.SUCCESS_RESPONSE_CODE) {
                throw new RegistryException(" Authentication with APIM failed: HTTP error code : " +
                        response.getStatusLine().getStatusCode());
            }

            JSONObject obj = new JSONObject(responseString);
            return obj.getJSONObject("data").getString("sessionId");

        } catch (Exception e) {
            throw new GovernanceException("Authentication failed with API Manager. Please check the credentials", e);
        }
    }

    /**
     * This method will publish api to APIM.
     * @param httpclient
     * @param httppost
     * @param params
     * @param httpContext
     * @return
     */
    public static ResponseAPIM callAPIMToPublishAPI(HttpClient httpclient, HttpPost httppost, List<NameValuePair> params,
                                             HttpContext httpContext)  throws GovernanceException {
        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, Constants.UTF_8_ENCODE));
            HttpResponse response = httpclient.execute(httppost, httpContext);
            if (response.getStatusLine().getStatusCode() != Constants.SUCCESS_RESPONSE_CODE) { // 200 is the successful response status code
                throw new RegistryException("Failed : HTTP error code : " +
                        response.getStatusLine().getStatusCode());
            }
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, Constants.UTF_8_ENCODE);
            Gson gson = new Gson();
            return gson.fromJson(responseString, ResponseAPIM.class);

        } catch (java.net.SocketTimeoutException e) {
            throw new GovernanceException("Connection timed out, Please check the network availability", e);
        } catch (UnsupportedEncodingException e) {
            throw new GovernanceException("Unsupported encode exception.", e);
        } catch (IOException e) {
            throw new GovernanceException("IO Exception occurred.", e);
        } catch (Exception e) {
            throw new GovernanceException(e.getMessage(), e);
        }
    }

    /**
     * This method will publish api to APIM 2.0.0.
     * @param httpclient
     * @param httppost
     * @param params
     * @param httpContext
     * @return
     */
    public static ResponseAPIM callAPIMToPublishAPI2(HttpClient httpclient, HttpPost httppost, List<NameValuePair> params,
                                                    HttpContext httpContext)  throws GovernanceException {
        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, Constants.UTF_8_ENCODE));
            HttpResponse response = httpclient.execute(httppost, httpContext);
            if (response.getStatusLine().getStatusCode() != Constants.CREATED_RESPONSE_CODE) { // 201 is the successful response status code
                throw new RegistryException("Failed : HTTP error code : " +
                        response.getStatusLine().getStatusCode());
            }
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, Constants.UTF_8_ENCODE);
            return null;

        } catch (java.net.SocketTimeoutException e) {
            throw new GovernanceException("Connection timed out, Please check the network availability", e);
        } catch (UnsupportedEncodingException e) {
            throw new GovernanceException("Unsupported encode exception.", e);
        } catch (IOException e) {
            throw new GovernanceException("IO Exception occurred.", e);
        } catch (Exception e) {
            throw new GovernanceException(e.getMessage(), e);
        }
    }
}