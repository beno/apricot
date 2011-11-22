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
 *
 */
package org.eclipse.ecr.web.framework.adapters;

import org.eclipse.ecr.web.framework.AdaptableResource;
import org.eclipse.ecr.web.framework.AdapterResource;


/**
 * @author bstefanescu
 *
 */
public interface AdapterFactory {

    boolean acceptTarget(AdaptableResource target);
    
	AdapterResource newInstance(AdaptableResource targetResource) throws Exception;
	
}
