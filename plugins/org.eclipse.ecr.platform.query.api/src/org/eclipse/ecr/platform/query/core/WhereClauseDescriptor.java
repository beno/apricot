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
 * $Id: JOOoConvertPluginImpl.java 18651 2007-05-13 20:28:53Z sfermigier $
 */

package org.eclipse.ecr.platform.query.core;

import org.eclipse.ecr.common.xmap.annotation.XNode;
import org.eclipse.ecr.common.xmap.annotation.XNodeList;
import org.eclipse.ecr.common.xmap.annotation.XObject;
import org.eclipse.ecr.core.search.api.client.querymodel.Escaper;
import org.eclipse.ecr.platform.query.api.PredicateDefinition;
import org.eclipse.ecr.platform.query.api.WhereClauseDefinition;

/**
 * Generic descriptor for query where clause, accepting predicates and a fixed
 * part. A custom escaper can also be set.
 *
 * @author Anahide Tchertchian
 * @since 5.4
 */
@XObject(value = "whereClause")
public class WhereClauseDescriptor implements WhereClauseDefinition {

    @XNode("@docType")
    protected String docType;

    @XNode("@escaper")
    protected Class<? extends Escaper> escaperClass;

    @XNodeList(value = "predicate", componentType = PredicateDescriptor.class, type = PredicateDefinition[].class)
    protected PredicateDefinition[] predicates;

    protected String fixedPart;

    @XNode("fixedPart@quoteParameters")
    protected boolean quoteFixedPartParameters = true;

    @XNode("fixedPart@escape")
    protected boolean escapeFixedPartParameters = true;

    public String getDocType() {
        return docType;
    }

    @XNode("fixedPart")
    public void setFixedPath(String fixedPart) {
        // remove new lines and following spaces
        this.fixedPart = fixedPart.replaceAll("\r?\n\\s*", " ");
    }

    public boolean getQuoteFixedPartParameters() {
        return quoteFixedPartParameters;
    }

    public boolean getEscapeFixedPartParameters() {
        return escapeFixedPartParameters;
    }

    public PredicateDefinition[] getPredicates() {
        return predicates;
    }

    public void setPredicates(PredicateDefinition[] predicates) {
        this.predicates = predicates;
    }

    public String getFixedPart() {
        return fixedPart;
    }

    public void setFixedPart(String fixedPart) {
        this.fixedPart = fixedPart;
    }

    public Class<? extends Escaper> getEscaperClass() {
        return escaperClass;
    }

}
