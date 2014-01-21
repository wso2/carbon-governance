/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.api.test;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.IOUtils;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaFilter;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.test.utils.BaseTestCase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SchemaTest extends BaseTestCase {
    public void testAddSchema() throws Exception {
        SchemaManager schemaManager = new SchemaManager(registry);

        Schema schema = schemaManager.newSchema("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/xsd/purchasing.xsd");
        schema.addAttribute("creator", "it is me");
        schema.addAttribute("version", "0.01");
        schemaManager.addSchema(schema);

        Schema newSchema = schemaManager.getSchema(schema.getId());
        assertEquals(schema.getSchemaElement().toString(), newSchema.getSchemaElement().toString());
        assertEquals("it is me", newSchema.getAttribute("creator"));
        assertEquals("0.01", newSchema.getAttribute("version"));

        // change the target namespace and check
        String oldSchemaPath = newSchema.getPath();
        assertEquals(oldSchemaPath, "/schemas/org/bar/purchasing/purchasing.xsd");
        assertTrue(registry.resourceExists("/schemas/org/bar/purchasing/purchasing.xsd"));

        OMElement schemaElement = newSchema.getSchemaElement();
        schemaElement.addAttribute("targetNamespace", "http://ww2.wso2.org/schema-test", null);
        schemaElement.declareNamespace("http://ww2.wso2.org/schema-test", "tns");
        schemaManager.updateSchema(newSchema);

        assertEquals("/schemas/org/wso2/ww2/schema_test/purchasing.xsd", newSchema.getPath());
        assertFalse(registry.resourceExists("/test_schemas/org/bar/purchasing.xsd"));

        // doing an update without changing anything.
        schemaManager.updateSchema(newSchema);

        assertEquals("/schemas/org/wso2/ww2/schema_test/purchasing.xsd", newSchema.getPath());
        assertEquals("0.01", newSchema.getAttribute("version"));

        newSchema = schemaManager.getSchema(schema.getId());
        assertEquals("it is me", newSchema.getAttribute("creator"));
        assertEquals("0.01", newSchema.getAttribute("version"));

        Schema[] schemas = schemaManager.findSchemas(new SchemaFilter() {
            public boolean matches(Schema schema) throws GovernanceException {
                if (schema.getAttribute("version").equals("0.01")) {
                    return true;
                }
                return false;
            }
        });
        assertEquals(1, schemas.length);
        assertEquals(newSchema.getId(), schemas[0].getId());

        // deleting the schema
        schemaManager.removeSchema(newSchema.getId());
        Schema deletedSchema = schemaManager.getSchema(newSchema.getId());
        assertNull(deletedSchema);
    }

    public void testAddSchemaFromContent() throws Exception {
        SchemaManager schemaManager = new SchemaManager(registry);
        byte[] bytes = null;
        try {
            InputStream inputStream = new URL("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/xsd/purchasing.xsd").openStream();
            try {
                bytes = IOUtils.toByteArray(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            fail("Unable to read WSDL content");
        }
        Schema schema = schemaManager.newSchema(bytes, "newPurchasing.xsd");
        schema.addAttribute("creator", "it is me");
        schema.addAttribute("version", "0.01");
        schemaManager.addSchema(schema);

        Schema newSchema = schemaManager.getSchema(schema.getId());
        assertEquals(schema.getSchemaElement().toString(), newSchema.getSchemaElement().toString());
        assertEquals("it is me", newSchema.getAttribute("creator"));
        assertEquals("0.01", newSchema.getAttribute("version"));

        // change the target namespace and check
        String oldSchemaPath = newSchema.getPath();
        assertEquals(oldSchemaPath, "/schemas/org/bar/purchasing/newPurchasing.xsd");
        assertTrue(registry.resourceExists("/schemas/org/bar/purchasing/newPurchasing.xsd"));
    }

    public void testAddSchemaFromContentNoName() throws Exception {
        SchemaManager schemaManager = new SchemaManager(registry);
        byte[] bytes = null;
        try {
            InputStream inputStream = new URL("http://svn.wso2.org/repos/wso2/carbon/platform/trunk/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/xsd/purchasing.xsd").openStream();
            try {
                bytes = IOUtils.toByteArray(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            fail("Unable to read WSDL content");
        }
        Schema schema = schemaManager.newSchema(bytes);
        schema.addAttribute("creator", "it is me");
        schema.addAttribute("version", "0.01");
        schemaManager.addSchema(schema);

        Schema newSchema = schemaManager.getSchema(schema.getId());
        assertEquals(schema.getSchemaElement().toString(), newSchema.getSchemaElement().toString());
        assertEquals("it is me", newSchema.getAttribute("creator"));
        assertEquals("0.01", newSchema.getAttribute("version"));

        // change the target namespace and check
        String oldSchemaPath = newSchema.getPath();
        assertEquals(oldSchemaPath, "/schemas/org/bar/purchasing/" + schema.getId() + ".xsd");
        assertTrue(registry.resourceExists("/schemas/org/bar/purchasing/" + schema.getId() + ".xsd"));
    }
}
