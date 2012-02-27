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

package org.eclipse.ecr.platform.query.api;

import org.eclipse.ecr.core.search.api.client.querymodel.Escaper;

public interface WhereClauseDefinition {

    String getDocType();

    void setFixedPath(String fixedPart);

    boolean getQuoteFixedPartParameters();

    boolean getEscapeFixedPartParameters();

    PredicateDefinition[] getPredicates();

    void setPredicates(PredicateDefinition[] predicates);

    String getFixedPart();

    void setFixedPart(String fixedPart);

    Class<? extends Escaper> getEscaperClass();

}
