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

package org.eclipse.ecr.core.search.api.client.querymodel;

/**
 * An interface for special character escaping in queries
 * for easy configuration of this matter.
 * <p>
 * This is meant for the contents of string literals in where clauses, once they
 * have been extracted. It's therefore not necessary to escape single quotes,
 * unless of course they have some meaning to the search backend.
 * </p>
 *
 * @author <a href="mailto:gracinet@nuxeo.com">Georges Racinet</a>
 *
 */
public interface Escaper {

    /**
     * Escapes the provided string
     * @param s
     * @return the escaped string
     */
    String escape(String s);
}
