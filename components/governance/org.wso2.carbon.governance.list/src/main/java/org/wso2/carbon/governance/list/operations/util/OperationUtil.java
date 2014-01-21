package org.wso2.carbon.governance.list.operations.util;

import org.apache.axis2.AxisFault;

public class OperationUtil {

    public static void handleException(String msg, Exception e) throws AxisFault{
        throw new AxisFault(msg, e);
    }

    public static void handleException(String msg) throws AxisFault{
        throw new AxisFault(msg);
    }
}
