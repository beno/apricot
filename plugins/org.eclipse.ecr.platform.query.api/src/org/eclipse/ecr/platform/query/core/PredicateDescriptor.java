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
import org.eclipse.ecr.common.xmap.annotation.XNodeList;
import org.eclipse.ecr.common.xmap.annotation.XObject;
import org.eclipse.ecr.platform.query.api.PredicateDefinition;
import org.eclipse.ecr.platform.query.api.PredicateFieldDefinition;

/**
 * Predicate descriptor accepting a schema and field, an operator, and a
 * parameter.
 *
 * @author Anahide Tchertchian
 * @since 5.4
 */
@XObject(value = "predicate")
public class PredicateDescriptor implements PredicateDefinition {

    @XNode("@parameter")
    protected String parameter;

    @XNode("@type")
    protected String type = ATOMIC_PREDICATE;

    protected String operator;

    @XNode("@operatorField")
    protected String operatorField;

    @XNode("@operatorSchema")
    protected String operatorSchema;

    @XNodeList(value = "field", componentType = FieldDescriptor.class, type = PredicateFieldDefinition[].class)
    protected PredicateFieldDefinition[] values;

    @XNode("@operator")
    public void setOperator(String operator) {
        this.operator = operator.toUpperCase();
    }

    public String getOperator() {
        return operator;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public PredicateFieldDefinition[] getValues() {
        return values;
    }

    public void setValues(PredicateFieldDefinition[] values) {
        this.values = values;
    }

    public String getType() {
        return type;
    }

    public String getOperatorField() {
        return operatorField;
    }

    public String getOperatorSchema() {
        return operatorSchema;
    }

}
