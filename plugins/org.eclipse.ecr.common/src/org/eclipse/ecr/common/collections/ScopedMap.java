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
 * $Id: ScopedMap.java 20283 2007-06-11 09:45:21Z dmihalache $
 */

package org.eclipse.ecr.common.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Scoped map holding data for a given scope.
 * <p>
 * Used to store context data and invalidate some data given its scope.
 * Implements Map for easier use from interface.
 *
 * @see ScopeType
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public class ScopedMap extends HashMap<String, Serializable> {

    private static final Log log = LogFactory.getLog(ScopedMap.class);

    private static final long serialVersionUID = -616031057117818344L;

    /**
     * Gets value for given scope and given key.
     */
    public Serializable getScopedValue(ScopeType scope, String key) {
        Serializable res = null;
        if (scope != null && key != null) {
            res = get(scope.getScopedKey(key));
        }
        return res;
    }

    /**
     * Gets value for given key using default scope.
     */
    public Serializable getScopedValue(String key) {
        return getScopedValue(ScopeType.DEFAULT, key);
    }

    /**
     * Creates a Map with entries from DEFAULT scope.
     */
    public Map<String, Serializable> getDefaultScopeValues() {
        return getScopeValues(ScopeType.DEFAULT);
    }

    /**
     * Creates a Map with entries from specified scope.
     */
    public Map<String, Serializable> getScopeValues(ScopeType scopeType) {
        Map<String, Serializable> defMap = new HashMap<String, Serializable>();

        final String defaultScopePrefix = scopeType.getScopePrefix();
        final int prefixLen = defaultScopePrefix.length();
        for (Map.Entry<String, Serializable> entry : entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(defaultScopePrefix)) {
                defMap.put(key.substring(prefixLen), entry.getValue());
            }
        }

        return defMap;
    }

    /**
     * Sets value for given scope and given key.
     */
    public void putScopedValue(ScopeType scope, String key,
            Serializable value) {
        if (scope == null || key == null) {
            log.error(String.format(
                    "Cannot set scope value using scopeType=%s and key=%s",
                    scope, key));
        } else {
            put(scope.getScopedKey(key), value);
        }
    }

    /**
     * Sets key using default scope.
     */
    public void putScopedValue(String key, Serializable value) {
        putScopedValue(ScopeType.DEFAULT, key, value);
    }

    /**
     * Removes all mappings for given scope.
     */
    public void clearScope(ScopeType scopeType) {
        if (scopeType == null) {
            log.error("Cannot clear map, specified scope is null");
        } else {
            String prefix = scopeType.getScopePrefix();
            List<String> toRemove = new ArrayList<String>();
            for (Map.Entry<String, Serializable> entry : entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(prefix)) {
                    toRemove.add(key);
                }
            }
            for (String key : toRemove) {
                remove(key);
            }
        }
    }

}
