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
 * $Id: JOOoConvertPluginImpl.java 18651 2007-05-13 20:28:53Z sfermigier $
 */

package org.eclipse.ecr.common.utils.i18n;

import java.io.Serializable;

public class Labeler implements Serializable {

    private static final long serialVersionUID = -4139432411098427880L;

    protected final String prefix;

    public Labeler(String prefix) {
        this.prefix = prefix;
    }

    protected static String unCapitalize(String s) {
        char c = Character.toLowerCase(s.charAt(0));
        return c + s.substring(1);
    }

    public String makeLabel(String itemId) {
        return prefix + '.' + unCapitalize(itemId);
    }

}
