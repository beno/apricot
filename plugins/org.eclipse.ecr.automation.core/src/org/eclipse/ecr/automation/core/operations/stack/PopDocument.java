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
package org.eclipse.ecr.automation.core.operations.stack;

import org.eclipse.ecr.automation.OperationContext;
import org.eclipse.ecr.automation.OperationException;
import org.eclipse.ecr.automation.core.Constants;
import org.eclipse.ecr.automation.core.annotations.Context;
import org.eclipse.ecr.automation.core.annotations.Operation;
import org.eclipse.ecr.automation.core.annotations.OperationMethod;
import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.api.DocumentRef;

/**
 * Pop a document from the context stack and restore the input from the poped
 * document. If on the top of the stack there is no document an exception is
 * thrown This operation contains dynamic logic so it should be handled in a
 * special way by the UI tools to validate the chain (a Pop operation can
 * succeed only if the last push operation has the same type as the pop)
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
@Operation(id = PopDocument.ID, category = Constants.CAT_EXECUTION_STACK, label = "Pop Document", description = "Restore the last saved input document in the context input stack. This operation must be used only if a PUSH operation was previously made. Return the last <i>pushed</i> document.")
public class PopDocument {

    public static final String ID = "Document.Pop";

    @Context
    protected OperationContext ctx;

    @OperationMethod
    public DocumentModel run() throws Exception {
        Object obj = ctx.pop(Constants.O_DOCUMENT);
        if (obj instanceof DocumentModel) {
            return (DocumentModel) obj;
        } else if (obj instanceof DocumentRef) {
            return ctx.getCoreSession().getDocument((DocumentRef) obj);
        }
        throw new OperationException(
                "Illegal state error for pop document operation. The context stack doesn't contains a document on its top");
    }

}
