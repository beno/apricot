/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.ecr.opencmis.impl.server;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.PropertyBoolean;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.eclipse.ecr.core.api.DocumentModel;

/**
 * Base abstract class for a live property of an object.
 * <p>
 * Concrete classes must also implement one of {@link PropertyId},
 * {@link PropertyString}, ...
 *
 * @see NuxeoPropertyData
 */
public abstract class NuxeoPropertyDataBase<T> implements PropertyData<T> {

    protected final PropertyDefinition<T> propertyDefinition;

    protected final DocumentModel doc;

    public NuxeoPropertyDataBase(PropertyDefinition<T> propertyDefinition,
            DocumentModel doc) {
        this.propertyDefinition = propertyDefinition;
        this.doc = doc;
    }

    public PropertyDefinition<T> getPropertyDefinition() {
        return propertyDefinition;
    }

    @Override
    public String getId() {
        return propertyDefinition.getId();
    }

    @Override
    public String getLocalName() {
        return propertyDefinition.getLocalName();
    }

    @Override
    public String getDisplayName() {
        return propertyDefinition.getDisplayName();
    }

    @Override
    public String getQueryName() {
        return propertyDefinition.getQueryName();
    }

    @SuppressWarnings("unchecked")
    public <U> U getValue() {
        return (U) getFirstValue();
    }

    @Override
    public abstract T getFirstValue();

    @Override
    public List<T> getValues() {
        return Collections.singletonList(getFirstValue());
    }

    public void setValue(Object value) {
        Serializable old = null;
        if (value == null && old == null) {
            return;
        }
        if (value != null && value.equals(old)) {
            return;
        }
        throw new CmisConstraintException("Read-only property: "
                + propertyDefinition.getId());
    }

    @Override
    public List<CmisExtensionElement> getExtensions() {
        return null;
    }

    @Override
    public void setExtensions(List<CmisExtensionElement> extensions) {
        throw new UnsupportedOperationException();
    }

    /**
     * A fixed property (whose value cannot be changed).
     */
    public static abstract class NuxeoPropertyDataFixed<T> extends
            NuxeoPropertyDataBase<T> {

        protected final T value;

        protected NuxeoPropertyDataFixed(
                PropertyDefinition<T> propertyDefinition, T value) {
            super(propertyDefinition, null);
            this.value = value;
        }

        @Override
        public T getFirstValue() {
            return value;
        }
    }

    /**
     * A fixed multi-valued property (whose value cannot be changed).
     */
    public static abstract class NuxeoPropertyMultiDataFixed<T> extends
            NuxeoPropertyDataBase<T> {

        protected final List<T> value;

        protected NuxeoPropertyMultiDataFixed(
                PropertyDefinition<T> propertyDefinition, List<T> value) {
            super(propertyDefinition, null);
            this.value = value;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> U getValue() {
            return (U) getValues();
        }

        @Override
        public T getFirstValue() {
            return value.size() == 0 ? null : value.get(0);
        }

        @Override
        public List<T> getValues() {
            return value;
        }
    }

    /**
     * A fixed ID property.
     */
    public static class NuxeoPropertyIdDataFixed extends
            NuxeoPropertyDataFixed<String> implements PropertyId {

        protected NuxeoPropertyIdDataFixed(
                PropertyDefinition<String> propertyDefinition, String value) {
            super(propertyDefinition, value);
        }
    }

    /**
     * A fixed multi-ID property.
     */
    public static class NuxeoPropertyIdMultiDataFixed extends
            NuxeoPropertyMultiDataFixed<String> implements PropertyId {

        protected NuxeoPropertyIdMultiDataFixed(
                PropertyDefinition<String> propertyDefinition,
                List<String> value) {
            super(propertyDefinition, value);
        }
    }

    /**
     * A fixed String property.
     */
    public static class NuxeoPropertyStringDataFixed extends
            NuxeoPropertyDataFixed<String> implements PropertyString {

        protected NuxeoPropertyStringDataFixed(
                PropertyDefinition<String> propertyDefinition, String value) {
            super(propertyDefinition, value);
        }

    }

    /**
     * A fixed Boolean property.
     */
    public static class NuxeoPropertyBooleanDataFixed extends
            NuxeoPropertyDataFixed<Boolean> implements PropertyBoolean {

        protected NuxeoPropertyBooleanDataFixed(
                PropertyDefinition<Boolean> propertyDefinition, Boolean value) {
            super(propertyDefinition, value);
        }

    }
}
