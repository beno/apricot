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
package org.eclipse.ecr.web.framework;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bstefanescu
 *
 */
public class TemplateView implements View {

	protected String viewId;
	
	protected AbstractResource target;
	
	protected Map<String, Object> args;
	
	public TemplateView(AbstractResource target, String viewPath) {
		this (target, viewPath, new HashMap<String, Object>());
	}
	
	public TemplateView(AbstractResource target, String viewPath, Map<String, Object> args) {
		this.target = target;
		this.args = args == null ? new HashMap<String, Object>() : args;
		this.viewId = "skin:".concat(viewPath);
	}

	@Override 
	public TemplateView set(String key, Object value) {
		args.put(key, value);
		return this;
	}
	
	public String getViewId() {
		return viewId;
	}
	
	@Override
	public View arg(String key, Object value) {
		args.put(key, value);
		return this;
	}

	@Override
	public void render(Writer out) throws Exception {
		target.initBindings(args);
        target.getApplication().getRenderingEngine().render(
                viewId, args, out);
        out.flush();
	}

	@Override
	public void render(OutputStream out) throws Exception {
		OutputStreamWriter writer = new OutputStreamWriter(out);
		render(writer);
		writer.flush();
	}

}
