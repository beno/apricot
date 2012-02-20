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

import java.io.Writer;
import java.util.Map;

import org.eclipse.ecr.web.framework.HTMLWriter;
import org.eclipse.ecr.web.framework.forms.Form;

public class FormWriter extends HTMLWriter {

	protected Map<String, Object> model;
	
	protected Form form;
	
	protected FieldData field;
	
	public FormWriter(Map<String, Object> model, Writer writer, Form form) {
		super (writer);
		this.model = model;
		this.form = form;
	}
	
	public Map<String, Object> getModel() {
		return model;
	}
	
	public Form getForm() {
		return form;
	}
	
	public FieldData getField() {
		return field;
	}
			
}
