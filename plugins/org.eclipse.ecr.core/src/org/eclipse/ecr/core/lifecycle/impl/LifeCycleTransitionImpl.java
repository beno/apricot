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
 * $Id$
 */

package org.eclipse.ecr.core.lifecycle.impl;

import org.eclipse.ecr.core.lifecycle.LifeCycleTransition;

/**
 * Life cycle transition implementation.
 *
 * @see org.eclipse.ecr.core.lifecycle.LifeCycleTransition
 *
 * @author <a href="mailto:ja@nuxeo.com">Julien Anguenot</a>
 */
public class LifeCycleTransitionImpl implements LifeCycleTransition {

    /** Name of the transition. */
    private final String name;

    /** Description of the transition. */
    private final String description;

    /** Destination state name for this transition. */
    private final String destinationStateName;


    public LifeCycleTransitionImpl(String name, String description, String destinationState) {
        this.name = name;
        this.description = description;
        destinationStateName = destinationState;
    }

    @Override
    public String getDestinationStateName() {
        return destinationStateName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }

}
