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
 * $Id$
 */

package org.eclipse.ecr.web.rendering.fm.adapters;

import org.eclipse.ecr.core.api.model.Property;
import org.eclipse.ecr.core.api.model.impl.ListProperty;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateSequenceModel;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class ListPropertyTemplate extends PropertyWrapper implements
        TemplateCollectionModel, TemplateSequenceModel, AdapterTemplateModel {

    protected final ListProperty property;

    public ListPropertyTemplate(DocumentObjectWrapper wrapper,
            ListProperty property) {
        super(wrapper);
        this.property = property;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object getAdaptedObject(Class hint) {
        return property;
    }

    @Override
    public TemplateModelIterator iterator() throws TemplateModelException {
        return new PropertyIteratorTemplate(wrapper, property.iterator());
    }

    @Override
    public TemplateModel get(int arg0) throws TemplateModelException {
        Property p = property.get(arg0);
        return wrap(p);
    }

    @Override
    public int size() throws TemplateModelException {
        return property.size();
    }

}
