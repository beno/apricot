/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ataillefer
 */

package org.eclipse.ecr.core.io.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.QName;
import org.eclipse.ecr.common.collections.PrimitiveArrays;
import org.eclipse.ecr.common.utils.Path;
import org.eclipse.ecr.core.api.Blob;
import org.eclipse.ecr.core.api.DocumentModel;
import org.eclipse.ecr.core.schema.Namespace;
import org.eclipse.ecr.core.schema.TypeConstants;
import org.eclipse.ecr.core.schema.types.ComplexType;
import org.eclipse.ecr.core.schema.types.Field;
import org.eclipse.ecr.core.schema.types.ListType;
import org.eclipse.ecr.core.schema.types.Type;
import org.eclipse.ecr.core.schema.types.primitives.BinaryType;
import org.eclipse.ecr.core.schema.types.primitives.BooleanType;
import org.eclipse.ecr.core.schema.types.primitives.DateType;
import org.eclipse.ecr.core.schema.types.primitives.DoubleType;
import org.eclipse.ecr.core.schema.types.primitives.IntegerType;
import org.eclipse.ecr.core.schema.types.primitives.LongType;
import org.eclipse.ecr.core.schema.types.primitives.StringType;

/**
 * A representation for an exported document aware of property types.
 * 
 * @author <a href="mailto:ataillefer@nuxeo.com">Antoine Taillefer</a>
 * @since 5.6
 */
public class TypedExportedDocumentImpl extends ExportedDocumentImpl {

    private static final String TYPE_ATTRIBUTE = "type";

    private static final String COMPLEX_TYPE_ID = "complex";

    private static final String SCALAR_LIST_TYPE_ID = "scalarList";

    private static final String COMPLEX_LIST_TYPE_ID = "complexList";

    public TypedExportedDocumentImpl() {
        super();
    }

    /**
     * Instantiates a new typed exported document impl.
     * 
     * @param doc the doc
     * @param path the path to use for this document this is used to remove full
     *            paths
     * @param inlineBlobs the inline blobs
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public TypedExportedDocumentImpl(DocumentModel doc, Path path,
            boolean inlineBlobs) throws IOException {
        super(doc, path, inlineBlobs);
    }

    /**
     * Instantiates a new typed exported document impl.
     * 
     * @param doc the doc
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public TypedExportedDocumentImpl(DocumentModel doc) throws IOException {
        super(doc, false);
    }

    /**
     * Instantiates a new typed exported document impl.
     * 
     * @param doc the doc
     * @param inlineBlobs the inline blobs
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public TypedExportedDocumentImpl(DocumentModel doc, boolean inlineBlobs)
            throws IOException {
        super(doc, doc.getPath(), inlineBlobs);
    }

    /**
     * Here we do what super does but add the "type" attribute to the XML
     * elements.
     */
    @Override
    @SuppressWarnings("rawtypes")
    protected void readProperty(Element parent, Namespace targetNs,
            Field field, Object value, boolean inlineBlobs) throws IOException {
        Type type = field.getType();
        QName name = QName.get(field.getName().getLocalName(), targetNs.prefix,
                targetNs.uri);
        Element element = parent.addElement(name);

        // extract the element content
        if (type.isSimpleType()) {
            element.addAttribute(TYPE_ATTRIBUTE, getSimpleTypeId(type));
            if (value != null) {
                element.addText(type.encode(value));
            }
        } else if (type.isComplexType()) {
            ComplexType ctype = (ComplexType) type;
            if (TypeConstants.isContentType(ctype)) {
                element.addAttribute(TYPE_ATTRIBUTE, TypeConstants.CONTENT);
                if (value != null) {
                    readBlob(element, ctype, (Blob) value, inlineBlobs);
                }
            } else {
                element.addAttribute(TYPE_ATTRIBUTE, COMPLEX_TYPE_ID);
                if (value != null) {
                    readComplex(element, ctype, (Map) value, inlineBlobs);
                }
            }
        } else if (type.isListType()) {
            element.addAttribute(TYPE_ATTRIBUTE,
                    ((ListType) type).isScalarList() ? SCALAR_LIST_TYPE_ID
                            : COMPLEX_LIST_TYPE_ID);
            if (value != null) {
                if (value instanceof List) {
                    readList(element, (ListType) type, (List) value,
                            inlineBlobs);
                } else if (value.getClass().getComponentType() != null) {
                    readList(element, (ListType) type,
                            PrimitiveArrays.toList(value), inlineBlobs);
                } else {
                    throw new IllegalArgumentException(
                            "A value of list type is neither list neither array: "
                                    + value);
                }
            }
        }
    }

    /**
     * Gets the simple type id.
     * 
     * @param type the type
     * @return the simple type id
     */
    protected String getSimpleTypeId(Type type) {

        String typeId = StringType.ID;

        if (BooleanType.INSTANCE == type) {
            typeId = BooleanType.ID;
        } else if (DateType.INSTANCE == type) {
            typeId = DateType.ID;
        } else if (LongType.INSTANCE == type) {
            typeId = LongType.ID;
        } else if (IntegerType.INSTANCE == type) {
            typeId = IntegerType.ID;
        } else if (DoubleType.INSTANCE == type) {
            typeId = DoubleType.ID;
        } else if (BinaryType.INSTANCE == type) {
            typeId = BinaryType.ID;
        }
        return typeId;

    }

}
