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

package org.eclipse.ecr.web.framework.forms.ftl;

import java.io.IOException;
import java.util.Map;

import org.eclipse.ecr.web.framework.FreemarkerRenderingEngine;
import org.eclipse.ecr.web.framework.forms.FormData;
import org.eclipse.ecr.web.framework.forms.FormDescriptor;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class FormDirective implements TemplateDirectiveModel {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void execute(Environment env, final Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {

        String id = null;
        SimpleScalar scalar = (SimpleScalar) params.get("id");
        if (scalar != null) {
            id = scalar.getAsString();
        } else {
            throw new TemplateException("@form id attribute is not defined", env);
        }

        Map<String, Object> model = (Map<String, Object>)FreemarkerRenderingEngine.getRootDataModel(env);
        if (model == null) {
        	throw new TemplateException("No WebContext available in current Freemarker rendering context", env);
        }
        
        FormData form = (FormData)model.get(id);
        if (form == null) {
        	throw new TemplateException("No form found in WebContext having the ID: "+id, env);
        }
		
		FormWriter writer = new FormWriter(model, env.getOut(), form);
		FormDescriptor fd = form.getDescriptor();
		try {
			writeFormStart(writer, fd, params);
			if (body != null) {
				body.render(writer);
			}
			writeFormEnd(writer, fd);
		} finally {
			writer.flush();
		}
	}

	protected void writeFormStart(FormWriter writer, FormDescriptor fd, Map<String, Object> params) throws IOException {
		writer.start("form").attrs(params).etag();		
		writer.start("input").attr("type", "hidden").attr("name", "__form_id__").attr("value", fd.id).etag();
	}
	
	protected void writeFormEnd(FormWriter writer, FormDescriptor fd) throws IOException {
		writer.end("form");
		writer.start("script").attr("language", "javascript").etag();
		writer.print("var form_").print(fd.id).print(" = new Form(\"#").print(fd.id).print("\").install(");
		fd.writeRulesAsJSon(writer);
		writer.print(");\n");
		writer.println("$(function () {$(\".popover-help\").popover({offset: 10})});");
		writer.end("script");
	}

}
