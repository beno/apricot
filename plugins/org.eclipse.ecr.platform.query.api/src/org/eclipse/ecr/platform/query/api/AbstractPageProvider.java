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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ecr.core.api.ClientException;
import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.api.SortInfo;

/**
 * Basic implementation for a {@link PageProvider}
 *
 * @author Anahide Tchertchian
 */
public abstract class AbstractPageProvider<T> implements PageProvider<T> {

    private static final long serialVersionUID = 1L;

    protected String name;

    protected long offset = 0;

    protected long pageSize = 0;

    protected long maxPageSize = DEFAULT_MAX_PAGE_SIZE;

    protected long resultsCount = UNKNOWN_SIZE;

    protected int currentEntryIndex = 0;

    /**
     * Integer keeping track of the higher page index giving results. Useful
     * for enabling or disabling the nextPage action when number of results
     * cannot be known.
     *
     * @since 5.5
     */
    protected int currentHigherNonEmptyPageIndex = 0;

    protected List<SortInfo> sortInfos;

    protected boolean sortable = false;

    protected List<T> selectedEntries;

    protected PageSelections<T> currentSelectPage;

    protected Map<String, Serializable> properties;

    protected Object[] parameters;

    protected DocumentModel searchDocumentModel;

    protected String errorMessage;

    protected Throwable error;

    protected PageProviderDefinition definition;

    public abstract List<T> getCurrentPage();

    /**
     * Page change hook, to override for custom behavior
     */
    protected void pageChanged() {
        currentEntryIndex = 0;
        currentSelectPage = null;
    }

    public void firstPage() {
        long pageSize = getPageSize();
        if (pageSize == 0) {
            // do nothing
            return;
        }
        long offset = getCurrentPageOffset();
        if (offset != 0) {
            setCurrentPageOffset(0);
            pageChanged();
        }
    }

    /**
     * @deprecated: use {@link #firstPage()} instead
     */
    @Deprecated
    public void rewind() {
        firstPage();
    }

    public long getCurrentPageIndex() {
        long pageSize = getPageSize();
        if (pageSize == 0) {
            return 0;
        }
        long offset = getCurrentPageOffset();
        return offset / pageSize;
    }

    public long getCurrentPageOffset() {
        return offset;
    }

    public void setCurrentPageOffset(long offset) {
        this.offset = offset;
    }

    public long getCurrentPageSize() {
        List<T> currentItems = getCurrentPage();
        if (currentItems != null) {
            return currentItems.size();
        }
        return 0;
    }

    public String getName() {
        return name;
    }

    public long getNumberOfPages() {
        long pageSize = getPageSize();
        if (pageSize == 0) {
            return 1;
        }
        long resultsCount = getResultsCount();
        if (resultsCount < 0) {
            return 0;
        } else {
            return (1 + (resultsCount - 1) / pageSize);
        }
    }

    public List<T> setCurrentPage(long page) {
        long pageSize = getPageSize();
        long offset = page * pageSize;
        setCurrentPageOffset(offset);
        pageChanged();
        return getCurrentPage();
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        long localPageSize = getPageSize();
        if (localPageSize != pageSize) {
            this.pageSize = pageSize;
            // reset offset too
            setCurrentPageOffset(0);
            refresh();
        }
    }

    public List<SortInfo> getSortInfos() {
        // break reference
        List<SortInfo> res = new ArrayList<SortInfo>();
        if (sortInfos != null) {
            res.addAll(sortInfos);
        }
        return res;
    }

    public SortInfo getSortInfo() {
        if (sortInfos != null && !sortInfos.isEmpty()) {
            return sortInfos.get(0);
        }
        return null;
    }

    protected boolean sortInfoChanged(List<SortInfo> oldSortInfos,
            List<SortInfo> newSortInfos) {
        if (oldSortInfos == null && newSortInfos == null) {
            return false;
        } else if (oldSortInfos == null) {
            oldSortInfos = Collections.emptyList();
        } else if (newSortInfos == null) {
            newSortInfos = Collections.emptyList();
        }
        if (oldSortInfos.size() != newSortInfos.size()) {
            return true;
        }
        for (int i = 0; i < oldSortInfos.size(); i++) {
            SortInfo oldSort = oldSortInfos.get(i);
            SortInfo newSort = newSortInfos.get(i);
            if (oldSort == null && newSort == null) {
                continue;
            } else if (oldSort == null || newSort == null) {
                return true;
            }
            if (!oldSort.equals(newSort)) {
                return true;
            }
        }
        return false;
    }

    public void setSortInfos(List<SortInfo> sortInfo) {
        if (sortInfoChanged(this.sortInfos, sortInfo)) {
            this.sortInfos = sortInfo;
            refresh();
        }
    }

    public void setSortInfo(SortInfo sortInfo) {
        List<SortInfo> newSortInfos = new ArrayList<SortInfo>();
        if (sortInfo != null) {
            newSortInfos.add(sortInfo);
        }
        setSortInfos(newSortInfos);
    }

    public void setSortInfo(String sortColumn, boolean sortAscending,
            boolean removeOtherSortInfos) {
        if (removeOtherSortInfos) {
            SortInfo sortInfo = new SortInfo(sortColumn, sortAscending);
            setSortInfo(sortInfo);
        } else {
            if (getSortInfoIndex(sortColumn, sortAscending) != -1) {
                // do nothing: sort on this column is not set
            } else if (getSortInfoIndex(sortColumn, !sortAscending) != -1) {
                // change direction
                List<SortInfo> newSortInfos = new ArrayList<SortInfo>();
                for (SortInfo sortInfo : getSortInfos()) {
                    if (sortColumn.equals(sortInfo.getSortColumn())) {
                        newSortInfos.add(new SortInfo(sortColumn, sortAscending));
                    } else {
                        newSortInfos.add(sortInfo);
                    }
                }
                setSortInfos(newSortInfos);
            } else {
                // just add it
                addSortInfo(sortColumn, sortAscending);
            }
        }
    }

    public void addSortInfo(String sortColumn, boolean sortAscending) {
        SortInfo sortInfo = new SortInfo(sortColumn, sortAscending);
        List<SortInfo> sortInfos = getSortInfos();
        if (sortInfos == null) {
            setSortInfo(sortInfo);
        } else {
            sortInfos.add(sortInfo);
            setSortInfos(sortInfos);
        }
    }

    public int getSortInfoIndex(String sortColumn, boolean sortAscending) {
        List<SortInfo> sortInfos = getSortInfos();
        if (sortInfos == null || sortInfos.isEmpty()) {
            return -1;
        } else {
            SortInfo sortInfo = new SortInfo(sortColumn, sortAscending);
            return sortInfos.indexOf(sortInfo);
        }
    }

    public boolean isNextPageAvailable() {
        long pageSize = getPageSize();
        if (pageSize == 0) {
            return false;
        }
        long resultsCount = getResultsCount();
        if (resultsCount < 0) {
            long currentPageIndex = getCurrentPageIndex();
            return currentPageIndex < getCurrentHigherNonEmptyPageIndex()
                    + getMaxNumberOfEmptyPages();
        } else {
            long offset = getCurrentPageOffset();
            return resultsCount > pageSize + offset;
        }
    }

    @Override
    public boolean isLastPageAvailable() {
        long resultsCount = getResultsCount();
        if (resultsCount < 0) {
            return false;
        }
        return isNextPageAvailable();
    }

    public boolean isPreviousPageAvailable() {
        long offset = getCurrentPageOffset();
        return offset > 0;
    }

    public void lastPage() {
        long pageSize = getPageSize();
        long resultsCount = getResultsCount();
        if (pageSize == 0 || resultsCount < 0) {
            // do nothing
            return;
        }
        if (resultsCount % pageSize == 0) {
            setCurrentPageOffset(resultsCount - pageSize);
        } else {
            setCurrentPageOffset(resultsCount - resultsCount % pageSize);
        }
        pageChanged();
    }

    /**
     * @deprecated: use {@link #lastPage()} instead
     */
    @Deprecated
    public void last() {
        lastPage();
    }

    public void nextPage() {
        long pageSize = getPageSize();
        if (pageSize == 0) {
            // do nothing
            return;
        }
        long offset = getCurrentPageOffset();
        offset += pageSize;
        setCurrentPageOffset(offset);
        pageChanged();
    }

    /**
     * @deprecated: use {@link #nextPage()} instead
     */
    @Deprecated
    public void next() {
        nextPage();
    }

    public void previousPage() {
        long pageSize = getPageSize();
        if (pageSize == 0) {
            // do nothing
            return;
        }
        long offset = getCurrentPageOffset();
        if (offset >= pageSize) {
            offset -= pageSize;
            setCurrentPageOffset(offset);
            pageChanged();
        }
    }

    /**
     * @deprecated: use {@link #previousPage()} instead
     */
    @Deprecated
    public void previous() {
        previousPage();
    }

    public void refresh() {
        setResultsCount(UNKNOWN_SIZE);
        setCurrentHigherNonEmptyPageIndex(-1);
        currentSelectPage = null;
        errorMessage = null;
        error = null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrentPageStatus() {
        long total = getNumberOfPages();
        long current = getCurrentPageIndex() + 1;
        if (total <= 0) {
            // number of pages unknown or there is only one page
            return String.format("%d", Long.valueOf(current));
        } else {
            return String.format("%d/%d", Long.valueOf(current),
                    Long.valueOf(total));
        }
    }

    public boolean isNextEntryAvailable() {
        long pageSize = getPageSize();
        long resultsCount = getResultsCount();
        if (pageSize == 0) {
            if (resultsCount < 0) {
                // results count unknown
                long currentPageSize = getCurrentPageSize();
                return currentEntryIndex < currentPageSize - 1;
            } else {
                return currentEntryIndex < resultsCount - 1;
            }
        } else {
            long currentPageSize = getCurrentPageSize();
            if (currentEntryIndex < currentPageSize - 1) {
                return true;
            }
            if (resultsCount < 0) {
                // results count unknown => do not look for entry in next page
                return false;
            } else {
                return isNextPageAvailable();
            }
        }
    }

    public boolean isPreviousEntryAvailable() {
        return (currentEntryIndex != 0 || isPreviousPageAvailable());
    }

    public void nextEntry() {
        long pageSize = getPageSize();
        long resultsCount = getResultsCount();
        if (pageSize == 0) {
            if (resultsCount < 0) {
                // results count unknown
                long currentPageSize = getCurrentPageSize();
                if (currentEntryIndex < currentPageSize - 1) {
                    currentEntryIndex++;
                    return;
                }
            } else {
                if (currentEntryIndex < resultsCount - 1) {
                    currentEntryIndex++;
                    return;
                }
            }
        } else {
            long currentPageSize = getCurrentPageSize();
            if (currentEntryIndex < currentPageSize - 1) {
                currentEntryIndex++;
                return;
            }
            if (resultsCount >= 0) {
                // if results count is unknown, do not look for entry in next
                // page
                if (isNextPageAvailable()) {
                    nextPage();
                    currentEntryIndex = 0;
                    return;
                }
            }
        }

    }

    public void previousEntry() {
        if (currentEntryIndex > 0) {
            currentEntryIndex--;
            return;
        }
        if (!isPreviousPageAvailable()) {
            return;
        }

        previousPage();
        List<T> currentPage = getCurrentPage();
        if (currentPage == null || currentPage.isEmpty()) {
            // things may have changed since last query
            currentEntryIndex = 0;
        } else {
            currentEntryIndex = (new Long(getPageSize() - 1)).intValue();
        }
    }

    public T getCurrentEntry() {
        List<T> currentPage = getCurrentPage();
        if (currentPage == null || currentPage.isEmpty()) {
            return null;
        }
        return currentPage.get(currentEntryIndex);
    }

    public void setCurrentEntry(T entry) throws ClientException {
        List<T> currentPage = getCurrentPage();
        if (currentPage == null || currentPage.isEmpty()) {
            throw new ClientException(String.format(
                    "Entry '%s' not found in current page", entry));
        }
        int i = currentPage.indexOf(entry);
        if (i == -1) {
            throw new ClientException(String.format(
                    "Entry '%s' not found in current page", entry));
        }
        currentEntryIndex = i;
    }

    public void setCurrentEntryIndex(long index) throws ClientException {
        int intIndex = new Long(index).intValue();
        List<T> currentPage = getCurrentPage();
        if (currentPage == null || currentPage.isEmpty()) {
            throw new ClientException(
                    String.format("Index %s not found in current page",
                            new Integer(intIndex)));
        }
        if (index >= currentPage.size()) {
            throw new ClientException(
                    String.format("Index %s not found in current page",
                            new Integer(intIndex)));
        }
        currentEntryIndex = intIndex;
    }

    public long getResultsCount() {
        return resultsCount;
    }

    public Map<String, Serializable> getProperties() {
        // break reference
        return new HashMap<String, Serializable>(properties);
    }

    public void setProperties(Map<String, Serializable> properties) {
        this.properties = properties;
    }

    public void setResultsCount(long resultsCount) {
        this.resultsCount = resultsCount;
        setCurrentHigherNonEmptyPageIndex(-1);
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public boolean isSortable() {
        return sortable;
    }

    public PageSelections<T> getCurrentSelectPage() {
        if (currentSelectPage == null) {
            List<PageSelection<T>> entries = new ArrayList<PageSelection<T>>();
            List<T> currentPage = getCurrentPage();
            currentSelectPage = new PageSelections<T>();
            currentSelectPage.setName(name);
            if (currentPage != null && !currentPage.isEmpty()) {
                if (selectedEntries == null || selectedEntries.isEmpty()) {
                    // no selection at all
                    for (int i = 0; i < currentPage.size(); i++) {
                        entries.add(new PageSelection<T>(currentPage.get(i),
                                false));
                    }
                } else {
                    boolean allSelected = true;
                    for (int i = 0; i < currentPage.size(); i++) {
                        T entry = currentPage.get(i);
                        Boolean selected = Boolean.valueOf(selectedEntries.contains(entry));
                        if (!Boolean.TRUE.equals(selected)) {
                            allSelected = false;
                        }
                        entries.add(new PageSelection<T>(entry,
                                selected.booleanValue()));
                    }
                    if (allSelected) {
                        currentSelectPage.setSelected(true);
                    }
                }
            }
            currentSelectPage.setEntries(entries);
        }
        return currentSelectPage;
    }

    public void setSelectedEntries(List<T> entries) {
        this.selectedEntries = entries;
        // reset current select page so that it's rebuilt
        currentSelectPage = null;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public DocumentModel getSearchDocumentModel() {
        return searchDocumentModel;
    }

    protected boolean searchDocumentModelChanged(DocumentModel oldDoc,
            DocumentModel newDoc) {
        if (oldDoc == null && newDoc == null) {
            return false;
        } else if (oldDoc == null || newDoc == null) {
            return true;
        }
        // do not compare properties and assume it's changed
        return true;
    }

    public void setSearchDocumentModel(DocumentModel searchDocumentModel) {
        if (searchDocumentModelChanged(this.searchDocumentModel,
                searchDocumentModel)) {
            refresh();
        }
        this.searchDocumentModel = searchDocumentModel;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Throwable getError() {
        return error;
    }

    public boolean hasError() {
        return error != null;
    }

    @Override
    public PageProviderDefinition getDefinition() {
        return definition;
    }

    @Override
    public void setDefinition(PageProviderDefinition providerDefinition) {
        this.definition = providerDefinition;
    }

    public long getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(long maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    /**
     * Returns the minimal value for the max page size, taking the lower value
     * between the requested page size and the maximum accepted page size.
     *
     * @since 5.4.2
     */
    public long getMinMaxPageSize() {
        long pageSize = getPageSize();
        long maxPageSize = getMaxPageSize();
        if (maxPageSize < 0) {
            maxPageSize = DEFAULT_MAX_PAGE_SIZE;
        }
        if (pageSize <= 0) {
            return maxPageSize;
        }
        if (maxPageSize > 0 && maxPageSize < pageSize) {
            return maxPageSize;
        }
        return pageSize;
    }

    /**
     * Returns an integer keeping track of the higher page index giving
     * results. Useful for enabling or disabling the nextPage action when
     * number of results cannot be known.
     *
     * @since 5.5
     */
    public int getCurrentHigherNonEmptyPageIndex() {
        return currentHigherNonEmptyPageIndex;
    }

    public void setCurrentHigherNonEmptyPageIndex(int higherFilledPageIndex) {
        this.currentHigherNonEmptyPageIndex = higherFilledPageIndex;
    }

    /**
     * Returns the maximum number of empty pages that can be fetched empty
     * (defaults to 1). Can be useful for displaying pages of a provider
     * without results count.
     *
     * @since 5.5
     */
    public int getMaxNumberOfEmptyPages() {
        return 1;
    }

}
