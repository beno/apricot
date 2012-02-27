/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anahide Tchertchian
 */
package org.eclipse.ecr.platform.query.api;

import java.io.Serializable;

/**
 * Entry wrapping selection information for given data entry
 *
 * @author Anahide Tchertchian
 */
public class PageSelection<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected boolean selected;

    protected T data;

    public PageSelection(T data, boolean selected) {
        this.data = data;
        this.selected = selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
