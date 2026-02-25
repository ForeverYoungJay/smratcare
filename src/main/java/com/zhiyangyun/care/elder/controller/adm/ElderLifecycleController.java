package com.zhiyangyun.care.elder.controller.adm;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionRequest;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionRecordResponse;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionResponse;
import com.zhiyangyun.care.elder.model.lifecycle.ChangeLogResponse;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeResponse;
import com.zhiyangyun.care.elder.service.lifecycle.ElderLifecycleService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.net.URLEncoder;

@RestController
@RequestMapping("/api/elder/lifecycle")
public class ElderLifecycleController {
  private final ElderLifecycleService lifecycleService;
  private final AuditLogService auditLogService;

  public ElderLifecycleController(ElderLifecycleService lifecycleService, AuditLogService auditLogService) {
    this.lifecycleService = lifecycleService;
    this.auditLogService = auditLogService;
  }

  @PostMapping("/admit")
  public Result<AdmissionResponse> admit(@Valid @RequestBody AdmissionRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    AdmissionResponse response = lifecycleService.admit(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "ADMIT", "ELDER", request.getElderId(), "入院办理");
    return Result.ok(response);
  }

  @PostMapping("/discharge")
  public Result<DischargeResponse> discharge(@Valid @RequestBody DischargeRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    DischargeResponse response = lifecycleService.discharge(request);
    auditLogService.record(tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DISCHARGE", "ELDER", request.getElderId(), "退院办理");
    return Result.ok(response);
  }

  @GetMapping("/changes")
  public Result<IPage<ChangeLogResponse>> changes(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String changeType,
      @RequestParam(required = false) String reason,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime startTime,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime endTime) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(lifecycleService.changeLogs(tenantId, pageNo, pageSize, elderId, changeType, reason, startTime, endTime));
  }

  @GetMapping("/admissions/page")
  public Result<IPage<AdmissionRecordResponse>> admissions(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String contractNo,
      @RequestParam(required = false) Integer elderStatus,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate admissionDateStart,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate admissionDateEnd) {
    Long tenantId = AuthContext.getOrgId();
    return Result.ok(lifecycleService.admissionPage(
        tenantId, pageNo, pageSize, keyword, contractNo, elderStatus, admissionDateStart, admissionDateEnd));
  }

  @GetMapping(value = "/changes/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportChanges(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String changeType,
      @RequestParam(required = false) String reason,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime startTime,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime endTime) {
    Long tenantId = AuthContext.getOrgId();
    List<ChangeLogResponse> records = loadAllChangeLogs(tenantId, elderId, changeType, reason, startTime, endTime);
    List<String> headers = List.of("老人姓名", "变更类型", "变更前", "变更后", "操作说明", "变更时间");
    List<List<String>> rows = records.stream()
        .map(item -> List.of(
            safe(item.getElderName()),
            safe(item.getChangeType()),
            safe(item.getBeforeValue()),
            safe(item.getAfterValue()),
            safe(item.getReason()),
            safe(item.getCreateTime())))
        .toList();
    return csvResponse("elder-change-logs", headers, rows);
  }

  @GetMapping(value = "/admissions/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportAdmissions(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String contractNo,
      @RequestParam(required = false) Integer elderStatus,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate admissionDateStart,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate admissionDateEnd) {
    Long tenantId = AuthContext.getOrgId();
    List<AdmissionRecordResponse> records = loadAllAdmissions(
        tenantId, keyword, contractNo, elderStatus, admissionDateStart, admissionDateEnd);
    List<String> headers = List.of("老人姓名", "合同号", "收费开始日期", "押金（元）", "入住状态", "备注", "登记时间");
    List<List<String>> rows = records.stream()
        .map(item -> List.of(
            safe(item.getElderName()),
            safe(item.getContractNo()),
            safe(item.getAdmissionDate()),
            safe(item.getDepositAmount()),
            statusText(item.getElderStatus()),
            safe(item.getRemark()),
            safe(item.getCreateTime())))
        .toList();
    return csvResponse("elder-admissions", headers, rows);
  }

  private List<ChangeLogResponse> loadAllChangeLogs(
      Long tenantId, Long elderId, String changeType, String reason, LocalDateTime startTime, LocalDateTime endTime) {
    long pageNo = 1;
    long pageSize = 500;
    List<ChangeLogResponse> all = new ArrayList<>();
    while (true) {
      IPage<ChangeLogResponse> page = lifecycleService.changeLogs(
          tenantId, pageNo, pageSize, elderId, changeType, reason, startTime, endTime);
      List<ChangeLogResponse> records = page.getRecords();
      if (records == null || records.isEmpty()) {
        break;
      }
      all.addAll(records);
      if (page.getCurrent() * page.getSize() >= page.getTotal()) {
        break;
      }
      pageNo++;
    }
    return all;
  }

  private List<AdmissionRecordResponse> loadAllAdmissions(
      Long tenantId,
      String keyword,
      String contractNo,
      Integer elderStatus,
      LocalDate admissionDateStart,
      LocalDate admissionDateEnd) {
    long pageNo = 1;
    long pageSize = 500;
    List<AdmissionRecordResponse> all = new ArrayList<>();
    while (true) {
      IPage<AdmissionRecordResponse> page = lifecycleService.admissionPage(
          tenantId, pageNo, pageSize, keyword, contractNo, elderStatus, admissionDateStart, admissionDateEnd);
      List<AdmissionRecordResponse> records = page.getRecords();
      if (records == null || records.isEmpty()) {
        break;
      }
      all.addAll(records);
      if (page.getCurrent() * page.getSize() >= page.getTotal()) {
        break;
      }
      pageNo++;
    }
    return all;
  }

  private String statusText(Integer status) {
    if (status == null) {
      return "";
    }
    if (status == 1) {
      return "在院";
    }
    if (status == 2) {
      return "请假";
    }
    if (status == 3) {
      return "离院";
    }
    return String.valueOf(status);
  }

  private String safe(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private ResponseEntity<byte[]> csvResponse(String filenameBase, List<String> headers, List<List<String>> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append('\uFEFF');
    sb.append(toCsvLine(headers)).append('\n');
    for (List<String> row : rows) {
      sb.append(toCsvLine(row)).append('\n');
    }
    byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    String filename = filenameBase + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
        .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
        .contentLength(bytes.length)
        .body(bytes);
  }

  private String toCsvLine(List<String> fields) {
    List<String> escaped = new ArrayList<>();
    for (String field : fields) {
      String value = field == null ? "" : field;
      value = value.replace("\"", "\"\"");
      if (value.contains(",") || value.contains("\n") || value.contains("\r") || value.contains("\"")) {
        value = "\"" + value + "\"";
      }
      escaped.add(value);
    }
    return String.join(",", escaped);
  }
}
