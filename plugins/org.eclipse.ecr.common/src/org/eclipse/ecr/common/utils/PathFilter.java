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


/**
 * A path filter.
 * Two types of wildcards are supported:
 * <ul>
 * <li><code>*</code> - match any char from a path segment
 * <li><code>**</code> - match any path segment
 * </ul>
 * @author  <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface PathFilter {

    boolean accept(Path path);

    boolean isExclusive();

}
