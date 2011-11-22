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

package org.eclipse.ecr.web.framework.fragments.mvel;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.ecr.web.framework.fragments.Condition;
import org.mvel2.MVEL;

/**
 * @author bstefanescu
 *
 */
public class MvelCondition implements Condition {

	private String expr;
	private volatile Serializable comp;
	
	public MvelCondition(String expr) {
		this.expr = expr;
	}
	
	public final Serializable getExpression() {
		Serializable c = comp;
		if (c == null) {
			synchronized (this) {
				if (comp == null) {
					comp = MVEL.compileExpression(expr); 
				}
				c = comp;
			}
		}
		return c;
	}
	
	@Override
	public boolean eval(Map<String, Object> ctx) throws Exception {
		return (Boolean)MVEL.executeExpression(getExpression(), ctx);
	}

}
