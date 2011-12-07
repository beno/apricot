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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * @author bstefanescu
 *
 */
public class HTMLWriter extends FilterWriter {

	public HTMLWriter(Writer out) {
		super(out);
	}

	public final HTMLWriter print(String s) throws IOException {
		write(s);
		return this;
	}

	public final HTMLWriter println(String s) throws IOException {
		write(s);
		write("\n");
		return this;		
	}

	public final HTMLWriter println() throws IOException {
		write("\n");
		return this;		
	}

	public final HTMLWriter attr(String key, String value) throws IOException {
		write(" ");
		write(key);
		write("=\"");
		write(value);
		write("\"");
		return this;		
	}

	public final HTMLWriter end(String tag) throws IOException {
		write("</"); write(tag); write(">\n");
		return this;
	}

	public final HTMLWriter end() throws IOException {
		write("/>\n");
		return this;
	}

	public final HTMLWriter etag() throws IOException {
		write(">\n");
		return this;
	}

	public final HTMLWriter start(String tag) throws IOException {
		write("<"); write(tag);
		return this;
	}

	public final HTMLWriter attrs(Map<String, Object> map) throws IOException {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			attr(entry.getKey(), entry.getValue().toString());
		}
		return this;
	}

}
