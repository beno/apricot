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

package org.eclipse.ecr.web.framework.forms;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.ecr.common.xmap.annotation.XNode;
import org.eclipse.ecr.common.xmap.annotation.XNodeList;
import org.eclipse.ecr.common.xmap.annotation.XObject;

@XObject("form")
public class FormDescriptor {

	@XNode("@id")
	public String id;
	
	@XNodeList(value="field", componentType=FieldDescriptor.class, type=FieldDescriptor[].class)
	public FieldDescriptor[] fields;
	
	public void writeRulesAsJSon(Writer writer) throws IOException {
		writer.write("{\n");
		if (fields.length > 0) {
			writeRulesAsJson(fields[0], writer);
			for (int i=1; i<fields.length; i++) {
				writer.write(",\n");
				writeRulesAsJson(fields[i], writer);
			}
		}
		writer.write("}");
	}
	
	private final void writeRulesAsJson(FieldDescriptor fd, Writer writer) throws IOException {
		writer.write(fd.name);
		writer.write(":[");
		if (fd.rules.length > 0) {
			fd.rules[0].writeJson(writer);
			for (int i=1; i<fd.rules.length; i++) {
				writer.write(",");
				fd.rules[i].writeJson(writer);
			}
		}
		writer.write("]\n");
	}
		
}
