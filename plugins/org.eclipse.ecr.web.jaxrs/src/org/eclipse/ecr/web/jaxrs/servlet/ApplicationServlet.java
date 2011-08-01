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
package org.eclipse.ecr.web.jaxrs.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.ecr.web.jaxrs.ApplicationHost;
import org.eclipse.ecr.web.jaxrs.ApplicationManager;
import org.eclipse.ecr.web.jaxrs.Reloadable;
import org.eclipse.ecr.web.jaxrs.Utils;
import org.eclipse.ecr.web.jaxrs.servlet.config.ServletDescriptor;
import org.eclipse.ecr.web.jaxrs.views.ResourceContext;
import org.eclipse.ecr.web.rendering.api.RenderingEngine;
import org.eclipse.ecr.web.rendering.api.ResourceLocator;
import org.eclipse.ecr.web.rendering.fm.FreemarkerEngine;
import org.osgi.framework.Bundle;

import com.sun.jersey.spi.container.servlet.ServletContainer;


/**
 * A hot re-loadable JAX-RS servlet.
 * 
 * This servlet is building a Jersey JAX-RS Application. If you need to support 
 * other JAX-RS containers than Jersey you need to write your own servlet. 
 * <p>
 * Use it as the webengine servlet in web.xml if you want hot reload, otherwise
 * directly use the Jersey servlet: {@link ServletContainer}.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class ApplicationServlet extends HttpServlet implements ManagedServlet, Reloadable, ResourceLocator {

    private static final long serialVersionUID = 1L;

    protected volatile boolean isDirty = false;

    protected Bundle bundle;

    protected ApplicationHost app;

    protected ServletContainer container;

    protected String resourcesPrefix;


    @Override
    public void setDescriptor(ServletDescriptor sd) {
        this.bundle = sd.getBundle();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        resourcesPrefix = config.getInitParameter("resources.prefix");
        if (resourcesPrefix == null) {
            resourcesPrefix = "/skin";
        }
        String name = config.getInitParameter("application.name");
        if (name == null) {
            name = ApplicationManager.DEFAULT_HOST;
        }
        app = ApplicationManager.getInstance().getOrCreateApplication(name);
        container = new ServletContainer(app);

        initContainer(config);
        app.setRendering(initRendering(config));

        app.addReloadListener(this);
    }

    @Override
    public void destroy() {
        destroyContainer();
        destroyRendering();
        container = null;
        app = null;
        bundle = null;
        resourcesPrefix = null;
    }

    @Override
    public synchronized void reload() {
        isDirty = true;
    }

    public RenderingEngine getRenderingEngine() {
        return app.getRendering();
    }

    public Bundle getBundle() {
        return bundle;
    }

    public ServletContainer getContainer() {
        return container;
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String pinfo = request.getPathInfo();
        if (pinfo != null && pinfo.startsWith(resourcesPrefix)) {
            super.service(request, response);
        } else {
            containerService(request, response);
        }
    }

    protected void containerService(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        if (isDirty) {
            reloadContainer();
        }
        String method = request.getMethod().toUpperCase();
        if (!"GET".equals(method)) {
            // force reading properties because jersey is consuming one
            // character
            // from the input stream - see WebComponent.isEntityPresent.
            request.getParameterMap();
        }
        ResourceContext ctx = new ResourceContext(app);
        ctx.setRequest(request);
        ResourceContext.setContext(ctx);
        request.setAttribute(ResourceContext.class.getName(), ctx);
        try {
            container.service(request, response);
        } finally {
            ResourceContext.destroyContext();
            request.removeAttribute(ResourceContext.class.getName());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        InputStream in = getServletContext().getResourceAsStream(pathInfo.substring(resourcesPrefix.length()));
        if (in != null) {
            String ctype = getServletContext().getMimeType(pathInfo);
            if (ctype != null) {
                resp.addHeader("Content-Type", ctype);
            }
            try {
                OutputStream out = resp.getOutputStream();
                byte[] bytes = new byte[1024*64];
                int r = in.read(bytes);
                while (r > -1) {
                    if (r > 0) {
                        out.write(bytes, 0, r);
                    }
                    r = in.read(bytes);
                }
                out.flush();
            } finally {
                in.close();
            }
        }
    }

    protected RenderingEngine initRendering(ServletConfig config) throws ServletException {
        RenderingEngine rendering;
        try {
            String v = config.getInitParameter(RenderingEngine.class.getName());
            if (v != null) {
                rendering = (RenderingEngine)Utils.getClassRef(v, bundle).newInstance();
            } else { // default settings
                rendering = new FreemarkerEngine();
                ((FreemarkerEngine)rendering).getConfiguration().setClassicCompatible(false);
            }
            rendering.setResourceLocator(this);
            return rendering;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void destroyRendering() {
        // do nothing
    }

    protected void initContainer(ServletConfig config) throws ServletException {
        Thread thread = Thread.currentThread();
        ClassLoader cl = thread.getContextClassLoader();
        thread.setContextClassLoader(ServiceClassLoader.getLoader());
        try {
            container.init(getServletConfig());
        } finally {
            thread.setContextClassLoader(cl);
        }
    }

    protected void destroyContainer() {
        Thread thread = Thread.currentThread();
        ClassLoader cl = thread.getContextClassLoader();
        thread.setContextClassLoader(ServiceClassLoader.getLoader());
        try {
            container.destroy();
            container = null;
        } finally {
            thread.setContextClassLoader(cl);
        }
    }

    protected synchronized void reloadContainer() throws ServletException {
        // reload is not working correctly since old classes are still referenced
        // for this to work we need a custom ResourceConfig but all fields in jersey
        // classes are private so we cannot set it ...
        //super.reload();
        Thread thread = Thread.currentThread();
        ClassLoader cl = thread.getContextClassLoader();
        thread.setContextClassLoader(ServiceClassLoader.getLoader());
        try {
            container.destroy();
            container = new ServletContainer(app);
            container.init(getServletConfig());
        } finally {
            thread.setContextClassLoader(cl);
            isDirty = false;
        }
    }


    @Override
    public File getResourceFile(String key) {
        return null;
    }

    @Override
    public URL getResourceURL(String key) {
        return ResourceContext.getContext().findEntry(key);
    }

}
