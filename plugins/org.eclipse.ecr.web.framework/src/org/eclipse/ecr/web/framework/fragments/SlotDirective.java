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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.ecr.web.framework.WebApplication;
import org.eclipse.ecr.web.framework.rendering.FreemarkerRenderingEngine;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;


/**
 * @author bstefanescu
 *
 */
public class SlotDirective implements TemplateDirectiveModel {

	public static SlotContext getSlotContext(Environment env) {
		return (SlotContext)env.getCustomAttribute(SlotDirective.class.getName());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
		
        String id = null;
        SimpleScalar scalar = (SimpleScalar) params.get("id");
        if (scalar != null) {
            id = scalar.getAsString();
        } else {
            throw new TemplateModelException("id attribute is not defined");
        }
        
        Map<String,Object> input = (Map<String,Object>)FreemarkerRenderingEngine.getRootDataModel(env);
		if (input == null) {
			throw new TemplateException("Failed to invoke 'slot' directive: cannot retrieve template data model", env);
		}
		WebApplication app = (WebApplication)input.get("Application");

        FragmentDescriptor[] fds = app.getFragmentManager().getFragments(id);
        if (fds == null || fds.length == 0) {
        	return;
        }
        		
        String key = SlotDirective.class.getName();
        SlotContext ctx = (SlotContext)env.getCustomAttribute(key);
        if (ctx == null) {
        	ctx = new SlotContext(app, input);
        	env.setCustomAttribute(key, ctx);
        }
        ctx.push(new Slot(fds));
        try {
        	body.render(env.getOut());
        } finally {
        	ctx.pop();
        }
        
	}

	
	class Slot implements Iterator<FragmentDescriptor>, Iterable<FragmentDescriptor> {
		protected FragmentDescriptor[] fds;
		protected int index = 0;
		
		protected Slot prev;
		
		public Slot(FragmentDescriptor[] fds) {
			this.fds = fds;
		}
		
		@Override
		public Iterator<FragmentDescriptor> iterator() {
			return this;
		}
		
		public FragmentDescriptor current() {
			return fds[index-1];
		}

		public FragmentHandler currentHandler() {
			return fds[index-1].getHandler();
		}
		
		@Override
		public boolean hasNext() {
			return index < fds.length;
		}
		
		@Override
		public FragmentDescriptor next() {
			try {
				return fds[index++];
			} catch (Throwable t) {
				throw new NoSuchElementException();
			}
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	class SlotContext {
		protected Map<String,Object> input;
		protected WebApplication app;
		protected Slot tail;
		
		public SlotContext(WebApplication app, Map<String, Object> rootModel) throws TemplateException {
			this.app = app;
			this.input = rootModel;
		}

		public WebApplication getApplication() {
			return app;
		}
		
		public final void push(Slot slot) {
			slot.prev = tail;
			tail = slot;
		}
		
		public final Slot pop() {
			if (tail == null) {
				return null;
			}			
			Slot slot = tail;			
			tail = slot.prev;
			slot.prev = null;
			return slot;
		}

		public final Slot peek() {
			return tail;
		}
		
		public Map<String, Object> getInput() {
			return input;
		}
	}
}
