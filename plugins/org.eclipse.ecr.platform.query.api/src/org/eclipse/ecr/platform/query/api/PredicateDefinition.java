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
 */

package org.eclipse.ecr.platform.query.api;

import org.eclipse.ecr.common.xmap.annotation.XNode;

public interface PredicateDefinition {

    String ATOMIC_PREDICATE = "atomic";

    String SUB_CLAUSE_PREDICATE = "subClause";

    @XNode("@operator")
    void setOperator(String operator);

    String getOperator();

    String getParameter();

    void setParameter(String parameter);

    PredicateFieldDefinition[] getValues();

    void setValues(PredicateFieldDefinition[] values);

    String getType();

    String getOperatorField();

    String getOperatorSchema();

}
