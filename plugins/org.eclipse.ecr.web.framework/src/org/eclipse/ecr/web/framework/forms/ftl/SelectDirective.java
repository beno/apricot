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
import java.util.Map;

import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;

public class SelectDirective extends InputDirective {

	@Override
	public void writeInput(FormWriter writer, Map<String, Object> params, TemplateDirectiveBody body) throws TemplateException, IOException {
		writer.start("select").attr("name", writer.field.name).attrs(params).etag();
		if (body != null) {
			body.render(writer);
		} //TODO make a select model to be able to easily select options
		writer.end("select");
	}
}
