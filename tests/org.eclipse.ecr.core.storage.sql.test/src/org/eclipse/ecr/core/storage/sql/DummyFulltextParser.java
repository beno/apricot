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

import java.util.Set;

/**
 * Dummy fulltext parser for tests that adds suffix "yeah" to all the indexed
 * words, in addition to normal indexing.
 */
public class DummyFulltextParser extends FulltextParser {

    protected static final String SUFFIX = "yeah";

    protected static Set<String> collected;

    @Override
    public void parse(String s, String path) {
        collected.add(path + "=" + s);
        int i1 = strings.size();
        super.parse(s, path);
        int i2 = strings.size();
        for (int i = i1; i < i2; i++) {
            String v = strings.get(i);
            strings.add(v + SUFFIX);
        }
    }

}
