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

package org.eclipse.ecr.web.framework.rendering;

import java.io.IOException;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class NavbarItemDirective implements TemplateDirectiveModel {

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void execute(Environment env, final Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
		
		NavbarDirective.NavbarWriter writer;
		try {
			writer = (NavbarDirective.NavbarWriter)env.getOut();
		} catch (ClassCastException e) {
			throw new TemplateException("navbar items must be used inside @navbar directive", env);
		}

		String name = null;
        SimpleScalar scalar = (SimpleScalar) params.get("id");
        if (scalar != null) {
            name = scalar.getAsString();
        }
		
		writer.start("li");
		if (writer.active.equals(name)) {
			writer.attr("class", writer.activeClass);
		}
		writer.etag();
        if (body != null) {        	
        	body.render(writer);
        }		
		writer.end("li");
	}
	
}
