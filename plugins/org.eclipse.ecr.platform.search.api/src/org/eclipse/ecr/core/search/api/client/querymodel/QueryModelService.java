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
 *
 * $Id$
 */

package org.eclipse.ecr.core.search.api.client.querymodel;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ecr.core.search.api.client.querymodel.descriptor.QueryModelDescriptor;
import org.eclipse.ecr.runtime.model.ComponentContext;
import org.eclipse.ecr.runtime.model.ComponentInstance;
import org.eclipse.ecr.runtime.model.DefaultComponent;

/**
 * @deprecated since 5.4: use ContentView instances in conjunction with
 *             PageProvider instead.
 */
@Deprecated
public class QueryModelService extends DefaultComponent {

    public static final String NAME = "org.eclipse.ecr.core.search.api.client.querymodel.QueryModelService";

    private static final Log log = LogFactory.getLog(QueryModelService.class);

    private Map<String, QueryModelDescriptor> descriptors;

    public QueryModelDescriptor getQueryModelDescriptor(String descriptorName) {
        if (log.isWarnEnabled()) {
            log.warn(String.format(
                    "Query models are deprecated as of Nuxeo 5.4 and "
                            + "will be removed for Nuxeo 5.6: the query "
                            + "model '%s' should be upgraded to use content views",
                    descriptorName));
        }
        return descriptors.get(descriptorName);
    }

    @Override
    public void activate(ComponentContext context) {
        descriptors = new HashMap<String, QueryModelDescriptor>();
    }

    @Override
    public void deactivate(ComponentContext context) {
        descriptors = null;
    }

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor) {

        QueryModelDescriptor descriptor = (QueryModelDescriptor) contribution;
        if (descriptor.isStateful()) {
            descriptor.initEscaper(contributor.getContext());
        }

        String name = descriptor.getName();
        QueryModelDescriptor existing = descriptors.get(name);
        if (existing != null) {

            if (descriptor.getMax() == null) {
                descriptor.setMax(existing.getMax());
            }

            if (descriptor.getDefaultSortAscending() == null) {
                descriptor.setDefaultSortAscending(existing.getDefaultSortAscending());
            }

            if (descriptor.getSortable() == null) {
                descriptor.setSortable(existing.getSortable());
            }

            if (descriptor.getBatchLength() == null) {
                descriptor.setBatchLength(existing.getBatchLength());
            }

            if (descriptor.getBatchSize() == null) {
                descriptor.setBatchSize(existing.getBatchSize());
            }

            if (descriptor.getDefaultSortColumn() == null) {
                descriptor.setDefaultSortColumn(existing.getDefaultSortColumn());
            }

            if (descriptor.getPattern() == null) {
                descriptor.setPattern(existing.getPattern());
            }

            if (descriptor.getWhereClause() == null) {
                descriptor.setWhereClause(existing.getWhereClause());
            }

            if (descriptor.getSortAscendingField() == null) {
                descriptor.setSortAscendingField(existing.getSortAscendingField());
            }

        }

        descriptors.put(descriptor.getName(), descriptor);
        if (log.isDebugEnabled()) {
            log.debug("registered QueryModelDescriptor: "
                    + descriptor.getName());
        }
        if (log.isWarnEnabled()) {
            log.warn(String.format(
                    "Query models are deprecated as of Nuxeo 5.4 and "
                            + "will be removed for Nuxeo 5.6: the query "
                            + "model '%s' should be upgraded to use content views",
                    name));
        }
    }

    @Override
    public void unregisterContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor) {

        QueryModelDescriptor descriptor = (QueryModelDescriptor) contribution;
        descriptors.remove(descriptor.getName());
        if (log.isDebugEnabled()) {
            log.debug("unregistered QueryModelDescriptor: "
                    + descriptor.getName());
        }
    }

}
