/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.eclipse.ecr.core.api.blobholder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.ecr.core.api.Blob;
import org.eclipse.ecr.core.api.ClientException;

public class SimpleBlobHolderWithProperties extends SimpleBlobHolder {

    protected final Map<String, Serializable> properties;

    public SimpleBlobHolderWithProperties(Blob blob,
            Map<String, Serializable> properties) {
        super(blob);
        this.properties = properties;
    }

    public SimpleBlobHolderWithProperties(List<Blob> blobs,
            Map<String, Serializable> properties) {
        super(blobs);
        this.properties = properties;
    }

    @Override
    public Serializable getProperty(String name) throws ClientException {
        if (properties == null) {
            return null;
        }
        return properties.get(name);
    }

    @Override
    public Map<String, Serializable> getProperties() {
        return properties;
    }

}
