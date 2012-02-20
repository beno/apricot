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

import java.net.URL;
import java.util.List;

import org.eclipse.ecr.web.framework.adapters.AdapterManager;
import org.eclipse.ecr.web.framework.fragments.FragmentManager;
import org.eclipse.ecr.web.framework.fragments.FragmentManagerImpl;
import org.eclipse.ecr.web.framework.rendering.FreemarkerRenderingEngine;
import org.eclipse.ecr.web.framework.skins.SkinManager;
import org.eclipse.ecr.web.rendering.api.RenderingEngine;

/**
 * The application represent a JAX-RS application and all its client bundles (that contributed something to that application).
 * All these contribution are contributed only to the JAX-RS application domain. 
 * The domain name is the same as the JAX-RS application name which is defined in the manifest. 
 * The default domain "*" applies to all JAX-RS application.
 * 
 * An application root must specify the name of the application it belongs so that the WebContext is setup properly 
 * 
 * @author bstefanescu
 *
 */
public class WebApplication {

	public final static String DEFAULT_APP_NAME = "default"; 
	
	protected String name;
	
	protected SkinManager skins;
	
	protected AdapterManager adapters;
	
	protected FragmentManager fragments;
	
	protected RenderingEngine rendering;
	
	public WebApplication(String name) {
		this.name = name;
		this.skins = new SkinManager();
		this.adapters = new AdapterManager();
		this.fragments = new FragmentManagerImpl();
		this.rendering = new FreemarkerRenderingEngine();
		rendering.setResourceLocator(skins);
	}
	
	public String getName() {
		return name;
	}
	
	public void destroy() {
		name = null;
		rendering = null;
		skins = null;
		adapters = null;
		fragments = null;
	}
	
	
	public RenderingEngine getRenderingEngine() {
		return rendering;
	}
	
	public SkinManager getSkinManager() {
		return skins;
	}
	
	public AdapterManager getAdapterManager() {
		return adapters;
	}
	
	public FragmentManager getFragmentManager() {
		return fragments;
	}
	
	public URL resolve(String location) {
		return skins.resolve(location);
	}
	
	public String getMessage(String language, String key) {
		return skins.getMessageProvider().getMessage(language, key);
	}
	
	public String getMessage(String language, String key, Object ... args) {
		return skins.getMessageProvider().getMessage(language, key, args);
	}
	
	public String getMessage(String language, String key, List<Object> args) {
		return skins.getMessageProvider().getMessage(language, key, args);
	}
	
}
