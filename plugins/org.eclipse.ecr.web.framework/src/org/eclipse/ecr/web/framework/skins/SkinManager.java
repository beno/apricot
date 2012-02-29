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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.ecr.web.framework.skins.i18n.MessageProvider;
import org.eclipse.ecr.web.rendering.api.ResourceLocator;

/**
 * @author bstefanescu
 *
 */
public class SkinManager implements ResourceLocator {

	protected List<SkinFragment> fragments = new ArrayList<SkinFragment>();
	private volatile SkinFragment[] cache;
	private volatile MessageProvider messages;
	
	public synchronized void addSkinFragment(SkinFragment fragment) {
		fragments.add(fragment);
		Collections.sort(fragments);
		cache = null;
		messages = null;
	}

	public synchronized void removeSkinFragment(SkinFragment fragment) {
		fragments.remove(fragment);
		cache = null;
		messages = null;
	}
	
	public SkinFragment[] getFragments() {
		SkinFragment[] _cache = cache;
		if (_cache == null) {
			synchronized (this) {
				if (cache == null) {
					cache = fragments.toArray(new SkinFragment[fragments.size()]);
				}
				_cache = cache;
			}
		}
		return _cache;
	}
	
	public MessageProvider getMessageProvider() {
		MessageProvider mp = messages;
		if (mp == null) {
			synchronized (this) {
				if (messages == null) {
					messages = new MessageProvider(this);
				}
				mp = messages;
			}
		}
		return mp;
	}
	
	public ResourceBundle getResourceBundle(String language) {
		return getMessageProvider().getMessages(language);
	}
	
	public List<URL> resolveAll(String location) {
		if (!location.startsWith("/")) {
			location = "/".concat(location);
		}
		ArrayList<URL> urls = new ArrayList<URL>();
		for (SkinFragment fragment : getFragments()) {
			URL url = fragment.resolve(location);
			if (url != null) {
				urls.add(url);
			}
		}
		return urls;
	}
		
	public URL resolve(String location) {
		if (!location.startsWith("/")) {
			location = "/".concat(location);
		}
		for (SkinFragment fragment : getFragments()) {
			URL url = fragment.resolve(location);
			if (url != null) {
				return url;
			}
		}
		return null;
	}

	@Override
	public File getResourceFile(String path) {
		return null;
	}
	
	@Override
	public URL getResourceURL(String path) {
		if (path.startsWith("skin:")) {
			path = path.substring("skin:".length());
			return resolve(path);
		} else {
            try {
                return new URL(path);
            } catch (MalformedURLException e) {
                return null;
            }
		}
		
	}
}
