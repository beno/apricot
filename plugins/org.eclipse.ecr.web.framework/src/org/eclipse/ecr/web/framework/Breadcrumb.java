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
package org.eclipse.ecr.web.framework;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bstefanescu
 *
 */
public class Breadcrumb {

	public static class Entry {
		protected String ref;
		protected String label;

		public Entry(String ref, String label) {
			this.ref = ref;
			this.label = label;
		}
		
		public String getRef() {
			return ref;
		}
		
		public String getLabel() {
			return label;
		}
	}

	protected Entry active;
	
	protected List<Entry> path;
	
	public Breadcrumb() {
		path = new ArrayList<Breadcrumb.Entry>();
	}
	
	public boolean isEmpty() {
		return active == null;
	}
	
	public boolean isPathEmpty() {
		return path.isEmpty();
	}
	
	public List<Entry> getPath() {
		return path;
	}
	
	public void add(String ref, String label) {
		if (active != null) {
			path.add(active);
		}
		active = new Entry(ref, label);
	}
	
	public Entry getActive() {
		return active;
	}
}
