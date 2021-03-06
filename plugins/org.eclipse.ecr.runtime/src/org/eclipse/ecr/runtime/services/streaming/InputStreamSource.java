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
 * $Id$
 */

package org.eclipse.ecr.runtime.services.streaming;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.ecr.common.utils.FileUtils;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class InputStreamSource extends AbstractStreamSource {

    protected InputStream in;


    public InputStreamSource(InputStream in) {
        this.in = in;
    }

    @Override
    public InputStream getStream() throws IOException {
        return in;
    }

    @Override
    public void destroy() {
        FileUtils.close(in); // always close the stream
        in = null;
    }

}
