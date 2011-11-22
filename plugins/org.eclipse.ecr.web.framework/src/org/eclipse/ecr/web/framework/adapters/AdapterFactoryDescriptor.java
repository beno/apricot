/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 */
package org.eclipse.ecr.web.framework.adapters;

import org.eclipse.ecr.web.framework.WebApplication;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
@XObject("factory")
public class AdapterFactoryDescriptor {

    @XNode("@path")
    public String path;

    @XNode("@class")
    public Class<? extends AdapterFactory> clazz;

    @XNode("@app")
    public String app = WebApplication.DEFAULT_APP_NAME;
}
