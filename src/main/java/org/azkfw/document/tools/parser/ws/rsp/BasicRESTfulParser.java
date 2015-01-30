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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.azkfw.document.tools.parser.AbstractDocumentParser;

/**
 * @since 1.0.0
 * @version 1.0.0 2015/01/30
 * @author kawakicchi
 */
public class BasicRESTfulParser extends AbstractDocumentParser<File, RESTfulParserEvent, RESTfulParserListener> implements RESTfulParser {

	public class ClassData {
		private String path;
		private Set<String> consumes;
		private Set<String> produces;

		public ClassData() {
			path = "";
			consumes = new HashSet<String>();
			produces = new HashSet<String>();
		}

		public ClassData(final ClassData data) {
			path = data.path;
			consumes = new HashSet<String>(data.consumes);
			produces = new HashSet<String>(data.produces);
		}

		public void setPath(final String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}

		public void addConsume(final String consume) {
			consumes.add(consume);
		}

		public void addProduce(final String produce) {
			produces.add(produce);
		}
	}

	public class MethodData extends ClassData {

		private Set<String> methodTypes;

		public MethodData() {
			methodTypes = new HashSet<String>();
		}

		public MethodData(final ClassData data) {
			super(data);
			methodTypes = new HashSet<String>();
		}

		public void addMethodType(final String type) {
			methodTypes.add(type);
		}
	}

	private ClassData getClassData(final Class<?> clazz) {
		ClassData data = new ClassData();
		Path path = clazz.getAnnotation(Path.class);
		if (null != path) {
			data.setPath(path.value());
		}
		Consumes consumes = clazz.getAnnotation(Consumes.class);
		if (null != consumes) {
			for (String s : consumes.value()) {
				data.addConsume(s);
			}
		}
		Produces produces = clazz.getAnnotation(Produces.class);
		if (null != produces) {
			for (String s : produces.value()) {
				data.addProduce(s);
			}
		}
		return data;
	}

	private MethodData getMethodData(final Method method, final ClassData clazz) {
		MethodData data = new MethodData(clazz);
		Path path = method.getAnnotation(Path.class);
		if (null != path) {
			java.nio.file.Path p = Paths.get(clazz.getPath(), path.value());
			data.setPath(p.toString());
		} else {
			return null;
		}
		GET get = method.getAnnotation(GET.class);
		if (null != get) {
			data.addMethodType("GET");
		}
		POST post = method.getAnnotation(POST.class);
		if (null != post) {
			data.addMethodType("POST");
		}
		PUT put = method.getAnnotation(PUT.class);
		if (null != put) {
			data.addMethodType("PUT");
		}
		DELETE delete = method.getAnnotation(DELETE.class);
		if (null != delete) {
			data.addMethodType("DELETE");
		}
		HEAD head = method.getAnnotation(HEAD.class);
		if (null != head) {
			data.addMethodType("HEAD");
		}
		Consumes consumes = method.getAnnotation(Consumes.class);
		if (null != consumes) {
			for (String s : consumes.value()) {
				data.addConsume(s);
			}
		}
		Produces produces = method.getAnnotation(Produces.class);
		if (null != produces) {
			for (String s : produces.value()) {
				data.addProduce(s);
			}
		}
		return data;
	}

	@Override
	protected final void doParse(final File document) {
		String basePath = "WEB-INF/classes/";
		String targetClassPath = "org.azkfw";

		String targetDir = targetClassPath.replace(".", "/");
		if (!targetDir.endsWith("/")) {
			targetDir += "/";
		}
		targetDir = basePath + targetDir;

		try {
			URL[] urls = { document.toURI().toURL() };
			ClassLoader loader = URLClassLoader.newInstance(urls);

			JarFile jarFile = new JarFile(document);
			for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {

				JarEntry entry = e.nextElement();
				if (!entry.isDirectory()) {
					String name = entry.getName();
					System.out.println(name);
					if (name.startsWith(targetDir) && name.endsWith(".class")) {

						String classPath = name.substring(basePath.length(), name.length() - 6).replace("/", ".");
						Class<?> clazz = loader.loadClass(classPath);

						ClassData classData = getClassData(clazz);

						Path pathClass = clazz.getAnnotation(Path.class);
						if (null != pathClass) {

							Method[] methods = clazz.getMethods();
							for (Method method : methods) {

								MethodData methodData = getMethodData(method, classData);
								if (null != methodData) {
									System.out.println(methodData.getPath());
								}

							}
						}

					}
				}
			}
			jarFile.close();

		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
