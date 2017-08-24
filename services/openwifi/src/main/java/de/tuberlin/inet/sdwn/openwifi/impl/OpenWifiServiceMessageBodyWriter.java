package de.tuberlin.inet.sdwn.openwifi.impl;

import org.slf4j.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;


@Provider
@Produces(MediaType.APPLICATION_JSON)
public class OpenWifiServiceMessageBodyWriter implements MessageBodyWriter<OpenWifiServiceEntity> {

    private Logger log;

    public OpenWifiServiceMessageBodyWriter(Logger log) {
        this.log = log;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == OpenWifiServiceEntity.class;
    }

    @Override
    public long getSize(OpenWifiServiceEntity openWifiServiceEntity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // deprecated
        return -1;
    }

    @Override
    public void writeTo(OpenWifiServiceEntity openWifiServiceEntity, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {

        try {

            JAXBContext ctx = JAXBContext.newInstance(OpenWifiServiceEntity.class);
            Marshaller m = ctx.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            m.marshal(openWifiServiceEntity, baos);
            m.marshal(openWifiServiceEntity, entityStream);
            log.info(baos.toString());

        } catch (JAXBException e) {
            throw new ProcessingException("Error serializing OpenWifiService: " + e.getMessage());
        }
    }
}
