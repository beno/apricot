/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.ecr.runtime.jtajca;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.apache.xbean.naming.context.WritableContext;

/**
 * Provides access to the nuxeo container naming context
 *
 */
public class NamingContextFactory implements InitialContextFactory {

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment)
            throws NamingException {
        if (NuxeoContainer.rootContext == null) {
            NuxeoContainer.rootContext = new NamingContext();
        }
        return NuxeoContainer.rootContext;
    }


}
