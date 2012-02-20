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
import org.eclipse.ecr.core.api.ClientException;
import org.eclipse.ecr.core.api.CoreSession;
import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.api.DocumentRef;
import org.eclipse.ecr.core.api.security.ACE;
import org.eclipse.ecr.core.api.security.ACL;
import org.eclipse.ecr.core.api.security.impl.ACLImpl;
import org.eclipse.ecr.core.api.security.impl.ACPImpl;

/**
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
@Operation(id = SetDocumentACE.ID, category = Constants.CAT_DOCUMENT, label = "Set ACL", description = "Set Acces Control Entry on the input document(s). Returns the document(s).")
public class SetDocumentACE {

    public static final String ID = "Document.SetACE";

    @Context
    protected CoreSession session;

    @Param(name = "user")
    protected String user;

    @Param(name = "permission")
    String permission;

    @Param(name = "acl", required = false, values = ACL.LOCAL_ACL)
    String aclName = ACL.LOCAL_ACL;

    @Param(name = "grant", required = false, values = "true")
    boolean grant = true;

    @Param(name = "overwrite", required = false, values = "true")
    boolean overwrite = true;

    @OperationMethod(collector=DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
        setACE(doc.getRef());
        return session.getDocument(doc.getRef());
    }

    @OperationMethod(collector=DocumentModelCollector.class)
    public DocumentModel run(DocumentRef doc) throws Exception {
        setACE(doc);
        return session.getDocument(doc);
    }

    protected void setACE(DocumentRef ref) throws ClientException {
        ACPImpl acp = new ACPImpl();
        ACLImpl acl = new ACLImpl(aclName);
        acp.addACL(acl);
        ACE ace = new ACE(user, permission, grant);
        acl.add(ace);
        session.setACP(ref, acp, false);
    }

}
