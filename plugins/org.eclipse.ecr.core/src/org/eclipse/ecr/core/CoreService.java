/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bogdan Stefanescu
 *     Florent Guillaume
 */

package org.eclipse.ecr.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ecr.core.api.CoreInstance;
import org.eclipse.ecr.core.api.CoreSessionFactory;
import org.eclipse.ecr.core.versioning.DefaultVersionRemovalPolicy;
import org.eclipse.ecr.core.versioning.VersionRemovalPolicy;
import org.eclipse.ecr.runtime.model.ComponentContext;
import org.eclipse.ecr.runtime.model.DefaultComponent;
import org.eclipse.ecr.runtime.model.Extension;

/**
 * Service used to register session factories and version removal policies.
 *
 * @author Bogdan Stefanescu
 * @author Florent Guillaume
 */
public class CoreService extends DefaultComponent {

    private static final Log log = LogFactory.getLog(CoreService.class);

    private ComponentContext context;

    private VersionRemovalPolicy versionRemovalPolicy;

    public VersionRemovalPolicy getVersionRemovalPolicy() {
        if (versionRemovalPolicy == null) {
            versionRemovalPolicy = new DefaultVersionRemovalPolicy();
        }
        return versionRemovalPolicy;
    }

    @Override
    public void activate(ComponentContext context) {
        this.context = context;
    }

    @Override
    public void deactivate(ComponentContext context) {
        this.context = null;
    }

    @Override
    public void registerExtension(Extension extension) {
        String point = extension.getExtensionPoint();
        if ("sessionFactory".equals(point)) {
            for (Object contrib : extension.getContributions()) {
                if (contrib instanceof CoreServiceFactoryDescriptor) {
                    registerSessionFactory((CoreServiceFactoryDescriptor) contrib);
                } else {
                    log.error("Invalid contribution to extension point 'sessionFactory': " +
                            contrib.getClass().getName());
                }
            }
        } else if ("versionRemovalPolicy".equals(point)) {
            for (Object contrib : extension.getContributions()) {
                if (contrib instanceof CoreServicePolicyDescriptor) {
                    registerVersionRemovalPolicy((CoreServicePolicyDescriptor) contrib);
                } else {
                    log.error("Invalid contribution to extension point 'versionRemovalPolicy': " +
                            contrib.getClass().getName());
                }
            }
        } else {
            log.error("Unknown extension point: " + point);
        }
    }

    @Override
    public void unregisterExtension(Extension extension) {
    }

    protected void registerSessionFactory(CoreServiceFactoryDescriptor desc) {
        String klass = desc.getKlass();
        try {
            CoreSessionFactory factory = (CoreSessionFactory) context.getRuntimeContext().loadClass(
                    klass).newInstance();
            CoreInstance.getInstance().initialize(factory);
        } catch (Exception e) {
            log.error("Failed to instantiate sessionFactory: " + klass, e);
        }
    }

    private void registerVersionRemovalPolicy(CoreServicePolicyDescriptor desc) {
        String klass = desc.getKlass();
        try {
            versionRemovalPolicy = (VersionRemovalPolicy) context.getRuntimeContext().loadClass(
                    klass).newInstance();
        } catch (Exception e) {
            log.error("Failed to instantiate versionRemovalPolicy: " + klass, e);
        }
    }

}
