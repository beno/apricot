/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.eclipse.ecr.core.query.sql.model;

import java.util.ArrayList;

/**
 * @author  <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class OperandList extends ArrayList<Operand> implements Operand {

    private static final long serialVersionUID = -4527766076726382014L;

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitOperandList(this);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (isEmpty()) {
            return "";
        }
        buf.append(get(0).toString());
        for (int i = 1, size = size(); i < size; i++) {
            buf.append(", ").append(get(i).toString());
        }
        return buf.toString();
    }

}
