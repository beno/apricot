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
 * $Id$
 */

package org.eclipse.ecr.common.collections;

import java.util.HashMap;

/**
 * A Class keyed map sensitive to class hierarchy.
 * This map provides an additional method {@link #find(Class)}
 * that can be used to lookup a class compatible to the given one
 * depending on the class hierarchy.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class ClassMap<T> extends HashMap<Class<?>, T>{

    private static final long serialVersionUID = 1L;

    public T find(Class<?> key) {
        T v = get(key);
        if (v == null) {
            Class<?> sk = key.getSuperclass();
            if (sk != null) {
                v = get(sk);
            }
            Class<?>[] itfs = null;
            if (v == null) { // try interfaces
                itfs = key.getInterfaces();
                for (Class<?> itf : itfs) {
                    v = get(itf);
                    if (v != null) {
                        break;
                    }
                }
            }
            if (v == null) {
                if (sk != null) { // superclass
                    v = find(sk);
                }
                if (v == null) { // interfaces
                    for (Class<?> itf : itfs) {
                        v = find(itf);
                        if (v != null) {
                            break;
                        }
                    }
                }
            }
            if (v != null) {
                put(key, v);
            }
        }
        return v;
    }

}
