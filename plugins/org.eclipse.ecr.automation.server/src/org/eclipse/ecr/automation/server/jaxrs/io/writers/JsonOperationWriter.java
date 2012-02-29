/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
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

import org.eclipse.ecr.automation.OperationDocumentation;
import org.eclipse.ecr.automation.server.jaxrs.io.JsonWriter;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
@Provider
@Produces("application/json")
public class JsonOperationWriter implements MessageBodyWriter<OperationDocumentation> {

    public long getSize(OperationDocumentation arg0, Class<?> arg1, Type arg2,
            Annotation[] arg3, MediaType arg4) {
        return -1;
    }

    public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2,
            MediaType arg3) {
        return OperationDocumentation.class.isAssignableFrom(arg0);
    }

    public void writeTo(OperationDocumentation op, Class<?> arg1, Type arg2, Annotation[] arg3,
            MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream out)
            throws IOException, WebApplicationException {
        JsonWriter.writeOperation(out, op);
    }

}
