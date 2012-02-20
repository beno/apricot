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

package org.eclipse.ecr.web.framework.fragments;

import java.io.Writer;
import java.util.Map;

import org.eclipse.ecr.web.framework.fragments.mvel.MvelCondition;
import org.eclipse.ecr.web.framework.fragments.mvel.MvelTemplate;
import org.w3c.dom.DocumentFragment;

/**
 * The default fragment handler. Can be sub-classed by implementors to add custom logic.
 * See {@link #loadConfiguration(DocumentFragment)} method for supporting custom configuration.
 * 
 * This default implementation is using mvel for templating and enablement conditions.
 * If not content type is specified static content will be served.
 * 
 * @author bstefanescu
 *
 */
public class DefaultFragmentHandler implements FragmentHandler {

	protected FragmentDescriptor fd;
	
	protected FragmentTemplate template;
	
	protected Condition enablement;
	
	@Override
	public void init(final FragmentDescriptor fd) throws Exception {
		this.fd = fd;
		if (fd.configuration != null) {
			loadConfiguration(fd.configuration);
		}
		if (fd.content == null || fd.content.length() == 0) {
			fd.content = getDefaultContent();
		}
		if ("mvel".equals(fd.contentType)) {
			template = new MvelTemplate(fd.content);
		} else {
			template = new StaticTemplate(fd.content);
		}
		if (fd.enablement != null && fd.enablement.length() > 0) {
			enablement = new MvelCondition(fd.enablement);
		} else {
			enablement = Condition.TRUE; 
		}
	}

	@Override
	public boolean isEnabled(Map<String, Object> ctx) throws Exception {
		return enablement.eval(ctx);
	}

	@Override
	public void render(Writer writer, Map<String, Object> ctx) throws Exception {
		template.render(writer, ctx);
	}

	protected String getDefaultContent() {
		return "NO CONTENT";
	}
	
	/**
	 * Implementors may override to load custom configuration found 
	 * in the descriptor 'configuration' element 
	 * @param doc
	 * @throws Exception
	 */
	protected void loadConfiguration(DocumentFragment doc) throws Exception {
		// do nothing
	}
}
