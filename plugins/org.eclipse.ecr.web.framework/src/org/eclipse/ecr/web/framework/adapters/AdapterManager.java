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
 */
package org.eclipse.ecr.web.framework.adapters;

import java.util.Map;



/**
 * TODO multiple adapter factories may be added to the same "path" - need to use 
 * the adapterfactory acceptTarget in order to choose the right adapter from the list
 *   
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class AdapterManager {

    protected Map<String, AdapterFactory> factories = new java.util.HashMap<String, AdapterFactory>();

    public AdapterFactory getAdapterFactory(String path) {
        return factories.get(path);
    }

    public AdapterFactory putAdapterFactory(String path, AdapterFactory factory) {
        return factories.put(path, factory);
    }

    public AdapterFactory removeAdapterFactory(String path) {
        return factories.remove(path);
    }

    public void addAdapterFactory(AdapterFactoryDescriptor descriptor)
            throws Exception {
        putAdapterFactory(descriptor.path, descriptor.clazz.newInstance());
    }

    public void removeAdapterFactory(AdapterFactoryDescriptor descriptor) {
        removeAdapterFactory(descriptor.path);
    }

}
