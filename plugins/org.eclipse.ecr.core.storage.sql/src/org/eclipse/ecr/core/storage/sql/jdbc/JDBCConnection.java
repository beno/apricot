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
package org.eclipse.ecr.core.storage.sql.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.eclipse.ecr.core.storage.StorageException;
import org.eclipse.ecr.core.storage.sql.Model;
import org.eclipse.ecr.core.storage.sql.Mapper.Identification;

/**
 * Holds a connection to a JDBC database.
 */
public class JDBCConnection {

    /** The model used to do the mapping. */
    protected final Model model;

    /** The SQL information. */
    protected final SQLInfo sqlInfo;

    /** The xa datasource. */
    protected final XADataSource xadatasource;

    /** The xa pooled connection. */
    private XAConnection xaconnection;

    /** The actual connection. */
    public Connection connection;

    protected XAResource xaresource;

    protected final JDBCConnectionPropagator connectionPropagator;

    /** If there's a chance the connection may be closed. */
    protected volatile boolean checkConnectionValid;

    // for debug
    private static final AtomicLong instanceCounter = new AtomicLong(0);

    // for debug
    private final long instanceNumber = instanceCounter.incrementAndGet();

    // for debug
    public final JDBCLogger logger = new JDBCLogger(
            String.valueOf(instanceNumber));

    /**
     * Creates a new Mapper.
     *
     * @param model the model
     * @param sqlInfo the sql info
     * @param xadatasource the XA datasource to use to get connections
     */
    public JDBCConnection(Model model, SQLInfo sqlInfo,
            XADataSource xadatasource,
            JDBCConnectionPropagator connectionPropagator)
            throws StorageException {
        this.model = model;
        this.sqlInfo = sqlInfo;
        this.xadatasource = xadatasource;
        this.connectionPropagator = connectionPropagator;
        connectionPropagator.addConnection(this);
        open();
    }

    public Identification getIdentification() {
        return new Identification(null, "" + instanceNumber);
    }

    protected void open() throws StorageException {
        try {
            xaconnection = xadatasource.getXAConnection();
            connection = xaconnection.getConnection();
            xaresource = xaconnection.getXAResource();
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                // ignore, including UndeclaredThrowableException
            }
        }
        if (xaconnection != null) {
            try {
                xaconnection.close();
            } catch (SQLException e) {
                // ignore
            }
        }
        xaconnection = null;
        connection = null;
        xaresource = null;
    }

    /**
     * Opens a new connection if the previous ones was broken or timed out.
     */
    protected void resetConnection() throws StorageException {
        logger.error("Resetting connection");
        close();
        open();
        // we had to reset a connection; notify all the others that they
        // should check their validity proactively
        connectionPropagator.checkConnectionValid(this);
    }

    /**
     * Checks that the connection is valid, and tries to reset it if not.
     */
    protected void checkConnectionValid() throws StorageException {
        if (checkConnectionValid) {
            doCheckConnectionValid();
            // only if there was no exception set the flag to false
            checkConnectionValid = false;
        }
    }

    protected void doCheckConnectionValid() throws StorageException {
        Statement st = null;
        try {
            st = connection.createStatement();
            st.execute(sqlInfo.dialect.getValidationQuery());
        } catch (Exception e) {
            if (sqlInfo.dialect.isConnectionClosedException(e)) {
                resetConnection();
            } else {
                throw new StorageException(e);
            }
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Checks the SQL error we got and determine if the low level connection has
     * to be reset.
     * <p>
     * Called with a generic Exception and not just SQLException because the
     * PostgreSQL JDBC driver sometimes fails to unwrap properly some
     * InvocationTargetException / UndeclaredThrowableException.
     */
    protected void checkConnectionReset(Throwable t) throws StorageException {
        if (sqlInfo.dialect.isConnectionClosedException(t)) {
            resetConnection();
        }
    }

    /**
     * Checks the XA error we got and determine if the low level connection has
     * to be reset.
     */
    protected void checkConnectionReset(XAException e) {
        if (sqlInfo.dialect.isConnectionClosedException(e)) {
            try {
                resetConnection();
            } catch (StorageException ee) {
                // swallow, exception already thrown by caller
            }
        }
    }

    protected void closeStatement(Statement s) throws SQLException {
        try {
            s.close();
        } catch (IllegalArgumentException e) {
            // ignore
            // http://bugs.mysql.com/35489 with JDBC 4 and driver <= 5.1.6
        }
    }

}
