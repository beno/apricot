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

import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.ecr.runtime.api.Framework;

/**
 * @author bstefanescu
 *
 */
public abstract class AbstractResource {

	protected String path;
	
	protected WebContext ctx;
	
	public AbstractResource(WebContext ctx) {
		initContext(ctx);
	}
	
	protected void addBreadcrumb(String name) {
		ctx.getBreadcrumb().add(getPath(), name);
	}

	protected void addBreadcrumb(String url, String name) {
		ctx.getBreadcrumb().add(url, name);
	}

	protected void initContext(WebContext ctx) {
		this.ctx = ctx;
		UriInfo uriInfo = ctx.getUriInfo();
		if (uriInfo != null) {
			initPath(uriInfo);
		}
	}
	
	protected void initPath(UriInfo uriInfo) {
		this.path = uriInfo.getMatchedURIs().get(0);
		if (this.path.endsWith("/")) {
			this.path = this.path.substring(0, this.path.length()-1);
		}
		this.path = ctx.getContextPath().concat("/").concat(this.path);
	}
	
	public WebContext getContext() {
		return ctx;
	}

	public View getView(String viewPath) {
		return new TemplateView(this, viewPath);
	}

	public View getView(String viewPath, Map<String, Object> args) {
		return new TemplateView(this, viewPath, args);
	}

	public void initBindings(Map<String, Object> args) {
		args.put("Application", ctx.getApplication());		
		args.put("This", this);		
		args.put("Context", ctx);
		args.put("Runtime", Framework.getRuntime());
		args.put("Request", ctx.getRequest());
		//args.put("contextPath", VirtualHostHelper.getContextPathProperty());
		args.put("basePath", ctx.getBasePath());
		args.put("skinPath", ctx.getSkinPath());
		args.putAll(ctx.getProperties());
	}
	
	public String getPath() {
		return path;
	}

	public WebApplication getApplication() {
		return ctx.getApplication();
	}
	
	public Response redirect(String absPath) {
		return Response.seeOther(ctx.getUri(absPath)).build();
	}
}
