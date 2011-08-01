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

package org.eclipse.ecr.core.api.local;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;


/**
 * A login module that is propagating the login information into the core login stack.
 * <p>
 * This login module doesn't make any authentication - it is called only after the authentication 
 * is successfully done by a previous login module.
 * <p>
 * The static method of this class can also be used to manage the current login stack.  
 * 
 * @author  eionica@nuxeo.com
 * 
 */
public class ClientLoginModule implements LoginModule {

    /**
     * The global login stack
     */
    protected static final LoginStack globalInstance = LoginStack.synchronizedStack();

    /**
     * The thread local login stack - for per thread logins
     */
    protected static final ThreadLocal<LoginStack> threadInstance = new ThreadLocal<LoginStack>() {
        @Override
        protected LoginStack initialValue() {
            return new LoginStack();
        }
    };

    public static LoginStack getThreadLocalLogin() {
        return threadInstance.get();
    }

    public static LoginStack.Entry getCurrentLogin() {
        LoginStack.Entry entry = threadInstance.get().peek();
        if (entry == null) {
            entry = globalInstance.peek();
        }
        return entry;
    }


    private Subject subject;
    private Map sharedState;
    // active login stack
    private LoginStack stack;
    // whether or not the login was propagated
    private boolean commited = false;

    /**
     * Initialize this LoginModule.
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map sharedState, Map options) {
        this.subject = subject;
        this.sharedState = sharedState;
        // Check if login must be propagated to entire JVM or only to the current thread.
        // the default is per-thread login
        boolean globalLogin = true;
        String global = (String)options.get("global");
        if (global != null) {
            globalLogin = Boolean.parseBoolean(global);
        }        
        if (globalLogin) {
            // propagate the login only for the current thread
            stack = threadInstance.get();
        } else {
            // propagate the login for all threads in JVM
            stack = globalInstance;
        }
    }


    @Override
    public boolean login() throws LoginException {
        // this login module doesn't make any user authentication 
        // it simply propagate the login to the login stack. 
        // So it must be put after the authenticating module.
        // The authenticating module should update the sharedState map
        // with the login info.
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        Principal p = null;
        Object user = sharedState.get("javax.security.auth.login.name");
        if (user instanceof Principal) {
            p = (Principal)user;
        } else {
            Set<Principal> principals = subject.getPrincipals();
            if (!principals.isEmpty()) {
                p = principals.iterator().next();
            }            
        }
        if (p != null) {
            Object credential = sharedState.get("javax.security.auth.login.password");
            stack.push(p, credential, subject);
            commited = true;
        }
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        commited = false;
        stack.clear();
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        if (commited) {
            stack.pop();
            commited = false;
        }
        return true;
    }

}
