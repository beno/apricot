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
package org.eclipse.ecr.automation.core.impl;

import org.eclipse.ecr.automation.AutomationService;
import org.eclipse.ecr.runtime.api.Framework;
import org.eclipse.ecr.runtime.services.event.Event;
import org.eclipse.ecr.runtime.services.event.EventListener;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class ReloadListener implements EventListener {

    @Override
    public boolean aboutToHandleEvent(Event event) {
        return true;
    }

    @Override
    public void handleEvent(Event event) {
        final String id = event.getId();
        if ("flushCompiledChains".equals(id) || "flush".equals(id)) {
            OperationServiceImpl svc = (OperationServiceImpl) Framework.getLocalService(AutomationService.class);
            svc.flushCompiledChains();
        }
    }

}
