package com.zhiyangyun.care.common.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 统一的 xlsx 报表写出：跨列合并的表头标题 + 副标题 + 列头 + 数据 + 合计行。
 * 数值列由 {@link Column#numeric()} 标记，合计行只对这些列求和。
 */
public final class ExcelReportWriter {
  private static final DateTimeFormatter STAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /** 一列的定义。numeric 为 true 时按数字写入并参与合计。 */
  public record Column(String header, int width, boolean numeric) {
    public static Column text(String header, int width) {
      return new Column(header, width, false);
    }

    public static Column number(String header, int width) {
      return new Column(header, width, true);
    }
  }

  private ExcelReportWriter() {
  }

  public static byte[] write(
      String sheetName,
      String title,
      String subTitle,
      List<Column> columns,
      List<List<Object>> rows) {
    if (columns == null || columns.isEmpty()) {
      throw new IllegalArgumentException("报表至少需要一列");
    }
    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sheet = workbook.createSheet(sheetName == null || sheetName.isBlank() ? "报表" : sheetName);
      int lastColumn = columns.size() - 1;

      CellStyle titleStyle = createTitleStyle(workbook);
      CellStyle subTitleStyle = createSubTitleStyle(workbook);
      CellStyle headerStyle = createHeaderStyle(workbook);
      CellStyle textStyle = createBodyStyle(workbook, false, false);
      CellStyle numberStyle = createBodyStyle(workbook, true, false);
      CellStyle totalTextStyle = createBodyStyle(workbook, false, true);
      CellStyle totalNumberStyle = createBodyStyle(workbook, true, true);

      int rowIndex = 0;
      Row titleRow = sheet.createRow(rowIndex++);
      titleRow.setHeightInPoints(24);
      Cell titleCell = titleRow.createCell(0);
      titleCell.setCellValue(title == null ? "" : title);
      titleCell.setCellStyle(titleStyle);
      if (lastColumn > 0) {
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastColumn));
      }

      Row subTitleRow = sheet.createRow(rowIndex++);
      Cell subTitleCell = subTitleRow.createCell(0);
      subTitleCell.setCellValue((subTitle == null || subTitle.isBlank() ? "" : subTitle + "　")
          + "导出时间：" + LocalDateTime.now().format(STAMP_FMT));
      subTitleCell.setCellStyle(subTitleStyle);
      if (lastColumn > 0) {
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, lastColumn));
      }

      Row headerRow = sheet.createRow(rowIndex++);
      headerRow.setHeightInPoints(18);
      for (int i = 0; i < columns.size(); i += 1) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns.get(i).header());
        cell.setCellStyle(headerStyle);
        sheet.setColumnWidth(i, Math.max(columns.get(i).width(), 8) * 256);
      }

      BigDecimal[] totals = new BigDecimal[columns.size()];
      List<List<Object>> body = rows == null ? List.of() : rows;
      for (List<Object> row : body) {
        Row dataRow = sheet.createRow(rowIndex++);
        for (int i = 0; i < columns.size(); i += 1) {
          Object value = i < row.size() ? row.get(i) : null;
          Cell cell = dataRow.createCell(i);
          if (columns.get(i).numeric()) {
            BigDecimal decimal = toDecimal(value);
            cell.setCellValue(decimal.doubleValue());
            cell.setCellStyle(numberStyle);
            totals[i] = (totals[i] == null ? BigDecimal.ZERO : totals[i]).add(decimal);
          } else {
            cell.setCellValue(stringOf(value));
            cell.setCellStyle(textStyle);
          }
        }
      }

      Row totalRow = sheet.createRow(rowIndex);
      for (int i = 0; i < columns.size(); i += 1) {
        Cell cell = totalRow.createCell(i);
        if (i == 0) {
          cell.setCellValue("合计（" + body.size() + " 条）");
          cell.setCellStyle(totalTextStyle);
        } else if (columns.get(i).numeric()) {
          cell.setCellValue((totals[i] == null ? BigDecimal.ZERO : totals[i]).doubleValue());
          cell.setCellStyle(totalNumberStyle);
        } else {
          cell.setCellValue("");
          cell.setCellStyle(totalTextStyle);
        }
      }

      workbook.write(out);
      return out.toByteArray();
    } catch (IOException ex) {
      throw new IllegalStateException("生成 Excel 报表失败", ex);
    }
  }

  private static CellStyle createTitleStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setFontHeightInPoints((short) 15);
    style.setFont(font);
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    return style;
  }

  private static CellStyle createSubTitleStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setFontHeightInPoints((short) 10);
    font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
    style.setFont(font);
    style.setAlignment(HorizontalAlignment.LEFT);
    return style;
  }

  private static CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    applyBorder(style);
    return style;
  }

  private static CellStyle createBodyStyle(Workbook workbook, boolean numeric, boolean total) {
    CellStyle style = workbook.createCellStyle();
    if (total) {
      Font font = workbook.createFont();
      font.setBold(true);
      style.setFont(font);
      style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
      style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
    if (numeric) {
      style.setAlignment(HorizontalAlignment.RIGHT);
      style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
    }
    applyBorder(style);
    return style;
  }

  private static void applyBorder(CellStyle style) {
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
  }

  private static BigDecimal toDecimal(Object value) {
    if (value == null) {
      return BigDecimal.ZERO;
    }
    if (value instanceof BigDecimal decimal) {
      return decimal;
    }
    if (value instanceof Number number) {
      return BigDecimal.valueOf(number.doubleValue());
    }
    try {
      return new BigDecimal(String.valueOf(value).trim());
    } catch (NumberFormatException ignored) {
      return BigDecimal.ZERO;
    }
  }

  private static String stringOf(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof LocalDateTime time) {
      return time.format(STAMP_FMT);
    }
    if (value instanceof LocalDate date) {
      return date.toString();
    }
    return String.valueOf(value);
  }
}
