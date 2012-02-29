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

package org.eclipse.ecr.platform.query.core;

import org.eclipse.ecr.common.xmap.annotation.XNode;
import org.eclipse.ecr.common.xmap.annotation.XObject;
import org.eclipse.ecr.platform.query.api.PredicateFieldDefinition;

/**
 * Field descriptor accepting a separate schema and field or a complete xpath.
 *
 * @author Anahide Tchertchian
 * @since 5.4
 */
@XObject(value = "field")
public class FieldDescriptor implements PredicateFieldDefinition {

    @XNode("@name")
    protected String name;

    @XNode("@schema")
    protected String schema;

    @XNode("@xpath")
    protected String xpath;

    public FieldDescriptor() {
    }

    public FieldDescriptor(String schema, String name) {
        this.name = name;
        this.schema = schema;
    }

    public FieldDescriptor(String xpath) {
        this.xpath = xpath;
    }

    public String getName() {
        return name;
    }

    public String getSchema() {
        return schema;
    }

    public String getXpath() {
        return xpath;
    }

}
