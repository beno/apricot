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


import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ecr.web.jaxrs.servlet.config.ServletRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sun.jersey.server.impl.provider.RuntimeDelegateImpl;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class Activator implements BundleActivator, ServiceTrackerCustomizer {

    private static final Log log = LogFactory.getLog(Activator.class);

    private static Activator instance;

    public static Activator getInstance() {
        return instance;
    }

    protected ServiceTracker httpServiceTracker;

    protected BundleContext context;
    protected ServiceReference pkgAdm;


    @Override
    public void start(BundleContext context) throws Exception {
        // we need to set by hand the runtime delegate to avoid letting ServiceFinder discover the implementation
        // which is not working in an OSGi environment
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());

        instance = this;
        this.context = context;
        pkgAdm = context.getServiceReference(PackageAdmin.class.getName());
        //TODO workaround to disable service tracker on regular Nuxeo distribs until finding a better solution
        if (!"Nuxeo".equals(context.getProperty(Constants.FRAMEWORK_VENDOR))) {
            httpServiceTracker = new ServiceTracker(context, HttpService.class.getName(), this);
            httpServiceTracker.open();
        }

        ApplicationManager.getInstance().start(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        ApplicationManager.getInstance().stop(context);

        if (httpServiceTracker != null) {
            httpServiceTracker.close();
            httpServiceTracker = null;
        }
        ServletRegistry.dispose();
        instance = null;
        context.ungetService(pkgAdm);
        pkgAdm = null;
        this.context = null;
    }

    public BundleContext getContext() {
        return context;
    }

    public PackageAdmin getPackageAdmin() {
        return (PackageAdmin)context.getService(pkgAdm);
    }

    @Override
    public Object addingService(ServiceReference reference) {
        Object service = context.getService(reference);
        try {
            if (service instanceof HttpService) {
                ServletRegistry.getInstance().initHttpService((HttpService)service);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize http service", e);
        }
        return service;
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        try {
            if (ServletRegistry.getInstance().getHttpService() == service) {
                ServletRegistry.getInstance().initHttpService(null);
            }
        } catch (Exception e) {
            log.error("Failed to remove http service", e);
        } finally {
            context.ungetService(reference);
        }
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        try {
            if (ServletRegistry.getInstance().getHttpService() == service) {
                ServletRegistry.getInstance().initHttpService(null);
                ServletRegistry.getInstance().initHttpService((HttpService)service);
            }
        } catch (Exception e) {
            log.error("Failed to update http service", e);
        }
    }
}
