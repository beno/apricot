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

package org.eclipse.ecr.runtime.api;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class to look up {@link DataSource}s without having to deal with
 * vendor-specific JNDI prefixes.
 *
 * @author Thierry Delprat
 * @author Florent Guillaume
 */
public class DataSourceHelper {

    private DataSourceHelper() {
    }

    private static final Log log = LogFactory.getLog(DataSourceHelper.class);

    public static final String PREFIX_PROPERTY = "org.eclipse.ecr.runtime.datasource.prefix";

    public static final String DEFAULT_PREFIX = "java:comp/env/jdbc";

    protected static String prefix;

    public static void autodetectPrefix() {
        Context ctx = InitialContextAccessor.getInitialContext();
        String name = ctx == null ? null : ctx.getClass().getName();
        if ("org.eclipse.ecr.runtime.jtajca.NamingContext".equals(name)) { // Nuxeo-Embedded
            prefix = DEFAULT_PREFIX;
        } else if ("org.jnp.interfaces.NamingContext".equals(name)) { // JBoss
            prefix = "java:";
        } else if ("org.apache.naming.SelectorContext".equals(name)) { // Tomcat
            prefix = DEFAULT_PREFIX;
        } else if ("org.mortbay.naming.local.localContextRoot".equals(name)) { // Jetty
            prefix = "jdbc";
        } else if ("com.sun.enterprise.naming.impl.SerialContext".equals(name)) { // GlassFish
            prefix = DEFAULT_PREFIX;
        } else {
            // unknown, use Java EE standard
            log.error("Unknown JNDI Context class: " + name);
            prefix = DEFAULT_PREFIX;
        }
        log.info("Using JDBC JNDI prefix: " + prefix);
    }



    /**
     * Get the JNDI prefix used for DataSource lookups.
     */
    public static String getDataSourceJNDIPrefix() {
        if (prefix == null) {
            if (Framework.isInitialized()) {
                String configuredPrefix = Framework.getProperty(PREFIX_PROPERTY);
                if (configuredPrefix != null) {
                    prefix = configuredPrefix;
                } else {
                    autodetectPrefix();
                }
            } else {
                prefix = DEFAULT_PREFIX;
            }
        }
        return prefix;
    }

    /**
     * Look up a datasource JNDI name given a partial name.
     * <p>
     * For a datasource {@code "jdbc/foo"}, then it's sufficient to pass {@code
     * "foo"} to this method.
     *
     * @param partialName the partial name
     * @return the datasource JNDI name
     */
    public static String getDataSourceJNDIName(String partialName) {
        String targetPrefix = getDataSourceJNDIPrefix();
        // keep suffix only (jdbc/foo -> foo)
        int idx = partialName.lastIndexOf("/");
        if (idx > 0) {
            partialName = partialName.substring(idx + 1);
        }
        // add prefix
        return targetPrefix + "/" + partialName;
    }

    /**
     * Look up a datasource given a partial name.
     * <p>
     * For a datasource {@code "jdbc/foo"}, then it's sufficient to pass {@code
     * "foo"} to this method.
     *
     * @param partialName the partial name
     * @return the datasource
     * @throws NamingException
     */
    public static DataSource getDataSource(String partialName)
            throws NamingException {
        String jndiName = getDataSourceJNDIName(partialName);
        InitialContext context = new InitialContext();
        Object resolved = context.lookup(jndiName);
        if (resolved instanceof Reference) {
            try {
                resolved = NamingManager.getObjectInstance(resolved,  new CompositeName(jndiName), context, null);
            } catch (Exception e) {
                throw new Error("Cannot get access to " + jndiName, e);
            }
        }
        return (DataSource)resolved;
    }

}
