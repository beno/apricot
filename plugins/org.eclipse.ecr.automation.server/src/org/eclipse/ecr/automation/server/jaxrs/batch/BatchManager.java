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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.ecr.core.api.Blob;

/**
 * Service interface to collect inputs (Blobs) for an operation or operation
 * chain
 *
 * @author Tiry (tdelprat@nuxeo.com)
 * @since 5.4.2
 */
public interface BatchManager {

    /**
     * Add an inputStream in a batch Will create a new {@link Batch} if needed
     *
     * Streams are persisted as temporary files
     *
     * @param batchId
     * @param idx
     * @param is
     * @param name
     * @param mime
     * @throws IOException
     */
    void addStream(String batchId, String idx, InputStream is,
            String name, String mime) throws IOException;

    /**
     * Get Blobs associated to a given batch. Returns null if batch does not
     * exist
     *
     * @param batchId
     * @return
     */
    List<Blob> getBlobs(String batchId);

    Blob getBlob(String batchId, String fileId);

    /**
     * Cleanup the temporary storage associated to the batch
     *
     * @param batchId
     */
    void clean(String batchId);

    /**
     * Initialize a batch with a given batchId and Context Name If batchId is
     * not provided, it will be automatically generated
     *
     * @param batchId
     * @param contextName
     * @return the batchId
     */
    String initBatch(String batchId, String contextName);


}
