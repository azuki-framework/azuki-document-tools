/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.azkfw.document.tools.parser.ws.rsp;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.azkfw.biz.zip.ZipUtility;
import org.azkfw.util.FileUtility;
import org.azkfw.util.UUIDUtility;

/**
 * @since 1.0.0
 * @version 1.0.0 2015/01/30
 * @author kawakicchi
 */
public class MyClassLoader {

	public static ClassLoader newInstance(final File file) {
		ClassLoader cl = null;

		File tempDir = null;
		try {
			List<String> classPaths = new ArrayList<String>();

			if (file.isDirectory()) {
				// get class file
				List<File> files = new ArrayList<File>();
				getClassFile(file, files);

				// create class loader
				cl = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() }, ClassLoader.getSystemClassLoader());

				// class
				int preSize = file.getAbsolutePath().length() + 1;
				for (File f : files) {
					String s = f.getAbsolutePath().substring(preSize, f.getAbsolutePath().length() - 6);
					s = s.replaceAll("\\\\", ".");
					s = s.replaceAll("/", ".");
					classPaths.add(s);
				}
			} else {
				if (file.getName().toLowerCase().endsWith(".jar")) {

					// create class loader
					cl = URLClassLoader.newInstance(new URL[] { file.toURI().toURL() }, ClassLoader.getSystemClassLoader());

					// class
					JarFile jarFile = new JarFile(file);
					for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {
						JarEntry entry = e.nextElement();
						if (!entry.isDirectory()) {
							if (entry.getName().toLowerCase().endsWith(".class")) {
								String s = entry.getName();
								s = s.substring(0, s.length() - 6);
								s = s.replaceAll("\\\\", ".");
								s = s.replaceAll("/", ".");
								classPaths.add(s);
							}
						}
					}
					jarFile.close();
				} else if (file.getName().toLowerCase().endsWith(".war")) {
					tempDir = Paths.get("temp", UUIDUtility.generateToShortString()).toFile();
					tempDir.mkdirs();
					ZipUtility.unzip(file.getAbsolutePath(), tempDir.getAbsolutePath());
					Path classFilePath = Paths.get(tempDir.getAbsolutePath(), "WEB-INF", "classes");

					// get class file
					List<File> files = new ArrayList<File>();
					getClassFile(classFilePath.toFile(), files);

					// create class loader
					List<URL> urls = new ArrayList<URL>();
					urls.add(classFilePath.toUri().toURL());
					File fs = Paths.get(tempDir.getAbsolutePath(), "WEB-INF", "lib").toFile();
					for (File f : fs.listFiles()) {
						if (f.isFile()) {
							if (f.getName().toLowerCase().endsWith(".jar")) {
								urls.add(f.toURI().toURL());
							}
						}
					}
					cl = URLClassLoader.newInstance(urls.toArray(new URL[] {}), ClassLoader.getSystemClassLoader());

					// class
					int preSize = classFilePath.toFile().getAbsolutePath().length() + 1;
					for (File f : files) {
						String s = f.getAbsolutePath().substring(preSize, f.getAbsolutePath().length() - 6);
						s = s.replaceAll("\\\\", ".");
						s = s.replaceAll("/", ".");
						classPaths.add(s);
					}
				}
			}

			for (String path : classPaths) {
				System.out.println(path);
				cl.loadClass(path);
			}

		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (null != tempDir) {
				FileUtility.remove(tempDir);
			}
		}
		return cl;
	}

	private static void getClassFile(final File file, final List<File> files) {
		if (file.isFile()) {
			if (file.getName().toLowerCase().endsWith(".class")) {
				files.add(file);
			}
		} else {
			File[] fs = file.listFiles();
			for (File f : fs) {
				getClassFile(f, files);
			}
		}
	}
}
