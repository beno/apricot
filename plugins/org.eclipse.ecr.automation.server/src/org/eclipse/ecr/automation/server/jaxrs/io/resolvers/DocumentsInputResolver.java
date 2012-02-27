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

import org.eclipse.ecr.common.utils.StringUtils;
import org.eclipse.ecr.automation.server.jaxrs.io.InputResolver;
import org.eclipse.ecr.core.api.DocumentRefList;
import org.eclipse.ecr.core.api.impl.DocumentRefListImpl;

/**
 * @author matic
 *
 */
public class DocumentsInputResolver implements InputResolver<DocumentRefList> {

    @Override
    public String getType() {
        return "docs";
    }

    @Override
    public DocumentRefList getInput(String input) {
        String[] ar = StringUtils.split(input, ',', true);
        DocumentRefList list = new DocumentRefListImpl(ar.length);
        for (String s : ar) {
            list.add(DocumentInputResolver.docRefFromString(s));
        }
        return list;
    }



}
