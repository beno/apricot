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
 * $Id: JOOoConvertPluginImpl.java 18651 2007-05-13 20:28:53Z sfermigier $
 */

package org.eclipse.ecr.common.utils;

/**
 * Provides utility methods for manipulating and examining
 * exceptions in a generic way.
 *
 * @author DM
 */
public final class ExceptionUtils {

    // This is an utility class.
    private ExceptionUtils() {
    }

    /**
     * Gets the root cause of the given <code>Throwable</code>.
     * <p>
     * This method walks through the exception chain up to the root of the
     * exceptions tree using {@link Throwable#getCause()}, and returns the root
     * exception.
     *
     * @param throwable
     *            the throwable to get the root cause for, may be null - this is
     *            to avoid throwing other un-interesting exception when handling
     *            a business-important exception
     * @return the root cause of the <code>Throwable</code>,
     *         <code>null</code> if none found or null throwable input
     */
    public static Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        if (throwable != null) {
            cause = throwable.getCause();
            while ((throwable = cause.getCause()) != null) {
                cause = throwable;
            }
        }

        return cause;
    }

}
