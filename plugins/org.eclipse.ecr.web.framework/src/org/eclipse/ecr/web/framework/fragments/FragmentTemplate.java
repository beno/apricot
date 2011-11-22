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

package org.eclipse.ecr.web.framework.fragments;

import java.io.Writer;
import java.util.Map;

/**
 * @author bstefanescu
 *
 */
public interface FragmentTemplate {

	void render(Writer writer, Map<String, Object> ctx) throws Exception;
	
}
