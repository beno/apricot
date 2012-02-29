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

import org.eclipse.ecr.common.xmap.annotation.XNode;
import org.eclipse.ecr.common.xmap.annotation.XObject;
import org.eclipse.ecr.core.api.SortInfo;

/**
 * Descriptor for sort info declaration.
 *
 * @author Anahide Tchertchian
 */
@XObject("sort")
public class SortInfoDescriptor {

    @XNode("@column")
    String column;

    @XNode("@ascending")
    boolean ascending = true;

    public String getColumn() {
        return column;
    }

    public boolean isAscending() {
        return ascending;
    }

    public SortInfo getSortInfo() {
        return new SortInfo(column, ascending);
    }

}
