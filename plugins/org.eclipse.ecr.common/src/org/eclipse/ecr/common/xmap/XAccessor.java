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

/**
 * @author  <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface XAccessor {

    /**
     * Gets the type of the object to be set by this setter.
     *
     * @return the setter object type
     */
    Class getType();

    /**
     * Sets the value of the underlying member.
     *
     * @param instance the instance of the object that owns this field
     * @param value the value to set
     * @throws Exception
     */
    void setValue(Object instance, Object value)
            throws Exception;


    Object getValue(Object instance) throws Exception;

}
