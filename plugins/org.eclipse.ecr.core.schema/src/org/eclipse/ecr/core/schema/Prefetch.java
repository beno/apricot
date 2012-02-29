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
package org.eclipse.ecr.core.schema;

import static org.apache.commons.lang.ObjectUtils.NULL;
import static org.eclipse.ecr.core.schema.types.ComplexTypeImpl.canonicalXPath;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.ecr.core.schema.types.Schema;
import org.eclipse.ecr.runtime.api.Framework;

/**
 * Information about what's to be prefetched: individual properties and whole
 * schemas.
 */
public class Prefetch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * map of prefix:name -> value
     * <p>
     * key can be a canonical xpath like prefix:name/0/othername
     * <p>
     * null values are stored as actual nulls
     */
    public Map<String, Serializable> values;

    /**
     * map of schema -> list of prefix:name
     */
    public Map<String, List<String>> keysBySchema;

    /**
     * map of schema -> name -> prefix:name
     */
    public Map<String, Map<String, String>> keysBySchemaAndName;

    public Prefetch() {
        values = new HashMap<String, Serializable>();
        keysBySchema = new HashMap<String, List<String>>();
        keysBySchemaAndName = new HashMap<String, Map<String, String>>();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void put(String prefixedName, String schemaName, String name,
            Serializable value) {
        values.put(prefixedName, value);
        if (schemaName != null) {
            Map<String, String> keysByName = keysBySchemaAndName.get(schemaName);
            if (keysByName == null) {
                keysBySchemaAndName.put(schemaName,
                        keysByName = new HashMap<String, String>());
            }
            keysByName.put(name, prefixedName);
            List<String> keys = keysBySchema.get(schemaName);
            if (keys == null) {
                keysBySchema.put(schemaName, keys = new LinkedList<String>());
            }
            keys.add(prefixedName);
        }
    }

    public Serializable get(String xpath) {
        xpath = canonicalXPath(xpath);
        if (values.containsKey(xpath)) {
            return values.get(xpath);
        }
        return NULL;
    }

    public Serializable get(String schemaName, String name) {
        Map<String, String> keysByName = keysBySchemaAndName.get(schemaName);
        if (keysByName != null) {
            String prefixedName = keysByName.get(name);
            if (prefixedName != null && values.containsKey(prefixedName)) {
                return values.get(prefixedName);
            }
        }
        return NULL;
    }

    public boolean isPrefetched(String xpath) {
        xpath = canonicalXPath(xpath);
        return values.containsKey(xpath);
    }

    public boolean isPrefetched(String schemaName, String name) {
        Map<String, String> keysByName = keysBySchemaAndName.get(schemaName);
        if (keysByName == null) {
            return false;
        }
        String prefixedName = keysByName.get(name);
        if (prefixedName == null) {
            return false;
        }
        return values.containsKey(prefixedName);
    }

    /**
     * Clears the prefetches for a given schema.
     */
    public void clearPrefetch(String schemaName) {
        keysBySchemaAndName.remove(schemaName);
        List<String> keys = keysBySchema.remove(schemaName);
        if (keys != null) {
            for (String prefixedName : keys) {
                values.remove(prefixedName);
            }
        }
    }

    /**
     * Gets the schema name for a given xpath.
     * <p>
     * The type is used to resolve non-prefixed properties.
     *
     * @return the schema name or {@code null}
     */
    public String getXPathSchema(String xpath, DocumentType type) {
        xpath = canonicalXPath(xpath);
        int i = xpath.indexOf('/');
        String prop = i == -1 ? xpath : xpath.substring(0, i);
        int p = prop.indexOf(':');
        if (p == -1) {
            for (Schema schema : type.getSchemas()) {
                if (schema.hasField(prop)) {
                    return schema.getName();
                }
            }
            return null;
        } else {
            String prefix = prop.substring(0, p);
            SchemaManager schemaManager = Framework.getLocalService(SchemaManager.class);
            Schema schema = schemaManager.getSchemaFromPrefix(prefix);
            if (schema == null) {
                schema = schemaManager.getSchema(prefix);
            }
            return schema == null ? null : schema.getName();
        }
    }

}
