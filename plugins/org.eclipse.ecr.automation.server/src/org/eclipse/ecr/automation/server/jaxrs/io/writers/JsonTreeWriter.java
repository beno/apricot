/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     matic
 */
package org.eclipse.ecr.automation.server.jaxrs.io.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.eclipse.ecr.automation.server.jaxrs.io.JsonWriter;

/**
 * @author bstefanescu
 *
 */
@Provider
@Produces("application/json")
public class JsonTreeWriter implements MessageBodyWriter<JsonNode> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return JsonNode.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(JsonNode t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(JsonNode t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException,
            WebApplicationException {
        JsonGenerator jg = JsonWriter.getFactory().createJsonGenerator(entityStream);
        jg.writeTree(t);
        jg.flush();
    }

}
