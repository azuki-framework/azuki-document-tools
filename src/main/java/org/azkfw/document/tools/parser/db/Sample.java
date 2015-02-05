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
package org.azkfw.document.tools.parser.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.azkfw.document.tools.parser.db.model.DatasourceModel;
import org.azkfw.document.tools.parser.db.model.FieldModel;
import org.azkfw.document.tools.parser.db.model.FieldTypeModel;
import org.azkfw.document.tools.parser.db.model.ForeignKeyFeildModel;
import org.azkfw.document.tools.parser.db.model.ForeignKeyModel;
import org.azkfw.document.tools.parser.db.model.IndexFieldModel;
import org.azkfw.document.tools.parser.db.model.IndexModel;
import org.azkfw.document.tools.parser.db.model.TableModel;

/**
 * @since 1.0.0
 * @version 1.0.0 2015/02/04
 * @author kawakicchi
 */
public class Sample {

	public static void main(final String[] args) {

		Sample sample = new Sample();
		sample.start();
	}

	public Sample() {

	}

	public void start() {
		DatasourceModel datasource = new DatasourceModel();

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "", "");

			// テーブル一覧取得
			ps = connection.prepareStatement("SHOW TABLES");
			rs = ps.executeQuery();
			while (rs.next()) {
				String name = rs.getString(1);

				TableModel table = new TableModel();
				table.setName(name);

				putTable(table, "bpsrv", connection);

				// select TABLE_NAME,TABLE_COMMENT from information_schema.tables where TABLE_SCHEMA = '';

				datasource.addTable(table);
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (null != rs) {
				try {
					rs.close();
				} catch (SQLException ex) {

				}
			}
			if (null != ps) {
				try {
					ps.close();
				} catch (SQLException ex) {

				}
			}
			if (null != connection) {
				try {
					connection.close();
				} catch (SQLException ex) {

				}
			}
		}

		outputDatasource(datasource, new File("C:\\temp\\sample.xlsx"));
	}

	private void putTable(final TableModel table, final String schema, final Connection connection) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// フィールド情報取得
			ps = connection.prepareStatement("SHOW COLUMNS FROM " + table.getName());
			rs = ps.executeQuery();
			while (rs.next()) {
				String name = rs.getString("Field");
				String type = rs.getString("Type");
				String nul = rs.getString("Null");
				Object def = rs.getObject("Default");

				FieldTypeModel fieldType = new FieldTypeModel();
				fieldType.setLabel(type);

				FieldModel field = new FieldModel();
				field.setName(name);
				field.setType(fieldType);
				field.setNotNull("NO".equals(nul));
				if (null == def) {
					field.setDefaultFlag(false);
				} else {
					field.setDefaultFlag(true);
					field.setDefaultValue(def);
				}

				// select column_name, column_comment from information_schema.columns where TABLE_SCHEMA = '';

				table.addField(field);
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;

			// インデックス情報取得
			ps = connection.prepareStatement("SHOW INDEXES FROM " + table.getName());
			rs = ps.executeQuery();
			while (rs.next()) {
				String name = rs.getString("key_name");
				String fieldName = rs.getString("column_name");
				// TODO: 順位を考慮
				int seq = rs.getInt("seq_in_index");
				int nonUnique = rs.getInt("non_unique");

				IndexModel index = table.getIndex(name);
				if (null == index) {
					index = new IndexModel();
					index.setName(name);
					index.setPrimaryKey("PRIMARY".equals(name));
					index.setUnique(0 == nonUnique);
					table.addIndex(index);
				}

				IndexFieldModel field = new IndexFieldModel();
				field.setName(fieldName);

				index.addField(field);
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;

			// 外部キー
			ps = connection.prepareStatement(getSQL1());
			ps.setString(1, schema);
			ps.setString(2, table.getName());
			rs = ps.executeQuery();
			while (rs.next()) {
				String name = rs.getString("key_name");
				String fieldName = rs.getString("column_name");
				String refTableName = rs.getString("referenced_table_name");
				String refFieldName = rs.getString("referenced_column_name");

				ForeignKeyModel foreignKey = table.getForeignKey(name);
				if (null == foreignKey) {
					foreignKey = new ForeignKeyModel();
					foreignKey.setName(name);
					foreignKey.setReferenceTableName(refTableName);
					table.addForeignKey(foreignKey);
				}

				ForeignKeyFeildModel field = new ForeignKeyFeildModel();
				field.setName(fieldName);

				ForeignKeyFeildModel referenceField = new ForeignKeyFeildModel();
				referenceField.setName(refFieldName);

				foreignKey.addField(field);
				foreignKey.addReferenceField(referenceField);
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;

		} finally {
			if (null != rs) {
				try {
					rs.close();
				} catch (SQLException ex) {

				}
			}
			if (null != ps) {
				try {
					ps.close();
				} catch (SQLException ex) {

				}
			}
		}
	}

	private String getSQL1() {
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT");
		sql.append("    CONSTRAINT_NAME         AS key_name");
		sql.append("  , COLUMN_NAME             AS column_name");
		sql.append("  , REFERENCED_TABLE_NAME   AS referenced_table_name");
		sql.append("  , REFERENCED_COLUMN_NAME  AS referenced_column_name ");
		sql.append("FROM");
		sql.append("  information_schema.key_column_usage ");
		sql.append("WHERE");
		sql.append("  constraint_schema=?");
		sql.append("  AND constraint_schema=REFERENCED_TABLE_SCHEMA"); // 同一スキーマ縛り
		sql.append("  AND table_name=?");
		sql.append("  AND REFERENCED_TABLE_SCHEMA IS NOT NULL");
		sql.append(";");

		return sql.toString();
	}

	@SuppressWarnings("unused")
	private String getSQL2() {
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT");
		sql.append("    CONSTRAINT_NAME         AS key_name");
		sql.append("  , REFERENCED_COLUMN_NAME  AS column_name");
		sql.append("  , TABLE_NAME              AS referenced_table_name");
		sql.append("  , COLUMN_NAME             AS referenced_column_name ");
		sql.append("FROM");
		sql.append("  information_schema.key_column_usage ");
		sql.append("WHERE");
		sql.append("  constraint_schema=?");
		sql.append("  AND constraint_schema=REFERENCED_TABLE_SCHEMA"); // 同一スキーマ縛り
		sql.append("  AND referenced_table_name=?");
		sql.append("  AND REFERENCED_TABLE_SCHEMA IS NOT NULL");
		sql.append(";");

		return sql.toString();
	}

	public void outputDatasource(final DatasourceModel datasource, final File destFile) {
		try {
			XLSXWriter writer = new XLSXWriter();

			XSSFWorkbook wb = writer.write(datasource);

			FileOutputStream out = new FileOutputStream(destFile);
			wb.write(out);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
