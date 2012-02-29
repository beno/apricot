/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florent Guillaume
 */

package org.eclipse.ecr.core.search.api.client.search.results.impl;

import java.io.Serializable;
import java.util.HashMap;

import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.search.api.client.search.results.ResultItem;

/*
 * @author Florent Guillaume
 */
public class DocumentModelResultItem extends HashMap<String, Serializable>
        implements ResultItem {

    private static final long serialVersionUID = 1L;

    public final DocumentModel doc;

    public DocumentModelResultItem(DocumentModel doc) {
        this.doc = doc;
    }

    @Override
    public String getName() {
        return doc.getName();
    }

    public DocumentModel getDocumentModel() {
        return doc;
    }

}
