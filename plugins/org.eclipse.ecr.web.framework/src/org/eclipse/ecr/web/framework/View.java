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
package org.eclipse.ecr.web.framework;

import java.io.OutputStream;
import java.io.Writer;

/**
 * @author bstefanescu
 * 
 */
public interface View {
	
	public View set(String key, Object value);
	
    public void render(OutputStream out) throws Exception;

    public void render(Writer out) throws Exception;

    public View arg(String key, Object value);
    
}
