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
package org.eclipse.ecr.opencmis.impl.server;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ChangeEventInfo;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PolicyIdList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PolicyIdListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RenditionDataImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.eclipse.ecr.core.api.ClientException;
import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.api.model.PropertyException;
import org.eclipse.ecr.core.api.security.SecurityConstants;
import org.eclipse.ecr.opencmis.impl.util.ListUtils;
import org.nuxeo.common.utils.StringUtils;

/**
 * Nuxeo implementation of a CMIS {@link ObjectData}, backed by a
 * {@link DocumentModel}.
 */
public class NuxeoObjectData implements ObjectData {

    public static final String STREAM_ICON = "nx:icon";

    public DocumentModel doc;

    public boolean creation = false; // TODO

    private List<String> propertyIds;

    private Boolean includeAllowableActions;

    private IncludeRelationships includeRelationships;

    private String renditionFilter;

    private Boolean includePolicyIds;

    private Boolean includeAcl;

    private static final BindingsObjectFactory objectFactory = new BindingsObjectFactoryImpl();

    private TypeDefinition type;

    private static final int CACHE_MAX_SIZE = 10;

    private static final int DEFAULT_MAX_RENDITIONS = 20;

    /** Cache for Properties objects, which are expensive to create. */
    private Map<String, Properties> propertiesCache = new HashMap<String, Properties>();

    private CallContext callContext;

    public NuxeoObjectData(NuxeoCmisService service, DocumentModel doc,
            String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePolicyIds, Boolean includeAcl,
            ExtensionsData extension) {
        this.doc = doc;
        propertyIds = getPropertyIdsFromFilter(filter);
        this.includeAllowableActions = includeAllowableActions;
        this.includeRelationships = includeRelationships;
        this.renditionFilter = renditionFilter;
        this.includePolicyIds = includePolicyIds;
        this.includeAcl = includeAcl;
        type = service.repository.getTypeDefinition(NuxeoTypeHelper.mappedId(doc.getType()));
        callContext = service.callContext;
    }

    protected NuxeoObjectData(NuxeoCmisService service, DocumentModel doc) {
        this(service, doc, null, null, null, null, null, null, null);
    }

    public NuxeoObjectData(NuxeoCmisService service, DocumentModel doc,
            OperationContext context) {
        this(service, doc, context.getFilterString(),
                Boolean.valueOf(context.isIncludeAllowableActions()),
                context.getIncludeRelationships(),
                context.getRenditionFilterString(),
                Boolean.valueOf(context.isIncludePolicies()),
                Boolean.valueOf(context.isIncludeAcls()), null);
    }

    private static final String STAR = "*";

    protected static final List<String> STAR_FILTER = Collections.singletonList(STAR);

    protected static List<String> getPropertyIdsFromFilter(String filter) {
        if (filter == null || filter.length() == 0)
            return STAR_FILTER;
        else {
            List<String> ids = Arrays.asList(filter.split(",\\s*"));
            if (ids.contains(STAR)) {
                ids = STAR_FILTER;
            }
            return ids;
        }
    }

    @Override
    public String getId() {
        return doc.getId();
    }

    @Override
    public BaseTypeId getBaseTypeId() {
        return NuxeoTypeHelper.getBaseTypeId(doc);
    }

    public TypeDefinition getTypeDefinition() {
        return type;
    }

    @Override
    public Properties getProperties() {
        return getProperties(propertyIds);
    }

    protected Properties getProperties(List<String> propertyIds) {
        // for STAR_FILTER the key is equal to STAR (see limitCacheSize)
        String key = StringUtils.join(propertyIds, ',');
        Properties properties = propertiesCache.get(key);
        if (properties == null) {
            Map<String, PropertyDefinition<?>> propertyDefinitions = type.getPropertyDefinitions();
            int len = propertyIds == STAR_FILTER ? propertyDefinitions.size()
                    : propertyIds.size();
            List<PropertyData<?>> props = new ArrayList<PropertyData<?>>(len);
            for (PropertyDefinition<?> pd : propertyDefinitions.values()) {
                if (propertyIds == STAR_FILTER
                        || propertyIds.contains(pd.getId())) {
                    props.add((PropertyData<?>) NuxeoPropertyData.construct(
                            this, pd, callContext));
                }
            }
            properties = objectFactory.createPropertiesData(props);
            limitCacheSize();
            propertiesCache.put(key, properties);
        }
        return properties;
    }

    /** Limits cache size, always keeps STAR filter. */
    protected void limitCacheSize() {
        if (propertiesCache.size() >= CACHE_MAX_SIZE) {
            Properties sf = propertiesCache.get(STAR);
            propertiesCache.clear();
            if (sf != null) {
                propertiesCache.put(STAR, sf);
            }
        }
    }

    public NuxeoPropertyDataBase<?> getProperty(String id) {
        // make use of cache
        return (NuxeoPropertyDataBase<?>) getProperties(STAR_FILTER).getProperties().get(
                id);
    }

    @Override
    public AllowableActions getAllowableActions() {
        if (!Boolean.TRUE.equals(includeAllowableActions)) {
            return null;
        }
        return getAllowableActions(doc, creation);
    }

    public static AllowableActions getAllowableActions(DocumentModel doc,
            boolean creation) {
        BaseTypeId baseType = NuxeoTypeHelper.getBaseTypeId(doc);
        boolean isDocument = baseType == BaseTypeId.CMIS_DOCUMENT;
        boolean isFolder = baseType == BaseTypeId.CMIS_FOLDER;
        boolean canWrite;
        try {
            canWrite = creation
                    || doc.getCoreSession().hasPermission(doc.getRef(),
                            SecurityConstants.WRITE);
        } catch (ClientException e) {
            canWrite = false;
        }

        Set<Action> set = EnumSet.noneOf(Action.class);
        set.add(Action.CAN_GET_OBJECT_PARENTS);
        set.add(Action.CAN_GET_PROPERTIES);
        if (isFolder) {
            set.add(Action.CAN_GET_DESCENDANTS);
            set.add(Action.CAN_GET_FOLDER_PARENT);
            set.add(Action.CAN_GET_FOLDER_TREE);
            set.add(Action.CAN_GET_CHILDREN);
        } else if (isDocument) {
            set.add(Action.CAN_GET_CONTENT_STREAM);
            set.add(Action.CAN_GET_ALL_VERSIONS);
            try {
                if (doc.isCheckedOut()) {
                    set.add(Action.CAN_CHECK_IN);
                    set.add(Action.CAN_CANCEL_CHECK_OUT);
                } else {
                    set.add(Action.CAN_CHECK_OUT);
                }
            } catch (ClientException e) {
                throw new CmisRuntimeException(e.toString(), e);
            }
        }
        if (isFolder || isDocument) {
            set.add(Action.CAN_GET_RENDITIONS);
        }
        if (canWrite) {
            if (isFolder) {
                set.add(Action.CAN_CREATE_DOCUMENT);
                set.add(Action.CAN_CREATE_FOLDER);
                set.add(Action.CAN_CREATE_RELATIONSHIP);
                set.add(Action.CAN_DELETE_TREE);
                set.add(Action.CAN_ADD_OBJECT_TO_FOLDER);
                set.add(Action.CAN_REMOVE_OBJECT_FROM_FOLDER);
            } else if (isDocument) {
                set.add(Action.CAN_SET_CONTENT_STREAM);
                set.add(Action.CAN_DELETE_CONTENT_STREAM);
            }
            set.add(Action.CAN_UPDATE_PROPERTIES);
            if (isFolder || isDocument) {
                // Relationships are not fileable
                set.add(Action.CAN_MOVE_OBJECT);
            }
            set.add(Action.CAN_DELETE_OBJECT);
        }
        if (Boolean.FALSE.booleanValue()) {
            // TODO
            set.add(Action.CAN_GET_OBJECT_RELATIONSHIPS);
            set.add(Action.CAN_APPLY_POLICY);
            set.add(Action.CAN_REMOVE_POLICY);
            set.add(Action.CAN_GET_APPLIED_POLICIES);
            set.add(Action.CAN_GET_ACL);
            set.add(Action.CAN_APPLY_ACL);
        }

        AllowableActionsImpl aa = new AllowableActionsImpl();
        aa.setAllowableActions(set);
        return aa;
    }

    @Override
    public List<RenditionData> getRenditions() {
        if (renditionFilter == null || renditionFilter.length() == 0) {
            return null;
        }
        // TODO parse rendition filter; for now returns them all
        return getRenditions(doc, null, null, callContext);
    }

    public static List<RenditionData> getRenditions(DocumentModel doc,
            BigInteger maxItems, BigInteger skipCount, CallContext callContext) {
        try {
            List<RenditionData> list = new ArrayList<RenditionData>();
            // first rendition is icon
            String iconPath;
            try {
                iconPath = (String) doc.getPropertyValue(NuxeoTypeHelper.NX_ICON);
            } catch (PropertyException e) {
                iconPath = null;
            }
            InputStream is = getIconStream(iconPath, callContext);
            if (is != null) {
                RenditionDataImpl ren = new RenditionDataImpl();
                ren.setStreamId(STREAM_ICON);
                ren.setMimeType(getIconMimeType(iconPath));
                ren.setKind("cmis:thumbnail");
                int slash = iconPath.lastIndexOf('/');
                String filename = slash == -1 ? iconPath
                        : iconPath.substring(slash + 1);
                ren.setTitle(filename);
                long len = getStreamLength(is);
                ren.setBigLength(BigInteger.valueOf(len));
                // TODO width, height
                // ren.setBigWidth(width);
                // ren.setBigHeight(height);
                list.add(ren);
            }

            // TODO other renditions from blob holder secondary blobs
            list = ListUtils.batchList(list, maxItems, skipCount, DEFAULT_MAX_RENDITIONS);
            return list;
        } catch (IOException e) {
            throw new CmisRuntimeException(e.toString(), e);
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }

    }

    public static InputStream getIconStream(String iconPath, CallContext context)
            throws ClientException {
        if (iconPath == null || iconPath.length() == 0) {
            return null;
        }
        if (!iconPath.startsWith("/")) {
            iconPath = '/' + iconPath;
        }
        ServletContext servletContext = (ServletContext) context.get(CallContext.SERVLET_CONTEXT);
        if (servletContext == null) {
            throw new CmisRuntimeException("Cannot get servlet context");
        }
        return servletContext.getResourceAsStream(iconPath);
    }

    public static long getStreamLength(InputStream is) throws IOException {
        byte[] buf = new byte[4096];
        long count = 0;
        int n;
        while ((n = is.read(buf)) != -1) {
            count += n;
        }
        is.close();
        return count;
    }

    protected static String getIconMimeType(String iconPath) {
        iconPath = iconPath.toLowerCase();
        if (iconPath.endsWith(".gif")) {
            return "image/gif";
        } else if (iconPath.endsWith(".png")) {
            return "image/png";
        } else if (iconPath.endsWith(".jpg") || iconPath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            // TODO use NXMimeType service
            return "application/octet-stream";
        }
    }

    @Override
    public List<ObjectData> getRelationships() {
        if (includeRelationships == null
                || includeRelationships == IncludeRelationships.NONE) {
            return null;
        }
        return new ArrayList<ObjectData>(0); // TODO
    }

    @Override
    public Acl getAcl() {
        if (!Boolean.TRUE.equals(includeAcl)) {
            return null;
        }
        AccessControlListImpl acl = new AccessControlListImpl();
        List<Ace> aces = new ArrayList<Ace>();
        acl.setAces(aces);
        return acl; // TODO
    }

    @Override
    public Boolean isExactAcl() {
        return Boolean.FALSE; // TODO
    }

    @Override
    public PolicyIdList getPolicyIds() {
        if (!Boolean.TRUE.equals(includePolicyIds)) {
            return null;
        }
        return new PolicyIdListImpl(); // TODO
    }

    @Override
    public ChangeEventInfo getChangeEventInfo() {
        return null;
        // throw new UnsupportedOperationException();
    }

    @Override
    public List<CmisExtensionElement> getExtensions() {
        return Collections.emptyList();
    }

    @Override
    public void setExtensions(List<CmisExtensionElement> extensions) {
    }

}
