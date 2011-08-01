/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florent Guillaume
 */
package org.eclipse.ecr.opencmis.bindings;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.shared.CallContextHandler;

/**
 * Call Context Handler for Nuxeo authentication that extracts the relevant user
 * name.
 * <p>
 * Authentication happened earlier in the chain through Nuxeo's authentication
 * filter, and a JAAS context has already been set up.
 * <p>
 * There is no password available, as authentication is opaque and may use SSO.
 */
public class NuxeoAuthCallContextHandler implements CallContextHandler,
        Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Map<String, String> getCallContextMap(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        HashMap<String, String> result = new HashMap<String, String>();
        if (principal != null) {
            result.put(CallContext.USERNAME, principal.getName());
        }
        return result;
    }

}
