package org.wso2.carbon.governance.rest.api.internal;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.rest.api.model.TypedList;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

@Provider
@Consumes({"application/json"})
public class GenericArtifactXMLMessageBodyWriter implements MessageBodyWriter<TypedList<?>> {


    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (TypedList.class.isAssignableFrom(type)) {
            return true;
        }
        return false;
    }

    @Override
    public long getSize(TypedList<?> typedList, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(TypedList<?> typedList, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        Document document = convertToDOM(typedList);
        writeTo(entityStream, document);
    }

    private void writeTo(OutputStream entityStream, Document document) {
        Source xmlSource = new DOMSource(document);
        Result outputTarget = new StreamResult(entityStream);
        try {
            TransformerFactory.newInstance().newTransformer()
                    .transform(xmlSource, outputTarget);
        } catch (TransformerException e) {
            throw new WebApplicationException(e);
        }

    }

    private Document convertToDOM(TypedList<?> typedList) {
        if (GenericArtifact.class.isAssignableFrom(typedList.getType())) {
            return converGenericArtifactToDOM((List<GenericArtifact>) typedList.getArtifacts());
        }
        return null;
    }

    private Document converGenericArtifactToDOM(List<GenericArtifact> artifacts) {
        Document document = getDocument();
        Element root = document.createElement("assets");
        document.appendChild(root);
        for (GenericArtifact artifact : artifacts) {
            root.appendChild(toDOM(document, artifact));
        }
        return document;
    }

    private Element toDOM(Document document, GenericArtifact artifact) {
        String artifactShortName = "restservcie";
        Element element = document.createElement(artifactShortName);
        element.appendChild(createChildElement(document, "name", artifact.getQName().getLocalPart()));
        try {
            for (String key : artifact.getAttributeKeys()) {
                //TODO value can be something else
                String value = artifact.getAttribute(key);
                if (key.indexOf("overview_") > -1) {
                    key = key.replace("overview_", "");
                }
                element.appendChild(createChildElement(document, key, value));
            }
        } catch (GovernanceException e) {
            e.printStackTrace();
        }
        return element;
    }

    private Element createChildElement(Document document, String elementName, String content) {
        Element name = document.createElement(elementName);
        name.setTextContent(content);
        return name;
    }

    private Document getDocument() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            return document;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }
}