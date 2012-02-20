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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The default messages corresponding to messages.properties file
 * 
 * @author bstefanescu
 *
 */
public class DefaultMessageBundle extends ResourceBundle {

	protected Map<String, String> properties;
	
	public DefaultMessageBundle(Map<String, String> properties) {
		this.properties = properties == null ? new HashMap<String, String>() : properties;
	}
	
	@Override
	public Enumeration<String> getKeys() {
		return Collections.enumeration(properties.keySet());
	}

	@Override
	protected Object handleGetObject(String key) {
		if (key == null) {
			throw new NullPointerException();
		}
        return properties.get(key);
	}

}
