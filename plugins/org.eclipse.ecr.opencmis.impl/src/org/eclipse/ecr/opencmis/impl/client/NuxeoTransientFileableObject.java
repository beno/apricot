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

import java.util.List;

import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.TransientFileableCmisObject;

/**
 * Transient CMIS fileable object for Nuxeo.
 */
public class NuxeoTransientFileableObject extends NuxeoTransientObject
        implements TransientFileableCmisObject {

    public NuxeoTransientFileableObject(NuxeoObject object) {
        super(object);
    }

    @Override
    public FileableCmisObject move(ObjectId sourceFolderId,
            ObjectId targetFolderId) {
        return ((NuxeoFileableObject) object).move(sourceFolderId,
                targetFolderId);
    }

    @Override
    public List<Folder> getParents() {
        return ((NuxeoFileableObject) object).getParents();
    }

    @Override
    public List<String> getPaths() {
        return ((NuxeoFileableObject) object).getPaths();
    }

    @Override
    public void addToFolder(ObjectId folderId, boolean allVersions) {
        ((NuxeoFileableObject) object).addToFolder(folderId, allVersions);
    }

    @Override
    public void removeFromFolder(ObjectId folderId) {
        ((NuxeoFileableObject) object).removeFromFolder(folderId);
    }
}
