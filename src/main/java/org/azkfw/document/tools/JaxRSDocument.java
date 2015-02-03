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
package org.azkfw.document.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.azkfw.document.tools.parser.ws.rsp.BasicRESTfulParser;
import org.azkfw.document.tools.parser.ws.rsp.RESTfulParser;
import org.azkfw.document.tools.parser.ws.rsp.RESTfulParserEvent;
import org.azkfw.document.tools.parser.ws.rsp.RESTfulParserListener;

/**
 * このクラスは、JAX-RS構成をエクセル出力するクラスです。
 * 
 * @since 1.0.0
 * @version 1.0.0 2015/01/30
 * @author kawakicchi
 */
public class JaxRSDocument {

	/**
	 * メイン関数
	 * 
	 * <p>
	 * 引数は次の通りです。
	 * <ul>
	 * <li>クラスパス、jarファイルパス、warファイルパス</li>
	 * <li>出力エクセルファイルパス</li>
	 * </ul>
	 * </p>
	 * 
	 * @param args 引数
	 */
	public static void main(final String[] args) {
		File document = new File(args[0]);
		File destFile = new File(args[1]);

		JaxRSDocument doc = new JaxRSDocument();
		doc.create(document, destFile);
	}

	public void create(final File document, final File destFile) {
		RESTfulParser parser = new BasicRESTfulParser();
		parser.addListener(new RESTfulParserListener() {
			@Override
			public void documentParserCallback(final RESTfulParserEvent event) {
			}
		});
		parser.parse(document);
	}

	public static void main2(final String[] args) throws IOException, ClassNotFoundException {

		JarFile jarFile = new JarFile(args[0]);

		Manifest manifest = jarFile.getManifest(); //マニフェストの取得
		// jarファイル内のファイルとディレクトリを表示
		printEntries(jarFile);

		// マニフェストの内容を表示
		printManifestAttributes(manifest);

		// jarファイル内のファイルを読み込む
		printFile(jarFile, "META-INF/MANIFEST.MF");

		// マニフェストの属性取得
		String className = getManifestAttribute(manifest, "JarCall-Class");
		System.out.println("[JarCall-Class]=[" + className + "]");
	}

	/**
	 * jarファイル内のファイルとディレクトリの一覧を表示する
	 * 
	 * @param jarFile jarファイル
	 */
	private static void printEntries(JarFile jarFile) {
		for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {
			JarEntry entry = e.nextElement();
			String dir = entry.isDirectory() ? "D" : "F";
			System.out.printf("[%s]%s%n", dir, entry.getName());
		}
	}

	/**
	 * マニフェストの内容を全て表示する
	 * 
	 * @param manifest マニフェスト
	 */
	private static void printManifestAttributes(Manifest manifest) {
		Attributes ma = manifest.getMainAttributes();
		for (Iterator<Object> i = ma.keySet().iterator(); i.hasNext();) {
			Object key = i.next();
			String val = (String) ma.get(key);
			System.out.printf("[%s]=[%s]%n", key, val);
		}
	}

	/**
	 * jarファイル内のファイルの内容を出力する
	 * 
	 * @param jarFile jarファイル
	 * @param name ファイル名
	 * @throws IOException
	 */
	private static void printFile(JarFile jarFile, String name) throws IOException {
		JarEntry entry = jarFile.getJarEntry(name);
		BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)));
		String line;
		while (null != (line = reader.readLine())) {
			System.out.println(line);
		}
	}

	/**
	 * マニフェストの属性を取得する
	 * 
	 * @param manifest マニフェスト
	 * @param key キー
	 * @return 値
	 */
	private static String getManifestAttribute(final Manifest manifest, String key) {
		Attributes a = manifest.getMainAttributes();
		return a.getValue(key);
	}

}
