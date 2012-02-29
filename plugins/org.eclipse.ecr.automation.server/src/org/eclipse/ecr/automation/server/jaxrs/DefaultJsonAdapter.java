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
 */
package org.eclipse.ecr.automation.server.jaxrs;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.ecr.automation.server.jaxrs.io.ObjectCodecService;
import org.eclipse.ecr.runtime.api.Framework;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class DefaultJsonAdapter implements JsonAdapter {

    protected Object object;

    public DefaultJsonAdapter(Object object) {
        this.object = object;
    }

    @Override
    public void toJSON(OutputStream out) throws IOException {
        ObjectCodecService service = Framework.getLocalService(ObjectCodecService.class);
        service.write(out, object);
    }

}
