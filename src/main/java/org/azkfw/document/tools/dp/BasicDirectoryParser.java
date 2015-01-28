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
package org.azkfw.document.tools.dp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * このクラスは、標準のディレクトリ解析を行うクラスです。
 * 
 * @since 1.0.0
 * @version 1.0.0 2015/01/28
 * @author kawakicchi
 */
public class BasicDirectoryParser implements DirectoryParser {

	private DirectoryParserEvent event;
	private List<DirectoryParserListener> listeners;

	private DirectoryParserDecorator decorator;

	public BasicDirectoryParser() {
		event = new DirectoryParserEvent(this);
		listeners = new ArrayList<DirectoryParserListener>();
	}

	@Override
	public final void addListener(final DirectoryParserListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public final void removeListener(final DirectoryParserListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
	public void setDecorator(final DirectoryParserDecorator decorator) {
		this.decorator = decorator;
	}

	@Override
	public void parse(final File directory) {
		if (null == decorator) {
			decorator = new DirectoryParserPlainDecorator();
		}

		nest(directory, "");
	}

	private void nest(final File file, final String prefix) {
		/*
		 * if (file.getName().startsWith(".") ||
		 * "target".equals(file.getName())) { return; }
		 */

		DirectoryParserFileInfo fi = new DirectoryParserFileInfo(file, prefix);
		synchronized (listeners) {
			for (DirectoryParserListener listener : listeners) {
				listener.directoryParserFindFile(event, fi);
			}
		}

		if (file.isFile()) {

		} else if (file.isDirectory()) {
			List<File> files = sort(file.listFiles());
			for (int i = 0; i < files.size(); i++) {

				String bufPrefix = prefix;
				if (bufPrefix.endsWith(decorator.getMiddleString())) {
					bufPrefix = bufPrefix.substring(0, bufPrefix.length() - 1) + decorator.getParentJoinString();
				} else if (bufPrefix.endsWith(decorator.getTerminalString())) {
					bufPrefix = bufPrefix.substring(0, bufPrefix.length() - 1) + decorator.getParentEmptyString();
				}
				if (i < files.size() - 1) {
					bufPrefix += decorator.getMiddleString();
				} else {
					bufPrefix += decorator.getTerminalString();
				}

				File f = files.get(i);

				nest(f, bufPrefix);
			}
		} else {

		}
	}

	private List<File> sort(final File[] files) {
		List<File> list = new ArrayList<File>();
		for (File f : files) {
			list.add(f);
		}

		Collections.sort(list, new Comparator<File>() {
			@Override
			public int compare(final File f1, final File f2) {
				if (f1.isFile() && f2.isDirectory()) {
					return 1;
				} else if (f1.isDirectory() && f2.isFile()) {
					return -1;
				} else if (f1.isFile() == f2.isFile() || f1.isDirectory() == f2.isDirectory()) {
					return f1.getName().compareTo(f2.getName());
				}
				return 0;
			}
		});

		return list;
	}
}
