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
import java.util.Arrays;
import java.util.Map;

import org.eclipse.ecr.common.utils.StringUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class FieldDirective implements TemplateDirectiveModel {

	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, final Map params,
			TemplateModel[] loopVars, TemplateDirectiveBody body)
			throws TemplateException, IOException {
		FormWriter writer;
		try {
			writer = (FormWriter) env.getOut();
		} catch (Exception e) {
			throw new TemplateException(
					"@field directives must be used inside a @form directive",
					env);
		}

		if (writer.field != null) {
			throw new TemplateException(
					"@field cannot be nested in another @field",
					env);			
		}
		
		FieldData field = createFieldData(params.get("multi") != null);
		
		Object v = params.get("name");
		if (v == null) {
			throw new TemplateException("@field directive must have a name", env);
		}
		field.name = v.toString();
		
		v = params.get("label");
		field.label = v == null ? "" : v.toString();
		v = params.get("help");
		field.help = v == null ? null : v.toString();
		//TODO this cannot work since forms.js may use another class
//		v = params.get("errorClass");
//		field.preferredErrorClass = v == null ? null : v.toString();
		
		// get error if any
		field.error = writer.form.getError(field.name);
		
		// get value if any
		field.values = writer.form.getStrings(field.name);

		if (field.values == null) {
			v = params.get("value");
			if (v != null) {
				String ar[] = StringUtils.split(v.toString(), ',', true);
				field.values = Arrays.asList(ar);
			}
		}
		writer.field = field;
		try {
			field.writeStartField(writer);
			if (body != null) {
				body.render(writer);
			}
			field.writeEndField(writer);
		} finally {
			writer.field = null;
		}
	}

	protected FieldData createFieldData(boolean multi) {
		return multi ? new MultiFieldData() : new FieldData();
	}
	
}
