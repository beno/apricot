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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ecr.core.storage.StorageException;
import org.eclipse.ecr.core.storage.sql.Invalidations;
import org.eclipse.ecr.core.storage.sql.InvalidationsPropagator;
import org.eclipse.ecr.core.storage.sql.Mapper;
import org.eclipse.ecr.core.storage.sql.RepositoryDescriptor;

/**
 * Encapsulates cluster node operations.
 * <p>
 * There is one cluster node handler per cluster node (repository).
 */
public class ClusterNodeHandler {

    private static final Log log = LogFactory.getLog(ClusterNodeHandler.class);

    /** Cluster node mapper. Used synchronized. */
    private final Mapper clusterNodeMapper;

    private final long clusteringDelay;

    // modified only under clusterMapper synchronization
    private long clusterNodeLastInvalidationTimeMillis;

    /** Propagator of invalidations to the cluster node's mappers. */
    public final InvalidationsPropagator propagator;

    public ClusterNodeHandler(Mapper clusterNodeMapper,
            RepositoryDescriptor repositoryDescriptor) throws StorageException {
        this.clusterNodeMapper = clusterNodeMapper;
        clusterNodeMapper.createClusterNode();
        clusteringDelay = repositoryDescriptor.clusteringDelay;
        processClusterInvalidationsNext();
        propagator = new InvalidationsPropagator();
    }

    public void close() throws StorageException {
        synchronized (clusterNodeMapper) {
            try {
                clusterNodeMapper.removeClusterNode();
            } catch (StorageException e) {
                log.error(e.getMessage(), e);
            }
            clusterNodeMapper.close();
        }
    }

    // TODO should be called by RepositoryManagement
    public void processClusterInvalidationsNext() {
        clusterNodeLastInvalidationTimeMillis = System.currentTimeMillis()
                - clusteringDelay - 1;
    }

    /**
     * Receives cluster invalidations from other cluster nodes.
     */
    public Invalidations receiveClusterInvalidations()
            throws StorageException {
        synchronized (clusterNodeMapper) {
            if (clusterNodeLastInvalidationTimeMillis + clusteringDelay > System.currentTimeMillis()) {
                // delay hasn't expired
                return null;
            }
            Invalidations invalidations = clusterNodeMapper.getClusterInvalidations();
            clusterNodeLastInvalidationTimeMillis = System.currentTimeMillis();
            return invalidations;
        }
    }

    /**
     * Sends cluster invalidations to other cluster nodes.
     */
    public void sendClusterInvalidations(Invalidations invalidations)
            throws StorageException {
        if (invalidations == null || invalidations.isEmpty()) {
            return;
        }
        synchronized (clusterNodeMapper) {
            clusterNodeMapper.insertClusterInvalidations(invalidations);
        }
    }

}
