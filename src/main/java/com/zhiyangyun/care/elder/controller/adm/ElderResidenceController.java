package com.zhiyangyun.care.elder.controller.adm;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischargeApply;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischarge;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderOutingRecord;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderTrialStay;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeApplyMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderOutingRecordMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderTrialStayMapper;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeApplyCreateRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeApplyReviewRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeRequest;
import com.zhiyangyun.care.elder.model.lifecycle.OutingCreateRequest;
import com.zhiyangyun.care.elder.model.lifecycle.OutingReturnRequest;
import com.zhiyangyun.care.elder.model.lifecycle.ResidenceLifecycleConstants;
import com.zhiyangyun.care.elder.model.lifecycle.TrialStayRequest;
import com.zhiyangyun.care.elder.service.lifecycle.ElderLifecycleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
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
@RequestMapping("/api/elder/lifecycle")
public class ElderResidenceController {
  private static final Logger log = LoggerFactory.getLogger(ElderResidenceController.class);

  private final ElderOutingRecordMapper outingMapper;
  private final ElderTrialStayMapper trialStayMapper;
  private final ElderDischargeApplyMapper dischargeApplyMapper;
  private final ElderDischargeMapper dischargeMapper;
  private final ElderMapper elderMapper;
  private final ElderLifecycleService lifecycleService;
  private final AuditLogService auditLogService;

  public ElderResidenceController(ElderOutingRecordMapper outingMapper,
                                  ElderTrialStayMapper trialStayMapper,
                                  ElderDischargeApplyMapper dischargeApplyMapper,
                                  ElderDischargeMapper dischargeMapper,
                                  ElderMapper elderMapper,
                                  ElderLifecycleService lifecycleService,
                                  AuditLogService auditLogService) {
    this.outingMapper = outingMapper;
    this.trialStayMapper = trialStayMapper;
    this.dischargeApplyMapper = dischargeApplyMapper;
    this.dischargeMapper = dischargeMapper;
    this.elderMapper = elderMapper;
    this.lifecycleService = lifecycleService;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/outing/page")
  public Result<IPage<ElderOutingRecord>> outingPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(ElderOutingRecord.class)
        .eq(ElderOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderOutingRecord::getOrgId, orgId)
        .eq(elderId != null, ElderOutingRecord::getElderId, elderId)
        .eq(status != null && !status.isBlank(), ElderOutingRecord::getStatus, status)
        .orderByDesc(ElderOutingRecord::getOutingDate)
        .orderByDesc(ElderOutingRecord::getCreateTime);
    return Result.ok(outingMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping("/outing")
  public Result<ElderOutingRecord> createOuting(@Valid @RequestBody OutingCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    ElderOutingRecord record = new ElderOutingRecord();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setElderId(request.getElderId());
    record.setElderName(resolveElderName(request.getElderId()));
    record.setOutingDate(request.getOutingDate());
    record.setExpectedReturnTime(request.getExpectedReturnTime());
    record.setCompanion(request.getCompanion());
    record.setReason(request.getReason());
    record.setStatus(ResidenceLifecycleConstants.OUTING_OUT);
    record.setRemark(request.getRemark());
    record.setCreatedBy(AuthContext.getStaffId());
    outingMapper.insert(record);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "OUTING_CREATE", "ELDER", request.getElderId(), "外出登记");
    return Result.ok(record);
  }

  @PutMapping("/outing/{id}/return")
  public Result<ElderOutingRecord> returnFromOuting(@PathVariable Long id, @RequestBody OutingReturnRequest request) {
    Long orgId = AuthContext.getOrgId();
    ElderOutingRecord record = outingMapper.selectById(id);
    if (record == null || !Objects.equals(orgId, record.getOrgId()) || Integer.valueOf(1).equals(record.getIsDeleted())) {
      throw new IllegalArgumentException("记录不存在");
    }
    OutingReturnRequest payload = request == null ? new OutingReturnRequest() : request;
    record.setStatus(ResidenceLifecycleConstants.OUTING_RETURNED);
    record.setActualReturnTime(payload.getActualReturnTime() == null ? LocalDateTime.now() : payload.getActualReturnTime());
    if (payload.getRemark() != null && !payload.getRemark().isBlank()) {
      record.setRemark(payload.getRemark());
    }
    outingMapper.updateById(record);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "OUTING_RETURN", "ELDER", record.getElderId(), "外出返院登记");
    return Result.ok(record);
  }

  @GetMapping("/trial-stay/page")
  public Result<IPage<ElderTrialStay>> trialStayPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(ElderTrialStay.class)
        .eq(ElderTrialStay::getIsDeleted, 0)
        .eq(orgId != null, ElderTrialStay::getOrgId, orgId)
        .eq(elderId != null, ElderTrialStay::getElderId, elderId)
        .eq(status != null && !status.isBlank(), ElderTrialStay::getStatus, status)
        .orderByDesc(ElderTrialStay::getTrialStartDate)
        .orderByDesc(ElderTrialStay::getCreateTime);
    return Result.ok(trialStayMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping("/trial-stay")
  public Result<ElderTrialStay> createTrialStay(@Valid @RequestBody TrialStayRequest request) {
    if (request.getTrialEndDate().isBefore(request.getTrialStartDate())) {
      throw new IllegalArgumentException("试住结束日期不能早于开始日期");
    }
    Long orgId = AuthContext.getOrgId();
    ElderTrialStay record = new ElderTrialStay();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setElderId(request.getElderId());
    record.setElderName(resolveElderName(request.getElderId()));
    record.setTrialStartDate(request.getTrialStartDate());
    record.setTrialEndDate(request.getTrialEndDate());
    record.setChannel(request.getChannel());
    record.setTrialPackage(request.getTrialPackage());
    record.setIntentLevel(request.getIntentLevel());
    record.setStatus(normalizeTrialStatus(request.getStatus()));
    record.setCareLevel(request.getCareLevel());
    record.setRemark(request.getRemark());
    record.setCreatedBy(AuthContext.getStaffId());
    trialStayMapper.insert(record);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "TRIAL_STAY_CREATE", "ELDER", request.getElderId(), "试住登记");
    return Result.ok(record);
  }

  @PutMapping("/trial-stay/{id}")
  public Result<ElderTrialStay> updateTrialStay(@PathVariable Long id, @Valid @RequestBody TrialStayRequest request) {
    if (request.getTrialEndDate().isBefore(request.getTrialStartDate())) {
      throw new IllegalArgumentException("试住结束日期不能早于开始日期");
    }
    Long orgId = AuthContext.getOrgId();
    ElderTrialStay record = trialStayMapper.selectById(id);
    if (record == null || !Objects.equals(orgId, record.getOrgId()) || Integer.valueOf(1).equals(record.getIsDeleted())) {
      throw new IllegalArgumentException("记录不存在");
    }
    record.setElderId(request.getElderId());
    record.setElderName(resolveElderName(request.getElderId()));
    record.setTrialStartDate(request.getTrialStartDate());
    record.setTrialEndDate(request.getTrialEndDate());
    record.setChannel(request.getChannel());
    record.setTrialPackage(request.getTrialPackage());
    record.setIntentLevel(request.getIntentLevel());
    record.setStatus(normalizeTrialStatus(request.getStatus() == null || request.getStatus().isBlank()
        ? record.getStatus() : request.getStatus()));
    record.setCareLevel(request.getCareLevel());
    record.setRemark(request.getRemark());
    trialStayMapper.updateById(record);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "TRIAL_STAY_UPDATE", "ELDER", request.getElderId(), "试住登记更新");
    return Result.ok(record);
  }

  @GetMapping("/discharge-apply/page")
  public Result<IPage<ElderDischargeApply>> dischargeApplyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(ElderDischargeApply.class)
        .eq(ElderDischargeApply::getIsDeleted, 0)
        .eq(orgId != null, ElderDischargeApply::getOrgId, orgId)
        .eq(elderId != null, ElderDischargeApply::getElderId, elderId)
        .eq(status != null && !status.isBlank(), ElderDischargeApply::getStatus, status)
        .orderByDesc(ElderDischargeApply::getApplyDate)
        .orderByDesc(ElderDischargeApply::getCreateTime);
    return Result.ok(dischargeApplyMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping("/discharge-apply")
  @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
  public Result<ElderDischargeApply> createDischargeApply(@Valid @RequestBody DischargeApplyCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    long pendingCount = dischargeApplyMapper.selectCount(Wrappers.lambdaQuery(ElderDischargeApply.class)
        .eq(ElderDischargeApply::getIsDeleted, 0)
        .eq(ElderDischargeApply::getOrgId, orgId)
        .eq(ElderDischargeApply::getElderId, request.getElderId())
        .eq(ElderDischargeApply::getStatus, ResidenceLifecycleConstants.DISCHARGE_APPLY_PENDING));
    if (pendingCount > 0) {
      throw new IllegalStateException("该老人已有待审核退住申请");
    }
    ElderDischargeApply record = new ElderDischargeApply();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setElderId(request.getElderId());
    record.setElderName(resolveElderName(request.getElderId()));
    record.setApplyDate(LocalDate.now());
    record.setPlannedDischargeDate(request.getPlannedDischargeDate());
    record.setReason(request.getReason());
    record.setStatus(ResidenceLifecycleConstants.DISCHARGE_APPLY_PENDING);
    record.setCreatedBy(AuthContext.getStaffId());
    try {
      dischargeApplyMapper.insert(record);
    } catch (DuplicateKeyException ex) {
      throw new IllegalStateException("该老人已有待审核退住申请");
    }
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DISCHARGE_APPLY_CREATE", "ELDER", request.getElderId(), "退住申请");
    return Result.ok(record);
  }

  @PutMapping("/discharge-apply/{id}/review")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<ElderDischargeApply> reviewDischargeApply(@PathVariable Long id,
                                                          @Valid @RequestBody DischargeApplyReviewRequest request) {
    Long orgId = AuthContext.getOrgId();
    ElderDischargeApply record = dischargeApplyMapper.selectById(id);
    if (record == null || !Objects.equals(orgId, record.getOrgId()) || Integer.valueOf(1).equals(record.getIsDeleted())) {
      throw new IllegalArgumentException("记录不存在");
    }
    String status = normalizeApplyStatus(request.getStatus());
    if (!ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED.equals(status)
        && !ResidenceLifecycleConstants.DISCHARGE_APPLY_REJECTED.equals(status)) {
      throw new IllegalArgumentException("审核状态仅支持 APPROVED 或 REJECTED");
    }
    if (!ResidenceLifecycleConstants.DISCHARGE_APPLY_PENDING.equals(record.getStatus())) {
      throw new IllegalStateException("该申请已审核，不能重复操作");
    }
    if (ResidenceLifecycleConstants.DISCHARGE_APPLY_REJECTED.equals(status)
        && (request.getReviewRemark() == null || request.getReviewRemark().isBlank())) {
      throw new IllegalArgumentException("驳回时请填写审核备注");
    }

    record.setReviewedBy(AuthContext.getStaffId());
    record.setReviewedByName(AuthContext.getUsername());
    record.setReviewedTime(LocalDateTime.now());

    if (ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED.equals(status)) {
      long dischargeCount = dischargeMapper.selectCount(Wrappers.lambdaQuery(ElderDischarge.class)
          .eq(ElderDischarge::getIsDeleted, 0)
          .eq(ElderDischarge::getOrgId, orgId)
          .eq(ElderDischarge::getElderId, record.getElderId()));
      try {
        if (dischargeCount == 0) {
          DischargeRequest dischargeRequest = new DischargeRequest();
          dischargeRequest.setTenantId(orgId);
          dischargeRequest.setOrgId(orgId);
          dischargeRequest.setCreatedBy(AuthContext.getStaffId());
          dischargeRequest.setElderId(record.getElderId());
          dischargeRequest.setDischargeDate(record.getPlannedDischargeDate());
          dischargeRequest.setReason(record.getReason());
          dischargeRequest.setRemark("退住申请审核通过自动触发");
          var dischargeResponse = lifecycleService.discharge(dischargeRequest);
          record.setLinkedDischargeId(dischargeResponse.getId());
          record.setAutoDischargeStatus(ResidenceLifecycleConstants.AUTO_DISCHARGE_SUCCESS);
          record.setAutoDischargeMessage("自动退住成功");
        } else {
          ElderDischarge discharge = dischargeMapper.selectOne(Wrappers.lambdaQuery(ElderDischarge.class)
              .eq(ElderDischarge::getIsDeleted, 0)
              .eq(ElderDischarge::getOrgId, orgId)
              .eq(ElderDischarge::getElderId, record.getElderId())
              .orderByDesc(ElderDischarge::getCreateTime)
              .last("limit 1"));
          record.setLinkedDischargeId(discharge == null ? null : discharge.getId());
          record.setAutoDischargeStatus(ResidenceLifecycleConstants.AUTO_DISCHARGE_SUCCESS);
          record.setAutoDischargeMessage("已存在退住登记，自动关联");
        }
      } catch (RuntimeException ex) {
        record.setAutoDischargeStatus(ResidenceLifecycleConstants.AUTO_DISCHARGE_FAILED);
        record.setAutoDischargeMessage(ex.getMessage());
        record.setReviewRemark(request.getReviewRemark());
        int failUpdated = dischargeApplyMapper.update(
            record,
            Wrappers.lambdaUpdate(ElderDischargeApply.class)
                .eq(ElderDischargeApply::getId, record.getId())
                .eq(ElderDischargeApply::getStatus, ResidenceLifecycleConstants.DISCHARGE_APPLY_PENDING));
        if (failUpdated == 0) {
          throw new IllegalStateException("申请状态已变更，请刷新后重试");
        }
        log.warn("Auto discharge failed, applyId={}, elderId={}, reason={}",
            record.getId(), record.getElderId(), ex.getMessage());
        throw ex;
      }
      auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
          "DISCHARGE_AUTO", "ELDER", record.getElderId(), "退住申请审核通过自动退住");
    } else {
      record.setAutoDischargeStatus(null);
      record.setAutoDischargeMessage(null);
      record.setLinkedDischargeId(null);
    }

    record.setStatus(status);
    record.setReviewRemark(request.getReviewRemark());
    int updated = dischargeApplyMapper.update(
        record,
        Wrappers.lambdaUpdate(ElderDischargeApply.class)
            .eq(ElderDischargeApply::getId, record.getId())
            .eq(ElderDischargeApply::getStatus, ResidenceLifecycleConstants.DISCHARGE_APPLY_PENDING));
    if (updated == 0) {
      throw new IllegalStateException("申请状态已变更，请刷新后重试");
    }
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DISCHARGE_APPLY_REVIEW", "ELDER", record.getElderId(), "退住申请审核:" + status);
    return Result.ok(record);
  }

  private boolean isTrialStatusValid(String status) {
    return ResidenceLifecycleConstants.TRIAL_REGISTERED.equals(status)
        || ResidenceLifecycleConstants.TRIAL_FINISHED.equals(status)
        || ResidenceLifecycleConstants.TRIAL_CONVERTED.equals(status)
        || ResidenceLifecycleConstants.TRIAL_CANCELLED.equals(status);
  }

  private String normalizeTrialStatus(String status) {
    if (status == null || status.isBlank()) {
      return ResidenceLifecycleConstants.TRIAL_REGISTERED;
    }
    String normalized = status.trim().toUpperCase();
    if (!isTrialStatusValid(normalized)) {
      throw new IllegalArgumentException("试住状态仅支持 REGISTERED/FINISHED/CONVERTED/CANCELLED");
    }
    return normalized;
  }

  private String normalizeApplyStatus(String status) {
    if (status == null || status.isBlank()) {
      throw new IllegalArgumentException("审核状态不能为空");
    }
    return status.trim().toUpperCase();
  }

  private String resolveElderName(Long elderId) {
    if (elderId == null) {
      return null;
    }
    ElderProfile elder = elderMapper.selectById(elderId);
    Long orgId = AuthContext.getOrgId();
    if (elder == null || Integer.valueOf(1).equals(elder.getIsDeleted()) || !Objects.equals(orgId, elder.getOrgId())) {
      throw new IllegalArgumentException("老人不存在");
    }
    return elder.getFullName();
  }
}
