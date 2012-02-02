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

package org.eclipse.ecr.common.xmap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author  <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@XMemberAnnotation(XMemberAnnotation.NODE_MAP)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface XNodeMap {

    /**
     * A path expression specifying the XML node to bind to.
     *
     * @return the node xpath
     */
    String value();

    /**
     * Whether to trim text content for element nodes.
     */
    boolean trim() default true;

    /**
     * The path relative to the current node
     * (which is located by {@link XNodeMap#value()}) which contain
     * the map key to be used.
     */
    String key();

    /**
     * The type of collection object.
     */
    Class type();

    /**
     * The type of the objects in this collection.
     *
     * @return the type of items
     */
    Class componentType();

    /**
     * Whether the container should be set to null when no value is specified.
     */
    boolean nullByDefault() default false;

}
