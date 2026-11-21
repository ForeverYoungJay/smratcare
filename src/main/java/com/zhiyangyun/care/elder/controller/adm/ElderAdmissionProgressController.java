package com.zhiyangyun.care.elder.controller.adm;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmissionProgress;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionProgressMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.Data;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 入住办理中心的暂存草稿 / 当前步骤 / 退回补充。步骤完成情况由前端从评估、合同、费用、床位等既有数据推导，这里不重复存储。 */
@RestController
@RequestMapping("/api/elder/admission-progress")
public class ElderAdmissionProgressController {
  static final String STATUS_DRAFT = "DRAFT";
  static final String STATUS_RETURNED = "RETURNED";

  private final ElderAdmissionProgressMapper progressMapper;
  private final ElderMapper elderMapper;
  private final AuditLogService auditLogService;

  public ElderAdmissionProgressController(ElderAdmissionProgressMapper progressMapper,
      ElderMapper elderMapper,
      AuditLogService auditLogService) {
    this.progressMapper = progressMapper;
    this.elderMapper = elderMapper;
    this.auditLogService = auditLogService;
  }

  @Data
  public static class ProgressRequest {
    @NotNull
    private Long elderId;
    @Size(max = 64)
    private String contractNo;
    @NotNull
    @Min(1)
    @Max(6)
    private Integer currentStep;
    @Size(max = 16)
    private String status;
    @Size(max = 200)
    private String returnReason;
    @Size(max = 4000)
    private String draftJson;
  }

  @GetMapping
  public Result<List<ElderAdmissionProgress>> list(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase(Locale.ROOT);
    return Result.ok(progressMapper.selectList(Wrappers.lambdaQuery(ElderAdmissionProgress.class)
        .eq(ElderAdmissionProgress::getIsDeleted, 0)
        .eq(orgId != null, ElderAdmissionProgress::getOrgId, orgId)
        .eq(elderId != null, ElderAdmissionProgress::getElderId, elderId)
        .eq(normalizedStatus != null, ElderAdmissionProgress::getStatus, normalizedStatus)
        .orderByDesc(ElderAdmissionProgress::getUpdateTime)
        .last("LIMIT 500")));
  }

  @PutMapping
  public Result<ElderAdmissionProgress> save(@Valid @RequestBody ProgressRequest request) {
    String status = normalizeStatus(request.getStatus());
    String returnReason = trimToNull(request.getReturnReason());
    if (STATUS_RETURNED.equals(status) && returnReason == null) {
      throw new IllegalArgumentException("退回补充时请填写原因");
    }
    ElderProfile elder = elderMapper.selectById(request.getElderId());
    if (elder == null || Integer.valueOf(1).equals(elder.getIsDeleted())) {
      throw new IllegalArgumentException("长者不存在");
    }
    ensureOrgAccess(elder.getOrgId());
    Long staffId = AuthContext.getStaffId();
    ElderAdmissionProgress progress = progressMapper.selectOne(Wrappers.lambdaQuery(ElderAdmissionProgress.class)
        .eq(ElderAdmissionProgress::getIsDeleted, 0)
        .eq(ElderAdmissionProgress::getOrgId, elder.getOrgId())
        .eq(ElderAdmissionProgress::getElderId, elder.getId())
        .last("LIMIT 1"));
    boolean created = progress == null;
    if (created) {
      progress = new ElderAdmissionProgress();
      progress.setTenantId(elder.getOrgId());
      progress.setOrgId(elder.getOrgId());
      progress.setElderId(elder.getId());
    }
    progress.setContractNo(trimToNull(request.getContractNo()));
    progress.setCurrentStep(request.getCurrentStep());
    progress.setStatus(status);
    progress.setReturnReason(STATUS_RETURNED.equals(status) ? returnReason : null);
    progress.setDraftJson(trimToNull(request.getDraftJson()));
    progress.setUpdatedBy(staffId);
    if (created) {
      progressMapper.insert(progress);
    } else {
      progressMapper.updateById(progress);
    }
    if (STATUS_RETURNED.equals(status)) {
      auditLogService.record(elder.getOrgId(), elder.getOrgId(), staffId, AuthContext.getUsername(),
          "ADMISSION_RETURN", "ELDER", elder.getId(), "入住办理退回补充：" + returnReason);
    }
    return Result.ok(progress);
  }

  static String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return STATUS_DRAFT;
    }
    String normalized = status.trim().toUpperCase(Locale.ROOT);
    if (!STATUS_DRAFT.equals(normalized) && !STATUS_RETURNED.equals(normalized)) {
      throw new IllegalArgumentException("办理状态只能是 DRAFT 或 RETURNED");
    }
    return normalized;
  }

  private void ensureOrgAccess(Long targetOrgId) {
    if (targetOrgId == null) {
      return;
    }
    Long currentOrgId = AuthContext.getOrgId();
    if (currentOrgId == null || AuthContext.isAdmin()) {
      return;
    }
    if (!Objects.equals(currentOrgId, targetOrgId)) {
      throw new AccessDeniedException("No permission to access another organization");
    }
  }

  private static String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
