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
import java.util.Map;

import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;

public class TextareaDirective extends InputDirective {

	@Override
	public void writeInput(FormWriter writer, Map<String, Object> params, TemplateDirectiveBody body) throws TemplateException, IOException {
		writer.start("textarea").attr("name", writer.field.name).attrs(params).etag();
		if (writer.field.values != null) {
			writer.write(writer.field.getValue());
		} else if (body != null) {
			StringWriter sw = new StringWriter();
			body.render(sw);
			writer.write(sw.toString());
		}
		writer.end("textarea");
	}
}
