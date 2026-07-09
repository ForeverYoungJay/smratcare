package com.zhiyangyun.care.elder.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.assessment.entity.AssessmentRecord;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderChangeLog;
import com.zhiyangyun.care.elder.model.AssignBedRequest;
import com.zhiyangyun.care.elder.model.ElderCareLevelAdjustmentRequest;
import com.zhiyangyun.care.elder.model.ElderCareProfileResponse;
import com.zhiyangyun.care.elder.model.ElderCreateRequest;
import com.zhiyangyun.care.elder.model.ElderProfileChangeApprovalRequest;
import com.zhiyangyun.care.elder.model.ElderResponse;
import com.zhiyangyun.care.elder.model.ElderUpdateRequest;
import com.zhiyangyun.care.elder.model.UnbindBedRequest;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderChangeLogMapper;
import com.zhiyangyun.care.elder.service.ElderService;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
@RequestMapping("/api/elder")
public class ElderController {
  private final ElderService elderService;
  private final com.zhiyangyun.care.family.support.FamilyNoticePublisher familyNoticePublisher;
  private final AuditLogService auditLogService;
  private final ElderMapper elderMapper;
  private final OaApprovalMapper oaApprovalMapper;
  private final AssessmentRecordMapper assessmentRecordMapper;
  private final ElderChangeLogMapper elderChangeLogMapper;

  public ElderController(
      ElderService elderService,
      AuditLogService auditLogService,
      ElderMapper elderMapper,
      OaApprovalMapper oaApprovalMapper,
      AssessmentRecordMapper assessmentRecordMapper,
      ElderChangeLogMapper elderChangeLogMapper,
      com.zhiyangyun.care.family.support.FamilyNoticePublisher familyNoticePublisher) {
    this.elderService = elderService;
    this.auditLogService = auditLogService;
    this.elderMapper = elderMapper;
    this.oaApprovalMapper = oaApprovalMapper;
    this.assessmentRecordMapper = assessmentRecordMapper;
    this.elderChangeLogMapper = elderChangeLogMapper;
    this.familyNoticePublisher = familyNoticePublisher;
  }

  @PreAuthorize("@elderAuthz.canWriteElder()")
  @PostMapping
  public Result<ElderResponse> create(@Valid @RequestBody ElderCreateRequest request) {
    Long tenantId = AuthContext.getOrgId();
    request.setTenantId(tenantId);
    request.setOrgId(tenantId);
    request.setCreatedBy(AuthContext.getStaffId());
    ElderResponse response = elderService.create(request);
    auditLogService.recordStructured(
        tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "ELDER", response == null ? null : response.getId(), "新增老人",
        null, response, request);
    return Result.ok(response);
  }

  @PreAuthorize("@elderAuthz.canWriteElder()")
  @PutMapping("/{id}")
  public Result<ElderResponse> update(@PathVariable Long id, @Valid @RequestBody ElderUpdateRequest request) {
    ElderResponse beforeSnapshot = elderService.get(id, AuthContext.getOrgId());
    request.setTenantId(AuthContext.getOrgId());
    request.setUpdatedBy(AuthContext.getStaffId());
    ElderResponse response = elderService.update(id, request);
    Long tenantId = AuthContext.getOrgId();
    auditLogService.recordStructured(
        tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "ELDER", id, "更新老人档案",
        beforeSnapshot, response, request);
    return Result.ok(response);
  }

  @PreAuthorize("@elderAuthz.canWriteElder()")
  @PostMapping("/{id}/profile-change-approval")
  public Result<OaApproval> applyProfileChangeApproval(
      @PathVariable Long id,
      @Valid @RequestBody ElderProfileChangeApprovalRequest request) {
    Long orgId = AuthContext.getOrgId();
    ElderProfile elder = elderMapper.selectById(id);
    if (elder == null || Integer.valueOf(1).equals(elder.getIsDeleted()) || (orgId != null && !orgId.equals(elder.getOrgId()))) {
      throw new IllegalArgumentException("老人档案不存在");
    }
    Long applicantId = AuthContext.getStaffId();
    OaApproval approval = new OaApproval();
    approval.setTenantId(orgId);
    approval.setOrgId(orgId);
    approval.setApprovalType("OFFICIAL_SEAL");
    approval.setTitle("老人档案修改申请：" + (elder.getFullName() == null ? ("#" + id) : elder.getFullName()));
    approval.setApplicantId(applicantId);
    approval.setApplicantName(AuthContext.getUsername());
    String module = request.getModule() == null || request.getModule().isBlank() ? "BASE_PROFILE" : request.getModule().trim().toUpperCase();
    String summary = request.getChangeSummary() == null ? "" : request.getChangeSummary().trim();
    approval.setFormData("{\"bizType\":\"ELDER_PROFILE_CHANGE\",\"elderId\":" + id
        + ",\"module\":\"" + module + "\""
        + ",\"reason\":\"" + escapeJson(request.getReason()) + "\""
        + ",\"summary\":\"" + escapeJson(summary) + "\"}");
    approval.setStatus("PENDING");
    approval.setRemark(request.getReason());
    approval.setCreatedBy(applicantId);
    approval.setStartTime(LocalDateTime.now());
    oaApprovalMapper.insert(approval);
    auditLogService.record(orgId, orgId, applicantId, AuthContext.getUsername(),
        "ELDER_PROFILE_CHANGE_APPLY", "ELDER", id, "提交老人档案修改审批申请");
    return Result.ok(approval);
  }

  @PreAuthorize("@elderAuthz.canReadElder()")
  @GetMapping("/{id}")
  public Result<ElderResponse> get(@PathVariable Long id) {
    return Result.ok(elderService.get(id, AuthContext.getOrgId()));
  }

  @PreAuthorize("@elderAuthz.canReadElder()")
  @GetMapping("/{id}/care-profile")
  public Result<ElderCareProfileResponse> careProfile(@PathVariable Long id) {
    ElderProfile elder = requireReadableElder(id);
    return Result.ok(buildCareProfile(elder));
  }

  @PreAuthorize("@elderAuthz.canWriteElder()")
  @PutMapping("/{id}/care-level")
  public Result<ElderResponse> adjustCareLevel(
      @PathVariable Long id,
      @Valid @RequestBody ElderCareLevelAdjustmentRequest request) {
    ElderProfile elder = requireReadableElder(id);
    String beforeCareLevel = normalizeText(elder.getCareLevel());
    String nextCareLevel = normalizeText(request.getCareLevel());
    if (nextCareLevel == null) {
      throw new IllegalArgumentException("护理等级不能为空");
    }
    if (!Objects.equals(beforeCareLevel, nextCareLevel)) {
      elder.setCareLevel(nextCareLevel);
      elderMapper.updateById(elder);
      ElderChangeLog log = new ElderChangeLog();
      log.setTenantId(elder.getTenantId());
      log.setOrgId(elder.getOrgId());
      log.setElderId(elder.getId());
      log.setChangeType("CARE_LEVEL_CHANGE");
      log.setBeforeValue(beforeCareLevel);
      log.setAfterValue(nextCareLevel);
      log.setReason(buildCareLevelReason(request));
      log.setCreatedBy(AuthContext.getStaffId());
      elderChangeLogMapper.insert(log);
      // 家属知情：护理等级调整同步通知家属（“提醒”关键词归为二级提醒），家属端可确认知悉
      familyNoticePublisher.publish(elder.getOrgId(),
          "护理等级调整提醒：" + elder.getFullName(),
          "长者 " + elder.getFullName() + " 的护理等级由“" + (beforeCareLevel == null ? "未评定" : beforeCareLevel)
              + "”调整为“" + nextCareLevel + "”。调整依据：" + buildCareLevelReason(request)
              + "。如有疑问请联系照护团队，可在消息详情中点击“我已知悉”确认。");
    }
    ElderResponse response = elderService.get(id, AuthContext.getOrgId());
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("beforeCareLevel", beforeCareLevel);
    context.put("afterCareLevel", nextCareLevel);
    context.put("assessmentRecordId", request.getAssessmentRecordId());
    context.put("reason", request.getReason());
    auditLogService.recordStructured(
        AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), AuthContext.getUsername(),
        "CARE_LEVEL_ADJUST", "ELDER", id, "护理等级动态调整",
        beforeCareLevel, response, context);
    return Result.ok(response);
  }

  @PreAuthorize("@elderAuthz.canReadElder()")
  @GetMapping("/page")
  public Result<IPage<ElderResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "false") Boolean signedOnly,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Integer elderStatus,
      @RequestParam(required = false) String lifecycleStatus,
      @RequestParam(required = false) String fullName,
      @RequestParam(required = false) String idCardNo,
      @RequestParam(required = false) String bedNo,
      @RequestParam(required = false) String careLevel,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortOrder) {
    Integer queryStatus = status == null ? elderStatus : status;
    return Result.ok(elderService.page(
        AuthContext.getOrgId(),
        pageNo,
        pageSize,
        keyword,
        signedOnly,
        queryStatus,
        lifecycleStatus,
        fullName,
        idCardNo,
        bedNo,
        careLevel,
        sortBy,
        sortOrder));
  }

  @PreAuthorize("@elderAuthz.canWriteElder()")
  @PostMapping("/{id}/assignBed")
  public Result<ElderResponse> assignBed(@PathVariable Long id, @Valid @RequestBody AssignBedRequest request) {
    ElderResponse beforeSnapshot = elderService.get(id, AuthContext.getOrgId());
    request.setTenantId(AuthContext.getOrgId());
    request.setCreatedBy(AuthContext.getStaffId());
    ElderResponse response = elderService.assignBed(id, request);
    Long tenantId = AuthContext.getOrgId();
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("bedId", request.getBedId());
    context.put("startDate", request.getStartDate());
    context.put("occupancyMode", request.getOccupancyMode());
    auditLogService.recordStructured(
        tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "BED_ASSIGN", "ELDER", id, "床位分配",
        beforeSnapshot, response, context);
    return Result.ok(response);
  }

  @PreAuthorize("@elderAuthz.canWriteElder()")
  @PostMapping("/{id}/unbindBed")
  public Result<ElderResponse> unbindBed(@PathVariable Long id,
      @RequestBody(required = false) UnbindBedRequest request,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(required = false) String reason) {
    Long tenantId = AuthContext.getOrgId();
    ElderResponse beforeSnapshot = elderService.get(id, tenantId);
    LocalDate actualEndDate = request != null && request.getEndDate() != null ? request.getEndDate() : endDate;
    String actualReason = request != null && request.getReason() != null ? request.getReason() : reason;
    ElderResponse response = elderService.unbindBed(id, actualEndDate, actualReason, tenantId, AuthContext.getStaffId());
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("endDate", actualEndDate);
    context.put("reason", actualReason);
    auditLogService.recordStructured(
        tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "BED_UNBIND", "ELDER", id, "床位解绑",
        beforeSnapshot, response, context);
    return Result.ok(response);
  }

  @PreAuthorize("@elderAuthz.canWriteElder()")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long tenantId = AuthContext.getOrgId();
    ElderResponse beforeSnapshot = elderService.get(id, tenantId);
    elderService.delete(id, tenantId, AuthContext.getStaffId());
    auditLogService.recordStructured(
        tenantId, tenantId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "ELDER", id, "删除老人档案",
        beforeSnapshot, null, null);
    return Result.ok(null);
  }

  private String escapeJson(String raw) {
    if (raw == null || raw.isBlank()) {
      return "";
    }
    return raw.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private ElderProfile requireReadableElder(Long id) {
    Long orgId = AuthContext.getOrgId();
    ElderProfile elder = elderMapper.selectById(id);
    if (elder == null || Integer.valueOf(1).equals(elder.getIsDeleted())
        || (orgId != null && !orgId.equals(elder.getOrgId()) && !orgId.equals(elder.getTenantId()))) {
      throw new IllegalArgumentException("老人档案不存在");
    }
    return elder;
  }

  private ElderCareProfileResponse buildCareProfile(ElderProfile elder) {
    Long orgId = AuthContext.getOrgId();
    List<AssessmentRecord> assessments = assessmentRecordMapper.selectList(Wrappers.lambdaQuery(AssessmentRecord.class)
        .eq(AssessmentRecord::getIsDeleted, 0)
        .eq(orgId != null, AssessmentRecord::getOrgId, orgId)
        .eq(AssessmentRecord::getElderId, elder.getId())
        .orderByDesc(AssessmentRecord::getAssessmentDate)
        .orderByDesc(AssessmentRecord::getUpdateTime)
        .last("LIMIT 6"));
    List<ElderChangeLog> changeLogs = elderChangeLogMapper.selectList(Wrappers.lambdaQuery(ElderChangeLog.class)
        .eq(ElderChangeLog::getElderId, elder.getId())
        .eq(ElderChangeLog::getChangeType, "CARE_LEVEL_CHANGE")
        .orderByDesc(ElderChangeLog::getCreateTime)
        .last("LIMIT 5"));

    ElderCareProfileResponse response = new ElderCareProfileResponse();
    response.setElderId(elder.getId());
    response.setElderName(elder.getFullName());
    response.setCurrentCareLevel(normalizeText(elder.getCareLevel()));
    response.setLatestAssessments(assessments.stream().map(this::toAssessmentSnapshot).toList());
    response.setChangeLogs(changeLogs.stream().map(this::toCareLevelChangeSnapshot).toList());
    response.setRiskTags(buildRiskTags(assessments));
    String suggestedCareLevel = suggestCareLevel(response.getCurrentCareLevel(), assessments);
    response.setSuggestedCareLevel(suggestedCareLevel);
    response.setAdjustmentRequired(suggestedCareLevel != null
        && !Objects.equals(normalizeText(response.getCurrentCareLevel()), suggestedCareLevel));
    response.setRecommendationReason(buildRecommendationReason(response, assessments));
    return response;
  }

  private ElderCareProfileResponse.AssessmentSnapshot toAssessmentSnapshot(AssessmentRecord record) {
    ElderCareProfileResponse.AssessmentSnapshot snapshot = new ElderCareProfileResponse.AssessmentSnapshot();
    snapshot.setId(record.getId());
    snapshot.setAssessmentType(record.getAssessmentType());
    snapshot.setLevelCode(record.getLevelCode());
    snapshot.setScore(record.getScore());
    snapshot.setAssessmentDate(record.getAssessmentDate());
    snapshot.setStatus(record.getStatus());
    snapshot.setResultSummary(record.getResultSummary());
    snapshot.setSuggestion(record.getSuggestion());
    return snapshot;
  }

  private ElderCareProfileResponse.CareLevelChangeSnapshot toCareLevelChangeSnapshot(ElderChangeLog log) {
    ElderCareProfileResponse.CareLevelChangeSnapshot snapshot = new ElderCareProfileResponse.CareLevelChangeSnapshot();
    snapshot.setId(log.getId());
    snapshot.setBeforeValue(log.getBeforeValue());
    snapshot.setAfterValue(log.getAfterValue());
    snapshot.setReason(log.getReason());
    snapshot.setCreateTime(log.getCreateTime());
    return snapshot;
  }

  private List<String> buildRiskTags(List<AssessmentRecord> records) {
    LinkedHashSet<String> tags = new LinkedHashSet<>();
    for (AssessmentRecord record : records) {
      String text = joinText(record.getAssessmentType(), record.getLevelCode(), record.getResultSummary(), record.getSuggestion());
      if (containsAny(text, "跌倒", "坠床", "FALL")) tags.add("跌倒风险");
      if (containsAny(text, "压疮", "压力性损伤", "PRESSURE")) tags.add("压疮风险");
      if (containsAny(text, "认知", "失智", "DEMENTIA", "COGNITIVE")) tags.add("认知风险");
      if (containsAny(text, "吞咽", "误吸", "ASPIRATION")) tags.add("吞咽风险");
      if (containsAny(text, "ADL", "自理", "失能", "重", "高", "A")) tags.add("重点照护");
    }
    return new ArrayList<>(tags);
  }

  private String suggestCareLevel(String currentCareLevel, List<AssessmentRecord> records) {
    for (AssessmentRecord record : records) {
      String text = joinText(record.getAssessmentType(), record.getLevelCode(), record.getResultSummary(), record.getSuggestion());
      if (containsAny(text, "重度", "重", "高", "HIGH", "VERY_HIGH", "A")) return "一级护理";
      if (containsAny(text, "中度", "中", "MEDIUM", "B")) return "二级护理";
      if (containsAny(text, "轻度", "轻", "低", "LOW", "C")) return "三级护理";
    }
    return normalizeText(currentCareLevel);
  }

  private String buildRecommendationReason(ElderCareProfileResponse response, List<AssessmentRecord> assessments) {
    if (assessments.isEmpty()) {
      return "暂无评估记录，建议先完成能力/风险评估后再调整护理等级。";
    }
    AssessmentRecord latest = assessments.get(0);
    String basis = "依据最近评估"
        + (latest.getAssessmentDate() == null ? "" : "（" + latest.getAssessmentDate() + "）")
        + "，等级结果：" + fallback(latest.getLevelCode(), "未标记");
    if (Boolean.TRUE.equals(response.getAdjustmentRequired())) {
      return basis + "，建议从“" + fallback(response.getCurrentCareLevel(), "未评定")
          + "”调整为“" + response.getSuggestedCareLevel() + "”。";
    }
    return basis + "，当前护理等级暂不需要调整。";
  }

  private String buildCareLevelReason(ElderCareLevelAdjustmentRequest request) {
    List<String> parts = new ArrayList<>();
    String reason = normalizeText(request.getReason());
    if (reason != null) {
      parts.add(reason);
    }
    if (request.getAssessmentRecordId() != null) {
      parts.add("关联评估记录#" + request.getAssessmentRecordId());
    }
    return parts.isEmpty() ? "护理等级动态调整" : String.join("；", parts);
  }

  private boolean containsAny(String text, String... keywords) {
    if (text == null || text.isBlank()) {
      return false;
    }
    String upper = text.toUpperCase();
    for (String keyword : keywords) {
      if (upper.contains(keyword.toUpperCase())) {
        return true;
      }
    }
    return false;
  }

  private String joinText(String... values) {
    return String.join(" ", java.util.Arrays.stream(values)
        .filter(value -> value != null && !value.isBlank())
        .toList());
  }

  private String normalizeText(String raw) {
    if (raw == null) {
      return null;
    }
    String trimmed = raw.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String fallback(String value, String fallback) {
    String normalized = normalizeText(value);
    return normalized == null ? fallback : normalized;
  }
}
