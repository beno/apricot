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
 */
package org.eclipse.ecr.web.jaxrs.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.ecr.core.api.CoreSession;
import org.eclipse.ecr.web.jaxrs.context.RequestContext;
import org.eclipse.ecr.web.jaxrs.session.impl.PerRequestCoreProvider;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class SessionFactory {

    public static final String SESSION_FACTORY_KEY = SessionFactory.class.getName();

    private static volatile String defaultRepository = "default";

    public static void setDefaultRepository(String repoName) {
        defaultRepository = repoName;
    }

    public static String getRepositoryName(HttpServletRequest request) {
        String v = request.getHeader("X-NXRepository");
        if (v == null) {
            v = request.getParameter("nxrepository");
        }
        return v != null ? v : defaultRepository;
    }

    public static CoreSessionProvider<?> getCoreProvider(HttpServletRequest request) {
        CoreSessionProvider<?> provider = (CoreSessionProvider<?>)request.getAttribute(SESSION_FACTORY_KEY);
        if (provider == null) {
            HttpSession s = request.getSession(false);
            if (s != null) {
                provider = (CoreSessionProvider<?>)s.getAttribute(SESSION_FACTORY_KEY);
            }
            if (provider == null) {
                provider = new PerRequestCoreProvider();
            }
            request.setAttribute(SESSION_FACTORY_KEY, provider);
        }
        return provider;
    }

    public static void dispose(HttpServletRequest request) {
        CoreSessionProvider<?> provider = (CoreSessionProvider<?>)request.getAttribute(SESSION_FACTORY_KEY);
        if (provider != null) {
            request.removeAttribute(SESSION_FACTORY_KEY);
            provider.onRequestDone(request);
        }
    }

    public static CoreSession getSession() {
        RequestContext ctx = RequestContext.getActiveContext();
        if (ctx == null) {
            throw new IllegalStateException("You are trying to acces RequestContext data but you are not in web request a context. Make sure you have the RequestContextFilter installed and you call this method from the HTTP request thread");
        }
        return getSession(ctx.getRequest());
    }

    public static CoreSession getSession(String repositoryName) {
        RequestContext ctx = RequestContext.getActiveContext();
        if (ctx == null) {
            throw new IllegalStateException("You are trying to acces RequestContext data but you are not in web request a context. Make sure you have the RequestContextFilter installed and you call this method from the HTTP request thread");
        }
        return getSession(ctx.getRequest(), repositoryName);
    }

    public static CoreSession getSession(HttpServletRequest request) {
        return getSession(request, getRepositoryName(request));
    }

    public static CoreSession getSession(HttpServletRequest request, String repositoryName) {
        return getCoreProvider(request).getSession(request, repositoryName);
    }

    public static SessionRef getSessionRef(HttpServletRequest request) {
        return getSessionRef(request, getRepositoryName(request));
    }

    public static SessionRef getSessionRef(HttpServletRequest request, String repositoryName) {
        return getCoreProvider(request).getSessionRef(request, repositoryName);
    }

}
