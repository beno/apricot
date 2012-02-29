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
package org.eclipse.ecr.runtime.model.impl;

import java.util.Set;

import org.eclipse.ecr.runtime.model.ComponentName;
import org.eclipse.ecr.runtime.model.RegistrationInfo;

/**
 * Deactivate components in the proper order to avoid exceptions at shutdown.
 * 
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class ShutdownTask {

    final static void shutdown(ComponentManagerImpl mgr) {
        RegistrationInfoImpl[] ris = mgr.reg.getComponentsArray();
        for (RegistrationInfoImpl ri : ris) {
            shutdown(mgr, ri);
        }
    }

    private static void shutdown(ComponentManagerImpl mgr,
            RegistrationInfoImpl ri) {
        ComponentName name = ri.getName();
        if (name == null) {
            return; // already destroyed
        }
        if (ri.getState() <= RegistrationInfo.RESOLVED) {
            // not yet activated so we can destroy it right now
            mgr.unregister(name);
            return;
        }
        // an active component - get the components depending on it
        Set<ComponentName> reqs = mgr.reg.requirements.get(name);
        if (reqs != null && !reqs.isEmpty()) {
            // there are some components depending on me - cannot shutdown
            for (ComponentName req : reqs.toArray(new ComponentName[reqs.size()])) {
                RegistrationInfoImpl parentRi = mgr.reg.components.get(req);
                if (parentRi != null) {
                    shutdown(mgr, parentRi);
                }
            }
        } else {
            // no components are depending on me - shutdown now
            mgr.unregister(name);
        }
    }

}
