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

import java.util.ArrayList;

/**
 * @author  <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class PathFilterSet extends ArrayList<PathFilter> implements PathFilter {

    private static final long serialVersionUID = -2967683005810353014L;

    private boolean isExclusive = true;

    public PathFilterSet() {

    }

    public PathFilterSet(boolean isExclusive) {
        this.isExclusive = isExclusive;
    }

    public boolean isExclusive() {
        return !isExclusive;
    }

    public boolean accept(Path path) {
        int inclusive = 0;
        boolean defaultValue = false;
        for (PathFilter filter : this) {
            boolean ret = filter.accept(path);
            if (ret) {
                if (!filter.isExclusive()) {
                    inclusive++;
                    defaultValue = true;
                }
            } else {
                if (filter.isExclusive()) {
                    return false;
                } else {
                    inclusive++;
                }
            }
        }
        return inclusive == 0 || defaultValue;
    }

}
