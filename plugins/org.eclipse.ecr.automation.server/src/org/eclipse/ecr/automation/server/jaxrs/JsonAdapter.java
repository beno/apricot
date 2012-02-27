/*
 * Copyright (c) 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 */
package org.eclipse.ecr.automation.server.jaxrs;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Should be implemented by objects that needs to be returned
 * in response to clients as JSOn objects
 * <p>
 * Implementors are encouraged to use jackson JSON library since it
 * is the one used by automation.
 * <p>
 * Also <b>note</b> that the JSON format for an object must follow the following schema:
 *
 * <pre>
 * {
 *   "entity-type": "typeName"
 *   "value": { the marshalled object }
 * }
 * </pre>
 *
 * The value is either a scalar value (from primitive types) either a JSON object <code>{ ... }</code>
 * The type name is the full class name of the serialized object.
 * The primitive types are mapped to a short name as following:
 * <ul>
 *   <li> string
 *   <li> date
 *   <li> boolean
 *   <li> long
 *   <li> double
 *   <li> null - this is a special type in case the objec is null - but
 *   this may never happens (since null objects are returning an empty content)
 * </ul>
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface JsonAdapter {

    void toJSON(OutputStream out) throws IOException;

}
