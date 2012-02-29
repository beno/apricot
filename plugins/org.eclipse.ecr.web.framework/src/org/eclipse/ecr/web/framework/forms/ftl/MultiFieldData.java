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

import freemarker.template.TemplateException;

public class MultiFieldData extends FieldData {

	@Override
	public void writeInputStart(FormWriter writer) throws IOException {
		writer.write("<li><label>\n");
	}

	@Override
	public void writeInputEnd(FormWriter writer) throws IOException {
		writer.write("</label></li>\n");
	}

	@Override
	public void writeStartField(FormWriter writer) throws TemplateException,
			IOException {
		writer.start("div");
		if (error != null) {
			writer.attr("class", "clearfix error").etag();
		} else {
			writer.attr("class", "clearfix").etag();
		}
		writer.start("label").etag().print(label);
		if (help != null) {
			printHelp(writer);
		}
		writer.end("label");		
		writer.println("<div class=\"input\">");
		writer.println("<ul class=\"inputs-list\">");
	}

	@Override
	public void writeEndField(FormWriter writer) throws TemplateException,
			IOException {
		if (error != null) {
			writer.start("span").attr("class", getPrederredErrorClass()).etag()
					.print(error).end("span");
		}
		writer.end("ul").end("div").end("div");
	}

}
