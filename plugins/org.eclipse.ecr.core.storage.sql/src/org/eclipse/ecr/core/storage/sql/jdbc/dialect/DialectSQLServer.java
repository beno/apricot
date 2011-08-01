/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florent Guillaume
 */

package org.eclipse.ecr.core.storage.sql.jdbc.dialect;

import java.io.Serializable;
import java.net.SocketException;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ecr.core.storage.StorageException;
import org.eclipse.ecr.core.storage.sql.BinaryManager;
import org.eclipse.ecr.core.storage.sql.ColumnType;
import org.eclipse.ecr.core.storage.sql.Model;
import org.eclipse.ecr.core.storage.sql.RepositoryDescriptor;
import org.eclipse.ecr.core.storage.sql.jdbc.db.Column;
import org.eclipse.ecr.core.storage.sql.jdbc.db.Database;
import org.eclipse.ecr.core.storage.sql.jdbc.db.Join;
import org.eclipse.ecr.core.storage.sql.jdbc.db.Table;

/**
 * Microsoft SQL Server-specific dialect.
 *
 * @author Florent Guillaume
 */
public class DialectSQLServer extends Dialect {

    private static final String DEFAULT_FULLTEXT_ANALYZER = "english";

    private static final String DEFAULT_FULLTEXT_CATALOG = "nuxeo";

    protected final String fulltextAnalyzer;

    protected final String fulltextCatalog;

    public DialectSQLServer(DatabaseMetaData metadata,
            BinaryManager binaryManager,
            RepositoryDescriptor repositoryDescriptor) throws StorageException {
        super(metadata, binaryManager, repositoryDescriptor);
        fulltextAnalyzer = repositoryDescriptor == null ? null
                : repositoryDescriptor.fulltextAnalyzer == null ? DEFAULT_FULLTEXT_ANALYZER
                        : repositoryDescriptor.fulltextAnalyzer;
        fulltextCatalog = repositoryDescriptor == null ? null
                : repositoryDescriptor.fulltextCatalog == null ? DEFAULT_FULLTEXT_CATALOG
                        : repositoryDescriptor.fulltextCatalog;

    }

    @Override
    public char openQuote() {
        return '[';
    }

    @Override
    public char closeQuote() {
        return ']';
    }

    @Override
    public String getNoColumnsInsertString() {
        return "DEFAULT VALUES";
    }

    @Override
    public String getNullColumnString() {
        return " NULL";
    }

    @Override
    public boolean qualifyIndexName() {
        return false;
    }

    @Override
    public String getAddColumnString() {
        return "ADD";
    }

    @Override
    public JDBCInfo getJDBCTypeAndString(ColumnType type) {
        switch (type.spec) {
        case STRING:
            if (type.isUnconstrained()) {
                return jdbcInfo("NVARCHAR(4000)", Types.VARCHAR);
            } else if (type.isClob() || type.length > 4000) {
                return jdbcInfo("NVARCHAR(MAX)", Types.CLOB);
            } else {
                return jdbcInfo("NVARCHAR(%d)", type.length, Types.VARCHAR);
            }
        case BOOLEAN:
            return jdbcInfo("BIT", Types.BIT);
        case LONG:
            return jdbcInfo("BIGINT", Types.BIGINT);
        case DOUBLE:
            return jdbcInfo("DOUBLE PRECISION", Types.DOUBLE);
        case TIMESTAMP:
            return jdbcInfo("DATETIME", Types.TIMESTAMP);
        case BLOBID:
            return jdbcInfo("VARCHAR(40)", Types.VARCHAR);
            // -----
        case NODEID:
        case NODEIDFK:
        case NODEIDFKNP:
        case NODEIDFKMUL:
        case NODEIDFKNULL:
        case NODEIDPK:
        case NODEVAL:
            return jdbcInfo("VARCHAR(36)", Types.VARCHAR);
        case SYSNAME:
        case SYSNAMEARRAY:
            return jdbcInfo("VARCHAR(256)", Types.VARCHAR);
        case TINYINT:
            return jdbcInfo("TINYINT", Types.TINYINT);
        case INTEGER:
            return jdbcInfo("INT", Types.INTEGER);
        case FTINDEXED:
            throw new AssertionError(type);
        case FTSTORED:
            return jdbcInfo("NVARCHAR(MAX)", Types.CLOB);
        case CLUSTERNODE:
            return jdbcInfo("SMALLINT", Types.SMALLINT);
        case CLUSTERFRAGS:
            return jdbcInfo("VARCHAR(8000)", Types.VARCHAR);
        }
        throw new AssertionError(type);
    }

    @Override
    public boolean isAllowedConversion(int expected, int actual,
            String actualName, int actualSize) {
        // CLOB vs VARCHAR compatibility
        if (expected == Types.VARCHAR && actual == Types.CLOB) {
            return true;
        }
        if (expected == Types.CLOB && actual == Types.VARCHAR) {
            return true;
        }
        // INTEGER vs BIGINT compatibility
        if (expected == Types.BIGINT && actual == Types.INTEGER) {
            return true;
        }
        if (expected == Types.INTEGER && actual == Types.BIGINT) {
            return true;
        }
        return false;
    }

    @Override
    public void setToPreparedStatement(PreparedStatement ps, int index,
            Serializable value, Column column) throws SQLException {
        switch (column.getJdbcType()) {
        case Types.VARCHAR:
        case Types.CLOB:
            setToPreparedStatementString(ps, index, value, column);
            return;
        case Types.BIT:
            ps.setBoolean(index, ((Boolean) value).booleanValue());
            return;
        case Types.TINYINT:
        case Types.INTEGER:
        case Types.BIGINT:
            ps.setLong(index, ((Long) value).longValue());
            return;
        case Types.DOUBLE:
            ps.setDouble(index, ((Double) value).doubleValue());
            return;
        case Types.TIMESTAMP:
            setToPreparedStatementTimestamp(ps, index, value, column);
            return;
        default:
            throw new SQLException("Unhandled JDBC type: "
                    + column.getJdbcType());
        }
    }

    @Override
    @SuppressWarnings("boxing")
    public Serializable getFromResultSet(ResultSet rs, int index, Column column)
            throws SQLException {
        switch (column.getJdbcType()) {
        case Types.VARCHAR:
        case Types.CLOB:
            return getFromResultSetString(rs, index, column);
        case Types.BIT:
            return rs.getBoolean(index);
        case Types.TINYINT:
        case Types.INTEGER:
        case Types.BIGINT:
            return rs.getLong(index);
        case Types.DOUBLE:
            return rs.getDouble(index);
        case Types.TIMESTAMP:
            return getFromResultSetTimestamp(rs, index, column);
        }
        throw new SQLException("Unhandled JDBC type: " + column.getJdbcType());
    }

    @Override
    public boolean getMaterializeFulltextSyntheticColumn() {
        return false;
    }

    @Override
    public int getFulltextIndexedColumns() {
        return 2;
    }

    @Override
    public boolean supportsMultipleFulltextIndexes() {
        // With SQL Server, only one full-text index is allowed per table...
        return false;
    }

    @Override
    public String getCreateFulltextIndexSql(String indexName,
            String quotedIndexName, Table table, List<Column> columns,
            Model model) {
        StringBuilder buf = new StringBuilder();
        buf.append(String.format("CREATE FULLTEXT INDEX ON %s (",
                table.getQuotedName()));
        Iterator<Column> it = columns.iterator();
        while (it.hasNext()) {
            buf.append(String.format("%s LANGUAGE %s",
                    it.next().getQuotedName(), getQuotedFulltextAnalyzer()));
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
        String fulltextUniqueIndex = "[fulltext_pk]";
        buf.append(String.format(") KEY INDEX %s ON [%s]", fulltextUniqueIndex,
                fulltextCatalog));
        return buf.toString();
    }

    @Override
    public String getDialectFulltextQuery(String query) {
        query = query.replace("*", "%");
        FulltextQuery ft = analyzeFulltextQuery(query);
        if (ft == null) {
            return "DONTMATCHANYTHINGFOREMPTYQUERY";
        }
        return translateFulltext(ft, "OR", "AND", "AND NOT", "\"");
    }

    // SELECT ..., FTTBL.RANK / 1000.0
    // FROM ... LEFT JOIN [fulltext] ON [fulltext].[id] = [hierarchy].[id]
    // ........ LEFT JOIN CONTAINSTABLE([fulltext], *, ?, LANGUAGE 'english')
    // .................. AS FTTBL
    // .................. ON [fulltext].[id] = FTTBL.[KEY]
    // WHERE ... AND FTTBL.[KEY] IS NOT NULL
    // ORDER BY FTTBL.RANK DESC
    @Override
    public FulltextMatchInfo getFulltextScoredMatchInfo(String fulltextQuery,
            String indexName, int nthMatch, Column mainColumn, Model model,
            Database database) {
        // TODO multiple indexes
        Table ft = database.getTable(model.FULLTEXT_TABLE_NAME);
        Column ftMain = ft.getColumn(model.MAIN_KEY);
        String nthSuffix = nthMatch == 1 ? "" : String.valueOf(nthMatch);
        String tableAlias = "_nxfttbl" + nthSuffix;
        String scoreAlias = "_nxscore" + nthSuffix;
        FulltextMatchInfo info = new FulltextMatchInfo();
        // there are two left joins here
        info.joins = new ArrayList<Join>();
        if (nthMatch == 1) {
            // Need only one JOIN involving the fulltext table
            info.joins.add(new Join(Join.LEFT, ft.getQuotedName(), null, null,
                    ftMain.getFullQuotedName(), mainColumn.getFullQuotedName()));
        }
        info.joins.add(new Join(
                Join.LEFT, //
                String.format("CONTAINSTABLE(%s, *, ?, LANGUAGE %s)",
                        ft.getQuotedName(), getQuotedFulltextAnalyzer()),
                tableAlias, // alias
                fulltextQuery, // param
                ftMain.getFullQuotedName(), // on1
                String.format("%s.[KEY]", tableAlias) // on2
        ));
        info.whereExpr = String.format("%s.[KEY] IS NOT NULL", tableAlias);
        info.scoreExpr = String.format("%s.RANK / 1000.0 AS %s", tableAlias,
                scoreAlias);
        info.scoreAlias = scoreAlias;
        info.scoreCol = new Column(mainColumn.getTable(), null,
                ColumnType.DOUBLE, null);
        return info;
    }

    protected String getQuotedFulltextAnalyzer() {
        if (!Character.isDigit(fulltextAnalyzer.charAt(0))) {
            return String.format("'%s'", fulltextAnalyzer);
        }
        return fulltextAnalyzer;
    }

    @Override
    public boolean supportsCircularCascadeDeleteConstraints() {
        return false;
    }

    @Override
    public boolean supportsUpdateFrom() {
        return true;
    }

    @Override
    public boolean doesUpdateFromRepeatSelf() {
        return true;
    }

    @Override
    public boolean needsAliasForDerivedTable() {
        return true;
    }

    @Override
    public boolean needsOriginalColumnInGroupBy() {
        // http://msdn.microsoft.com/en-us/library/ms177673.aspx
        // A column alias that is defined in the SELECT list cannot be used to
        // specify a grouping column.
        return true;
    }

    @Override
    public String getSecurityCheckSql(String idColumnName) {
        return String.format("dbo.NX_ACCESS_ALLOWED(%s, ?, ?) = 1",
                idColumnName);
    }

    @Override
    public String getInTreeSql(String idColumnName) {
        return String.format("dbo.NX_IN_TREE(%s, ?) = 1", idColumnName);
    }

    @Override
    public String getSQLStatementsFilename() {
        return "resources/nuxeovcs/sqlserver.sql.txt";
    }

    @Override
    public String getTestSQLStatementsFilename() {
        return "resources/nuxeovcs/sqlserver.test.sql.txt";
    }

    @Override
    public Map<String, Serializable> getSQLStatementsProperties(Model model,
            Database database) {
        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        properties.put("idType", "VARCHAR(36)");
        properties.put("fulltextEnabled", Boolean.valueOf(!fulltextDisabled));
        properties.put("fulltextCatalog", fulltextCatalog);
        return properties;
    }

    @Override
    public boolean isClusteringSupported() {
        return true;
    }

    @Override
    public String getClusterInsertInvalidations() {
        return "EXEC dbo.NX_CLUSTER_INVAL ?, ?, ?";
    }

    @Override
    public String getClusterGetInvalidations() {
        return "DELETE I OUTPUT DELETED.[id], DELETED.[fragments], DELETED.[kind] "
                + "FROM [cluster_invals] AS I WHERE I.[nodeid] = @@SPID";
    }

    @Override
    public boolean isConnectionClosedException(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        if (t instanceof SocketException) {
            return true;
        }
        // java.sql.SQLException: Invalid state, the Connection object is
        // closed.
        String message = t.getMessage();
        if (message.contains("the Connection object is closed")) {
            return true;
        }
        return false;
    }

}
