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

package org.eclipse.ecr.web.framework.fragments;


/**
 * @author bstefanescu
 *
 */
public interface FragmentManager {

	void addFragment(FragmentDescriptor desc);
	
	void removeFragment(FragmentDescriptor desc);

	/**
	 * Get the fragments contributed to the given 'id'.
	 * Return null if no fragments are found. 
	 * @param id
	 * @return
	 */
	FragmentDescriptor[] getFragments(String id);

}
