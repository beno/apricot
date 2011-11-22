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

package org.eclipse.ecr.web.framework.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bstefanescu
 *
 */
public class FragmentManagerImpl implements FragmentManager {

	protected Map<String, List<FragmentDescriptor>> registry;
	protected volatile Map<String, FragmentDescriptor[]> cache;

	public FragmentManagerImpl() {
		registry = new HashMap<String, List<FragmentDescriptor>>();		
	}
	
	public Map<String, List<FragmentDescriptor>> getRegistry() {
		return registry;
	}
	
	@Override
	public synchronized void addFragment(FragmentDescriptor desc) {
		List<FragmentDescriptor> fragments = registry.get(desc.target);
		if (fragments == null) {
			fragments = new ArrayList<FragmentDescriptor>();
			registry.put(desc.target, fragments);
		}
		fragments.add(desc);
		Collections.sort(fragments);
		cache = null;
	}

	@Override
	public synchronized void removeFragment(FragmentDescriptor desc) {
		List<FragmentDescriptor> fragments = registry.get(desc.target);
		if (fragments != null) {
			fragments.remove(desc);
		}
		cache = null;
	}
	
	@Override
	public FragmentDescriptor[] getFragments(String id) {
		Map<String, FragmentDescriptor[]> _cache = cache;
		if (_cache == null) {
			synchronized (this) {
				if (cache == null) {
					cache = new HashMap<String, FragmentDescriptor[]>();
					for (Map.Entry<String, List<FragmentDescriptor>> entry : registry.entrySet()) {
						List<FragmentDescriptor> v = entry.getValue();
						cache.put(entry.getKey(), v.toArray(new FragmentDescriptor[v.size()]));
					}
				}
				_cache = cache;
			}
		}
		return _cache.get(id);
	}

}
