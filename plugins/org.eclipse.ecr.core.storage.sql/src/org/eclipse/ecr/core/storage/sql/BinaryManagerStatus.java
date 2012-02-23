/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florent Guillaume
 */
package org.eclipse.ecr.core.storage.sql;

/**
 * Status of a BinaryManager, including files that may have just been deleted by
 * GC
 */
public class BinaryManagerStatus {

    public long gcDuration;

    public long numBinaries;

    public long sizeBinaries;

    public long numBinariesGC;

    public long sizeBinariesGC;

    /**
     * The GC duration, in milliseconds
     */
    public long getGCDuration() {
        return gcDuration;
    }

    /**
     * The number of binaries.
     */
    public long getNumBinaries() {
        return numBinaries;
    }

    /**
     * The cumulated size of the binaries.
     */
    public long getSizeBinaries() {
        return sizeBinaries;
    }

    /**
     * The number of garbage collected binaries.
     */
    public long getNumBinariesGC() {
        return numBinariesGC;
    }

    /**
     * The cumulated size of the garbage collected binaries.
     */
    public long getSizeBinariesGC() {
        return sizeBinariesGC;
    }

}
