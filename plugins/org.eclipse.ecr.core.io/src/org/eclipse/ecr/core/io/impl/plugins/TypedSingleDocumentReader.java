/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ataillefer
 */

package org.eclipse.ecr.core.io.impl.plugins;

import java.io.IOException;

import org.eclipse.ecr.core.api.ClientException;
import org.eclipse.ecr.core.api.CoreSession;
import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.api.DocumentRef;
import org.eclipse.ecr.core.io.ExportedDocument;
import org.eclipse.ecr.core.io.impl.TypedExportedDocumentImpl;

/**
 * @author <a href="mailto:ataillefer@nuxeo.com">Antoine Taillefer</a>
 * @since 5.6
 */
public class TypedSingleDocumentReader extends SingleDocumentReader {

    public TypedSingleDocumentReader(CoreSession session, DocumentModel root) {
        super(session, root);
    }

    public TypedSingleDocumentReader(CoreSession session, DocumentRef root)
            throws ClientException {
        this(session, session.getDocument(root));
    }

    @Override
    public ExportedDocument read() throws IOException {
        if (doc != null) {
            if (readDone && !enableRepeatedReads) {
                return null;
            } else {
                readDone = true;
                return new TypedExportedDocumentImpl(doc);
            }
        }
        doc = null;
        return null;
    }

}
