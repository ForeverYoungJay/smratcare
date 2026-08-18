package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.common.excel.ExcelReportWriter;
import com.zhiyangyun.care.common.excel.ExcelReportWriter.Column;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/** xlsx 写出：表头标题、列头、明细、合计行。 */
class ExcelReportWriterTest {

  @Test
  void writes_title_header_body_and_total_row() throws Exception {
    List<Column> columns = List.of(
        Column.text("长者", 12),
        Column.number("应收", 12),
        Column.text("状态", 10),
        Column.number("欠费", 12));
    List<List<Object>> rows = List.of(
        List.of("张三", new BigDecimal("1200.50"), "部分收款", new BigDecimal("200.50")),
        List.of("李四", new BigDecimal("800.00"), "已结清", BigDecimal.ZERO));

    byte[] bytes = ExcelReportWriter.write("代养费登记表", "代养费登记表", "账期：2026-05", columns, rows);

    try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      Sheet sheet = workbook.getSheetAt(0);
      assertEquals("代养费登记表", sheet.getSheetName());

      // 第 1 行是跨列合并的标题
      assertEquals("代养费登记表", sheet.getRow(0).getCell(0).getStringCellValue());
      assertEquals(1, sheet.getNumMergedRegions() > 0 ? 1 : 0);
      assertEquals(0, sheet.getMergedRegion(0).getFirstRow());
      assertEquals(3, sheet.getMergedRegion(0).getLastColumn());

      // 第 2 行副标题带账期与导出时间
      String subTitle = sheet.getRow(1).getCell(0).getStringCellValue();
      assertTrue(subTitle.contains("账期：2026-05"), "副标题应包含账期");
      assertTrue(subTitle.contains("导出时间"), "副标题应包含导出时间");

      // 第 3 行列头
      Row header = sheet.getRow(2);
      assertEquals("长者", header.getCell(0).getStringCellValue());
      assertEquals("应收", header.getCell(1).getStringCellValue());
      assertEquals("欠费", header.getCell(3).getStringCellValue());

      // 明细按数字写入，便于在 Excel 里继续算
      Row first = sheet.getRow(3);
      assertEquals("张三", first.getCell(0).getStringCellValue());
      assertEquals(1200.50, first.getCell(1).getNumericCellValue(), 0.001);

      // 合计行只对数值列求和，文本列留空
      Row total = sheet.getRow(5);
      assertEquals("合计（2 条）", total.getCell(0).getStringCellValue());
      assertEquals(2000.50, total.getCell(1).getNumericCellValue(), 0.001);
      assertEquals("", total.getCell(2).getStringCellValue());
      assertEquals(200.50, total.getCell(3).getNumericCellValue(), 0.001);
    }
  }

  @Test
  void empty_rows_still_produce_total_row() throws Exception {
    byte[] bytes = ExcelReportWriter.write(
        "电费月报", "电费月报", "账期：2026-05",
        List.of(Column.text("房间", 10), Column.number("电费", 12)),
        List.of());

    try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      Sheet sheet = workbook.getSheetAt(0);
      Row total = sheet.getRow(3);
      assertEquals("合计（0 条）", total.getCell(0).getStringCellValue());
      assertEquals(0.0, total.getCell(1).getNumericCellValue(), 0.001);
    }
  }

  @Test
  void non_numeric_value_in_numeric_column_falls_back_to_zero() throws Exception {
    byte[] bytes = ExcelReportWriter.write(
        "押金台账", "押金台账", null,
        List.of(Column.text("长者", 10), Column.number("在押余额", 12)),
        List.of(List.of("王五", "N/A")));

    try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
      Sheet sheet = workbook.getSheetAt(0);
      assertEquals(0.0, sheet.getRow(3).getCell(1).getNumericCellValue(), 0.001);
      assertEquals(0.0, sheet.getRow(4).getCell(1).getNumericCellValue(), 0.001);
    }
  }

  @Test
  void empty_columns_is_rejected() {
    assertThrows(IllegalArgumentException.class,
        () -> ExcelReportWriter.write("空报表", "空报表", null, List.of(), List.of()));
  }
}
