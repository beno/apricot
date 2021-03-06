/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 */
package org.eclipse.ecr.application;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

import org.eclipse.ecr.runtime.jtajca.NamingContextFactory;

/**
 * @author bstefanescu
 *
 */
public class ContextFactoryBuilder implements InitialContextFactoryBuilder{

	@Override
	public InitialContextFactory createInitialContextFactory(
			Hashtable<?, ?> arg0) throws NamingException {
		return new NamingContextFactory();
	}

}
