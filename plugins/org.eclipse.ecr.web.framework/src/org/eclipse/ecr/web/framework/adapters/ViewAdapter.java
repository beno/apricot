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
 */
package org.eclipse.ecr.web.framework.adapters;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.eclipse.ecr.web.framework.AdaptableResource;
import org.eclipse.ecr.web.framework.AdapterResource;
import org.eclipse.ecr.web.framework.View;
import org.eclipse.ecr.web.framework.ViewNotFoundException;

/**
 * @author bstefanescu
 * 
 */
public class ViewAdapter extends AdapterResource {

    public ViewAdapter(AdaptableResource target) {
    	super (target);
    }

    @GET
    @Path("{segment}")
    public Object doGet(@PathParam("segment") String segment) {
        try {
        	View view = target.getView(segment);
        	if (view == null) {
        		throw new ViewNotFoundException();
        	}
        	return view;
        } catch (Exception e) {
            return Response.status(500).build();
        }
    }
}
