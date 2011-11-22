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
import java.util.List;

import freemarker.template.TemplateException;

public class FieldData {

	public String name;

	public String label;

	public String help;
	
	public List<String> values;

	public String error;

	public String preferredErrorClass;
	
	public void writeInputStart(FormWriter writer) throws IOException {
		// do nothing
	}

	public void writeInputEnd(FormWriter writer) throws IOException {
		// do nothing
	}
	
	public String getValue() {
		return values != null && !values.isEmpty() ? values.get(0) : null;
	}

	public List<String> getValues() {
		return values != null && !values.isEmpty() ? values : null;
	}

	public String getPrederredErrorClass() {
		return preferredErrorClass == null ? "help-block" : preferredErrorClass;
	}

	public void writeStartField(FormWriter writer) throws TemplateException,
			IOException {
		writer.start("div");
		if (error != null) {
			writer.attr("class", "clearfix error").etag();			
		} else {
			writer.attr("class", "clearfix").etag();
		}
		writer.start("label").attr("for", name).etag().print(label);
		if (help != null) {
			printHelp(writer);
		}
		writer.end("label");
		writer.start("div").attr("class", "input").etag();		
	}

	public void writeEndField(FormWriter writer) throws TemplateException,
			IOException {
		if (error != null) {
			writer.start("span").attr("class", getPrederredErrorClass()).etag().print(error).end("span");
		}
		writer.end("div").end("div");
	}
	
	protected void printHelp(FormWriter writer) throws IOException {
		writer.print("<span")
		  .attr("class", "popover-help")
		  .attr("title", label).attr("data-content", help);
		writer.etag().print("[?]").print("</span>");
	}

}
