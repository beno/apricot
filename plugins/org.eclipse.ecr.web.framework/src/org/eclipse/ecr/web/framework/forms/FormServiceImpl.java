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
import java.util.Map;

import org.eclipse.ecr.runtime.model.ComponentContext;
import org.eclipse.ecr.runtime.model.ComponentInstance;
import org.eclipse.ecr.runtime.model.DefaultComponent;

public class FormServiceImpl extends DefaultComponent implements FormService {

	public final static String XP_FORMS = "forms";
	
	protected Map<String, FieldValidator> validators;
	protected Map<String, FormDescriptor> forms;
	
	@Override
	public void activate(ComponentContext context) throws Exception {
		forms = new HashMap<String, FormDescriptor>();
		validators = new HashMap<String, FieldValidator>();
		validators.put("required", new FieldValidator() {
			@Override
			public boolean validate(RuleDescriptor rd, String value, Form form) {
				return value != null && value.length() > 0;
			}
		});
		validators.put("minlen", new FieldValidator() {
			@Override
			public boolean validate(RuleDescriptor rd, String value, Form form) {
				return Integer.parseInt(rd.value) <= value.length();
			}
		});
		validators.put("maxlen", new FieldValidator() {
			@Override
			public boolean validate(RuleDescriptor rd, String value, Form form) {
				return Integer.parseInt(rd.value) >= value.length();
			}
		});
		validators.put("min", new FieldValidator() {
			@Override
			public boolean validate(RuleDescriptor rd, String value, Form form) {
				return Integer.parseInt(rd.value) <= Integer.parseInt(value);
			}
		});
		validators.put("max", new FieldValidator() {
			@Override
			public boolean validate(RuleDescriptor rd, String value, Form form) {
				return Integer.parseInt(rd.value) >= Integer.parseInt(value);
			}
		});
		validators.put("regex", new FieldValidator() {
			@Override
			public boolean validate(RuleDescriptor rd, String value, Form form) {
				return value.matches(rd.value);
			}
		});
		validators.put("sameAs", new FieldValidator() {
			@Override
			public boolean validate(RuleDescriptor rd, String value, Form form) {
				return value != null && value.equals(form.getString(rd.value));
			}
		});
	}
	
	@Override
	public void deactivate(ComponentContext context) throws Exception {
		forms = null;
		validators = null;
	}
	
	@Override
	public void registerContribution(Object contribution,
			String extensionPoint, ComponentInstance contributor)
			throws Exception {
		if (XP_FORMS.equals(extensionPoint)) {
			FormDescriptor fd = (FormDescriptor)contribution;
			forms.put(fd.id, fd);
		}
	}
	
	@Override
	public void unregisterContribution(Object contribution,
			String extensionPoint, ComponentInstance contributor)
			throws Exception {
	}
	
	@Override
	public FormDescriptor getFormDescriptor(String id) {
		return forms.get(id);
	}
	
	public FieldValidator getValidator(String type) {
		return validators.get(type);
	}
	
}
