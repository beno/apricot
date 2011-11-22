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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.eclipse.ecr.web.framework.forms.FormData;

public class FormWriter extends FilterWriter {

	protected Map<String, Object> model;
	
	protected FormData form;
	
	protected FieldData field;
	
	public FormWriter(Map<String, Object> model, Writer writer, FormData form) {
		super (writer);
		this.model = model;
		this.form = form;
	}
	
	public Map<String, Object> getModel() {
		return model;
	}
	
	public FormData getForm() {
		return form;
	}
	
	public FieldData getField() {
		return field;
	}
		
	public final FormWriter print(String s) throws IOException {
		write(s);
		return this;
	}

	public final FormWriter println(String s) throws IOException {
		write(s);
		write("\n");
		return this;		
	}

	public final FormWriter println() throws IOException {
		write("\n");
		return this;		
	}

	public final FormWriter attr(String key, String value) throws IOException {
		write(" ");
		write(key);
		write("=\"");
		write(value);
		write("\"");
		return this;		
	}

	public final FormWriter end(String tag) throws IOException {
		write("</"); write(tag); write(">\n");
		return this;
	}

	public final FormWriter end() throws IOException {
		write("/>\n");
		return this;
	}

	public final FormWriter etag() throws IOException {
		write(">\n");
		return this;
	}

	public final FormWriter start(String tag) throws IOException {
		write("<"); write(tag);
		return this;
	}

	public final FormWriter attrs(Map<String, Object> map) throws IOException {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			attr(entry.getKey(), entry.getValue().toString());
		}
		return this;
	}

	public void writeFormStart(FormData form) throws IOException {
		this.form = form;
	}
	
	public void writeFormEnd() throws IOException {
		this.form = null;
	}	
	
}
