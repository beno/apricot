/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stephane Lacoin
 */

package org.eclipse.ecr.runtime.jtajca;

import javax.naming.NamingException;

import org.apache.xbean.naming.context.WritableContext;

/**
 * Naming context for nuxeo container. Basically re-locate
 * geronimo implementation in nuxeo namespace.
 *
 * @since 5.6
 *
 */
public class NamingContext extends WritableContext {

    public NamingContext() throws NamingException {
        super();
    }

    private static final long serialVersionUID = 1L;

}
