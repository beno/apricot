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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.ecr.automation.OperationChain;
import org.eclipse.ecr.automation.OperationContext;
import org.eclipse.ecr.automation.OperationParameters;
import org.eclipse.ecr.automation.OperationType;
import org.eclipse.ecr.automation.core.scripting.Scripting;
import org.eclipse.ecr.core.api.CoreSession;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class ExecutionRequest {

    protected Object input;

    protected RestOperationContext ctx;

    protected Map<String, Object> params;

    public ExecutionRequest() {
        this (null);
    }

    public ExecutionRequest(Object input) {
        ctx = new RestOperationContext();
        this.input = input;
        this.params = new HashMap<String, Object>();
    }

    public void setInput(Object input) {
        this.input = input;
    }

    public Object getInput() {
        return input;
    }

    public void setContextParam(String key, Object value) {
        ctx.put(key, value);
    }

    public void setContextParam(String key, String value) {
        ctx.put(key, value);
    }

    public void setParam(String key, Object jsonObject) {
        params.put(key, jsonObject);
    }

    public void setParam(String key, String value) {
        if (value.startsWith("expr:")) {
            value = value.substring(5).trim();
            if (value.contains("@{")) {
                params.put(key, Scripting.newTemplate(value));
            } else {
                params.put(key, Scripting.newExpression(value));
            }
        } else {
            params.put(key, value);
        }
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public OperationContext createContext(HttpServletRequest request,
            CoreSession session) throws Exception {
        ctx.addRequestCleanupHandler(request);
        ctx.setCoreSession(session);
        ctx.setInput(input);
        ctx.put("request", request);
        return ctx;
    }

    public OperationChain createChain(OperationType op) {
        OperationChain chain = new OperationChain("operation");
        OperationParameters oparams = new OperationParameters(op.getId(),
                params);
        chain.add(oparams);
        return chain;
    }
}
