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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.azkfw.document.tools.parser.db.model.DatasourceModel;
import org.azkfw.document.tools.parser.db.model.FieldModel;
import org.azkfw.document.tools.parser.db.model.ForeignKeyFeildModel;
import org.azkfw.document.tools.parser.db.model.ForeignKeyModel;
import org.azkfw.document.tools.parser.db.model.IndexFieldModel;
import org.azkfw.document.tools.parser.db.model.IndexModel;
import org.azkfw.document.tools.parser.db.model.TableModel;

/**
 * @since 1.0.0
 * @version 1.0.0 2015/02/05
 * @author kawakicchi
 */
public class XLSXWriter {

	private class CellStyleManager {
		private XSSFWorkbook workbook;
		private Map<XSSFCellStyle, List<XSSFCellStyle>> cellStyles;

		public CellStyleManager(final XSSFWorkbook workbook) {
			this.workbook = workbook;
			this.cellStyles = new HashMap<XSSFCellStyle, List<XSSFCellStyle>>();
		}

		public void set(final XSSFCellStyle style) {
			if (!cellStyles.containsKey(style)) {
				List<XSSFCellStyle> lst = new ArrayList<XSSFCellStyle>();
				lst.add(style);
				cellStyles.put(style, lst);
			}
		}

		public XSSFCellStyle get(final XSSFCellStyle style, final short top, final short bottom, final short left, final short right) {
			XSSFCellStyle result = null;
			if (cellStyles.containsKey(style)) {
				List<XSSFCellStyle> styles = cellStyles.get(style);
				for (XSSFCellStyle s : styles) {
					if (s.getBorderTop() != top) {
						continue;
					}
					if (s.getBorderBottom() != bottom) {
						continue;
					}
					if (s.getBorderLeft() != left) {
						continue;
					}
					if (s.getBorderRight() != right) {
						continue;
					}
					result = s;
					break;
				}
				if (null == result) {
					result = workbook.createCellStyle();
					result.cloneStyleFrom(style);
					result.setBorderTop(top);
					result.setBorderBottom(bottom);
					result.setBorderLeft(left);
					result.setBorderRight(right);
				}
			}
			return result;
		}
	}

	private CellStyleManager styleManager;

	private XSSFWorkbook workbook;

	private XSSFFont fontTitle;
	private XSSFFont fontLabel;
	private XSSFFont fontValue;
	private XSSFFont fontLink;
	private XSSFCellStyle styleTitle;

	private XSSFCellStyle defStyleHeadValue;
	private XSSFCellStyle defStyleLabel;
	private XSSFCellStyle defStyleListValue;
	private XSSFCellStyle defStyleListValuePK;
	private XSSFCellStyle defStyleListValueCenter;
	private XSSFCellStyle defStyleListValueLink;

	private short BD_RECT = CellStyle.BORDER_MEDIUM;

	public XSSFWorkbook write(final DatasourceModel datasource) {
		workbook = new XSSFWorkbook();

		styleManager = new CellStyleManager(workbook);

		// 基本スタイル生成
		fontTitle = workbook.createFont();
		fontTitle.setBold(true);
		fontTitle.setItalic(true);
		fontTitle.setFontHeightInPoints((short) 12);
		fontLabel = workbook.createFont();
		fontLabel.setBold(true);
		fontLabel.setFontHeightInPoints((short) 9);
		fontValue = workbook.createFont();
		fontValue.setBold(false);
		fontValue.setFontHeightInPoints((short) 9);
		fontLink = workbook.createFont();
		fontLink.setUnderline(Font.U_SINGLE);
		fontLink.setColor(IndexedColors.BLUE.getIndex());
		fontLink.setFontHeightInPoints((short) 9);

		styleTitle = workbook.createCellStyle();
		styleTitle.setFont(fontTitle);
		/////////////////////////////////////////////////////////////////////
		XSSFFont fontPK = workbook.createFont();
		fontPK.setBold(true);
		fontPK.setItalic(true);
		fontPK.setFontHeightInPoints((short) 9);

		// テーブル情報用Valueスタイル
		defStyleHeadValue = workbook.createCellStyle();
		defStyleHeadValue.setFillPattern(CellStyle.SOLID_FOREGROUND);
		defStyleHeadValue.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		defStyleHeadValue.setFont(fontValue);
		defStyleHeadValue.setBorderTop(CellStyle.BORDER_THIN);
		defStyleHeadValue.setBorderBottom(CellStyle.BORDER_THIN);
		defStyleHeadValue.setBorderLeft(CellStyle.BORDER_THIN);
		defStyleHeadValue.setBorderRight(CellStyle.BORDER_THIN);
		styleManager.set(defStyleHeadValue);
		defStyleLabel = workbook.createCellStyle();
		defStyleLabel.setFillPattern(CellStyle.SOLID_FOREGROUND);
		defStyleLabel.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		defStyleLabel.setFont(fontLabel);
		defStyleLabel.setBorderTop(CellStyle.BORDER_THIN);
		defStyleLabel.setBorderBottom(CellStyle.BORDER_THIN);
		defStyleLabel.setBorderLeft(CellStyle.BORDER_THIN);
		defStyleLabel.setBorderRight(CellStyle.BORDER_THIN);
		styleManager.set(defStyleLabel);
		// デフォルトスタイル定義
		defStyleListValue = workbook.createCellStyle();
		defStyleListValue.setFillPattern(CellStyle.SOLID_FOREGROUND);
		defStyleListValue.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		defStyleListValue.setFont(fontValue);
		defStyleListValue.setBorderTop(CellStyle.BORDER_DOTTED);
		defStyleListValue.setBorderBottom(CellStyle.BORDER_DOTTED);
		defStyleListValue.setBorderLeft(CellStyle.BORDER_THIN);
		defStyleListValue.setBorderRight(CellStyle.BORDER_THIN);
		styleManager.set(defStyleListValue);
		// Valueスタイル(pk)
		defStyleListValuePK = workbook.createCellStyle();
		defStyleListValuePK.setFillPattern(CellStyle.SOLID_FOREGROUND);
		defStyleListValuePK.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		defStyleListValuePK.setFont(fontPK);
		defStyleListValuePK.setBorderTop(CellStyle.BORDER_DOTTED);
		defStyleListValuePK.setBorderBottom(CellStyle.BORDER_DOTTED);
		defStyleListValuePK.setBorderLeft(CellStyle.BORDER_THIN);
		defStyleListValuePK.setBorderRight(CellStyle.BORDER_THIN);
		styleManager.set(defStyleListValuePK);
		// Valueスタイル(center)
		defStyleListValueCenter = workbook.createCellStyle();
		defStyleListValueCenter.setFillPattern(CellStyle.SOLID_FOREGROUND);
		defStyleListValueCenter.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		defStyleListValueCenter.setFont(fontValue);
		defStyleListValueCenter.setAlignment(CellStyle.ALIGN_CENTER);
		defStyleListValueCenter.setBorderTop(CellStyle.BORDER_DOTTED);
		defStyleListValueCenter.setBorderBottom(CellStyle.BORDER_DOTTED);
		defStyleListValueCenter.setBorderLeft(CellStyle.BORDER_THIN);
		defStyleListValueCenter.setBorderRight(CellStyle.BORDER_THIN);
		styleManager.set(defStyleListValueCenter);
		// Valueスタイル(link)
		defStyleListValueLink = workbook.createCellStyle();
		defStyleListValueLink.setFillPattern(CellStyle.SOLID_FOREGROUND);
		defStyleListValueLink.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		defStyleListValueLink.setFont(fontLink);
		defStyleListValueLink.setBorderTop(CellStyle.BORDER_DOTTED);
		defStyleListValueLink.setBorderBottom(CellStyle.BORDER_DOTTED);
		defStyleListValueLink.setBorderLeft(CellStyle.BORDER_THIN);
		defStyleListValueLink.setBorderRight(CellStyle.BORDER_THIN);
		styleManager.set(defStyleListValueLink);

		// シート作成
		workbook.createSheet(getTableListSheetName());
		for (TableModel table : datasource.getTables()) {
			workbook.createSheet(getTableSheetName(table.getName()));
		}

		// テーブル一覧シート
		createTableListSheet(datasource, workbook.getSheet(getTableListSheetName()));
		// テーブルシート
		for (TableModel table : datasource.getTables()) {
			XSSFSheet sheet = workbook.getSheet(getTableSheetName(table.getName()));
			createTableSheet(datasource, table, sheet);
		}

		return workbook;
	}

	private XSSFSheet createTableListSheet(final DatasourceModel datasource, final XSSFSheet sheet) {

		// 列幅調整
		sheet.setColumnWidth(0, 640 * 1);
		sheet.setColumnWidth(1, 640 * 2); // No
		sheet.setColumnWidth(2, 640 * 10); // 論理テーブル名
		sheet.setColumnWidth(3, 640 * 10); // 物理テーブル名
		sheet.setColumnWidth(4, 640 * 15); // コメント
		sheet.setColumnWidth(5, 640 * 1);

		/////////////////////////////////////////////////////////////////////
		XSSFCellStyle styleLabel = workbook.createCellStyle();
		styleLabel.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleLabel.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		styleLabel.setFont(fontLabel);
		styleLabel.setBorderTop(CellStyle.BORDER_THIN);
		styleLabel.setBorderBottom(CellStyle.BORDER_THIN);
		styleLabel.setBorderLeft(CellStyle.BORDER_THIN);
		styleLabel.setBorderRight(CellStyle.BORDER_THIN);

		XSSFCellStyle styleValue1 = workbook.createCellStyle();
		styleValue1.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleValue1.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		styleValue1.setFont(fontValue);
		styleValue1.setBorderTop(CellStyle.BORDER_DOTTED);
		styleValue1.setBorderBottom(CellStyle.BORDER_DOTTED);
		styleValue1.setBorderLeft(CellStyle.BORDER_THIN);
		styleValue1.setBorderRight(CellStyle.BORDER_THIN);

		XSSFCellStyle styleValue2 = workbook.createCellStyle();
		styleValue2.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleValue2.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		styleValue2.setFont(fontLink);
		styleValue2.setBorderTop(CellStyle.BORDER_DOTTED);
		styleValue2.setBorderBottom(CellStyle.BORDER_DOTTED);
		styleValue2.setBorderLeft(CellStyle.BORDER_THIN);
		styleValue2.setBorderRight(CellStyle.BORDER_THIN);

		/////////////////////////////////////////////////////////////////////
		XSSFRow row = null;
		XSSFCell cell = null;
		int rowIndex = 0;

		////////////////////////////////////////////////////////////////////////////
		// テーブル情報
		////////////////////////////////////////////////////////////////////////////

		row = sheet.createRow(rowIndex); ///////////////////////////////////////////
		cell = createCell(1, "テーブル一覧", styleTitle, row);

		List<TableModel> tables = datasource.getTables();

		short top = BD_RECT;
		short bottom = CellStyle.BORDER_THIN;
		if (0 == tables.size()) {
			bottom = BD_RECT;
		}

		rowIndex++;
		row = sheet.createRow(rowIndex); ///////////////////////////////////////////
		cell = createCell(1, "No", styleManager.get(defStyleLabel, top, bottom, BD_RECT, CellStyle.BORDER_THIN), row);
		cell = createCell(2, "論理テーブル名", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
		cell = createCell(3, "物理テーブル名", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
		cell = createCell(4, "備考", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, BD_RECT), row);

		rowIndex++;
		for (int i = 0; i < tables.size(); i++) {
			int bufRowIndex = rowIndex + i;
			TableModel table = tables.get(i);

			Hyperlink link = createTableLink(table.getName());

			top = CellStyle.BORDER_DOTTED;
			bottom = CellStyle.BORDER_DOTTED;
			if (i == 0) {
				top = CellStyle.BORDER_THIN;
			}
			if (i + 1 == tables.size()) {
				bottom = BD_RECT;
			}

			row = sheet.createRow(bufRowIndex); ///////////////////////////////////////////
			cell = createCell(1, String.format("%d", i + 1), styleManager.get(defStyleListValue, top, bottom, BD_RECT, CellStyle.BORDER_THIN), row);
			cell = createCell(2, table.getLabel(), styleManager.get(defStyleListValue, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN),
					row);
			cell = createCell(3, table.getName(), styleManager.get(defStyleListValueLink, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN),
					link, row);
			cell = createCell(4, table.getComment(), styleManager.get(defStyleListValue, top, bottom, CellStyle.BORDER_THIN, BD_RECT), row);
		}
		rowIndex += tables.size();

		return sheet;
	}

	private XSSFSheet createTableSheet(final DatasourceModel datasource, final TableModel table, final XSSFSheet sheet) {

		// 列幅調整
		for (int i = 0; i < 39; i++) {
			sheet.setColumnWidth(i, 640);
		}

		/////////////////////////////////////////////////////////////////////
		XSSFRow row = null;
		XSSFCell cell = null;
		int rowIndex = 0;

		////////////////////////////////////////////////////////////////////////////
		// テーブル情報
		////////////////////////////////////////////////////////////////////////////
		{
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, "テーブル情報", styleTitle, row);

			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, 7, "システム名", styleManager.get(defStyleLabel, BD_RECT, CellStyle.BORDER_THIN, BD_RECT, CellStyle.BORDER_THIN), row);
			cell = createCell(8, 12, "",
					styleManager.get(defStyleHeadValue, BD_RECT, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(20, 3, "作成者",
					styleManager.get(defStyleLabel, BD_RECT, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(23, 15, "", styleManager.get(defStyleHeadValue, BD_RECT, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, BD_RECT), row);
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 7));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 8, 19));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 20, 22));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 23, 37));

			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, 7, "サブシステム名",
					styleManager.get(defStyleLabel, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, BD_RECT, CellStyle.BORDER_THIN), row);
			cell = createCell(8, 12, "",
					styleManager.get(defStyleHeadValue, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN),
					row);
			cell = createCell(20, 3, "作成日",
					styleManager.get(defStyleLabel, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(23, 15, "",
					styleManager.get(defStyleHeadValue, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, BD_RECT), row);
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 7));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 8, 19));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 20, 22));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 23, 37));

			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, 7, "スキーマ名",
					styleManager.get(defStyleLabel, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, BD_RECT, CellStyle.BORDER_THIN), row);
			cell = createCell(8, 12, "",
					styleManager.get(defStyleHeadValue, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN),
					row);
			cell = createCell(20, 3, "更新日",
					styleManager.get(defStyleLabel, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(23, 15, "",
					styleManager.get(defStyleHeadValue, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, BD_RECT), row);
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 7));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 8, 19));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 20, 22));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 23, 37));

			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, 7, "論理テーブル名",
					styleManager.get(defStyleLabel, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, BD_RECT, CellStyle.BORDER_THIN), row);
			cell = createCell(8, 12, table.getLabel(),
					styleManager.get(defStyleHeadValue, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN),
					row);
			cell = createCell(20, 3, "",
					styleManager.get(defStyleLabel, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(23, 15, "",
					styleManager.get(defStyleHeadValue, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, BD_RECT), row);
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 7));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 8, 19));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 20, 22));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 23, 37));

			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, 7, "物理テーブル名",
					styleManager.get(defStyleLabel, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, BD_RECT, CellStyle.BORDER_THIN), row);
			cell = createCell(8, 12, table.getName(),
					styleManager.get(defStyleHeadValue, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN),
					row);
			cell = createCell(20, 3, "",
					styleManager.get(defStyleLabel, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(23, 15, "",
					styleManager.get(defStyleHeadValue, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, BD_RECT), row);
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 7));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 8, 19));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 20, 22));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 23, 37));

			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, 37, "コメント", styleManager.get(defStyleHeadValue, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, BD_RECT, BD_RECT), row);
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 37));

			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, 37, "", styleManager.get(defStyleHeadValue, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, BD_RECT, BD_RECT), row);
			row = sheet.createRow(rowIndex + 1); ///////////////////////////////////////////
			cell = createCell(1, 37, "", styleManager.get(defStyleHeadValue, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN, BD_RECT, BD_RECT), row);
			row = sheet.createRow(rowIndex + 2); ///////////////////////////////////////////
			cell = createCell(1, 37, "", styleManager.get(defStyleHeadValue, CellStyle.BORDER_THIN, BD_RECT, BD_RECT, BD_RECT), row);
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 2, 1, 37));
			rowIndex += 3;
		}
		////////////////////////////////////////////////////////////////////////////
		// カラム情報
		////////////////////////////////////////////////////////////////////////////
		{
			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, "カラム情報", styleTitle, row);

			List<FieldModel> fields = table.getFields();

			short top = BD_RECT;
			short bottom = CellStyle.BORDER_THIN;
			if (0 == fields.size()) {
				bottom = BD_RECT;
			}

			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, 2, "No", styleManager.get(defStyleLabel, top, bottom, BD_RECT, CellStyle.BORDER_THIN), row);
			cell = createCell(3, 6, "論理名", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(9, 6, "物理名", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(15, 6, "データタイプ", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(21, 3, "NotNull", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(24, 3, "デフォルト", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(27, 11, "コメント", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, BD_RECT), row);
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 2));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 3, 8));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 9, 14));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 15, 20));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 21, 23));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 24, 26));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 27, 37));

			IndexModel primaryIndex = table.getPrimaryIndex();

			rowIndex++;
			for (int i = 0; i < fields.size(); i++) {
				int bufRowIndex = rowIndex + i;
				FieldModel field = fields.get(i);

				top = CellStyle.BORDER_DOTTED;
				bottom = CellStyle.BORDER_DOTTED;
				if (i == 0) {
					top = CellStyle.BORDER_THIN;
				}
				if (i + 1 == fields.size()) {
					bottom = BD_RECT;
				}

				XSSFCellStyle defStylePK = defStyleListValue;
				if (null != primaryIndex.getField(field.getName())) {
					defStylePK = defStyleListValuePK;
				}

				row = sheet.createRow(bufRowIndex); ///////////////////////////////////////////
				cell = createCell(1, 2, String.format("%d", i + 1), styleManager.get(defStyleListValue, top, bottom, BD_RECT, CellStyle.BORDER_THIN),
						row);
				cell = createCell(3, 6, field.getLabel(), styleManager.get(defStylePK, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN),
						row);
				cell = createCell(9, 6, field.getName(), styleManager.get(defStylePK, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
				cell = createCell(15, 6, field.getType().getLabel(),
						styleManager.get(defStylePK, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
				cell = createCell(21, 3, toTrue(field.isNotNull()),
						styleManager.get(defStyleListValueCenter, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
				cell = createCell(24, 3, toDefault(field),
						styleManager.get(defStyleListValue, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
				cell = createCell(27, 11, field.getComment(), styleManager.get(defStyleListValue, top, bottom, CellStyle.BORDER_THIN, BD_RECT), row);
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 1, 2));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 3, 8));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 9, 14));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 15, 20));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 21, 23));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 24, 26));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 27, 37));
			}
			rowIndex += fields.size();
		}
		////////////////////////////////////////////////////////////////////////////
		// インデックス情報
		////////////////////////////////////////////////////////////////////////////
		{
			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, "インデックス情報", styleTitle, row);

			List<IndexModel> indexs = table.getIndexs();

			short top = BD_RECT;
			short bottom = CellStyle.BORDER_THIN;
			if (0 == indexs.size()) {
				bottom = BD_RECT;
			}

			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, 2, "No", styleManager.get(defStyleLabel, top, bottom, BD_RECT, CellStyle.BORDER_THIN), row);
			cell = createCell(3, 6, "インデックス名", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(9, 12, "カラムリスト", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(21, 3, "主キー", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(24, 3, "ユニーク", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(27, 11, "コメント", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, BD_RECT), row);
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 2));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 3, 8));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 9, 20));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 21, 23));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 24, 26));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 27, 37));

			rowIndex++;
			for (int i = 0; i < indexs.size(); i++) {
				int bufRowIndex = rowIndex + i;
				IndexModel index = indexs.get(i);

				top = CellStyle.BORDER_DOTTED;
				bottom = CellStyle.BORDER_DOTTED;
				if (i == 0) {
					top = CellStyle.BORDER_THIN;
				}
				if (i + 1 == indexs.size()) {
					bottom = BD_RECT;
				}

				row = sheet.createRow(bufRowIndex); ///////////////////////////////////////////
				cell = createCell(1, 2, String.format("%d", i + 1), styleManager.get(defStyleListValue, top, bottom, BD_RECT, CellStyle.BORDER_THIN),
						row);
				cell = createCell(3, 6, index.getName(),
						styleManager.get(defStyleListValue, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
				cell = createCell(9, 12, toColumnList(index),
						styleManager.get(defStyleListValue, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
				cell = createCell(21, 3, toTrue(index.isPrimaryKey()),
						styleManager.get(defStyleListValueCenter, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
				cell = createCell(24, 3, toTrue(index.isUnique()),
						styleManager.get(defStyleListValueCenter, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
				cell = createCell(27, 11, index.getComment(), styleManager.get(defStyleListValue, top, bottom, CellStyle.BORDER_THIN, BD_RECT), row);
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 1, 2));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 3, 8));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 9, 20));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 21, 23));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 24, 26));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 27, 37));
			}
			rowIndex += indexs.size();
		}
		////////////////////////////////////////////////////////////////////////////
		// 外部キー情報
		////////////////////////////////////////////////////////////////////////////
		{
			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, "外部キー情報", styleTitle, row);

			List<ForeignKeyModel> foreignKeys = table.getForeignKeys();

			short top = BD_RECT;
			short bottom = CellStyle.BORDER_THIN;
			if (0 == foreignKeys.size()) {
				bottom = BD_RECT;
			}

			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, 2, "No", styleManager.get(defStyleLabel, top, bottom, BD_RECT, CellStyle.BORDER_THIN), row);
			cell = createCell(3, 6, "外部キー名", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(9, 12, "カラムリスト", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(21, 6, "参照先テーブル", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(27, 11, "参照先カラムリスト", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, BD_RECT), row);
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 2));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 3, 8));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 9, 20));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 21, 26));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 27, 37));

			rowIndex++;
			for (int i = 0; i < foreignKeys.size(); i++) {
				int bufRowIndex = rowIndex + i;
				ForeignKeyModel foreignKey = foreignKeys.get(i);

				Hyperlink link = createTableLink(foreignKey.getReferenceTableName());

				top = CellStyle.BORDER_DOTTED;
				bottom = CellStyle.BORDER_DOTTED;
				if (i == 0) {
					top = CellStyle.BORDER_THIN;
				}
				if (i + 1 == foreignKeys.size()) {
					bottom = BD_RECT;
				}

				row = sheet.createRow(bufRowIndex); ///////////////////////////////////////////
				cell = createCell(1, 2, String.format("%d", i + 1), styleManager.get(defStyleListValue, top, bottom, BD_RECT, CellStyle.BORDER_THIN),
						row);
				cell = createCell(3, 6, foreignKey.getName(),
						styleManager.get(defStyleListValue, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
				cell = createCell(9, 12, toForeignKeyColumnList(foreignKey),
						styleManager.get(defStyleListValue, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
				cell = createCell(21, 6, foreignKey.getReferenceTableName(),
						styleManager.get(defStyleListValueLink, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), link, row);
				cell = createCell(27, 11, toForeignKeyRefColumnList(foreignKey),
						styleManager.get(defStyleListValue, top, bottom, CellStyle.BORDER_THIN, BD_RECT), row);
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 1, 2));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 3, 8));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 9, 20));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 21, 26));
				sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 27, 37));
			}
			rowIndex += foreignKeys.size();
		}
		////////////////////////////////////////////////////////////////////////////
		// 外部キー(Ref)情報
		////////////////////////////////////////////////////////////////////////////
		{
			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, "外部キー情報(PK側)", styleTitle, row);

			int size = 0;
			for (TableModel targetTable : datasource.getTables()) {
				if (targetTable.equals(table)) {
					continue;
				}
				List<ForeignKeyModel> foreignKeys = targetTable.getForeignKeys();
				for (ForeignKeyModel foreignKey : foreignKeys) {
					if (!foreignKey.getReferenceTableName().equals(table.getName())) {
						continue;
					}
					size++;
				}
			}

			short top = BD_RECT;
			short bottom = CellStyle.BORDER_THIN;
			if (0 == size) {
				bottom = BD_RECT;
			}

			rowIndex++;
			row = sheet.createRow(rowIndex); ///////////////////////////////////////////
			cell = createCell(1, 2, "No", styleManager.get(defStyleLabel, top, bottom, BD_RECT, CellStyle.BORDER_THIN), row);
			cell = createCell(3, 6, "外部キー名", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(9, 12, "カラムリスト", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(21, 6, "参照元テーブル", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
			cell = createCell(27, 11, "参照元カラムリスト", styleManager.get(defStyleLabel, top, bottom, CellStyle.BORDER_THIN, BD_RECT), row);
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 1, 2));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 3, 8));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 9, 20));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 21, 26));
			sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 27, 37));

			int cnt = 0;
			rowIndex++;
			for (TableModel targetTable : datasource.getTables()) {
				if (targetTable.equals(table)) {
					continue;
				}

				List<ForeignKeyModel> foreignKeys = targetTable.getForeignKeys();
				for (ForeignKeyModel foreignKey : foreignKeys) {
					if (!foreignKey.getReferenceTableName().equals(table.getName())) {
						continue;
					}

					int bufRowIndex = rowIndex + cnt;
					Hyperlink link = createTableLink(targetTable.getName());

					top = CellStyle.BORDER_DOTTED;
					bottom = CellStyle.BORDER_DOTTED;
					if (cnt == 0) {
						top = CellStyle.BORDER_THIN;
					}
					if (cnt + 1 == size) {
						bottom = BD_RECT;
					}

					row = sheet.createRow(bufRowIndex); ///////////////////////////////////////////
					cell = createCell(1, 2, String.format("%d", cnt + 1),
							styleManager.get(defStyleListValue, top, bottom, BD_RECT, CellStyle.BORDER_THIN), row);
					cell = createCell(3, 6, foreignKey.getName(),
							styleManager.get(defStyleListValue, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
					cell = createCell(9, 12, toForeignKeyRefColumnList(foreignKey),
							styleManager.get(defStyleListValue, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), row);
					cell = createCell(21, 6, targetTable.getName(),
							styleManager.get(defStyleListValueLink, top, bottom, CellStyle.BORDER_THIN, CellStyle.BORDER_THIN), link, row);
					cell = createCell(27, 11, toForeignKeyColumnList(foreignKey),
							styleManager.get(defStyleListValue, top, bottom, CellStyle.BORDER_THIN, BD_RECT), row);
					sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 1, 2));
					sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 3, 8));
					sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 9, 20));
					sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 21, 26));
					sheet.addMergedRegion(new CellRangeAddress(bufRowIndex, bufRowIndex, 27, 37));

					cnt++;
				}
			}
		}
		return sheet;
	}

	private Hyperlink createTableLink(final String name) {
		CreationHelper ch = workbook.getCreationHelper();
		Hyperlink link = ch.createHyperlink(Hyperlink.LINK_DOCUMENT);
		link.setAddress(String.format("%s!A1", name));
		return link;
	}

	private String getTableListSheetName() {
		return "テーブル一覧";
	}

	private String getTableSheetName(final String name) {
		return String.format("%s", name);
	}

	private XSSFCell createCell(final int col, final String text, final XSSFCellStyle style, final XSSFRow row) {
		return createCell(col, 1, text, style, null, row);
	}

	private XSSFCell createCell(final int col, final int size, final String text, final XSSFCellStyle style, final XSSFRow row) {
		return createCell(col, size, text, style, null, row);
	}

	private XSSFCell createCell(final int col, final String text, final XSSFCellStyle style, final Hyperlink link, final XSSFRow row) {
		return createCell(col, 1, text, style, link, row);
	}

	private XSSFCell createCell(final int col, final int size, final String text, final XSSFCellStyle style, final Hyperlink link, final XSSFRow row) {
		XSSFCell cell = null;
		for (int i = size - 1; i >= 0; i--) {
			cell = row.createCell(col + i, Cell.CELL_TYPE_STRING);
			if (null != style) {
				cell.setCellStyle(style);
			}
			if (null != link) {
				cell.setHyperlink(link);
			}
		}
		cell.setCellValue(s(text));
		return cell;
	}

	private static final String toColumnList(final IndexModel index) {
		List<IndexFieldModel> fields = index.getFields();
		StringBuilder s = new StringBuilder();
		for (IndexFieldModel field : fields) {
			if (0 < s.length()) {
				s.append(", ");
			}
			s.append(field.getName());
		}
		return s.toString();
	}

	private static final String toForeignKeyColumnList(final ForeignKeyModel index) {
		List<ForeignKeyFeildModel> fields = index.getFields();
		StringBuilder s = new StringBuilder();
		for (ForeignKeyFeildModel field : fields) {
			if (0 < s.length()) {
				s.append(", ");
			}
			s.append(field.getName());
		}
		return s.toString();
	}

	private static final String toForeignKeyRefColumnList(final ForeignKeyModel index) {
		List<ForeignKeyFeildModel> fields = index.getReferenceFields();
		StringBuilder s = new StringBuilder();
		for (ForeignKeyFeildModel field : fields) {
			if (0 < s.length()) {
				s.append(", ");
			}
			s.append(field.getName());
		}
		return s.toString();
	}

	private static final String toDefault(final FieldModel field) {
		if (field.isDefaultFlag()) {
			if (null == field.getDefaultValue()) {
				return "null";
			} else {
				return field.getDefaultValue().toString();
			}
		}
		return null;
	}

	private static final String toTrue(final boolean flag) {
		return (flag) ? "YES" : "";
	}

	private static final String toYesNo(final boolean flag) {
		return (flag) ? "Yes" : "No";
	}

	private static final String s(final String string) {
		if (null != string) {
			return string;
		} else {
			return "";
		}
	}

}
