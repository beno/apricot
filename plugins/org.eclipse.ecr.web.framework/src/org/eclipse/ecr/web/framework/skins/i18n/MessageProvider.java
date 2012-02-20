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
 *
 */

package org.eclipse.ecr.web.framework.skins.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ecr.web.framework.skins.SkinManager;

/**
 * @author bstefanescu
 *
 */
public class MessageProvider {

	private Log log = LogFactory.getLog(MessageProvider.class);
	
	protected volatile DefaultMessageBundle defaultMessages;
	
	protected ConcurrentMap<String, ResourceBundle> messages;
	
	protected SkinManager mgr;
	
	public MessageProvider(SkinManager mgr) {
		this.mgr = mgr;
		messages = new ConcurrentHashMap<String, ResourceBundle>();
	}
	
	public String getMessage(String language, String key) {
		ResourceBundle bundle = getMessages(language);
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return new StringBuilder().append('!').append(key).append('!').toString();
		}
	}
	
	public String getMessage(String language, String key, Object ... args) {
		String msg = getMessage(language, key);
		if (args != null && args.length > 0) {
			msg = MessageFormat.format(msg, args);
		}
		return msg;
	}
	
	public String getMessage(String language, String key, List<Object> args) {
		String msg = getMessage(language, key);
		if (args != null && !args.isEmpty()) {
			msg = MessageFormat.format(msg, args);
		}
		return msg;
	}
	
	public ResourceBundle getMessages(String language) {
		if (language == null) {
			return getDefaultMessages();
		}
		ResourceBundle r = messages.get(language);
		if (r == null) {
			//TODO -security pronlem here - if user type random langauges this will grow the cache - must not store inexisting languages
			synchronized (this) {
				r = computeMessageBundle(language);
				messages.put(language, r);
			}
		}
		return r;
	}
	
	public DefaultMessageBundle getDefaultMessages() {
		DefaultMessageBundle r = defaultMessages;
		if (r == null) {
			synchronized (this) {
				r = computeDefaultMessages();
				defaultMessages = r;
			}
		}
		return r;
	}
	
	private DefaultMessageBundle computeDefaultMessages() {
		try {
			Map<String, String> properties = loadProperties("/i18n/messages.properties");
			return new DefaultMessageBundle(properties == null ? new HashMap<String, String>() : properties);
		} catch (IOException e) {
			log.error("Failed to load default message bundle", e);			
		}
		return new DefaultMessageBundle(new HashMap<String, String>());
	}
	
	private ResourceBundle computeMessageBundle(String language) {
		DefaultMessageBundle parent = getDefaultMessages();
		try {
			String location = new StringBuilder().append("/i18n/messages_").append(language).append(
			".properties").toString();			
			Map<String, String> properties = loadProperties(location);
			return properties == null ? parent : new MessageBundle(parent, properties);
		} catch (IOException e) {
			log.error("Failed to load message bundle for language: "+language, e);
		}
		return parent;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, String> loadProperties(String location) throws IOException {
		List<URL> urls = mgr.resolveAll(location);
		if (urls.isEmpty()) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		// need to reverse the list to add last the files with a lesser order.
		for (int i=urls.size()-1; i>=0; i--) {
			URL url = urls.get(i);
			InputStream in =  url.openStream(); 
			Properties props = new Properties();
			try {
				props.load(in);
				map.putAll((Map)props);
			} finally {
				in.close();
			}
		}
		return map;
	}
	
}
