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

public class OptionDirective implements TemplateDirectiveModel {

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void execute(Environment env, final Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {

		FormWriter writer;
		try {
			writer = (FormWriter) env.getOut();
		} catch (Exception e) {
			throw new TemplateException(
					"@option directives must be used inside a @select directive",
					env);
		}

		if (writer.field == null) {
			throw new TemplateException(
					"@option directives must be used inside a @select directive",
					env);			
		}
		
		Object v = params.get("value");
		if (v == null) {
			throw new TemplateException(
					"@option directives must have a value attribute",
					env);
		}
		String value = v.toString();
		
		String label;
		if (body != null) {
			StringWriter sw = new StringWriter();
			body.render(sw);
			label = sw.toString();
		} else {
			label = value;
		}

		writer.start("option").attr("value", value);
		List<String> values = writer.field.getValues();
		if (values != null && values.contains(value)) {
			writer.attr("selected", "true");
		}
		writer.etag().print(label).end("option");
		
	}
}
