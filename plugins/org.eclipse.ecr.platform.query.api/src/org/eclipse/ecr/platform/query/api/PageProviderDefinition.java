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

package org.eclipse.ecr.platform.query.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.ecr.core.api.SortInfo;

/**
 * Page provider descriptor interface handling all attributes common to a
 * {@link PageProvider} generation.
 *
 * @author Anahide Tchertchian
 * @since 5.4
 */
public interface PageProviderDefinition extends Serializable {

    String getName();

    boolean isEnabled();

    Map<String, String> getProperties();

    String[] getQueryParameters();

    boolean getQuotePatternParameters();

    boolean getEscapePatternParameters();

    void setPattern(String pattern);

    String getPattern();

    WhereClauseDefinition getWhereClause();

    boolean isSortable();

    List<SortInfo> getSortInfos();

    String getSortInfosBinding();

    long getPageSize();

    String getPageSizeBinding();

    Long getMaxPageSize();

}
