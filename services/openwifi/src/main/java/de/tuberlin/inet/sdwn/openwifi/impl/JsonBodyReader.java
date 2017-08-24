package de.tuberlin.inet.sdwn.openwifi.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class JsonBodyReader implements MessageBodyReader<JsonNode> {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == JsonNode.class;
    }

    @Override
    public JsonNode readFrom(Class<JsonNode> type, Type genericType, Annotation[] annotations,
                             MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                             InputStream entityStream) throws IOException, WebApplicationException {

        return mapper.readTree(entityStream);
    }
}
