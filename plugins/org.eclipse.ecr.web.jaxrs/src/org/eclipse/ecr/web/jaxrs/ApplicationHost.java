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
package org.eclipse.ecr.web.jaxrs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import org.eclipse.ecr.web.jaxrs.servlet.config.ResourceExtension;
import org.eclipse.ecr.web.jaxrs.views.BundleResource;
import org.eclipse.ecr.web.jaxrs.views.TemplateViewMessageBodyWriter;
import org.eclipse.ecr.web.jaxrs.views.ViewMessageBodyWriter;
import org.eclipse.ecr.web.rendering.api.RenderingEngine;
import org.osgi.framework.Bundle;

/**
 * A composite JAX-RS application that can receive fragments from outside.
 * 
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class ApplicationHost extends Application {

    protected final String name;

    protected final List<ApplicationFragment> apps;

    protected List<Reloadable> listeners;

    protected RenderingEngine rendering;

    /**
     * Sub-Resources extensions
     */
    protected Map<String, ResourceExtension> extensions;


    /**
     * Root resource classes to owner bundles.
     * This is a fall-back for FrameworkUtils.getBundle(class)
     * since is not supported in all OSGi like frameworks
     */
    protected HashMap<Class<?>, Bundle> class2Bundles;


    public ApplicationHost(String name) {
        this.name = name;
        apps = new ArrayList<ApplicationFragment>();
        class2Bundles = new HashMap<Class<?>, Bundle>();
        listeners = new ArrayList<Reloadable>();
        extensions = new HashMap<String, ResourceExtension>();
    }

    public BundleResource getExtension(BundleResource target, String segment) {
        ResourceExtension xt = getExtension(target.getClass().getName()+"#"+segment);
        if (xt != null) {
            BundleResource res = target.getResource(xt.getResourceClass());
            if (res != null && res.accept(target)) {
                res.getContext().pushBundle(xt.getBundle());
                return res;
            }
        }
        return null;
    }

    public RenderingEngine getRendering() {
        return rendering;
    }

    public void setRendering(RenderingEngine rendering) {
        this.rendering = rendering;
    }

    public synchronized void addExtension(ResourceExtension xt) throws Exception {
        extensions.put(xt.getId(), xt);
        class2Bundles.put(xt.getResourceClass(), xt.getBundle());
        if (rendering != null) {
            rendering.flushCache();
        }
    }

    public synchronized void removeExtension(ResourceExtension xt) throws Exception {
        extensions.remove(xt.getId());
        class2Bundles.remove(xt.getResourceClass());
        if (rendering != null) {
            rendering.flushCache();
        }
    }

    public synchronized ResourceExtension getExtension(String id) {
        return extensions.get(id);
    }

    public synchronized ResourceExtension[] getExtensions(ResourceExtension xt) {
        return extensions.values().toArray(new ResourceExtension[extensions.size()]);
    }

    public String getName() {
        return name;
    }

    public synchronized void add(ApplicationFragment app) {
        apps.add(app);
    }

    public synchronized void remove(ApplicationFragment app) {
        apps.remove(app);
    }

    public synchronized ApplicationFragment[] getApplications() {
        return apps.toArray(new ApplicationFragment[apps.size()]);
    }

    public synchronized void addReloadListener(Reloadable listener) {
        listeners.add(listener);
    }

    public synchronized void removeReloadListener(Reloadable listener) {
        listeners.remove(listener);
    }

    public synchronized void reload() throws Exception {
        for (ApplicationFragment fragment : apps) {
            fragment.reload();
        }
        //TODO this will not work with extension subresources - find a fix
        class2Bundles = new HashMap<Class<?>, Bundle>();
        for (Reloadable listener : listeners) {
            listener.reload();
        }
        if (rendering != null) {
            rendering.flushCache();
        }
    }

    /**
     * Get the bundle declaring the given root class.
     * This method is not synchronized since it is assumed to be called
     * after the application was created and before it was destroyed.
     * <br>
     * When a bundle is refreshing this method may throw
     * exceptions but it is not usual to refresh bundles at runtime
     * and making requests in same time.
     * 
     * @param clazz
     * @return
     */
    public Bundle getBundle(Class<?> clazz) {
        return class2Bundles.get(clazz);
    }

    @Override
    public synchronized Set<Class<?>> getClasses() {
        HashSet<Class<?>> result = new HashSet<Class<?>>();
        for (ApplicationFragment app : getApplications()) {
            for (Class<?> clazz : app.getClasses()) {
                if (clazz.isAnnotationPresent(Path.class)) {
                    class2Bundles.put(clazz, app.getBundle());
                }
                result.add(clazz);
            }
        }
        return result;
    }

    @Override
    public synchronized Set<Object> getSingletons() {
        HashSet<Object> result = new HashSet<Object>();
        result.add(new TemplateViewMessageBodyWriter());
        result.add(new ViewMessageBodyWriter());
        for (ApplicationFragment app : getApplications()) {
            for (Object obj : app.getSingletons()) {
                if (obj.getClass().isAnnotationPresent(Path.class)) {
                    class2Bundles.put(obj.getClass(), app.getBundle());
                }
                result.add(obj);
            }
        }
        return result;
    }

}
