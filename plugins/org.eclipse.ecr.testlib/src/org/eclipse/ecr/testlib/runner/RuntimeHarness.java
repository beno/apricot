/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bogdan Stefanescu
 *     Damien Metzler (Leroy Merlin, http://www.leroymerlin.fr/)
 */
package org.eclipse.ecr.testlib.runner;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.nuxeo.osgi.OSGiAdapter;
import org.eclipse.ecr.runtime.model.RuntimeContext;
import org.eclipse.ecr.testlib.WorkingDirectoryConfigurator;

/**
 * TODO: Move this to org.nuxeo.runtime package
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public interface RuntimeHarness {

    /**
     * Gets the framework working directory.
     */
    File getWorkingDir();

    /**
     * Fires the event {@code FrameworkEvent.STARTED}.
     */
    void fireFrameworkStarted() throws Exception;

    /**
     * Deploys a whole OSGI bundle.
     * <p>
     * The lookup is first done on symbolic name, as set in <code>MANIFEST.MF</code>
     * and then falls back to the bundle url (e.g., <code>nuxeo-platform-search-api</code>)
     * for backwards compatibility.
     *
     * @param bundle the symbolic name
     */
    void deployBundle(String bundle) throws Exception;

    /**
     * Undeploys a contribution from a given bundle.
     * <p>
     * The path will be relative to the bundle root.
     * Example:
     * <code>
     * undeployContrib("org.nuxeo.ecm.core", "OSGI-INF/CoreExtensions.xml")
     * </code>
     *
     * @param bundle the bundle
     * @param contrib the contribution
     */
    void undeployContrib(String bundle, String contrib) throws Exception;

    /**
     * @deprecated use {@link #undeployContrib(String, String)} instead
     */
    @Deprecated
    void undeployContrib(String contrib);

    /**
     * @deprecated use {@link #undeployContrib(String, String)} instead
     */
    @Deprecated
    void undeploy(String contrib);

    RuntimeContext deployTestContrib(String bundle, URL contrib) throws Exception;

    /**
     * Deploys an XML contribution from outside a bundle.
     * <p>
     * This should be used by tests
     * wiling to deploy test contribution as part of a real bundle.
     * <p>
     * The bundle owner is important since the contribution may depend on resources
     * deployed in that bundle.
     * <p>
     * Note that the owner bundle MUST be an already deployed bundle.
     *
     * @param bundle the bundle that becomes the contribution owner
     * @param contrib the contribution to deploy as part of the given bundle
     */
    RuntimeContext deployTestContrib(String bundle, String contrib) throws Exception;

    /**
     * Deploys a contribution from a given bundle.
     * <p>
     * The path will be relative to the bundle root.
     * Example:
     * <code>
     * deployContrib("org.nuxeo.ecm.core", "OSGI-INF/CoreExtensions.xml")
     * </code>
     * <p>
     * For compatibility reasons the name of the bundle may be a jar name, but
     * this use is discouraged and deprecated.
     *
     * @param bundle the name of the bundle to peek the contrib in
     * @param contrib the path to contrib in the bundle.
     */
    void deployContrib(String bundle, String contrib) throws Exception;

    /**
     * Deploys a contribution file by looking for it in the class loader.
     * <p>
     * The first contribution file found by the class loader will be used.
     * You have no guarantee in case of name collisions.
     *
     * @deprecated use the less ambiguous {@link #deployContrib(String, String)}
     * @param contrib the relative path to the contribution file
     */
    @Deprecated
    void deployContrib(String contrib);

    /**
     * @deprecated use <code>deployContrib()</code> instead
     */
    @Deprecated
    void deploy(String contrib);

    void start() throws Exception;

    void stop() throws Exception;

    boolean isStarted();

    void deployFolder(File folder, ClassLoader loader) throws Exception;

    void addWorkingDirectoryConfigurator(WorkingDirectoryConfigurator config);

    /**
     *
     * Framework properties for variable injections
     *
     * @since 5.4.2
     * @return
     */
    Properties getProperties();

    /**
     *
     * Runtime context for deployment
     *
     * @since 5.4.2
     * @return
     */
    RuntimeContext getContext();

    /**
     *
     * OSGI bridge
     *
     * @since 5.4.2
     * @return
     */
    OSGiAdapter getOSGiAdapter();
}
