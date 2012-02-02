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

package org.eclipse.ecr.common.utils;

import java.io.Serializable;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public final class Null implements Serializable {

    public static final Null VALUE = new Null();

    private static final long serialVersionUID = 1L;

    private Null() {
    }

    /**
     * Serialization handling.
     */
    private Object readResolve() {
        return VALUE;
    }

    @Override
    public String toString() {
        return "Null";
    }

}
