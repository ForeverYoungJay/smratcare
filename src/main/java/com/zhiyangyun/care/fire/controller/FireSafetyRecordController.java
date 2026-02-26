package com.zhiyangyun.care.fire.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.util.QrCodeUtil;
import com.zhiyangyun.care.fire.entity.FireSafetyRecord;
import com.zhiyangyun.care.fire.mapper.FireSafetyRecordMapper;
import com.zhiyangyun.care.fire.model.FireSafetyQrResponse;
import com.zhiyangyun.care.fire.model.FireSafetyReportDetailResponse;
import com.zhiyangyun.care.fire.model.FireSafetyRecordRequest;
import com.zhiyangyun.care.fire.model.FireSafetyReportSummaryResponse;
import com.zhiyangyun.care.fire.model.FireSafetyScanCompleteRequest;
import com.zhiyangyun.care.fire.model.FireSafetyTypeCount;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fire/records")
public class FireSafetyRecordController {
  private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final Set<String> ALLOWED_TYPES = Set.of(
      "FACILITY",
      "CONTROL_ROOM_DUTY",
      "MONTHLY_CHECK",
      "DAY_PATROL",
      "NIGHT_PATROL",
      "MAINTENANCE_REPORT",
      "FAULT_MAINTENANCE");

  private static final List<String> TYPE_ORDER = List.of(
      "FACILITY",
      "CONTROL_ROOM_DUTY",
      "MONTHLY_CHECK",
      "DAY_PATROL",
      "NIGHT_PATROL",
      "MAINTENANCE_REPORT",
      "FAULT_MAINTENANCE");

  private static final Set<String> ALLOWED_STATUS = Set.of("OPEN", "CLOSED");

  private final FireSafetyRecordMapper recordMapper;

  public FireSafetyRecordController(FireSafetyRecordMapper recordMapper) {
    this.recordMapper = recordMapper;
  }

  @GetMapping("/page")
  public Result<IPage<FireSafetyRecord>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String recordType,
      @RequestParam(required = false) String inspectorName,
      @RequestParam(required = false) String status,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime checkTimeStart,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime checkTimeEnd) {
    var wrapper = buildQueryWrapper(keyword, recordType, inspectorName, status, checkTimeStart, checkTimeEnd);
    wrapper.orderByDesc(FireSafetyRecord::getCheckTime);
    return Result.ok(recordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<FireSafetyRecord> create(@Valid @RequestBody FireSafetyRecordRequest request) {
    Long orgId = AuthContext.getOrgId();
    FireSafetyRecord record = new FireSafetyRecord();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setRecordType(normalizeRecordTypeForWrite(request.getRecordType()));
    record.setTitle(request.getTitle());
    record.setLocation(request.getLocation());
    record.setInspectorName(request.getInspectorName());
    record.setCheckTime(request.getCheckTime());
    record.setStatus(normalizeStatusForWrite(request.getStatus()));
    record.setIssueDescription(request.getIssueDescription());
    record.setActionTaken(request.getActionTaken());
    record.setNextCheckDate(request.getNextCheckDate());
    record.setDutyRecord(request.getDutyRecord());
    record.setHandoverPunchTime(request.getHandoverPunchTime());
    record.setEquipmentBatchNo(request.getEquipmentBatchNo());
    record.setEquipmentUpdateNote(request.getEquipmentUpdateNote());
    record.setEquipmentAgingDisposal(request.getEquipmentAgingDisposal());
    record.setCreatedBy(AuthContext.getStaffId());
    recordMapper.insert(record);
    return Result.ok(record);
  }

  @PutMapping("/{id}")
  public Result<FireSafetyRecord> update(@PathVariable Long id, @Valid @RequestBody FireSafetyRecordRequest request) {
    FireSafetyRecord existing = findAccessibleRecord(id);
    if (existing == null) {
      return Result.ok(null);
    }
    existing.setRecordType(normalizeRecordTypeForWrite(request.getRecordType()));
    existing.setTitle(request.getTitle());
    existing.setLocation(request.getLocation());
    existing.setInspectorName(request.getInspectorName());
    existing.setCheckTime(request.getCheckTime());
    existing.setStatus(normalizeStatusForWrite(request.getStatus()));
    existing.setIssueDescription(request.getIssueDescription());
    existing.setActionTaken(request.getActionTaken());
    existing.setNextCheckDate(request.getNextCheckDate());
    existing.setDutyRecord(request.getDutyRecord());
    existing.setHandoverPunchTime(request.getHandoverPunchTime());
    existing.setEquipmentBatchNo(request.getEquipmentBatchNo());
    existing.setEquipmentUpdateNote(request.getEquipmentUpdateNote());
    existing.setEquipmentAgingDisposal(request.getEquipmentAgingDisposal());
    recordMapper.updateById(existing);
    return Result.ok(existing);
  }

  @PutMapping("/{id}/close")
  public Result<FireSafetyRecord> close(@PathVariable Long id) {
    FireSafetyRecord existing = findAccessibleRecord(id);
    if (existing == null) {
      return Result.ok(null);
    }
    existing.setStatus("CLOSED");
    existing.setUpdateTime(LocalDateTime.now());
    recordMapper.updateById(existing);
    return Result.ok(existing);
  }

  @PostMapping("/{id}/qr/generate")
  public Result<FireSafetyQrResponse> generateQr(@PathVariable Long id) {
    FireSafetyRecord existing = findAccessibleRecord(id);
    if (existing == null) {
      return Result.ok(null);
    }
    LocalDateTime now = LocalDateTime.now();
    String token = QrCodeUtil.generate();
    existing.setQrToken(token);
    existing.setQrGeneratedAt(now);
    recordMapper.updateById(existing);

    FireSafetyQrResponse response = new FireSafetyQrResponse();
    response.setRecordId(existing.getId());
    response.setQrToken(token);
    response.setQrContent("FIRE_PATROL:" + existing.getId() + ":" + token);
    response.setGeneratedAt(now);
    return Result.ok(response);
  }

  @PostMapping("/scan/complete")
  public Result<FireSafetyRecord> scanComplete(@Valid @RequestBody FireSafetyScanCompleteRequest request) {
    Long orgId = AuthContext.getOrgId();
    String token = request.getQrToken() == null ? null : request.getQrToken().trim();
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("qrToken 不能为空");
    }
    FireSafetyRecord existing = recordMapper.selectOne(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .eq(FireSafetyRecord::getQrToken, token));
    if (existing == null) {
      return Result.ok(null);
    }
    existing.setScanCompletedAt(LocalDateTime.now());
    existing.setStatus("CLOSED");
    if (request.getInspectorName() != null && !request.getInspectorName().isBlank()) {
      existing.setInspectorName(request.getInspectorName().trim());
    }
    if (request.getActionTaken() != null && !request.getActionTaken().isBlank()) {
      existing.setActionTaken(request.getActionTaken().trim());
    }
    recordMapper.updateById(existing);
    return Result.ok(existing);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    FireSafetyRecord existing = findAccessibleRecord(id);
    if (existing != null) {
      existing.setIsDeleted(1);
      recordMapper.updateById(existing);
    }
    return Result.ok(null);
  }

  @GetMapping("/summary")
  public Result<FireSafetyReportSummaryResponse> summary(
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateTo) {
    Long orgId = AuthContext.getOrgId();
    LocalDate startDate = dateFrom;
    LocalDate endDate = dateTo;
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      LocalDate temp = startDate;
      startDate = endDate;
      endDate = temp;
    }

    LocalDateTime startTime = startDate == null ? null : startDate.atStartOfDay();
    LocalDateTime endTime = endDate == null ? null : endDate.plusDays(1).atStartOfDay().minusNanos(1);

    var baseWrapper = Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .ge(startTime != null, FireSafetyRecord::getCheckTime, startTime)
        .le(endTime != null, FireSafetyRecord::getCheckTime, endTime);

    long totalCount = recordMapper.selectCount(baseWrapper);

    long openCount = recordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .ge(startTime != null, FireSafetyRecord::getCheckTime, startTime)
        .le(endTime != null, FireSafetyRecord::getCheckTime, endTime)
        .eq(FireSafetyRecord::getStatus, "OPEN"));

    long closedCount = recordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .ge(startTime != null, FireSafetyRecord::getCheckTime, startTime)
        .le(endTime != null, FireSafetyRecord::getCheckTime, endTime)
        .eq(FireSafetyRecord::getStatus, "CLOSED"));

    long overdueCount = recordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .ge(startTime != null, FireSafetyRecord::getCheckTime, startTime)
        .le(endTime != null, FireSafetyRecord::getCheckTime, endTime)
        .eq(FireSafetyRecord::getStatus, "OPEN")
        .isNotNull(FireSafetyRecord::getNextCheckDate)
        .lt(FireSafetyRecord::getNextCheckDate, LocalDate.now()));

    List<Map<String, Object>> typeMapRows = recordMapper.selectMaps(Wrappers.query(FireSafetyRecord.class)
        .select("record_type as recordType", "count(1) as cnt")
        .eq("is_deleted", 0)
        .eq(orgId != null, "org_id", orgId)
        .ge(startTime != null, "check_time", startTime)
        .le(endTime != null, "check_time", endTime)
        .groupBy("record_type"));

    Map<String, Long> typeCountMap = typeMapRows.stream().collect(Collectors.toMap(
        row -> String.valueOf(row.get("recordType")),
        row -> Long.parseLong(String.valueOf(row.get("cnt"))),
        (a, b) -> a,
        LinkedHashMap::new));

    List<FireSafetyTypeCount> typeStats = TYPE_ORDER.stream().map(type -> {
      FireSafetyTypeCount stat = new FireSafetyTypeCount();
      stat.setRecordType(type);
      stat.setCount(typeCountMap.getOrDefault(type, 0L));
      return stat;
    }).toList();

    FireSafetyReportSummaryResponse response = new FireSafetyReportSummaryResponse();
    response.setTotalCount(totalCount);
    response.setOpenCount(openCount);
    response.setClosedCount(closedCount);
    response.setOverdueCount(overdueCount);
    response.setDailyCompletedCount(recordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .ge(startTime != null, FireSafetyRecord::getCheckTime, startTime)
        .le(endTime != null, FireSafetyRecord::getCheckTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "DAY_PATROL")
        .isNotNull(FireSafetyRecord::getScanCompletedAt)));
    response.setMonthlyCompletedCount(recordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .ge(startTime != null, FireSafetyRecord::getCheckTime, startTime)
        .le(endTime != null, FireSafetyRecord::getCheckTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "MONTHLY_CHECK")
        .isNotNull(FireSafetyRecord::getScanCompletedAt)));
    response.setDutyRecordCount(recordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .ge(startTime != null, FireSafetyRecord::getCheckTime, startTime)
        .le(endTime != null, FireSafetyRecord::getCheckTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "CONTROL_ROOM_DUTY")
        .isNotNull(FireSafetyRecord::getDutyRecord)
        .ne(FireSafetyRecord::getDutyRecord, "")));
    response.setHandoverPunchCount(recordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .ge(startTime != null, FireSafetyRecord::getCheckTime, startTime)
        .le(endTime != null, FireSafetyRecord::getCheckTime, endTime)
        .isNotNull(FireSafetyRecord::getHandoverPunchTime)));
    response.setEquipmentUpdateCount(recordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .ge(startTime != null, FireSafetyRecord::getCheckTime, startTime)
        .le(endTime != null, FireSafetyRecord::getCheckTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "FACILITY")
        .isNotNull(FireSafetyRecord::getEquipmentBatchNo)
        .ne(FireSafetyRecord::getEquipmentBatchNo, "")));
    response.setEquipmentAgingDisposalCount(recordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .ge(startTime != null, FireSafetyRecord::getCheckTime, startTime)
        .le(endTime != null, FireSafetyRecord::getCheckTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "FACILITY")
        .isNotNull(FireSafetyRecord::getEquipmentAgingDisposal)
        .ne(FireSafetyRecord::getEquipmentAgingDisposal, "")));
    response.setTypeStats(typeStats);
    return Result.ok(response);
  }

  @GetMapping("/report/detail")
  public Result<FireSafetyReportDetailResponse> detail(
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(defaultValue = "100") int limit) {
    LocalDate startDate = dateFrom;
    LocalDate endDate = dateTo;
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      LocalDate temp = startDate;
      startDate = endDate;
      endDate = temp;
    }
    LocalDateTime startTime = startDate == null ? null : startDate.atStartOfDay();
    LocalDateTime endTime = endDate == null ? null : endDate.plusDays(1).atStartOfDay().minusNanos(1);
    int safeLimit = Math.max(1, Math.min(limit, 1000));
    Long orgId = AuthContext.getOrgId();

    List<FireSafetyRecord> records = recordMapper.selectList(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .ge(startTime != null, FireSafetyRecord::getCheckTime, startTime)
        .le(endTime != null, FireSafetyRecord::getCheckTime, endTime)
        .orderByDesc(FireSafetyRecord::getCheckTime)
        .last("limit " + safeLimit));

    FireSafetyReportDetailResponse response = new FireSafetyReportDetailResponse();
    response.setTotalCount((long) records.size());
    response.setDailyCompletedCount(records.stream()
        .filter(r -> "DAY_PATROL".equals(r.getRecordType()) && r.getScanCompletedAt() != null)
        .count());
    response.setMonthlyCompletedCount(records.stream()
        .filter(r -> "MONTHLY_CHECK".equals(r.getRecordType()) && r.getScanCompletedAt() != null)
        .count());
    response.setDutyRecordCount(records.stream()
        .filter(r -> "CONTROL_ROOM_DUTY".equals(r.getRecordType()) && notBlank(r.getDutyRecord()))
        .count());
    response.setHandoverPunchCount(records.stream()
        .filter(r -> r.getHandoverPunchTime() != null)
        .count());
    response.setEquipmentUpdateCount(records.stream()
        .filter(r -> "FACILITY".equals(r.getRecordType()) && notBlank(r.getEquipmentBatchNo()))
        .count());
    response.setEquipmentAgingDisposalCount(records.stream()
        .filter(r -> "FACILITY".equals(r.getRecordType()) && notBlank(r.getEquipmentAgingDisposal()))
        .count());

    List<FireSafetyReportDetailResponse.FireSafetyReportRecordItem> items = new ArrayList<>();
    for (FireSafetyRecord record : records) {
      FireSafetyReportDetailResponse.FireSafetyReportRecordItem item =
          new FireSafetyReportDetailResponse.FireSafetyReportRecordItem();
      item.setId(record.getId());
      item.setRecordType(record.getRecordType());
      item.setTitle(record.getTitle());
      item.setLocation(record.getLocation());
      item.setInspectorName(record.getInspectorName());
      item.setCheckTime(formatDateTime(record.getCheckTime()));
      item.setStatus(record.getStatus());
      item.setScanCompletedAt(formatDateTime(record.getScanCompletedAt()));
      item.setDutyRecord(record.getDutyRecord());
      item.setHandoverPunchTime(formatDateTime(record.getHandoverPunchTime()));
      item.setEquipmentBatchNo(record.getEquipmentBatchNo());
      item.setEquipmentUpdateNote(record.getEquipmentUpdateNote());
      item.setEquipmentAgingDisposal(record.getEquipmentAgingDisposal());
      item.setIssueDescription(record.getIssueDescription());
      item.setActionTaken(record.getActionTaken());
      items.add(item);
    }
    response.setRecords(items);
    return Result.ok(response);
  }

  @GetMapping(value = "/report/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateTo) {
    FireSafetyReportDetailResponse detail = detail(dateFrom, dateTo, 1000).getData();
    List<String> headers = List.of(
        "记录ID",
        "类型",
        "标题",
        "位置",
        "负责人",
        "检查时间",
        "扫码完成时间",
        "状态",
        "值班记录",
        "交接班打卡",
        "设备批号",
        "设备更新记录",
        "设备老化处置",
        "问题描述",
        "处置措施");
    List<List<String>> rows = detail.getRecords().stream().map(item -> List.of(
        stringOf(item.getId()),
        stringOf(item.getRecordType()),
        stringOf(item.getTitle()),
        stringOf(item.getLocation()),
        stringOf(item.getInspectorName()),
        stringOf(item.getCheckTime()),
        stringOf(item.getScanCompletedAt()),
        stringOf(item.getStatus()),
        stringOf(item.getDutyRecord()),
        stringOf(item.getHandoverPunchTime()),
        stringOf(item.getEquipmentBatchNo()),
        stringOf(item.getEquipmentUpdateNote()),
        stringOf(item.getEquipmentAgingDisposal()),
        stringOf(item.getIssueDescription()),
        stringOf(item.getActionTaken()))).toList();
    return csvResponse("fire-safety-report", headers, rows);
  }

  private FireSafetyRecord findAccessibleRecord(Long id) {
    Long orgId = AuthContext.getOrgId();
    return recordMapper.selectOne(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getId, id)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId));
  }

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FireSafetyRecord> buildQueryWrapper(
      String keyword,
      String recordType,
      String inspectorName,
      String status,
      LocalDateTime checkTimeStart,
      LocalDateTime checkTimeEnd) {
    Long orgId = AuthContext.getOrgId();
    String normalizedType = normalizeRecordType(recordType);
    String normalizedStatus = normalizeStatus(status);
    LocalDateTime startTime = checkTimeStart;
    LocalDateTime endTime = checkTimeEnd;
    if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
      LocalDateTime temp = startTime;
      startTime = endTime;
      endTime = temp;
    }

    var wrapper = Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .eq(normalizedType != null, FireSafetyRecord::getRecordType, normalizedType)
        .like(inspectorName != null && !inspectorName.isBlank(), FireSafetyRecord::getInspectorName, inspectorName)
        .eq(normalizedStatus != null, FireSafetyRecord::getStatus, normalizedStatus)
        .ge(startTime != null, FireSafetyRecord::getCheckTime, startTime)
        .le(endTime != null, FireSafetyRecord::getCheckTime, endTime);

    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(FireSafetyRecord::getTitle, keyword)
          .or().like(FireSafetyRecord::getLocation, keyword)
          .or().like(FireSafetyRecord::getIssueDescription, keyword)
          .or().like(FireSafetyRecord::getActionTaken, keyword)
          .or().like(FireSafetyRecord::getInspectorName, keyword));
    }
    return wrapper;
  }

  private String normalizeRecordType(String recordType) {
    if (recordType == null || recordType.isBlank()) {
      return null;
    }
    String normalized = recordType.trim().toUpperCase();
    if (!ALLOWED_TYPES.contains(normalized)) {
      throw new IllegalArgumentException("recordType 不合法");
    }
    return normalized;
  }

  private String normalizeRecordTypeForWrite(String recordType) {
    String normalized = normalizeRecordType(recordType);
    if (normalized == null) {
      throw new IllegalArgumentException("recordType 不能为空");
    }
    return normalized;
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if (!ALLOWED_STATUS.contains(normalized)) {
      throw new IllegalArgumentException("status 仅支持 OPEN/CLOSED");
    }
    return normalized;
  }

  private String normalizeStatusForWrite(String status) {
    if (status == null || status.isBlank()) {
      return "OPEN";
    }
    String normalized = normalizeStatus(status);
    return normalized == null ? "OPEN" : normalized;
  }

  private boolean notBlank(String text) {
    return text != null && !text.isBlank();
  }

  private String formatDateTime(LocalDateTime value) {
    return value == null ? "" : value.format(DATETIME_FORMAT);
  }

  private String stringOf(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private ResponseEntity<byte[]> csvResponse(String filenameBase, List<String> headers, List<List<String>> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append("\uFEFF");
    sb.append(headers.stream().map(this::escapeCsv).collect(Collectors.joining(","))).append('\n');
    for (List<String> row : rows) {
      sb.append(row.stream().map(this::escapeCsv).collect(Collectors.joining(","))).append('\n');
    }
    byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
    String filename = filenameBase + "-" + LocalDate.now() + ".csv";
    responseHeaders.setContentDispositionFormData("attachment", filename);
    return ResponseEntity.ok().headers(responseHeaders).body(bytes);
  }

  private String escapeCsv(String value) {
    String text = value == null ? "" : value;
    if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
      return "\"" + text.replace("\"", "\"\"") + "\"";
    }
    return text;
  }
}
