/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
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

package org.eclipse.ecr.runtime.api.login;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * The default callback handler used by Framework.login methods.
 * <p>
 * This callback handler supports 3 types of callbacks:
 * <ul>
 *   <li> the standard name callback
 *   <li> the standard password callback
 *   <li> a custom credentials callback that can be used to pass 
 *   specific login information.
 * </ul>
 *
 * @author eionica@nuxeo.com
 */
public class CredentialsCallbackHandler implements CallbackHandler {

    private final String name;

    private final Object credentials;

    public CredentialsCallbackHandler(String username, Object credentials) {
        this.name = username;
        this.credentials = credentials;
    }

    @Override
    public void handle(Callback[] callbacks)
            throws UnsupportedCallbackException {
        for (Callback cb : callbacks) {
            if (cb instanceof NameCallback) {
                ((NameCallback)cb).setName(name);
            } else if (cb instanceof PasswordCallback) {
                if (credentials instanceof CharSequence) {
                    // TODO cache the password to avoid recomputing it? 
                    ((PasswordCallback)cb).setPassword(credentials.toString().toCharArray());
                } else if (credentials instanceof char[]) {                    
                    ((PasswordCallback)cb).setPassword((char[])credentials);   
                } else {
                    // the credentials are not in a password format. use null
                    ((PasswordCallback)cb).setPassword(null);
                }
            } else if (cb instanceof CredentialsCallback) {
                // if neither name or password callback are given use the generic credentials callback
                ((CredentialsCallback)cb).setCredentials(credentials);
            } else {
                throw new UnsupportedCallbackException(cb,
                        "Unsupported callback "+cb+". " +
                        "Only NameCallback, PasswordCallback and CredentialsCallback are supported by this handler.");
            }
        }
    }

}
