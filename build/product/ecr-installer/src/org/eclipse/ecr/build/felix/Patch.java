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
package org.eclipse.ecr.build.felix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.ecr.build.Utils;

/**
 * @author bstefanescu
 *
 */
public abstract class Patch {

	private static final String LUCENE_CORE = "org.apache.lucene.core";
	
	private static final String JAVAX_TRANSACTION = "javax.transaction";
	
	private static final String H2 = "org.h2";
	
	private static final Patch[] PATCHES = new Patch[] {
		new PatchLucene(),
		new PatchJavaxTransaction(),
		new PatchH2()
	};	
		
	
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Usage: Patch plugins_dir");
			System.exit(1);
		}
		
		File plugins = new File(args[0]);

		applyPatches(plugins);		
	}
	
	
	protected static void applyPatches(File plugins) throws Exception {
		String[] list = plugins.list();
		if (list == null) {
			return;
		}		
		
		for (String name : list) {
			for (Patch p : PATCHES) {
				if (p.accept(name)) {
					System.out.println("Apply patch on "+name);
					p.run(new File(plugins, name));
				}
			}
		}
	}
	
	public static Manifest getManifest(File bundle) throws Exception {
		JarFile jar = new JarFile(bundle);
		try { 
			ZipEntry entry = jar.getEntry("META-INF/MANIFEST.MF");
			return new Manifest(jar.getInputStream(entry));
		} finally {
			jar.close();
		}
	}

	public static void setManifest(File bundle, Manifest mf) throws Exception {
		File tmp = File.createTempFile("patched-bundle-", ".jar");
		tmp.deleteOnExit();
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tmp));
		JarFile jar = new JarFile(bundle);		
		try { 
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					out.putNextEntry(new ZipEntry(entry));
				} else if (entry.getName().equals("META-INF/MANIFEST.MF")) {
					ZipEntry zentry = new ZipEntry("META-INF/MANIFEST.MF");
					out.putNextEntry(zentry);
					mf.write(out);
				} else {
					ZipEntry zentry = new ZipEntry(entry);
					out.putNextEntry(zentry);
					InputStream in = jar.getInputStream(entry);
					try {
						Utils.copy(in, out);
					} finally {
						in.close();
					}					
				}
			}
		} finally {
			try {
				jar.close();
			} finally {
				out.close();
			}
		}
		
		Utils.copyFile(tmp, bundle);
		tmp.delete();
	}

	protected abstract void run(File jar) throws Exception;
	
	protected abstract boolean accept(String fileName);
	
	static class PatchLucene extends Patch {
		
		@Override
		protected boolean accept(String fileName) {
			return fileName.startsWith(LUCENE_CORE);
		}
		
		@Override
		protected void run(File jar) throws Exception {
			Manifest mf = getManifest(jar);
			String value = mf.getMainAttributes().getValue("Export-Package");
			value = value.replace(";mandatory:=core", "");
			mf.getMainAttributes().putValue("Export-Package", value);
			setManifest(jar, mf);
			System.out.println("Patch done on "+jar.getName());
		}
		
	}

	static class PatchJavaxTransaction extends Patch {
		
		@Override
		protected boolean accept(String fileName) {
			return fileName.startsWith(JAVAX_TRANSACTION);
		}
		
		@Override
		protected void run(File jar) throws Exception {
			Manifest mf = getManifest(jar);
			String value = mf.getMainAttributes().getValue("Fragment-Host");
			if (!value.contains("extension:=framework")) {
				value += ";extension:=framework";
				mf.getMainAttributes().putValue("Fragment-Host", value);
				setManifest(jar, mf);
				System.out.println("Patch done on "+jar.getName());
			} else {
				System.out.println("No patch needed on "+jar.getName());
			}
		}
	}
	
	static class PatchH2 extends Patch {
		@Override
		protected boolean accept(String fileName) {
			return fileName.startsWith(H2);
		}
		
		@Override
		protected void run(File jar) throws Exception {
			Manifest mf = getManifest(jar);
			String value = mf.getMainAttributes().getValue("Import-Package");
			if (value == null) {
				value = "javax.sql,javax.naming,javax.naming.spi,javax.transaction.xa";
				mf.getMainAttributes().putValue("Import-Package", value);
				setManifest(jar, mf);
				System.out.println("Patch done on "+jar.getName());
			} else {
				System.out.println("No patch needed on "+jar.getName());
			}
		}
	}

}
