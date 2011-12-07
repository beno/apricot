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
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class InputDirective implements TemplateDirectiveModel {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute(Environment env, final Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
		
		FormWriter writer;
		try {
			writer = (FormWriter) env.getOut();
		} catch (Exception e) {
			throw new TemplateException(
					"@input directives must be used inside a @form directive",
					env);
		}

		if (writer.field == null) {
			throw new TemplateException(
					"@input directives must be used inside a @field directive",
					env);			
		}
		
		writer.field.writeInputStart(writer);
		if (params.isEmpty()) {
			writeCustomInput(writer, params, body);			
		} else {
			writeInput(writer, params, body);
		}
		writer.field.writeInputEnd(writer);
	}
	
	public void writeInput(FormWriter writer, Map<String, Object> params, TemplateDirectiveBody body) throws TemplateException, IOException {
		writer.start("input").attr("name", writer.field.name).attrs(params);
		List<String> values = writer.field.getValues();
		if (values != null) {
			Object o = params.get("type");
			if (o != null) {
				String type = o.toString().toLowerCase();
				if ("checkbox".equals(type) || "radio".equals(type)) {
					o = params.get("value");
					if (o != null) {
						if (values.contains(o.toString())) {
							writer.print(" checked");
						}
					}
				} else {
					writer.attr("value", values.get(0));
				}
			}
		}
		writer.etag();
		// handle body if any
		if (body != null) {
			StringWriter sw = new StringWriter();
			body.render(sw);
			writer.print("<span>").print(sw.toString()).print("</span>");
		}
	}

	protected void writeCustomInput(FormWriter writer, Map<String, Object> params, TemplateDirectiveBody body) throws TemplateException, IOException {
		if (body != null) {
			StringWriter sw = new StringWriter();
			body.render(sw);
			//TODO how to handle values?
			writer.print(sw.toString());
		}
	}
}
