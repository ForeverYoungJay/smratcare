package com.zhiyangyun.care.hr.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.entity.CardAccount;
import com.zhiyangyun.care.finance.mapper.CardAccountMapper;
import com.zhiyangyun.care.hr.model.HrAccessControlRecordResponse;
import com.zhiyangyun.care.hr.model.HrCardSyncItemResponse;
import com.zhiyangyun.care.hr.entity.StaffProfile;
import com.zhiyangyun.care.hr.entity.StaffTrainingRecord;
import com.zhiyangyun.care.hr.model.HrExpenseItemResponse;
import com.zhiyangyun.care.hr.mapper.StaffProfileMapper;
import com.zhiyangyun.care.hr.mapper.StaffTrainingRecordMapper;
import com.zhiyangyun.care.hr.model.HrAttendanceAbnormalResponse;
import com.zhiyangyun.care.hr.model.HrAttendanceRecordResponse;
import com.zhiyangyun.care.hr.model.HrGenericApprovalResponse;
import com.zhiyangyun.care.hr.model.HrContractReminderResponse;
import com.zhiyangyun.care.hr.model.HrExpenseApprovalRequest;
import com.zhiyangyun.care.hr.model.HrPolicyAlertResponse;
import com.zhiyangyun.care.hr.model.HrProfileContractResponse;
import com.zhiyangyun.care.hr.model.HrProfileDocumentRequest;
import com.zhiyangyun.care.hr.model.HrRecruitmentNeedRequest;
import com.zhiyangyun.care.hr.model.HrRecruitmentNeedResponse;
import com.zhiyangyun.care.hr.model.HrStaffCertificateRequest;
import com.zhiyangyun.care.hr.model.HrStaffCertificateResponse;
import com.zhiyangyun.care.hr.model.HrWorkbenchSummaryResponse;
import com.zhiyangyun.care.hr.model.StaffPerformanceRankItem;
import com.zhiyangyun.care.hr.model.StaffPerformanceSummaryResponse;
import com.zhiyangyun.care.hr.model.StaffPointsAdjustRequest;
import com.zhiyangyun.care.hr.model.StaffPointsAccountResponse;
import com.zhiyangyun.care.hr.model.StaffPointsLogResponse;
import com.zhiyangyun.care.hr.model.StaffProfileRequest;
import com.zhiyangyun.care.hr.model.StaffProfileResponse;
import com.zhiyangyun.care.hr.model.StaffTrainingRequest;
import com.zhiyangyun.care.hr.model.StaffTrainingResponse;
import com.zhiyangyun.care.hr.service.HrPerformanceService;
import com.zhiyangyun.care.hr.service.StaffPointsService;
import com.zhiyangyun.care.hr.model.StaffPointsRuleRequest;
import com.zhiyangyun.care.hr.model.StaffPointsRuleResponse;
import com.zhiyangyun.care.hr.service.StaffPointsRuleService;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.entity.OaDocument;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.oa.mapper.OaDocumentMapper;
import com.zhiyangyun.care.schedule.entity.AttendanceRecord;
import com.zhiyangyun.care.schedule.mapper.AttendanceRecordMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
@RequestMapping("/api/admin/hr")
public class AdminHrController {
  private final StaffProfileMapper staffProfileMapper;
  private final StaffTrainingRecordMapper trainingRecordMapper;
  private final StaffMapper staffMapper;
  private final StaffPointsService staffPointsService;
  private final StaffPointsRuleService staffPointsRuleService;
  private final HrPerformanceService performanceService;
  private final AuditLogService auditLogService;
  private final OaApprovalMapper oaApprovalMapper;
  private final OaDocumentMapper oaDocumentMapper;
  private final AttendanceRecordMapper attendanceRecordMapper;
  private final CrmContractMapper crmContractMapper;
  private final CardAccountMapper cardAccountMapper;
  private final ElderMapper elderMapper;
  private final ObjectMapper objectMapper;

  public AdminHrController(StaffProfileMapper staffProfileMapper,
      StaffTrainingRecordMapper trainingRecordMapper,
      StaffMapper staffMapper,
      StaffPointsService staffPointsService,
      StaffPointsRuleService staffPointsRuleService,
      HrPerformanceService performanceService,
      AuditLogService auditLogService,
      OaApprovalMapper oaApprovalMapper,
      OaDocumentMapper oaDocumentMapper,
      AttendanceRecordMapper attendanceRecordMapper,
      CrmContractMapper crmContractMapper,
      CardAccountMapper cardAccountMapper,
      ElderMapper elderMapper,
      ObjectMapper objectMapper) {
    this.staffProfileMapper = staffProfileMapper;
    this.trainingRecordMapper = trainingRecordMapper;
    this.staffMapper = staffMapper;
    this.staffPointsService = staffPointsService;
    this.staffPointsRuleService = staffPointsRuleService;
    this.performanceService = performanceService;
    this.auditLogService = auditLogService;
    this.oaApprovalMapper = oaApprovalMapper;
    this.oaDocumentMapper = oaDocumentMapper;
    this.attendanceRecordMapper = attendanceRecordMapper;
    this.crmContractMapper = crmContractMapper;
    this.cardAccountMapper = cardAccountMapper;
    this.elderMapper = elderMapper;
    this.objectMapper = objectMapper;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/profile")
  public Result<StaffProfileResponse> upsertProfile(@RequestBody StaffProfileRequest request) {
    Long orgId = AuthContext.getOrgId();
    if (request.getStaffId() == null) {
      return Result.error(400, "staffId required");
    }
    StaffAccount staff = staffMapper.selectById(request.getStaffId());
    if (staff == null) {
      return Result.error(404, "Staff not found");
    }
    StaffProfile profile = staffProfileMapper.selectOne(
        Wrappers.lambdaQuery(StaffProfile.class)
            .eq(StaffProfile::getOrgId, orgId)
            .eq(StaffProfile::getStaffId, request.getStaffId())
            .eq(StaffProfile::getIsDeleted, 0));
    if (profile == null) {
      profile = new StaffProfile();
      profile.setOrgId(orgId);
      profile.setStaffId(request.getStaffId());
    }
    if (request.getJobTitle() != null) {
      profile.setJobTitle(request.getJobTitle());
    }
    if (request.getEmploymentType() != null) {
      profile.setEmploymentType(request.getEmploymentType());
    }
    if (request.getHireDate() != null) {
      profile.setHireDate(request.getHireDate());
    }
    if (request.getQualificationLevel() != null) {
      profile.setQualificationLevel(request.getQualificationLevel());
    }
    if (request.getCertificateNo() != null) {
      profile.setCertificateNo(request.getCertificateNo());
    }
    if (request.getEmergencyContactName() != null) {
      profile.setEmergencyContactName(request.getEmergencyContactName());
    }
    if (request.getEmergencyContactPhone() != null) {
      profile.setEmergencyContactPhone(request.getEmergencyContactPhone());
    }
    if (request.getStatus() != null) {
      profile.setStatus(request.getStatus());
      staff.setStatus(request.getStatus());
      staffMapper.updateById(staff);
    }
    if (request.getLeaveDate() != null) {
      profile.setLeaveDate(request.getLeaveDate());
    }
    if (request.getLeaveReason() != null) {
      profile.setLeaveReason(request.getLeaveReason());
    }
    if (request.getRemark() != null) {
      profile.setRemark(request.getRemark());
    }

    if (profile.getId() == null) {
      if (profile.getStatus() == null) {
        profile.setStatus(1);
      }
      staffProfileMapper.insert(profile);
    } else {
      staffProfileMapper.updateById(profile);
    }

    return Result.ok(toProfileResponse(staff, profile));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/profile/{staffId}")
  public Result<StaffProfileResponse> getProfile(@PathVariable Long staffId) {
    Long orgId = AuthContext.getOrgId();
    StaffProfile profile = staffProfileMapper.selectOne(
        Wrappers.lambdaQuery(StaffProfile.class)
            .eq(StaffProfile::getOrgId, orgId)
            .eq(StaffProfile::getStaffId, staffId)
            .eq(StaffProfile::getIsDeleted, 0));
    StaffAccount staff = staffMapper.selectById(staffId);
    return Result.ok(toProfileResponse(staff, profile));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/staff/page")
  public Result<IPage<StaffProfileResponse>> staffPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .eq(status != null, StaffAccount::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(StaffAccount::getRealName, keyword)
          .or().like(StaffAccount::getStaffNo, keyword)
          .or().like(StaffAccount::getPhone, keyword));
    }
    IPage<StaffAccount> page = staffMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> staffIds = page.getRecords().stream().map(StaffAccount::getId).toList();
    Map<Long, StaffProfile> profileMap = staffProfileMapper.selectList(
            Wrappers.lambdaQuery(StaffProfile.class)
                .eq(StaffProfile::getIsDeleted, 0)
                .eq(orgId != null, StaffProfile::getOrgId, orgId)
                .in(!staffIds.isEmpty(), StaffProfile::getStaffId, staffIds))
        .stream()
        .collect(Collectors.toMap(StaffProfile::getStaffId, p -> p, (a, b) -> a));

    IPage<StaffProfileResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream()
        .map(staff -> toProfileResponse(staff, profileMap.get(staff.getId())))
        .toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/workbench/summary")
  public Result<HrWorkbenchSummaryResponse> workbenchSummary(
      @RequestParam(required = false, defaultValue = "30") Integer warningDays) {
    Long orgId = AuthContext.getOrgId();
    int resolvedWarningDays = warningDays == null || warningDays < 0 ? 30 : warningDays;
    LocalDate today = LocalDate.now();
    LocalDate warningDeadline = today.plusDays(resolvedWarningDays);
    LocalDateTime dayStart = today.atStartOfDay();
    LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

    Long onJobCount = count(staffMapper.selectCount(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .eq(StaffAccount::getStatus, 1)));
    Long leftCount = count(staffMapper.selectCount(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .eq(StaffAccount::getStatus, 0)));

    Long todayTrainingCount = count(trainingRecordMapper.selectCount(Wrappers.lambdaQuery(StaffTrainingRecord.class)
        .eq(StaffTrainingRecord::getIsDeleted, 0)
        .eq(orgId != null, StaffTrainingRecord::getOrgId, orgId)
        .and(w -> w
            .le(StaffTrainingRecord::getStartDate, today)
            .and(x -> x.ge(StaffTrainingRecord::getEndDate, today).or().isNull(StaffTrainingRecord::getEndDate))
            .or()
            .eq(StaffTrainingRecord::getStartDate, today))));

    Long pendingLeaveApprovalCount = count(oaApprovalMapper.selectCount(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getStatus, "PENDING")
        .eq(OaApproval::getApprovalType, "LEAVE")));

    Long attendanceAbnormalCount = count(attendanceRecordMapper.selectCount(Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getIsDeleted, 0)
        .eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .notIn(AttendanceRecord::getStatus, "NORMAL", "ON_TIME")
        .ge(AttendanceRecord::getCheckInTime, dayStart)
        .lt(AttendanceRecord::getCheckInTime, dayEnd)));

    Long contractExpiringCount = count(crmContractMapper.selectCount(Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getIsDeleted, 0)
        .eq(orgId != null, CrmContract::getOrgId, orgId)
        .isNotNull(CrmContract::getEffectiveTo)
        .ge(CrmContract::getEffectiveTo, today)
        .le(CrmContract::getEffectiveTo, warningDeadline)
        .ne(CrmContract::getStatus, "VOID")));

    HrWorkbenchSummaryResponse summary = new HrWorkbenchSummaryResponse();
    summary.setOnJobCount(onJobCount);
    summary.setLeftCount(leftCount);
    summary.setTodayTrainingCount(todayTrainingCount);
    summary.setPendingLeaveApprovalCount(pendingLeaveApprovalCount);
    summary.setAttendanceAbnormalCount(attendanceAbnormalCount);
    summary.setContractExpiringCount(contractExpiringCount);
    summary.setWarningDays(resolvedWarningDays);
    return Result.ok(summary);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/contract/reminder/page")
  public Result<IPage<HrContractReminderResponse>> contractReminderPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "30") Integer warningDays,
      @RequestParam(required = false, defaultValue = "false") Boolean includeExpired) {
    Long orgId = AuthContext.getOrgId();
    int resolvedWarningDays = warningDays == null || warningDays < 0 ? 30 : warningDays;
    boolean showExpired = Boolean.TRUE.equals(includeExpired);
    LocalDate today = LocalDate.now();
    LocalDate warningDeadline = today.plusDays(resolvedWarningDays);

    var wrapper = Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getIsDeleted, 0)
        .eq(orgId != null, CrmContract::getOrgId, orgId)
        .isNotNull(CrmContract::getEffectiveTo)
        .le(CrmContract::getEffectiveTo, warningDeadline)
        .ne(CrmContract::getStatus, "VOID");
    if (!showExpired) {
      wrapper.ge(CrmContract::getEffectiveTo, today);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(CrmContract::getContractNo, keyword.trim())
          .or().like(CrmContract::getElderName, keyword.trim())
          .or().like(CrmContract::getName, keyword.trim())
          .or().like(CrmContract::getPhone, keyword.trim()));
    }
    wrapper.orderByAsc(CrmContract::getEffectiveTo).orderByDesc(CrmContract::getUpdateTime);
    IPage<CrmContract> page = crmContractMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

    IPage<HrContractReminderResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(contract -> {
      HrContractReminderResponse item = new HrContractReminderResponse();
      item.setContractId(contract.getId());
      item.setContractNo(contract.getContractNo());
      item.setElderName(contract.getElderName());
      item.setContactName(contract.getName());
      item.setContactPhone(contract.getPhone());
      item.setEffectiveFrom(contract.getEffectiveFrom());
      item.setEffectiveTo(contract.getEffectiveTo());
      item.setStatus(contract.getStatus());
      item.setContractStatus(contract.getContractStatus());
      if (contract.getEffectiveTo() != null) {
        item.setRemainingDays(ChronoUnit.DAYS.between(today, contract.getEffectiveTo()));
      }
      return item;
    }).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/attendance/abnormal/page")
  public Result<IPage<HrAttendanceAbnormalResponse>> attendanceAbnormalPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    LocalDate from = parseDate(dateFrom);
    LocalDate to = parseDate(dateTo);

    var wrapper = Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getIsDeleted, 0)
        .eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .eq(staffId != null, AttendanceRecord::getStaffId, staffId)
        .eq(status != null && !status.isBlank(), AttendanceRecord::getStatus, status)
        .ge(from != null, AttendanceRecord::getCheckInTime, from == null ? null : from.atStartOfDay())
        .lt(to != null, AttendanceRecord::getCheckInTime, to == null ? null : to.plusDays(1).atStartOfDay());
    if (status == null || status.isBlank()) {
      wrapper.notIn(AttendanceRecord::getStatus, "NORMAL", "ON_TIME");
    }
    if (keyword != null && !keyword.isBlank()) {
      List<Long> staffIds = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
              .eq(StaffAccount::getIsDeleted, 0)
              .eq(orgId != null, StaffAccount::getOrgId, orgId)
              .and(w -> w.like(StaffAccount::getRealName, keyword.trim())
                  .or().like(StaffAccount::getStaffNo, keyword.trim())
                  .or().like(StaffAccount::getPhone, keyword.trim())))
          .stream()
          .map(StaffAccount::getId)
          .toList();
      if (staffIds.isEmpty()) {
        return Result.ok(new Page<>(pageNo, pageSize, 0));
      }
      wrapper.in(AttendanceRecord::getStaffId, staffIds);
    }
    wrapper.orderByDesc(AttendanceRecord::getCheckInTime);
    IPage<AttendanceRecord> page = attendanceRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIds(
            page.getRecords().stream().map(AttendanceRecord::getStaffId).distinct().toList())
        .stream()
        .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));
    IPage<HrAttendanceAbnormalResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(record -> {
      HrAttendanceAbnormalResponse item = new HrAttendanceAbnormalResponse();
      StaffAccount staff = staffMap.get(record.getStaffId());
      item.setId(record.getId());
      item.setStaffId(record.getStaffId());
      item.setStaffName(staff == null ? null : staff.getRealName());
      item.setCheckInTime(record.getCheckInTime());
      item.setCheckOutTime(record.getCheckOutTime());
      item.setStatus(record.getStatus());
      return item;
    }).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/recruitment/need/page")
  public Result<IPage<HrRecruitmentNeedResponse>> recruitmentNeedPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String scene) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getApprovalType, "HR_RECRUITMENT")
        .eq(status != null && !status.isBlank(), OaApproval::getStatus, status);
    if (scene != null && !scene.isBlank()) {
      wrapper.like(OaApproval::getFormData, "\"" + "scene" + "\":\"" + scene + "\"");
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaApproval::getTitle, keyword.trim())
          .or().like(OaApproval::getApplicantName, keyword.trim())
          .or().like(OaApproval::getRemark, keyword.trim()));
    }
    wrapper.orderByDesc(OaApproval::getCreateTime);
    IPage<OaApproval> page = oaApprovalMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

    IPage<HrRecruitmentNeedResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(this::toRecruitmentNeedResponse).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/recruitment/need")
  public Result<HrRecruitmentNeedResponse> createRecruitmentNeed(@RequestBody HrRecruitmentNeedRequest request) {
    Long orgId = AuthContext.getOrgId();
    OaApproval entity = new OaApproval();
    entity.setTenantId(orgId);
    entity.setOrgId(orgId);
    entity.setApprovalType("HR_RECRUITMENT");
    entity.setTitle(request.getTitle());
    entity.setApplicantId(AuthContext.getStaffId());
    entity.setApplicantName(AuthContext.getUsername());
    entity.setAmount(request.getHeadcount() == null ? null : java.math.BigDecimal.valueOf(request.getHeadcount()));
    entity.setStartTime(request.getRequiredDate() == null ? null : request.getRequiredDate().atStartOfDay());
    entity.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "PENDING" : request.getStatus());
    entity.setRemark(request.getRemark());
    entity.setCreatedBy(AuthContext.getStaffId());
    entity.setFormData(writeJson(Map.of(
        "positionName", empty(request.getPositionName()),
        "departmentName", empty(request.getDepartmentName()),
        "requiredDate", request.getRequiredDate() == null ? "" : request.getRequiredDate().toString(),
        "headcount", request.getHeadcount() == null ? 0 : request.getHeadcount(),
        "scene", empty(request.getScene())
    )));
    oaApprovalMapper.insert(entity);
    return Result.ok(toRecruitmentNeedResponse(entity));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/policy/page")
  public Result<IPage<OaDocument>> policyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String folder) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaDocument.class)
        .eq(OaDocument::getIsDeleted, 0)
        .eq(orgId != null, OaDocument::getOrgId, orgId)
        .eq(folder != null && !folder.isBlank(), OaDocument::getFolder, folder);
    if (folder == null || folder.isBlank()) {
      wrapper.and(w -> w.like(OaDocument::getFolder, "制度").or().like(OaDocument::getFolder, "规章"));
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaDocument::getName, keyword.trim())
          .or().like(OaDocument::getUploaderName, keyword.trim())
          .or().like(OaDocument::getRemark, keyword.trim()));
    }
    wrapper.orderByDesc(OaDocument::getUploadedAt);
    return Result.ok(oaDocumentMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/policy-alert/page")
  public Result<IPage<HrPolicyAlertResponse>> policyAlertPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "180") Integer staleDays) {
    Long orgId = AuthContext.getOrgId();
    int resolvedStaleDays = staleDays == null || staleDays < 1 ? 180 : staleDays;
    LocalDateTime threshold = LocalDateTime.now().minusDays(resolvedStaleDays);
    var wrapper = Wrappers.lambdaQuery(OaDocument.class)
        .eq(OaDocument::getIsDeleted, 0)
        .eq(orgId != null, OaDocument::getOrgId, orgId)
        .le(OaDocument::getUploadedAt, threshold)
        .and(w -> w.like(OaDocument::getFolder, "制度").or().like(OaDocument::getFolder, "规章"));
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaDocument::getName, keyword.trim())
          .or().like(OaDocument::getUploaderName, keyword.trim())
          .or().like(OaDocument::getFolder, keyword.trim()));
    }
    wrapper.orderByAsc(OaDocument::getUploadedAt);
    IPage<OaDocument> page = oaDocumentMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    IPage<HrPolicyAlertResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(this::toPolicyAlertResponse).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/profile/contract/page")
  public Result<IPage<HrProfileContractResponse>> profileContractPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    LocalDate from = parseDate(dateFrom);
    LocalDate to = parseDate(dateTo);
    LocalDate today = LocalDate.now();
    var wrapper = Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getIsDeleted, 0)
        .eq(orgId != null, CrmContract::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), CrmContract::getStatus, status)
        .ge(from != null, CrmContract::getEffectiveTo, from)
        .le(to != null, CrmContract::getEffectiveTo, to);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(CrmContract::getContractNo, keyword.trim())
          .or().like(CrmContract::getElderName, keyword.trim())
          .or().like(CrmContract::getName, keyword.trim())
          .or().like(CrmContract::getPhone, keyword.trim()));
    }
    wrapper.orderByAsc(CrmContract::getEffectiveTo).orderByDesc(CrmContract::getUpdateTime);
    IPage<CrmContract> page = crmContractMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    IPage<HrProfileContractResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> toProfileContractResponse(item, today)).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/profile/template/page")
  public Result<IPage<OaDocument>> profileTemplatePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(queryProfileDocuments("合同模板", pageNo, pageSize, keyword));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/profile/template")
  public Result<OaDocument> createProfileTemplate(@RequestBody HrProfileDocumentRequest request) {
    return Result.ok(createProfileDocument("合同模板", request));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/profile/attachment/page")
  public Result<IPage<OaDocument>> profileAttachmentPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(queryProfileDocuments("档案附件", pageNo, pageSize, keyword));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/profile/attachment")
  public Result<OaDocument> createProfileAttachment(@RequestBody HrProfileDocumentRequest request) {
    return Result.ok(createProfileDocument("档案附件", request));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/profile/certificate/page")
  public Result<IPage<HrStaffCertificateResponse>> profileCertificatePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String expiryFrom,
      @RequestParam(required = false) String expiryTo) {
    Long orgId = AuthContext.getOrgId();
    LocalDate from = parseDate(expiryFrom);
    LocalDate to = parseDate(expiryTo);
    var wrapper = Wrappers.lambdaQuery(StaffTrainingRecord.class)
        .eq(StaffTrainingRecord::getIsDeleted, 0)
        .eq(orgId != null, StaffTrainingRecord::getOrgId, orgId)
        .eq(staffId != null, StaffTrainingRecord::getStaffId, staffId)
        .eq(StaffTrainingRecord::getTrainingType, "CERTIFICATE")
        .ge(from != null, StaffTrainingRecord::getEndDate, from)
        .le(to != null, StaffTrainingRecord::getEndDate, to);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(StaffTrainingRecord::getTrainingName, keyword.trim())
          .or().like(StaffTrainingRecord::getCertificateNo, keyword.trim())
          .or().like(StaffTrainingRecord::getProvider, keyword.trim())
          .or().like(StaffTrainingRecord::getRemark, keyword.trim()));
    }
    wrapper.orderByAsc(StaffTrainingRecord::getEndDate).orderByDesc(StaffTrainingRecord::getUpdateTime);
    IPage<StaffTrainingRecord> page = trainingRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return Result.ok(toCertificatePage(page, LocalDate.now()));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/profile/certificate")
  public Result<HrStaffCertificateResponse> createProfileCertificate(@RequestBody HrStaffCertificateRequest request) {
    Long orgId = AuthContext.getOrgId();
    StaffTrainingRecord record = new StaffTrainingRecord();
    record.setOrgId(orgId);
    record.setStaffId(request.getStaffId());
    record.setTrainingType("CERTIFICATE");
    record.setTrainingName(request.getCertificateName());
    record.setCertificateNo(request.getCertificateNo());
    record.setProvider(request.getIssuingAuthority());
    record.setStartDate(request.getIssueDate());
    record.setEndDate(request.getExpiryDate());
    record.setStatus(1);
    record.setRemark(request.getRemark());
    trainingRecordMapper.insert(record);
    IPage<StaffTrainingRecord> page = new Page<>(1, 1, 1);
    page.setRecords(List.of(record));
    IPage<HrStaffCertificateResponse> converted = toCertificatePage(page, LocalDate.now());
    return Result.ok(converted.getRecords().isEmpty() ? null : converted.getRecords().get(0));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/profile/certificate/reminder/page")
  public Result<IPage<HrStaffCertificateResponse>> profileCertificateReminderPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "30") Integer warningDays,
      @RequestParam(required = false, defaultValue = "false") Boolean includeExpired) {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    int resolvedWarningDays = warningDays == null || warningDays < 0 ? 30 : warningDays;
    LocalDate deadline = today.plusDays(resolvedWarningDays);
    boolean showExpired = Boolean.TRUE.equals(includeExpired);
    var wrapper = Wrappers.lambdaQuery(StaffTrainingRecord.class)
        .eq(StaffTrainingRecord::getIsDeleted, 0)
        .eq(orgId != null, StaffTrainingRecord::getOrgId, orgId)
        .eq(StaffTrainingRecord::getTrainingType, "CERTIFICATE")
        .isNotNull(StaffTrainingRecord::getEndDate)
        .le(StaffTrainingRecord::getEndDate, deadline);
    if (!showExpired) {
      wrapper.ge(StaffTrainingRecord::getEndDate, today);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(StaffTrainingRecord::getTrainingName, keyword.trim())
          .or().like(StaffTrainingRecord::getCertificateNo, keyword.trim())
          .or().like(StaffTrainingRecord::getProvider, keyword.trim())
          .or().like(StaffTrainingRecord::getRemark, keyword.trim()));
    }
    wrapper.orderByAsc(StaffTrainingRecord::getEndDate).orderByDesc(StaffTrainingRecord::getUpdateTime);
    IPage<StaffTrainingRecord> page = trainingRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return Result.ok(toCertificatePage(page, today));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/integration/access-control/page")
  public Result<IPage<HrAccessControlRecordResponse>> integrationAccessControlPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    LocalDate from = parseDate(dateFrom);
    LocalDate to = parseDate(dateTo);
    var wrapper = Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getIsDeleted, 0)
        .eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), AttendanceRecord::getStatus, status)
        .ge(from != null, AttendanceRecord::getCheckInTime, from == null ? null : from.atStartOfDay())
        .lt(to != null, AttendanceRecord::getCheckInTime, to == null ? null : to.plusDays(1).atStartOfDay());
    if (keyword != null && !keyword.isBlank()) {
      List<Long> staffIds = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
              .eq(StaffAccount::getIsDeleted, 0)
              .eq(orgId != null, StaffAccount::getOrgId, orgId)
              .and(w -> w.like(StaffAccount::getRealName, keyword.trim())
                  .or().like(StaffAccount::getStaffNo, keyword.trim())
                  .or().like(StaffAccount::getPhone, keyword.trim())))
          .stream()
          .map(StaffAccount::getId)
          .toList();
      if (staffIds.isEmpty()) {
        return Result.ok(new Page<>(pageNo, pageSize, 0));
      }
      wrapper.in(AttendanceRecord::getStaffId, staffIds);
    }
    wrapper.orderByDesc(AttendanceRecord::getCheckInTime);
    IPage<AttendanceRecord> page = attendanceRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIds(
            page.getRecords().stream().map(AttendanceRecord::getStaffId).distinct().toList())
        .stream()
        .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));
    IPage<HrAccessControlRecordResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> {
      HrAccessControlRecordResponse row = new HrAccessControlRecordResponse();
      StaffAccount staff = staffMap.get(item.getStaffId());
      row.setId(item.getId());
      row.setStaffId(item.getStaffId());
      row.setStaffName(staff == null ? null : staff.getRealName());
      row.setCheckInTime(item.getCheckInTime());
      row.setCheckOutTime(item.getCheckOutTime());
      row.setAttendanceStatus(item.getStatus());
      row.setAccessResult(resolveAccessResult(item.getStatus()));
      return row;
    }).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/integration/card-sync/page")
  public Result<IPage<HrCardSyncItemResponse>> integrationCardSyncPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(CardAccount.class)
        .eq(CardAccount::getIsDeleted, 0)
        .eq(orgId != null, CardAccount::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), CardAccount::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      List<Long> elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
              .eq(ElderProfile::getIsDeleted, 0)
              .eq(orgId != null, ElderProfile::getOrgId, orgId)
              .like(ElderProfile::getFullName, keyword.trim()))
          .stream()
          .map(ElderProfile::getId)
          .toList();
      if (elderIds.isEmpty()) {
        wrapper.like(CardAccount::getCardNo, keyword.trim());
      } else {
        wrapper.and(w -> w.like(CardAccount::getCardNo, keyword.trim())
            .or().in(CardAccount::getElderId, elderIds));
      }
    }
    wrapper.orderByDesc(CardAccount::getUpdateTime);
    IPage<CardAccount> page = cardAccountMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, ElderProfile> elderMap = elderMapper.selectBatchIds(
            page.getRecords().stream().map(CardAccount::getElderId).filter(x -> x != null).distinct().toList())
        .stream()
        .collect(Collectors.toMap(ElderProfile::getId, e -> e, (a, b) -> a));
    IPage<HrCardSyncItemResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> {
      HrCardSyncItemResponse row = new HrCardSyncItemResponse();
      ElderProfile elder = elderMap.get(item.getElderId());
      row.setCardAccountId(item.getId());
      row.setElderId(item.getElderId());
      row.setElderName(elder == null ? null : elder.getFullName());
      row.setCardNo(item.getCardNo());
      row.setCardStatus(item.getStatus());
      row.setLossFlag(item.getLossFlag());
      row.setOpenTime(item.getOpenTime());
      row.setLastRechargeTime(item.getLastRechargeTime());
      row.setRemark(item.getRemark());
      row.setSyncStatus(resolveCardSyncStatus(item));
      return row;
    }).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/expense/meal-fee/page")
  public Result<IPage<HrExpenseItemResponse>> expenseMealFeePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageExpenseByKeyword(pageNo, pageSize, keyword, status, "餐费", "meal"));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/expense/electricity-fee/page")
  public Result<IPage<HrExpenseItemResponse>> expenseElectricityFeePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageExpenseByKeyword(pageNo, pageSize, keyword, status, "电费", "electricity"));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/expense/training-reimburse/page")
  public Result<IPage<HrExpenseItemResponse>> expenseTrainingReimbursePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageExpenseByKeyword(pageNo, pageSize, keyword, status, "培训", "training"));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/expense/subsidy/page")
  public Result<IPage<HrExpenseItemResponse>> expenseSubsidyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageExpenseByKeyword(pageNo, pageSize, keyword, status, "补贴", "subsidy"));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/expense/salary-subsidy/page")
  public Result<IPage<HrExpenseItemResponse>> expenseSalarySubsidyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageExpenseByKeyword(pageNo, pageSize, keyword, status, "工资补贴", "salary"));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/expense/approval-flow/page")
  public Result<IPage<HrExpenseItemResponse>> expenseApprovalFlowPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getApprovalType, "REIMBURSE")
        .eq(status != null && !status.isBlank(), OaApproval::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaApproval::getTitle, keyword.trim())
          .or().like(OaApproval::getApplicantName, keyword.trim())
          .or().like(OaApproval::getRemark, keyword.trim()));
    }
    wrapper.orderByDesc(OaApproval::getCreateTime);
    IPage<OaApproval> page = oaApprovalMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    IPage<HrExpenseItemResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> {
      HrExpenseItemResponse row = new HrExpenseItemResponse();
      row.setId(item.getId());
      row.setExpenseType(resolveExpenseType(item));
      row.setTitle(item.getTitle());
      row.setApplicantName(item.getApplicantName());
      row.setAmount(item.getAmount());
      row.setStatus(item.getStatus());
      row.setApplyStartTime(item.getStartTime());
      row.setApplyEndTime(item.getEndTime());
      row.setCreateTime(item.getCreateTime());
      row.setRemark(item.getRemark());
      return row;
    }).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/expense/approval-flow")
  public Result<HrExpenseItemResponse> createExpenseApprovalFlow(@RequestBody HrExpenseApprovalRequest request) {
    if (request == null || request.getAmount() == null || request.getAmount().signum() <= 0) {
      return Result.error(400, "amount required");
    }
    Long orgId = AuthContext.getOrgId();
    OaApproval entity = new OaApproval();
    entity.setTenantId(orgId);
    entity.setOrgId(orgId);
    entity.setApprovalType("REIMBURSE");
    entity.setTitle(request.getTitle());
    entity.setApplicantId(AuthContext.getStaffId());
    entity.setApplicantName(AuthContext.getUsername());
    entity.setAmount(request.getAmount());
    entity.setStartTime(request.getApplyStartTime());
    entity.setEndTime(request.getApplyEndTime());
    entity.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "PENDING" : request.getStatus());
    entity.setRemark(request.getRemark());
    entity.setCreatedBy(AuthContext.getStaffId());
    Map<String, Object> ext = new HashMap<>();
    ext.put("scene", request.getScene() == null ? "" : request.getScene());
    ext.put("expenseType", request.getExpenseType() == null ? "" : request.getExpenseType());
    entity.setFormData(writeJson(ext));
    if (entity.getTitle() == null || entity.getTitle().isBlank()) {
      entity.setTitle((request.getExpenseType() == null || request.getExpenseType().isBlank() ? "报销" : request.getExpenseType()) + "申请");
    }
    oaApprovalMapper.insert(entity);

    HrExpenseItemResponse row = new HrExpenseItemResponse();
    row.setId(entity.getId());
    row.setExpenseType(request.getExpenseType());
    row.setTitle(entity.getTitle());
    row.setApplicantName(entity.getApplicantName());
    row.setAmount(entity.getAmount());
    row.setStatus(entity.getStatus());
    row.setApplyStartTime(entity.getStartTime());
    row.setApplyEndTime(entity.getEndTime());
    row.setCreateTime(entity.getCreateTime());
    row.setRemark(entity.getRemark());
    return Result.ok(row);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/attendance/leave/page")
  public Result<IPage<HrGenericApprovalResponse>> attendanceLeavePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageGenericApprovals(pageNo, pageSize, keyword, status, "LEAVE", null));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/attendance/shift-change/page")
  public Result<IPage<HrGenericApprovalResponse>> attendanceShiftChangePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageGenericApprovals(pageNo, pageSize, keyword, status, "LEAVE", "shift-change"));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/attendance/overtime/page")
  public Result<IPage<HrGenericApprovalResponse>> attendanceOvertimePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageGenericApprovals(pageNo, pageSize, keyword, status, "OVERTIME", null));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/attendance/record/page")
  public Result<IPage<HrAttendanceRecordResponse>> attendanceRecordPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    LocalDate from = parseDate(dateFrom);
    LocalDate to = parseDate(dateTo);
    var wrapper = Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getIsDeleted, 0)
        .eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), AttendanceRecord::getStatus, status)
        .ge(from != null, AttendanceRecord::getCheckInTime, from == null ? null : from.atStartOfDay())
        .lt(to != null, AttendanceRecord::getCheckInTime, to == null ? null : to.plusDays(1).atStartOfDay());
    if (keyword != null && !keyword.isBlank()) {
      List<Long> staffIds = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
              .eq(StaffAccount::getIsDeleted, 0)
              .eq(orgId != null, StaffAccount::getOrgId, orgId)
              .and(w -> w.like(StaffAccount::getRealName, keyword.trim())
                  .or().like(StaffAccount::getStaffNo, keyword.trim())
                  .or().like(StaffAccount::getPhone, keyword.trim())))
          .stream()
          .map(StaffAccount::getId)
          .toList();
      if (staffIds.isEmpty()) {
        return Result.ok(new Page<>(pageNo, pageSize, 0));
      }
      wrapper.in(AttendanceRecord::getStaffId, staffIds);
    }
    wrapper.orderByDesc(AttendanceRecord::getCheckInTime);
    IPage<AttendanceRecord> page = attendanceRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIds(
            page.getRecords().stream().map(AttendanceRecord::getStaffId).distinct().toList())
        .stream()
        .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));
    IPage<HrAttendanceRecordResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> {
      HrAttendanceRecordResponse row = new HrAttendanceRecordResponse();
      StaffAccount staff = staffMap.get(item.getStaffId());
      row.setId(item.getId());
      row.setStaffId(item.getStaffId());
      row.setStaffName(staff == null ? null : staff.getRealName());
      row.setCheckInTime(item.getCheckInTime());
      row.setCheckOutTime(item.getCheckOutTime());
      row.setStatus(item.getStatus());
      row.setAbnormal(!"NORMAL".equalsIgnoreCase(item.getStatus()) && !"ON_TIME".equalsIgnoreCase(item.getStatus()));
      return row;
    }).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/terminate")
  public Result<Void> terminate(@RequestParam Long staffId,
      @RequestParam(required = false) String leaveDate,
      @RequestParam(required = false) String leaveReason) {
    Long orgId = AuthContext.getOrgId();
    StaffAccount staff = staffMapper.selectById(staffId);
    if (staff == null) {
      return Result.error(404, "Staff not found");
    }
    staff.setStatus(0);
    staffMapper.updateById(staff);

    StaffProfile profile = staffProfileMapper.selectOne(
        Wrappers.lambdaQuery(StaffProfile.class)
            .eq(StaffProfile::getOrgId, orgId)
            .eq(StaffProfile::getStaffId, staffId)
            .eq(StaffProfile::getIsDeleted, 0));
    if (profile == null) {
      profile = new StaffProfile();
      profile.setOrgId(orgId);
      profile.setStaffId(staffId);
      profile.setStatus(0);
    }
    profile.setStatus(0);
    if (leaveDate != null && !leaveDate.isBlank()) {
      profile.setLeaveDate(parseDate(leaveDate));
    }
    if (leaveReason != null) {
      profile.setLeaveReason(leaveReason);
    }
    if (profile.getId() == null) {
      staffProfileMapper.insert(profile);
    } else {
      staffProfileMapper.updateById(profile);
    }
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/reinstate")
  public Result<Void> reinstate(@RequestParam Long staffId) {
    Long orgId = AuthContext.getOrgId();
    StaffAccount staff = staffMapper.selectById(staffId);
    if (staff == null) {
      return Result.error(404, "Staff not found");
    }
    staff.setStatus(1);
    staffMapper.updateById(staff);

    StaffProfile profile = staffProfileMapper.selectOne(
        Wrappers.lambdaQuery(StaffProfile.class)
            .eq(StaffProfile::getOrgId, orgId)
            .eq(StaffProfile::getStaffId, staffId)
            .eq(StaffProfile::getIsDeleted, 0));
    if (profile == null) {
      profile = new StaffProfile();
      profile.setOrgId(orgId);
      profile.setStaffId(staffId);
    }
    profile.setStatus(1);
    profile.setLeaveDate(null);
    profile.setLeaveReason(null);
    if (profile.getId() == null) {
      staffProfileMapper.insert(profile);
    } else {
      staffProfileMapper.updateById(profile);
    }
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/training")
  public Result<StaffTrainingRecord> createTraining(@RequestBody StaffTrainingRequest request) {
    Long orgId = AuthContext.getOrgId();
    StaffTrainingRecord record = new StaffTrainingRecord();
    record.setOrgId(orgId);
    record.setStaffId(request.getStaffId());
    record.setTrainingName(request.getTrainingName());
    record.setTrainingType(request.getTrainingType());
    record.setProvider(request.getProvider());
    record.setStartDate(request.getStartDate());
    record.setEndDate(request.getEndDate());
    record.setHours(request.getHours());
    record.setScore(request.getScore());
    record.setStatus(request.getStatus());
    record.setCertificateNo(request.getCertificateNo());
    record.setRemark(request.getRemark());
    if (record.getStatus() == null) {
      record.setStatus(1);
    }
    trainingRecordMapper.insert(record);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "STAFF_TRAINING", record.getId(), "新增培训记录");
    return Result.ok(record);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/training/{id}")
  public Result<StaffTrainingRecord> updateTraining(@PathVariable Long id,
      @RequestBody StaffTrainingRequest request) {
    StaffTrainingRecord record = trainingRecordMapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (record == null || (orgId != null && !orgId.equals(record.getOrgId()))) {
      return Result.error(404, "Training record not found");
    }
    if (request.getStaffId() != null) {
      record.setStaffId(request.getStaffId());
    }
    if (request.getTrainingName() != null) {
      record.setTrainingName(request.getTrainingName());
    }
    if (request.getTrainingType() != null) {
      record.setTrainingType(request.getTrainingType());
    }
    if (request.getProvider() != null) {
      record.setProvider(request.getProvider());
    }
    if (request.getStartDate() != null) {
      record.setStartDate(request.getStartDate());
    }
    if (request.getEndDate() != null) {
      record.setEndDate(request.getEndDate());
    }
    if (request.getHours() != null) {
      record.setHours(request.getHours());
    }
    if (request.getScore() != null) {
      record.setScore(request.getScore());
    }
    if (request.getStatus() != null) {
      record.setStatus(request.getStatus());
    }
    if (request.getCertificateNo() != null) {
      record.setCertificateNo(request.getCertificateNo());
    }
    if (request.getRemark() != null) {
      record.setRemark(request.getRemark());
    }
    trainingRecordMapper.updateById(record);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "STAFF_TRAINING", record.getId(), "更新培训记录");
    return Result.ok(record);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/training/{id}")
  public Result<Void> deleteTraining(@PathVariable Long id) {
    StaffTrainingRecord record = trainingRecordMapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (record == null || (orgId != null && !orgId.equals(record.getOrgId()))) {
      return Result.error(404, "Training record not found");
    }
    record.setIsDeleted(1);
    trainingRecordMapper.updateById(record);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "STAFF_TRAINING", record.getId(), "删除培训记录");
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/training/page")
  public Result<IPage<StaffTrainingResponse>> trainingPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(StaffTrainingRecord.class)
        .eq(StaffTrainingRecord::getIsDeleted, 0)
        .eq(orgId != null, StaffTrainingRecord::getOrgId, orgId)
        .eq(staffId != null, StaffTrainingRecord::getStaffId, staffId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(StaffTrainingRecord::getTrainingName, keyword)
          .or().like(StaffTrainingRecord::getProvider, keyword));
    }
    if (dateFrom != null && !dateFrom.isBlank()) {
      LocalDate start = parseDate(dateFrom);
      wrapper.ge(start != null, StaffTrainingRecord::getStartDate, start);
    }
    if (dateTo != null && !dateTo.isBlank()) {
      LocalDate end = parseDate(dateTo);
      wrapper.le(end != null, StaffTrainingRecord::getEndDate, end);
    }
    wrapper.orderByDesc(StaffTrainingRecord::getCreateTime);
    IPage<StaffTrainingRecord> page = trainingRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIds(
            page.getRecords().stream().map(StaffTrainingRecord::getStaffId).distinct().toList())
        .stream()
        .collect(Collectors.toMap(StaffAccount::getId, s -> s));

    IPage<StaffTrainingResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(record -> {
      StaffTrainingResponse item = new StaffTrainingResponse();
      StaffAccount staff = staffMap.get(record.getStaffId());
      item.setId(record.getId());
      item.setStaffId(record.getStaffId());
      item.setStaffName(staff == null ? null : staff.getRealName());
      item.setTrainingName(record.getTrainingName());
      item.setTrainingType(record.getTrainingType());
      item.setProvider(record.getProvider());
      item.setStartDate(record.getStartDate());
      item.setEndDate(record.getEndDate());
      item.setHours(record.getHours());
      item.setScore(record.getScore());
      item.setStatus(record.getStatus());
      item.setCertificateNo(record.getCertificateNo());
      item.setRemark(record.getRemark());
      item.setCreateTime(record.getCreateTime());
      return item;
    }).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/points/adjust")
  public Result<StaffPointsAccountResponse> adjustPoints(@RequestBody StaffPointsAdjustRequest request) {
    if (request.getStaffId() == null || request.getChangePoints() == null) {
      return Result.error(400, "staffId and changePoints required");
    }
    StaffPointsAccountResponse response = staffPointsService.adjust(
        AuthContext.getOrgId(), AuthContext.getStaffId(), request);
    if (response == null) {
      return Result.error(400, "insufficient points");
    }
    auditLogService.record(AuthContext.getOrgId(), AuthContext.getOrgId(), AuthContext.getStaffId(), AuthContext.getUsername(),
        "ADJUST", "STAFF_POINTS", request.getStaffId(), "积分调整");
    return Result.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/points/rule/page")
  public Result<IPage<StaffPointsRuleResponse>> rulePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize) {
    return Result.ok(staffPointsRuleService.page(AuthContext.getOrgId(), pageNo, pageSize));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/points/rule")
  public Result<StaffPointsRuleResponse> upsertRule(@RequestBody StaffPointsRuleRequest request) {
    return Result.ok(staffPointsRuleService.upsert(AuthContext.getOrgId(), request));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/points/rule/{id}")
  public Result<Void> deleteRule(@PathVariable Long id) {
    staffPointsRuleService.delete(AuthContext.getOrgId(), id);
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/points/log/page")
  public Result<IPage<StaffPointsLogResponse>> pointsLogPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String sourceType) {
    return Result.ok(
        staffPointsService.pageLogs(
            AuthContext.getOrgId(), pageNo, pageSize, staffId, dateFrom, dateTo, sourceType));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/performance/summary")
  public Result<StaffPerformanceSummaryResponse> performanceSummary(
      @RequestParam Long staffId,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    return Result.ok(performanceService.summary(AuthContext.getOrgId(), staffId, dateFrom, dateTo));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/performance/ranking")
  public Result<List<StaffPerformanceRankItem>> performanceRanking(
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String staffCategory) {
    return Result.ok(performanceService.ranking(AuthContext.getOrgId(), dateFrom, dateTo, sortBy, staffCategory));
  }

  private Long count(Long value) {
    return value == null ? 0L : value;
  }

  private HrRecruitmentNeedResponse toRecruitmentNeedResponse(OaApproval item) {
    Map<String, Object> ext = parseJson(item == null ? null : item.getFormData());
    HrRecruitmentNeedResponse response = new HrRecruitmentNeedResponse();
    response.setId(item == null ? null : item.getId());
    response.setTitle(item == null ? null : item.getTitle());
    response.setStatus(item == null ? null : item.getStatus());
    response.setRemark(item == null ? null : item.getRemark());
    response.setApplicantName(item == null ? null : item.getApplicantName());
    response.setCreateTime(item == null ? null : item.getCreateTime());
    response.setHeadcount(item == null || item.getAmount() == null ? asInt(ext.get("headcount")) : item.getAmount().intValue());
    response.setPositionName(asString(ext.get("positionName")));
    response.setDepartmentName(asString(ext.get("departmentName")));
    response.setScene(asString(ext.get("scene")));
    LocalDate requiredDate = parseDate(asString(ext.get("requiredDate")));
    if (requiredDate == null && item != null && item.getStartTime() != null) {
      requiredDate = item.getStartTime().toLocalDate();
    }
    response.setRequiredDate(requiredDate);
    return response;
  }

  private HrPolicyAlertResponse toPolicyAlertResponse(OaDocument item) {
    HrPolicyAlertResponse response = new HrPolicyAlertResponse();
    response.setDocumentId(item == null ? null : item.getId());
    response.setName(item == null ? null : item.getName());
    response.setFolder(item == null ? null : item.getFolder());
    response.setUploaderName(item == null ? null : item.getUploaderName());
    response.setUploadedAt(item == null ? null : item.getUploadedAt());
    if (item != null && item.getUploadedAt() != null) {
      long days = ChronoUnit.DAYS.between(item.getUploadedAt(), LocalDateTime.now());
      response.setLastUpdatedDays(days);
      response.setAlertLevel(days > 365 ? "HIGH" : days > 180 ? "MEDIUM" : "LOW");
    } else {
      response.setLastUpdatedDays(0L);
      response.setAlertLevel("LOW");
    }
    return response;
  }

  private HrProfileContractResponse toProfileContractResponse(CrmContract item, LocalDate today) {
    HrProfileContractResponse response = new HrProfileContractResponse();
    response.setContractId(item == null ? null : item.getId());
    response.setContractNo(item == null ? null : item.getContractNo());
    response.setElderName(item == null ? null : item.getElderName());
    response.setContactName(item == null ? null : item.getName());
    response.setContactPhone(item == null ? null : item.getPhone());
    response.setStatus(item == null ? null : item.getStatus());
    response.setContractStatus(item == null ? null : item.getContractStatus());
    response.setFlowStage(item == null ? null : item.getFlowStage());
    response.setEffectiveFrom(item == null ? null : item.getEffectiveFrom());
    response.setEffectiveTo(item == null ? null : item.getEffectiveTo());
    if (item != null && item.getEffectiveTo() != null && today != null) {
      response.setRemainingDays(ChronoUnit.DAYS.between(today, item.getEffectiveTo()));
    }
    return response;
  }

  private IPage<OaDocument> queryProfileDocuments(String defaultFolder, long pageNo, long pageSize, String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaDocument.class)
        .eq(OaDocument::getIsDeleted, 0)
        .eq(orgId != null, OaDocument::getOrgId, orgId)
        .eq(OaDocument::getFolder, defaultFolder);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaDocument::getName, keyword.trim())
          .or().like(OaDocument::getUploaderName, keyword.trim())
          .or().like(OaDocument::getRemark, keyword.trim()));
    }
    wrapper.orderByDesc(OaDocument::getUploadedAt);
    return oaDocumentMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  private OaDocument createProfileDocument(String defaultFolder, HrProfileDocumentRequest request) {
    Long orgId = AuthContext.getOrgId();
    OaDocument entity = new OaDocument();
    entity.setTenantId(orgId);
    entity.setOrgId(orgId);
    entity.setName(request == null ? null : request.getName());
    entity.setFolder(request == null || request.getFolder() == null || request.getFolder().isBlank() ? defaultFolder : request.getFolder());
    entity.setUrl(request == null ? null : request.getUrl());
    entity.setSizeBytes(request == null ? null : request.getSizeBytes());
    entity.setUploaderId(AuthContext.getStaffId());
    entity.setUploaderName(request == null || request.getUploaderName() == null || request.getUploaderName().isBlank()
        ? AuthContext.getUsername() : request.getUploaderName());
    entity.setUploadedAt(LocalDateTime.now());
    entity.setRemark(request == null ? null : request.getRemark());
    entity.setCreatedBy(AuthContext.getStaffId());
    oaDocumentMapper.insert(entity);
    return entity;
  }

  private IPage<HrStaffCertificateResponse> toCertificatePage(IPage<StaffTrainingRecord> source, LocalDate today) {
    List<Long> staffIds = source.getRecords().stream().map(StaffTrainingRecord::getStaffId).distinct().toList();
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIds(staffIds)
        .stream()
        .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));
    IPage<HrStaffCertificateResponse> resp = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
    resp.setRecords(source.getRecords().stream().map(item -> {
      HrStaffCertificateResponse row = new HrStaffCertificateResponse();
      StaffAccount staff = staffMap.get(item.getStaffId());
      row.setId(item.getId());
      row.setStaffId(item.getStaffId());
      row.setStaffName(staff == null ? null : staff.getRealName());
      row.setCertificateName(item.getTrainingName());
      row.setCertificateNo(item.getCertificateNo());
      row.setIssuingAuthority(item.getProvider());
      row.setIssueDate(item.getStartDate());
      row.setExpiryDate(item.getEndDate());
      row.setStatus(item.getStatus());
      row.setRemark(item.getRemark());
      if (today != null && item.getEndDate() != null) {
        row.setRemainingDays(ChronoUnit.DAYS.between(today, item.getEndDate()));
      }
      return row;
    }).toList());
    return resp;
  }

  private String resolveAccessResult(String status) {
    if (status == null || status.isBlank()) {
      return "UNKNOWN";
    }
    if ("NORMAL".equalsIgnoreCase(status) || "ON_TIME".equalsIgnoreCase(status)) {
      return "PASS";
    }
    return "EXCEPTION";
  }

  private String resolveCardSyncStatus(CardAccount account) {
    if (account == null) {
      return "UNKNOWN";
    }
    if (account.getLossFlag() != null && account.getLossFlag() == 1) {
      return "CARD_LOST";
    }
    if ("ACTIVE".equalsIgnoreCase(account.getStatus())) {
      return "SYNCED";
    }
    return "PENDING";
  }

  private IPage<HrExpenseItemResponse> pageExpenseByKeyword(
      long pageNo, long pageSize, String keyword, String status, String titleKeyword, String remarkKeyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getApprovalType, "REIMBURSE")
        .eq(status != null && !status.isBlank(), OaApproval::getStatus, status)
        .and(w -> w.like(OaApproval::getTitle, titleKeyword)
            .or().like(OaApproval::getRemark, titleKeyword)
            .or().like(OaApproval::getRemark, remarkKeyword)
            .or().like(OaApproval::getFormData, remarkKeyword));
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaApproval::getTitle, keyword.trim())
          .or().like(OaApproval::getApplicantName, keyword.trim())
          .or().like(OaApproval::getRemark, keyword.trim()));
    }
    wrapper.orderByDesc(OaApproval::getCreateTime);
    IPage<OaApproval> page = oaApprovalMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    IPage<HrExpenseItemResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> {
      HrExpenseItemResponse row = new HrExpenseItemResponse();
      row.setId(item.getId());
      row.setExpenseType(titleKeyword);
      row.setTitle(item.getTitle());
      row.setApplicantName(item.getApplicantName());
      row.setAmount(item.getAmount());
      row.setStatus(item.getStatus());
      row.setApplyStartTime(item.getStartTime());
      row.setApplyEndTime(item.getEndTime());
      row.setCreateTime(item.getCreateTime());
      row.setRemark(item.getRemark());
      return row;
    }).toList());
    return resp;
  }

  private IPage<HrGenericApprovalResponse> pageGenericApprovals(
      long pageNo, long pageSize, String keyword, String status, String approvalType, String scene) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(approvalType != null && !approvalType.isBlank(), OaApproval::getApprovalType, approvalType)
        .eq(status != null && !status.isBlank(), OaApproval::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaApproval::getTitle, keyword.trim())
          .or().like(OaApproval::getApplicantName, keyword.trim())
          .or().like(OaApproval::getRemark, keyword.trim()));
    }
    if (scene != null && !scene.isBlank()) {
      wrapper.like(OaApproval::getFormData, "\"" + "scene" + "\":\"" + scene + "\"");
    }
    wrapper.orderByDesc(OaApproval::getCreateTime);
    IPage<OaApproval> page = oaApprovalMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    IPage<HrGenericApprovalResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> {
      HrGenericApprovalResponse row = new HrGenericApprovalResponse();
      row.setId(item.getId());
      row.setTitle(item.getTitle());
      row.setApprovalType(item.getApprovalType());
      row.setApplicantName(item.getApplicantName());
      row.setStatus(item.getStatus());
      row.setAmount(item.getAmount());
      row.setStartTime(item.getStartTime());
      row.setEndTime(item.getEndTime());
      row.setCreateTime(item.getCreateTime());
      row.setRemark(item.getRemark());
      Map<String, Object> ext = parseJson(item.getFormData());
      row.setScene(asString(ext.get("scene")));
      return row;
    }).toList());
    return resp;
  }

  private String resolveExpenseType(OaApproval item) {
    if (item == null) {
      return null;
    }
    String title = item.getTitle() == null ? "" : item.getTitle();
    String remark = item.getRemark() == null ? "" : item.getRemark();
    Map<String, Object> ext = parseJson(item.getFormData());
    String extExpenseType = asString(ext.get("expenseType"));
    if (extExpenseType != null && !extExpenseType.isBlank()) {
      return extExpenseType;
    }
    String unionText = (title + " " + remark).toLowerCase();
    if (title.contains("工资补贴") || unionText.contains("salary")) {
      return "工资补贴";
    }
    if (title.contains("补贴") || unionText.contains("subsidy")) {
      return "补贴";
    }
    if (title.contains("餐") || unionText.contains("meal")) {
      return "餐费";
    }
    if (title.contains("电") || unionText.contains("electricity")) {
      return "电费";
    }
    if (title.contains("培训") || unionText.contains("training")) {
      return "培训费用";
    }
    return "报销";
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
      return objectMapper.writeValueAsString(payload);
    } catch (Exception ex) {
      return "{}";
    }
  }

  private String asString(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  private Integer asInt(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.intValue();
    }
    try {
      return Integer.parseInt(String.valueOf(value));
    } catch (Exception ex) {
      return null;
    }
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

  private String empty(String value) {
    return value == null ? "" : value;
  }

  private StaffProfileResponse toProfileResponse(StaffAccount staff, StaffProfile profile) {
    StaffProfileResponse response = new StaffProfileResponse();
    if (staff != null) {
      response.setStaffId(staff.getId());
      response.setStaffNo(staff.getStaffNo());
      response.setRealName(staff.getRealName());
      response.setPhone(staff.getPhone());
      response.setDepartmentId(staff.getDepartmentId());
      if (profile == null || profile.getStatus() == null) {
        response.setStatus(staff.getStatus());
      }
    }
    if (profile != null) {
      response.setJobTitle(profile.getJobTitle());
      response.setEmploymentType(profile.getEmploymentType());
      response.setHireDate(profile.getHireDate());
      response.setQualificationLevel(profile.getQualificationLevel());
      response.setCertificateNo(profile.getCertificateNo());
      response.setEmergencyContactName(profile.getEmergencyContactName());
      response.setEmergencyContactPhone(profile.getEmergencyContactPhone());
      response.setStatus(profile.getStatus());
      response.setLeaveDate(profile.getLeaveDate());
      response.setLeaveReason(profile.getLeaveReason());
      response.setRemark(profile.getRemark());
      response.setUpdateTime(profile.getUpdateTime());
    }
    return response;
  }
}
