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

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

/**
 * Search main context if name not found in nuxeo's context.
 *
 * @since 5.6
 *
 */
public class NamingContextFacade extends NamingContext {

    private static final long serialVersionUID = 1L;

    protected final Context delegate;

    public NamingContextFacade(Context delegate) throws NamingException {
        super();
        this.delegate = delegate;
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        try {
            return super.lookup(name);
        } catch (NamingException e) {
            return delegate.lookup(name);
        }
    }


}
