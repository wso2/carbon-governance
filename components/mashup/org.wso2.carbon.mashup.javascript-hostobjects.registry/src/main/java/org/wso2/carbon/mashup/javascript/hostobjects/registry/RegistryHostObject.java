/**
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.mashup.javascript.hostobjects.registry;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * <p/>
 * This is a JavaScript Rhino host object aimed to provide a set of registry
 * specific utility functions to the javascript service developers.
 * </p>
 */
public class RegistryHostObject extends ScriptableObject {

    private Registry registry;
    private static String basePath = "";

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws CarbonException {
        RegistryHostObject registryHostObject = new RegistryHostObject();
        if (args.length == 0) {
            AxisConfiguration axisConfig = registryHostObject.getConfigContext(cx).getAxisConfiguration();
            Object object = cx.getThreadLocal(MashupConstants.AXIS2_SERVICE);
            AxisService axisService;
            if (object instanceof AxisService) {
                axisService = (AxisService) object;
            } else {
                throw new CarbonException("Error obtaining the AxisService.");
            }
            String mashupAuthor = (String) axisService.getParameter(
                    MashupConstants.MASHUP_AUTHOR).getValue();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            registryHostObject.registry = RegistryHostObjectContext.getUserRegistry(mashupAuthor, tenantId);
            return registryHostObject;
        } else {
            throw new CarbonException("Registry() constructor doesn't accept arguments.");
        }
    }

    /**
     * Type to be used for this object inside the javascript.
     */
    public String getClassName() {
        return "Registry";
    }

    public static void jsFunction_remove(Context cx, Scriptable thisObj, Object[] arguments,
                                         Function funObj) throws CarbonException {
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        if (arguments.length == 1) {
            if (arguments[0] instanceof String) {
                try {
                    registryHostObject.registry.delete(getAbsoluteRegistryPath((String) arguments[0]));
                } catch (RegistryException e) {
                    throw new CarbonException("Registry error occurred while executing delete()" +
                                              " operation.", e);
                }
            } else {
                throw new CarbonException("Path argument of method delete() should be a string.");
            }
        } else {
            throw new CarbonException("Invalid no. of arguments for delete() method");
        }
    }

    public static Scriptable jsFunction_get(Context cx, Scriptable thisObj, Object[] arguments,
                                            Function funObj) throws CarbonException {
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        if (arguments.length == 1) {
            if (arguments[0] instanceof String) {
                try {
                    Scriptable hostObject;
                    Resource resource = registryHostObject.registry.get(getAbsoluteRegistryPath((String) arguments[0]));
                    if(resource instanceof Collection) {
                        hostObject = cx.newObject(
                            registryHostObject, "Collection", new Object[]{resource});
                    } else {
                        hostObject = cx.newObject(
                            registryHostObject, "Resource", new Object[]{resource});
                    }
                    return hostObject;
                } catch (RegistryException e) {
                    throw new CarbonException("Registry error occurred while executing get() " +
                                              "operation.", e);
                }
            } else {
                throw new CarbonException("Path argument of method get() should be a string");
            }
        } else if (arguments.length == 3) {
            if (arguments[0] instanceof String && arguments[1] instanceof Number &&
                arguments[2] instanceof Number) {
                try {
                    Collection collection = registryHostObject.registry.get(
                            getAbsoluteRegistryPath((String) arguments[0]), ((Number) arguments[1]).intValue(),
                            ((Number) arguments[2]).intValue());
                    CollectionHostObject collectionHostobject = (CollectionHostObject) cx.newObject(
                            registryHostObject, "Collection", new Object[]{collection});
                    return collectionHostobject;
                } catch (RegistryException e) {
                    throw new CarbonException("Registry error occurred while executing get() " +
                                              "operation.", e);
                }

            } else {
                throw new CarbonException("Invalid argument types for get() method.");
            }
        } else {
            throw new CarbonException("Invalid no. of arguments for get() method");
        }
    }

    public static String jsFunction_put(Context cx, Scriptable thisObj, Object[] arguments,
                                        Function funObj) throws CarbonException {
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        if (arguments.length == 2) {
            if (arguments[0] instanceof String && arguments[1] instanceof Scriptable) {
                ResourceHostObject resourceHostobject = (ResourceHostObject) arguments[1];
                try {
                    return registryHostObject.registry.put(getAbsoluteRegistryPath((String) arguments[0]),
                                                           resourceHostobject.getResource());
                } catch (RegistryException e) {
                    throw new CarbonException("Registry error occurred while executing get() " +
                                              "operation.", e);
                }
            } else {
                throw new CarbonException("Invalid argument types for put() method.");
            }
        } else {
            throw new CarbonException("Invalid no. of arguments for put() method");
        }
    }

    public static Scriptable jsFunction_newCollection(Context cx, Scriptable thisObj,
                                                      Object[] arguments,
                                                      Function funObj) throws CarbonException {
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        if (arguments.length == 0) {
            if (registryHostObject.registry != null) {
                try {
                    Collection collection = registryHostObject.registry.newCollection();
                    CollectionHostObject collectionHostobject = (CollectionHostObject) cx.newObject(
                            registryHostObject, "Collection", new Object[]{collection});
                    return collectionHostobject;
                } catch (RegistryException e) {
                    throw new CarbonException("Error occurred while creating a new Collection.", e);
                }
            } else {
                throw new CarbonException("Registry has not initialized.");
            }
        } else {
            throw new CarbonException("newCollection() Method doesn't accept arguments.");
        }
    }

    public static Scriptable jsFunction_newResource(Context cx, Scriptable thisObj,
                                                    Object[] arguments,
                                                    Function funObj) throws CarbonException {
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        if (arguments.length == 0) {
            if (registryHostObject.registry != null) {
                try {
                    Resource resource = registryHostObject.registry.newResource();
                    ResourceHostObject resourceHostobject = (ResourceHostObject) cx.newObject(
                            registryHostObject, "Resource", new Object[]{resource});
                    return resourceHostobject;
                } catch (RegistryException e) {
                    throw new CarbonException("Error occurred while creating a new Resource.", e);
                }
            } else {
                throw new CarbonException("Registry has not initialized.");
            }
        } else {
            throw new CarbonException("newResource() Method doesn't accept arguments.");
        }
    }

    public static boolean jsFunction_resourceExists(Context cx, Scriptable thisObj,
                                                    Object[] arguments,
                                                    Function funObj) throws CarbonException {
        RegistryHostObject registryHostObject = (RegistryHostObject) thisObj;
        if (arguments.length == 1) {
            if (arguments[0] instanceof String) {
                try {
                    return registryHostObject.registry.resourceExists(getAbsoluteRegistryPath((String) arguments[0]));
                } catch (RegistryException e) {
                    throw new CarbonException("Error occurred while creating a new Resource.", e);
                }
            } else {
                throw new CarbonException("Invalid argument types for resourceExists() method.");
            }
        } else {
            throw new CarbonException("Invalid no. of arguments");
        }
    }

    private ConfigurationContext getConfigContext(Context cx) throws CarbonException {
        Object configurationContextObject = cx
                .getThreadLocal(MashupConstants.AXIS2_CONFIGURATION_CONTEXT);
        if (configurationContextObject != null
            && configurationContextObject instanceof ConfigurationContext) {
            return (ConfigurationContext) configurationContextObject;
        } else {
            throw new CarbonException(
                    "Error obtaining the Service Meta Data : Axis2 ConfigurationContext");
        }
    }

    private static String getAbsoluteRegistryPath(String relativePath) {
        return basePath + RegistryConstants.PATH_SEPARATOR + relativePath;
    }
}
