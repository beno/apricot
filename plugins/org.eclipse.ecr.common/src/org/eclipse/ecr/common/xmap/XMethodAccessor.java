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
 * $Id$
 */

package org.eclipse.ecr.common.xmap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class XMethodAccessor implements XAccessor {

    private final Method setter;
    private final Class klass;
    Method getter;

    public XMethodAccessor(Method method, Class klass) {
        setter = method;
        setter.setAccessible(true);
        //
        this.klass = klass;
    }

    public Class getType() {
        return setter.getParameterTypes()[0];
    }

    public void setValue(Object instance, Object value) throws IllegalAccessException, InvocationTargetException {
        setter.invoke(instance, value);
    }

    @Override
    public String toString() {
        return "XMethodSetter {method: " + setter + '}';
    }

    public Object getValue(Object instance) throws Exception {
        // lazy initialization for getter to keep the compatibility
        // with current xmap definition
        if (getter == null) {
            getter = findGetter(klass);
        }
        if (getter != null) {
            return getter.invoke(instance);
        }
        return null;
    }

    private Method findGetter(Class klass) {
        String setterName = setter.getName();
        if (setterName.toLowerCase().startsWith("set")) {
            String suffix = setterName.substring(3);
            String prefix = null;

            Class<?>[] classes = setter.getParameterTypes();
            Class<?> clazz = classes[0];
            // compute the getter name
            if (clazz == Boolean.class || clazz == Boolean.TYPE) {
                prefix = "is";
            } else {
                prefix = "get";
            }
            String getterName = prefix + suffix;
            try {
                return klass.getMethod(getterName, new Class[0]);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "there is NO getter defined for annotated setter: " + setterName, e);
            }
        }
        return null;
    }

}
