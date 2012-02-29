/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefanescu Bogdan
 */
package org.eclipse.ecr.opencmis.impl.server;

import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;

import org.eclipse.ecr.common.utils.IdUtils;
import org.eclipse.ecr.core.api.Blob;
import org.eclipse.ecr.core.api.ClientException;
import org.eclipse.ecr.core.api.CoreSession;
import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.api.pathsegment.PathSegmentService;
import org.eclipse.ecr.runtime.api.Framework;

/**
 * A helper to create a document from a blob to avoid dependencies on
 * filemanager. This is a temporary solution - should find a way to correctly
 * integrate cmis either by integrating filemanager in apricot, either by
 * refactoring cmis bridge.
 * 
 * For now this class is used in NuxeoCmisService to avoid importing
 * filemanager.
 */
public class BlobHelper {

    private static final MimetypesFileTypeMap map = new MimetypesFileTypeMap();

    public static String getMimeTypeFromFilename(String name) {
        return map.getContentType(name);
    }

    public static String fetchTitle(String filename) {
        String title = filename.trim();
        if (title.length() == 0) {
            title = IdUtils.generateStringId();
        }
        return title;
    }

    public static String getFileName(String filename) {
        int i = filename.lastIndexOf('/');
        if (i == -1) {
            i = filename.lastIndexOf('\\');
        }
        if (i == -1) {
            return filename;
        } else {
            return filename.substring(i+1);
        }
    }

    public static DocumentModel createDocumentFromBlob(CoreSession session, Blob blob, String path, String name) throws IOException, ClientException {
        String typeName = "File";
        DocumentModel docModel;
        String filename = getFileName(name);
        String title = fetchTitle(filename);
        blob.setFilename(filename);
        if (blob.getMimeType() == null || "application/octet-stream".equalsIgnoreCase(blob.getMimeType())) {
            blob.setMimeType(getMimeTypeFromFilename(filename));
        }

        PathSegmentService pss;
        try {
            pss = Framework.getService(PathSegmentService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }
        docModel = session.createDocumentModel(typeName);

        // Updating known attributes (title, filename, content)
        docModel.setProperty("dublincore", "title", title);
        docModel.setProperty("file", "filename", filename);
        docModel.setProperty("file", "content", blob);

        // writing the new document to the repository
        docModel.setPathInfo(path, pss.generatePathSegment(docModel));
        docModel = session.createDocument(docModel);

        session.save();

        return docModel;
    }

}
