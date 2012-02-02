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
 * $Id: ScopeType.java 19046 2007-05-21 13:03:50Z sfermigier $
 */

package org.eclipse.ecr.common.collections;

/**
 * Scope type definitions for a scoped map.
 * <p>
 * Only request and default scopes are defined for now, but others may be added.
 *
 * @see ScopedMap
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public enum ScopeType {
    DEFAULT, REQUEST;

    public String getScopedKey(String key) {
        return getScopePrefix() + key;
    }

    public String getScopePrefix() {
        return name().toLowerCase() + '/';
    }

}
