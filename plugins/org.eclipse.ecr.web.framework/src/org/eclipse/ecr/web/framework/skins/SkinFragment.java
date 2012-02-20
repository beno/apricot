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
package org.eclipse.ecr.web.framework.skins;

import java.net.URL;

import org.eclipse.ecr.common.xmap.annotation.XNode;
import org.eclipse.ecr.common.xmap.annotation.XObject;
import org.eclipse.ecr.web.framework.WebApplication;
import org.osgi.framework.Bundle;

/**
 * A fragment is a contribution of skins 
 * @author bstefanescu
 *
 */
@XObject("skin")
public class SkinFragment implements Comparable<SkinFragment> {

	/**
	 * Path must not end with a /. If not so the service will remove the trailing /.
	 */
	@XNode("@path")
	protected String path;
	
	@XNode("@order")
	protected int order = 100;
	
    @XNode("@app")
    public String app = WebApplication.DEFAULT_APP_NAME;

	protected Bundle bundle;
	
	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
		if (path.endsWith("/")) {
			path = path.substring(0, path.length()-1);
		}
	}
	
	/**
	 * location must begin with a '/'
	 * @param location
	 * @return
	 */
	public URL resolve(String location) {
		return bundle.getEntry(path+location);
	}

	@Override
	public int hashCode() {
		return bundle.getSymbolicName().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof SkinFragment) {
			return ((SkinFragment)obj).bundle.getSymbolicName().equals(bundle.getSymbolicName());
		}
		return false;
	}
	
	@Override
	public int compareTo(SkinFragment o) {
		return order - o.order;
	}
}
