/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 */

package org.eclipse.ecr.common;

import java.util.Calendar;

/**
 * This class is used for transmitting dirty tag context on server and client
 * side from EJB invokes to the core API (NXP-4914).
 * <p>
 * Core API is loaded in a
 * separate class loader and cannot be accessed by the interceptor. In any
 * context, nuxeo common classes are always accessible by any class loaders.
 * This is the only place identified for putting that kind of logic without
 * modifying the server assemblies.
 *
 * @author matic
 */
public class DirtyUpdateInvokeBridge {

    protected static final ThreadLocal<ThreadContext> contextHolder = new ThreadLocal<ThreadContext>();

    private DirtyUpdateInvokeBridge() {
    }

    public static class ThreadContext {
        public final Long tag;

        public final Long invoked;

        ThreadContext(Long tag) {
            this.tag = tag;
            invoked = Calendar.getInstance().getTimeInMillis();
        }
    }

    public static void putTagInThreadContext(Object tag) {
        contextHolder.set(new ThreadContext((Long) tag));
    }

    public static void clearThreadContext() {
        contextHolder.remove();
    }

    public static ThreadContext getThreadContext() {
        return contextHolder.get();
    }

}
