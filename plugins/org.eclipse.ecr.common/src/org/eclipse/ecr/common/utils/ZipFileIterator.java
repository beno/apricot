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
 *
 * $Id$
 */

package org.eclipse.ecr.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * An iterator over the entries in a zip file.
 * <p>
 * The iterator support filtering using {@link ZipEntryFilter}
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class ZipFileIterator implements Iterator<ZipEntry> {

    private static final Log log = LogFactory.getLog(ZipFileIterator.class);

    private final ZipFile zip;
    private final ZipEntryFilter filter;
    private final Enumeration<? extends ZipEntry> entries;
    // the current entry
    private ZipEntry zentry;


    public ZipFileIterator(ZipFile zip, ZipEntryFilter filter) {
        this.zip = zip;
        this.filter = filter;
        entries = zip.entries();
        initNextEntry();
    }

    public ZipFileIterator(File file) throws IOException {
        this(new ZipFile(file), null);
    }

    public ZipFileIterator(File file, ZipEntryFilter filter) throws IOException {
        this(new ZipFile(file), filter);
    }

    public ZipFileIterator(ZipFile zip) {
        this(zip, null);
    }


    public boolean hasNext() {
        return zentry != null;
    }

    public ZipEntry next() {
        if (zentry == null) {
            throw new NoSuchElementException("There no more elements to iterate over");
        }
        ZipEntry ze = zentry; // the current entry to return
        initNextEntry();
        return ze;
    }

    private void initNextEntry() {
        // get next entry
        if (entries.hasMoreElements()) {
            zentry = entries.nextElement();
        } else {
            zentry = null;
            return;
        }
        // do filtering if needed
        if (filter != null) {
            while (!filter.accept(zentry.getName())) {
                if (entries.hasMoreElements()) {
                    zentry = entries.nextElement();
                } else {
                    zentry = null;
                    break;
                }
            }
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("remove is not supported by this iterator");
    }

    public ZipFile getZipFile() {
        return zip;
    }

    public void close() {
        if (zip != null) {
            try {
                zip.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

}
