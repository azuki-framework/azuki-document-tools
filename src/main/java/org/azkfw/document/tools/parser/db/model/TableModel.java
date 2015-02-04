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
 * このクラスは、テーブル情報を保持するモデルクラスです。
 * 
 * @since 1.0.0
 * @version 1.0.0 2015/02/04
 * @author kawakicchi
 */
public final class TableModel {

	/** ラベル */
	private String label;

	/** テーブル名 */
	private String name;

	/** コメント */
	private String comment;

	/** フィールドリスト */
	private List<FieldModel> fields;

	/** インデックスリスト */
	private List<IndexModel> indexs;

	/** 外部キーリスト */
	private List<ForeignKeyModel> foreignKeys;

	/**
	 * コンストラクタ
	 */
	public TableModel() {
		label = null;
		name = null;
		comment = null;
		fields = new ArrayList<FieldModel>();
		indexs = new ArrayList<IndexModel>();
		foreignKeys = new ArrayList<ForeignKeyModel>();
	}

	/**
	 * ラベルを設定する。
	 * 
	 * @param label ラベル
	 */
	public void setLabel(final String label) {
		this.label = label;
	}

	/**
	 * ラベルを取得する。
	 * 
	 * @return ラベル
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * テーブル名を設定する。
	 * 
	 * @param name テーブル名
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * テーブル名を取得する。
	 * 
	 * @return テーブル名
	 */
	public String getName() {
		return name;
	}

	/**
	 * コメントを設定する。
	 * 
	 * @param comment コメント
	 */
	public void setComment(final String comment) {
		this.comment = comment;
	}

	/**
	 * コメントを取得する。
	 * 
	 * @return コメント
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * フィールドを追加する。
	 * 
	 * @param field フィールド
	 */
	public void addField(final FieldModel field) {
		fields.add(field);
	}

	/**
	 * フィールドリストを取得する。
	 * 
	 * @return フィールドリスト
	 */
	public List<FieldModel> getFields() {
		return fields;
	}

	public FieldModel getField(final String name) {
		for (FieldModel field : fields) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * インデックスを追加する。
	 * 
	 * @param index インデックス
	 */
	public void addIndex(final IndexModel index) {
		indexs.add(index);
	}

	/**
	 * インデックスリストを取得する。
	 * 
	 * @return インデックスリスト
	 */
	public List<IndexModel> getIndexs() {
		return indexs;
	}

	public IndexModel getIndex(final String name) {
		for (IndexModel index : indexs) {
			if (index.getName().equals(name)) {
				return index;
			}
		}
		return null;
	}

	public void addForeignKey(final ForeignKeyModel foreignKey) {
		this.addForeignKey(foreignKey);
	}

	public List<ForeignKeyModel> getForeignKeys() {
		return foreignKeys;
	}

	public ForeignKeyModel getForeignKey(final String name) {
		for (ForeignKeyModel foreignKey : foreignKeys) {
			if (foreignKey.getName().equals(name)) {
				return foreignKey;
			}
		}
		return null;
	}
}
