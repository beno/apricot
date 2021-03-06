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

package org.eclipse.ecr.core.api.blobholder;

import org.eclipse.ecr.core.api.DocumentModel;

/**
 * Interface for the contributed factory classes.
 *
 * @author tiry
 */
public interface BlobHolderFactory {

    BlobHolder getBlobHolder(DocumentModel doc);

}
