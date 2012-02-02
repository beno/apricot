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

import org.eclipse.ecr.common.xmap.annotation.XNode;
import org.eclipse.ecr.common.xmap.annotation.XNodeList;
import org.eclipse.ecr.common.xmap.annotation.XObject;

@XObject
public class FieldDescriptor {

	@XNode("@name")
	public String name;

	@XNode("@label")
	public String label;
	
	@XNodeList(value="rule", componentType=RuleDescriptor.class, type=RuleDescriptor[].class)
	protected RuleDescriptor[] rules;
	
}
