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

package org.eclipse.ecr.web.framework.fragments.mvel;

import java.io.Writer;
import java.util.Map;

import org.eclipse.ecr.web.framework.fragments.FragmentTemplate;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

/**
 * @author bstefanescu
 *
 */
public class MvelTemplate implements FragmentTemplate {

	private String content;
	private volatile CompiledTemplate temp;
	
	public MvelTemplate(String content) {
		this.content = content;
	}
	
	public final CompiledTemplate getExpression() {
		CompiledTemplate c = temp;
		if (c == null) {
			synchronized (this) {
				if (temp == null) {
					temp = TemplateCompiler.compileTemplate(content); 
				}
				c = temp;
			}
		}
		return c;
	}	
	
	@Override
	public void render(Writer writer, Map<String, Object> ctx) throws Exception {
		Object result = TemplateRuntime.execute(getExpression(), ctx);
		if (result != null) {
			writer.write(result.toString());
		}
	}

}
