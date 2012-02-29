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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * List of {@link PageSelection} elements.
 *
 * @author Anahide Tchertchian
 */
public class PageSelections<T> {

    protected String name;

    protected boolean selected;

    protected List<PageSelection<T>> entries;

    public PageSelections() {
        super();
    }

    public PageSelections(Collection<? extends PageSelection<T>> c) {
        super();
        if (c != null) {
            this.entries = new ArrayList<PageSelection<T>>(c);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public List<PageSelection<T>> getEntries() {
        return entries;
    }

    public void setEntries(List<PageSelection<T>> entries) {
        this.entries = entries;
    }

    public int getSize() {
        if (entries == null) {
            return 0;
        } else {
            return entries.size();
        }
    }

    /**
     * @deprecated just here for compatibility with SelectDatamodel methods,
     *             use {@link #getSize()} instead
     */
    @Deprecated
    public int getRowCount() {
        return getSize();
    }

    /**
     * @deprecated just here for compatibility with SelectDatamodel methods,
     *             use {@link #getEntries()} instead
     */
    @Deprecated
    public List<PageSelection<T>> getRows() {
        return getEntries();
    }

}
