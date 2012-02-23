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

package org.eclipse.ecr.core.api.localconfiguration;

import org.eclipse.ecr.core.api.ClientException;
import org.eclipse.ecr.core.api.CoreSession;

/**
 * Base class for {@link LocalConfiguration} implementers.
 * <p>
 * Provides default implementation for most methods.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.5
 */
public abstract class AbstractLocalConfiguration<T> implements
        LocalConfiguration<T> {

    @Override
    public boolean canMerge() {
        return false;
    }

    @Override
    public T merge(T other) {
        return other;
    }

    @Override
    public void save(CoreSession session) throws ClientException {
        // do nothing
    }

}
