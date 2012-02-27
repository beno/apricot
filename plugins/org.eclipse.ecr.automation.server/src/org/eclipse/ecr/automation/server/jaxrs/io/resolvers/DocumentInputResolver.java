/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     matic
 */
package org.eclipse.ecr.automation.server.jaxrs.io.resolvers;

import org.eclipse.ecr.automation.server.jaxrs.io.InputResolver;
import org.eclipse.ecr.core.api.DocumentRef;
import org.eclipse.ecr.core.api.IdRef;
import org.eclipse.ecr.core.api.PathRef;

/**
 * @author matic
 *
 */
public class DocumentInputResolver implements InputResolver<DocumentRef> {

    @Override
    public String getType() {
       return "doc";
    }

    @Override
    public DocumentRef getInput(String content) {
        return docRefFromString(content);
    }

     public static DocumentRef docRefFromString(String input) {
        if (input.startsWith("/")) {
            return new PathRef(input);
        } else {
            return new IdRef(input);
        }
    }

}
