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
package org.eclipse.ecr.platform.audit.api.comment;

import java.util.List;

import org.eclipse.ecr.core.api.CoreSession;
import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.api.DocumentRef;
import org.eclipse.ecr.core.api.IdRef;
import org.eclipse.ecr.core.api.event.DocumentEventTypes;
import org.eclipse.ecr.platform.audit.api.LogEntry;

/**
 * Helper to manage {@link LogEntry} comment processing
 * (code was moved from the Seam bean)
 *
 * @author Tiry (tdelprat@nuxeo.com)
 * @since 5.4.2
 */
public class CommentProcessorHelper {

    protected CoreSession documentManager;

    public CommentProcessorHelper(CoreSession documentManager) {
        this.documentManager = documentManager;
    }

    public void processComments(List<LogEntry> logEntries) {
        if (logEntries == null) {
            return;
        }
        for (LogEntry entry : logEntries) {
            String comment  = getLogComment(entry);
            LinkedDocument linkedDoc = getLogLinkedDocument(entry);
            entry.setPreprocessedComment(new UIAuditComment(comment, linkedDoc));
        }
    }

    public String getLogComment(LogEntry entry) {
        String oldComment = entry.getComment();
        if (oldComment == null) {
            return null;
        }

        String newComment = oldComment;
        boolean targetDocExists = false;
        try {
            String strDocRef = oldComment.split(":")[1];

            DocumentRef docRef = new IdRef(strDocRef);
            targetDocExists = documentManager.exists(docRef);
        } catch (Exception e) {
        }

        if (targetDocExists) {
            String eventId = entry.getEventId();
            // update comment
            if (DocumentEventTypes.DOCUMENT_DUPLICATED.equals(eventId)) {
                newComment = "audit.duplicated_to";
            } else if (DocumentEventTypes.DOCUMENT_CREATED_BY_COPY.equals(eventId)) {
                newComment = "audit.copied_from";
            } else if (DocumentEventTypes.DOCUMENT_MOVED.equals(eventId)) {
                newComment = "audit.moved_from";
            }
        }

        return newComment;
    }

    public LinkedDocument getLogLinkedDocument(LogEntry entry) {
        String oldComment = entry.getComment();
        if (oldComment == null) {
            return null;
        }

        LinkedDocument linkedDoc = null;

        try {
            String repoName = oldComment.split(":")[0];
            String strDocRef = oldComment.split(":")[1];

            DocumentRef docRef = new IdRef(strDocRef);

            // create linked doc, broken by default
            linkedDoc = new LinkedDocument();
            linkedDoc.setDocumentRef(docRef);
            linkedDoc.setRepository(repoName);

            // try to resolve target document
            // XXX multi-repository management
            DocumentModel targetDoc = documentManager.getDocument(docRef);
            if (targetDoc != null) {
                linkedDoc.setDocument(targetDoc);
                linkedDoc.setBrokenDocument(false);
            }
        } catch (Exception e) {
            // not the expected format or broken document
        }

        return linkedDoc;
    }

}
