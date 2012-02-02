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

import java.lang.reflect.Field;

/**
 * @author  <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class XFieldAccessor implements XAccessor {

    private final Field field;

    public XFieldAccessor(Field field) {
        this.field = field;
        this.field.setAccessible(true);
    }

    public Class getType() {
        return field.getType();
    }

    public void setValue(Object instance, Object value)
            throws IllegalAccessException {
        field.set(instance, value);
    }

    public Object getValue(Object instance) throws Exception  {
       return field.get(instance);
    }

}
