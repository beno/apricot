/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florent Guillaume
 */
package org.eclipse.ecr.opencmis.impl.client;

import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.eclipse.ecr.core.api.ClientException;
import org.eclipse.ecr.core.api.CoreSession;
import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.api.IdRef;
import org.eclipse.ecr.opencmis.impl.server.NuxeoObjectData;

/**
 * Base abstract fileable live local CMIS Object.
 */
public abstract class NuxeoFileableObject extends NuxeoObject implements
        FileableCmisObject {

    public NuxeoFileableObject(NuxeoSession session, NuxeoObjectData data,
            ObjectType type) {
        super(session, data, type);
    }

    @Override
    public List<Folder> getParents() {
        try {
            CoreSession coreSession = data.doc.getCoreSession();
            DocumentModel parent = coreSession.getParentDocument(new IdRef(
                    getId()));
            if (parent == null || service.isFilteredOut(parent)) {
                return Collections.emptyList();
            }
            Folder folder = (Folder) session.getObject(parent,
                    session.getDefaultContext());
            return Collections.singletonList(folder);
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public List<String> getPaths() {
        return Collections.singletonList(data.doc.getPathAsString());
    }

    @Override
    public void addToFolder(ObjectId folderId, boolean allVersions) {
        throw new UnsupportedOperationException("Multi-filing not supported");
    }

    @Override
    public void removeFromFolder(ObjectId folderId) {
        service.removeObjectFromFolder(getRepositoryId(), getId(),
                folderId == null ? null : folderId.getId(), null);
    }

    @Override
    public NuxeoFileableObject move(ObjectId sourceFolder, ObjectId targetFolder) {
        Holder<String> objectIdHolder = new Holder<String>(getId());
        if (sourceFolder == null) {
            throw new CmisInvalidArgumentException("Missing source folder");
        }
        if (targetFolder == null) {
            throw new CmisInvalidArgumentException("Missing target folder");
        }
        service.moveObject(getRepositoryId(), objectIdHolder,
                targetFolder.getId(), sourceFolder.getId(), null);
        return this;
    }

}
