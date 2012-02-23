/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.eclipse.ecr.core.api.quota;

/**
 * Adapter giving statistics about a given
 * {@link org.eclipse.ecr.core.api.DocumentModel}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public interface QuotaStats {

    /**
     * Returns the intrinsic cardinal value of the underlying document.
     */
    long getIntrinsic();

    /**
     * Returns the cardinal value of all the children of the underlying
     * document.
     */
    long getChildren();

    /**
     * Returns the cardinal value of all the descendants of the underlying
     * document. plus the value of {@link #getIntrinsic()}.
     */
    long getTotal();

}
