package com.zhiyangyun.care.medicalcare.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.mapper.HealthInspectionMapper;
import com.zhiyangyun.care.medicalcare.entity.MedicalCvdRiskAssessment;
import com.zhiyangyun.care.medicalcare.mapper.MedicalCvdRiskAssessmentMapper;
import com.zhiyangyun.care.medicalcare.model.MedicalAiGenerateTaskResponse;
import com.zhiyangyun.care.medicalcare.model.MedicalAiHealthReportRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalAiHealthReportResponse;
import com.zhiyangyun.care.oa.entity.OaDocument;
import com.zhiyangyun.care.oa.mapper.OaDocumentMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medical-care/ai-reports")
public class MedicalAiHealthReportController {
  private static final String FOLDER = "AI_HEALTH_REPORT";
  private final OaDocumentMapper oaDocumentMapper;
  private final MedicalCvdRiskAssessmentMapper cvdRiskAssessmentMapper;
  private final HealthInspectionMapper inspectionMapper;
  private final ObjectMapper objectMapper;

  public MedicalAiHealthReportController(
      OaDocumentMapper oaDocumentMapper,
      MedicalCvdRiskAssessmentMapper cvdRiskAssessmentMapper,
      HealthInspectionMapper inspectionMapper,
      ObjectMapper objectMapper) {
    this.oaDocumentMapper = oaDocumentMapper;
    this.cvdRiskAssessmentMapper = cvdRiskAssessmentMapper;
    this.inspectionMapper = inspectionMapper;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/page")
  public Result<IPage<MedicalAiHealthReportResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    LocalDate from = parseDate(dateFrom);
    LocalDate to = parseDate(dateTo);
    List<OaDocument> source = oaDocumentMapper.selectList(Wrappers.lambdaQuery(OaDocument.class)
        .eq(OaDocument::getIsDeleted, 0)
        .eq(orgId != null, OaDocument::getOrgId, orgId)
        .eq(OaDocument::getFolder, FOLDER)
        .orderByDesc(OaDocument::getUploadedAt));
    List<MedicalAiHealthReportResponse> records = source.stream()
        .map(this::toResponse)
        .filter(item -> type == null || type.isBlank() || type.equalsIgnoreCase(item.getType()))
        .filter(item -> status == null || status.isBlank() || status.equalsIgnoreCase(item.getStatus()))
        .filter(item -> from == null || item.getDateTo() == null || !item.getDateTo().isBefore(from))
        .filter(item -> to == null || item.getDateFrom() == null || !item.getDateFrom().isAfter(to))
        .toList();
    long total = records.size();
    int start = (int) Math.max(0, (pageNo - 1) * pageSize);
    int end = (int) Math.min(total, start + pageSize);
    List<MedicalAiHealthReportResponse> pageList = start >= end ? List.of() : records.subList(start, end);
    IPage<MedicalAiHealthReportResponse> page = new Page<>(pageNo, pageSize, total);
    page.setRecords(pageList);
    return Result.ok(page);
  }

  @PostMapping("/generate")
  public Result<MedicalAiHealthReportResponse> generate(@RequestBody MedicalAiHealthReportRequest request) {
    Long orgId = AuthContext.getOrgId();
    LocalDate to = request == null || request.getDateTo() == null ? LocalDate.now() : request.getDateTo();
    LocalDate from = request == null || request.getDateFrom() == null ? to.minusDays(6) : request.getDateFrom();
    String type = request == null || request.getType() == null || request.getType().isBlank()
        ? "WEEKLY" : request.getType().toUpperCase(Locale.ROOT);

    long highRiskCount = cvdRiskAssessmentMapper.selectCount(Wrappers.lambdaQuery(MedicalCvdRiskAssessment.class)
        .eq(MedicalCvdRiskAssessment::getIsDeleted, 0)
        .eq(orgId != null, MedicalCvdRiskAssessment::getOrgId, orgId)
        .ge(MedicalCvdRiskAssessment::getAssessmentDate, from)
        .le(MedicalCvdRiskAssessment::getAssessmentDate, to)
        .in(MedicalCvdRiskAssessment::getRiskLevel, "HIGH", "VERY_HIGH"));
    long abnormalInspection = inspectionMapper.selectCount(Wrappers.lambdaQuery(HealthInspection.class)
        .eq(HealthInspection::getIsDeleted, 0)
        .eq(orgId != null, HealthInspection::getOrgId, orgId)
        .ge(HealthInspection::getInspectionDate, from)
        .le(HealthInspection::getInspectionDate, to)
        .in(HealthInspection::getStatus, "ABNORMAL", "FOLLOWING"));

    Map<String, Object> ext = new HashMap<>();
    ext.put("type", type);
    ext.put("status", "GENERATED");
    ext.put("dateFrom", from.toString());
    ext.put("dateTo", to.toString());
    ext.put("highRiskCount", highRiskCount + abnormalInspection);

    OaDocument doc = new OaDocument();
    doc.setTenantId(orgId);
    doc.setOrgId(orgId);
    doc.setName("AI健康评估报告-" + type + "-" + DateTimeFormatter.BASIC_ISO_DATE.format(to));
    doc.setFolder(FOLDER);
    doc.setUploaderId(AuthContext.getStaffId());
    doc.setUploaderName(AuthContext.getUsername());
    doc.setUploadedAt(LocalDateTime.now());
    doc.setRemark(writeJson(ext));
    doc.setCreatedBy(AuthContext.getStaffId());
    oaDocumentMapper.insert(doc);

    return Result.ok(toResponse(doc));
  }

  @PostMapping("/{id}/publish")
  public Result<MedicalAiHealthReportResponse> publish(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    OaDocument doc = oaDocumentMapper.selectById(id);
    if (doc == null || (orgId != null && !orgId.equals(doc.getOrgId())) || !FOLDER.equals(doc.getFolder())) {
      return Result.error(404, "report not found");
    }
    Map<String, Object> ext = parseJson(doc.getRemark());
    ext.put("status", "PUBLISHED");
    doc.setRemark(writeJson(ext));
    oaDocumentMapper.updateById(doc);
    return Result.ok(toResponse(doc));
  }

  @PostMapping("/{id}/generate-tasks")
  public Result<MedicalAiGenerateTaskResponse> generateTasks(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    OaDocument doc = oaDocumentMapper.selectById(id);
    if (doc == null || (orgId != null && !orgId.equals(doc.getOrgId())) || !FOLDER.equals(doc.getFolder())) {
      return Result.error(404, "report not found");
    }
    MedicalAiGenerateTaskResponse response = new MedicalAiGenerateTaskResponse();
    response.setInspectionRoute("/medical-care/inspection?filter=generated_from_ai&reportId=" + id);
    response.setMedicalTodoRoute("/medical-care/center?filter=generated_from_ai&reportId=" + id);
    response.setNursingTaskRoute("/medical-care/care-task-board?filter=generated_from_ai&reportId=" + id);
    return Result.ok(response);
  }

  private MedicalAiHealthReportResponse toResponse(OaDocument doc) {
    Map<String, Object> ext = parseJson(doc == null ? null : doc.getRemark());
    MedicalAiHealthReportResponse response = new MedicalAiHealthReportResponse();
    response.setId(doc == null ? null : doc.getId());
    String type = asString(ext.get("type"));
    response.setType(type == null || type.isBlank() ? "WEEKLY" : type.toUpperCase(Locale.ROOT));
    String status = asString(ext.get("status"));
    response.setStatus(status == null || status.isBlank() ? "GENERATED" : status.toUpperCase(Locale.ROOT));
    response.setDateFrom(parseDate(asString(ext.get("dateFrom"))));
    response.setDateTo(parseDate(asString(ext.get("dateTo"))));
    Long highRiskCount = toLong(ext.get("highRiskCount"));
    response.setHighRiskCount(highRiskCount);
    response.setCreatedAt(doc == null ? null : doc.getUploadedAt());
    response.setRangeText((response.getDateFrom() == null ? "-" : response.getDateFrom().toString())
        + " ~ "
        + (response.getDateTo() == null ? "-" : response.getDateTo().toString()));
    return response;
  }

  private LocalDate parseDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(value);
    } catch (Exception ex) {
      return null;
    }
  }

  private Map<String, Object> parseJson(String payload) {
    if (payload == null || payload.isBlank()) {
      return new HashMap<>();
    }
    try {
      return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
    } catch (Exception ex) {
      return new HashMap<>();
    }
  }

  private String writeJson(Map<String, Object> payload) {
    try {
      return objectMapper.writeValueAsString(payload == null ? new HashMap<>() : payload);
    } catch (Exception ex) {
      return "{}";
    }
  }

  private String asString(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  private Long toLong(Object value) {
    if (value == null) {
      return 0L;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    try {
      return Long.parseLong(String.valueOf(value));
    } catch (Exception ex) {
      return 0L;
    }
  }
}
