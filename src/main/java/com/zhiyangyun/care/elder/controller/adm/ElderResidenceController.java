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
import com.zhiyangyun.care.elder.entity.lifecycle.ElderMedicalOutingRecord;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderOutingRecord;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderTrialStay;
import com.zhiyangyun.care.elder.model.BedReleaseResult;
import com.zhiyangyun.care.elder.model.ElderDepartureType;
import com.zhiyangyun.care.elder.model.ElderLifecycleStatus;
import com.zhiyangyun.care.elder.model.ElderStatus;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderChangeLogMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDeathRegisterMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeApplyMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderMedicalOutingRecordMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderOutingRecordMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderTrialStayMapper;
import com.zhiyangyun.care.elder.model.lifecycle.DeathRegisterCancelRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DeathRegisterCreateRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DeathRegisterUpdateRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeApplyCreateRequest;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeApplyReviewRequest;
import com.zhiyangyun.care.elder.model.lifecycle.MedicalOutingCreateRequest;
import com.zhiyangyun.care.elder.model.lifecycle.MedicalOutingReturnRequest;
import com.zhiyangyun.care.elder.model.lifecycle.OutingCreateRequest;
import com.zhiyangyun.care.elder.model.lifecycle.OutingReturnRequest;
import com.zhiyangyun.care.elder.model.lifecycle.ResidenceLifecycleConstants;
import com.zhiyangyun.care.elder.model.lifecycle.ResidenceStatusSummaryResponse;
import com.zhiyangyun.care.elder.model.lifecycle.TrialStayRequest;
import com.zhiyangyun.care.life.entity.IncidentReport;
import com.zhiyangyun.care.life.mapper.IncidentReportMapper;
import com.zhiyangyun.care.elder.service.ElderLifecycleStateService;
import com.zhiyangyun.care.elder.service.ElderOccupancyService;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/elder/lifecycle")
public class ElderResidenceController {
  private final ElderOutingRecordMapper outingMapper;
  private final ElderChangeLogMapper changeLogMapper;
  private final ElderMedicalOutingRecordMapper medicalOutingMapper;
  private final ElderDeathRegisterMapper deathRegisterMapper;
  private final ElderTrialStayMapper trialStayMapper;
  private final ElderDischargeApplyMapper dischargeApplyMapper;
  private final ElderMapper elderMapper;
  private final IncidentReportMapper incidentReportMapper;
  private final AuditLogService auditLogService;
  private final ElderLifecycleStateService elderLifecycleStateService;
  private final ElderOccupancyService elderOccupancyService;

  public ElderResidenceController(ElderOutingRecordMapper outingMapper,
                                  ElderChangeLogMapper changeLogMapper,
                                  ElderMedicalOutingRecordMapper medicalOutingMapper,
                                  ElderDeathRegisterMapper deathRegisterMapper,
                                  ElderTrialStayMapper trialStayMapper,
                                  ElderDischargeApplyMapper dischargeApplyMapper,
                                  ElderMapper elderMapper,
                                  IncidentReportMapper incidentReportMapper,
                                  AuditLogService auditLogService,
                                  ElderLifecycleStateService elderLifecycleStateService,
                                  ElderOccupancyService elderOccupancyService) {
    this.outingMapper = outingMapper;
    this.changeLogMapper = changeLogMapper;
    this.medicalOutingMapper = medicalOutingMapper;
    this.deathRegisterMapper = deathRegisterMapper;
    this.trialStayMapper = trialStayMapper;
    this.dischargeApplyMapper = dischargeApplyMapper;
    this.elderMapper = elderMapper;
    this.incidentReportMapper = incidentReportMapper;
    this.auditLogService = auditLogService;
    this.elderLifecycleStateService = elderLifecycleStateService;
    this.elderOccupancyService = elderOccupancyService;
  }

  @PreAuthorize("@elderAuthz.canReadResidence()")
  @GetMapping("/status-summary")
  public Result<ResidenceStatusSummaryResponse> statusSummary() {
    Long orgId = AuthContext.getOrgId();
    ResidenceStatusSummaryResponse response = new ResidenceStatusSummaryResponse();
    long inHospital = elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getLifecycleStatus, ElderLifecycleStatus.IN_HOSPITAL));
    long outing = elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getLifecycleStatus, ElderLifecycleStatus.OUTING));
    long discharged = elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getLifecycleStatus, ElderLifecycleStatus.DISCHARGED));
    long intent = elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .and(q -> q.isNull(ElderProfile::getLifecycleStatus)
            .or()
            .eq(ElderProfile::getLifecycleStatus, ElderLifecycleStatus.INTENT)));

    long trial = elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getLifecycleStatus, ElderLifecycleStatus.TRIAL));
    long medicalOuting = elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getLifecycleStatus, ElderLifecycleStatus.MEDICAL_OUTING));
    long dischargePending = elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getLifecycleStatus, ElderLifecycleStatus.DISCHARGE_PENDING));
    long death = elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getLifecycleStatus, ElderLifecycleStatus.DECEASED));

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
    response.setGeneratedAt(now);
    return Result.ok(response);
  }

  @PreAuthorize("@elderAuthz.canManageOuting()")
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

  @PreAuthorize("@elderAuthz.canManageOuting()")
  @PostMapping("/outing")
  public Result<ElderOutingRecord> createOuting(@Valid @RequestBody OutingCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    ensureNoActiveOuting(request.getElderId(), orgId);
    ElderProfile elder = resolveElder(request.getElderId());
    Integer beforeStatus = elder.getStatus();
    ElderOutingRecord record = new ElderOutingRecord();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setElderId(request.getElderId());
    record.setElderName(resolveElderName(request.getElderId()));
    record.setOutingDate(request.getOutingDate());
    record.setExpectedReturnTime(request.getExpectedReturnTime());
    record.setCompanion(request.getCompanion());
    record.setReason(request.getReason());
    record.setLeaveNoteImageUrl(request.getLeaveNoteImageUrl());
    record.setStatus(ResidenceLifecycleConstants.OUTING_OUT);
    record.setRemark(request.getRemark());
    record.setCreatedBy(AuthContext.getStaffId());
    outingMapper.insert(record);
    updateElderStatus(
        elder,
        ElderStatus.OUTING,
        com.zhiyangyun.care.elder.model.ElderLifecycleStatus.OUTING,
        null,
        "OUTING_CREATE",
        request.getReason(),
        "ELDER_OUTING",
        record.getId());
    recordStatusChange(request.getElderId(), beforeStatus, ElderStatus.OUTING, "外出登记");
    auditLogService.recordStructured(
        orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "OUTING_CREATE", "ELDER", request.getElderId(), "外出登记",
        buildStatusSnapshot(beforeStatus, elder.getBedId()), record,
        buildStatusTransitionContext(beforeStatus, ElderStatus.OUTING, request.getReason()));
    return Result.ok(record);
  }

  @PreAuthorize(OUTING_ACCESS)
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
      updateElderStatus(
          elder,
          ElderStatus.IN_HOSPITAL,
          com.zhiyangyun.care.elder.model.ElderLifecycleStatus.IN_HOSPITAL,
          null,
          "OUTING_RETURN",
          "外出返院",
          "ELDER_OUTING",
          record.getId());
      recordStatusChange(record.getElderId(), beforeStatus, ElderStatus.IN_HOSPITAL, "外出返院");
    }
    auditLogService.recordStructured(
        orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "OUTING_RETURN", "ELDER", record.getElderId(), "外出返院登记",
        buildStatusSnapshot(beforeStatus, elder.getBedId()), record,
        buildStatusTransitionContext(beforeStatus, hasAnyActiveOuting(record.getElderId(), orgId) ? beforeStatus : ElderStatus.IN_HOSPITAL, payload.getRemark()));
    return Result.ok(record);
  }

  @DeleteMapping("/outing/{id}")
  @PreAuthorize("@elderAuthz.canManageDischarge()")
  public Result<Boolean> deleteOuting(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    ElderOutingRecord record = outingMapper.selectById(id);
    if (record == null || !Objects.equals(orgId, record.getOrgId()) || Integer.valueOf(1).equals(record.getIsDeleted())) {
      throw new IllegalArgumentException("记录不存在");
    }
    boolean wasOut = ResidenceLifecycleConstants.OUTING_OUT.equals(record.getStatus());
    record.setIsDeleted(1);
    outingMapper.updateById(record);
    if (wasOut && !hasAnyActiveOuting(record.getElderId(), orgId)) {
      ElderProfile elder = resolveElder(record.getElderId());
      Integer beforeStatus = elder.getStatus();
      updateElderStatus(
          elder,
          ElderStatus.IN_HOSPITAL,
          com.zhiyangyun.care.elder.model.ElderLifecycleStatus.IN_HOSPITAL,
          null,
          "OUTING_DELETE",
          "删除外出记录",
          "ELDER_OUTING",
          record.getId());
      recordStatusChange(record.getElderId(), beforeStatus, ElderStatus.IN_HOSPITAL, "删除外出记录");
    }
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "OUTING_DELETE", "ELDER", record.getElderId(), "删除外出登记");
    return Result.ok(true);
  }

  @GetMapping("/medical-outing/page")
  @PreAuthorize("@elderAuthz.canManageOuting()")
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
  @PreAuthorize("@elderAuthz.canManageOuting()")
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
    updateElderStatus(
        elder,
        ElderStatus.OUTING,
        com.zhiyangyun.care.elder.model.ElderLifecycleStatus.MEDICAL_OUTING,
        null,
        "MEDICAL_OUTING_CREATE",
        request.getReason(),
        "ELDER_MEDICAL_OUTING",
        record.getId());
    recordStatusChange(request.getElderId(), beforeStatus, ElderStatus.OUTING, "外出就医登记");
    auditLogService.recordStructured(
        orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MEDICAL_OUTING_CREATE", "ELDER", request.getElderId(), "外出就医登记",
        buildStatusSnapshot(beforeStatus, elder.getBedId()), record,
        buildStatusTransitionContext(beforeStatus, ElderStatus.OUTING, request.getReason()));
    return Result.ok(record);
  }

  @PutMapping("/medical-outing/{id}/return")
  @PreAuthorize("@elderAuthz.canManageOuting()")
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
      updateElderStatus(
          elder,
          ElderStatus.IN_HOSPITAL,
          com.zhiyangyun.care.elder.model.ElderLifecycleStatus.IN_HOSPITAL,
          null,
          "MEDICAL_OUTING_RETURN",
          "外出就医返院",
          "ELDER_MEDICAL_OUTING",
          record.getId());
      recordStatusChange(record.getElderId(), beforeStatus, ElderStatus.IN_HOSPITAL, "外出就医返院");
    }
    auditLogService.recordStructured(
        orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MEDICAL_OUTING_RETURN", "ELDER", record.getElderId(), "外出就医返院登记",
        buildStatusSnapshot(beforeStatus, elder.getBedId()), record,
        buildStatusTransitionContext(beforeStatus, hasAnyActiveOuting(record.getElderId(), orgId) ? beforeStatus : ElderStatus.IN_HOSPITAL, payload.getRemark()));
    return Result.ok(record);
  }

  @DeleteMapping("/medical-outing/{id}")
  @PreAuthorize("@elderAuthz.canManageOuting()")
  public Result<Boolean> deleteMedicalOuting(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    ElderMedicalOutingRecord record = medicalOutingMapper.selectById(id);
    if (record == null || !Objects.equals(orgId, record.getOrgId()) || Integer.valueOf(1).equals(record.getIsDeleted())) {
      throw new IllegalArgumentException("记录不存在");
    }
    boolean wasOut = ResidenceLifecycleConstants.MEDICAL_OUTING_OUT.equals(record.getStatus());
    record.setIsDeleted(1);
    medicalOutingMapper.updateById(record);
    if (wasOut && !hasAnyActiveOuting(record.getElderId(), orgId)) {
      ElderProfile elder = resolveElder(record.getElderId());
      Integer beforeStatus = elder.getStatus();
      updateElderStatus(
          elder,
          ElderStatus.IN_HOSPITAL,
          com.zhiyangyun.care.elder.model.ElderLifecycleStatus.IN_HOSPITAL,
          null,
          "MEDICAL_OUTING_DELETE",
          "删除外出就医记录",
          "ELDER_MEDICAL_OUTING",
          record.getId());
      recordStatusChange(record.getElderId(), beforeStatus, ElderStatus.IN_HOSPITAL, "删除外出就医记录");
    }
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "MEDICAL_OUTING_DELETE", "ELDER", record.getElderId(), "删除外出就医登记");
    return Result.ok(true);
  }

  @GetMapping(value = "/medical-outing/export", produces = "text/csv;charset=UTF-8")
  @PreAuthorize("@elderAuthz.canManageOuting()")
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
  @PreAuthorize("@elderAuthz.canManageOuting()")
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
  @PreAuthorize("@elderAuthz.canManageOuting()")
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
    Long previousBedId = elder.getBedId();
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
    releaseBedForDeparture(elder, request.getDeathDate(), "死亡登记释放床位");
    updateElderStatus(
        elder,
        ElderStatus.DISCHARGED,
        com.zhiyangyun.care.elder.model.ElderLifecycleStatus.DECEASED,
        ElderDepartureType.DEATH,
        "DEATH_REGISTER_CREATE",
        request.getCause(),
        "ELDER_DEATH_REGISTER",
        record.getId());
    recordStatusChange(request.getElderId(), beforeStatus, ElderStatus.DISCHARGED, "死亡登记");
    auditLogService.recordStructured(
        orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DEATH_REGISTER_CREATE", "ELDER", request.getElderId(), "死亡登记",
        buildStatusSnapshot(beforeStatus, previousBedId), record,
        buildDepartureContext(beforeStatus, ElderStatus.DISCHARGED, request.getCause(), previousBedId));
    return Result.ok(record);
  }

  @PutMapping("/death-register/{id}")
  @PreAuthorize("@elderAuthz.canManageOuting()")
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
  @PreAuthorize("@elderAuthz.canManageOuting()")
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
    Integer rollbackStatus = record.getBeforeStatus() == null ? ElderStatus.IN_HOSPITAL : record.getBeforeStatus();
    updateElderStatus(
        elder,
        rollbackStatus,
        null,
        null,
        "DEATH_REGISTER_CANCEL",
        "死亡登记作废",
        "ELDER_DEATH_REGISTER",
        record.getId());
    recordStatusChange(record.getElderId(), beforeStatus, rollbackStatus, "死亡登记作废");
    auditLogService.recordStructured(
        orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DEATH_REGISTER_CANCEL", "ELDER", record.getElderId(), "死亡登记作废",
        buildStatusSnapshot(beforeStatus, elder.getBedId()), record,
        buildStatusTransitionContext(beforeStatus, rollbackStatus, request == null ? null : request.getRemark()));
    return Result.ok(record);
  }

  @DeleteMapping("/death-register/{id}")
  @PreAuthorize("@elderAuthz.canManageOuting()")
  public Result<Boolean> deleteDeathRegister(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    ElderDeathRegister record = deathRegisterMapper.selectById(id);
    if (record == null || !Objects.equals(orgId, record.getOrgId()) || Integer.valueOf(1).equals(record.getIsDeleted())) {
      throw new IllegalArgumentException("记录不存在");
    }
    if (!ResidenceLifecycleConstants.DEATH_CANCELLED.equals(record.getStatus())) {
      throw new IllegalStateException("仅允许删除已作废的死亡登记");
    }
    record.setIsDeleted(1);
    deathRegisterMapper.updateById(record);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DEATH_REGISTER_DELETE", "ELDER", record.getElderId(), "删除死亡登记");
    return Result.ok(true);
  }

  @GetMapping(value = "/death-register/export", produces = "text/csv;charset=UTF-8")
  @PreAuthorize("hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
  @PreAuthorize("@elderAuthz.canManageTrial()")
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
  @PreAuthorize("@elderAuthz.canManageTrial()")
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
  @PreAuthorize("@elderAuthz.canManageTrial()")
  public Result<ElderTrialStay> createTrialStay(@Valid @RequestBody TrialStayRequest request) {
    if (request.getTrialEndDate().isBefore(request.getTrialStartDate())) {
      throw new IllegalArgumentException("试住结束日期不能早于开始日期");
    }
    Long orgId = AuthContext.getOrgId();
    ElderProfile elder = resolveElder(request.getElderId());
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
    syncTrialLifecycleForElder(
        elder.getId(),
        "TRIAL_STAY_CREATE",
        "试住登记",
        "ELDER_TRIAL_STAY",
        record.getId());
    auditLogService.recordStructured(
        orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "TRIAL_STAY_CREATE", "ELDER", request.getElderId(), "试住登记",
        null, record, request);
    return Result.ok(record);
  }

  @PutMapping("/trial-stay/{id}")
  @PreAuthorize("@elderAuthz.canManageTrial()")
  public Result<ElderTrialStay> updateTrialStay(@PathVariable Long id, @Valid @RequestBody TrialStayRequest request) {
    if (request.getTrialEndDate().isBefore(request.getTrialStartDate())) {
      throw new IllegalArgumentException("试住结束日期不能早于开始日期");
    }
    Long orgId = AuthContext.getOrgId();
    ElderTrialStay record = trialStayMapper.selectById(id);
    if (record == null || !Objects.equals(orgId, record.getOrgId()) || Integer.valueOf(1).equals(record.getIsDeleted())) {
      throw new IllegalArgumentException("记录不存在");
    }
    Long previousElderId = record.getElderId();
    ElderTrialStay beforeSnapshot = copyTrialStay(record);
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
    syncTrialLifecycleForElder(
        record.getElderId(),
        "TRIAL_STAY_UPDATE",
        "试住登记更新",
        "ELDER_TRIAL_STAY",
        record.getId());
    if (previousElderId != null && !Objects.equals(previousElderId, record.getElderId())) {
      syncTrialLifecycleForElder(
          previousElderId,
          "TRIAL_STAY_TRANSFER",
          "试住登记转移后刷新状态",
          "ELDER_TRIAL_STAY",
          record.getId());
    }
    auditLogService.recordStructured(
        orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "TRIAL_STAY_UPDATE", "ELDER", request.getElderId(), "试住登记更新",
        beforeSnapshot, record, request);
    return Result.ok(record);
  }

  @DeleteMapping("/trial-stay/{id}")
  @PreAuthorize("@elderAuthz.canManageTrial()")
  public Result<Boolean> deleteTrialStay(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    ElderTrialStay record = trialStayMapper.selectById(id);
    if (record == null || !Objects.equals(orgId, record.getOrgId()) || Integer.valueOf(1).equals(record.getIsDeleted())) {
      throw new IllegalArgumentException("记录不存在");
    }
    ElderTrialStay beforeSnapshot = copyTrialStay(record);
    Long elderId = record.getElderId();
    record.setIsDeleted(1);
    trialStayMapper.updateById(record);
    syncTrialLifecycleForElder(
        elderId,
        "TRIAL_STAY_DELETE",
        "删除试住登记",
        "ELDER_TRIAL_STAY",
        record.getId());
    auditLogService.recordStructured(
        orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "TRIAL_STAY_DELETE", "ELDER", record.getElderId(), "删除试住登记",
        beforeSnapshot, null, Map.of("trialStayId", id));
    return Result.ok(true);
  }

  @GetMapping("/discharge-apply/page")
  @PreAuthorize("@elderAuthz.canManageDischarge()")
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
  @PreAuthorize("@elderAuthz.canManageDischarge()")
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
  @PreAuthorize("@elderAuthz.canManageDischarge()")
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
    record.setProofFileUrl(request.getProofFileUrl());
    record.setStatus(ResidenceLifecycleConstants.DISCHARGE_APPLY_PENDING);
    record.setCreatedBy(AuthContext.getStaffId());
    try {
      dischargeApplyMapper.insert(record);
    } catch (DuplicateKeyException ex) {
      throw new IllegalStateException("该老人已有待审核退住申请");
    }
    auditLogService.recordStructured(
        orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DISCHARGE_APPLY_CREATE", "ELDER", request.getElderId(), "退住申请",
        null, record, request);
    return Result.ok(record);
  }

  @PutMapping("/discharge-apply/{id}/review")
  @PreAuthorize("@elderAuthz.canApproveDischarge()")
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

    ElderDischargeApply beforeSnapshot = copyDischargeApply(record);
    record.setReviewedBy(AuthContext.getStaffId());
    record.setReviewedByName(AuthContext.getUsername());
    record.setReviewedTime(LocalDateTime.now());

    if (ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED.equals(status)) {
      ElderProfile elder = resolveElder(record.getElderId());
      String beforeLifecycleStatus = elder.getLifecycleStatus();
      record.setLinkedDischargeId(null);
      record.setAutoDischargeStatus(ResidenceLifecycleConstants.AUTO_DISCHARGE_PENDING_SETTLEMENT);
      record.setAutoDischargeMessage("审核已通过，请先完成退住费用审核与退院结算，财务退款后自动释放床位并回写离院状态");
      updateElderStatus(
          elder,
          ElderStatus.IN_HOSPITAL,
          com.zhiyangyun.care.elder.model.ElderLifecycleStatus.DISCHARGE_PENDING,
          null,
          "DISCHARGE_APPLY_APPROVE",
          request.getReviewRemark(),
          "ELDER_DISCHARGE_APPLY",
          record.getId());
      recordLifecycleStatusChange(
          record.getElderId(),
          beforeLifecycleStatus,
          com.zhiyangyun.care.elder.model.ElderLifecycleStatus.DISCHARGE_PENDING,
          "退住申请审核通过");
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
    auditLogService.recordStructured(
        orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DISCHARGE_APPLY_REVIEW", "ELDER", record.getElderId(), "退住申请审核:" + status,
        beforeSnapshot, record, request);
    return Result.ok(record);
  }

  @DeleteMapping("/discharge-apply/{id}")
  @PreAuthorize("@elderAuthz.canManageDischarge()")
  public Result<Boolean> deleteDischargeApply(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    ElderDischargeApply record = dischargeApplyMapper.selectById(id);
    if (record == null || !Objects.equals(orgId, record.getOrgId()) || Integer.valueOf(1).equals(record.getIsDeleted())) {
      throw new IllegalArgumentException("记录不存在");
    }
    if (ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED.equals(record.getStatus())) {
      throw new IllegalStateException("已通过的退住申请不可删除");
    }
    record.setIsDeleted(1);
    dischargeApplyMapper.updateById(record);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DISCHARGE_APPLY_DELETE", "ELDER", record.getElderId(), "删除退住申请");
    return Result.ok(true);
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
    updateElderStatus(elder, status, null, null, "STATUS_CHANGE", "状态变更", "ELDER", elder == null ? null : elder.getId());
  }

  private void updateElderStatus(
      ElderProfile elder,
      Integer status,
      String lifecycleStatus,
      String departureType,
      String eventType,
      String reason,
      String bizRefType,
      Long bizRefId) {
    if (elder == null || status == null) {
      return;
    }
    elder.setStatus(status);
    if (lifecycleStatus == null || lifecycleStatus.isBlank()) {
      elderLifecycleStateService.transitionFromLegacyStatus(
          elder,
          status,
          departureType,
          eventType,
          reason,
          bizRefType,
          bizRefId,
          AuthContext.getStaffId());
      return;
    }
    elderLifecycleStateService.transition(
        elder,
        lifecycleStatus,
        departureType,
        eventType,
        reason,
        bizRefType,
        bizRefId,
        AuthContext.getStaffId());
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

  private void syncTrialLifecycleForElder(
      Long elderId,
      String eventType,
      String reason,
      String bizRefType,
      Long bizRefId) {
    Long orgId = AuthContext.getOrgId();
    if (elderId == null || orgId == null) {
      return;
    }
    ElderProfile elder = resolveElder(elderId);
    String beforeLifecycleStatus = elder.getLifecycleStatus();
    String currentLifecycleStatus = ElderLifecycleStatus.normalize(beforeLifecycleStatus);
    long activeTrialCount = trialStayMapper.selectCount(Wrappers.lambdaQuery(ElderTrialStay.class)
        .eq(ElderTrialStay::getIsDeleted, 0)
        .eq(ElderTrialStay::getOrgId, orgId)
        .eq(ElderTrialStay::getElderId, elderId)
        .eq(ElderTrialStay::getStatus, ResidenceLifecycleConstants.TRIAL_REGISTERED));
    if (activeTrialCount > 0) {
      if (currentLifecycleStatus == null
          || ElderLifecycleStatus.INTENT.equals(currentLifecycleStatus)
          || ElderLifecycleStatus.TRIAL.equals(currentLifecycleStatus)) {
        elderLifecycleStateService.transition(
            elder,
            ElderLifecycleStatus.TRIAL,
            null,
            eventType,
            reason,
            bizRefType,
            bizRefId,
            AuthContext.getStaffId());
        recordLifecycleStatusChange(elderId, beforeLifecycleStatus, ElderLifecycleStatus.TRIAL, reason);
      }
      return;
    }
    if (ElderLifecycleStatus.TRIAL.equals(currentLifecycleStatus)) {
      elderLifecycleStateService.transition(
          elder,
          ElderLifecycleStatus.INTENT,
          null,
          eventType,
          reason,
          bizRefType,
          bizRefId,
          AuthContext.getStaffId());
      recordLifecycleStatusChange(elderId, beforeLifecycleStatus, ElderLifecycleStatus.INTENT, reason);
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

  private void recordBedChange(Long elderId, Long beforeBedId, Long afterBedId, String reason) {
    Long orgId = AuthContext.getOrgId();
    if (elderId == null || orgId == null) {
      return;
    }
    ElderChangeLog logRecord = new ElderChangeLog();
    logRecord.setTenantId(orgId);
    logRecord.setOrgId(orgId);
    logRecord.setElderId(elderId);
    logRecord.setChangeType("BED_CHANGE");
    logRecord.setBeforeValue(beforeBedId == null ? null : String.valueOf(beforeBedId));
    logRecord.setAfterValue(afterBedId == null ? null : String.valueOf(afterBedId));
    logRecord.setReason(reason);
    logRecord.setCreatedBy(AuthContext.getStaffId());
    changeLogMapper.insert(logRecord);
  }

  private void recordLifecycleStatusChange(Long elderId, String beforeLifecycleStatus, String afterLifecycleStatus, String reason) {
    Long orgId = AuthContext.getOrgId();
    if (elderId == null || orgId == null || Objects.equals(beforeLifecycleStatus, afterLifecycleStatus)) {
      return;
    }
    ElderChangeLog logRecord = new ElderChangeLog();
    logRecord.setTenantId(orgId);
    logRecord.setOrgId(orgId);
    logRecord.setElderId(elderId);
    logRecord.setChangeType("LIFECYCLE_STATUS");
    logRecord.setBeforeValue(beforeLifecycleStatus);
    logRecord.setAfterValue(afterLifecycleStatus);
    logRecord.setReason(reason);
    logRecord.setCreatedBy(AuthContext.getStaffId());
    changeLogMapper.insert(logRecord);
  }

  private String elderStatusText(Integer status) {
    if (status == null) return "";
    return ElderStatus.text(status);
  }

  private Map<String, Object> buildStatusSnapshot(Integer status, Long bedId) {
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("elderStatus", status);
    snapshot.put("elderStatusText", elderStatusText(status));
    snapshot.put("bedId", bedId);
    return snapshot;
  }

  private Map<String, Object> buildStatusTransitionContext(Integer beforeStatus, Integer afterStatus, String reason) {
    Map<String, Object> context = new LinkedHashMap<>();
    context.put("beforeStatus", beforeStatus);
    context.put("beforeStatusText", elderStatusText(beforeStatus));
    context.put("afterStatus", afterStatus);
    context.put("afterStatusText", elderStatusText(afterStatus));
    context.put("reason", reason);
    return context;
  }

  private Map<String, Object> buildDepartureContext(
      Integer beforeStatus,
      Integer afterStatus,
      String reason,
      Long previousBedId) {
    Map<String, Object> context = buildStatusTransitionContext(beforeStatus, afterStatus, reason);
    context.put("previousBedId", previousBedId);
    context.put("bedReleased", previousBedId != null);
    return context;
  }

  private ElderTrialStay copyTrialStay(ElderTrialStay source) {
    if (source == null) {
      return null;
    }
    ElderTrialStay copy = new ElderTrialStay();
    copy.setId(source.getId());
    copy.setTenantId(source.getTenantId());
    copy.setOrgId(source.getOrgId());
    copy.setElderId(source.getElderId());
    copy.setElderName(source.getElderName());
    copy.setTrialStartDate(source.getTrialStartDate());
    copy.setTrialEndDate(source.getTrialEndDate());
    copy.setChannel(source.getChannel());
    copy.setTrialPackage(source.getTrialPackage());
    copy.setIntentLevel(source.getIntentLevel());
    copy.setStatus(source.getStatus());
    copy.setCareLevel(source.getCareLevel());
    copy.setRemark(source.getRemark());
    copy.setCreatedBy(source.getCreatedBy());
    copy.setCreateTime(source.getCreateTime());
    copy.setUpdateTime(source.getUpdateTime());
    copy.setIsDeleted(source.getIsDeleted());
    return copy;
  }

  private ElderDischargeApply copyDischargeApply(ElderDischargeApply source) {
    if (source == null) {
      return null;
    }
    ElderDischargeApply copy = new ElderDischargeApply();
    copy.setId(source.getId());
    copy.setTenantId(source.getTenantId());
    copy.setOrgId(source.getOrgId());
    copy.setElderId(source.getElderId());
    copy.setElderName(source.getElderName());
    copy.setApplyDate(source.getApplyDate());
    copy.setPlannedDischargeDate(source.getPlannedDischargeDate());
    copy.setReason(source.getReason());
    copy.setProofFileUrl(source.getProofFileUrl());
    copy.setStatus(source.getStatus());
    copy.setLinkedDischargeId(source.getLinkedDischargeId());
    copy.setAutoDischargeStatus(source.getAutoDischargeStatus());
    copy.setAutoDischargeMessage(source.getAutoDischargeMessage());
    copy.setReviewRemark(source.getReviewRemark());
    copy.setReviewedBy(source.getReviewedBy());
    copy.setReviewedByName(source.getReviewedByName());
    copy.setReviewedTime(source.getReviewedTime());
    copy.setCreatedBy(source.getCreatedBy());
    copy.setCreateTime(source.getCreateTime());
    copy.setUpdateTime(source.getUpdateTime());
    copy.setIsDeleted(source.getIsDeleted());
    return copy;
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

  private void releaseBedForDeparture(ElderProfile elder, LocalDate endDate, String reason) {
    if (elder == null || elder.getId() == null) {
      return;
    }
    BedReleaseResult releaseResult = elderOccupancyService.releaseBedAndCloseRelation(
        elder.getTenantId(), elder.getOrgId(), elder.getId(), endDate, reason);
    elder.setBedId(null);
    if (releaseResult.getPreviousBedId() != null) {
      recordBedChange(
          elder.getId(),
          releaseResult.getPreviousBedId(),
          null,
          reason + (releaseResult.getClosedRelationCount() > 0 ? "" : "（补记）"));
    }
  }

  private String resolveElderName(Long elderId) {
    return resolveElder(elderId).getFullName();
  }
}
