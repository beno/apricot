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
 */
package org.eclipse.ecr.automation.server.jaxrs.batch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.ecr.common.utils.FileUtils;
import org.eclipse.ecr.common.utils.Path;
import org.eclipse.ecr.core.api.Blob;
import org.eclipse.ecr.core.api.impl.blob.FileBlob;

/**
 *
 * Batch Object to encapsulate all data related to a batch, especially the
 * temporary files used for Blobs
 *
 * @author Tiry (tdelprat@nuxeo.com)
 * @since 5.4.2
 */
public class Batch {

    protected Map<String, Blob> uploadedBlob = new ConcurrentHashMap<String, Blob>();

    protected final String id;

    protected final String baseDir;

    public Batch(String id) {
        this.id = id;
        baseDir = new Path(System.getProperty("java.io.tmpdir")).append(id).toString();
        new File(baseDir).mkdirs();
    }

    public void addBlob(String idx, Blob blob) {
        uploadedBlob.put(idx, blob);
    }

    public void addStream(String idx, InputStream is, String name, String mime)
            throws IOException {

        File tmp = new File(new Path(baseDir).append(name).toString());
        FileUtils.copyToFile(is, tmp);
        FileBlob blob = new FileBlob(tmp);
        if (mime != null) {
            blob.setMimeType(mime);
        } else {
            blob.setMimeType("application/octetstream");
        }
        blob.setFilename(name);
        addBlob(idx, blob);
    }

    /**
     * Return the uploaded blobs in the order the user choose to upload them
     *
     * @return
     */
    public List<Blob> getBlobs() {

        List<Blob> blobs = new ArrayList<Blob>();

        List<String> sortedIdx = new ArrayList<String>(uploadedBlob.keySet());
        Collections.sort(sortedIdx);

        for (String k : sortedIdx) {
            blobs.add(uploadedBlob.get(k));
        }
        return blobs;
    }

    public Blob getBlob(String fileId) {
        return uploadedBlob.get(fileId);
    }

    public void clear() {
        uploadedBlob.clear();
        FileUtils.deleteTree(new File(baseDir));
    }

}
