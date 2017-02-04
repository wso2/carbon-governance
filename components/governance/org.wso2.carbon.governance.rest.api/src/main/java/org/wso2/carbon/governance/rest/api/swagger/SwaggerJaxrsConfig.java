/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.rest.api.swagger;

import com.wordnik.swagger.jaxrs.config.BeanConfig;
import org.wso2.carbon.utils.NetworkUtils;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "SwaggerJaxrsConfig", loadOnStartup = 1)
public class SwaggerJaxrsConfig extends BeanConfig {

    public SwaggerJaxrsConfig(){
        super();
    }

    public void setBasePath(String basePath)
    {
        // Hostname
        String hostName = "localhost";
        try {
            hostName = NetworkUtils.getMgtHostName();
        } catch (Exception ignored) {
        }

        super.setBasePath("https://" +
                hostName + ":" + System.getProperty("mgt.transport.https.port") + "/governance");
    }
}