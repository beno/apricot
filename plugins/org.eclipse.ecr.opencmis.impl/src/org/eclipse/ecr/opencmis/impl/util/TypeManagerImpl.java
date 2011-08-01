/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jens Huebel, Open Text
 *   Florent Guillaume, Nuxeo
 */
package org.eclipse.ecr.opencmis.impl.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.Converter;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionListImpl;
import org.apache.chemistry.opencmis.server.support.TypeManager;

/**
 * Manages a type system for a repository.
 * <p>
 * Types can be added, the inheritance can be managed and type can be retrieved
 * for a given type id.
 * <p>
 * Structures are not copied when returned.
 */
public class TypeManagerImpl implements TypeManager {

    public static final int DEFAULT_MAX_TYPE_CHILDREN = 100;

    protected Map<String, TypeDefinitionContainer> typesMap = new HashMap<String, TypeDefinitionContainer>();

    @Override
    public TypeDefinitionContainer getTypeById(String typeId) {
        return typesMap.get(typeId);
    }

    @Override
    public TypeDefinition getTypeByQueryName(String typeQueryName) {
        for (Entry<String, TypeDefinitionContainer> entry : typesMap.entrySet()) {
            TypeDefinition type = entry.getValue().getTypeDefinition();
            if (type.getQueryName().equals(typeQueryName))
                return type;
        }
        return null;
    }

    public TypeDefinitionList getTypeChildren(String typeId,
            Boolean includePropertyDefinitions, BigInteger maxItems,
            BigInteger skipCount) {
        TypeDefinitionContainer typec;
        if (typeId == null) {
            // return root types
            typec = null;
        } else {
            typec = typesMap.get(typeId);
            if (typec == null) {
                throw new CmisInvalidArgumentException("No such type: "
                        + typeId);
            }
        }
        List<TypeDefinitionContainer> types;
        if (typec == null) {
            // return root types
            // TODO maintain pre-computed root types
            types = new ArrayList<TypeDefinitionContainer>(4);
            for (TypeDefinitionContainer tc : typesMap.values()) {
                if (tc.getTypeDefinition().getParentTypeId() == null)
                    types.add(tc);
            }
        } else {
            types = typec.getChildren();
        }
        List<TypeDefinition> list = new ArrayList<TypeDefinition>(types.size());
        for (TypeDefinitionContainer tdc : types) {
            TypeDefinition type = tdc.getTypeDefinition();
            if (!Boolean.TRUE.equals(includePropertyDefinitions)) {
                type = Converter.convert(Converter.convert(type)); // clone
                // TODO avoid recomputing type-without-properties
                type.getPropertyDefinitions().clear();
            }
            list.add(type);
        }
        list = ListUtils.batchList(list, maxItems, skipCount,
                DEFAULT_MAX_TYPE_CHILDREN);
        return new TypeDefinitionListImpl(list);
    }

    public List<TypeDefinitionContainer> getTypeDescendants(String typeId,
            int depth, Boolean includePropertyDefinitions) {
        List<TypeDefinitionContainer> types;
        boolean includeProps = Boolean.TRUE.equals(includePropertyDefinitions);
        if (typeId == null) {
            // return all types, unlimited depth
            types = new ArrayList<TypeDefinitionContainer>(4);
            for (TypeDefinitionContainer tc : typesMap.values()) {
                if (tc.getTypeDefinition().getParentTypeId() == null)
                    types.add(tc);
            }
            if (!includeProps) {
                // remove props
                types = cloneTypes(types, -1, false);
            }
        } else {
            TypeDefinitionContainer typec = typesMap.get(typeId);
            if (typec == null) {
                throw new CmisInvalidArgumentException("No such type: "
                        + typeId);
            }
            if (depth == 0 || depth < -1) {
                throw new CmisInvalidArgumentException("Invalid depth: "
                        + depth);
            }
            if (depth == -1) {
                types = typec.getChildren();
                if (!includeProps) {
                    // remove props
                    types = cloneTypes(types, -1, false);
                }
            } else {
                types = typec.getChildren();
                // truncate tree
                types = cloneTypes(types, depth - 1, includeProps);
            }
        }
        return types;
    }

    @Override
    public Collection<TypeDefinitionContainer> getTypeDefinitionList() {
        List<TypeDefinitionContainer> typeRoots = new ArrayList<TypeDefinitionContainer>();
        // iterate types map and return a list collecting the root types:
        for (TypeDefinitionContainer typeCont : typesMap.values()) {
            if (typeCont.getTypeDefinition().getParentTypeId() == null)
                typeRoots.add(typeCont);
        }
        return typeRoots;
    }

    @Override
    public List<TypeDefinitionContainer> getRootTypes() {
        List<TypeDefinitionContainer> rootTypes = new ArrayList<TypeDefinitionContainer>();
        for (TypeDefinitionContainer type : typesMap.values()) {
            String id = type.getTypeDefinition().getId();
            if (BaseTypeId.CMIS_DOCUMENT.value().equals(id)
                    || BaseTypeId.CMIS_FOLDER.value().equals(id)
                    || BaseTypeId.CMIS_RELATIONSHIP.value().equals(id)
                    || BaseTypeId.CMIS_POLICY.value().equals(id)) {
                rootTypes.add(type);
            }
        }
        return rootTypes;
    }

    /**
     * Add a type to the type system. Add all properties from inherited types,
     * add type to children of parent types.
     *
     * @param type new type to add
     */
    public void addTypeDefinition(TypeDefinition type) {
        String id = type.getId();
        if (typesMap.containsKey(id)) {
            throw new RuntimeException("Type already exists: " + id);
        }

        TypeDefinitionContainer typeContainer = new TypeDefinitionContainerImpl(
                type);
        // add type to type map
        typesMap.put(id, typeContainer);

        String parentId = type.getParentTypeId();
        if (parentId != null) {
            if (!typesMap.containsKey(parentId)) {
                throw new RuntimeException("Cannot add type " + id
                        + ", parent does not exist: " + parentId);
            }
            TypeDefinitionContainer parentTypeContainer = typesMap.get(parentId);
            // add new type to children of parent types
            parentTypeContainer.getChildren().add(typeContainer);
            // recursively add inherited properties
            Map<String, PropertyDefinition<?>> propDefs = typeContainer.getTypeDefinition().getPropertyDefinitions();
            addInheritedProperties(propDefs,
                    parentTypeContainer.getTypeDefinition());
        }
    }

    @Override
    public String getPropertyIdForQueryName(TypeDefinition typeDefinition,
            String propQueryName) {
        for (PropertyDefinition<?> pd : typeDefinition.getPropertyDefinitions().values()) {
            if (pd.getQueryName().equals(propQueryName))
                return pd.getId();
        }
        return null;
    }

    protected void addInheritedProperties(
            Map<String, PropertyDefinition<?>> propDefs, TypeDefinition type) {
        if (type.getPropertyDefinitions() != null) {
            addInheritedPropertyDefinitions(propDefs,
                    type.getPropertyDefinitions());
        }
        TypeDefinitionContainer parentTypeContainer = typesMap.get(type.getParentTypeId());
        if (parentTypeContainer != null) {
            addInheritedProperties(propDefs,
                    parentTypeContainer.getTypeDefinition());
        }
    }

    protected void addInheritedPropertyDefinitions(
            Map<String, PropertyDefinition<?>> propDefs,
            Map<String, PropertyDefinition<?>> superPropDefs) {
        for (PropertyDefinition<?> superPropDef : superPropDefs.values()) {
            PropertyDefinition<?> clone = Converter.convert(Converter.convert(superPropDef));
            ((AbstractPropertyDefinition<?>) clone).setIsInherited(Boolean.TRUE);
            propDefs.put(superPropDef.getId(), clone);
        }
    }

    /**
     * Returns a clone of a types tree.
     * <p>
     * Removes properties on the clone if requested, cuts the children of the
     * clone if the depth is exceeded.
     */
    protected static List<TypeDefinitionContainer> cloneTypes(
            List<TypeDefinitionContainer> types, int depth,
            boolean includePropertyDefinitions) {
        List<TypeDefinitionContainer> res = new ArrayList<TypeDefinitionContainer>(
                types.size());
        for (TypeDefinitionContainer tc : types) {
            AbstractTypeDefinition td = ((AbstractTypeDefinition) tc.getTypeDefinition()).clone();
            if (!includePropertyDefinitions) {
                td.setPropertyDefinitions(null);
            }
            TypeDefinitionContainerImpl clone = new TypeDefinitionContainerImpl(
                    td);
            if (depth != 0) {
                clone.setChildren(cloneTypes(tc.getChildren(), depth - 1,
                        includePropertyDefinitions));
            }
            res.add(clone);
        }
        return res;
    }

}
