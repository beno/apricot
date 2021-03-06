/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 */
package org.eclipse.ecr.automation.core.operations.document;

import org.eclipse.ecr.automation.core.Constants;
import org.eclipse.ecr.automation.core.annotations.Context;
import org.eclipse.ecr.automation.core.annotations.Operation;
import org.eclipse.ecr.automation.core.annotations.OperationMethod;
import org.eclipse.ecr.automation.core.annotations.Param;
import org.eclipse.ecr.automation.core.collectors.DocumentModelCollector;
import org.eclipse.ecr.core.api.CoreSession;
import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.api.DocumentRef;

/**
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
@Operation(id = GetDocumentParent.ID, category = Constants.CAT_DOCUMENT, label = "Get Parent", description = "Get the parent document of the input document. The parent document will become the input for the next operation. You can use the 'type' parameter to specify which parent to select from the document ancestors")
public class GetDocumentParent {

    public static final String ID = "Document.GetParent";

    @Context
    protected CoreSession session;

    @Param(name = "type", required = false)
    protected String type;

    @OperationMethod(collector=DocumentModelCollector.class)
    public DocumentModel run(DocumentRef doc) throws Exception {
        if (type == null) {
            return session.getParentDocument(doc);
        }
        type = type.trim();
        if (type.length() == 0) {
            return session.getParentDocument(doc);
        }
        DocumentModel parent = session.getParentDocument(doc);
        while (parent != null && !type.equals(parent.getType())) {
            parent = session.getParentDocument(parent.getRef());
        }
        return parent;
    }

}
