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

package org.eclipse.ecr.web.framework.forms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ecr.runtime.api.Framework;

public class Form {

	private final static Log log = LogFactory.getLog(Form.class);
	
	public static Form newForm(String id) {
		return new Form(id);
	}
	
	public static Form newForm(FormData data) {
		String id = data.getFirst("__form_id__");
		if (id == null) {
			log.error("Form not supported - should define a __form_id__ attribute");
			throw new WebApplicationException(500);
		}
		Form form = new Form(id, data);
		form.validate();
		return form;
	}
	
	@SuppressWarnings("unchecked")
	public static Form newForm(ServletRequest request) {
		FormData data = new FormData();
		Map<String, String[]> map = (Map<String, String[]>)request.getParameterMap();
		for (Map.Entry<String, String[]> entry : map.entrySet()) {
			String key = entry.getKey();
			String[] value = entry.getValue();
			if (value != null) {
				data.put(key, Arrays.asList(value));
			}
		}
		return Form.newForm(data);		
	}

	
	protected FormService service;
	
	protected FormData data;
	
	protected FormDescriptor fd;
	
	protected Map<String, String> errors;
	
	protected Form(String id) {
		this (id, new FormData());
	}
	
	protected Form(String id, FormData data) {
		this.data = data;
		this.service = Framework.getLocalService(FormService.class);
		this.fd = service.getFormDescriptor(id);
		this.errors = new HashMap<String, String>();
	}

	public String getId() {
		return fd.id;
	}
	
	public final FormData getData() {
		return data;
	}
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	public FormService getService() {
		return service;
	}
	
	public Map<String, String> getErrors() {
		return errors;
	}
	
	public String getError(String name) {
		return errors.get(name);
	}
	
	public String getString(String key) {
		return data.getFirst(key);
	}

	/**
	 * This is like getString but trims the string and return "" for null values
	 * @param key
	 * @return
	 */
	public String getSafeString(String key) {
		String v = data.getFirst(key);
		return v == null ? "" : v.trim();
	}

	public Boolean getBoolean(String key) {
		String v = data.getFirst(key);
		return v != null ? Boolean.valueOf(v) : null;
	}
	
	public Long getLong(String key) {
		String v = data.getFirst(key);
		return v != null ? Long.valueOf(v) : null;
	}

	public boolean getBoolean(String key, boolean defValue) {
		String v = data.getFirst(key);
		return v != null ? Boolean.parseBoolean(v) : defValue;
	}
	
	public long getLong(String key, long defValue) {
		String v = data.getFirst(key);
		return v != null ? Long.parseLong(v) : defValue;
	}

	public List<String> getStrings(String key) {
		return data.get(key);
	}

	public FormDescriptor getDescriptor() {
		return fd;
	}
	
	public Map<String,String> validate() {
		for (FieldDescriptor field : fd.fields) {
			for (RuleDescriptor rd : field.rules) {
				FieldValidator v = service.getValidator(rd.type);
				if (v != null) {
					if (!v.validate(rd, getString(field.name), this)) {
						errors.put(field.name, rd.message);
					}
				}
			}
		}
		return errors;
	}
	
}
