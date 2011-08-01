/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 *
 * $Id$
 */

package org.eclipse.ecr.web.jaxrs.views;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
@Provider
@Produces("*/*")
public class TemplateViewMessageBodyWriter implements MessageBodyWriter<TemplateView> {

    private static final Log log = LogFactory.getLog(TemplateViewMessageBodyWriter.class);

    //@ResourceContext private HttpServletRequest request;

    @Override
    public void writeTo(TemplateView t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            t.render(entityStream);
        } catch (Throwable e) {
            log.error("Failed to render view: "+t.getUrl(), e);
            throw new IOException("Failed to render view: "+t.getUrl(), e);
        }
    }

    @Override
    public long getSize(TemplateView arg0, Class<?> arg1, Type arg2,
            Annotation[] arg3, MediaType arg4) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> arg0, Type type, Annotation[] arg2,
            MediaType arg3) {
        return TemplateView.class.isAssignableFrom(arg0);
    }

}
