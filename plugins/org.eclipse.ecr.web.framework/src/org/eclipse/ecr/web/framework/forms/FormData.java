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

package org.eclipse.ecr.web.framework.forms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.ecr.runtime.api.Framework;

public class FormData {

	protected FormService service;
	
	protected MultivaluedMap<String, String> params;
	
	protected FormDescriptor fd;
	
	protected Map<String, String> errors;
	
	public FormData(String id) {
		this (id, new MultivaluedMapImpl());
	}
	
	public FormData(String id, MultivaluedMap<String, String> params) {
		this.params = params;
		this.service = Framework.getLocalService(FormService.class);
		this.fd = service.getFormDescriptor(id);
		this.errors = new HashMap<String, String>();
	}

	public String getId() {
		return fd.id;
	}
	
	public final MultivaluedMap<String, String> getParams() {
		return params;
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
		return params.getFirst(key);
	}

	public List<String> getStrings(String key) {
		return params.get(key);
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
