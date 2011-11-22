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
 */

package org.eclipse.ecr.web.framework.forms;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Provider
@Consumes("application/x-www-form-urlencoded")
public class FormMessageBodyReader implements MessageBodyReader<FormData> {

	private final static Log log = LogFactory.getLog(FormMessageBodyReader.class);
	
	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		if (type.isAssignableFrom(FormData.class)) {
			return true;
		} 
		return false;
	}

	@Override
	public FormData readFrom(Class<FormData> arg0, Type arg1, Annotation[] arg2,
			MediaType arg3, MultivaluedMap<String, String> params,
			InputStream arg5) throws IOException, WebApplicationException {
		String id = params.getFirst("__form_id__");
		if (id == null) {
			log.error("Form not supported - should define a __form_id__ attribute");
			throw new WebApplicationException(500);
		}
		FormData form = new FormData(id, params);
		form.validate();
		return form;
	}
	
}
