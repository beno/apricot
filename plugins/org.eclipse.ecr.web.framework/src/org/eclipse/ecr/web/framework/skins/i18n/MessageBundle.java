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

package org.eclipse.ecr.web.framework.skins.i18n;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author bstefanescu
 *
 */
public class MessageBundle extends ResourceBundle {

	/**
	 * The parent to delegate
	 */
	protected DefaultMessageBundle parent;
	
	protected Map<String,String> properties;

	/**
	 * parent and properties cannot be null
	 * @param parent
	 * @param properties
	 */
	MessageBundle(DefaultMessageBundle parent, Map<String,String> properties) {
		this.parent = parent;
		this.properties = properties;
	}
	
	@Override
	public Enumeration<String> getKeys() {//TODO this is returning duplicate keys
		HashSet<String> set = new HashSet<String>();
		set.addAll(parent.properties.keySet());
		set.addAll(properties.keySet());
		return Collections.enumeration(set);
	}

	@Override
	protected Object handleGetObject(String key) {
		if (key == null) {
			throw new NullPointerException();
		}
        Object v = properties.get(key);
        if (v == null) {
        	v = parent.handleGetObject(key);
        }
        return v;
	}

}
