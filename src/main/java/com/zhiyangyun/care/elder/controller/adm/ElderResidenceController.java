package com.zhiyangyun.care.elder.controller.adm;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDeathRegister;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderChangeLog;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischargeApply;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischarge;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderMedicalOutingRecord;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderOutingRecord;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderTrialStay;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderChangeLogMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDeathRegisterMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeApplyMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderMedicalOutingRecordMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderOutingRecordMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderTrialStayMapper;
import com.zhiyangyun.care.elder.model.lifecycle.DeathRegisterCancelRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DeathRegisterCreateRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DeathRegisterUpdateRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeApplyCreateRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeApplyReviewRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeRequest;
import com.zhiyangyun.care.elder.model.lifecycle.MedicalOutingCreateRequest;
import com.zhiyangyun.care.elder.model.lifecycle.MedicalOutingReturnRequest;
import com.zhiyangyun.care.elder.model.lifecycle.OutingCreateRequest;
import com.zhiyangyun.care.elder.model.lifecycle.OutingReturnRequest;
import com.zhiyangyun.care.elder.model.lifecycle.ResidenceLifecycleConstants;
import com.zhiyangyun.care.elder.model.lifecycle.ResidenceStatusSummaryResponse;
import com.zhiyangyun.care.elder.model.lifecycle.TrialStayRequest;
import com.zhiyangyun.care.elder.service.lifecycle.ElderLifecycleService;
import com.zhiyangyun.care.life.entity.IncidentReport;
import com.zhiyangyun.care.life.mapper.IncidentReportMapper;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
  private final ElderChangeLogMapper changeLogMapper;
  private final ElderMedicalOutingRecordMapper medicalOutingMapper;
  private final ElderDeathRegisterMapper deathRegisterMapper;
  private final ElderTrialStayMapper trialStayMapper;
  private final ElderDischargeApplyMapper dischargeApplyMapper;
  private final ElderDischargeMapper dischargeMapper;
  private final ElderMapper elderMapper;
  private final IncidentReportMapper incidentReportMapper;
  private final ElderLifecycleService lifecycleService;
  private final AuditLogService auditLogService;

  public ElderResidenceController(ElderOutingRecordMapper outingMapper,
                                  ElderChangeLogMapper changeLogMapper,
                                  ElderMedicalOutingRecordMapper medicalOutingMapper,
                                  ElderDeathRegisterMapper deathRegisterMapper,
                                  ElderTrialStayMapper trialStayMapper,
                                  ElderDischargeApplyMapper dischargeApplyMapper,
                                  ElderDischargeMapper dischargeMapper,
                                  ElderMapper elderMapper,
                                  IncidentReportMapper incidentReportMapper,
                                  ElderLifecycleService lifecycleService,
                                  AuditLogService auditLogService) {
    this.outingMapper = outingMapper;
    this.changeLogMapper = changeLogMapper;
    this.medicalOutingMapper = medicalOutingMapper;
    this.deathRegisterMapper = deathRegisterMapper;
    this.trialStayMapper = trialStayMapper;
    this.dischargeApplyMapper = dischargeApplyMapper;
    this.dischargeMapper = dischargeMapper;
    this.elderMapper = elderMapper;
    this.incidentReportMapper = incidentReportMapper;
    this.lifecycleService = lifecycleService;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/status-summary")
  public Result<ResidenceStatusSummaryResponse> statusSummary() {
    Long orgId = AuthContext.getOrgId();
    ResidenceStatusSummaryResponse response = new ResidenceStatusSummaryResponse();

    long inHospital = elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getStatus, 1));
    long outing = elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getStatus, 2));
    long discharged = elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getStatus, 3));
    long intent = elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .notIn(ElderProfile::getStatus, 1, 2, 3));

    long trial = trialStayMapper.selectCount(Wrappers.lambdaQuery(ElderTrialStay.class)
        .eq(ElderTrialStay::getIsDeleted, 0)
        .eq(orgId != null, ElderTrialStay::getOrgId, orgId)
        .eq(ElderTrialStay::getStatus, ResidenceLifecycleConstants.TRIAL_REGISTERED));
    long medicalOuting = medicalOutingMapper.selectCount(Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
        .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderMedicalOutingRecord::getOrgId, orgId)
        .eq(ElderMedicalOutingRecord::getStatus, ResidenceLifecycleConstants.MEDICAL_OUTING_OUT));
    long dischargePending = dischargeApplyMapper.selectCount(Wrappers.lambdaQuery(ElderDischargeApply.class)
        .eq(ElderDischargeApply::getIsDeleted, 0)
        .eq(orgId != null, ElderDischargeApply::getOrgId, orgId)
        .eq(ElderDischargeApply::getStatus, ResidenceLifecycleConstants.DISCHARGE_APPLY_PENDING));
    long death = deathRegisterMapper.selectCount(Wrappers.lambdaQuery(ElderDeathRegister.class)
        .eq(ElderDeathRegister::getIsDeleted, 0)
        .eq(orgId != null, ElderDeathRegister::getOrgId, orgId)
        .eq(ElderDeathRegister::getStatus, ResidenceLifecycleConstants.DEATH_REGISTERED));

    LocalDateTime now = LocalDateTime.now();
    long overdueOuting = outingMapper.selectCount(Wrappers.lambdaQuery(ElderOutingRecord.class)
        .eq(ElderOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderOutingRecord::getOrgId, orgId)
        .eq(ElderOutingRecord::getStatus, ResidenceLifecycleConstants.OUTING_OUT)
        .isNotNull(ElderOutingRecord::getExpectedReturnTime)
        .lt(ElderOutingRecord::getExpectedReturnTime, now))
        + medicalOutingMapper.selectCount(Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
            .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
            .eq(orgId != null, ElderMedicalOutingRecord::getOrgId, orgId)
            .eq(ElderMedicalOutingRecord::getStatus, ResidenceLifecycleConstants.MEDICAL_OUTING_OUT)
            .isNotNull(ElderMedicalOutingRecord::getExpectedReturnTime)
            .lt(ElderMedicalOutingRecord::getExpectedReturnTime, now));
    long openIncident = incidentReportMapper.selectCount(Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId)
        .eq(IncidentReport::getStatus, "OPEN"));

    response.setIntentCount(intent);
    response.setTrialCount(trial);
    response.setInHospitalCount(inHospital);
    response.setOutingCount(outing);
    response.setMedicalOutingCount(medicalOuting);
    response.setDischargePendingCount(dischargePending);
    response.setDischargedCount(discharged);
    response.setDeathCount(death);
    response.setOpenIncidentCount(openIncident);
    response.setOverdueOutingCount(overdueOuting);
    return Result.ok(response);
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
    ensureNoActiveOuting(request.getElderId(), orgId);
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
    ElderProfile elder = resolveElder(record.getElderId());
    Integer beforeStatus = elder.getStatus();
    if (!hasAnyActiveOuting(record.getElderId(), orgId)) {
      updateElderStatus(elder, 1);
      recordStatusChange(record.getElderId(), beforeStatus, 1, "外出返院");
    }
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "OUTING_RETURN", "ELDER", record.getElderId(), "外出返院登记");
    return Result.ok(record);
  }

  @GetMapping("/medical-outing/page")
  public Result<IPage<ElderMedicalOutingRecord>> medicalOutingPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeMedicalOutingStatusForQuery(status);
    var wrapper = Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
        .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderMedicalOutingRecord::getOrgId, orgId)
        .eq(elderId != null, ElderMedicalOutingRecord::getElderId, elderId)
        .eq(normalizedStatus != null, ElderMedicalOutingRecord::getStatus, normalizedStatus)
        .and(keyword != null && !keyword.isBlank(),
            w -> w.like(ElderMedicalOutingRecord::getElderName, keyword)
                .or().like(ElderMedicalOutingRecord::getHospitalName, keyword)
                .or().like(ElderMedicalOutingRecord::getDepartment, keyword)
                .or().like(ElderMedicalOutingRecord::getDiagnosis, keyword)
                .or().like(ElderMedicalOutingRecord::getReason, keyword))
        .orderByDesc(ElderMedicalOutingRecord::getOutingDate)
        .orderByDesc(ElderMedicalOutingRecord::getCreateTime);
    return Result.ok(medicalOutingMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping("/medical-outing")
  @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
  public Result<ElderMedicalOutingRecord> createMedicalOuting(@Valid @RequestBody MedicalOutingCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    ensureNoActiveOuting(request.getElderId(), orgId);
    ElderProfile elder = resolveElder(request.getElderId());
    Integer beforeStatus = elder.getStatus();
    ElderMedicalOutingRecord record = new ElderMedicalOutingRecord();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setElderId(request.getElderId());
    record.setElderName(resolveElderName(request.getElderId()));
    record.setOutingDate(request.getOutingDate());
    record.setExpectedReturnTime(request.getExpectedReturnTime());
    record.setHospitalName(request.getHospitalName());
    record.setDepartment(request.getDepartment());
    record.setDiagnosis(request.getDiagnosis());
    record.setCompanion(request.getCompanion());
    record.setReason(request.getReason());
    record.setStatus(ResidenceLifecycleConstants.MEDICAL_OUTING_OUT);
    record.setRemark(request.getRemark());
    record.setCreatedBy(AuthContext.getStaffId());
    medicalOutingMapper.insert(record);
    updateElderStatus(elder, 2);
    recordStatusChange(request.getElderId(), beforeStatus, 2, "外出就医登记");
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MEDICAL_OUTING_CREATE", "ELDER", request.getElderId(), "外出就医登记");
    return Result.ok(record);
  }

  @PutMapping("/medical-outing/{id}/return")
  @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
  public Result<ElderMedicalOutingRecord> returnFromMedicalOuting(@PathVariable Long id,
                                                                   @RequestBody MedicalOutingReturnRequest request) {
    Long orgId = AuthContext.getOrgId();
    ElderMedicalOutingRecord record = medicalOutingMapper.selectById(id);
    if (record == null || !Objects.equals(orgId, record.getOrgId()) || Integer.valueOf(1).equals(record.getIsDeleted())) {
      throw new IllegalArgumentException("记录不存在");
    }
    ElderProfile elder = resolveElder(record.getElderId());
    Integer beforeStatus = elder.getStatus();
    MedicalOutingReturnRequest payload = request == null ? new MedicalOutingReturnRequest() : request;
    record.setStatus(ResidenceLifecycleConstants.MEDICAL_OUTING_RETURNED);
    record.setActualReturnTime(payload.getActualReturnTime() == null ? LocalDateTime.now() : payload.getActualReturnTime());
    if (payload.getRemark() != null && !payload.getRemark().isBlank()) {
      record.setRemark(payload.getRemark());
    }
    medicalOutingMapper.updateById(record);
    if (!hasAnyActiveOuting(record.getElderId(), orgId)) {
      updateElderStatus(elder, 1);
      recordStatusChange(record.getElderId(), beforeStatus, 1, "外出就医返院");
    }
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MEDICAL_OUTING_RETURN", "ELDER", record.getElderId(), "外出就医返院登记");
    return Result.ok(record);
  }

  @GetMapping(value = "/medical-outing/export", produces = "text/csv;charset=UTF-8")
  @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
  public ResponseEntity<byte[]> exportMedicalOuting(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeMedicalOutingStatusForQuery(status);
    var wrapper = Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
        .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
        .eq(orgId != null, ElderMedicalOutingRecord::getOrgId, orgId)
        .eq(elderId != null, ElderMedicalOutingRecord::getElderId, elderId)
        .eq(normalizedStatus != null, ElderMedicalOutingRecord::getStatus, normalizedStatus)
        .and(keyword != null && !keyword.isBlank(),
            w -> w.like(ElderMedicalOutingRecord::getElderName, keyword)
                .or().like(ElderMedicalOutingRecord::getHospitalName, keyword)
                .or().like(ElderMedicalOutingRecord::getDepartment, keyword)
                .or().like(ElderMedicalOutingRecord::getDiagnosis, keyword)
                .or().like(ElderMedicalOutingRecord::getReason, keyword))
        .orderByDesc(ElderMedicalOutingRecord::getOutingDate)
        .orderByDesc(ElderMedicalOutingRecord::getCreateTime);
    List<ElderMedicalOutingRecord> records = medicalOutingMapper.selectList(wrapper);
    List<String> headers = List.of("老人姓名", "外出日期", "预计返院", "实际返院", "医院", "科室", "诊断", "原因", "状态", "备注");
    List<List<String>> rows = records.stream()
        .map(item -> List.of(
            safe(item.getElderName()),
            safe(item.getOutingDate()),
            safe(item.getExpectedReturnTime()),
            safe(item.getActualReturnTime()),
            safe(item.getHospitalName()),
            safe(item.getDepartment()),
            safe(item.getDiagnosis()),
            safe(item.getReason()),
            safe(item.getStatus()),
            safe(item.getRemark())))
        .toList();
    return csvResponse("elder-medical-outing", headers, rows);
  }

  @GetMapping("/death-register/page")
  public Result<IPage<ElderDeathRegister>> deathRegisterPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeDeathStatusForQuery(status);
    var wrapper = Wrappers.lambdaQuery(ElderDeathRegister.class)
        .eq(ElderDeathRegister::getIsDeleted, 0)
        .eq(orgId != null, ElderDeathRegister::getOrgId, orgId)
        .eq(elderId != null, ElderDeathRegister::getElderId, elderId)
        .eq(normalizedStatus != null, ElderDeathRegister::getStatus, normalizedStatus)
        .and(keyword != null && !keyword.isBlank(),
            w -> w.like(ElderDeathRegister::getElderName, keyword)
                .or().like(ElderDeathRegister::getCause, keyword)
                .or().like(ElderDeathRegister::getCertificateNo, keyword)
                .or().like(ElderDeathRegister::getReportedBy, keyword))
        .orderByDesc(ElderDeathRegister::getDeathDate)
        .orderByDesc(ElderDeathRegister::getCreateTime);
    return Result.ok(deathRegisterMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping("/death-register")
  @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
  public Result<ElderDeathRegister> createDeathRegister(@Valid @RequestBody DeathRegisterCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    long activeCount = deathRegisterMapper.selectCount(Wrappers.lambdaQuery(ElderDeathRegister.class)
        .eq(ElderDeathRegister::getIsDeleted, 0)
        .eq(ElderDeathRegister::getOrgId, orgId)
        .eq(ElderDeathRegister::getElderId, request.getElderId())
        .eq(ElderDeathRegister::getStatus, ResidenceLifecycleConstants.DEATH_REGISTERED));
    if (activeCount > 0) {
      throw new IllegalStateException("该老人已有有效死亡登记");
    }
    ElderProfile elder = resolveElder(request.getElderId());
    Integer beforeStatus = elder.getStatus();
    ElderDeathRegister record = new ElderDeathRegister();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setElderId(request.getElderId());
    record.setElderName(resolveElderName(request.getElderId()));
    record.setDeathDate(request.getDeathDate());
    record.setDeathTime(request.getDeathTime());
    record.setPlace(request.getPlace());
    record.setCause(request.getCause());
    record.setCertificateNo(request.getCertificateNo());
    record.setReportedBy(request.getReportedBy());
    record.setReportedTime(request.getReportedTime() == null ? LocalDateTime.now() : request.getReportedTime());
    record.setBeforeStatus(beforeStatus);
    record.setStatus(ResidenceLifecycleConstants.DEATH_REGISTERED);
    record.setRemark(request.getRemark());
    record.setCreatedBy(AuthContext.getStaffId());
    deathRegisterMapper.insert(record);
    updateElderStatus(elder, 3);
    recordStatusChange(request.getElderId(), beforeStatus, 3, "死亡登记");
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DEATH_REGISTER_CREATE", "ELDER", request.getElderId(), "死亡登记");
    return Result.ok(record);
  }

  @PutMapping("/death-register/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
  public Result<ElderDeathRegister> updateDeathRegister(@PathVariable Long id,
                                                        @Valid @RequestBody DeathRegisterUpdateRequest request) {
    Long orgId = AuthContext.getOrgId();
    ElderDeathRegister record = deathRegisterMapper.selectById(id);
    if (record == null || !Objects.equals(orgId, record.getOrgId()) || Integer.valueOf(1).equals(record.getIsDeleted())) {
      throw new IllegalArgumentException("记录不存在");
    }
    if (ResidenceLifecycleConstants.DEATH_CANCELLED.equals(record.getStatus())) {
      throw new IllegalStateException("已取消的死亡登记不可更正");
    }
    record.setDeathDate(request.getDeathDate());
    record.setDeathTime(request.getDeathTime());
    record.setPlace(request.getPlace());
    record.setCause(request.getCause());
    record.setCertificateNo(request.getCertificateNo());
    record.setReportedBy(request.getReportedBy());
    record.setReportedTime(request.getReportedTime());
    record.setRemark(request.getRemark());
    record.setUpdatedBy(AuthContext.getStaffId());
    deathRegisterMapper.updateById(record);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DEATH_REGISTER_UPDATE", "ELDER", record.getElderId(), "死亡登记更正");
    return Result.ok(record);
  }

  @PutMapping("/death-register/{id}/cancel")
  @PreAuthorize("hasRole('ADMIN')")
  public Result<ElderDeathRegister> cancelDeathRegister(@PathVariable Long id,
                                                        @RequestBody(required = false) DeathRegisterCancelRequest request) {
    Long orgId = AuthContext.getOrgId();
    ElderDeathRegister record = deathRegisterMapper.selectById(id);
    if (record == null || !Objects.equals(orgId, record.getOrgId()) || Integer.valueOf(1).equals(record.getIsDeleted())) {
      throw new IllegalArgumentException("记录不存在");
    }
    if (ResidenceLifecycleConstants.DEATH_CANCELLED.equals(record.getStatus())) {
      return Result.ok(record);
    }
    ElderProfile elder = resolveElder(record.getElderId());
    Integer beforeStatus = elder.getStatus();
    record.setStatus(ResidenceLifecycleConstants.DEATH_CANCELLED);
    if (request != null && request.getRemark() != null && !request.getRemark().isBlank()) {
      record.setRemark(request.getRemark());
    }
    record.setCancelledBy(AuthContext.getStaffId());
    record.setCancelledTime(LocalDateTime.now());
    deathRegisterMapper.updateById(record);
    Integer rollbackStatus = record.getBeforeStatus() == null ? 1 : record.getBeforeStatus();
    updateElderStatus(elder, rollbackStatus);
    recordStatusChange(record.getElderId(), beforeStatus, rollbackStatus, "死亡登记作废");
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DEATH_REGISTER_CANCEL", "ELDER", record.getElderId(), "死亡登记作废");
    return Result.ok(record);
  }

  @GetMapping(value = "/death-register/export", produces = "text/csv;charset=UTF-8")
  @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
  public ResponseEntity<byte[]> exportDeathRegister(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeDeathStatusForQuery(status);
    var wrapper = Wrappers.lambdaQuery(ElderDeathRegister.class)
        .eq(ElderDeathRegister::getIsDeleted, 0)
        .eq(orgId != null, ElderDeathRegister::getOrgId, orgId)
        .eq(elderId != null, ElderDeathRegister::getElderId, elderId)
        .eq(normalizedStatus != null, ElderDeathRegister::getStatus, normalizedStatus)
        .and(keyword != null && !keyword.isBlank(),
            w -> w.like(ElderDeathRegister::getElderName, keyword)
                .or().like(ElderDeathRegister::getCause, keyword)
                .or().like(ElderDeathRegister::getCertificateNo, keyword)
                .or().like(ElderDeathRegister::getReportedBy, keyword))
        .orderByDesc(ElderDeathRegister::getDeathDate)
        .orderByDesc(ElderDeathRegister::getCreateTime);
    List<ElderDeathRegister> records = deathRegisterMapper.selectList(wrapper);
    List<String> headers = List.of("老人姓名", "死亡日期", "死亡时间", "地点", "死亡原因", "证明编号", "上报人", "上报时间", "状态", "备注");
    List<List<String>> rows = records.stream()
        .map(item -> List.of(
            safe(item.getElderName()),
            safe(item.getDeathDate()),
            safe(item.getDeathTime()),
            safe(item.getPlace()),
            safe(item.getCause()),
            safe(item.getCertificateNo()),
            safe(item.getReportedBy()),
            safe(item.getReportedTime()),
            safe(item.getStatus()),
            safe(item.getRemark())))
        .toList();
    return csvResponse("elder-death-register", headers, rows);
  }

  @GetMapping("/trial-stay/page")
  public Result<IPage<ElderTrialStay>> trialStayPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate trialStartDateFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate trialStartDateTo) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeTrialStatusForQuery(status);
    LocalDate startDate = trialStartDateFrom;
    LocalDate endDate = trialStartDateTo;
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      LocalDate temp = startDate;
      startDate = endDate;
      endDate = temp;
    }
    var wrapper = Wrappers.lambdaQuery(ElderTrialStay.class)
        .eq(ElderTrialStay::getIsDeleted, 0)
        .eq(orgId != null, ElderTrialStay::getOrgId, orgId)
        .eq(elderId != null, ElderTrialStay::getElderId, elderId)
        .eq(normalizedStatus != null, ElderTrialStay::getStatus, normalizedStatus)
        .and(keyword != null && !keyword.isBlank(), w -> w.like(ElderTrialStay::getElderName, keyword)
            .or().like(ElderTrialStay::getCareLevel, keyword)
            .or().like(ElderTrialStay::getChannel, keyword))
        .ge(startDate != null, ElderTrialStay::getTrialStartDate, startDate)
        .le(endDate != null, ElderTrialStay::getTrialStartDate, endDate)
        .orderByDesc(ElderTrialStay::getTrialStartDate)
        .orderByDesc(ElderTrialStay::getCreateTime);
    return Result.ok(trialStayMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping(value = "/trial-stay/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportTrialStay(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate trialStartDateFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate trialStartDateTo) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeTrialStatusForQuery(status);
    LocalDate startDate = trialStartDateFrom;
    LocalDate endDate = trialStartDateTo;
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      LocalDate temp = startDate;
      startDate = endDate;
      endDate = temp;
    }
    var wrapper = Wrappers.lambdaQuery(ElderTrialStay.class)
        .eq(ElderTrialStay::getIsDeleted, 0)
        .eq(orgId != null, ElderTrialStay::getOrgId, orgId)
        .eq(elderId != null, ElderTrialStay::getElderId, elderId)
        .eq(normalizedStatus != null, ElderTrialStay::getStatus, normalizedStatus)
        .and(keyword != null && !keyword.isBlank(), w -> w.like(ElderTrialStay::getElderName, keyword)
            .or().like(ElderTrialStay::getCareLevel, keyword)
            .or().like(ElderTrialStay::getChannel, keyword))
        .ge(startDate != null, ElderTrialStay::getTrialStartDate, startDate)
        .le(endDate != null, ElderTrialStay::getTrialStartDate, endDate)
        .orderByDesc(ElderTrialStay::getTrialStartDate)
        .orderByDesc(ElderTrialStay::getCreateTime);
    List<ElderTrialStay> records = trialStayMapper.selectList(wrapper);
    List<String> headers = List.of("老人姓名", "试住开始日期", "试住结束日期", "来源渠道", "试住套餐", "意向等级", "状态", "护理等级", "备注", "创建时间");
    List<List<String>> rows = records.stream()
        .map(item -> List.of(
            safe(item.getElderName()),
            safe(item.getTrialStartDate()),
            safe(item.getTrialEndDate()),
            safe(item.getChannel()),
            safe(item.getTrialPackage()),
            safe(item.getIntentLevel()),
            trialStatusText(item.getStatus()),
            safe(item.getCareLevel()),
            safe(item.getRemark()),
            safe(item.getCreateTime())))
        .toList();
    return csvResponse("elder-trial-stay", headers, rows);
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
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate plannedDateFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate plannedDateTo) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeApplyStatusForQuery(status);
    LocalDate startDate = plannedDateFrom;
    LocalDate endDate = plannedDateTo;
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      LocalDate temp = startDate;
      startDate = endDate;
      endDate = temp;
    }
    var wrapper = Wrappers.lambdaQuery(ElderDischargeApply.class)
        .eq(ElderDischargeApply::getIsDeleted, 0)
        .eq(orgId != null, ElderDischargeApply::getOrgId, orgId)
        .eq(elderId != null, ElderDischargeApply::getElderId, elderId)
        .eq(normalizedStatus != null, ElderDischargeApply::getStatus, normalizedStatus)
        .and(keyword != null && !keyword.isBlank(), w -> w.like(ElderDischargeApply::getElderName, keyword)
            .or().like(ElderDischargeApply::getReason, keyword)
            .or().like(ElderDischargeApply::getReviewRemark, keyword))
        .ge(startDate != null, ElderDischargeApply::getPlannedDischargeDate, startDate)
        .le(endDate != null, ElderDischargeApply::getPlannedDischargeDate, endDate)
        .orderByDesc(ElderDischargeApply::getApplyDate)
        .orderByDesc(ElderDischargeApply::getCreateTime);
    return Result.ok(dischargeApplyMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping(value = "/discharge-apply/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> exportDischargeApply(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate plannedDateFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate plannedDateTo) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeApplyStatusForQuery(status);
    LocalDate startDate = plannedDateFrom;
    LocalDate endDate = plannedDateTo;
    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
      LocalDate temp = startDate;
      startDate = endDate;
      endDate = temp;
    }
    var wrapper = Wrappers.lambdaQuery(ElderDischargeApply.class)
        .eq(ElderDischargeApply::getIsDeleted, 0)
        .eq(orgId != null, ElderDischargeApply::getOrgId, orgId)
        .eq(elderId != null, ElderDischargeApply::getElderId, elderId)
        .eq(normalizedStatus != null, ElderDischargeApply::getStatus, normalizedStatus)
        .and(keyword != null && !keyword.isBlank(), w -> w.like(ElderDischargeApply::getElderName, keyword)
            .or().like(ElderDischargeApply::getReason, keyword)
            .or().like(ElderDischargeApply::getReviewRemark, keyword))
        .ge(startDate != null, ElderDischargeApply::getPlannedDischargeDate, startDate)
        .le(endDate != null, ElderDischargeApply::getPlannedDischargeDate, endDate)
        .orderByDesc(ElderDischargeApply::getApplyDate)
        .orderByDesc(ElderDischargeApply::getCreateTime);
    List<ElderDischargeApply> records = dischargeApplyMapper.selectList(wrapper);
    List<String> headers = List.of("老人姓名", "申请日期", "计划退住日期", "申请原因", "自动退住状态", "自动退住说明", "退住单号", "审核人", "审核备注", "审核状态", "审核时间");
    List<List<String>> rows = records.stream()
        .map(item -> List.of(
            safe(item.getElderName()),
            safe(item.getApplyDate()),
            safe(item.getPlannedDischargeDate()),
            safe(item.getReason()),
            safe(item.getAutoDischargeStatus()),
            safe(item.getAutoDischargeMessage()),
            safe(item.getLinkedDischargeId()),
            safe(item.getReviewedByName()),
            safe(item.getReviewRemark()),
            applyStatusText(item.getStatus()),
            safe(item.getReviewedTime())))
        .toList();
    return csvResponse("elder-discharge-apply", headers, rows);
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

  private String normalizeTrialStatusForQuery(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if (!isTrialStatusValid(normalized)) {
      throw new IllegalArgumentException("试住状态仅支持 REGISTERED/FINISHED/CONVERTED/CANCELLED");
    }
    return normalized;
  }

  private String normalizeApplyStatusForQuery(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if (!ResidenceLifecycleConstants.DISCHARGE_APPLY_PENDING.equals(normalized)
        && !ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED.equals(normalized)
        && !ResidenceLifecycleConstants.DISCHARGE_APPLY_REJECTED.equals(normalized)) {
      throw new IllegalArgumentException("审核状态仅支持 PENDING/APPROVED/REJECTED");
    }
    return normalized;
  }

  private String normalizeMedicalOutingStatusForQuery(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if (!ResidenceLifecycleConstants.MEDICAL_OUTING_OUT.equals(normalized)
        && !ResidenceLifecycleConstants.MEDICAL_OUTING_RETURNED.equals(normalized)) {
      throw new IllegalArgumentException("外出就医状态仅支持 OUT/RETURNED");
    }
    return normalized;
  }

  private String normalizeDeathStatusForQuery(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if (!ResidenceLifecycleConstants.DEATH_REGISTERED.equals(normalized)
        && !ResidenceLifecycleConstants.DEATH_CANCELLED.equals(normalized)) {
      throw new IllegalArgumentException("死亡登记状态仅支持 REGISTERED/CANCELLED");
    }
    return normalized;
  }

  private String trialStatusText(String status) {
    if (ResidenceLifecycleConstants.TRIAL_REGISTERED.equals(status)) {
      return "已登记";
    }
    if (ResidenceLifecycleConstants.TRIAL_FINISHED.equals(status)) {
      return "已结束";
    }
    if (ResidenceLifecycleConstants.TRIAL_CONVERTED.equals(status)) {
      return "已转签约";
    }
    if (ResidenceLifecycleConstants.TRIAL_CANCELLED.equals(status)) {
      return "已取消";
    }
    return safe(status);
  }

  private String applyStatusText(String status) {
    if (ResidenceLifecycleConstants.DISCHARGE_APPLY_PENDING.equals(status)) {
      return "待审核";
    }
    if (ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED.equals(status)) {
      return "已通过";
    }
    if (ResidenceLifecycleConstants.DISCHARGE_APPLY_REJECTED.equals(status)) {
      return "已驳回";
    }
    return safe(status);
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

  private void updateElderStatus(ElderProfile elder, Integer status) {
    if (elder == null || status == null) {
      return;
    }
    elder.setStatus(status);
    elderMapper.updateById(elder);
  }

  private boolean hasAnyActiveOuting(Long elderId, Long orgId) {
    if (elderId == null || orgId == null) {
      return false;
    }
    long outingCount = outingMapper.selectCount(Wrappers.lambdaQuery(ElderOutingRecord.class)
        .eq(ElderOutingRecord::getIsDeleted, 0)
        .eq(ElderOutingRecord::getOrgId, orgId)
        .eq(ElderOutingRecord::getElderId, elderId)
        .eq(ElderOutingRecord::getStatus, ResidenceLifecycleConstants.OUTING_OUT));
    long medicalOutingCount = medicalOutingMapper.selectCount(Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
        .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
        .eq(ElderMedicalOutingRecord::getOrgId, orgId)
        .eq(ElderMedicalOutingRecord::getElderId, elderId)
        .eq(ElderMedicalOutingRecord::getStatus, ResidenceLifecycleConstants.MEDICAL_OUTING_OUT));
    return outingCount + medicalOutingCount > 0;
  }

  private void ensureNoActiveOuting(Long elderId, Long orgId) {
    if (hasAnyActiveOuting(elderId, orgId)) {
      throw new IllegalStateException("该老人已有外出中的记录，请先返院后再登记");
    }
  }

  private void recordStatusChange(Long elderId, Integer beforeStatus, Integer afterStatus, String reason) {
    Long orgId = AuthContext.getOrgId();
    if (elderId == null || orgId == null) {
      return;
    }
    ElderChangeLog logRecord = new ElderChangeLog();
    logRecord.setTenantId(orgId);
    logRecord.setOrgId(orgId);
    logRecord.setElderId(elderId);
    logRecord.setChangeType("STATUS");
    logRecord.setBeforeValue(elderStatusText(beforeStatus));
    logRecord.setAfterValue(elderStatusText(afterStatus));
    logRecord.setReason(reason);
    logRecord.setCreatedBy(AuthContext.getStaffId());
    changeLogMapper.insert(logRecord);
  }

  private String elderStatusText(Integer status) {
    if (status == null) return "";
    if (status == 1) return "在院";
    if (status == 2) return "外出";
    if (status == 3) return "离院";
    return String.valueOf(status);
  }

  private ElderProfile resolveElder(Long elderId) {
    if (elderId == null) {
      throw new IllegalArgumentException("老人不存在");
    }
    ElderProfile elder = elderMapper.selectById(elderId);
    Long orgId = AuthContext.getOrgId();
    if (elder == null || Integer.valueOf(1).equals(elder.getIsDeleted()) || !Objects.equals(orgId, elder.getOrgId())) {
      throw new IllegalArgumentException("老人不存在");
    }
    return elder;
  }

  private String resolveElderName(Long elderId) {
    return resolveElder(elderId).getFullName();
  }
}
