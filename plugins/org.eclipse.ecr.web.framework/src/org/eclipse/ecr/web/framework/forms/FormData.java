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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

public class FormData extends HashMap<String, List<String>> implements MultivaluedMap<String, String> {

	private static final long serialVersionUID = -7063103957476783018L;

	public FormData() {
	}
	
	public FormData(int initialCapacity) {
		super (initialCapacity);
	}

	@Override
	public void add(String key, String value) {
		List<String> list = get(key);
		if (list == null) {
			list = new ArrayList<String>();
			put(key, list);
		}
		list.add(value == null ? "" : value);
	}

	@Override
	public String getFirst(String key) {
		List<String> list = get(key);
		if (list == null || list.isEmpty()) {
			return null;
		}		
		return list.get(0);
	}

	@Override
	public void putSingle(String key, String value) {
		put(key, Collections.singletonList(value == null ? "" : value));
	}
	
}
