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

package org.eclipse.ecr.core.search.api.client.querymodel.descriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ecr.common.xmap.annotation.XNode;
import org.eclipse.ecr.common.xmap.annotation.XObject;

@XObject(value = "field")
@Deprecated
public class FacetDescriptor {

    private static final Log log = LogFactory.getLog(FacetDescriptor.class);

    @XNode("@required")
    protected boolean required;

    @XNode("@name")
    protected final String setName() {
        String msg = "Facet post filtering has been replaced by "
                + "the \"ecm:mixinType\" query pseudo-field";
        log.error(msg);
        throw new RuntimeException(msg);
    }

    public String getName() {
        return null;
    }

    public boolean isRequired() {
        return required;
    }

}
