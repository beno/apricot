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

package org.eclipse.ecr.web.framework.forms;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.ecr.common.xmap.annotation.XNode;
import org.eclipse.ecr.common.xmap.annotation.XObject;

@XObject
public class RuleDescriptor {

	@XNode("@type")
	public String type;
	
	@XNode("@value")
	public String value;

	@XNode
	public String message = "Validation error!";	
	
	public void writeJson(Writer writer) throws IOException {
		writer.write("{type:");
		writer.write("\"");
		writer.write(type);
		writer.write("\"");
		if (value != null && value.length() > 0) {
			writer.write(",value:");
			if ("min".equals(type) || "max".equals(type)) {
				writer.write(value);
			} else {
				writer.write("\"");
				writer.write(value);
				writer.write("\"");
			}
		}
		writer.append(",message:");
		writer.write("\"");
		writer.write(message);
		writer.write("\"}\n");
	}
	
}
