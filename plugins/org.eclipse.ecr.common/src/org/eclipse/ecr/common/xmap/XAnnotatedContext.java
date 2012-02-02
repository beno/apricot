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

package org.eclipse.ecr.common.xmap;

import org.eclipse.ecr.common.xmap.annotation.XContext;
import org.w3c.dom.Element;

/**
 * @author  <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class XAnnotatedContext extends XAnnotatedMember {

    protected XAnnotatedContext(XMap xmap, XAccessor accessor, XContext anno) {
        super(xmap, accessor);
        path = new Path(anno.value());
    }

    @Override
    protected Object getValue(Context ctx, Element base) throws Exception {
        return ctx.getProperty(path.path);
    }

}
