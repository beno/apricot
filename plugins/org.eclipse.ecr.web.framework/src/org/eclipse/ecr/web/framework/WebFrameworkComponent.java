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
 */
package org.eclipse.ecr.web.framework;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.ecr.runtime.model.ComponentContext;
import org.eclipse.ecr.runtime.model.ComponentInstance;
import org.eclipse.ecr.runtime.model.DefaultComponent;
import org.eclipse.ecr.web.framework.adapters.AdapterFactoryDescriptor;
import org.eclipse.ecr.web.framework.fragments.FragmentDescriptor;
import org.eclipse.ecr.web.framework.skins.SkinFragment;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class WebFrameworkComponent extends DefaultComponent implements WebFramework {

	public final static String XP_FRAGMENTS = "fragments";
    public final static String XP_ADAPTERS = "adapters";
    public final static String XP_SKINS = "skins";

    protected ConcurrentMap<String, WebApplication> apps;
    
    protected Map<String,String> mimeTypes;


    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public void activate(ComponentContext context) throws Exception {
    	apps = new ConcurrentHashMap<String, WebApplication>();
    	URL url = context.getRuntimeContext().getBundle().getEntry("META-INF/mime.properties");
    	mimeTypes = new HashMap<String, String>();
    	if (url != null) {
    		Properties props = new Properties();
    		InputStream in = url.openStream();
    		try {
    			props.load(in);
    			mimeTypes.putAll(new HashMap<String, String>((Map)props));
    		} finally {
    			in.close();
    		}
    	}
    	// create the default application
    	getOrCreateApplication(WebApplication.DEFAULT_APP_NAME);
    }

    @Override
    public void deactivate(ComponentContext context) throws Exception {
    	apps = null;
    	mimeTypes = null;
    }
    
    @Override
    public WebApplication getOrCreateApplication(String name) {
    	WebApplication app = apps.get(name);
    	if (app == null) {
    		synchronized (this) {
    			app = new WebApplication(name);
    			apps.put(name, app);
    		}
    	}
    	return app;
    }
    
    @Override
    public WebApplication getApplication(String name) {
    	return apps.get(name);
    }    
    
    public WebApplication getDefaultApplication() {
    	return getApplication(WebApplication.DEFAULT_APP_NAME);
    }
    
    @Override
    public WebApplication[] getApplications() {
    	return apps.values().toArray(new WebApplication[apps.size()]);
    }

    public void addAdapterFactory(AdapterFactoryDescriptor descriptor) throws Exception {
    	getOrCreateApplication(descriptor.app).getAdapterManager().addAdapterFactory(descriptor);
    }

    public void removeAdapterFactory(AdapterFactoryDescriptor descriptor) {
    	WebApplication app = getApplication(descriptor.app);
    	if (app != null) {    		
    		app.getAdapterManager().removeAdapterFactory(descriptor);
    	}
    }

	public void addSkinFragment(SkinFragment fragment) {
		getOrCreateApplication(fragment.app).getSkinManager().addSkinFragment(fragment);
	}

	public void removeSkinFragment(SkinFragment fragment) {		
    	WebApplication app = getApplication(fragment.app);
    	if (app != null) {    		
    		app.getSkinManager().removeSkinFragment(fragment);
    	}
	}

	public void addFragmentDescriptor(FragmentDescriptor fragment) {
		getOrCreateApplication(fragment.app).getFragmentManager().addFragment(fragment);
	}

	public void removeFragmentDescriptor(FragmentDescriptor fragment) {		
    	WebApplication app = getApplication(fragment.app);
    	if (app != null) {    		
    		app.getFragmentManager().removeFragment(fragment);
    	}
	}

	@Override
	public String getMimeType(String fileName, String defaultType) {
		String type = null;
		int p = fileName.lastIndexOf('.');
		if (p > -1) {
			String ext = fileName.substring(p+1);
			type = mimeTypes.get(ext); 
		}		
		return type != null ? type : defaultType;
	}

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (XP_ADAPTERS.equals(extensionPoint)) {
            addAdapterFactory((AdapterFactoryDescriptor) contribution);
        } else if (XP_SKINS.equals(extensionPoint)) {
        	SkinFragment fragment = (SkinFragment)contribution;
        	fragment.setBundle(contributor.getContext().getBundle());
        	addSkinFragment(fragment);
        } else if (XP_FRAGMENTS.equals(extensionPoint)) {
        	FragmentDescriptor fragment = (FragmentDescriptor)contribution;
        	fragment.build(contributor.getContext().getBundle());
        	addFragmentDescriptor(fragment);
        }
    }

    @Override
    public void unregisterContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
        if (XP_ADAPTERS.equals(extensionPoint)) {
            removeAdapterFactory((AdapterFactoryDescriptor) contribution);
        } else if (XP_SKINS.equals(extensionPoint)) {
        	removeSkinFragment((SkinFragment)contribution);
        } else if (XP_FRAGMENTS.equals(extensionPoint)) {
        	FragmentDescriptor fragment = (FragmentDescriptor)contribution;
        	removeFragmentDescriptor(fragment);        	
        }
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (WebFramework.class.isAssignableFrom(adapter)) {
            return adapter.cast(this);
        }
        return null;
    }
    
}
