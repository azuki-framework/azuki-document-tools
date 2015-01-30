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
package org.azkfw.document.tools.parser.dp;

/**
 * このクラスは、ディレクトリ解析の装飾をテキストで定義したクラスです。
 * 
 * @since 1.0.0
 * @version 1.0.0 2015/01/28
 * @author kawakicchi
 */
public class DirectoryParserPlainDecorator implements DirectoryParserDecorator {

	private String middle;
	private String terminal;
	private String parentEmpty;
	private String parentJoin;

	public DirectoryParserPlainDecorator() {
		middle = "├";
		terminal = "└";
		parentEmpty = "　";
		parentJoin = "│";
	}

	@Override
	public final String getMiddleString() {
		return middle;
	}

	@Override
	public final boolean isMiddleString(final String string) {
		return middle.equals(string);
	}

	@Override
	public final String getTerminalString() {
		return terminal;
	}

	@Override
	public final boolean isTerminalString(final String string) {
		return terminal.equals(string);
	}

	@Override
	public final String getParentEmptyString() {
		return parentEmpty;
	}

	@Override
	public final boolean isParentEmptyString(final String string) {
		return parentEmpty.equals(string);
	}

	@Override
	public final String getParentJoinString() {
		return parentJoin;
	}

	@Override
	public final boolean isParentJoinString(final String string) {
		return parentJoin.equals(string);
	}

}
