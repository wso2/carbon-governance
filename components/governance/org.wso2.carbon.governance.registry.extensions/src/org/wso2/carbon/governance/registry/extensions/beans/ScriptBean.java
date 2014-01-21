package org.wso2.carbon.governance.registry.extensions.beans;

public class ScriptBean {
    private boolean isConsole;
    private String functionName;
    private String script;
    private String eventName;

    public ScriptBean(boolean console, String functionName, String eventName, String script) {
        isConsole = console;
        this.functionName = functionName;
        this.script = script;
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public boolean isConsole() {
        return isConsole;
    }

    public void setConsole(boolean console) {
        isConsole = console;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
