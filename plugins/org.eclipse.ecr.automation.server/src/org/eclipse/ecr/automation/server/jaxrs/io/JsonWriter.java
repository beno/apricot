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
package org.eclipse.ecr.automation.server.jaxrs.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.ecr.automation.AutomationService;
import org.eclipse.ecr.automation.OperationDocumentation;
import org.eclipse.ecr.automation.OperationDocumentation.Param;
import org.eclipse.ecr.automation.server.jaxrs.AutomationInfo;
import org.eclipse.ecr.automation.server.jaxrs.ExceptionHandler;
import org.eclipse.ecr.automation.server.jaxrs.LoginInfo;
import org.eclipse.ecr.runtime.api.Framework;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class JsonWriter {

    protected static JsonFactory factory = createFactory();

    public static JsonFactory createFactory() {
        factory = new JsonFactory();
        factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        final ObjectMapper oc = new ObjectMapper(factory);
        factory.setCodec(oc);
        return factory;
    }

    public static JsonFactory getFactory() {
        return factory;
    }

    public static JsonGenerator createGenerator(OutputStream out)
            throws IOException {
        return factory.createJsonGenerator(out, JsonEncoding.UTF8);
    }

    public static void writeAutomationInfo(OutputStream out,
            AutomationInfo info, boolean prettyPrint) throws IOException {
        writeAutomationInfo(createGenerator(out), info, prettyPrint);
    }

    public static void writeAutomationInfo(JsonGenerator jg,
            AutomationInfo info, boolean prettyPrint) throws IOException {
        if (prettyPrint) {
            jg.useDefaultPrettyPrinter();
        }
        jg.writeStartObject();
        writePaths(jg);
        writeCodecs(jg);
        writeOperations(jg, info);
        writeChains(jg, info);
        jg.writeEndObject();
        jg.flush();
    }

    private static void writePaths(JsonGenerator jg) throws IOException {
        jg.writeObjectFieldStart("paths");
        jg.writeStringField("login", "login");
        jg.writeEndObject();
    }

    private static void writeCodecs(JsonGenerator jg) throws IOException {
        jg.writeArrayFieldStart("codecs");
        ObjectCodecService codecs = Framework.getLocalService(ObjectCodecService.class);
        for (ObjectCodec<?> codec : codecs.getCodecs()) {
            if (!codec.isBuiltin()) {
                jg.writeString(codec.getClass().getName());
            }
        }
        jg.writeEndArray();
    }

    /**
     * Used to export operations to studio
     *
     * @param info
     * @return
     * @throws IOException
     */
    public static String exportOperations() throws IOException {
        List<OperationDocumentation> ops = Framework.getLocalService(
                AutomationService.class).getDocumentation();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator jg = factory.createJsonGenerator(out);
        jg.useDefaultPrettyPrinter();
        jg.writeStartObject();
        jg.writeArrayFieldStart("operations");
        for (OperationDocumentation op : ops) {
            writeOperation(jg, op);
        }
        jg.writeEndArray();
        jg.writeEndObject();
        jg.flush();
        return out.toString("UTF-8");
    }

    private static void writeOperations(JsonGenerator jg, AutomationInfo info)
            throws IOException {
        jg.writeArrayFieldStart("operations");
        for (OperationDocumentation op : info.getOperations()) {
            writeOperation(jg, op);
        }
        jg.writeEndArray();
    }

    private static void writeChains(JsonGenerator jg, AutomationInfo info)
            throws IOException {
        jg.writeArrayFieldStart("chains");
        for (OperationDocumentation op : info.getChains()) {
            writeOperation(jg, op, "Chain." + op.id);
        }
        jg.writeEndArray();
    }

    public static void writeOperation(OutputStream out,
            OperationDocumentation op) throws IOException {
        writeOperation(createGenerator(out), op, op.url);
    }

    public static void writeOperation(JsonGenerator jg,
            OperationDocumentation op) throws IOException {
        writeOperation(jg, op, op.url);
    }

    public static void writeOperation(JsonGenerator jg,
            OperationDocumentation op, String url) throws IOException {
        jg.writeStartObject();
        jg.writeStringField("id", op.id);
        jg.writeStringField("label", op.label);
        jg.writeStringField("category", op.category);
        jg.writeStringField("requires", op.requires);
        jg.writeStringField("description", op.description);
        if (op.since != null && op.since.length() > 0) {
            jg.writeStringField("since", op.since);
        }
        jg.writeStringField("url", url);
        jg.writeArrayFieldStart("signature");
        for (String s : op.signature) {
            jg.writeString(s);
        }
        jg.writeEndArray();
        writeParams(jg, op.params);
        jg.writeEndObject();
        jg.flush();
    }

    private static void writeParams(JsonGenerator jg, List<Param> params)
            throws IOException {
        jg.writeArrayFieldStart("params");
        for (Param p : params) {
            jg.writeStartObject();
            jg.writeStringField("name", p.name);
            jg.writeStringField("type", p.type);
            jg.writeBooleanField("required", p.isRequired);

            jg.writeStringField("widget", p.widget);
            jg.writeNumberField("order", p.order);
            jg.writeArrayFieldStart("values");
            for (String value : p.values) {
                jg.writeString(value);
            }
            jg.writeEndArray();
            jg.writeEndObject();
        }
        jg.writeEndArray();
    }

    public static void writeLogin(OutputStream out, LoginInfo login)
            throws IOException {
        writeLogin(createGenerator(out), login);
    }

    public static void writeLogin(JsonGenerator jg, LoginInfo login)
            throws IOException {
        jg.writeStartObject();
        jg.writeStringField("entity-type", "login");
        jg.writeStringField("username", login.getUsername());
        jg.writeBooleanField("isAdministrator", login.isAdministrator());
        jg.writeArrayFieldStart("groups");
        for (String group : login.getGroups()) {
            jg.writeString(group);
        }
        jg.writeEndArray();
        jg.writeEndObject();
        jg.flush();
    }

    public static void writePrimitive(OutputStream out, Object value)
            throws IOException {
        writePrimitive(createGenerator(out), value);
    }

    public static void writePrimitive(JsonGenerator jg, Object value)
            throws IOException {
        jg.writeStartObject();
        jg.writeStringField("entity-type", "primitive");
        if (value != null) {
            Class<?> type = value.getClass();
            if (type == String.class) {
                jg.writeStringField("value", (String) value);
            } else if (type == Boolean.class) {
                jg.writeBooleanField("value", (Boolean) value);
            } else if (type == Long.class) {
                jg.writeNumberField("value", ((Number) value).longValue());
            } else if (type == Double.class) {
                jg.writeNumberField("value", ((Number) value).doubleValue());
            } else if (type == Integer.class) {
                jg.writeNumberField("value", ((Number) value).intValue());
            } else if (type == Float.class) {
                jg.writeNumberField("value", ((Number) value).floatValue());
            }
        } else {
            jg.writeNullField("value");
        }
        jg.writeEndObject();
        jg.flush();
    }

    public static void writeException(OutputStream out, ExceptionHandler eh)
            throws IOException {
        writeException(createGenerator(out), eh);
    }

    public static void writeException(JsonGenerator jg, ExceptionHandler eh)
            throws IOException {
        jg.writeStartObject();
        jg.writeStringField("entity-type", "exception");
        jg.writeStringField("type", eh.getType());
        jg.writeNumberField("status", eh.getStatus());
        jg.writeStringField("message", eh.getMessage());
        jg.writeStringField("stack", eh.getSerializedStackTrace());
        jg.writeObjectField("cause", eh.getCause());
        jg.writeEndObject();
        jg.flush();
    }

}
