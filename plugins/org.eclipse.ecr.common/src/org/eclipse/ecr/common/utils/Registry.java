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
 *
 * $Id: Registry.java 2531 2006-09-04 23:01:57Z janguenot $
 */

package org.eclipse.ecr.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Generic registry implementation.
 *
 * @author <a href="mailto:ja@nuxeo.com">Julien Anguenot</a>
 */
public class Registry<T> {

    private final String name;

    private final Map<String, T> registry;

    public Registry(String name) {
        this.name = name;
        registry = new HashMap<String, T>();
    }

    public String getName() {
        return name;
    }

    public void register(String name, T object) {
        if (!isRegistered(name) && !isRegistered(object)) {
            registry.put(name, object);
        }
    }

    public void unregister(String name) {
        if (isRegistered(name)) {
            registry.remove(name);
        }
    }

    public boolean isRegistered(T object) {
        return registry.containsValue(object);
    }

    public boolean isRegistered(String name) {
        return registry.containsKey(name);
    }

    public int size() {
        return registry.size();
    }

    public T getObjectByName(String name) {
        return registry.get(name);
    }

    public void clear() {
        registry.clear();
    }

    public Set<String> getKeys() {
        return registry.keySet();
    }

}
