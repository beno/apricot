/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Julien Anguenot
 *     Florent Guillaume
 */
package org.eclipse.ecr.core.search.api.client.search.results.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ecr.core.api.ClientException;
import org.eclipse.ecr.core.api.CoreSession;
import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.api.DocumentModelList;
import org.eclipse.ecr.core.api.DocumentSecurityException;
import org.eclipse.ecr.core.query.sql.model.SQLQuery;
import org.eclipse.ecr.core.search.api.client.SearchException;
import org.eclipse.ecr.core.search.api.client.search.results.ResultItem;
import org.eclipse.ecr.core.search.api.client.search.results.ResultSet;
import org.eclipse.ecr.runtime.api.Framework;

/**
 * Result set implementation.
 */
public class ResultSetImpl extends ArrayList<ResultItem> implements ResultSet {

    private static final long serialVersionUID = -6376330426798015144L;

    protected final int offset;

    /** 0 means all results */
    protected final int range;

    protected final int totalHits;

    protected final int pageHits;

    protected final String query;

    protected final SQLQuery sqlQuery;

    protected final CoreSession session;

    protected Boolean detachResultsFlag;

    /**
     * Constructor used when a CoreSession is available.
     */
    public ResultSetImpl(String query, CoreSession session, int offset,
            int range, List<ResultItem> resultItems, int totalHits, int pageHits) {
        this.query = query;
        sqlQuery = null;
        this.session = session;
        this.offset = offset;
        this.range = range;
        this.totalHits = totalHits;
        this.pageHits = pageHits;
        if (resultItems != null) {
            addAll(resultItems);
        }
    }

    public boolean detachResults() {
        if (detachResultsFlag == null) {
            detachResultsFlag = Boolean.valueOf(Framework.getProperty(
                    ALWAYS_DETACH_SEARCH_RESULTS_KEY, "false"));
        }
        return detachResultsFlag.booleanValue();
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getRange() {
        return range;
    }

    @Override
    public ResultSet nextPage() throws SearchException {
        if (!hasNextPage()) {
            return null;
        }
        return replay(offset + range, range);
    }

    @Override
    public ResultSet goToPage(int page) throws SearchException {
        int newOffset = range * (page - 1);
        if (newOffset >= 0 && newOffset < totalHits) {
            return replay(newOffset, range);
        }
        return null;
    }

    @Override
    public int getTotalHits() {
        return totalHits;
    }

    @Override
    public int getPageHits() {
        return pageHits;
    }

    @Override
    public boolean hasNextPage() {
        if (range == 0) {
            return false;
        }
        if (pageHits < range) {
            return false;
        }
        return (offset + range) < totalHits;
    }

    @Override
    public boolean isFirstPage() {
        return range == 0 ? true : offset < range;
    }

    @Override
    public ResultSet replay() throws SearchException {
        return replay(offset, range);
    }

    @Override
    public ResultSet replay(int offset, int range) throws SearchException {
        if (session != null) {
            try {
                DocumentModelList list = session.query(query, null, range,
                        offset, true);
                List<ResultItem> resultItems = new ArrayList<ResultItem>(
                        list.size());
                for (DocumentModel doc : list) {
                    if (doc == null) {
                        continue;
                    }
                    if (detachResults()) {
                        // detach the document so that we can use it beyond the
                        // session
                        try {
                            doc.detach(true);
                        } catch (DocumentSecurityException e) {
                            // no access to the document (why?)
                            continue;
                        }
                    }
                    resultItems.add(new DocumentModelResultItem(doc));
                }
                return new ResultSetImpl(query, session, offset, range,
                        resultItems, (int) list.totalSize(), list.size());
            } catch (ClientException e) {
                throw new SearchException("QueryException for: " + query, e);
            }
        }
        throw new SearchException("No session");
    }

    @Override
    public int getPageNumber() {
        if (range == 0) {
            return 1;
        }
        return (offset + range) / range;
    }

}
