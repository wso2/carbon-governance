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
package org.wso2.carbon.governance.gadgets.impactanalysis.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class WSDLBean {
	private String id;
	private String path;
	private String qName;
	
	private PolicyBean[] attachedPolicies;
	private SchemaBean[] attachedSchemas;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getqName() {
		return qName;
	}
	public void setqName(String qName) {
		this.qName = qName;
	}
	public PolicyBean[] getAttachedPolicies() {
		return attachedPolicies;
	}
	public void setAttachedPolicies(PolicyBean[] attachedPolicies) {
		this.attachedPolicies = attachedPolicies;
	}
	public SchemaBean[] getAttachedSchemas() {
		return attachedSchemas;
	}
	public void setAttachedSchemas(SchemaBean[] attachedSchemas) {
		this.attachedSchemas = attachedSchemas;
	}
}
