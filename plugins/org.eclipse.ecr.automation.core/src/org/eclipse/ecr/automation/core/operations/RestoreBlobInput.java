/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 */
package org.eclipse.ecr.automation.core.operations;

import org.eclipse.ecr.automation.OperationContext;
import org.eclipse.ecr.automation.OperationException;
import org.eclipse.ecr.automation.core.Constants;
import org.eclipse.ecr.automation.core.annotations.Context;
import org.eclipse.ecr.automation.core.annotations.Operation;
import org.eclipse.ecr.automation.core.annotations.OperationMethod;
import org.eclipse.ecr.automation.core.annotations.Param;
import org.eclipse.ecr.core.api.Blob;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
@Operation(id = RestoreBlobInput.ID, category = Constants.CAT_EXECUTION, label = "Restore File Input", description = "Restore the file input from a context variable given its name. Return the file.")
public class RestoreBlobInput {

    public static final String ID = "Context.RestoreBlobInput";

    @Context
    protected OperationContext ctx;

    @Param(name = "name")
    protected String name;

    @OperationMethod
    public Blob run() throws Exception {
        Object obj = ctx.get(name);
        if (obj instanceof Blob) {
            return (Blob) obj;
        }
        throw new OperationException(
                "Illegal state error for restore file operation. The context map doesn't contains a file variable with name "
                        + name);
    }

}
