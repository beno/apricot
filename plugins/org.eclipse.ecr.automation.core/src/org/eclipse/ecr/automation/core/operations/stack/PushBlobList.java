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
import org.eclipse.ecr.automation.core.Constants;
import org.eclipse.ecr.automation.core.annotations.Context;
import org.eclipse.ecr.automation.core.annotations.Operation;
import org.eclipse.ecr.automation.core.annotations.OperationMethod;
import org.eclipse.ecr.automation.core.util.BlobList;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
@Operation(id = PushBlobList.ID, category = Constants.CAT_EXECUTION_STACK, label = "Push File List",
        description = "Push the input file list on the context stack. The file list can be restored later as the input using the corrresponding pop operation. Returns the input file list.")
public class PushBlobList {

    public static final String ID = "Blob.PushList";

    @Context
    protected OperationContext ctx;

    @OperationMethod
    public BlobList run(BlobList list) {
        ctx.push(Constants.O_BLOBS, list);
        return list;
    }

}
