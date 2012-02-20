/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florent Guillaume
 */
package org.eclipse.ecr.core.storage.sql;

public class TestSQLRepositoryLockingNoTX extends TestSQLRepositoryLocking {

    @Override
    protected boolean useTX() {
        return false;
    }

    @Override
    public void testLockingWithMultipleThreads() throws Exception {
        // TODO XXX check what's happening, it never returns
    }

}
