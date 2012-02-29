/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
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

import static org.eclipse.ecr.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import static org.eclipse.ecr.core.api.event.DocumentEventTypes.DOCUMENT_REMOVED;
import static org.eclipse.ecr.core.api.event.DocumentEventTypes.DOCUMENT_UPDATED;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Converter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChangeEventInfoDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeContainer;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ecr.common.utils.Path;
import org.eclipse.ecr.core.api.Blob;
import org.eclipse.ecr.core.api.ClientException;
import org.eclipse.ecr.core.api.CoreSession;
import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.api.DocumentModelList;
import org.eclipse.ecr.core.api.DocumentRef;
import org.eclipse.ecr.core.api.Filter;
import org.eclipse.ecr.core.api.IdRef;
import org.eclipse.ecr.core.api.IterableQueryResult;
import org.eclipse.ecr.core.api.LifeCycleConstants;
import org.eclipse.ecr.core.api.PathRef;
import org.eclipse.ecr.core.api.VersioningOption;
import org.eclipse.ecr.core.api.impl.CompoundFilter;
import org.eclipse.ecr.core.api.impl.FacetFilter;
import org.eclipse.ecr.core.api.impl.LifeCycleFilter;
import org.eclipse.ecr.core.api.impl.blob.ByteArrayBlob;
import org.eclipse.ecr.core.api.model.PropertyException;
import org.eclipse.ecr.core.api.pathsegment.PathSegmentService;
import org.eclipse.ecr.core.api.repository.Repository;
import org.eclipse.ecr.core.api.repository.RepositoryManager;
import org.eclipse.ecr.core.schema.FacetNames;
import org.eclipse.ecr.opencmis.impl.util.ListUtils;
import org.eclipse.ecr.opencmis.impl.util.ListUtils.BatchedList;
import org.eclipse.ecr.opencmis.impl.util.SimpleImageInfo;
import org.eclipse.ecr.opencmis.impl.util.TypeManagerImpl;
import org.eclipse.ecr.platform.audit.api.AuditReader;
import org.eclipse.ecr.platform.audit.api.LogEntry;
import org.eclipse.ecr.runtime.api.Framework;

/**
 * Nuxeo implementation of the CMIS Services, on top of a {@link CoreSession}.
 */
public class NuxeoCmisService extends AbstractCmisService {

    public static final int DEFAULT_TYPE_LEVELS = 2;

    public static final int DEFAULT_FOLDER_LEVELS = 2;

    public static final int DEFAULT_CHANGE_LOG_SIZE = 100;

    public static final int DEFAULT_QUERY_SIZE = 100;

    public static final int DEFAULT_MAX_CHILDREN = 100;

    public static final int DEFAULT_MAX_RELATIONSHIPS = 100;

    public static final String NUXEO_WAR = "nuxeo.war";

    private static final Log log = LogFactory.getLog(NuxeoCmisService.class);

    protected final BindingsObjectFactory objectFactory = new BindingsObjectFactoryImpl();

    protected final NuxeoRepository repository;

    protected final CoreSession coreSession;

    /** When false, we don't own the core session and shouldn't close it. */
    protected final boolean coreSessionOwned;

    /** Filter that hides HiddenInNavigation and deleted objects. */
    protected final Filter documentFilter;

    protected final CallContext callContext;

    /** Constructor called by binding. */
    public NuxeoCmisService(NuxeoRepository repository, CallContext callContext) {
        this.repository = repository;
        this.callContext = callContext;
        this.coreSession = repository == null ? null
                : openCoreSession(repository.getId());
        coreSessionOwned = true;
        documentFilter = getDocumentFilter();
    }

    /** Constructor called by high-level session from existing core session. */
    public NuxeoCmisService(NuxeoRepository repository,
            CallContext callContext, CoreSession coreSession) {
        this.repository = repository;
        this.callContext = callContext;
        this.coreSession = coreSession;
        coreSessionOwned = false;
        documentFilter = getDocumentFilter();
    }

    // called in a finally block from dispatcher
    @Override
    public void close() {
        if (coreSession != null && coreSessionOwned) {
            closeCoreSession();
        }
        clearObjectInfos();
    }

    protected CoreSession openCoreSession(String repositoryId) {
        try {
            Repository repository = Framework.getService(
                    RepositoryManager.class).getRepository(repositoryId);
            if (repository == null) {
                throw new CmisRuntimeException("Cannot get repository: "
                        + repositoryId);
            }
            return repository.open();
        } catch (CmisRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    protected void closeCoreSession() {
        try {
            Repository.close(coreSession);
        } catch (Exception e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    public NuxeoRepository getNuxeoRepository() {
        return repository;
    }

    public CoreSession getCoreSession() {
        return coreSession;
    }

    public BindingsObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public CallContext getCallContext() {
        return callContext;
    }

    /** Gets the filter that hides HiddenInNavigation and deleted objects. */
    protected Filter getDocumentFilter() {
        Filter facetFilter = new FacetFilter(FacetNames.HIDDEN_IN_NAVIGATION,
                false);
        Filter lcFilter = new LifeCycleFilter(LifeCycleConstants.DELETED_STATE,
                false);
        return new CompoundFilter(facetFilter, lcFilter);
    }

    protected String getIdFromDocumentRef(DocumentRef ref)
            throws ClientException {
        if (ref instanceof IdRef) {
            return ((IdRef) ref).value;
        } else {
            return coreSession.getDocument(ref).getId();
        }
    }

    /* This is the only method that does not have a repositoryId / coreSession. */
    @Override
    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        List<NuxeoRepository> repos = NuxeoRepositories.getRepositories();
        List<RepositoryInfo> infos = new ArrayList<RepositoryInfo>(repos.size());
        for (NuxeoRepository repo : repos) {
            String latestChangeLogToken = getLatestChangeLogToken(repo.getId());
            infos.add(repo.getRepositoryInfo(latestChangeLogToken));
        }
        return infos;
    }

    @Override
    public RepositoryInfo getRepositoryInfo(String repositoryId,
            ExtensionsData extension) {
        String latestChangeLogToken = getLatestChangeLogToken(repositoryId);
        return NuxeoRepositories.getRepository(repositoryId).getRepositoryInfo(
                latestChangeLogToken);
    }

    @Override
    public TypeDefinition getTypeDefinition(String repositoryId, String typeId,
            ExtensionsData extension) {
        TypeDefinition type = repository.getTypeDefinition(typeId);
        if (type == null) {
            throw new CmisInvalidArgumentException("No such type: " + typeId);
        }
        // TODO copy only when local binding
        // clone
        return Converter.convert(Converter.convert(type));

    }

    @Override
    public TypeDefinitionList getTypeChildren(String repositoryId,
            String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        TypeDefinitionList types = repository.getTypeChildren(typeId,
                includePropertyDefinitions, maxItems, skipCount);
        // TODO copy only when local binding
        // clone
        return Converter.convert(Converter.convert(types));
    }

    @Override
    public List<TypeDefinitionContainer> getTypeDescendants(
            String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        int d = depth == null ? DEFAULT_TYPE_LEVELS : depth.intValue();
        List<TypeDefinitionContainer> types = repository.getTypeDescendants(
                typeId, d, includePropertyDefinitions);
        // clone
        // TODO copy only when local binding
        List<CmisTypeContainer> tmp = new ArrayList<CmisTypeContainer>(
                types.size());
        Converter.convertTypeContainerList(types, tmp);
        return Converter.convertTypeContainerList(tmp);
    }

    protected DocumentModel getDocumentModel(String id) {
        DocumentRef docRef = new IdRef(id);
        try {
            if (!coreSession.exists(docRef)) {
                throw new CmisObjectNotFoundException(docRef.toString());
            }
            DocumentModel doc = coreSession.getDocument(docRef);
            if (isFilteredOut(doc)) {
                throw new CmisObjectNotFoundException(docRef.toString());
            }
            return doc;
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public NuxeoObjectData getObject(String repositoryId, String objectId,
            String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePolicyIds, Boolean includeAcl,
            ExtensionsData extension) {
        DocumentModel doc = getDocumentModel(objectId);
        NuxeoObjectData data = new NuxeoObjectData(this, doc, filter,
                includeAllowableActions, includeRelationships, renditionFilter,
                includePolicyIds, includeAcl, extension);
        collectObjectInfo(repositoryId, data);
        return data;
    }

    /**
     * Checks if the doc should be ignored because it is "invisible" (deleted,
     * hidden in navigation).
     */
    public boolean isFilteredOut(DocumentModel doc) {
        return !documentFilter.accept(doc);
    }

    /** Creates bare unsaved document model. */
    protected DocumentModel createDocumentModel(ObjectId folder,
            TypeDefinition type) {
        DocumentModel doc;
        String typeId = type.getId();
        String nuxeoTypeId = type.getLocalName();
        if (BaseTypeId.CMIS_DOCUMENT.value().equals(typeId)) {
            nuxeoTypeId = NuxeoTypeHelper.NUXEO_FILE;
        } else if (BaseTypeId.CMIS_FOLDER.value().equals(typeId)) {
            nuxeoTypeId = NuxeoTypeHelper.NUXEO_FOLDER;
        } else if (BaseTypeId.CMIS_RELATIONSHIP.value().equals(typeId)) {
            nuxeoTypeId = NuxeoTypeHelper.NUXEO_RELATION;
        }
        try {
            doc = coreSession.createDocumentModel(nuxeoTypeId);
        } catch (ClientException e) {
            throw new IllegalArgumentException(typeId);
        }
        if (folder != null) {
            DocumentModel parentDoc;
            try {
                parentDoc = coreSession.getDocument(new IdRef(folder.getId()));
            } catch (ClientException e) {
                throw new CmisRuntimeException("Cannot create object", e);
            }
            String pathSegment = nuxeoTypeId; // default path segment based on
                                              // id
            doc.setPathInfo(parentDoc.getPathAsString(), pathSegment);
        }
        return doc;
    }

    /** Creates and save document model. */
    protected DocumentModel createDocumentModel(ObjectId folder,
            ContentStream contentStream, String name) {

        DocumentModel parent;
        try {
            parent = coreSession.getDocument(new IdRef(folder.getId()));
        } catch (ClientException e) {
            throw new CmisRuntimeException("Cannot create object", e);
        }
        String path = parent.getPathAsString();

        Blob blob;
        if (contentStream == null) {
            blob = new ByteArrayBlob(new byte[0], null, null, name, null);
        } else {
            try {
                blob = NuxeoPropertyData.getPersistentBlob(contentStream, null);
            } catch (IOException e) {
                throw new CmisRuntimeException(e.toString(), e);
            }
        }

        try {
            return BlobHelper.createDocumentFromBlob(coreSession, blob, path,
                    name);
        } catch (Exception e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    // create and save session
    protected NuxeoObjectData createObject(String repositoryId,
            Properties properties, ObjectId folder, BaseTypeId baseType,
            ContentStream contentStream) {
        String typeId;
        Map<String, PropertyData<?>> p;
        PropertyData<?> d;
        TypeDefinition type = null;
        if (properties != null //
                && (p = properties.getProperties()) != null //
                && (d = p.get(PropertyIds.OBJECT_TYPE_ID)) != null) {
            typeId = (String) d.getFirstValue();
            if (baseType == null) {
                type = repository.getTypeDefinition(typeId);
                if (type == null) {
                    throw new IllegalArgumentException(typeId);
                }
                baseType = type.getBaseTypeId();
            }
        } else {
            typeId = null;
        }
        if (typeId == null) {
            switch (baseType) {
            case CMIS_DOCUMENT:
                typeId = BaseTypeId.CMIS_DOCUMENT.value();
                break;
            case CMIS_FOLDER:
                typeId = BaseTypeId.CMIS_FOLDER.value();
                break;
            case CMIS_POLICY:
                throw new CmisRuntimeException("Cannot create policy");
            case CMIS_RELATIONSHIP:
                throw new CmisRuntimeException("Cannot create relationship");
            default:
                throw new CmisRuntimeException("No base type");
            }
        }
        if (type == null) {
            type = repository.getTypeDefinition(typeId);
        }
        if (type == null || type.getBaseTypeId() != baseType) {
            throw new CmisInvalidArgumentException(typeId);
        }
        if (type.isCreatable() == Boolean.FALSE) {
            throw new CmisInvalidArgumentException("Not creatable: " + typeId);
        }

        // name from properties
        PropertyData<?> npd = properties.getProperties().get(PropertyIds.NAME);
        String name = npd == null ? null : (String) npd.getFirstValue();
        if (StringUtils.isBlank(name)) {
            name = null;
        }

        // content stream filename default
        if (contentStream != null
                && StringUtils.isBlank(contentStream.getFileName())
                && name != null) {
            // infer filename from name property
            contentStream = new ContentStreamImpl(name,
                    contentStream.getBigLength(), contentStream.getMimeType(),
                    contentStream.getStream());
        }

        DocumentModel doc = null;
        if (BaseTypeId.CMIS_DOCUMENT.value().equals(typeId)) {
            doc = createDocumentModel(folder, contentStream, name);
        }
        boolean created = doc != null;
        if (!created) {
            doc = createDocumentModel(folder, type);
        }

        NuxeoObjectData data = new NuxeoObjectData(this, doc);
        updateProperties(data, null, properties, true);
        if (!created && contentStream != null) {
            try {
                NuxeoPropertyData.setContentStream(doc, contentStream, true);
            } catch (CmisContentAlreadyExistsException e) {
                // cannot happen, overwrite = true
            } catch (IOException e) {
                throw new CmisRuntimeException(e.toString(), e);
            }
        }
        try {
            if (!created) {
                // set path segment from properties (name/title)
                PathSegmentService pss = Framework.getLocalService(PathSegmentService.class);
                String pathSegment = pss.generatePathSegment(doc);
                Path path = doc.getPath();
                doc.setPathInfo(path == null ? null
                        : path.removeLastSegments(1).toString(), pathSegment);
                doc = coreSession.createDocument(doc);
            } else {
                doc = coreSession.saveDocument(doc);
            }
            data.doc = doc;
            coreSession.save();
        } catch (ClientException e) {
            throw new CmisRuntimeException("Cannot create", e);
        }
        collectObjectInfo(repositoryId, data);
        return data;
    }

    protected <T> void updateProperties(NuxeoObjectData object,
            String changeToken, Properties properties, boolean creation) {
        TypeDefinition type = object.getTypeDefinition();
        // TODO changeToken
        Map<String, PropertyData<?>> p;
        if (properties == null || (p = properties.getProperties()) == null) {
            return;
        }
        for (Entry<String, PropertyData<?>> en : p.entrySet()) {
            String key = en.getKey();
            PropertyData<?> d = en.getValue();
            setObjectProperty(object, key, d, type, creation);
        }
    }

    protected <T> void updateProperties(NuxeoObjectData object,
            String changeToken, Map<String, ?> properties, TypeDefinition type,
            boolean creation) {
        // TODO changeToken
        if (properties == null) {
            return;
        }
        for (Entry<String, ?> en : properties.entrySet()) {
            String key = en.getKey();
            Object value = en.getValue();
            @SuppressWarnings("unchecked")
            PropertyDefinition<T> pd = (PropertyDefinition<T>) type.getPropertyDefinitions().get(
                    key);
            if (pd == null) {
                throw new CmisRuntimeException("Unknown property: " + key);
            }
            setObjectProperty(object, key, value, pd, creation);
        }
    }

    protected <T> void setObjectProperty(NuxeoObjectData object, String key,
            PropertyData<T> d, TypeDefinition type, boolean creation) {
        @SuppressWarnings("unchecked")
        PropertyDefinition<T> pd = (PropertyDefinition<T>) type.getPropertyDefinitions().get(
                key);
        if (pd == null) {
            throw new CmisRuntimeException("Unknown property: " + key);
        }
        Object value;
        if (d == null) {
            value = null;
        } else if (pd.getCardinality() == Cardinality.SINGLE) {
            value = d.getFirstValue();
        } else {
            value = d.getValues();
        }
        setObjectProperty(object, key, value, pd, creation);
    }

    protected <T> void setObjectProperty(NuxeoObjectData object, String key,
            Object value, PropertyDefinition<T> pd, boolean creation) {
        Updatability updatability = pd.getUpdatability();
        if (updatability == Updatability.READONLY
                || (updatability == Updatability.ONCREATE && !creation)) {
            // log.error("Read-only property, ignored: " + key);
            return;
        }
        if (PropertyIds.OBJECT_TYPE_ID.equals(key)
                || PropertyIds.LAST_MODIFICATION_DATE.equals(key)) {
            return;
        }
        // TODO avoid constructing property object just to set value
        NuxeoPropertyDataBase<T> np = (NuxeoPropertyDataBase<T>) NuxeoPropertyData.construct(
                object, pd, callContext);
        np.setValue(value);
    }

    /** Sets initial versioning state and returns its id. */
    protected String setInitialVersioningState(NuxeoObjectData object,
            VersioningState versioningState) {
        try {
            if (versioningState == null) {
                // default is MAJOR, per spec
                versioningState = VersioningState.MAJOR;
            }
            String id;
            DocumentRef ref = null;
            switch (versioningState) {
            case NONE: // cannot be made non-versionable in Nuxeo
            case CHECKEDOUT:
                id = object.getId();
                break;
            case MINOR:
                ref = object.doc.checkIn(VersioningOption.MINOR, null);
                object.doc.getCoreSession().save();
                // id = ref.toString();
                id = object.getId();
                break;
            case MAJOR:
                ref = object.doc.checkIn(VersioningOption.MAJOR, null);
                object.doc.getCoreSession().save();
                // id = ref.toString();
                id = object.getId();
                break;
            default:
                throw new AssertionError(versioningState);
            }
            return id;
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public String create(String repositoryId, Properties properties,
            String folderId, ContentStream contentStream,
            VersioningState versioningState, List<String> policies,
            ExtensionsData extension) {
        // TODO policies
        NuxeoObjectData object = createObject(repositoryId, properties,
                new ObjectIdImpl(folderId), null, contentStream);
        return setInitialVersioningState(object, versioningState);
    }

    @Override
    public String createDocument(String repositoryId, Properties properties,
            String folderId, ContentStream contentStream,
            VersioningState versioningState, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        // TODO policies, addAces, removeAces
        NuxeoObjectData object = createObject(repositoryId, properties,
                new ObjectIdImpl(folderId), BaseTypeId.CMIS_DOCUMENT,
                contentStream);
        return setInitialVersioningState(object, versioningState);
    }

    @Override
    public String createFolder(String repositoryId, Properties properties,
            String folderId, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        // TODO policies, addAces, removeAces
        NuxeoObjectData object = createObject(repositoryId, properties,
                new ObjectIdImpl(folderId), BaseTypeId.CMIS_FOLDER, null);
        return object.getId();
    }

    @Override
    public String createPolicy(String repositoryId, Properties properties,
            String folderId, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        throw new CmisNotSupportedException();
    }

    @Override
    public String createRelationship(String repositoryId,
            Properties properties, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        NuxeoObjectData object = createObject(repositoryId, properties, null,
                BaseTypeId.CMIS_RELATIONSHIP, null);
        return object.getId();
    }

    @Override
    public String createDocumentFromSource(String repositoryId,
            String sourceId, Properties properties, String folderId,
            VersioningState versioningState, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        if (folderId == null) {
            // no unfileable objects for now
            throw new CmisInvalidArgumentException("Invalid null folder ID");
        }
        DocumentModel doc = getDocumentModel(sourceId);
        DocumentModel folder = getDocumentModel(folderId);
        try {
            DocumentModel copyDoc = coreSession.copy(doc.getRef(),
                    folder.getRef(), null);
            NuxeoObjectData copy = new NuxeoObjectData(this, copyDoc);
            if (properties != null && properties.getPropertyList() != null
                    && !properties.getPropertyList().isEmpty()) {
                updateProperties(copy, null, properties, false);
                copy.doc = coreSession.saveDocument(copyDoc);
            }
            coreSession.save();
            return setInitialVersioningState(copy, versioningState);
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    public NuxeoObjectData copy(String sourceId, String targetId,
            Map<String, ?> properties, TypeDefinition type,
            VersioningState versioningState, List<Policy> policies,
            List<Ace> addACEs, List<Ace> removeACEs, OperationContext context) {
        DocumentModel doc = getDocumentModel(sourceId);
        DocumentModel folder = getDocumentModel(targetId);
        try {
            DocumentModel copyDoc = coreSession.copy(doc.getRef(),
                    folder.getRef(), null);
            NuxeoObjectData copy = new NuxeoObjectData(this, copyDoc, context);
            if (properties != null && !properties.isEmpty()) {
                updateProperties(copy, null, properties, type, false);
                copy.doc = coreSession.saveDocument(copyDoc);
            }
            coreSession.save();
            String id = setInitialVersioningState(copy, versioningState);
            NuxeoObjectData res;
            if (id.equals(copy.getId())) {
                res = copy;
            } else {
                // return the version
                res = new NuxeoObjectData(this, getDocumentModel(id));
            }
            return res;
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public void deleteContentStream(String repositoryId,
            Holder<String> objectIdHolder, Holder<String> changeTokenHolder,
            ExtensionsData extension) {
        setContentStream(repositoryId, objectIdHolder, Boolean.TRUE,
                changeTokenHolder, null, extension);
    }

    @Override
    public FailedToDeleteData deleteTree(String repositoryId, String folderId,
            Boolean allVersions, UnfileObject unfileObjects,
            Boolean continueOnFailure, ExtensionsData extension) {
        if (unfileObjects == UnfileObject.UNFILE) {
            throw new CmisConstraintException("Unfiling not supported");
        }
        if (repository.getRootFolderId().equals(folderId)) {
            throw new CmisInvalidArgumentException("Cannot delete root");
        }
        try {
            DocumentModel doc = getDocumentModel(folderId);
            if (!doc.isFolder()) {
                throw new CmisInvalidArgumentException("Not a folder: "
                        + folderId);
            }
            coreSession.removeDocument(new IdRef(folderId));
            coreSession.save();
            // TODO returning null fails in opencmis 0.1.0 due to
            // org.apache.chemistry.opencmis.client.runtime.PersistentFolderImpl.deleteTree
            return new FailedToDeleteDataImpl();
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public AllowableActions getAllowableActions(String repositoryId,
            String objectId, ExtensionsData extension) {
        DocumentModel doc = getDocumentModel(objectId);
        return NuxeoObjectData.getAllowableActions(doc, false);
    }

    @Override
    public ContentStream getContentStream(String repositoryId, String objectId,
            String streamId, BigInteger offset, BigInteger length,
            ExtensionsData extension) {
        // TODO offset, length
        if (NuxeoObjectData.STREAM_ICON.equals(streamId)) {
            try {
                DocumentModel doc = getDocumentModel(objectId);
                String iconPath;
                try {
                    iconPath = (String) doc.getPropertyValue(NuxeoTypeHelper.NX_ICON);
                } catch (PropertyException e) {
                    iconPath = null;
                }
                InputStream is = NuxeoObjectData.getIconStream(iconPath,
                        callContext);
                if (is == null) {
                    throw new CmisConstraintException(
                            "No icon content stream: " + objectId);
                }

                int slash = iconPath.lastIndexOf('/');
                String filename = slash == -1 ? iconPath
                        : iconPath.substring(slash + 1);

                SimpleImageInfo info;
                try {
                    info = new SimpleImageInfo(is);
                } catch (IOException e) {
                    throw new CmisRuntimeException(e.toString(), e);
                }
                // refetch now-consumed stream
                is = NuxeoObjectData.getIconStream(iconPath, callContext);
                return new ContentStreamImpl(filename,
                        BigInteger.valueOf(info.getLength()),
                        info.getMimeType(), is);
            } catch (ClientException e) {
                throw new CmisRuntimeException(e.toString(), e);
            }
        } else if (streamId == null) {
            DocumentModel doc = getDocumentModel(objectId);
            ContentStream cs = NuxeoPropertyData.getContentStream(doc);
            if (cs != null) {
                return cs;
            }
            throw new CmisConstraintException("No content stream: " + objectId);
        }
        throw new CmisInvalidArgumentException("Invalid stream id: " + streamId);
    }

    @Override
    public List<RenditionData> getRenditions(String repositoryId,
            String objectId, String renditionFilter, BigInteger maxItems,
            BigInteger skipCount, ExtensionsData extension) {
        DocumentModel doc = getDocumentModel(objectId);
        return NuxeoObjectData.getRenditions(doc, maxItems, skipCount,
                callContext);
    }

    @Override
    public ObjectData getObjectByPath(String repositoryId, String path,
            String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePolicyIds, Boolean includeAcl,
            ExtensionsData extension) {
        DocumentModel doc;
        try {
            DocumentRef pathRef = new PathRef(path);
            if (coreSession.exists(pathRef)) {
                doc = coreSession.getDocument(pathRef);
                if (isFilteredOut(doc)) {
                    throw new CmisObjectNotFoundException(path);
                }
            } else {
                // Adobe Drive 2 confuses cmis:name and path segment
                // try using sequence of titles
                doc = getObjectByPathOfNames(path);
            }
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
        ObjectData data = new NuxeoObjectData(this, doc, filter,
                includeAllowableActions, includeRelationships, renditionFilter,
                includePolicyIds, includeAcl, extension);
        collectObjectInfo(repositoryId, data);
        return data;
    }

    /**
     * Gets a document given a path built out of dc:title components.
     * <p>
     * Filtered out docs are ignored.
     */
    protected DocumentModel getObjectByPathOfNames(String path)
            throws ClientException, CmisObjectNotFoundException {
        DocumentModel doc = coreSession.getRootDocument();
        for (String name : new Path(path).segments()) {
            String query = String.format(
                    "SELECT * FROM Document WHERE ecm:parentId = %s AND dc:title = %s",
                    escapeStringForNXQL(doc.getId()), escapeStringForNXQL(name));
            DocumentModelList docs = coreSession.query(query);
            if (docs.isEmpty()) {
                throw new CmisObjectNotFoundException(path);
            }
            doc = null;
            for (DocumentModel d : docs) {
                if (isFilteredOut(d)) {
                    continue;
                }
                if (doc == null) {
                    doc = d;
                } else {
                    log.warn(String.format(
                            "Path '%s' returns several documents for '%s'",
                            path, name));
                    break;
                }
            }
            if (doc == null) {
                throw new CmisObjectNotFoundException(path);
            }
        }
        return doc;
    }

    protected static String REPLACE_QUOTE = Matcher.quoteReplacement("\\'");

    protected static String escapeStringForNXQL(String s) {
        return "'" + s.replaceAll("'", REPLACE_QUOTE) + "'";
    }

    @Override
    public Properties getProperties(String repositoryId, String objectId,
            String filter, ExtensionsData extension) {
        DocumentModel doc = getDocumentModel(objectId);
        NuxeoObjectData data = new NuxeoObjectData(this, doc, filter, null,
                null, null, null, null, null);
        return data.getProperties();
    }

    protected boolean collectObjectInfos = true;

    protected Map<String, ObjectInfo> objectInfos;

    // part of CMIS API and of ObjectInfoHandler
    @Override
    public ObjectInfo getObjectInfo(String repositoryId, String objectId) {
        ObjectInfo info = getObjectInfo().get(objectId);
        if (info != null) {
            return info;
        }
        DocumentModel doc = getDocumentModel(objectId);
        NuxeoObjectData data = new NuxeoObjectData(this, doc, null,
                Boolean.TRUE, IncludeRelationships.BOTH, null, Boolean.TRUE,
                Boolean.FALSE, null);
        return getObjectInfo(repositoryId, data);
    }

    // AbstractCmisService helper
    protected ObjectInfo getObjectInfo(String repositoryId, ObjectData data) {
        ObjectInfo info = getObjectInfo().get(data.getId());
        if (info != null) {
            return info;
        }
        try {
            collectObjectInfos = false;
            info = getObjectInfoIntern(repositoryId, data);
            getObjectInfo().put(info.getId(), info);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            collectObjectInfos = true;
        }
        return info;
    }

    protected Map<String, ObjectInfo> getObjectInfo() {
        if (objectInfos == null) {
            objectInfos = new HashMap<String, ObjectInfo>();
        }
        return objectInfos;
    }

    @Override
    public void clearObjectInfos() {
        objectInfos = null;
    }

    protected void collectObjectInfo(String repositoryId, String objectId) {
        if (collectObjectInfos && callContext.isObjectInfoRequired()) {
            getObjectInfo(repositoryId, objectId);
        }
    }

    protected void collectObjectInfo(String repositoryId, ObjectData data) {
        if (collectObjectInfos && callContext.isObjectInfoRequired()) {
            getObjectInfo(repositoryId, data);
        }
    }

    @Override
    public void addObjectInfo(ObjectInfo info) {
        // ObjectInfoHandler, unused here
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveObject(String repositoryId, Holder<String> objectIdHolder,
            String targetFolderId, String sourceFolderId,
            ExtensionsData extension) {
        String objectId;
        if (objectIdHolder == null
                || (objectId = objectIdHolder.getValue()) == null) {
            throw new CmisInvalidArgumentException("Missing object ID");
        }
        if (repository.getRootFolderId().equals(objectId)) {
            throw new CmisConstraintException("Cannot move root");
        }
        if (targetFolderId == null) {
            throw new CmisInvalidArgumentException("Missing target folder ID");
        }
        try {
            getDocumentModel(objectId); // check exists and not deleted
            DocumentRef docRef = new IdRef(objectId);
            DocumentModel parent = coreSession.getParentDocument(docRef);
            if (isFilteredOut(parent)) {
                throw new CmisObjectNotFoundException("No parent: " + objectId);
            }
            if (sourceFolderId == null) {
                sourceFolderId = parent.getId();
            } else {
                // check it's the actual parent
                if (!parent.getId().equals(sourceFolderId)) {
                    throw new CmisInvalidArgumentException("Object " + objectId
                            + " is not filed in " + sourceFolderId);
                }
            }
            DocumentModel target = getDocumentModel(targetFolderId);
            if (!target.isFolder()) {
                throw new CmisInvalidArgumentException(
                        "Target is not a folder: " + targetFolderId);
            }
            coreSession.move(docRef, new IdRef(targetFolderId), null);
            coreSession.save();
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public void setContentStream(String repositoryId,
            Holder<String> objectIdHolder, Boolean overwriteFlag,
            Holder<String> changeTokenHolder, ContentStream contentStream,
            ExtensionsData extension) {
        String objectId;
        if (objectIdHolder == null
                || (objectId = objectIdHolder.getValue()) == null) {
            throw new CmisInvalidArgumentException("Missing object ID");
        }

        DocumentModel doc = getDocumentModel(objectId);
        // TODO test doc checkout state
        try {
            NuxeoPropertyData.setContentStream(doc, contentStream,
                    !Boolean.FALSE.equals(overwriteFlag));
            coreSession.saveDocument(doc);
            coreSession.save();
        } catch (CmisBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public void updateProperties(String repositoryId,
            Holder<String> objectIdHolder, Holder<String> changeTokenHolder,
            Properties properties, ExtensionsData extension) {
        String objectId;
        if (objectIdHolder == null
                || (objectId = objectIdHolder.getValue()) == null) {
            throw new CmisInvalidArgumentException("Missing object ID");
        }
        DocumentModel doc = getDocumentModel(objectId);
        NuxeoObjectData object = new NuxeoObjectData(this, doc);
        String changeToken = changeTokenHolder == null ? null
                : changeTokenHolder.getValue();
        updateProperties(object, changeToken, properties, false);
        try {
            coreSession.saveDocument(doc);
            coreSession.save();
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public Acl applyAcl(String repositoryId, String objectId, Acl addAces,
            Acl removeAces, AclPropagation aclPropagation,
            ExtensionsData extension) {
        throw new CmisNotSupportedException();
    }

    @Override
    public Acl applyAcl(String repositoryId, String objectId, Acl aces,
            AclPropagation aclPropagation) {
        throw new CmisNotSupportedException();
    }

    @Override
    public Acl getAcl(String repositoryId, String objectId,
            Boolean onlyBasicPermissions, ExtensionsData extension) {
        throw new CmisNotSupportedException();
    }

    @Override
    public ObjectList getContentChanges(String repositoryId,
            Holder<String> changeLogTokenHolder, Boolean includeProperties,
            String filter, Boolean includePolicyIds, Boolean includeAcl,
            BigInteger maxItems, ExtensionsData extension) {
        if (changeLogTokenHolder == null) {
            throw new CmisInvalidArgumentException(
                    "Missing change log token holder");

        }
        String changeLogToken = changeLogTokenHolder.getValue();
        long minDate;
        if (changeLogToken == null) {
            minDate = 0;
        } else {
            try {
                minDate = Long.parseLong(changeLogToken);
            } catch (NumberFormatException e) {
                throw new CmisInvalidArgumentException(
                        "Invalid change log token");
            }
        }
        try {
            AuditReader reader = Framework.getService(AuditReader.class);
            if (reader == null) {
                throw new CmisRuntimeException("Cannot find audit service");
            }
            int max = maxItems == null ? -1 : maxItems.intValue();
            if (max < 0) {
                max = DEFAULT_CHANGE_LOG_SIZE;
            }
            Map<String, Object> params = new HashMap<String, Object>();
            String query = "FROM LogEntry log" //
                    + " WHERE log.eventDate >= :minDate" //
                    + "   AND log.eventId IN (:evCreated, :evModified, :evRemoved)" //
                    + "   AND log.repositoryId = :repoId" //
                    + " ORDER BY log.eventDate";
            params.put("minDate", new Date(minDate));
            params.put("evCreated", DOCUMENT_CREATED);
            params.put("evModified", DOCUMENT_UPDATED);
            params.put("evRemoved", DOCUMENT_REMOVED);
            params.put("repoId", repositoryId);
            List<?> entries = reader.nativeQuery(query, params, 1, max + 1);
            ObjectListImpl ol = new ObjectListImpl();
            boolean hasMoreItems = entries.size() > max;
            ol.setHasMoreItems(Boolean.valueOf(hasMoreItems));
            if (hasMoreItems) {
                entries = entries.subList(0, max);
            }
            List<ObjectData> ods = new ArrayList<ObjectData>(entries.size());
            Date date = null;
            for (Object entry : entries) {
                LogEntry logEntry = (LogEntry) entry;
                ObjectDataImpl od = new ObjectDataImpl();
                ChangeEventInfoDataImpl cei = new ChangeEventInfoDataImpl();
                // change type
                String eventId = logEntry.getEventId();
                ChangeType changeType;
                if (DOCUMENT_CREATED.equals(eventId)) {
                    changeType = ChangeType.CREATED;
                } else if (DOCUMENT_UPDATED.equals(eventId)) {
                    changeType = ChangeType.UPDATED;
                } else if (DOCUMENT_REMOVED.equals(eventId)) {
                    changeType = ChangeType.DELETED;
                } else {
                    continue;
                }
                cei.setChangeType(changeType);
                // change time
                GregorianCalendar changeTime = (GregorianCalendar) Calendar.getInstance();
                date = logEntry.getEventDate();
                changeTime.setTime(date);
                cei.setChangeTime(changeTime);
                od.setChangeEventInfo(cei);
                // properties: id, doc type
                PropertiesImpl properties = new PropertiesImpl();
                properties.addProperty(new PropertyIdImpl(
                        PropertyIds.OBJECT_ID, logEntry.getDocUUID()));
                properties.addProperty(new PropertyIdImpl(
                        PropertyIds.OBJECT_TYPE_ID, logEntry.getDocType()));
                od.setProperties(properties);
                ods.add(od);
            }
            ol.setObjects(ods);
            ol.setNumItems(BigInteger.valueOf(-1));
            String latestChangeLogToken = date == null ? null
                    : String.valueOf(date.getTime());
            changeLogTokenHolder.setValue(latestChangeLogToken);
            return ol;
        } catch (Exception e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    protected String getLatestChangeLogToken(String repositoryId) {
        try {
            AuditReader reader = Framework.getService(AuditReader.class);
            if (reader == null) {
                log.warn("Audit Service not found. latest change log token will be '0'");
                return "0";
                // throw new CmisRuntimeException("Cannot find audit service");
            }
            // TODO XXX repositoryId as well
            Map<String, Object> params = new HashMap<String, Object>();
            String query = "FROM LogEntry log" //
                    + " WHERE log.eventId IN (:evCreated, :evModified, :evRemoved)" //
                    + " ORDER BY log.eventDate DESC";
            params.put("evCreated", DOCUMENT_CREATED);
            params.put("evModified", DOCUMENT_UPDATED);
            params.put("evRemoved", DOCUMENT_REMOVED);
            List<?> entries = reader.nativeQuery(query, params, 1, 1);
            if (entries.size() == 0) {
                return "0";
            }
            LogEntry logEntry = (LogEntry) entries.get(0);
            return String.valueOf(logEntry.getEventDate().getTime());
        } catch (Exception e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public ObjectList query(String repositoryId, String statement,
            Boolean searchAllVersions, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        long skip = skipCount == null ? 0 : skipCount.longValue();
        if (skip < 0) {
            skip = 0;
        }
        long max = maxItems == null ? -1 : maxItems.longValue();
        if (max <= 0) {
            max = DEFAULT_QUERY_SIZE;
        }
        long numItems;
        List<ObjectData> list;
        IterableQueryResult res = null;
        try {
            Map<String, PropertyDefinition<?>> typeInfo = new HashMap<String, PropertyDefinition<?>>();
            if (searchAllVersions == null) {
                searchAllVersions = Boolean.FALSE; // spec default 2.2.6.1.1
            }
            res = coreSession.queryAndFetch(statement, CMISQLQueryMaker.TYPE,
                    this, typeInfo, searchAllVersions);

            // convert from Nuxeo to CMIS format
            list = new ArrayList<ObjectData>();
            if (skip > 0) {
                res.skipTo(skip);
            }
            for (Map<String, Serializable> map : res) {
                ObjectDataImpl od = makeObjectData(map, typeInfo);

                // optional stuff
                String id = od.getId();
                if (id != null) { // null if JOIN in original query
                    DocumentModel doc = null;
                    if (Boolean.TRUE.equals(includeAllowableActions)) {
                        doc = getDocumentModel(id);
                        AllowableActions allowableActions = NuxeoObjectData.getAllowableActions(
                                doc, false);
                        od.setAllowableActions(allowableActions);
                    }
                    if (includeRelationships != null
                            && includeRelationships != IncludeRelationships.NONE) {
                        // TODO get relationships using a JOIN
                        // added to the original query
                        List<ObjectData> relationships = NuxeoObjectData.getRelationships(
                                id, includeRelationships, coreSession, this);
                        od.setRelationships(relationships);
                    }
                    if (renditionFilter != null && renditionFilter.length() > 0) {
                        if (doc == null) {
                            doc = getDocumentModel(id);
                        }
                        // TODO parse rendition filter; for now returns them all
                        List<RenditionData> renditions = NuxeoObjectData.getRenditions(
                                doc, null, null, callContext);
                        od.setRenditions(renditions);
                    }
                }

                list.add(od);
                if (list.size() >= max) {
                    break;
                }
            }
            numItems = res.size();
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        } finally {
            if (res != null) {
                res.close();
            }
        }
        ObjectListImpl objList = new ObjectListImpl();
        objList.setObjects(list);
        objList.setNumItems(BigInteger.valueOf(numItems));
        objList.setHasMoreItems(Boolean.valueOf(numItems > skip + list.size()));
        return objList;
    }

    protected ObjectDataImpl makeObjectData(Map<String, Serializable> map,
            Map<String, PropertyDefinition<?>> typeInfo) {
        ObjectDataImpl od = new ObjectDataImpl();
        PropertiesImpl properties = new PropertiesImpl();
        for (Entry<String, Serializable> en : map.entrySet()) {
            String queryName = en.getKey();
            PropertyDefinition<?> pd = typeInfo.get(queryName);
            if (pd == null) {
                throw new NullPointerException("Cannot get " + queryName);
            }
            AbstractPropertyData<?> p = (AbstractPropertyData<?>) objectFactory.createPropertyData(
                    pd, en.getValue());
            p.setLocalName(pd.getLocalName());
            p.setDisplayName(pd.getDisplayName());
            // queryName and pd.getQueryName() may be different
            // for qualified properties
            p.setQueryName(queryName);
            properties.addProperty(p);
        }
        od.setProperties(properties);
        return od;
    }

    @Override
    public void addObjectToFolder(String repositoryId, String objectId,
            String folderId, Boolean allVersions, ExtensionsData extension) {
        throw new CmisNotSupportedException();
    }

    @Override
    public void removeObjectFromFolder(String repositoryId, String objectId,
            String folderId, ExtensionsData extension) {
        if (folderId != null) {
            // check it's the actual parent
            try {
                DocumentModel folder = getDocumentModel(folderId);
                DocumentModel parent = coreSession.getParentDocument(new IdRef(
                        objectId));
                if (!parent.getId().equals(folder.getId())) {
                    throw new CmisInvalidArgumentException("Object " + objectId
                            + " is not filed in  " + folderId);
                }
            } catch (ClientException e) {
                throw new CmisRuntimeException(e.toString(), e);
            }
        }
        deleteObject(repositoryId, objectId, Boolean.FALSE, extension);
    }

    @Override
    public ObjectInFolderList getChildren(String repositoryId, String folderId,
            String filter, String orderBy, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems,
            BigInteger skipCount, ExtensionsData extension) {
        if (folderId == null) {
            throw new CmisInvalidArgumentException("Null folderId");
        }
        return getChildrenInternal(repositoryId, folderId, filter, orderBy,
                includeAllowableActions, includeRelationships, renditionFilter,
                includePathSegment, maxItems, skipCount, false);
    }

    protected ObjectInFolderList getChildrenInternal(String repositoryId,
            String folderId, String filter, String orderBy,
            Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems,
            BigInteger skipCount, boolean folderOnly) {
        ObjectInFolderListImpl result = new ObjectInFolderListImpl();
        List<ObjectInFolderData> list = new ArrayList<ObjectInFolderData>();
        DocumentModel folder = getDocumentModel(folderId);
        if (!folder.isFolder()) {
            return null;
        }
        DocumentModelList children;
        try {
            children = coreSession.getChildren(folder.getRef());
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
        for (DocumentModel child : children) {
            if (isFilteredOut(child)) {
                continue;
            }
            if (folderOnly && !child.isFolder()) {
                continue;
            }
            NuxeoObjectData data = new NuxeoObjectData(this, child, filter,
                    includeAllowableActions, includeRelationships,
                    renditionFilter, Boolean.FALSE, Boolean.FALSE, null);
            ObjectInFolderDataImpl oifd = new ObjectInFolderDataImpl();
            oifd.setObject(data);
            if (Boolean.TRUE.equals(includePathSegment)) {
                oifd.setPathSegment(child.getName());
            }
            list.add(oifd);
            collectObjectInfo(repositoryId, data);
        }

        if (StringUtils.isNotBlank(orderBy)) {
            Collections.sort(list, new OrderByComparator(orderBy, repository));
        }

        BatchedList<ObjectInFolderData> batch = ListUtils.getBatchedList(list,
                maxItems, skipCount, DEFAULT_MAX_CHILDREN);
        result.setObjects(batch.getList());
        result.setHasMoreItems(batch.getHasMoreItems());
        result.setNumItems(batch.getNumItems());
        collectObjectInfo(repositoryId, folderId);
        return result;
    }

    public static class OrderByComparator implements
            Comparator<ObjectInFolderData> {

        protected static String ASC = " asc";

        protected static String DESC = " desc";

        protected String[] props;

        protected boolean[] descs;

        public OrderByComparator(String orderBy, NuxeoRepository repository) {
            TypeManagerImpl typeManager = repository.getTypeManager();
            String[] orders = orderBy.split(",");
            props = new String[orders.length];
            descs = new boolean[orders.length];
            for (int i = 0; i < orders.length; i++) {
                String order = orders[i].trim();
                String lower = order.toLowerCase();
                String prop;
                boolean desc;
                if (lower.endsWith(DESC)) {
                    prop = order.substring(0, order.length() - DESC.length()).trim();
                    desc = true;
                } else if (lower.endsWith(ASC)) {
                    prop = order.substring(0, order.length() - ASC.length()).trim();
                    desc = false;
                } else {
                    prop = order;
                    desc = false;
                }
                String propId = typeManager.getPropertyIdForQueryName(prop);
                if (propId == null) {
                    throw new CmisInvalidArgumentException("Invalid orderBy: "
                            + orderBy);
                }
                props[i] = propId;
                descs[i] = desc;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public int compare(ObjectInFolderData ob1, ObjectInFolderData ob2) {
            int cmp = 0;
            for (int i = 0; i < props.length; i++) {
                String prop = props[i];
                boolean desc = descs[i];
                NuxeoPropertyDataBase<?> p1 = ((NuxeoObjectData) ob1.getObject()).getProperty(prop);
                NuxeoPropertyDataBase<?> p2 = ((NuxeoObjectData) ob2.getObject()).getProperty(prop);
                Object v1 = p1 == null ? null : p1.getValue();
                Object v2 = p2 == null ? null : p2.getValue();
                if (v1 == null && v2 == null) {
                    cmp = 0;
                } else if (v1 == null) {
                    cmp = -1;
                } else if (v2 == null) {
                    cmp = 1;
                } else {
                    cmp = ((Comparable<Object>) v1).compareTo(v2);
                }
                if (desc) {
                    cmp = -cmp;
                }
                if (cmp != 0) {
                    break;
                }
            }
            return cmp;
        }
    }

    @Override
    public List<ObjectInFolderContainer> getDescendants(String repositoryId,
            String folderId, BigInteger depth, String filter,
            Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, ExtensionsData extension) {
        if (folderId == null) {
            throw new CmisInvalidArgumentException("Null folderId");
        }
        int levels = depth == null ? DEFAULT_FOLDER_LEVELS : depth.intValue();
        if (levels == 0) {
            throw new CmisInvalidArgumentException("Invalid depth: 0");
        }
        return getDescendantsInternal(repositoryId, folderId, filter,
                includeAllowableActions, includeRelationships, renditionFilter,
                includePathSegment, 0, levels, false);
    }

    @Override
    public List<ObjectInFolderContainer> getFolderTree(String repositoryId,
            String folderId, BigInteger depth, String filter,
            Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, ExtensionsData extension) {
        if (folderId == null) {
            throw new CmisInvalidArgumentException("Null folderId");
        }
        int levels = depth == null ? DEFAULT_FOLDER_LEVELS : depth.intValue();
        if (levels == 0) {
            throw new CmisInvalidArgumentException("Invalid depth: 0");
        }
        return getDescendantsInternal(repositoryId, folderId, filter,
                includeAllowableActions, includeRelationships, renditionFilter,
                includePathSegment, 0, levels, true);
    }

    protected List<ObjectInFolderContainer> getDescendantsInternal(
            String repositoryId, String folderId, String filter,
            Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegments, int level, int maxLevels,
            boolean folderOnly) {
        if (maxLevels != -1 && level >= maxLevels) {
            return null;
        }
        ObjectInFolderList children = getChildrenInternal(repositoryId,
                folderId, filter, null, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegments,
                null, null, folderOnly);
        if (children == null) {
            return Collections.emptyList();
        }
        List<ObjectInFolderContainer> res = new ArrayList<ObjectInFolderContainer>(
                children.getObjects().size());
        for (ObjectInFolderData child : children.getObjects()) {
            ObjectInFolderContainerImpl oifc = new ObjectInFolderContainerImpl();
            oifc.setObject(child);
            // recurse
            List<ObjectInFolderContainer> subChildren = getDescendantsInternal(
                    repositoryId, child.getObject().getId(), filter,
                    includeAllowableActions, includeRelationships,
                    renditionFilter, includePathSegments, level + 1, maxLevels,
                    folderOnly);
            if (subChildren != null) {
                oifc.setChildren(subChildren);
            }
            res.add(oifc);
        }
        return res;
    }

    @Override
    public ObjectData getFolderParent(String repositoryId, String folderId,
            String filter, ExtensionsData extension) {
        List<ObjectParentData> parents = getObjectParentsInternal(repositoryId,
                folderId, filter, null, null, null, Boolean.TRUE, true);
        return parents.isEmpty() ? null : parents.get(0).getObject();
    }

    @Override
    public List<ObjectParentData> getObjectParents(String repositoryId,
            String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, ExtensionsData extension) {
        return getObjectParentsInternal(repositoryId, objectId, filter,
                includeAllowableActions, includeRelationships, renditionFilter,
                includeRelativePathSegment, false);
    }

    protected List<ObjectParentData> getObjectParentsInternal(
            String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, boolean folderOnly) {
        String pathSegment;
        String parentId;
        try {
            DocumentRef docRef = new IdRef(objectId);
            if (!coreSession.exists(docRef)) {
                throw new CmisObjectNotFoundException(objectId);
            }
            DocumentModel doc = coreSession.getDocument(docRef);
            if (isFilteredOut(doc)) {
                throw new CmisObjectNotFoundException(objectId);
            }
            if (folderOnly && !doc.isFolder()) {
                throw new CmisInvalidArgumentException("Not a folder: "
                        + objectId);
            }
            pathSegment = doc.getName();
            if (pathSegment == null) { // root
                return Collections.emptyList();
            }
            DocumentRef parentRef = doc.getParentRef();
            if (parentRef == null) { // placeless
                return Collections.emptyList();
            }
            if (!coreSession.exists(parentRef)) { // non-accessible
                return Collections.emptyList();
            }
            DocumentModel parent = coreSession.getDocument(parentRef);
            if (isFilteredOut(parent)) { // filtered out
                return Collections.emptyList();
            }
            parentId = parent.getId();
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }

        ObjectData od = getObject(repositoryId, parentId, filter,
                includeAllowableActions, includeRelationships, renditionFilter,
                Boolean.FALSE, Boolean.FALSE, null);
        ObjectParentDataImpl opd = new ObjectParentDataImpl(od);
        if (!Boolean.FALSE.equals(includeRelativePathSegment)) {
            opd.setRelativePathSegment(pathSegment);
        }
        return Collections.<ObjectParentData> singletonList(opd);
    }

    @Override
    public void applyPolicy(String repositoryId, String policyId,
            String objectId, ExtensionsData extension) {
        throw new CmisNotSupportedException();
    }

    @Override
    public List<ObjectData> getAppliedPolicies(String repositoryId,
            String objectId, String filter, ExtensionsData extension) {
        return Collections.emptyList();
    }

    @Override
    public void removePolicy(String repositoryId, String policyId,
            String objectId, ExtensionsData extension) {
        throw new CmisNotSupportedException();
    }

    @Override
    public ObjectList getObjectRelationships(String repositoryId,
            String objectId, Boolean includeSubRelationshipTypes,
            RelationshipDirection relationshipDirection, String typeId,
            String filter, Boolean includeAllowableActions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        IncludeRelationships includeRelationships;
        if (relationshipDirection == null
                || relationshipDirection == RelationshipDirection.SOURCE) {
            includeRelationships = IncludeRelationships.SOURCE;
        } else if (relationshipDirection == RelationshipDirection.TARGET) {
            includeRelationships = IncludeRelationships.TARGET;
        } else { // RelationshipDirection.EITHER
            includeRelationships = IncludeRelationships.BOTH;
        }
        List<ObjectData> rels = NuxeoObjectData.getRelationships(objectId,
                includeRelationships, coreSession, this);
        BatchedList<ObjectData> batch = ListUtils.getBatchedList(rels,
                maxItems, skipCount, DEFAULT_MAX_RELATIONSHIPS);
        ObjectListImpl res = new ObjectListImpl();
        res.setObjects(batch.getList());
        res.setNumItems(batch.getNumItems());
        res.setHasMoreItems(batch.getHasMoreItems());
        for (ObjectData data : res.getObjects()) {
            collectObjectInfo(repositoryId, data);
        }
        return res;
    }

    @Override
    public void checkIn(String repositoryId, Holder<String> objectIdHolder,
            Boolean major, Properties properties, ContentStream contentStream,
            String checkinComment, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        String objectId;
        if (objectIdHolder == null
                || (objectId = objectIdHolder.getValue()) == null) {
            throw new CmisInvalidArgumentException("Missing object ID");
        }
        VersioningOption option = Boolean.TRUE.equals(major) ? VersioningOption.MAJOR
                : VersioningOption.MINOR;

        DocumentModel doc = getDocumentModel(objectId);
        if (doc.isVersion() || doc.isProxy()) {
            throw new CmisInvalidArgumentException("Cannot check in non-PWC: "
                    + doc);
        }

        NuxeoObjectData object = new NuxeoObjectData(this, doc);
        updateProperties(object, null, properties, false);
        if (contentStream != null) {
            try {
                NuxeoPropertyData.setContentStream(doc, contentStream, true);
            } catch (IOException e) {
                throw new CmisRuntimeException(e.toString(), e);
            }
        }
        try {
            coreSession.saveDocument(doc);
            DocumentRef ver = doc.checkIn(option, checkinComment);
            coreSession.save();
            objectIdHolder.setValue(getIdFromDocumentRef(ver));
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    public String checkIn(String objectId, boolean major,
            Map<String, ?> properties, ObjectType type,
            ContentStream contentStream, String checkinComment) {
        VersioningOption option = major ? VersioningOption.MAJOR
                : VersioningOption.MINOR;
        DocumentModel doc = getDocumentModel(objectId);
        if (doc.isVersion() || doc.isProxy()) {
            throw new CmisInvalidArgumentException("Cannot check in non-PWC: "
                    + doc);
        }
        NuxeoObjectData object = new NuxeoObjectData(this, doc);
        updateProperties(object, null, properties, type, false);
        if (contentStream != null) {
            try {
                NuxeoPropertyData.setContentStream(doc, contentStream, true);
            } catch (IOException e) {
                throw new CmisRuntimeException(e.toString(), e);
            }
        }
        try {
            coreSession.saveDocument(doc);
            DocumentRef ver = doc.checkIn(option, checkinComment);
            coreSession.save();
            return getIdFromDocumentRef(ver);
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public void checkOut(String repositoryId, Holder<String> objectIdHolder,
            ExtensionsData extension, Holder<Boolean> contentCopiedHolder) {
        String objectId;
        if (objectIdHolder == null
                || (objectId = objectIdHolder.getValue()) == null) {
            throw new CmisInvalidArgumentException("Missing object ID");
        }
        String pwcId = checkOut(objectId);
        objectIdHolder.setValue(pwcId);
        if (contentCopiedHolder != null) {
            contentCopiedHolder.setValue(Boolean.TRUE);
        }
    }

    public String checkOut(String objectId) {
        DocumentModel doc = getDocumentModel(objectId);
        try {
            if (doc.isProxy()) {
                throw new CmisInvalidArgumentException(
                        "Cannot check out non-version: " + objectId);
            }
            // find pwc
            DocumentModel pwc;
            if (doc.isVersion()) {
                pwc = coreSession.getWorkingCopy(doc.getRef());
                if (pwc == null) {
                    // no live document available
                    // TODO do a restore somewhere
                    throw new CmisObjectNotFoundException(objectId);
                }
            } else {
                pwc = doc;
            }
            if (pwc.isCheckedOut()) {
                throw new CmisConstraintException("Already checked out: "
                        + objectId);
            }
            pwc.checkOut();
            coreSession.save();
            return pwc.getId();
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public void cancelCheckOut(String repositoryId, String objectId,
            ExtensionsData extension) {
        cancelCheckOut(objectId);
    }

    public void cancelCheckOut(String objectId) {
        DocumentModel doc = getDocumentModel(objectId);
        try {
            if (doc.isVersion() || doc.isProxy() || !doc.isCheckedOut()) {
                throw new CmisInvalidArgumentException(
                        "Cannot cancel check out of non-PWC: " + doc);
            }
            DocumentRef docRef = doc.getRef();
            // find last version
            DocumentRef verRef = coreSession.getLastDocumentVersionRef(docRef);
            if (verRef == null) {
                // delete
                coreSession.removeDocument(docRef);
            } else {
                // restore and keep checked in
                coreSession.restoreToVersion(docRef, verRef, true, true);
            }
            coreSession.save();
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public ObjectList getCheckedOutDocs(String repositoryId, String folderId,
            String filter, String orderBy, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        // columns from filter
        List<String> props;
        if (StringUtils.isBlank(filter)) {
            props = Arrays.asList(PropertyIds.OBJECT_ID,
                    PropertyIds.OBJECT_TYPE_ID, PropertyIds.BASE_TYPE_ID);
        } else {
            props = NuxeoObjectData.getPropertyIdsFromFilter(filter);
            // same as query names
        }
        // clause from folderId
        List<String> clauses = new ArrayList<String>(3);
        clauses.add(NuxeoTypeHelper.NX_ISVERSION + " = false");
        // TODO optimize storage of ecm:isCheckedIn to avoid this OR
        clauses.add("(" + NuxeoTypeHelper.NX_ISCHECKEDIN + " = false" //
                + " OR " + NuxeoTypeHelper.NX_ISCHECKEDIN + " IS NULL)");
        if (folderId != null) {
            String qid = "'" + folderId.replace("'", "''") + "'";
            clauses.add("IN_FOLDER(" + qid + ")");
        }
        // orderBy
        String order;
        if (StringUtils.isBlank(orderBy)) {
            order = "";
        } else {
            order = " ORDER BY " + orderBy;
        }
        String statement = "SELECT " + StringUtils.join(props, ", ") + " FROM "
                + BaseTypeId.CMIS_DOCUMENT.value() + " WHERE "
                + StringUtils.join(clauses, " AND ") + order;
        Boolean searchAllVersions = Boolean.TRUE;
        return query(repositoryId, statement, searchAllVersions,
                includeAllowableActions, includeRelationships, renditionFilter,
                maxItems, skipCount, extension);
    }

    @Override
    public List<ObjectData> getAllVersions(String repositoryId,
            String objectId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, ExtensionsData extension) {
        DocumentModel doc;
        if (objectId != null) {
            // atompub passes object id
            doc = getDocumentModel(objectId);
        } else if (versionSeriesId != null) {
            // soap passes version series id
            // version series id is (for now) id of live document
            // TODO deal with removal of live doc
            doc = getDocumentModel(versionSeriesId);
        } else {
            throw new CmisInvalidArgumentException(
                    "Missing object ID or version series ID");
        }
        try {
            List<DocumentRef> versions = coreSession.getVersionsRefs(doc.getRef());
            List<ObjectData> list = new ArrayList<ObjectData>(versions.size());
            for (DocumentRef verRef : versions) {
                String verId = getIdFromDocumentRef(verRef);
                ObjectData od = getObject(repositoryId, verId, filter,
                        includeAllowableActions, IncludeRelationships.NONE,
                        null, Boolean.FALSE, Boolean.FALSE, null);
                list.add(od);
            }
            // PWC last
            DocumentModel pwc = doc.isVersion() ? coreSession.getWorkingCopy(doc.getRef())
                    : doc;
            if (pwc != null && pwc.isCheckedOut()) {
                NuxeoObjectData od = new NuxeoObjectData(this, pwc, filter,
                        includeAllowableActions, IncludeRelationships.NONE,
                        null, Boolean.FALSE, Boolean.FALSE, extension);
                list.add(od);
            }
            // CoreSession returns them in creation order,
            // CMIS wants them last first
            Collections.reverse(list);
            return list;
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public NuxeoObjectData getObjectOfLatestVersion(String repositoryId,
            String objectId, String versionSeriesId, Boolean major,
            String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePolicyIds, Boolean includeAcl,
            ExtensionsData extension) {
        DocumentModel doc;
        if (objectId != null) {
            // atompub passes object id
            doc = getDocumentModel(objectId);
        } else if (versionSeriesId != null) {
            // soap passes version series id
            // version series id is (for now) id of live document
            // TODO deal with removal of live doc
            doc = getDocumentModel(versionSeriesId);
        } else {
            throw new CmisInvalidArgumentException(
                    "Missing object ID or version series ID");
        }
        try {
            if (Boolean.TRUE.equals(major)) {
                // we must list all versions
                List<DocumentModel> versions = coreSession.getVersions(doc.getRef());
                Collections.reverse(versions);
                for (DocumentModel ver : versions) {
                    if (ver.isMajorVersion()) {
                        return getObject(repositoryId, ver.getId(), filter,
                                includeAllowableActions, includeRelationships,
                                renditionFilter, includePolicyIds, includeAcl,
                                null);
                    }
                }
                return null;
            } else {
                DocumentRef verRef = coreSession.getLastDocumentVersionRef(doc.getRef());
                String verId = getIdFromDocumentRef(verRef);
                return getObject(repositoryId, verId, filter,
                        includeAllowableActions, includeRelationships,
                        renditionFilter, includePolicyIds, includeAcl, null);
            }
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public Properties getPropertiesOfLatestVersion(String repositoryId,
            String objectId, String versionSeriesId, Boolean major,
            String filter, ExtensionsData extension) {
        NuxeoObjectData od = getObjectOfLatestVersion(repositoryId, objectId,
                versionSeriesId, major, filter, Boolean.FALSE,
                IncludeRelationships.NONE, null, Boolean.FALSE, Boolean.FALSE,
                null);
        return od == null ? null : od.getProperties();
    }

    @Override
    public void deleteObject(String repositoryId, String objectId,
            Boolean allVersions, ExtensionsData extension) {
        try {
            DocumentModel doc = getDocumentModel(objectId);
            if (doc.isFolder()) {
                // check that there are no children left
                DocumentModelList docs = coreSession.getChildren(new IdRef(
                        objectId), null, documentFilter, null);
                if (docs.size() > 0) {
                    throw new CmisConstraintException(
                            "Cannot delete non-empty folder: " + objectId);
                }
            }
            coreSession.removeDocument(doc.getRef());
            coreSession.save();
        } catch (ClientException e) {
            throw new CmisRuntimeException(e.toString(), e);
        }
    }

    @Override
    public void deleteObjectOrCancelCheckOut(String repositoryId,
            String objectId, Boolean allVersions, ExtensionsData extension) {
        deleteObject(repositoryId, objectId, allVersions, extension);
    }

}
