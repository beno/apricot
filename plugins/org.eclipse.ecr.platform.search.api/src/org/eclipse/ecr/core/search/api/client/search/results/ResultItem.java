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
 * $Id: ResultItem.java 28480 2008-01-04 14:04:49Z sfermigier $
 */

package org.eclipse.ecr.core.search.api.client.search.results;

import java.io.Serializable;
import java.util.Map;

/**
 * Result item.
 * <p>
 * Result items are stored on ResultSet instances.
 *
 * @author <a href="mailto:ja@nuxeo.com">Julien Anguenot</a>
 */
public interface ResultItem extends Map<String, Serializable> {

    /**
     * Return the name of the result item.
     *
     * @return the name of the result item.
     */
    String getName();

}
