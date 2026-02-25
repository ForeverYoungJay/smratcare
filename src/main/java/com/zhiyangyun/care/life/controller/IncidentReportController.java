package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.life.entity.IncidentReport;
import com.zhiyangyun.care.life.mapper.IncidentReportMapper;
import com.zhiyangyun.care.life.model.IncidentReportRequest;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/life/incident")
public class IncidentReportController {
  private final IncidentReportMapper incidentMapper;
  private final ElderMapper elderMapper;

  public IncidentReportController(IncidentReportMapper incidentMapper, ElderMapper elderMapper) {
    this.incidentMapper = incidentMapper;
    this.elderMapper = elderMapper;
  }

  @GetMapping("/page")
  public Result<IPage<IncidentReport>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String elderName,
      @RequestParam(required = false) String incidentType,
      @RequestParam(required = false) String reporterName,
      @RequestParam(required = false) String level,
      @RequestParam(required = false) String status,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime incidentTimeStart,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime incidentTimeEnd) {
    var wrapper = buildQueryWrapper(keyword, elderName, incidentType, reporterName, level, status, incidentTimeStart, incidentTimeEnd);
    wrapper.orderByDesc(IncidentReport::getIncidentTime);
    return Result.ok(incidentMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String elderName,
      @RequestParam(required = false) String incidentType,
      @RequestParam(required = false) String reporterName,
      @RequestParam(required = false) String level,
      @RequestParam(required = false) String status,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime incidentTimeStart,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      @RequestParam(required = false) LocalDateTime incidentTimeEnd) {
    var wrapper = buildQueryWrapper(keyword, elderName, incidentType, reporterName, level, status, incidentTimeStart, incidentTimeEnd);
    wrapper.orderByDesc(IncidentReport::getIncidentTime);
    List<IncidentReport> records = incidentMapper.selectList(wrapper);
    List<String> headers = List.of("老人姓名", "事故类型", "事故等级", "值班护工", "事故描述", "处理措施", "发生时间", "处理状态");
    List<List<String>> rows = records.stream()
        .map(item -> List.of(
            safe(item.getElderName()),
            safe(item.getIncidentType()),
            "MAJOR".equals(item.getLevel()) ? "重大" : "一般",
            safe(item.getReporterName()),
            safe(item.getDescription()),
            safe(item.getActionTaken()),
            safe(item.getIncidentTime()),
            "CLOSED".equals(item.getStatus()) ? "已关闭" : "处理中"))
        .toList();
    return csvResponse("incident-report", headers, rows);
  }

  @PostMapping
  public Result<IncidentReport> create(@Valid @RequestBody IncidentReportRequest request) {
    Long orgId = AuthContext.getOrgId();
    IncidentReport report = new IncidentReport();
    report.setTenantId(orgId);
    report.setOrgId(orgId);
    report.setElderId(request.getElderId());
    report.setElderName(resolveElderName(request.getElderId(), request.getElderName()));
    report.setReporterName(request.getReporterName());
    report.setIncidentTime(request.getIncidentTime());
    report.setIncidentType(request.getIncidentType());
    report.setLevel(normalizeIncidentLevelForWrite(request.getLevel()));
    report.setDescription(request.getDescription());
    report.setActionTaken(request.getActionTaken());
    report.setStatus(normalizeIncidentStatusForWrite(request.getStatus()));
    report.setCreatedBy(AuthContext.getStaffId());
    incidentMapper.insert(report);
    return Result.ok(report);
  }

  @PutMapping("/{id}")
  public Result<IncidentReport> update(@PathVariable Long id, @Valid @RequestBody IncidentReportRequest request) {
    IncidentReport existing = incidentMapper.selectById(id);
    if (existing == null) {
      return Result.ok(null);
    }
    existing.setElderId(request.getElderId());
    existing.setElderName(resolveElderName(request.getElderId(), request.getElderName()));
    existing.setReporterName(request.getReporterName());
    existing.setIncidentTime(request.getIncidentTime());
    existing.setIncidentType(request.getIncidentType());
    existing.setLevel(normalizeIncidentLevelForWrite(request.getLevel()));
    existing.setDescription(request.getDescription());
    existing.setActionTaken(request.getActionTaken());
    existing.setStatus(normalizeIncidentStatusForWrite(request.getStatus()));
    incidentMapper.updateById(existing);
    return Result.ok(existing);
  }

  @PutMapping("/{id}/close")
  public Result<IncidentReport> close(@PathVariable Long id) {
    IncidentReport existing = incidentMapper.selectById(id);
    if (existing == null) {
      return Result.ok(null);
    }
    existing.setStatus("CLOSED");
    existing.setUpdateTime(LocalDateTime.now());
    incidentMapper.updateById(existing);
    return Result.ok(existing);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    IncidentReport existing = incidentMapper.selectById(id);
    if (existing != null) {
      existing.setIsDeleted(1);
      incidentMapper.updateById(existing);
    }
    return Result.ok(null);
  }

  private String resolveElderName(Long elderId, String fallback) {
    if (fallback != null && !fallback.isBlank()) {
      return fallback;
    }
    if (elderId == null) {
      return null;
    }
    ElderProfile elder = elderMapper.selectById(elderId);
    return elder == null ? null : elder.getFullName();
  }

  private String normalizeIncidentLevel(String level) {
    if (level == null || level.isBlank()) {
      return null;
    }
    String normalized = level.trim().toUpperCase();
    if (!"NORMAL".equals(normalized) && !"MAJOR".equals(normalized)) {
      throw new IllegalArgumentException("事故等级仅支持 NORMAL/MAJOR");
    }
    return normalized;
  }

  private String normalizeIncidentStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if (!"OPEN".equals(normalized) && !"CLOSED".equals(normalized)) {
      throw new IllegalArgumentException("事故状态仅支持 OPEN/CLOSED");
    }
    return normalized;
  }

  private String normalizeIncidentLevelForWrite(String level) {
    if (level == null || level.isBlank()) {
      return "NORMAL";
    }
    String normalized = normalizeIncidentLevel(level);
    return normalized == null ? "NORMAL" : normalized;
  }

  private String normalizeIncidentStatusForWrite(String status) {
    if (status == null || status.isBlank()) {
      return "OPEN";
    }
    String normalized = normalizeIncidentStatus(status);
    return normalized == null ? "OPEN" : normalized;
  }

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<IncidentReport> buildQueryWrapper(
      String keyword,
      String elderName,
      String incidentType,
      String reporterName,
      String level,
      String status,
      LocalDateTime incidentTimeStart,
      LocalDateTime incidentTimeEnd) {
    Long orgId = AuthContext.getOrgId();
    String normalizedLevel = normalizeIncidentLevel(level);
    String normalizedStatus = normalizeIncidentStatus(status);
    LocalDateTime startTime = incidentTimeStart;
    LocalDateTime endTime = incidentTimeEnd;
    if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
      LocalDateTime temp = startTime;
      startTime = endTime;
      endTime = temp;
    }
    var wrapper = Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .like(elderName != null && !elderName.isBlank(), IncidentReport::getElderName, elderName)
        .like(incidentType != null && !incidentType.isBlank(), IncidentReport::getIncidentType, incidentType)
        .like(reporterName != null && !reporterName.isBlank(), IncidentReport::getReporterName, reporterName)
        .eq(normalizedLevel != null, IncidentReport::getLevel, normalizedLevel)
        .eq(normalizedStatus != null, IncidentReport::getStatus, normalizedStatus)
        .ge(startTime != null, IncidentReport::getIncidentTime, startTime)
        .le(endTime != null, IncidentReport::getIncidentTime, endTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(IncidentReport::getElderName, keyword)
          .or().like(IncidentReport::getIncidentType, keyword)
          .or().like(IncidentReport::getReporterName, keyword));
    }
    return wrapper;
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
