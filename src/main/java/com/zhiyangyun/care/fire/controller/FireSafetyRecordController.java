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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasAnyRole('GUARD','LOGISTICS_EMPLOYEE','LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
  private static final Set<String> QR_ENABLED_TYPES = Set.of("FACILITY", "MONTHLY_CHECK", "DAY_PATROL", "NIGHT_PATROL");

  private static final List<String> TYPE_ORDER = List.of(
      "FACILITY",
      "CONTROL_ROOM_DUTY",
      "MONTHLY_CHECK",
      "DAY_PATROL",
      "NIGHT_PATROL",
      "MAINTENANCE_REPORT",
      "FAULT_MAINTENANCE");

  private static final Set<String> ALLOWED_STATUS = Set.of("OPEN", "RUNNING", "CLOSED");
  private static final Set<String> ACTIVE_STATUSES = Set.of("OPEN", "RUNNING");

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
    Long orgId = requireOrgId();
    var wrapper = buildQueryWrapper(orgId, keyword, recordType, inspectorName, status, checkTimeStart, checkTimeEnd);
    wrapper.orderByDesc(FireSafetyRecord::getCheckTime);
    IPage<FireSafetyRecord> page = recordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    hydrateRecords(page.getRecords());
    return Result.ok(page);
  }

  @PostMapping
  public Result<FireSafetyRecord> create(@Valid @RequestBody FireSafetyRecordRequest request) {
    Long orgId = requireOrgId();
    FireSafetyRecord record = new FireSafetyRecord();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    applyRequest(record, request);
    record.setCreatedBy(AuthContext.getStaffId());
    recordMapper.insert(record);
    hydrateRecord(record);
    return Result.ok(record);
  }

  @PutMapping("/{id}")
  public Result<FireSafetyRecord> update(@PathVariable Long id, @Valid @RequestBody FireSafetyRecordRequest request) {
    FireSafetyRecord existing = findRequiredRecord(id);
    applyRequest(existing, request);
    recordMapper.updateById(existing);
    hydrateRecord(existing);
    return Result.ok(existing);
  }

  @PutMapping("/{id}/close")
  public Result<FireSafetyRecord> close(@PathVariable Long id) {
    FireSafetyRecord existing = findRequiredRecord(id);
    existing.setStatus("CLOSED");
    existing.setUpdateTime(LocalDateTime.now());
    recordMapper.updateById(existing);
    hydrateRecord(existing);
    return Result.ok(existing);
  }

  @PostMapping("/{id}/recheck")
  public Result<FireSafetyRecord> recheck(@PathVariable Long id) {
    FireSafetyRecord existing = findRequiredRecord(id);
    if (!"FACILITY".equals(existing.getRecordType())) {
      throw new IllegalArgumentException("当前记录类型暂不支持二次检查登记");
    }
    LocalDateTime now = LocalDateTime.now();
    FireSafetyRecord record = new FireSafetyRecord();
    record.setTenantId(existing.getTenantId());
    record.setOrgId(existing.getOrgId());
    record.setRecordType(existing.getRecordType());
    record.setTitle(existing.getTitle());
    record.setLocation(existing.getLocation());
    record.setInspectorName(existing.getInspectorName());
    record.setCheckTime(now);
    record.setStatus("OPEN");
    record.setIssueDescription(null);
    record.setActionTaken(null);
    record.setNextCheckDate(resolveRecheckNextCheckDate(existing, now));
    record.setDailyReminderEnabled(Boolean.TRUE.equals(existing.getDailyReminderEnabled()));
    record.setDailyReminderTime(existing.getDailyReminderTime());
    record.setSourceRecordId(existing.getSourceRecordId() == null ? existing.getId() : existing.getSourceRecordId());
    record.setCheckRound(existing.getCheckRound() == null ? 2 : existing.getCheckRound() + 1);
    record.setDutyRecord(existing.getDutyRecord());
    record.setHandoverPunchTime(null);
    record.setEquipmentBatchNo(existing.getEquipmentBatchNo());
    record.setProductProductionDate(existing.getProductProductionDate());
    record.setProductExpiryDate(existing.getProductExpiryDate());
    record.setCheckCycleDays(existing.getCheckCycleDays());
    record.setEquipmentUpdateNote(existing.getEquipmentUpdateNote());
    record.setEquipmentAgingDisposal(existing.getEquipmentAgingDisposal());
    record.setImageUrlsText(existing.getImageUrlsText());
    record.setThirdPartyMaintenanceFileUrl(existing.getThirdPartyMaintenanceFileUrl());
    record.setPurchaseContractFileUrl(existing.getPurchaseContractFileUrl());
    record.setContractStartDate(existing.getContractStartDate());
    record.setContractEndDate(existing.getContractEndDate());
    record.setPurchaseDocumentUrlsText(existing.getPurchaseDocumentUrlsText());
    record.setCreatedBy(AuthContext.getStaffId());
    recordMapper.insert(record);
    hydrateRecord(record);
    return Result.ok(record);
  }

  @PostMapping("/{id}/qr/generate")
  public Result<FireSafetyQrResponse> generateQr(@PathVariable Long id) {
    FireSafetyRecord existing = findRequiredRecord(id);
    validateQrEnabled(existing.getRecordType());
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
    Long orgId = requireOrgId();
    String token = request.getQrToken() == null ? null : request.getQrToken().trim();
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("qrToken 不能为空");
    }
    FireSafetyRecord existing = recordMapper.selectOne(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .eq(FireSafetyRecord::getQrToken, token));
    if (existing == null) {
      return Result.error(404, "未匹配到巡查记录，请核对二维码");
    }
    validateQrEnabled(existing.getRecordType());
    existing.setScanCompletedAt(LocalDateTime.now());
    existing.setStatus("CLOSED");
    if (request.getInspectorName() != null && !request.getInspectorName().isBlank()) {
      existing.setInspectorName(request.getInspectorName().trim());
    }
    if (request.getActionTaken() != null && !request.getActionTaken().isBlank()) {
      existing.setActionTaken(request.getActionTaken().trim());
    }
    recordMapper.updateById(existing);
    hydrateRecord(existing);
    return Result.ok(existing);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    FireSafetyRecord existing = findRequiredRecord(id);
    existing.setIsDeleted(1);
    recordMapper.updateById(existing);
    return Result.ok(null);
  }

  @GetMapping("/summary")
  public Result<FireSafetyReportSummaryResponse> summary(
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateTo,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime checkTimeStart,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime checkTimeEnd,
      @RequestParam(required = false) String recordType) {
    Long orgId = requireOrgId();
    LocalDateTime[] timeRange = resolveTimeRange(dateFrom, dateTo, checkTimeStart, checkTimeEnd);
    LocalDateTime startTime = timeRange[0];
    LocalDateTime endTime = timeRange[1];
    String normalizedRecordType = normalizeRecordTypeForQuery(recordType);

    var baseWrapper = buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime);

    long totalCount = recordMapper.selectCount(baseWrapper);

    long openCount = recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .in(FireSafetyRecord::getStatus, ACTIVE_STATUSES));

    long closedCount = recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .eq(FireSafetyRecord::getStatus, "CLOSED"));

    long overdueCount = recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .in(FireSafetyRecord::getStatus, ACTIVE_STATUSES)
        .isNotNull(FireSafetyRecord::getNextCheckDate)
        .lt(FireSafetyRecord::getNextCheckDate, LocalDate.now()));

    List<Map<String, Object>> typeMapRows = recordMapper.selectMaps(Wrappers.query(FireSafetyRecord.class)
        .select("record_type as recordType", "count(1) as cnt")
        .eq("is_deleted", 0)
        .eq(orgId != null, "org_id", orgId)
        .eq(normalizedRecordType != null, "record_type", normalizedRecordType)
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
    response.setDailyCompletedCount(recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "DAY_PATROL")
        .isNotNull(FireSafetyRecord::getScanCompletedAt)));
    response.setMonthlyCompletedCount(recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "MONTHLY_CHECK")
        .isNotNull(FireSafetyRecord::getScanCompletedAt)));
    response.setDutyRecordCount(recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "CONTROL_ROOM_DUTY")
        .isNotNull(FireSafetyRecord::getDutyRecord)
        .ne(FireSafetyRecord::getDutyRecord, "")));
    response.setHandoverPunchCount(recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .isNotNull(FireSafetyRecord::getHandoverPunchTime)));
    response.setEquipmentUpdateCount(recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "FACILITY")
        .isNotNull(FireSafetyRecord::getEquipmentBatchNo)
        .ne(FireSafetyRecord::getEquipmentBatchNo, "")));
    response.setEquipmentAgingDisposalCount(recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "FACILITY")
        .isNotNull(FireSafetyRecord::getEquipmentAgingDisposal)
        .ne(FireSafetyRecord::getEquipmentAgingDisposal, "")));
    response.setExpiringSoonCount(recordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .eq(normalizedRecordType != null, FireSafetyRecord::getRecordType, normalizedRecordType)
        .isNotNull(FireSafetyRecord::getProductExpiryDate)
        .between(FireSafetyRecord::getProductExpiryDate, LocalDate.now(), LocalDate.now().plusDays(30))));
    response.setNextCheckDueSoonCount(recordMapper.selectCount(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(orgId != null, FireSafetyRecord::getOrgId, orgId)
        .eq(normalizedRecordType != null, FireSafetyRecord::getRecordType, normalizedRecordType)
        .in(FireSafetyRecord::getStatus, ACTIVE_STATUSES)
        .isNotNull(FireSafetyRecord::getNextCheckDate)
        .between(FireSafetyRecord::getNextCheckDate, LocalDate.now(), LocalDate.now().plusDays(7))));
    response.setTypeStats(typeStats);
    return Result.ok(response);
  }

  @GetMapping("/report/detail")
  public Result<FireSafetyReportDetailResponse> detail(
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) String recordType,
      @RequestParam(defaultValue = "100") int limit) {
    Long orgId = requireOrgId();
    LocalDateTime[] timeRange = resolveTimeRange(dateFrom, dateTo, null, null);
    LocalDateTime startTime = timeRange[0];
    LocalDateTime endTime = timeRange[1];
    String normalizedRecordType = normalizeRecordTypeForQuery(recordType);
    int safeLimit = Math.max(1, Math.min(limit, 1000));
    long totalCount = recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime));

    List<FireSafetyRecord> records = recordMapper.selectList(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .orderByDesc(FireSafetyRecord::getCheckTime)
        .last("limit " + safeLimit));
    hydrateRecords(records);

    FireSafetyReportDetailResponse response = new FireSafetyReportDetailResponse();
    response.setTotalCount(totalCount);
    response.setDailyCompletedCount(recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "DAY_PATROL")
        .isNotNull(FireSafetyRecord::getScanCompletedAt)));
    response.setMonthlyCompletedCount(recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "MONTHLY_CHECK")
        .isNotNull(FireSafetyRecord::getScanCompletedAt)));
    response.setDutyRecordCount(recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "CONTROL_ROOM_DUTY")
        .isNotNull(FireSafetyRecord::getDutyRecord)
        .ne(FireSafetyRecord::getDutyRecord, "")));
    response.setHandoverPunchCount(recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .isNotNull(FireSafetyRecord::getHandoverPunchTime)));
    response.setEquipmentUpdateCount(recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "FACILITY")
        .isNotNull(FireSafetyRecord::getEquipmentBatchNo)
        .ne(FireSafetyRecord::getEquipmentBatchNo, "")));
    response.setEquipmentAgingDisposalCount(recordMapper.selectCount(buildPeriodWrapper(orgId, normalizedRecordType, startTime, endTime)
        .eq(FireSafetyRecord::getRecordType, "FACILITY")
        .isNotNull(FireSafetyRecord::getEquipmentAgingDisposal)
        .ne(FireSafetyRecord::getEquipmentAgingDisposal, "")));

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
      item.setProductProductionDate(formatDate(record.getProductProductionDate()));
      item.setProductExpiryDate(formatDate(record.getProductExpiryDate()));
      item.setCheckCycleDays(record.getCheckCycleDays());
      item.setEquipmentUpdateNote(record.getEquipmentUpdateNote());
      item.setEquipmentAgingDisposal(record.getEquipmentAgingDisposal());
      item.setIssueDescription(record.getIssueDescription());
      item.setActionTaken(record.getActionTaken());
      item.setImageUrls(record.getImageUrls() == null ? new ArrayList<>() : new ArrayList<>(record.getImageUrls()));
      item.setThirdPartyMaintenanceFileUrl(record.getThirdPartyMaintenanceFileUrl());
      item.setPurchaseContractFileUrl(record.getPurchaseContractFileUrl());
      item.setContractStartDate(formatDate(record.getContractStartDate()));
      item.setContractEndDate(formatDate(record.getContractEndDate()));
      item.setPurchaseDocumentUrls(record.getPurchaseDocumentUrls() == null ? new ArrayList<>() : new ArrayList<>(record.getPurchaseDocumentUrls()));
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
    Long orgId = requireOrgId();
    LocalDateTime[] timeRange = resolveTimeRange(dateFrom, dateTo, null, null);
    List<FireSafetyRecord> records = recordMapper.selectList(buildPeriodWrapper(orgId, null, timeRange[0], timeRange[1])
        .orderByDesc(FireSafetyRecord::getCheckTime));
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
        "产品生产日期",
        "产品过期日期",
        "检查周期(天)",
        "设备更新记录",
        "设备老化处置",
        "第三方维保记录单",
        "采购合同",
        "合约开始日期",
        "合约结束日期",
        "采购单据",
        "问题描述",
        "处置措施");
    List<List<String>> rows = records.stream().map(item -> List.of(
        stringOf(item.getId()),
        stringOf(item.getRecordType()),
        stringOf(item.getTitle()),
        stringOf(item.getLocation()),
        stringOf(item.getInspectorName()),
        formatDateTime(item.getCheckTime()),
        formatDateTime(item.getScanCompletedAt()),
        stringOf(item.getStatus()),
        stringOf(item.getDutyRecord()),
        formatDateTime(item.getHandoverPunchTime()),
        stringOf(item.getEquipmentBatchNo()),
        formatDate(item.getProductProductionDate()),
        formatDate(item.getProductExpiryDate()),
        stringOf(item.getCheckCycleDays()),
        stringOf(item.getEquipmentUpdateNote()),
        stringOf(item.getEquipmentAgingDisposal()),
        stringOf(item.getThirdPartyMaintenanceFileUrl()),
        stringOf(item.getPurchaseContractFileUrl()),
        formatDate(item.getContractStartDate()),
        formatDate(item.getContractEndDate()),
        stringOf(joinLines(item.getPurchaseDocumentUrls())),
        stringOf(item.getIssueDescription()),
        stringOf(item.getActionTaken()))).toList();
    return csvResponse("fire-safety-report", headers, rows);
  }

  @GetMapping(value = "/maintenance/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportMaintenanceLog(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String inspectorName,
      @RequestParam(required = false) String status,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime checkTimeStart,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime checkTimeEnd) {
    Long orgId = requireOrgId();
    var wrapper = buildQueryWrapper(orgId, keyword, "MAINTENANCE_REPORT", inspectorName, status, checkTimeStart, checkTimeEnd);
    wrapper.orderByDesc(FireSafetyRecord::getCheckTime).last("limit 5000");
    List<FireSafetyRecord> records = recordMapper.selectList(wrapper);
    List<String> headers = List.of(
        "记录ID",
        "标题",
        "区域",
        "负责人",
        "检查时间",
        "设备批号",
        "产品生产日期",
        "产品过期日期",
        "检查周期(天)",
        "第三方维保记录单",
        "采购合同",
        "合约开始日期",
        "合约结束日期",
        "采购单据",
        "状态",
        "问题描述",
        "处置措施");
    List<List<String>> rows = records.stream().map(item -> List.of(
        stringOf(item.getId()),
        stringOf(item.getTitle()),
        stringOf(item.getLocation()),
        stringOf(item.getInspectorName()),
        formatDateTime(item.getCheckTime()),
        stringOf(item.getEquipmentBatchNo()),
        formatDate(item.getProductProductionDate()),
        formatDate(item.getProductExpiryDate()),
        stringOf(item.getCheckCycleDays()),
        stringOf(item.getThirdPartyMaintenanceFileUrl()),
        stringOf(item.getPurchaseContractFileUrl()),
        formatDate(item.getContractStartDate()),
        formatDate(item.getContractEndDate()),
        stringOf(joinLines(item.getPurchaseDocumentUrls())),
        stringOf(item.getStatus()),
        stringOf(item.getIssueDescription()),
        stringOf(item.getActionTaken()))).toList();
    return csvResponse("fire-maintenance-log", headers, rows);
  }

  private FireSafetyRecord findAccessibleRecord(Long orgId, Long id) {
    return recordMapper.selectOne(Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getId, id)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(FireSafetyRecord::getOrgId, orgId));
  }

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FireSafetyRecord> buildQueryWrapper(
      Long orgId,
      String keyword,
      String recordType,
      String inspectorName,
      String status,
      LocalDateTime checkTimeStart,
      LocalDateTime checkTimeEnd) {
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
        .eq(FireSafetyRecord::getOrgId, orgId)
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

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FireSafetyRecord> buildPeriodWrapper(
      Long orgId,
      String recordType,
      LocalDateTime startTime,
      LocalDateTime endTime) {
    return Wrappers.lambdaQuery(FireSafetyRecord.class)
        .eq(FireSafetyRecord::getIsDeleted, 0)
        .eq(FireSafetyRecord::getOrgId, orgId)
        .eq(recordType != null, FireSafetyRecord::getRecordType, recordType)
        .ge(startTime != null, FireSafetyRecord::getCheckTime, startTime)
        .le(endTime != null, FireSafetyRecord::getCheckTime, endTime);
  }

  private void applyRequest(FireSafetyRecord record, FireSafetyRecordRequest request) {
    validateRequest(request);
    record.setRecordType(normalizeRecordTypeForWrite(request.getRecordType()));
    record.setTitle(request.getTitle().trim());
    record.setLocation(trimToNull(request.getLocation()));
    record.setInspectorName(request.getInspectorName().trim());
    record.setCheckTime(request.getCheckTime());
    record.setStatus(normalizeStatusForWrite(request.getStatus()));
    record.setIssueDescription(trimToNull(request.getIssueDescription()));
    record.setActionTaken(trimToNull(request.getActionTaken()));
    record.setNextCheckDate(request.getNextCheckDate());
    record.setDailyReminderEnabled(Boolean.TRUE.equals(request.getDailyReminderEnabled()));
    record.setDailyReminderTime(Boolean.TRUE.equals(request.getDailyReminderEnabled()) ? request.getDailyReminderTime() : null);
    record.setDutyRecord(trimToNull(request.getDutyRecord()));
    record.setHandoverPunchTime(request.getHandoverPunchTime());
    record.setEquipmentBatchNo(trimToNull(request.getEquipmentBatchNo()));
    record.setProductProductionDate(request.getProductProductionDate());
    record.setProductExpiryDate(request.getProductExpiryDate());
    record.setCheckCycleDays(request.getCheckCycleDays());
    record.setEquipmentUpdateNote(trimToNull(request.getEquipmentUpdateNote()));
    record.setEquipmentAgingDisposal(trimToNull(request.getEquipmentAgingDisposal()));
    record.setImageUrlsText(joinImageUrls(request.getImageUrls()));
    record.setThirdPartyMaintenanceFileUrl(trimToNull(request.getThirdPartyMaintenanceFileUrl()));
    record.setPurchaseContractFileUrl(trimToNull(request.getPurchaseContractFileUrl()));
    record.setContractStartDate(request.getContractStartDate());
    record.setContractEndDate(request.getContractEndDate());
    record.setPurchaseDocumentUrlsText(joinImageUrls(request.getPurchaseDocumentUrls()));
  }

  private void validateRequest(FireSafetyRecordRequest request) {
    if (request.getCheckCycleDays() != null && request.getCheckCycleDays() <= 0) {
      throw new IllegalArgumentException("检查周期必须大于 0");
    }
    if (request.getProductProductionDate() != null
        && request.getProductExpiryDate() != null
        && request.getProductExpiryDate().isBefore(request.getProductProductionDate())) {
      throw new IllegalArgumentException("产品过期日期不能早于生产日期");
    }
    if (request.getNextCheckDate() != null
        && request.getCheckTime() != null
        && request.getNextCheckDate().isBefore(request.getCheckTime().toLocalDate())) {
      throw new IllegalArgumentException("下次检查日期不能早于检查日期");
    }
    if (Boolean.TRUE.equals(request.getDailyReminderEnabled()) && request.getDailyReminderTime() == null) {
      throw new IllegalArgumentException("开启每日提醒后必须设置提醒时间");
    }
    if (request.getContractStartDate() != null
        && request.getContractEndDate() != null
        && request.getContractEndDate().isBefore(request.getContractStartDate())) {
      throw new IllegalArgumentException("合约结束日期不能早于开始日期");
    }
  }

  private LocalDate resolveRecheckNextCheckDate(FireSafetyRecord existing, LocalDateTime now) {
    if (existing.getCheckCycleDays() != null && existing.getCheckCycleDays() > 0) {
      return now.toLocalDate().plusDays(existing.getCheckCycleDays());
    }
    LocalDate nextCheckDate = existing.getNextCheckDate();
    if (nextCheckDate != null && nextCheckDate.isBefore(now.toLocalDate())) {
      return now.toLocalDate();
    }
    return nextCheckDate;
  }

  private FireSafetyRecord findRequiredRecord(Long id) {
    FireSafetyRecord existing = findAccessibleRecord(requireOrgId(), id);
    if (existing == null) {
      throw new IllegalArgumentException("消防记录不存在或无权限访问");
    }
    return existing;
  }

  private Long requireOrgId() {
    Long orgId = AuthContext.getOrgId();
    if (orgId == null) {
      throw new IllegalArgumentException("当前账号缺少机构信息，请重新登录后重试");
    }
    return orgId;
  }

  private LocalDateTime[] resolveTimeRange(
      LocalDate dateFrom,
      LocalDate dateTo,
      LocalDateTime checkTimeStart,
      LocalDateTime checkTimeEnd) {
    LocalDateTime startTime = checkTimeStart;
    LocalDateTime endTime = checkTimeEnd;
    if (startTime == null && dateFrom != null) {
      startTime = dateFrom.atStartOfDay();
    }
    if (endTime == null && dateTo != null) {
      endTime = dateTo.plusDays(1).atStartOfDay().minusNanos(1);
    }
    if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
      LocalDateTime temp = startTime;
      startTime = endTime;
      endTime = temp;
    }
    return new LocalDateTime[] {startTime, endTime};
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private void validateQrEnabled(String recordType) {
    if (!QR_ENABLED_TYPES.contains(recordType)) {
      throw new IllegalArgumentException("当前记录类型不支持二维码巡查闭环");
    }
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

  private String normalizeRecordTypeForQuery(String recordType) {
    return normalizeRecordType(recordType);
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
      throw new IllegalArgumentException("status 仅支持 OPEN/RUNNING/CLOSED");
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

  private void hydrateRecords(List<FireSafetyRecord> records) {
    if (records == null || records.isEmpty()) {
      return;
    }
    records.forEach(this::hydrateRecord);
  }

  private void hydrateRecord(FireSafetyRecord record) {
    if (record == null) {
      return;
    }
    record.setImageUrls(splitImageUrls(record.getImageUrlsText()));
    record.setPurchaseDocumentUrls(splitImageUrls(record.getPurchaseDocumentUrlsText()));
    if (record.getCheckRound() == null || record.getCheckRound() <= 0) {
      record.setCheckRound(1);
    }
  }

  private String joinLines(List<String> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }
    return values.stream().filter(item -> item != null && !item.isBlank()).collect(Collectors.joining("\n"));
  }

  private String joinImageUrls(List<String> imageUrls) {
    if (imageUrls == null || imageUrls.isEmpty()) {
      return null;
    }
    List<String> normalized = imageUrls.stream()
        .map(this::trimToNull)
        .filter(item -> item != null)
        .distinct()
        .limit(9)
        .toList();
    if (normalized.isEmpty()) {
      return null;
    }
    return String.join("\n", normalized);
  }

  private List<String> splitImageUrls(String imageUrlsText) {
    if (imageUrlsText == null || imageUrlsText.isBlank()) {
      return Collections.emptyList();
    }
    return Arrays.stream(imageUrlsText.split("\\R+"))
        .map(this::trimToNull)
        .filter(item -> item != null)
        .distinct()
        .toList();
  }

  private boolean notBlank(String text) {
    return text != null && !text.isBlank();
  }

  private String formatDateTime(LocalDateTime value) {
    return value == null ? "" : value.format(DATETIME_FORMAT);
  }

  private String formatDate(LocalDate value) {
    return value == null ? "" : value.toString();
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
