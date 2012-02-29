/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anahide Tchertchian
 */
package org.eclipse.ecr.platform.query.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ecr.common.xmap.annotation.XNode;
import org.eclipse.ecr.common.xmap.annotation.XNodeList;
import org.eclipse.ecr.common.xmap.annotation.XNodeMap;
import org.eclipse.ecr.common.xmap.annotation.XObject;
import org.eclipse.ecr.core.api.SortInfo;
import org.eclipse.ecr.platform.query.api.PageProviderDefinition;
import org.eclipse.ecr.platform.query.api.WhereClauseDefinition;

/**
 * Core Query page provider descriptor.
 *
 * @author Anahide Tchertchian
 * @since 5.4
 */
@XObject("coreQueryPageProvider")
public class CoreQueryPageProviderDescriptor implements PageProviderDefinition {

    private static final long serialVersionUID = 1L;

    @XNode("@name")
    String name;

    @XNode("@enabled")
    boolean enabled = true;

    @XNodeMap(value = "property", key = "@name", type = HashMap.class, componentType = String.class)
    Map<String, String> properties = new HashMap<String, String>();

    @XNodeList(value = "parameter", type = String[].class, componentType = String.class)
    String[] queryParameters;

    @XNode("pageSize")
    long pageSize = 0;

    @XNode("pageSizeBinding")
    String pageSizeBinding;

    @XNode("maxPageSize")
    Long maxPageSize;

    @XNode("sortable")
    boolean sortable = true;

    @XNodeList(value = "sort", type = ArrayList.class, componentType = SortInfoDescriptor.class)
    List<SortInfoDescriptor> sortInfos;

    @XNode("sortInfosBinding")
    String sortInfosBinding;

    protected String pattern;

    @XNode("pattern@quoteParameters")
    protected boolean quotePatternParameters = true;

    @XNode("pattern@escapeParameters")
    protected boolean escapePatternParameters = true;

    @XNode("whereClause")
    protected WhereClauseDescriptor whereClause;

    @XNode("pattern")
    public void setPattern(String pattern) {
        // remove new lines and following spaces
        if (pattern != null) {
            this.pattern = pattern.replaceAll("\r?\n\\s*", " ");
        }
    }

    public boolean getQuotePatternParameters() {
        return quotePatternParameters;
    }

    public boolean getEscapePatternParameters() {
        return escapePatternParameters;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String[] getQueryParameters() {
        return queryParameters;
    }

    public String getPattern() {
        return pattern;
    }

    public WhereClauseDefinition getWhereClause() {
        return whereClause;
    }

    public boolean isSortable() {
        return sortable;
    }

    public List<SortInfo> getSortInfos() {
        List<SortInfo> res = new ArrayList<SortInfo>();
        if (sortInfos != null) {
            for (SortInfoDescriptor sortInfo : sortInfos) {
                res.add(sortInfo.getSortInfo());
            }
        }
        return res;
    }

    public long getPageSize() {
        return pageSize;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPageSizeBinding() {
        return pageSizeBinding;
    }

    public String getSortInfosBinding() {
        return sortInfosBinding;
    }

    public String getName() {
        return name;
    }

    @Override
    public Long getMaxPageSize() {
        return maxPageSize;
    }

}
