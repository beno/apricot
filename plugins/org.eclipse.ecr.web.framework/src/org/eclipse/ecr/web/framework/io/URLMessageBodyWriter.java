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
 */
package org.eclipse.ecr.web.framework.io;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.FileUtils;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
@Provider
public class URLMessageBodyWriter implements MessageBodyWriter<URL> {

    private static final Log log = LogFactory.getLog(URLMessageBodyWriter.class);

    // @ResourceContext private HttpServletRequest request;

    @Override
    public void writeTo(URL t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
        	java.io.InputStream in = t.openStream();
        	try {
        		FileUtils.copy(in, entityStream);
        	} finally {
        		try {
        			entityStream.flush();
        		} finally {
        			in.close();
        		}
        	}
        } catch (Throwable e) {
            log.error("Failed to get resource: " + t, e);
            throw new IOException("Failed to get resource", e);
        }
    }

    @Override
    public long getSize(URL arg0, Class<?> arg1, Type arg2, Annotation[] arg3,
            MediaType arg4) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> arg0, Type type, Annotation[] arg2,
            MediaType arg3) {
        return URL.class.isAssignableFrom(arg0);
    }

}
