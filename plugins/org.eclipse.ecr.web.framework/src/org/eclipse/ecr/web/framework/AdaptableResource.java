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
 *
 */
package org.eclipse.ecr.web.framework;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.eclipse.ecr.web.framework.adapters.AdapterFactory;

/**
 * @author bstefanescu
 *
 */
public abstract class AdaptableResource extends AbstractResource {

	public AdaptableResource(WebContext ctx) {
		super (ctx);
	}
	
	@Path("@{segment}")
	public Object getWebAdapter(@PathParam("segment") String segment) {
		AdapterFactory factory = getApplication().getAdapterManager().getAdapterFactory(segment);
		if (factory == null) {
			return Response.status(404).build();
		}
		try {
			return factory.newInstance(this);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).build();
		}
	}
	
}
