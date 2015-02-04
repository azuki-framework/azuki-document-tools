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
package org.azkfw.document.tools.parser.db.model;

import java.util.ArrayList;
import java.util.List;

/**
 * このクラスは、インデックス情報を保持するモデルクラスです。
 * @since 1.0.0
 * @version 1.0.0 2015/02/04
 * @author kawakicchi
 */
public class IndexModel {

	/** インデックス名 */
	private String name;

	/** ユニークフラグ */
	private boolean uniqueFlag;

	/** フィールドリスト */
	private List<IndexFieldModel> fields;

	/**
	 * コンストラクタ
	 */
	public IndexModel() {
		name = null;
		uniqueFlag = false;
		fields = new ArrayList<IndexFieldModel>();
	}

	/**
	 * インデックス名を設定する。
	 * 
	 * @param name インデックス名
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * インデックス名を取得する。
	 * 
	 * @return インデックス名
	 */
	public String getName() {
		return name;
	}

	/**
	 * ユニーク有無を設定する。
	 * 
	 * @param flag 有無
	 */
	public void setUnique(final boolean flag) {
		this.uniqueFlag = flag;
	}

	/**
	 * ユニーク有無を判断する。
	 * 
	 * @return 判断
	 */
	public boolean isUnique() {
		return uniqueFlag;
	}

	public void addField(final IndexFieldModel field) {
		fields.add(field);
	}

	public List<IndexFieldModel> getFields() {
		return fields;
	}
}
