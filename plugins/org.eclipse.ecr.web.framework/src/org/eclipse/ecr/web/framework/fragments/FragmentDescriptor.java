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

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.ecr.common.utils.FileUtils;
import org.eclipse.ecr.common.xmap.annotation.XContent;
import org.eclipse.ecr.common.xmap.annotation.XNode;
import org.eclipse.ecr.common.xmap.annotation.XObject;
import org.eclipse.ecr.web.framework.WebApplication;
import org.osgi.framework.Bundle;
import org.w3c.dom.DocumentFragment;

/**
 * @author bstefanescu
 *
 */
@XObject("fragment")
public class FragmentDescriptor implements Comparable<FragmentDescriptor> {

	@XNode("@target")
	public String target;

	@XNode("@class")
	public String clazz;
	
	@XNode("@order")
	public int order = 100;

    @XNode("@app")
    public String app = WebApplication.DEFAULT_APP_NAME;

	@XNode("@disabled")
	public boolean disabled;

	@XContent("configuration")
	public DocumentFragment configuration;

	@XNode(value="enablement", trim=true)
	public String enablement;

	@XContent("content")
	public String content;

	@XNode("content@type")
	public String contentType;
	
	@XNode("content@src")
	public String contentSrc;
	
	private FragmentHandler handler;
	
	/**
	 * @return the handler
	 */
	public FragmentHandler getHandler() {
		return handler;
	}
	
	public boolean isEnabled(Map<String, Object> ctx) {
		try {
			return !disabled && getHandler().isEnabled(ctx);
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}
	
	public void build(Bundle bundle) throws Exception {
		if (contentSrc != null) {
			if (contentType == null || contentType.length() == 0) {
				int p = contentSrc.lastIndexOf('.');
				if (p > -1) {
					contentType = contentSrc.substring(p+1).toLowerCase();
				}
			}
			URL url = bundle.getEntry(contentSrc);
			if (url == null) {
				throw new IllegalArgumentException("fragment 'src' could not be found");
			}
			InputStream in = url.openStream();
			try {
				contentSrc = FileUtils.read(in); 
			} finally {
				in.close();
			}
		}
		// instantiate handler
		if (clazz == null || clazz.length() == 0) {
			handler = new DefaultFragmentHandler();
		} else {
			handler = (FragmentHandler)bundle.loadClass(clazz).newInstance();
		}
		handler.init(this);
	}

	@Override
	public int compareTo(FragmentDescriptor o) {
		return order - o.order;
	}
}
