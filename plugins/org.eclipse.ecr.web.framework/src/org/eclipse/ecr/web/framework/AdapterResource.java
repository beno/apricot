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
 */

package org.eclipse.ecr.web.framework;

import java.util.Map;

/**
 * @author bstefanescu
 *
 */
public abstract class AdapterResource extends AbstractResource {

	protected AdaptableResource target;
	
	public AdapterResource(AdaptableResource target) {
		super (target.getContext());
		this.target = target;
	}
	
	public AdaptableResource getTarget() {
		return target;
	}

	@Override
	public void initBindings(Map<String, Object> args) {
		super.initBindings(args);
		args.put("Adapter", this);
		args.put("This", target);
	}
}
