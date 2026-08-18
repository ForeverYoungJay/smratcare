package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.common.excel.ExcelReportWriter;
import com.zhiyangyun.care.common.excel.ExcelReportWriter.Column;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.finance.entity.FinanceDepositAccount;
import com.zhiyangyun.care.finance.entity.FinanceElectricityReading;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.mapper.FinanceDepositAccountMapper;
import com.zhiyangyun.care.finance.mapper.FinanceElectricityReadingMapper;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 四种 Excel 报表：代养费登记表、押金台账、电费月报、欠费催缴清单。 */
@RestController
@RequestMapping("/api/finance/report/excel")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class FinanceExcelReportController {
  private static final String XLSX_MEDIA_TYPE =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  private final BillMonthlyMapper billMonthlyMapper;
  private final PaymentRecordMapper paymentRecordMapper;
  private final FinanceDepositAccountMapper depositAccountMapper;
  private final FinanceElectricityReadingMapper electricityReadingMapper;
  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;

  public FinanceExcelReportController(BillMonthlyMapper billMonthlyMapper,
      PaymentRecordMapper paymentRecordMapper,
      FinanceDepositAccountMapper depositAccountMapper,
      FinanceElectricityReadingMapper electricityReadingMapper,
      ElderMapper elderMapper,
      BedMapper bedMapper,
      RoomMapper roomMapper) {
    this.billMonthlyMapper = billMonthlyMapper;
    this.paymentRecordMapper = paymentRecordMapper;
    this.depositAccountMapper = depositAccountMapper;
    this.electricityReadingMapper = electricityReadingMapper;
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
  }

  /** 代养费登记表：按账期列出应收、已收、三项抵扣与欠费。 */
  @GetMapping("/care-fee-register")
  public ResponseEntity<byte[]> careFeeRegister(@RequestParam(required = false) String month) {
    Long orgId = AuthContext.getOrgId();
    YearMonth targetMonth = parseMonth(month);
    List<BillMonthly> bills = loadBills(orgId, targetMonth, false);
    Map<Long, ElderProfile> elderMap = loadElders(orgId, bills.stream()
        .map(BillMonthly::getElderId).filter(Objects::nonNull).toList());
    Map<Long, String[]> bedRoomMap = loadBedRoom(elderMap.values());
    Map<Long, BigDecimal[]> deductionMap = loadDeductions(bills);

    List<Column> columns = List.of(
        Column.text("长者", 14),
        Column.text("房间 / 床位", 18),
        Column.text("账单月份", 12),
        Column.number("应收金额", 14),
        Column.number("长护险抵扣", 14),
        Column.number("折扣减免", 14),
        Column.number("消费券抵扣", 14),
        Column.number("实收现金", 14),
        Column.number("欠费金额", 14),
        Column.text("状态", 12));
    List<List<Object>> rows = new ArrayList<>();
    for (BillMonthly bill : bills) {
      ElderProfile elder = elderMap.get(bill.getElderId());
      BigDecimal[] deduction = deductionMap.getOrDefault(bill.getId(),
          new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
      rows.add(List.of(
          nameOf(elder, bill.getElderId()),
          bedRoomText(bedRoomMap, bill.getElderId()),
          stringOf(bill.getBillMonth()),
          scale2(bill.getTotalAmount()),
          deduction[0],
          deduction[1],
          deduction[2],
          deduction[3],
          scale2(bill.getOutstandingAmount()),
          billStatusText(bill)));
    }
    byte[] bytes = ExcelReportWriter.write(
        "代养费登记表",
        "代养费登记表",
        "账期：" + targetMonth,
        columns,
        rows);
    return xlsx("代养费登记表-" + targetMonth, bytes);
  }

  /** 押金台账：应缴、已缴、已扣、已退与在押余额。 */
  @GetMapping("/deposit-ledger")
  public ResponseEntity<byte[]> depositLedger(@RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(FinanceDepositAccount.class)
        .eq(FinanceDepositAccount::getIsDeleted, 0)
        .eq(orgId != null, FinanceDepositAccount::getOrgId, orgId);
    if (status != null && !status.isBlank()) {
      wrapper.eq(FinanceDepositAccount::getStatus, status.trim().toUpperCase(java.util.Locale.ROOT));
    }
    List<FinanceDepositAccount> accounts = depositAccountMapper.selectList(wrapper);
    Map<Long, ElderProfile> elderMap = loadElders(orgId, accounts.stream()
        .map(FinanceDepositAccount::getElderId).filter(Objects::nonNull).toList());
    Map<Long, String[]> bedRoomMap = loadBedRoom(elderMap.values());

    List<Column> columns = List.of(
        Column.text("长者", 14),
        Column.text("护理等级", 14),
        Column.text("房间 / 床位", 18),
        Column.number("应缴标准", 14),
        Column.number("已缴", 14),
        Column.number("应缴差额", 14),
        Column.number("已扣", 14),
        Column.number("已退", 14),
        Column.number("在押余额", 14),
        Column.text("状态", 12));
    List<List<Object>> rows = new ArrayList<>();
    for (FinanceDepositAccount account : accounts) {
      ElderProfile elder = elderMap.get(account.getElderId());
      BigDecimal gap = scale2(account.getStandardAmount()).subtract(scale2(account.getPaidAmount()));
      rows.add(List.of(
          nameOf(elder, account.getElderId()),
          elder == null || elder.getCareLevel() == null ? "" : elder.getCareLevel(),
          bedRoomText(bedRoomMap, account.getElderId()),
          scale2(account.getStandardAmount()),
          scale2(account.getPaidAmount()),
          gap.max(BigDecimal.ZERO),
          scale2(account.getDeductedAmount()),
          scale2(account.getRefundedAmount()),
          scale2(account.getBalanceAmount()),
          depositStatusText(account.getStatus())));
    }
    byte[] bytes = ExcelReportWriter.write(
        "押金台账",
        "押金台账",
        "统计范围：" + (status == null || status.isBlank() ? "全部状态" : status),
        columns,
        rows);
    return xlsx("押金台账-" + LocalDate.now(), bytes);
  }

  /** 电费月报：按房间列出读数、用电量与电费。 */
  @GetMapping("/electricity-monthly")
  public ResponseEntity<byte[]> electricityMonthly(@RequestParam(required = false) String month) {
    Long orgId = AuthContext.getOrgId();
    YearMonth targetMonth = parseMonth(month);
    List<FinanceElectricityReading> readings = electricityReadingMapper.selectList(
        Wrappers.lambdaQuery(FinanceElectricityReading.class)
            .eq(FinanceElectricityReading::getIsDeleted, 0)
            .eq(orgId != null, FinanceElectricityReading::getOrgId, orgId)
            .eq(FinanceElectricityReading::getBillMonth, targetMonth.toString())
            .orderByAsc(FinanceElectricityReading::getBuilding)
            .orderByAsc(FinanceElectricityReading::getFloorNo)
            .orderByAsc(FinanceElectricityReading::getRoomNo));

    List<Column> columns = List.of(
        Column.text("楼栋", 12),
        Column.text("楼层", 10),
        Column.text("房间", 12),
        Column.number("上期读数", 14),
        Column.number("本期读数", 14),
        Column.number("用电量(度)", 14),
        Column.number("电价(元/度)", 14),
        Column.number("电费金额", 14),
        Column.text("分摊方式", 16),
        Column.number("分摊人数", 12),
        Column.number("人均金额", 14),
        Column.text("缴费状态", 12));
    List<List<Object>> rows = new ArrayList<>();
    for (FinanceElectricityReading reading : readings) {
      rows.add(List.of(
          stringOf(reading.getBuilding()),
          stringOf(reading.getFloorNo()),
          stringOf(reading.getRoomNo()),
          scale2(reading.getPreviousReading()),
          scale2(reading.getCurrentReading()),
          scale2(reading.getUsageAmount()),
          reading.getUnitPrice() == null ? BigDecimal.ZERO : reading.getUnitPrice(),
          scale2(reading.getFeeAmount()),
          "PER_RESIDENT".equals(reading.getShareMode()) ? "按在住人数均摊" : "整间计费",
          reading.getResidentCount() == null ? 0 : reading.getResidentCount(),
          scale2(reading.getPerResidentAmount()),
          "PAID".equals(reading.getPayStatus()) ? "已缴" : "未缴"));
    }
    byte[] bytes = ExcelReportWriter.write(
        "电费月报",
        "电费月报",
        "账期：" + targetMonth,
        columns,
        rows);
    return xlsx("电费月报-" + targetMonth, bytes);
  }

  /** 欠费催缴清单：只列欠费未清的账单，附押金差额与电费未缴作为催缴依据。 */
  @GetMapping("/overdue-collection")
  public ResponseEntity<byte[]> overdueCollection(@RequestParam(required = false) String month) {
    Long orgId = AuthContext.getOrgId();
    YearMonth targetMonth = parseMonth(month);
    List<BillMonthly> bills = loadBills(orgId, targetMonth, true);
    Map<Long, ElderProfile> elderMap = loadElders(orgId, bills.stream()
        .map(BillMonthly::getElderId).filter(Objects::nonNull).toList());
    Map<Long, String[]> bedRoomMap = loadBedRoom(elderMap.values());
    Map<Long, BigDecimal> depositGapMap = loadDepositGap(orgId);

    List<Column> columns = List.of(
        Column.text("长者", 14),
        Column.text("房间 / 床位", 18),
        Column.text("联系电话", 16),
        Column.text("账单月份", 12),
        Column.number("应收金额", 14),
        Column.number("已收金额", 14),
        Column.number("欠费金额", 14),
        Column.number("押金差额", 14),
        Column.text("催缴说明", 30));
    List<List<Object>> rows = new ArrayList<>();
    for (BillMonthly bill : bills) {
      ElderProfile elder = elderMap.get(bill.getElderId());
      BigDecimal outstanding = scale2(bill.getOutstandingAmount());
      BigDecimal depositGap = depositGapMap.getOrDefault(bill.getElderId(), BigDecimal.ZERO);
      List<String> notes = new ArrayList<>();
      notes.add("账单欠费 " + outstanding + " 元");
      if (depositGap.compareTo(BigDecimal.ZERO) > 0) {
        notes.add("押金还差 " + depositGap + " 元");
      }
      rows.add(List.of(
          nameOf(elder, bill.getElderId()),
          bedRoomText(bedRoomMap, bill.getElderId()),
          elder == null || elder.getPhone() == null ? "" : elder.getPhone(),
          stringOf(bill.getBillMonth()),
          scale2(bill.getTotalAmount()),
          scale2(bill.getPaidAmount()),
          outstanding,
          depositGap,
          String.join("；", notes)));
    }
    byte[] bytes = ExcelReportWriter.write(
        "欠费催缴清单",
        "欠费催缴清单",
        "账期：" + targetMonth,
        columns,
        rows);
    return xlsx("欠费催缴清单-" + targetMonth, bytes);
  }

  private List<BillMonthly> loadBills(Long orgId, YearMonth month, boolean onlyOutstanding) {
    var wrapper = Wrappers.lambdaQuery(BillMonthly.class)
        .eq(BillMonthly::getIsDeleted, 0)
        .eq(orgId != null, BillMonthly::getOrgId, orgId)
        .eq(BillMonthly::getBillMonth, month.toString())
        .ne(BillMonthly::getStatus, 9);
    if (onlyOutstanding) {
      wrapper.gt(BillMonthly::getOutstandingAmount, BigDecimal.ZERO);
    }
    wrapper.orderByDesc(BillMonthly::getOutstandingAmount).orderByAsc(BillMonthly::getElderId);
    return billMonthlyMapper.selectList(wrapper);
  }

  /** billId -> [长护险, 折扣, 消费券, 实收现金]。 */
  private Map<Long, BigDecimal[]> loadDeductions(List<BillMonthly> bills) {
    Map<Long, BigDecimal[]> result = new HashMap<>();
    List<Long> billIds = bills.stream().map(BillMonthly::getId).filter(Objects::nonNull).toList();
    if (billIds.isEmpty()) {
      return result;
    }
    List<PaymentRecord> records = paymentRecordMapper.selectList(
        Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .in(PaymentRecord::getBillMonthlyId, billIds));
    for (PaymentRecord record : records) {
      BigDecimal[] slot = result.computeIfAbsent(record.getBillMonthlyId(),
          key -> new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
      slot[0] = slot[0].add(scale2(record.getLtciDeductAmount()));
      slot[1] = slot[1].add(scale2(record.getDiscountAmount()));
      slot[2] = slot[2].add(scale2(record.getVoucherAmount()));
      slot[3] = slot[3].add(scale2(record.getAmount()));
    }
    return result;
  }

  private Map<Long, BigDecimal> loadDepositGap(Long orgId) {
    Map<Long, BigDecimal> result = new HashMap<>();
    List<FinanceDepositAccount> accounts = depositAccountMapper.selectList(
        Wrappers.lambdaQuery(FinanceDepositAccount.class)
            .eq(FinanceDepositAccount::getIsDeleted, 0)
            .eq(orgId != null, FinanceDepositAccount::getOrgId, orgId)
            .ne(FinanceDepositAccount::getStatus, "CLOSED"));
    for (FinanceDepositAccount account : accounts) {
      BigDecimal gap = scale2(account.getStandardAmount()).subtract(scale2(account.getPaidAmount()));
      if (gap.compareTo(BigDecimal.ZERO) > 0) {
        result.put(account.getElderId(), gap);
      }
    }
    return result;
  }

  private Map<Long, ElderProfile> loadElders(Long orgId, List<Long> elderIds) {
    List<Long> distinct = elderIds.stream().filter(Objects::nonNull).distinct().toList();
    if (distinct.isEmpty()) {
      return new HashMap<>();
    }
    return elderMapper.selectList(
            Wrappers.lambdaQuery(ElderProfile.class)
                .eq(orgId != null, ElderProfile::getOrgId, orgId)
                .in(ElderProfile::getId, distinct))
        .stream()
        .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a, HashMap::new));
  }

  /** elderId -> [roomNo, bedNo]。 */
  private Map<Long, String[]> loadBedRoom(java.util.Collection<ElderProfile> elders) {
    Map<Long, String[]> result = new HashMap<>();
    List<Long> bedIds = elders.stream()
        .map(ElderProfile::getBedId).filter(Objects::nonNull).distinct().toList();
    if (bedIds.isEmpty()) {
      return result;
    }
    Map<Long, Bed> bedMap = bedMapper.selectList(
            Wrappers.lambdaQuery(Bed.class).in(Bed::getId, bedIds))
        .stream()
        .collect(Collectors.toMap(Bed::getId, Function.identity(), (a, b) -> a, HashMap::new));
    List<Long> roomIds = bedMap.values().stream()
        .map(Bed::getRoomId).filter(Objects::nonNull).distinct().toList();
    Map<Long, Room> roomMap = roomIds.isEmpty()
        ? new HashMap<>()
        : roomMapper.selectList(Wrappers.lambdaQuery(Room.class).in(Room::getId, roomIds))
            .stream()
            .collect(Collectors.toMap(Room::getId, Function.identity(), (a, b) -> a, HashMap::new));
    for (ElderProfile elder : elders) {
      Bed bed = elder.getBedId() == null ? null : bedMap.get(elder.getBedId());
      Room room = bed == null || bed.getRoomId() == null ? null : roomMap.get(bed.getRoomId());
      result.put(elder.getId(), new String[] {
          room == null ? null : room.getRoomNo(),
          bed == null ? null : bed.getBedNo()
      });
    }
    return result;
  }

  private String bedRoomText(Map<Long, String[]> map, Long elderId) {
    String[] pair = elderId == null ? null : map.get(elderId);
    if (pair == null) {
      return "-";
    }
    return (pair[0] == null ? "-" : pair[0]) + " / " + (pair[1] == null ? "-" : pair[1]);
  }

  private String nameOf(ElderProfile elder, Long elderId) {
    if (elder != null && elder.getFullName() != null) {
      return elder.getFullName();
    }
    return elderId == null ? "-" : ("长者#" + elderId);
  }

  private static String billStatusText(BillMonthly bill) {
    BigDecimal outstanding = scale2(bill.getOutstandingAmount());
    if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
      return "已结清";
    }
    return scale2(bill.getPaidAmount()).compareTo(BigDecimal.ZERO) > 0 ? "部分收款" : "未收款";
  }

  private static String depositStatusText(String status) {
    if (status == null) {
      return "-";
    }
    return switch (status) {
      case "UNPAID" -> "未缴";
      case "PARTIAL" -> "部分缴";
      case "PAID" -> "已缴清";
      case "CLOSED" -> "已结清";
      default -> status;
    };
  }

  private static YearMonth parseMonth(String month) {
    if (month == null || month.isBlank()) {
      return YearMonth.now();
    }
    try {
      return YearMonth.parse(month.trim());
    } catch (Exception ignored) {
      throw new IllegalArgumentException("账期格式应为 yyyy-MM");
    }
  }

  private static BigDecimal scale2(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }

  private static String stringOf(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private ResponseEntity<byte[]> xlsx(String filename, byte[] bytes) {
    String encoded = URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8).replace("+", "%20");
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(XLSX_MEDIA_TYPE))
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"report.xlsx\"; filename*=UTF-8''" + encoded)
        .body(bytes);
  }
}
