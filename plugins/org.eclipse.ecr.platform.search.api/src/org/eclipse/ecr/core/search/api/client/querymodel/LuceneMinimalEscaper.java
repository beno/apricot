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

package org.eclipse.ecr.core.search.api.client.querymodel;

/**
 * A very minimal escaper: does double quotes (putting just one is incorrect), +, -
 * and !
 *
 * @author <a href="mailto:gracinet@nuxeo.com">Georges Racinet</a>
 */
// XXX this escaper does not seem to be doing an accurate job for VCS
public class LuceneMinimalEscaper implements Escaper {

    @Override
    public String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '+' || c == '-' || c == '!' || c == '"') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

}
