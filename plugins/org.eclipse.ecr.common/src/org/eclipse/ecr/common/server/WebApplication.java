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

package org.eclipse.ecr.common.server;

import org.eclipse.ecr.common.xmap.annotation.XNode;
import org.eclipse.ecr.common.xmap.annotation.XObject;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@XObject("webapp")
public class WebApplication {

    @XNode("root")
    protected String root;

    @XNode("webXml")
    protected String webXml;

    @XNode("@name")
    protected String name;

    @XNode("@path")
    protected String path;

    @XNode("@warPreprocessing")
    protected boolean warPreprocessing=false;

    public String getWebRoot() {
        return root;
    }

    public String getConfigurationFile() {
        return webXml;
    }

    public String getName() {
        return name;
    }

    public String getContextPath() {
        return path;
    }

    public boolean needsWarPreprocessing() {
        return warPreprocessing;
    }

}
