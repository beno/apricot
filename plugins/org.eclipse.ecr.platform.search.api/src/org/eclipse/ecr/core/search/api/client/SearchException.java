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
 * $Id: SearchException.java 23183 2007-07-30 17:28:06Z janguenot $
 */

package org.eclipse.ecr.core.search.api.client;

import org.eclipse.ecr.core.api.WrappedException;

/**
 * Searching related exception.
 *
 * @author <a href="mailto:ja@nuxeo.com">Julien Anguenot</a>
 *
 */
public class SearchException extends Exception {

    private static final long serialVersionUID = -961763618434457798L;

    public SearchException() {
    }

    public SearchException(String message) {
        super(message);
    }

    public SearchException(String message, Throwable cause) {
        super(message, WrappedException.wrap(cause));
    }

    public SearchException(Throwable cause) {
        super(WrappedException.wrap(cause));
    }

}
