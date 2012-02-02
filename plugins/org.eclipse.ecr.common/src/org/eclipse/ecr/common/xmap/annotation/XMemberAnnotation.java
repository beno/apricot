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
 * An annotation to identify XMap annotations.
 * <p>
 * This annotation has a single parameter "value" of type
 * <code>int</code> that specifies the type of the annotation.
 *
 * @author  <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface XMemberAnnotation {

    int NODE           = 1;
    int NODE_LIST      = 2;
    int NODE_MAP       = 3;
    int PARENT         = 4;
    int CONTENT        = 5;
    int CONTEXT        = 6;

    /**
     * The type of the annotation.
     */
    int value();

}
