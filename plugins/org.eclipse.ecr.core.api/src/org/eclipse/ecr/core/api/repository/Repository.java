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

package org.eclipse.ecr.core.api.repository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecr.common.xmap.annotation.XNode;
import org.eclipse.ecr.common.xmap.annotation.XNodeMap;
import org.eclipse.ecr.common.xmap.annotation.XObject;
import org.eclipse.ecr.core.api.ClientException;
import org.eclipse.ecr.core.api.CoreInstance;
import org.eclipse.ecr.core.api.CoreSession;
import org.eclipse.ecr.runtime.api.Framework;
import org.eclipse.ecr.runtime.api.ServiceGroup;
import org.eclipse.ecr.runtime.api.ServiceManager;


/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@XObject("repository")
public class Repository implements Serializable {

    private static final long serialVersionUID = -5884097487266847648L;

    @XNode("@repositoryUri")
    private String repositoryUri;

    @XNode("@isDefault")
    private boolean isDefault;

    @XNode("@name")
    private String name;

    @XNode("@group")
    private String group;

    @XNode("@label")
    private String label;

    @XNodeMap(value = "property", key = "@name", type = HashMap.class, componentType = String.class)
    private Map<String, String> properties;

    @XNode("@supportsTags")
    protected Boolean supportsTags=null;

    public Repository() {
    }

    public Repository(String name, String label) {
        this.name = name;
        this.label = label;
        properties = new HashMap<String, String>();
    }

    public Repository(String name) {
        this(name, name);
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public String getProperty(String name, String defValue) {
        String val = properties.get(name);
        if (val == null) {
            return defValue;
        }
        return val;
    }

    public String removeProperty(String name) {
        return properties.remove(name);
    }

    public String setProperty(String name, String value) {
        return properties.put(name, value);
    }


    public CoreSession open() throws Exception {
        return open(new HashMap<String, Serializable>());
    }

    protected CoreSession lookupSession() throws Exception {
        CoreSession session;
        if (group != null) {
            ServiceManager mgr = Framework.getLocalService(ServiceManager.class);
            ServiceGroup sg = mgr.getGroup(group);
            if (sg == null) {
                // TODO maybe throw other exception
                throw new ClientException("group '" + group + "' not defined");
            }
            session = sg.getService(CoreSession.class, name);
        } else {
            session = Framework.getService(CoreSession.class, name);
        }
        return session;
    }

    public boolean supportsTags() throws Exception {
        if (supportsTags==null) {
            CoreSession unconnectedSession =lookupSession();
            supportsTags =  unconnectedSession.supportsTags(name);
            // avoid leaking DocumentManagerBean
            unconnectedSession.destroy();
        }
        return supportsTags;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean value) {
        this.isDefault = value;
    }

    public CoreSession open(Map<String, Serializable> context) throws Exception {
        CoreSession session = lookupSession();
        if (repositoryUri == null) {
            repositoryUri = name;
        }
        session.connect(repositoryUri, context);
        return session;
    }

    public static void close(CoreSession session) {
        CoreInstance.getInstance().close(session);
    }

    public static RepositoryInstance newRepositoryInstance(Repository repository) {
        return new RepositoryInstanceHandler(repository).getProxy();
    }

    public RepositoryInstance newInstance() {
        return newRepositoryInstance(this);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(Repository.class.getSimpleName());
        buf.append(" {name=").append(name);
        buf.append(", label=").append(label);
        buf.append('}');

        return buf.toString();
    }

    public String getRepositoryUri() {
        return repositoryUri;
    }

}
