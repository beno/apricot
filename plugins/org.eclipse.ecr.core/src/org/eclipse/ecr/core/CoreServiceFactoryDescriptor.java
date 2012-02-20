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
 */

package org.eclipse.ecr.core;

import org.eclipse.ecr.common.xmap.annotation.XNode;
import org.eclipse.ecr.common.xmap.annotation.XObject;

/**
 * Descriptor for Core Service sessionFactory extension point configuration.
 *
 * @author Florent Guillaume
 */
@XObject("factory")
public class CoreServiceFactoryDescriptor {

    @XNode("@class")
    protected String klass;

    public String getKlass() {
        return klass;
    }

}
