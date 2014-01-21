package org.wso2.carbon.governance.list.operations.util;

public class OperationsConstants {

    public final static String NAMESPACE_PART1 = "http://services.";
    public final static String NAMESPACE_PART2 = ".governance.carbon.wso2.org";
    public final static String GET = "get";
    public final static String ADD = "add";
    public final static String UPDATE = "update";
    public final static String DELETE = "delete";
    public final static String DEPENDENCIES = "Dependencies";
    public final static String ARTIFACT_IDS = "ArtifactIDs";
    public final static String IN = "In";
    public final static String OUT = "Out";
    public final static String REQUEST = "Request";
    public final static String RESPONSE = "Response";
    public final static String METADATA_NAMESPACE = "http://www.wso2.org/governance/metadata";
    public final static String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    public final static String REGISTRY_EXCEPTION2_XSD = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:ax2232=\"http://api.registry.carbon.wso2.org/xsd\" attributeFormDefault=\"qualified\" elementFormDefault=\"qualified\" targetNamespace=\"http://exceptions.core.registry.carbon.wso2.org/xsd\">\n" +
                            "            <xs:import namespace=\"http://api.registry.carbon.wso2.org/xsd\" />\n" +
                            "            <xs:complexType name=\"RegistryException\">\n" +
                            "                <xs:complexContent>\n" +
                            "                    <xs:extension base=\"ax2232:RegistryException\">\n" +
                            "                        <xs:sequence />\n" +
                            "                    </xs:extension>\n" +
                            "                </xs:complexContent>\n" +
                            "            </xs:complexType>\n" +
                            "        </xs:schema>";


    public final static String GOVERNANCE_EXCEPTION_XSD = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:ax2233=\"http://exceptions.core.registry.carbon.wso2.org/xsd\" attributeFormDefault=\"qualified\" elementFormDefault=\"qualified\" targetNamespace=\"http://exception.api.governance.carbon.wso2.org/xsd\">\n" +
                            "            <xs:import namespace=\"http://exceptions.core.registry.carbon.wso2.org/xsd\" />\n" +
                            "            <xs:complexType name=\"GovernanceException\">\n" +
                            "                <xs:complexContent>\n" +
                            "                    <xs:extension base=\"ax2233:RegistryException\">\n" +
                            "                        <xs:sequence />\n" +
                            "                    </xs:extension>\n" +
                            "                </xs:complexContent>\n" +
                            "            </xs:complexType>\n" +
                            "        </xs:schema>";


    public final static String REGISTRY_EXCEPTION1_XSD = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" attributeFormDefault=\"qualified\" elementFormDefault=\"qualified\" targetNamespace=\"http://api.registry.carbon.wso2.org/xsd\">\n" +
                            "            <xs:complexType name=\"RegistryException\">\n" +
                            "                <xs:sequence />\n" +
                            "            </xs:complexType>\n" +
                            "        </xs:schema>";


    public final static String CUSTOM_XSD = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:ax2234=\"http://exception.api.governance.carbon.wso2.org/xsd\" attributeFormDefault=\"qualified\" elementFormDefault=\"qualified\">\n" +
                "            <xs:import namespace=\"http://exception.api.governance.carbon.wso2.org/xsd\" />\n" +
                "            <xs:element>\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element minOccurs=\"0\" name=\"GovernanceException\" nillable=\"true\" type=\"ax2234:GovernanceException\" />\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "            <xs:element>\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element minOccurs=\"0\" nillable=\"true\"/>\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "            <xs:element>\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element minOccurs=\"0\" name=\"return\"/>\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "        </xs:schema>";
}
