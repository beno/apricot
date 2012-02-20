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

package org.eclipse.ecr.web.framework.rendering;

import java.io.Writer;

import org.eclipse.ecr.web.framework.forms.ftl.FieldDirective;
import org.eclipse.ecr.web.framework.forms.ftl.FormDirective;
import org.eclipse.ecr.web.framework.forms.ftl.InputDirective;
import org.eclipse.ecr.web.framework.forms.ftl.OptionDirective;
import org.eclipse.ecr.web.framework.forms.ftl.SelectDirective;
import org.eclipse.ecr.web.framework.forms.ftl.TextareaDirective;
import org.eclipse.ecr.web.framework.fragments.FragmentDirective;
import org.eclipse.ecr.web.framework.fragments.FragmentsDirective;
import org.eclipse.ecr.web.framework.fragments.SlotDirective;
import org.eclipse.ecr.web.framework.pagination.PaginationDirective;
import org.eclipse.ecr.web.rendering.api.RenderingException;
import org.eclipse.ecr.web.rendering.api.ResourceLocator;
import org.eclipse.ecr.web.rendering.fm.FreemarkerEngine;
import org.eclipse.ecr.web.rendering.fm.extensions.BlockWriter;
import org.eclipse.ecr.web.rendering.fm.extensions.BlockWriterRegistry;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @author bstefanescu
 *
 */
public class FreemarkerRenderingEngine extends FreemarkerEngine {

    public FreemarkerRenderingEngine() {
        this(null, null);
    }

    public FreemarkerRenderingEngine(Configuration cfg, ResourceLocator locator) {
    	super(cfg, locator);
    	this.cfg.setSharedVariable("slot", new SlotDirective());
        this.cfg.setSharedVariable("fragments", new FragmentsDirective());
        this.cfg.setSharedVariable("fragment", new FragmentDirective());
        
        this.cfg.setSharedVariable("form", new FormDirective());
        this.cfg.setSharedVariable("field", new FieldDirective());
        this.cfg.setSharedVariable("input", new InputDirective());
        this.cfg.setSharedVariable("textarea", new TextareaDirective());
        this.cfg.setSharedVariable("select", new SelectDirective());
        this.cfg.setSharedVariable("option", new OptionDirective());
        
        this.cfg.setSharedVariable("paginate", new PaginationDirective());
        
        this.cfg.setSharedVariable("navbar", new NavbarDirective());
        this.cfg.setSharedVariable("navbar_item", new NavbarItemDirective());
    }
    
	@Override
	public void render(String template, Object input, Writer writer)
			throws RenderingException {
		try {
            Template temp = cfg.getTemplate(template);
            BlockWriter bw = new BlockWriter(temp.getName(), "",
                    new BlockWriterRegistry());
            Environment env = temp.createProcessingEnvironment(input, bw,
                    wrapper);
            env.setCustomAttribute("__ROOT_DATA_MODEL__", input);
            env.process();
            bw.copyTo(writer);
        } catch (Exception e) {
            throw new RenderingException(e);
        }
	}
	
	public static Object getRootDataModel(Environment env) {
		return env.getCustomAttribute("__ROOT_DATA_MODEL__");
	}
	
}
