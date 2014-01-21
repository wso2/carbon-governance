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
package org.wso2.carbon.governance.gadgets.ui.beans;

import java.util.ArrayList;

public class AssociationBeanLocal {

	private String associationType;
	private ArrayList<DestinationPath> destinationPaths = new ArrayList<DestinationPath>();
	
	public String getAssociationType() {
		return associationType;
	}
	public void setAssociationType(String associationType) {
		this.associationType = associationType;
	}
	public ArrayList<DestinationPath> getDestinationPaths() {
		return destinationPaths;
	}
	public void setDestinationPaths(String path, ArrayList<AssociationBeanLocal> associations) {
		this.destinationPaths.add(new DestinationPath(path, associations));
	}

    public static class DestinationPath {
        private String path;
        private ArrayList<AssociationBeanLocal> associations;

        public DestinationPath(String path, ArrayList<AssociationBeanLocal> associations) {
            this.path = path;
            this.associations = associations;
        }

        public String getPath() {
            return path;
        }

        public ArrayList<AssociationBeanLocal> getAssociations() {
            return associations;
        }
    }
	
}
