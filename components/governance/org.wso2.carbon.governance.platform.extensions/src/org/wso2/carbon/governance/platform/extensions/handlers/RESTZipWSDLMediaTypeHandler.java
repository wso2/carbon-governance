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
package org.wso2.carbon.governance.platform.extensions.handlers;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.extensions.handlers.ZipWSDLMediaTypeHandler;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;
import org.wso2.carbon.user.core.UserRealm;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class extends the default {@link ZipWSDLMediaTypeHandler}, to be able to process CXF model
 * files. It identifies RESTful services, and corresponding endpoints based on operation names.
 */
@SuppressWarnings({"unused", "UnusedAssignment"})
public class RESTZipWSDLMediaTypeHandler extends ZipWSDLMediaTypeHandler {

    //    <handler class="org.wso2.carbon.governance.platform.extensions.handlers.RESTZipWSDLMediaTypeHandler">
    //        <property name="wsdlMediaType">application/wsdl+xml</property>
    //        <property name="schemaMediaType">application/xsd+xml</property>
    //        <property name="threadPoolSize">50</property>
    //        <!--property name="disableWSDLValidation">true</property>
    //        <property name="disableSchemaValidation">true</property>
    //        <property name="wsdlExtension">.wsdl</property>
    //        <property name="schemaExtension">.xsd</property>
    //        <property name="restExtension">.cxf</property>
    //        <property name="archiveExtension">.gar</property>
    //        <property name="tempFilePrefix">wsdl</property-->
    //        <property name="schemaLocationConfiguration" type="xml">
    //            <location>/governance/schemas/</location>
    //        </property>
    //        <property name="wsdlLocationConfiguration" type="xml">
    //            <location>/governance/wsdls/</location>
    //        </property>
    //        <filter class="org.wso2.carbon.registry.core.jdbc.handlers.filters.MediaTypeMatcher">
    //            <property name="mediaType">application/vnd.wso2.governance-archive</property>
    //        </filter>
    //    </handler>

    private String wsdlExtension = ".wsdl";

    private String xsdExtension = ".xsd";

    private String restExtension = ".cxf";

    private String archiveExtension = ".gar";

    private String tempFilePrefix = "wsdl";

    private int threadPoolSize = 50;

    private static final Log log = LogFactory.getLog(RESTZipWSDLMediaTypeHandler.class);

    public void setWsdlExtension(String wsdlExtension) {
        this.wsdlExtension = wsdlExtension;
        super.setWsdlExtension(wsdlExtension);
    }

    public void setSchemaExtension(String xsdExtension) {
        this.xsdExtension = xsdExtension;
        super.setSchemaExtension(xsdExtension);
    }

    public void setArchiveExtension(String archiveExtension) {
        this.archiveExtension = archiveExtension;
        super.setArchiveExtension(archiveExtension);
    }

    public void setTempFilePrefix(String tempFilePrefix) {
        this.tempFilePrefix = tempFilePrefix;
        super.setTempFilePrefix(tempFilePrefix);
    }

    public void setThreadPoolSize(String threadPoolSize) {
        this.threadPoolSize = Integer.parseInt(threadPoolSize);
        super.setThreadPoolSize(threadPoolSize);
    }

    public void setRestExtension(String restExtension) {
        this.restExtension = restExtension;
    }

    public void put(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();
        try {
            Resource resource = requestContext.getResource();
            String path = requestContext.getResourcePath().getPath();
            try {
                if (resource != null) {
                    Object resourceContent = resource.getContent();
                    InputStream in = new ByteArrayInputStream((byte[]) resourceContent);
                    Stack<File> fileList = new Stack<File>();
                    List<String> uriList = new LinkedList<String>();
                    List<UploadTask> tasks = new LinkedList<UploadTask>();

                    int threadPoolSize = this.threadPoolSize;

                    int wsdlPathDepth = Integer.MAX_VALUE;
                    int xsdPathDepth = Integer.MAX_VALUE;
                    File tempFile = File.createTempFile(tempFilePrefix, archiveExtension);
                    File tempDir = new File(tempFile.getAbsolutePath().substring(0,
                            tempFile.getAbsolutePath().length() - archiveExtension.length()));
                    try {
                        BufferedOutputStream out = new BufferedOutputStream(
                                new FileOutputStream(tempFile));
                        try {
                            byte[] contentChunk = new byte[1024];
                            int byteCount;
                            while ((byteCount = in.read(contentChunk)) != -1) {
                                out.write(contentChunk, 0, byteCount);
                            }
                            out.flush();
                        } finally {
                            out.close();
                        }
                        ZipEntry entry;

                        makeDir(tempDir);
                        ZipInputStream zs;
                        List<String> wsdlUriList = new LinkedList<String>();
                        List<String> xsdUriList = new LinkedList<String>();
                        zs = new ZipInputStream(new FileInputStream(tempFile));
                        try {
                            entry = zs.getNextEntry();
                            while (entry != null) {
                                String entryName = entry.getName();
                                FileOutputStream os;
                                File file = new File(tempFile.getAbsolutePath().substring(0,
                                        tempFile.getAbsolutePath().length() -
                                                archiveExtension.length()) + File.separator + entryName);
                                if (entry.isDirectory()) {
                                    if (!file.exists()) {
                                        makeDirs(file);
                                        fileList.push(file);
                                    }
                                    entry = zs.getNextEntry();
                                    continue;
                                }
                                File parentFile = file.getParentFile();
                                if (!parentFile.exists()) {
                                    makeDirs(parentFile);
                                }
                                os = new FileOutputStream(file);
                                try {
                                    fileList.push(file);
                                    byte[] contentChunk = new byte[1024];
                                    int byteCount;
                                    while ((byteCount = zs.read(contentChunk)) != -1) {
                                        os.write(contentChunk, 0, byteCount);
                                    }
                                } finally {
                                    os.close();
                                }
                                zs.closeEntry();
                                entry = zs.getNextEntry();
                                if (entryName != null &&
                                        entryName.toLowerCase().endsWith(wsdlExtension)) {
                                    String uri = tempFile.toURI().toString();
                                    uri = uri.substring(0, uri.length() -
                                            archiveExtension.length()) + "/" + entryName;
                                    if (uri.startsWith("file:")) {
                                        uri = uri.substring(5);
                                    }
                                    while (uri.startsWith("/")) {
                                        uri = uri.substring(1);
                                    }
                                    uri = "file:///" + uri;
                                    if (uri.endsWith("/")) {
                                        uri = uri.substring(0, uri.length() -1);
                                    }
                                    int uriPathDepth = uri.split("/").length;
                                    if (uriPathDepth < wsdlPathDepth) {
                                        wsdlPathDepth = uriPathDepth;
                                        wsdlUriList = new LinkedList<String>();
                                    }
                                    if (wsdlPathDepth == uriPathDepth) {
                                        wsdlUriList.add(uri);
                                    }
                                } else if (entryName != null &&
                                        entryName.toLowerCase().endsWith(xsdExtension)) {
                                    String uri = tempFile.toURI().toString();
                                    uri = uri.substring(0, uri.length() -
                                            archiveExtension.length()) + "/" + entryName;
                                    if (uri.startsWith("file:")) {
                                        uri = uri.substring(5);
                                    }
                                    while (uri.startsWith("/")) {
                                        uri = uri.substring(1);
                                    }
                                    uri = "file:///" + uri;
                                    if (uri.endsWith("/")) {
                                        uri = uri.substring(0, uri.length() -1);
                                    }
                                    int uriPathDepth = uri.split("/").length;
                                    if (uriPathDepth < xsdPathDepth) {
                                        xsdPathDepth = uriPathDepth;
                                        xsdUriList = new LinkedList<String>();
                                    }
                                    if (xsdPathDepth == uriPathDepth) {
                                        xsdUriList.add(uri);
                                    }
                                } else if (entryName != null) {
                                    String uri = tempFile.toURI().toString();
                                    uri = uri.substring(0, uri.length() -
                                            archiveExtension.length()) + "/" + entryName;
                                    if (uri.startsWith("file:")) {
                                        uri = uri.substring(5);
                                    }
                                    while (uri.startsWith("/")) {
                                        uri = uri.substring(1);
                                    }
                                    uri = "file:///" + uri;
                                    if (uri.endsWith("/")) {
                                        uri = uri.substring(0, uri.length() -1);
                                    }
                                    uriList.add(uri);
                                }
                            }
                        } finally {
                            zs.close();
                        }
                        Map<String, String> localPathMap = null;
                        if (CurrentSession.getLocalPathMap() != null) {
                            localPathMap =
                                    Collections.unmodifiableMap(CurrentSession.getLocalPathMap());
                        }
                        if (wsdlUriList.isEmpty() && xsdUriList.isEmpty()) {
                            throw new RegistryException(
                                    "No WSDLs or Schemas found in the given WSDL archive");
                        }
                        if (wsdlPathDepth < Integer.MAX_VALUE) {
                            for (String uri : wsdlUriList) {
                                tasks.add(new UploadWSDLTask(requestContext, uri,
                                        CurrentSession.getTenantId(),
                                        CurrentSession.getUserRegistry(),
                                        CurrentSession.getUserRealm(),
                                        CurrentSession.getUser(),
                                        CurrentSession.getCallerTenantId(),
                                        localPathMap));
                            }
                        }
                        if (xsdPathDepth < Integer.MAX_VALUE) {
                            for (String uri : xsdUriList) {
                                tasks.add(new UploadXSDTask(requestContext, uri,
                                        CurrentSession.getTenantId(),
                                        CurrentSession.getUserRegistry(),
                                        CurrentSession.getUserRealm(),
                                        CurrentSession.getUser(),
                                        CurrentSession.getCallerTenantId(),
                                        localPathMap));
                            }
                        }

                        for (String uri : uriList) {
                            if (uri.endsWith(restExtension)) {
                                tasks.add(new UploadRESTModelTask(requestContext, uri,
                                        CurrentSession.getTenantId(),
                                        CurrentSession.getUserRegistry(),
                                        CurrentSession.getUserRealm(),
                                        CurrentSession.getUser(),
                                        CurrentSession.getCallerTenantId(),
                                        localPathMap));
                            }
                        }

                        // calculate thread pool size for efficient use of resources in concurrent
                        // update scenarios.
                        int toAdd = wsdlUriList.size() + xsdUriList.size();
                        if (toAdd < threadPoolSize) {
                            if (toAdd < (threadPoolSize / 8)) {
                                threadPoolSize = 0;
                            } else if (toAdd < (threadPoolSize / 2)) {
                                threadPoolSize = (threadPoolSize / 8);
                            } else {
                                threadPoolSize = (threadPoolSize / 4);
                            }
                        }
                    } finally {
                        in.close();
                        resourceContent = null;
                        resource.setContent(null);
                    }
                    uploadFiles(tasks, tempFile, fileList, tempDir, threadPoolSize, path, uriList,
                            requestContext);
                }
            } catch (IOException e) {
                throw new RegistryException("Error occurred while unpacking Governance Archive", e);
            }

            requestContext.setProcessingComplete(true);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    protected class UploadRESTModelTask extends UploadTask {

        public UploadRESTModelTask(RequestContext requestContext, String uri, int tenantId,
                                   UserRegistry userRegistry, UserRealm userRealm, String userId,
                                   int callerTenantId, Map<String, String> localPathMap) {
            super(requestContext, uri, tenantId, userRegistry, userRealm, userId, callerTenantId,
                    localPathMap);
        }

        @SuppressWarnings("unchecked")
        protected void doProcessing(RequestContext requestContext, String uri)
            throws RegistryException {
            UserRegistry governanceSystemRegistry =
                    RegistryCoreServiceComponent.getRegistryService().getGovernanceSystemRegistry();
            ServiceManager manager = new ServiceManager(governanceSystemRegistry);
            try {
                InputStream stream = new FileInputStream(new File(uri.substring(
                        "file:///".length() - 1)));
                try {
                    OMElement payload = new StAXOMBuilder(stream).getDocumentElement();
                    AXIOMXPath xpath = new AXIOMXPath("//ns:resource/@name");
                    String namespace = payload.getNamespace().getNamespaceURI();
                    xpath.addNamespace("ns", namespace);
                    List<OMAttribute> services = (List<OMAttribute>) xpath.evaluate(payload);
                    for (OMAttribute service : services) {
                        String serviceName = service.getAttributeValue();
                        final Service restService =
                                manager.newService(new QName(namespace, serviceName));
                        AXIOMXPath xpath2 = new AXIOMXPath("//ns:resource[@name = \"" +
                                serviceName + "\"]/ns:operation/@name");
                        xpath2.addNamespace("ns", namespace);
                        List<OMAttribute> operations = (List<OMAttribute>) xpath2.evaluate(payload);
                        for (OMAttribute operation : operations) {
                            restService.addAttribute("endpoints_entry", ":/" + serviceName + "/" +
                                    operation.getAttributeValue());
                        }
                        restService.setAttribute("overview_version", "1.0.0");
                        boolean updateLockAvailable = CommonUtil.isUpdateLockAvailable();
                        if (!updateLockAvailable) {
                            CommonUtil.releaseUpdateLock();
                        }
                        try {
                            Service[] existingServices = manager.findServices(new ServiceFilter() {
                                public boolean matches(Service service) throws GovernanceException {
                                    return service.getQName().getLocalPart().equals(
                                            restService.getQName().getLocalPart()) &&
                                            service.getQName().getNamespaceURI().equals(
                                                    restService.getQName().getNamespaceURI());
                                }
                            });
                            if (existingServices == null || existingServices.length <= 0) {
                                manager.addService(restService);
                            }
                        } finally {
                            if (!updateLockAvailable) {
                                CommonUtil.acquireUpdateLock();
                            }
                        }

                    }
                } finally {
                    stream.close();
                }
            } catch (JaxenException e) {
                log.error("An error occurred while performing XPath evaluation", e);
            } catch (XMLStreamException e) {
                log.error("Unable to parse file contents", e);
            } catch (IOException e) {
                log.error("Error occurred while reading file", e);
            }
        }
    }
}
