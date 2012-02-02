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

package org.eclipse.ecr.common.xmap;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ecr.common.xmap.annotation.XNodeMap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
@SuppressWarnings( { "SuppressionAnnotation" })
public class XAnnotatedMap extends XAnnotatedList {

    protected static final ElementMapVisitor elementMapVisitor = new ElementMapVisitor();

    protected static final ElementValueMapVisitor elementVisitor = new ElementValueMapVisitor();

    protected static final AttributeValueMapVisitor attributeVisitor = new AttributeValueMapVisitor();

    protected final Path key;

    protected final boolean isNullByDefault;

    public XAnnotatedMap(XMap xmap, XAccessor setter, XNodeMap anno) {
        super(xmap, setter);
        path = new Path(anno.value());
        trim = anno.trim();
        key = new Path(anno.key());
        type = anno.type();
        componentType = anno.componentType();
        valueFactory = xmap.getValueFactory(componentType);
        xao = xmap.register(componentType);
        isNullByDefault = anno.nullByDefault();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object getValue(Context ctx, Element base)
            throws IllegalAccessException, InstantiationException {
        Map<String, Object> values = (Map) type.newInstance();
        if (xao != null) {
            DOMHelper.visitMapNodes(ctx, this, base, path, elementMapVisitor,
                    values);
        } else {
            if (path.attribute != null) {
                // attribute list
                DOMHelper.visitMapNodes(ctx, this, base, path,
                        attributeVisitor, values);
            } else {
                // element list
                DOMHelper.visitMapNodes(ctx, this, base, path, elementVisitor,
                        values);
            }
        }
        if (isNullByDefault && values.isEmpty()) {
            values = null;
        }
        return values;
    }

    @Override
    public void toXML(Object instance, Element parent) throws Exception {
        Object v = accessor.getValue(instance);
        if (v != null && v instanceof Map<?, ?>) {
            Map<String, ?> map = (Map<String, ?>) v;
            if (xao == null) {
                for (Map.Entry<String, ?> entry : map.entrySet()) {
                    String entryKey = entry.getKey();
                    String value = valueFactory.serialize(null,
                            entry.getValue());
                    Element e = XMLBuilder.addElement(parent, path);
                    Element keyElement = XMLBuilder.getOrCreateElement(e, key);
                    XMLBuilder.fillField(keyElement, entryKey, key.attribute);
                    XMLBuilder.fillField(e, value, null);
                }
            } else {
                for (Map.Entry<String, ?> entry : map.entrySet()) {
                    String entryKey = entry.getKey();
                    Element e = XMLBuilder.addElement(parent, path);
                    Element keyElement = XMLBuilder.getOrCreateElement(e, key);
                    XMLBuilder.fillField(keyElement, entryKey, key.attribute);
                    XMLBuilder.toXML(entry.getValue(), e, xao);
                }
            }
        }
    }
}

class ElementMapVisitor implements DOMHelper.NodeMapVisitor {

    private static final Log log = LogFactory.getLog(ElementMapVisitor.class);

    public void visitNode(Context ctx, XAnnotatedMember xam, Node node,
            String key, Map<String, Object> result) {
        try {
            result.put(key, xam.xao.newInstance(ctx, (Element) node));
        } catch (Exception e) {
            log.error(e, e);
        }
    }
}

class ElementValueMapVisitor implements DOMHelper.NodeMapVisitor {
    public void visitNode(Context ctx, XAnnotatedMember xam, Node node,
            String key, Map<String, Object> result) {
        String val = node.getTextContent();
        if (xam.trim) {
            val = val.trim();
        }
        if (xam.valueFactory != null) {
            result.put(key, xam.valueFactory.deserialize(ctx, val));
        } else {
            // TODO: log warning?
            result.put(key, val);
        }
    }
}

class AttributeValueMapVisitor implements DOMHelper.NodeMapVisitor {
    public void visitNode(Context ctx, XAnnotatedMember xam, Node node,
            String key, Map<String, Object> result) {
        String val = node.getNodeValue();
        if (xam.valueFactory != null) {
            result.put(key, xam.valueFactory.deserialize(ctx, val));
        } else {
            // TODO: log warning?
            result.put(key, val);
        }
    }
}
