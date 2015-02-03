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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.azkfw.document.tools.parser.dp.BasicDirectoryParser;
import org.azkfw.document.tools.parser.dp.DirectoryParser;
import org.azkfw.document.tools.parser.dp.DirectoryParserDecorator;
import org.azkfw.document.tools.parser.dp.DirectoryParserEvent;
import org.azkfw.document.tools.parser.dp.DirectoryParserFileInfo;
import org.azkfw.document.tools.parser.dp.DirectoryParserListener;
import org.azkfw.document.tools.parser.dp.DirectoryParserPlainDecorator;

/**
 * このクラスは、ディレクトリ構成をエクセル出力するクラスです。
 * 
 * @since 1.0.0
 * @version 1.0.0 2015/01/28
 * @author kawakicchi
 */
public class DirectoryTreeDocument {

	/**
	 * メイン関数
	 * 
	 * <p>
	 * 引数は次の通りです。
	 * <ul>
	 * <li>構成を出力するディレクトリ</li>
	 * <li>出力エクセルファイルパス</li>
	 * </ul>
	 * </p>
	 * 
	 * @param args 引数
	 */
	public static void main(final String[] args) {
		File directory = new File(args[0]);
		File file = new File(args[1]);

		DirectoryTreeDocument doc = new DirectoryTreeDocument();
		doc.create(directory, file);
	}

	private DirectoryParserDecorator decorator;
	private XSSFSheet sheet;
	private int countFile;
	private int maxCol;

	private int offsetCol;
	private int offsetRow;

	public DirectoryTreeDocument() {
		decorator = new DirectoryParserPlainDecorator();

		offsetCol = 0;
		offsetRow = 0;
	}

	private class Image {

		private ByteArrayOutputStream byteArrayOut1;

		public void load(final InputStream stream) throws IOException {
			BufferedImage map1 = ImageIO.read(stream);
			byteArrayOut1 = new ByteArrayOutputStream();
			ImageIO.write(map1, "png", byteArrayOut1);
		}

		public byte[] toByteArray() {
			return byteArrayOut1.toByteArray();
		}

		public void release() throws IOException {
			byteArrayOut1.close();
		}
	}

	private Image imgFile;
	private Image imgDirectory;

	public boolean create(final File directory, final File destFile) {
		boolean result = false;

		countFile = 0;
		maxCol = 0;

		try {
			imgFile = new Image();
			imgDirectory = new Image();
			imgFile.load(this.getClass().getResourceAsStream("/file.png"));
			imgDirectory.load(this.getClass().getResourceAsStream("/directory.png"));

			XSSFWorkbook wb = new XSSFWorkbook();
			sheet = wb.createSheet("ディレクトリ構成");

			DirectoryParser parser = new BasicDirectoryParser();
			parser.setDecorator(decorator);
			parser.addListener(new DirectoryParserListener() {
				@Override
				public void documentParserCallback(final DirectoryParserEvent event) {
					onFindFile(event);
				}
			});
			parser.parse(directory);

			// 
			for (int col = 0; col <= maxCol; col++) {
				sheet.setColumnWidth(offsetCol + col, 2 * 256 + 60);
			}

			FileOutputStream out = new FileOutputStream(destFile);
			wb.write(out);
			out.close();

			// image
			imgFile.release();
			imgDirectory.release();

			result = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		}

		return result;
	}

	private void onFindFile(final DirectoryParserEvent event) {
		DirectoryParserFileInfo info = event.getInfo();
		String prefix = info.getPrefix();
		File file = info.getFile();

		XSSFRow row = sheet.createRow(offsetRow + countFile);
		int col = 0;
		for (int i = 0; i < prefix.length(); i++) {
			Character c = prefix.charAt(i);

			XSSFCell cell = row.createCell(offsetCol + col);
			cell.setCellValue(c.toString());
			col++;
		}

		XSSFDrawing patriarch = sheet.createDrawingPatriarch();
		XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 18, 18, (short) (offsetCol + col), (offsetRow + countFile),
				(short) (offsetCol + col + 1), (offsetRow + countFile + 1));
		anchor.setAnchorType(XSSFClientAnchor.MOVE_DONT_RESIZE);
		if (file.isFile()) {
			patriarch.createPicture(anchor, sheet.getWorkbook().addPicture(imgFile.toByteArray(), HSSFWorkbook.PICTURE_TYPE_PNG));
		} else {
			patriarch.createPicture(anchor, sheet.getWorkbook().addPicture(imgDirectory.toByteArray(), HSSFWorkbook.PICTURE_TYPE_PNG));
		}

		XSSFCell cell = row.createCell(offsetCol + col + 1);
		cell.setCellValue(file.getName());

		maxCol = Math.max(maxCol, col + 1);

		countFile++;
	}

}
