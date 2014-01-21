/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.lcm.beans;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class JSBean {

    private ScriptFunctionBean[] consoleFunctions;
    private ScriptBean consoleScript;
    private ScriptFunctionBean[] serverFunctions;
    private ScriptBean serverScript;

    public ScriptFunctionBean[] getConsoleFunctions() {
        return consoleFunctions;
    }

    public void setConsoleFunctions(ScriptFunctionBean[] consoleFunctions) {
        this.consoleFunctions = consoleFunctions;
    }

    public ScriptBean getConsoleScript() {
        return consoleScript;
    }

    public void setConsoleScript(ScriptBean consoleScript) {
        this.consoleScript = consoleScript;
    }

    public ScriptFunctionBean[] getServerFunctions() {
        return serverFunctions;
    }

    public void setServerFunctions(ScriptFunctionBean[] serverFunctions) {
        this.serverFunctions = serverFunctions;
    }

    public ScriptBean getServerScript() {
        return serverScript;
    }

    public void setServerScript(ScriptBean serverScript) {
        this.serverScript = serverScript;
    }
}
