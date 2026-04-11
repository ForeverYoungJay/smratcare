package com.zhiyangyun.care.elder.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.model.AssignBedRequest;
import com.zhiyangyun.care.elder.model.ElderCreateRequest;
import com.zhiyangyun.care.elder.model.ElderProfileChangeApprovalRequest;
import com.zhiyangyun.care.elder.model.ElderResponse;
import com.zhiyangyun.care.elder.model.ElderUpdateRequest;
import com.zhiyangyun.care.elder.model.UnbindBedRequest;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.service.ElderService;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
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
  private final AuditLogService auditLogService;
  private final ElderMapper elderMapper;
  private final OaApprovalMapper oaApprovalMapper;

  public ElderController(
      ElderService elderService,
      AuditLogService auditLogService,
      ElderMapper elderMapper,
      OaApprovalMapper oaApprovalMapper) {
    this.elderService = elderService;
    this.auditLogService = auditLogService;
    this.elderMapper = elderMapper;
    this.oaApprovalMapper = oaApprovalMapper;
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

  private String escapeJson(String raw) {
    if (raw == null || raw.isBlank()) {
      return "";
    }
    return raw.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
