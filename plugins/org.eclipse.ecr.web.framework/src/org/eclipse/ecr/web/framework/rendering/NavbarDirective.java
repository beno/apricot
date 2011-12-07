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
import java.io.Writer;
import java.util.Map;

import org.eclipse.ecr.web.framework.HTMLWriter;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class NavbarDirective implements TemplateDirectiveModel {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute(Environment env, final Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
				
		String active = "";
		SimpleScalar scalar = (SimpleScalar) params.remove("active");
        if (scalar != null) {
            active = scalar.getAsString();
        }

        scalar = (SimpleScalar) params.remove("default");
        if (scalar != null && active.length() == 0) {
            active = scalar.getAsString();
        }

        String activeClass = "active";
        scalar = (SimpleScalar) params.remove("active-class");
        if (scalar != null) {
            activeClass = scalar.getAsString();
        }

        if (body != null) {        	
        	NavbarWriter writer = new NavbarWriter(active, activeClass, env.getOut());
        	writer.start("ul").attrs(params).etag();
        	try {
        		body.render(writer);
        	} finally {
        		writer.end("ul");
        		writer.flush();
        	}
        }
	}
	
	static class NavbarWriter extends HTMLWriter {
		
		protected String active;
		
		protected String activeClass;
		
		public NavbarWriter(String active, String activeClass, Writer writer) {
			super (writer);
			this.active = active;
			this.activeClass = activeClass;
		}
		
	}
}
