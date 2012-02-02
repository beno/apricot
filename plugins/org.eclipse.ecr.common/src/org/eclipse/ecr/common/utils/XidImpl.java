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
 *     Sun Seng David TAN
 */

package org.eclipse.ecr.common.utils;

import java.io.Serializable;
import java.util.Arrays;

import javax.transaction.xa.Xid;

/**
 * A Serializable {@link Xid} independent of the transaction manager
 * implementation.
 */
public class XidImpl implements Xid, Serializable {

    private static final long serialVersionUID = 1L;

    public final int fid;

    public final byte[] gtrid;

    public final byte[] bqual;

    /**
     * Copy constructor.
     *
     * @param xid the xid to copy
     */
    public XidImpl(Xid xid) {
        fid = xid.getFormatId();
        gtrid = xid.getGlobalTransactionId().clone();
        bqual = xid.getBranchQualifier().clone();
    }

    /**
     * Constructor mostly used in unit tests.
     *
     * @param id the global transaction id
     */
    public XidImpl(String id) {
        fid = 0;
        gtrid = id.getBytes();
        // MySQL JDBC driver needs a non 0-length branch qualifier
        bqual = new byte[] { 0 };
    }

    public int getFormatId() {
        return fid;
    }

    public byte[] getGlobalTransactionId() {
        return gtrid.clone();
    }

    public byte[] getBranchQualifier() {
        return bqual.clone();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hash = 1;
        hash = prime * hash + fid;
        hash = prime * hash + Arrays.hashCode(gtrid);
        hash = prime * hash + Arrays.hashCode(bqual);
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof XidImpl)) {
            return false;
        }
        return equals((XidImpl) other);
    }

    private boolean equals(XidImpl other) {
        if (other == this) {
            return true;
        }
        return fid == other.fid && Arrays.equals(gtrid, other.gtrid)
                && Arrays.equals(bqual, other.bqual);
    }

}
