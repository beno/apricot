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

import java.io.IOException;
import java.util.Map;

import org.eclipse.ecr.web.framework.fragments.SlotDirective.SlotContext;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;


/**
 * @author bstefanescu
 *
 */
public class FragmentDirective implements TemplateDirectiveModel {

	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
		
		SlotContext ctx = SlotDirective.getSlotContext(env);
		if (ctx == null) {
			throw new TemplateException("'fragment' directive must be called in a 'slot' context", env);
		}
		        
        try {
        	ctx.peek().currentHandler().render(env.getOut(), ctx.getInput());
        } catch (IOException e) {
        	throw e;
        } catch (TemplateException e) {
        	throw e;
        } catch (Exception e) {
        	throw new TemplateException("Failed to render fragment with "+ctx.peek().currentHandler(), e, env);
        }
	}
	
}
