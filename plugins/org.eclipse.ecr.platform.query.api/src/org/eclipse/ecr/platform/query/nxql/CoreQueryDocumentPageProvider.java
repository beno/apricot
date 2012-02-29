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
package org.eclipse.ecr.platform.query.nxql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ecr.core.api.ClientException;
import org.eclipse.ecr.core.api.ClientRuntimeException;
import org.eclipse.ecr.core.api.CoreSession;
import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.api.DocumentModelList;
import org.eclipse.ecr.core.api.SortInfo;
import org.eclipse.ecr.platform.query.api.AbstractPageProvider;
import org.eclipse.ecr.platform.query.api.PageProviderDefinition;
import org.eclipse.ecr.platform.query.api.PageSelections;

/**
 * Page provider performing a query on a core session.
 * <p>
 * It builds the query at each call so that it can refresh itself when the
 * query changes.
 * <p>
 * TODO: describe needed properties
 *
 * @author Anahide Tchertchian
 * @since 5.4
 */
public class CoreQueryDocumentPageProvider extends
        AbstractPageProvider<DocumentModel> {

    public static final String CORE_SESSION_PROPERTY = "coreSession";

    public static final String CHECK_QUERY_CACHE_PROPERTY = "checkQueryCache";

    private static final Log log = LogFactory.getLog(CoreQueryDocumentPageProvider.class);

    private static final long serialVersionUID = 1L;

    protected String query;

    protected List<DocumentModel> currentPageDocuments;

    @Override
    public List<DocumentModel> getCurrentPage() {
        checkQueryCache();
        if (currentPageDocuments == null) {
            error = null;
            errorMessage = null;

            CoreSession coreSession = getCoreSession();
            if (query == null) {
                buildQuery(coreSession);
            }
            if (query == null) {
                throw new ClientRuntimeException(String.format(
                        "Cannot perform null query: check provider '%s'",
                        getName()));
            }

            currentPageDocuments = new ArrayList<DocumentModel>();

            try {

                long minMaxPageSize = getMinMaxPageSize();

                long offset = getCurrentPageOffset();
                if (log.isDebugEnabled()) {
                    log.debug(String.format(
                            "Perform query for provider '%s': '%s' with pageSize=%s, offset=%s",
                            getName(), query, Long.valueOf(minMaxPageSize),
                            Long.valueOf(offset)));
                }

                DocumentModelList docs = coreSession.query(query, null,
                        minMaxPageSize, offset, true);
                long resultsCount = docs.totalSize();
                setResultsCount(resultsCount);
                currentPageDocuments = docs;

                if (log.isDebugEnabled()) {
                    log.debug(String.format(
                            "Performed query for provider '%s': got %s hits",
                            getName(), Long.valueOf(resultsCount)));
                }

            } catch (Exception e) {
                error = e;
                errorMessage = e.getMessage();
                log.warn(e.getMessage(), e);
            }
        }
        return currentPageDocuments;
    }

    protected void buildQuery(CoreSession coreSession) {
        try {
            SortInfo[] sortArray = null;
            if (sortInfos != null) {
                sortArray = sortInfos.toArray(new SortInfo[] {});
            }
            String newQuery;
            PageProviderDefinition def = getDefinition();
            if (def.getWhereClause() == null) {
                newQuery = NXQLQueryBuilder.getQuery(def.getPattern(),
                        getParameters(), def.getQuotePatternParameters(),
                        def.getEscapePatternParameters(), sortArray);
            } else {
                DocumentModel searchDocumentModel = getSearchDocumentModel();
                if (searchDocumentModel == null) {
                    throw new ClientException(String.format(
                            "Cannot build query of provider '%s': "
                                    + "no search document model is set",
                            getName()));
                }
                newQuery = NXQLQueryBuilder.getQuery(searchDocumentModel,
                        def.getWhereClause(), getParameters(), sortArray);
            }

            if (newQuery != null && !newQuery.equals(query)) {
                // query has changed => refresh
                refresh();
                query = newQuery;
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected void checkQueryCache() {
        // maybe handle refresh of select page according to query
        Map<String, Serializable> props = getProperties();
        if (props.containsKey(CHECK_QUERY_CACHE_PROPERTY)
                && Boolean.TRUE.equals(Boolean.valueOf((String) props.get(CHECK_QUERY_CACHE_PROPERTY)))) {
            CoreSession coreSession = getCoreSession();
            buildQuery(coreSession);
        }
    }

    protected CoreSession getCoreSession() {
        Map<String, Serializable> props = getProperties();
        CoreSession coreSession = (CoreSession) props.get(CORE_SESSION_PROPERTY);
        if (coreSession == null) {
            throw new ClientRuntimeException("cannot find core session");
        }
        return coreSession;
    }

    @Override
    public PageSelections<DocumentModel> getCurrentSelectPage() {
        checkQueryCache();
        return super.getCurrentSelectPage();
    }

    public String getCurrentQuery() {
        return query;
    }

    @Override
    protected void pageChanged() {
        super.pageChanged();
        currentPageDocuments = null;
    }

    @Override
    public void refresh() {
        super.refresh();
        query = null;
        currentPageDocuments = null;
    }

}
