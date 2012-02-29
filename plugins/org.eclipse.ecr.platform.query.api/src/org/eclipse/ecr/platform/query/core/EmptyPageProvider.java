/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anahide Tchertchian
 */
package org.eclipse.ecr.platform.query.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.eclipse.ecr.platform.query.api.AbstractPageProvider;

/**
 * @author Anahide Tchertchian
 */
public class EmptyPageProvider<T extends Serializable> extends
        AbstractPageProvider<T> {

    private static final long serialVersionUID = 1L;

    @Override
    public List<T> getCurrentPage() {
        return Collections.emptyList();
    }

}
