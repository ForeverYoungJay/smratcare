package com.zhiyangyun.care.hr.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Department;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.DepartmentMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.entity.CardAccount;
import com.zhiyangyun.care.finance.mapper.CardAccountMapper;
import com.zhiyangyun.care.hr.entity.HrDormitoryRoomConfig;
import com.zhiyangyun.care.hr.model.HrAccessControlRecordResponse;
import com.zhiyangyun.care.hr.model.HrCardSyncItemResponse;
import com.zhiyangyun.care.hr.entity.StaffProfile;
import com.zhiyangyun.care.hr.entity.StaffMonthlyFeeBill;
import com.zhiyangyun.care.hr.entity.StaffServicePlan;
import com.zhiyangyun.care.hr.entity.StaffTrainingRecord;
import com.zhiyangyun.care.hr.mapper.HrDormitoryRoomConfigMapper;
import com.zhiyangyun.care.hr.mapper.StaffMonthlyFeeBillMapper;
import com.zhiyangyun.care.hr.model.HrExpenseItemResponse;
import com.zhiyangyun.care.hr.mapper.StaffServicePlanMapper;
import com.zhiyangyun.care.hr.mapper.StaffProfileMapper;
import com.zhiyangyun.care.hr.mapper.StaffTrainingRecordMapper;
import com.zhiyangyun.care.auth.entity.StaffRole;
import com.zhiyangyun.care.auth.mapper.StaffRoleMapper;
import com.zhiyangyun.care.hr.model.HrAttendanceAbnormalResponse;
import com.zhiyangyun.care.hr.model.HrAttendanceRecordResponse;
import com.zhiyangyun.care.hr.model.HrBatchActionSummaryResponse;
import com.zhiyangyun.care.hr.model.HrGenericApprovalResponse;
import com.zhiyangyun.care.hr.model.HrContractReminderResponse;
import com.zhiyangyun.care.hr.model.HrSocialSecurityApplyRequest;
import com.zhiyangyun.care.hr.model.HrSocialSecurityBillGenerateRequest;
import com.zhiyangyun.care.hr.model.HrSocialSecurityCompleteRequest;
import com.zhiyangyun.care.hr.model.HrExpenseApprovalRequest;
import com.zhiyangyun.care.hr.model.HrDormitoryOverviewResponse;
import com.zhiyangyun.care.hr.model.HrDormitoryRoomConfigRequest;
import com.zhiyangyun.care.hr.model.HrDormitoryRoomConfigResponse;
import com.zhiyangyun.care.hr.model.HrDormitoryStaffResponse;
import com.zhiyangyun.care.hr.model.HrStaffElectricityImportRequest;
import com.zhiyangyun.care.hr.model.HrStaffElectricityImportRowRequest;
import com.zhiyangyun.care.hr.model.HrStaffFeeGenerateRequest;
import com.zhiyangyun.care.hr.model.HrStaffFeeSyncRequest;
import com.zhiyangyun.care.hr.model.HrStaffMonthlyFeeBillResponse;
import com.zhiyangyun.care.hr.model.HrPolicyAlertResponse;
import com.zhiyangyun.care.hr.model.HrProfileContractResponse;
import com.zhiyangyun.care.hr.model.HrProfileDocumentRequest;
import com.zhiyangyun.care.hr.model.HrRecruitmentNeedRequest;
import com.zhiyangyun.care.hr.model.HrRecruitmentNeedResponse;
import com.zhiyangyun.care.hr.model.HrSocialSecurityReminderResponse;
import com.zhiyangyun.care.hr.model.HrSocialSecuritySummaryResponse;
import com.zhiyangyun.care.hr.model.HrStaffServicePlanRequest;
import com.zhiyangyun.care.hr.model.HrStaffServicePlanResponse;
import com.zhiyangyun.care.hr.model.HrStaffBirthdayResponse;
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
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.entity.OaDocument;
import com.zhiyangyun.care.oa.entity.OaDocumentFolder;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.oa.mapper.OaDocumentMapper;
import com.zhiyangyun.care.oa.mapper.OaDocumentFolderMapper;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.schedule.entity.AttendanceRecord;
import com.zhiyangyun.care.schedule.mapper.AttendanceRecordMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
  private static final String HIDDEN_PLATFORM_USERNAME = "sysadmin_root";
  private final StaffProfileMapper staffProfileMapper;
  private final StaffTrainingRecordMapper trainingRecordMapper;
  private final StaffServicePlanMapper staffServicePlanMapper;
  private final HrDormitoryRoomConfigMapper dormitoryRoomConfigMapper;
  private final StaffMonthlyFeeBillMapper staffMonthlyFeeBillMapper;
  private final StaffMapper staffMapper;
  private final DepartmentMapper departmentMapper;
  private final StaffRoleMapper staffRoleMapper;
  private final StaffPointsService staffPointsService;
  private final StaffPointsRuleService staffPointsRuleService;
  private final HrPerformanceService performanceService;
  private final AuditLogService auditLogService;
  private final OaApprovalMapper oaApprovalMapper;
  private final OaTaskMapper oaTaskMapper;
  private final OaTodoMapper oaTodoMapper;
  private final OaDocumentMapper oaDocumentMapper;
  private final OaDocumentFolderMapper oaDocumentFolderMapper;
  private final AttendanceRecordMapper attendanceRecordMapper;
  private final CardAccountMapper cardAccountMapper;
  private final ElderMapper elderMapper;
  private final ObjectMapper objectMapper;

  public AdminHrController(StaffProfileMapper staffProfileMapper,
      StaffTrainingRecordMapper trainingRecordMapper,
      StaffServicePlanMapper staffServicePlanMapper,
      HrDormitoryRoomConfigMapper dormitoryRoomConfigMapper,
      StaffMonthlyFeeBillMapper staffMonthlyFeeBillMapper,
      StaffMapper staffMapper,
      DepartmentMapper departmentMapper,
      StaffRoleMapper staffRoleMapper,
      StaffPointsService staffPointsService,
      StaffPointsRuleService staffPointsRuleService,
      HrPerformanceService performanceService,
      AuditLogService auditLogService,
      OaApprovalMapper oaApprovalMapper,
      OaTaskMapper oaTaskMapper,
      OaTodoMapper oaTodoMapper,
      OaDocumentMapper oaDocumentMapper,
      OaDocumentFolderMapper oaDocumentFolderMapper,
      AttendanceRecordMapper attendanceRecordMapper,
      CardAccountMapper cardAccountMapper,
      ElderMapper elderMapper,
      ObjectMapper objectMapper) {
    this.staffProfileMapper = staffProfileMapper;
    this.trainingRecordMapper = trainingRecordMapper;
    this.staffServicePlanMapper = staffServicePlanMapper;
    this.dormitoryRoomConfigMapper = dormitoryRoomConfigMapper;
    this.staffMonthlyFeeBillMapper = staffMonthlyFeeBillMapper;
    this.staffMapper = staffMapper;
    this.departmentMapper = departmentMapper;
    this.staffRoleMapper = staffRoleMapper;
    this.staffPointsService = staffPointsService;
    this.staffPointsRuleService = staffPointsRuleService;
    this.performanceService = performanceService;
    this.auditLogService = auditLogService;
    this.oaApprovalMapper = oaApprovalMapper;
    this.oaTaskMapper = oaTaskMapper;
    this.oaTodoMapper = oaTodoMapper;
    this.oaDocumentMapper = oaDocumentMapper;
    this.oaDocumentFolderMapper = oaDocumentFolderMapper;
    this.attendanceRecordMapper = attendanceRecordMapper;
    this.cardAccountMapper = cardAccountMapper;
    this.elderMapper = elderMapper;
    this.objectMapper = objectMapper;
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
    profile.setJobTitle(normalizeBlank(request.getJobTitle()));
    profile.setEmploymentType(normalizeBlank(request.getEmploymentType()));
    String requestContractNo = normalizeBlank(request.getContractNo());
    if (requestContractNo != null) {
      profile.setContractNo(requestContractNo);
    } else if (normalizeBlank(profile.getContractNo()) == null) {
      profile.setContractNo(generateStaffContractNo(orgId, request.getStaffId()));
    }
    profile.setContractType(normalizeBlank(request.getContractType()));
    profile.setContractStatus(normalizeBlank(request.getContractStatus()));
    profile.setContractStartDate(request.getContractStartDate());
    profile.setContractEndDate(request.getContractEndDate());
    profile.setHireDate(request.getHireDate());
    profile.setQualificationLevel(normalizeBlank(request.getQualificationLevel()));
    profile.setCertificateNo(normalizeBlank(request.getCertificateNo()));
    profile.setEmergencyContactName(normalizeBlank(request.getEmergencyContactName()));
    profile.setEmergencyContactPhone(normalizeBlank(request.getEmergencyContactPhone()));
    profile.setBirthday(request.getBirthday());
    if (request.getStatus() != null) {
      profile.setStatus(request.getStatus());
      staff.setStatus(request.getStatus());
      staffMapper.updateById(staff);
    }
    profile.setLeaveDate(request.getLeaveDate());
    profile.setLeaveReason(normalizeBlank(request.getLeaveReason()));
    profile.setSocialSecurityStatus(normalizeBlank(request.getSocialSecurityStatus()));
    profile.setSocialSecurityStartDate(request.getSocialSecurityStartDate());
    profile.setSocialSecurityReminderDays(request.getSocialSecurityReminderDays());
    profile.setSocialSecurityCompanyApply(normalizeFlag(request.getSocialSecurityCompanyApply()));
    profile.setSocialSecurityNeedDirectorApproval(normalizeFlag(request.getSocialSecurityNeedDirectorApproval()));
    String workflowStatus = normalizeBlank(request.getSocialSecurityWorkflowStatus());
    if (workflowStatus != null) {
      profile.setSocialSecurityWorkflowStatus(normalizeSocialSecurityWorkflowStatus(workflowStatus));
    } else if (normalizeBlank(profile.getSocialSecurityWorkflowStatus()) == null) {
      profile.setSocialSecurityWorkflowStatus(resolveInitialSocialSecurityWorkflowStatus(profile.getSocialSecurityStatus()));
    }
    profile.setSocialSecurityMonthlyAmount(normalizeMoney(request.getSocialSecurityMonthlyAmount()));
    if (normalizeBlank(request.getSocialSecurityLastBilledMonth()) != null) {
      profile.setSocialSecurityLastBilledMonth(normalizeBlank(request.getSocialSecurityLastBilledMonth()));
    }
    profile.setSocialSecurityRemark(normalizeBlank(request.getSocialSecurityRemark()));
    profile.setRemark(normalizeBlank(request.getRemark()));

    if (profile.getId() == null) {
      if (profile.getStatus() == null) {
        profile.setStatus(1);
      }
      staffProfileMapper.insert(profile);
    } else {
      staffProfileMapper.updateById(profile);
    }
    syncBirthdayCalendarTask(orgId, staff, profile);

    return Result.ok(toProfileResponse(staff, profile));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/staff/page")
  public Result<IPage<StaffProfileResponse>> staffPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Long departmentId,
      @RequestParam(required = false) Long roleId,
      @RequestParam(required = false) String contractStatus) {
    Long orgId = AuthContext.getOrgId();
    List<Long> matchedContractStaffIds = findMatchedContractStaffIds(orgId, keyword, contractStatus);
    List<Long> matchedRoleStaffIds = findRoleStaffIds(orgId, roleId);
    var wrapper = Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .ne(StaffAccount::getUsername, HIDDEN_PLATFORM_USERNAME)
        .eq(departmentId != null, StaffAccount::getDepartmentId, departmentId)
        .eq(status != null, StaffAccount::getStatus, status);
    if (contractStatus != null && !contractStatus.isBlank()) {
      if (matchedContractStaffIds.isEmpty()) {
        wrapper.eq(StaffAccount::getId, -1L);
      } else {
        wrapper.in(StaffAccount::getId, matchedContractStaffIds);
      }
    }
    if (roleId != null) {
      if (matchedRoleStaffIds.isEmpty()) {
        wrapper.eq(StaffAccount::getId, -1L);
      } else {
        wrapper.in(StaffAccount::getId, matchedRoleStaffIds);
      }
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(StaffAccount::getRealName, keyword)
          .or().like(StaffAccount::getStaffNo, keyword)
          .or().like(StaffAccount::getPhone, keyword)
          .or(!matchedContractStaffIds.isEmpty(), x -> x.in(StaffAccount::getId, matchedContractStaffIds)));
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

  @PreAuthorize("hasAnyRole('HR_EMPLOYEE','HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
        .ne(StaffAccount::getUsername, HIDDEN_PLATFORM_USERNAME)
        .eq(StaffAccount::getStatus, 1)));
    Long leftCount = count(staffMapper.selectCount(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .ne(StaffAccount::getUsername, HIDDEN_PLATFORM_USERNAME)
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

    Long contractExpiringCount = count(staffProfileMapper.selectCount(Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getIsDeleted, 0)
        .eq(orgId != null, StaffProfile::getOrgId, orgId)
        .isNotNull(StaffProfile::getContractEndDate)
        .ge(StaffProfile::getContractEndDate, today)
        .le(StaffProfile::getContractEndDate, warningDeadline)
        .and(w -> w.isNull(StaffProfile::getContractStatus)
            .or().notIn(StaffProfile::getContractStatus, List.of("TERMINATED", "VOID")))));

    List<StaffProfile> birthdayProfiles = staffProfileMapper.selectList(Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getIsDeleted, 0)
        .eq(orgId != null, StaffProfile::getOrgId, orgId)
        .isNotNull(StaffProfile::getBirthday));
    Map<Long, StaffAccount> birthdayStaffMap = staffMapper.selectBatchIdsSafe(
            birthdayProfiles.stream().map(StaffProfile::getStaffId).distinct().toList())
        .stream()
        .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));
    long birthdayTodayCount = birthdayProfiles.stream()
        .filter(item -> item.getBirthday() != null)
        .filter(item -> {
          StaffAccount staff = birthdayStaffMap.get(item.getStaffId());
          return staff != null && Integer.valueOf(1).equals(staff.getStatus());
        })
        .filter(item -> item.getBirthday().getMonthValue() == today.getMonthValue()
            && item.getBirthday().getDayOfMonth() == today.getDayOfMonth())
        .count();
    long birthdayUpcomingCount = birthdayProfiles.stream()
        .filter(item -> item.getBirthday() != null)
        .filter(item -> {
          StaffAccount staff = birthdayStaffMap.get(item.getStaffId());
          return staff != null && Integer.valueOf(1).equals(staff.getStatus());
        })
        .map(item -> daysUntilBirthday(item.getBirthday(), today))
        .filter(days -> days != null && days >= 0 && days <= 7)
        .count();
    long birthdayTodoCount = count(oaTodoMapper.selectCount(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(OaTodo::getStatus, "OPEN")
        .like(OaTodo::getContent, "[BIRTHDAY_REMINDER:")));

    HrWorkbenchSummaryResponse summary = new HrWorkbenchSummaryResponse();
    summary.setOnJobCount(onJobCount);
    summary.setLeftCount(leftCount);
    summary.setTodayTrainingCount(todayTrainingCount);
    summary.setPendingLeaveApprovalCount(pendingLeaveApprovalCount);
    summary.setAttendanceAbnormalCount(attendanceAbnormalCount);
    summary.setContractExpiringCount(contractExpiringCount);
    summary.setBirthdayTodayCount(birthdayTodayCount);
    summary.setBirthdayUpcomingCount(birthdayUpcomingCount);
    summary.setBirthdayTodoCount(birthdayTodoCount);
    summary.setWarningDays(resolvedWarningDays);
    return Result.ok(summary);
  }

  @PreAuthorize("hasAnyRole('HR_EMPLOYEE','HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/staff/birthday/page")
  public Result<IPage<HrStaffBirthdayResponse>> staffBirthdayPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String scope,
      @RequestParam(required = false) Integer month,
      @RequestParam(required = false, defaultValue = "true") Boolean onJobOnly) {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    List<StaffProfile> profiles = staffProfileMapper.selectList(Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getIsDeleted, 0)
        .eq(orgId != null, StaffProfile::getOrgId, orgId)
        .isNotNull(StaffProfile::getBirthday));
    List<Long> staffIds = profiles.stream().map(StaffProfile::getStaffId).distinct().toList();
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIdsSafe(staffIds).stream()
        .filter(staff -> !isHiddenPlatformStaff(staff))
        .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));
    String scopeCode = scope == null ? "ALL" : scope.trim().toUpperCase(Locale.ROOT);
    int filterMonth = month == null ? 0 : month;
    boolean onlyOnJob = onJobOnly == null || onJobOnly;

    List<HrStaffBirthdayResponse> rows = profiles.stream()
        .map(profile -> toBirthdayResponse(profile, staffMap.get(profile.getStaffId()), today))
        .filter(item -> item.getStaffId() != null)
        .filter(item -> !onlyOnJob || Integer.valueOf(1).equals(item.getStatus()))
        .filter(item -> matchBirthdayScope(item, scopeCode, filterMonth))
        .filter(item -> {
          if (keyword == null || keyword.isBlank()) {
            return true;
          }
          String key = keyword.trim();
          return (item.getRealName() != null && item.getRealName().contains(key))
              || (item.getStaffNo() != null && item.getStaffNo().contains(key))
              || (item.getPhone() != null && item.getPhone().contains(key));
        })
        .sorted((a, b) -> {
          int cmp = Integer.compare(a.getDaysUntil() == null ? 9999 : a.getDaysUntil(), b.getDaysUntil() == null ? 9999 : b.getDaysUntil());
          if (cmp != 0) {
            return cmp;
          }
          LocalDate ad = a.getNextBirthday();
          LocalDate bd = b.getNextBirthday();
          if (ad == null && bd == null) return 0;
          if (ad == null) return 1;
          if (bd == null) return -1;
          return ad.compareTo(bd);
        })
        .toList();

    long total = rows.size();
    int fromIndex = (int) Math.max((pageNo - 1) * pageSize, 0);
    int toIndex = Math.min(fromIndex + (int) pageSize, rows.size());
    List<HrStaffBirthdayResponse> paged = fromIndex >= toIndex ? List.of() : rows.subList(fromIndex, toIndex);
    IPage<HrStaffBirthdayResponse> page = new Page<>(pageNo, pageSize, total);
    page.setRecords(paged);
    return Result.ok(page);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

    var wrapper = Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getIsDeleted, 0)
        .eq(orgId != null, StaffProfile::getOrgId, orgId)
        .isNotNull(StaffProfile::getContractEndDate)
        .le(StaffProfile::getContractEndDate, warningDeadline)
        .and(w -> w.isNull(StaffProfile::getContractStatus)
            .or().notIn(StaffProfile::getContractStatus, List.of("TERMINATED", "VOID")));
    if (!showExpired) {
      wrapper.ge(StaffProfile::getContractEndDate, today);
    }
    if (keyword != null && !keyword.isBlank()) {
      List<Long> matchedStaffIds = findMatchedStaffIds(orgId, keyword);
      wrapper.and(w -> w.like(StaffProfile::getContractNo, keyword.trim())
          .or(matchedStaffIds != null && !matchedStaffIds.isEmpty(), x -> x.in(StaffProfile::getStaffId, matchedStaffIds)));
    }
    wrapper.orderByAsc(StaffProfile::getContractEndDate).orderByDesc(StaffProfile::getUpdateTime);
    IPage<StaffProfile> page = staffProfileMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, StaffAccount> staffMap = loadStaffMap(page.getRecords().stream().map(StaffProfile::getStaffId).toList());

    IPage<HrContractReminderResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream()
        .map(profile -> toContractReminderResponse(profile, staffMap.get(profile.getStaffId()), today))
        .toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
              .ne(StaffAccount::getUsername, HIDDEN_PLATFORM_USERNAME)
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
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIdsSafe(
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
      item.setReviewed(record.getReviewed());
      item.setReviewRemark(record.getReviewRemark());
      item.setReviewedAt(record.getReviewedAt());
      return item;
    }).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
          .or().like(OaApproval::getRemark, keyword.trim())
          .or().like(OaApproval::getFormData, keyword.trim()));
    }
    wrapper.orderByDesc(OaApproval::getCreateTime);
    IPage<OaApproval> page = oaApprovalMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

    IPage<HrRecruitmentNeedResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(this::toRecruitmentNeedResponse).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
    entity.setFormData(writeJson(buildRecruitmentExtWithDefaults(request)));
    oaApprovalMapper.insert(entity);
    return Result.ok(toRecruitmentNeedResponse(entity));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PutMapping("/recruitment/need/{id}")
  public Result<HrRecruitmentNeedResponse> updateRecruitmentNeed(
      @PathVariable Long id,
      @RequestBody HrRecruitmentNeedRequest request) {
    Long orgId = AuthContext.getOrgId();
    OaApproval entity = oaApprovalMapper.selectOne(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getId, id)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getApprovalType, "HR_RECRUITMENT")
        .last("LIMIT 1"));
    if (entity == null) {
      return Result.error(404, "招聘事项不存在");
    }
    if (request.getTitle() != null) {
      entity.setTitle(request.getTitle());
    }
    if (request.getHeadcount() != null) {
      entity.setAmount(java.math.BigDecimal.valueOf(request.getHeadcount()));
    }
    if (request.getRequiredDate() != null) {
      entity.setStartTime(request.getRequiredDate().atStartOfDay());
    }
    if (request.getStatus() != null && !request.getStatus().isBlank()) {
      entity.setStatus(request.getStatus());
    }
    if (request.getRemark() != null) {
      entity.setRemark(request.getRemark());
    }
    Map<String, Object> merged = parseJson(entity.getFormData());
    merged.putAll(buildRecruitmentExtWithDefaults(request));
    entity.setFormData(writeJson(merged));
    oaApprovalMapper.updateById(entity);
    return Result.ok(toRecruitmentNeedResponse(entity));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @DeleteMapping("/recruitment/need/{id}")
  public Result<Void> deleteRecruitmentNeed(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    OaApproval entity = oaApprovalMapper.selectOne(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getId, id)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getApprovalType, "HR_RECRUITMENT")
        .last("LIMIT 1"));
    if (entity == null) {
      return Result.ok(null);
    }
    entity.setIsDeleted(1);
    oaApprovalMapper.updateById(entity);
    return Result.ok(null);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PutMapping("/recruitment/need/batch/status")
  public Result<Integer> batchUpdateRecruitmentNeedStatus(
      @RequestBody OaBatchIdsRequest request,
      @RequestParam String status) {
    if (status == null || status.isBlank()) {
      return Result.error(400, "status required");
    }
    Long orgId = AuthContext.getOrgId();
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    List<OaApproval> rows = oaApprovalMapper.selectList(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getApprovalType, "HR_RECRUITMENT")
        .in(OaApproval::getId, ids));
    for (OaApproval row : rows) {
      row.setStatus(status.trim());
      oaApprovalMapper.updateById(row);
    }
    return Result.ok(rows.size());
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/recruitment/materials/folder/bootstrap")
  public Result<Map<String, Object>> bootstrapRecruitmentMaterialFolders(
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false) String departmentName) {
    Long orgId = AuthContext.getOrgId();
    int resolvedYear = year == null || year < 2000 ? LocalDate.now().getYear() : year;
    String resolvedDepartment = departmentName == null || departmentName.isBlank() ? "综合部门" : departmentName.trim();
    Long rootId = ensureFolder(orgId, 0L, "人事行政档案", "入职/离职资料总目录");
    Long yearId = ensureFolder(orgId, rootId, String.valueOf(resolvedYear), "按年度归档");
    Long deptId = ensureFolder(orgId, yearId, resolvedDepartment, "按部门归档");
    Long onboardingId = ensureFolder(orgId, deptId, "入职资料收集", "入职资料");
    Long offboardingId = ensureFolder(orgId, deptId, "退职资料归档", "离职资料");
    Map<String, Object> result = new HashMap<>();
    result.put("rootFolderId", rootId);
    result.put("yearFolderId", yearId);
    result.put("departmentFolderId", deptId);
    result.put("onboardingFolderId", onboardingId);
    result.put("offboardingFolderId", offboardingId);
    result.put("folderPath", "人事行政档案/" + resolvedYear + "/" + resolvedDepartment);
    return Result.ok(result);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/policy")
  public Result<OaDocument> createPolicy(@RequestBody HrProfileDocumentRequest request) {
    return Result.ok(createProfileDocument("规章制度", request));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
    var wrapper = Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getIsDeleted, 0)
        .eq(orgId != null, StaffProfile::getOrgId, orgId)
        .and(w -> w.isNotNull(StaffProfile::getContractNo)
            .or().isNotNull(StaffProfile::getContractStartDate)
            .or().isNotNull(StaffProfile::getContractEndDate))
        .eq(status != null && !status.isBlank(), StaffProfile::getContractStatus, status)
        .ge(from != null, StaffProfile::getContractEndDate, from)
        .le(to != null, StaffProfile::getContractEndDate, to);
    if (keyword != null && !keyword.isBlank()) {
      List<Long> matchedStaffIds = findMatchedStaffIds(orgId, keyword);
      wrapper.and(w -> w.like(StaffProfile::getContractNo, keyword.trim())
          .or(matchedStaffIds != null && !matchedStaffIds.isEmpty(), x -> x.in(StaffProfile::getStaffId, matchedStaffIds)));
    }
    wrapper.orderByAsc(StaffProfile::getContractEndDate).orderByDesc(StaffProfile::getUpdateTime);
    IPage<StaffProfile> page = staffProfileMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, StaffAccount> staffMap = loadStaffMap(page.getRecords().stream().map(StaffProfile::getStaffId).toList());
    IPage<HrProfileContractResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream()
        .map(item -> toProfileContractResponse(item, staffMap.get(item.getStaffId()), today))
        .toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasAnyRole('HR_EMPLOYEE','HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/social-security/summary")
  public Result<HrSocialSecuritySummaryResponse> socialSecuritySummary() {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    LocalDate monthStart = today.withDayOfMonth(1);
    LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
    LocalDate upcomingDeadline = today.plusDays(7);

    List<StaffProfile> profiles = staffProfileMapper.selectList(Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getIsDeleted, 0)
        .eq(orgId != null, StaffProfile::getOrgId, orgId));
    Map<Long, StaffAccount> staffMap = loadStaffMap(profiles.stream().map(StaffProfile::getStaffId).toList());

    long thisMonthNewParticipantCount = profiles.stream()
        .filter(profile -> profile.getSocialSecurityStartDate() != null)
        .filter(profile -> !profile.getSocialSecurityStartDate().isBefore(monthStart))
        .filter(profile -> !profile.getSocialSecurityStartDate().isAfter(monthEnd))
        .count();

    long dueReminderCount = profiles.stream()
        .filter(profile -> shouldTrackSocialSecurity(profile, staffMap.get(profile.getStaffId())))
        .map(this::resolveSocialSecurityReminderDate)
        .filter(Objects::nonNull)
        .filter(date -> !date.isAfter(today))
        .count();

    long upcomingReminderCount = profiles.stream()
        .filter(profile -> shouldTrackSocialSecurity(profile, staffMap.get(profile.getStaffId())))
        .map(this::resolveSocialSecurityReminderDate)
        .filter(Objects::nonNull)
        .filter(date -> date.isAfter(today) && !date.isAfter(upcomingDeadline))
        .count();

    HrSocialSecuritySummaryResponse response = new HrSocialSecuritySummaryResponse();
    response.setDueReminderCount(dueReminderCount);
    response.setUpcomingReminderCount(upcomingReminderCount);
    response.setThisMonthNewParticipantCount(thisMonthNewParticipantCount);
    return Result.ok(response);
  }

  @PreAuthorize("hasAnyRole('HR_EMPLOYEE','HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/social-security/reminder/page")
  public Result<IPage<HrSocialSecurityReminderResponse>> socialSecurityReminderPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String scope,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    LocalDate upcomingDeadline = today.plusDays(7);
    String scopeCode = scope == null ? "ALL" : scope.trim().toUpperCase(Locale.ROOT);
    String statusCode = status == null ? null : status.trim().toUpperCase(Locale.ROOT);

    List<StaffProfile> profiles = staffProfileMapper.selectList(Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getIsDeleted, 0)
        .eq(orgId != null, StaffProfile::getOrgId, orgId)
        .and(statusCode != null && !statusCode.isBlank(),
            w -> w.eq(StaffProfile::getSocialSecurityStatus, statusCode)
                .or().eq(StaffProfile::getSocialSecurityStatus, status)));
    Map<Long, StaffAccount> staffMap = loadStaffMap(profiles.stream().map(StaffProfile::getStaffId).toList());

    List<HrSocialSecurityReminderResponse> rows = profiles.stream()
        .map(profile -> toSocialSecurityReminderResponse(profile, staffMap.get(profile.getStaffId()), today))
        .filter(item -> item.getStaffId() != null)
        .filter(item -> matchSocialSecurityScope(item, scopeCode, today, upcomingDeadline))
        .filter(item -> {
          if (keyword == null || keyword.isBlank()) {
            return true;
          }
          String key = keyword.trim();
          return (item.getStaffName() != null && item.getStaffName().contains(key))
              || (item.getStaffNo() != null && item.getStaffNo().contains(key))
              || (item.getPhone() != null && item.getPhone().contains(key));
        })
        .sorted((a, b) -> {
          long aDays = a.getRemainingDays() == null ? Long.MAX_VALUE : a.getRemainingDays();
          long bDays = b.getRemainingDays() == null ? Long.MAX_VALUE : b.getRemainingDays();
          int cmp = Long.compare(aDays, bDays);
          if (cmp != 0) {
            return cmp;
          }
          String aName = a.getStaffName() == null ? "" : a.getStaffName();
          String bName = b.getStaffName() == null ? "" : b.getStaffName();
          return aName.compareTo(bName);
        })
        .toList();

    long total = rows.size();
    int fromIndex = (int) Math.max((pageNo - 1) * pageSize, 0);
    int toIndex = Math.min(fromIndex + (int) pageSize, rows.size());
    List<HrSocialSecurityReminderResponse> paged = fromIndex >= toIndex ? List.of() : rows.subList(fromIndex, toIndex);
    IPage<HrSocialSecurityReminderResponse> page = new Page<>(pageNo, pageSize, total);
    page.setRecords(paged);
    return Result.ok(page);
  }

  @PreAuthorize("hasAnyRole('HR_EMPLOYEE','HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/social-security/apply")
  public Result<StaffProfileResponse> applySocialSecurity(@RequestBody HrSocialSecurityApplyRequest request) {
    Long orgId = AuthContext.getOrgId();
    if (request == null || request.getStaffId() == null) {
      return Result.error(400, "staffId required");
    }
    StaffAccount staff = findStaff(orgId, request.getStaffId());
    if (staff == null) {
      return Result.error(404, "Staff not found");
    }
    StaffProfile profile = ensureSocialSecurityProfile(orgId, staff);
    profile.setSocialSecurityCompanyApply(normalizeFlag(request.getSocialSecurityCompanyApply()) == 1 ? 1 : 0);
    profile.setSocialSecurityNeedDirectorApproval(normalizeFlag(request.getSocialSecurityNeedDirectorApproval()));
    profile.setSocialSecurityMonthlyAmount(normalizeMoney(request.getSocialSecurityMonthlyAmount()));
    if (normalizeBlank(request.getSocialSecurityRemark()) != null) {
      profile.setSocialSecurityRemark(normalizeBlank(request.getSocialSecurityRemark()));
    }
    profile.setSocialSecurityApplySubmittedAt(LocalDateTime.now());
    profile.setSocialSecurityApplySubmittedBy(AuthContext.getStaffId());
    profile.setSocialSecurityDirectorDecisionAt(null);
    profile.setSocialSecurityDirectorDecisionBy(null);
    profile.setSocialSecurityCompletedAt(null);
    if (normalizeFlag(profile.getSocialSecurityNeedDirectorApproval()) == 1) {
      profile.setSocialSecurityWorkflowStatus("PENDING_DIRECTOR");
      profile.setSocialSecurityStatus("PROCESSING");
      clearSocialSecurityFinanceTodo(orgId, profile);
      syncSocialSecurityDirectorTodo(orgId, staff, profile);
    } else {
      profile.setSocialSecurityWorkflowStatus("PENDING_FINANCE");
      profile.setSocialSecurityStatus("PROCESSING");
      clearSocialSecurityDirectorTodos(orgId, staff.getId());
      syncSocialSecurityFinanceTodo(orgId, staff, profile);
    }
    staffProfileMapper.updateById(profile);
    return Result.ok(toProfileResponse(staff, profile));
  }

  @PreAuthorize("hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN')")
  @PutMapping("/social-security/{staffId}/director-decision")
  public Result<StaffProfileResponse> socialSecurityDirectorDecision(
      @PathVariable Long staffId,
      @RequestParam boolean approved,
      @RequestParam(required = false) String remark) {
    Long orgId = AuthContext.getOrgId();
    StaffAccount staff = findStaff(orgId, staffId);
    if (staff == null) {
      return Result.error(404, "Staff not found");
    }
    StaffProfile profile = ensureSocialSecurityProfile(orgId, staff);
    if (!"PENDING_DIRECTOR".equals(normalizeBlank(profile.getSocialSecurityWorkflowStatus()))) {
      return Result.error(400, "当前不是院长待审状态");
    }
    profile.setSocialSecurityDirectorDecisionAt(LocalDateTime.now());
    profile.setSocialSecurityDirectorDecisionBy(AuthContext.getStaffId());
    if (normalizeBlank(remark) != null) {
      profile.setSocialSecurityRemark(normalizeBlank(remark));
    }
    clearSocialSecurityDirectorTodos(orgId, staffId);
    if (approved) {
      profile.setSocialSecurityWorkflowStatus("PENDING_FINANCE");
      profile.setSocialSecurityStatus("PROCESSING");
      syncSocialSecurityFinanceTodo(orgId, staff, profile);
    } else {
      profile.setSocialSecurityWorkflowStatus("REJECTED");
      profile.setSocialSecurityStatus("PENDING");
      clearSocialSecurityFinanceTodo(orgId, profile);
    }
    staffProfileMapper.updateById(profile);
    return Result.ok(toProfileResponse(staff, profile));
  }

  @PreAuthorize("hasAnyRole('HR_EMPLOYEE','HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN','FINANCE_MINISTER','FINANCE_EMPLOYEE','ACCOUNTANT','CASHIER','FINANCE')")
  @PutMapping("/social-security/{staffId}/complete")
  public Result<StaffProfileResponse> completeSocialSecurity(
      @PathVariable Long staffId,
      @RequestBody(required = false) HrSocialSecurityCompleteRequest request) {
    Long orgId = AuthContext.getOrgId();
    StaffAccount staff = findStaff(orgId, staffId);
    if (staff == null) {
      return Result.error(404, "Staff not found");
    }
    StaffProfile profile = ensureSocialSecurityProfile(orgId, staff);
    if (normalizeBlank(profile.getSocialSecurityWorkflowStatus()) == null
        || List.of("DRAFT", "REJECTED", "STOPPED").contains(profile.getSocialSecurityWorkflowStatus())) {
      return Result.error(400, "请先发起社保申请流程");
    }
    if (request != null && request.getSocialSecurityStartDate() != null) {
      profile.setSocialSecurityStartDate(request.getSocialSecurityStartDate());
    } else if (profile.getSocialSecurityStartDate() == null) {
      profile.setSocialSecurityStartDate(LocalDate.now());
    }
    if (request != null && normalizeBlank(request.getSocialSecurityRemark()) != null) {
      profile.setSocialSecurityRemark(normalizeBlank(request.getSocialSecurityRemark()));
    }
    profile.setSocialSecurityStatus("COMPLETED");
    profile.setSocialSecurityWorkflowStatus("ACTIVE");
    profile.setSocialSecurityCompletedAt(LocalDateTime.now());
    clearSocialSecurityDirectorTodos(orgId, staffId);
    clearSocialSecurityFinanceTodo(orgId, profile);
    if (zeroIfNull(profile.getSocialSecurityMonthlyAmount()).signum() > 0) {
      upsertSocialSecurityMonthlyBill(orgId, staff, profile, YearMonth.now(), AuthContext.getStaffId());
    }
    staffProfileMapper.updateById(profile);
    return Result.ok(toProfileResponse(staff, profile));
  }

  @PreAuthorize("hasAnyRole('HR_EMPLOYEE','HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN','FINANCE_MINISTER','FINANCE_EMPLOYEE','ACCOUNTANT','CASHIER','FINANCE')")
  @PostMapping("/social-security/monthly-bill/generate")
  public Result<HrBatchActionSummaryResponse> generateSocialSecurityMonthlyBill(
      @RequestBody(required = false) HrSocialSecurityBillGenerateRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    YearMonth feeMonth = resolveFeeMonth(request == null ? null : request.getMonth());
    List<Long> filterStaffIds = sanitizeIds(request == null ? null : request.getStaffIds());
    List<StaffProfile> profiles = staffProfileMapper.selectList(Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getOrgId, orgId)
        .eq(StaffProfile::getIsDeleted, 0)
        .in(!filterStaffIds.isEmpty(), StaffProfile::getStaffId, filterStaffIds));
    Map<Long, StaffAccount> staffMap = loadStaffMap(profiles.stream().map(StaffProfile::getStaffId).toList());

    int successCount = 0;
    int skippedCount = 0;
    java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
    for (StaffProfile profile : profiles) {
      StaffAccount staff = staffMap.get(profile.getStaffId());
      if (!canGenerateSocialSecurityBill(profile, staff, feeMonth)) {
        skippedCount++;
        continue;
      }
      StaffMonthlyFeeBill bill = upsertSocialSecurityMonthlyBill(orgId, staff, profile, feeMonth, operatorId);
      if (bill == null) {
        skippedCount++;
        continue;
      }
      successCount++;
      totalAmount = totalAmount.add(zeroIfNull(bill.getAmount()));
    }
    HrBatchActionSummaryResponse response = new HrBatchActionSummaryResponse();
    response.setTotalCount(profiles.size());
    response.setSuccessCount(successCount);
    response.setSkippedCount(skippedCount);
    response.setTotalAmount(totalAmount);
    response.setMessage("社保月度记账已生成 " + feeMonth);
    return Result.ok(response);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/profile/template/page")
  public Result<IPage<OaDocument>> profileTemplatePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(queryProfileDocuments("合同模板", pageNo, pageSize, keyword));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/profile/template")
  public Result<OaDocument> createProfileTemplate(@RequestBody HrProfileDocumentRequest request) {
    return Result.ok(createProfileDocument("合同模板", request));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/profile/attachment/page")
  public Result<IPage<OaDocument>> profileAttachmentPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(queryProfileDocuments("档案附件", pageNo, pageSize, keyword));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/profile/attachment")
  public Result<OaDocument> createProfileAttachment(@RequestBody HrProfileDocumentRequest request) {
    return Result.ok(createProfileDocument("档案附件", request));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/profile/contract-attachment/page")
  public Result<IPage<OaDocument>> profileContractAttachmentPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(queryProfileDocuments("合同附件", pageNo, pageSize, keyword));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/profile/contract-attachment")
  public Result<OaDocument> createProfileContractAttachment(@RequestBody HrProfileDocumentRequest request) {
    return Result.ok(createProfileDocument("合同附件", request));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
        .and(w -> w.eq(StaffTrainingRecord::getTrainingScene, "CERTIFICATE")
            .or().eq(StaffTrainingRecord::getTrainingType, "CERTIFICATE"))
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/profile/certificate")
  public Result<HrStaffCertificateResponse> createProfileCertificate(@RequestBody HrStaffCertificateRequest request) {
    Long orgId = AuthContext.getOrgId();
    StaffTrainingRecord record = new StaffTrainingRecord();
    record.setOrgId(orgId);
    record.setStaffId(request.getStaffId());
    record.setTrainingScene("CERTIFICATE");
    record.setTrainingType("CERTIFICATE");
    record.setTrainingName(request.getCertificateName());
    record.setCertificateNo(request.getCertificateNo());
    record.setProvider(request.getIssuingAuthority());
    record.setStartDate(request.getIssueDate());
    record.setEndDate(request.getExpiryDate());
    record.setStatus(1);
    record.setCertificateRequired(1);
    record.setCertificateStatus("ISSUED");
    record.setRemark(request.getRemark());
    fillTrainingSnapshots(record, request.getStaffId(), null, null, request.getIssueDate(), false);
    trainingRecordMapper.insert(record);
    IPage<StaffTrainingRecord> page = new Page<>(1, 1, 1);
    page.setRecords(List.of(record));
    IPage<HrStaffCertificateResponse> converted = toCertificatePage(page, LocalDate.now());
    return Result.ok(converted.getRecords().isEmpty() ? null : converted.getRecords().get(0));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
        .and(w -> w.eq(StaffTrainingRecord::getTrainingScene, "CERTIFICATE")
            .or().eq(StaffTrainingRecord::getTrainingType, "CERTIFICATE"))
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIdsSafe(
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
    List<Long> elderIds = page.getRecords().stream()
        .map(CardAccount::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds)
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/expense/meal-fee/page")
  public Result<IPage<HrStaffMonthlyFeeBillResponse>> expenseMealFeePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String month,
      @RequestParam(required = false) String financeSyncStatus) {
    return Result.ok(pageStaffMonthlyFee(pageNo, pageSize, keyword, month, financeSyncStatus, "MEAL"));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/expense/electricity-fee/page")
  public Result<IPage<HrStaffMonthlyFeeBillResponse>> expenseElectricityFeePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String month,
      @RequestParam(required = false) String financeSyncStatus) {
    return Result.ok(pageStaffMonthlyFee(pageNo, pageSize, keyword, month, financeSyncStatus, "ELECTRICITY"));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/expense/service-plan/{staffId}")
  public Result<HrStaffServicePlanResponse> getStaffServicePlan(@PathVariable Long staffId) {
    Long orgId = AuthContext.getOrgId();
    StaffAccount staff = findStaff(orgId, staffId);
    if (staff == null) {
      return Result.error(404, "员工不存在");
    }
    StaffServicePlan plan = staffServicePlanMapper.selectOne(
        Wrappers.lambdaQuery(StaffServicePlan.class)
            .eq(StaffServicePlan::getOrgId, orgId)
            .eq(StaffServicePlan::getStaffId, staffId)
            .eq(StaffServicePlan::getIsDeleted, 0)
            .last("LIMIT 1"));
    Department department = staff.getDepartmentId() == null ? null : departmentMapper.selectById(staff.getDepartmentId());
    return Result.ok(toServicePlanResponse(plan, staff, department));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/dormitory/overview")
  public Result<HrDormitoryOverviewResponse> dormitoryOverview(
      @RequestParam(required = false) Integer status) {
    List<HrDormitoryStaffResponse> rows = buildDormitoryRows(AuthContext.getOrgId(), status);
    List<HrDormitoryRoomConfigResponse> roomConfigs = listDormitoryRoomConfigs(AuthContext.getOrgId(), null, null, null, null, "ENABLED");
    HrDormitoryOverviewResponse response = new HrDormitoryOverviewResponse();
    response.setStaffCount((long) rows.size());
    response.setLiveInDormitoryCount(rows.stream()
        .filter(item -> normalizeFlag(item.getLiveInDormitory()) == 1)
        .count());
    response.setAssignedBedCount(rows.stream()
        .filter(this::hasCompleteDormitoryAssignment)
        .count());
    response.setPendingAssignCount(rows.stream()
        .filter(item -> normalizeFlag(item.getLiveInDormitory()) == 1)
        .filter(item -> !hasCompleteDormitoryAssignment(item))
        .count());
    response.setBuildingCount((long) Stream.concat(
            rows.stream()
                .map(HrDormitoryStaffResponse::getDormitoryBuilding)
                .map(this::normalizeBlank)
                .filter(Objects::nonNull),
            roomConfigs.stream()
                .map(HrDormitoryRoomConfigResponse::getBuilding)
                .map(this::normalizeBlank)
                .filter(Objects::nonNull))
        .distinct()
        .count());
    response.setRoomCount((long) Stream.concat(
            rows.stream()
                .map(this::dormitoryRoomKey)
                .filter(Objects::nonNull),
            roomConfigs.stream()
                .map(this::dormitoryRoomConfigKey)
                .filter(Objects::nonNull))
        .distinct()
        .count());
    response.setConfiguredRoomCount((long) roomConfigs.size());
    response.setConfiguredBedCapacity(roomConfigs.stream()
        .map(HrDormitoryRoomConfigResponse::getBedCapacity)
        .filter(Objects::nonNull)
        .mapToLong(Integer::longValue)
        .sum());
    response.setMeterBoundCount(rows.stream()
        .filter(item -> normalizeFlag(item.getLiveInDormitory()) == 1)
        .filter(item -> normalizeBlank(item.getMeterNo()) != null)
        .count());
    response.setConflictCount(rows.stream()
        .filter(item -> Boolean.TRUE.equals(item.getOccupancyConflict()))
        .count());
    return Result.ok(response);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/dormitory/page")
  public Result<IPage<HrDormitoryStaffResponse>> dormitoryPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Long departmentId,
      @RequestParam(required = false) String dormitoryState,
      @RequestParam(required = false) String building,
      @RequestParam(required = false) String roomNo) {
    List<HrDormitoryStaffResponse> filtered = buildDormitoryRows(AuthContext.getOrgId(), status).stream()
        .filter(item -> matchesDormitoryRow(item, keyword, departmentId, dormitoryState, building, roomNo))
        .toList();
    long current = pageNo <= 0 ? 1 : pageNo;
    long size = pageSize <= 0 ? 20 : pageSize;
    int fromIndex = (int) Math.min(filtered.size(), Math.max(0, (current - 1) * size));
    int toIndex = (int) Math.min(filtered.size(), fromIndex + size);
    IPage<HrDormitoryStaffResponse> page = new Page<>(current, size, filtered.size());
    page.setRecords(filtered.subList(fromIndex, toIndex));
    return Result.ok(page);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/dormitory/map")
  public Result<List<HrDormitoryStaffResponse>> dormitoryMap(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Long departmentId,
      @RequestParam(required = false) String dormitoryState,
      @RequestParam(required = false) String building,
      @RequestParam(required = false) String roomNo) {
    return Result.ok(buildDormitoryRows(AuthContext.getOrgId(), status).stream()
        .filter(item -> matchesDormitoryRow(item, keyword, departmentId, dormitoryState, building, roomNo))
        .toList());
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/dormitory/room-config/list")
  public Result<List<HrDormitoryRoomConfigResponse>> dormitoryRoomConfigList(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String building,
      @RequestParam(required = false) String floorLabel,
      @RequestParam(required = false) String roomNo,
      @RequestParam(required = false) String status) {
    return Result.ok(listDormitoryRoomConfigs(AuthContext.getOrgId(), keyword, building, floorLabel, roomNo, status));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/dormitory/room-config")
  public Result<HrDormitoryRoomConfigResponse> upsertDormitoryRoomConfig(@RequestBody HrDormitoryRoomConfigRequest request) {
    Long orgId = AuthContext.getOrgId();
    if (request == null) {
      return Result.error(400, "request required");
    }
    String building = normalizeBlank(request.getBuilding());
    String roomNo = normalizeBlank(request.getRoomNo());
    if (building == null || roomNo == null) {
      return Result.error(400, "楼栋和房间号不能为空");
    }
    int bedCapacity = request.getBedCapacity() == null ? 0 : Math.max(request.getBedCapacity(), 0);
    if (bedCapacity <= 0) {
      return Result.error(400, "标准床位数必须大于 0");
    }
    HrDormitoryRoomConfig entity = request.getId() == null ? null : dormitoryRoomConfigMapper.selectOne(
        Wrappers.lambdaQuery(HrDormitoryRoomConfig.class)
            .eq(HrDormitoryRoomConfig::getId, request.getId())
            .eq(HrDormitoryRoomConfig::getOrgId, orgId)
            .eq(HrDormitoryRoomConfig::getIsDeleted, 0)
            .last("LIMIT 1"));
    if (entity == null) {
      entity = new HrDormitoryRoomConfig();
      entity.setOrgId(orgId);
    }
    HrDormitoryRoomConfig duplicate = dormitoryRoomConfigMapper.selectOne(
        Wrappers.lambdaQuery(HrDormitoryRoomConfig.class)
            .eq(HrDormitoryRoomConfig::getOrgId, orgId)
            .eq(HrDormitoryRoomConfig::getBuilding, building)
            .eq(HrDormitoryRoomConfig::getRoomNo, roomNo)
            .eq(HrDormitoryRoomConfig::getIsDeleted, 0)
            .ne(request.getId() != null, HrDormitoryRoomConfig::getId, request.getId())
            .last("LIMIT 1"));
    if (duplicate != null) {
      return Result.error(400, "该楼栋房间已存在基础配置");
    }
    entity.setBuilding(building);
    entity.setRoomNo(roomNo);
    entity.setFloorLabel(firstNonBlank(normalizeBlank(request.getFloorLabel()), resolveDormitoryFloorLabel(roomNo)));
    entity.setBedCapacity(bedCapacity);
    entity.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
    entity.setStatus(normalizeBlank(request.getStatus()) == null ? "ENABLED" : normalizeBlank(request.getStatus()).toUpperCase(Locale.ROOT));
    entity.setRemark(normalizeBlank(request.getRemark()));
    saveDormitoryRoomConfig(entity);
    return Result.ok(toDormitoryRoomConfigResponse(entity));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @DeleteMapping("/dormitory/room-config/{id}")
  public Result<Void> deleteDormitoryRoomConfig(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    HrDormitoryRoomConfig entity = dormitoryRoomConfigMapper.selectOne(
        Wrappers.lambdaQuery(HrDormitoryRoomConfig.class)
            .eq(HrDormitoryRoomConfig::getId, id)
            .eq(HrDormitoryRoomConfig::getOrgId, orgId)
            .eq(HrDormitoryRoomConfig::getIsDeleted, 0)
            .last("LIMIT 1"));
    if (entity == null) {
      return Result.ok(null);
    }
    entity.setIsDeleted(1);
    dormitoryRoomConfigMapper.updateById(entity);
    return Result.ok(null);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/expense/service-plan")
  public Result<HrStaffServicePlanResponse> upsertStaffServicePlan(@RequestBody HrStaffServicePlanRequest request) {
    Long orgId = AuthContext.getOrgId();
    if (request == null || request.getStaffId() == null) {
      return Result.error(400, "staffId required");
    }
    StaffAccount staff = findStaff(orgId, request.getStaffId());
    if (staff == null) {
      return Result.error(404, "员工不存在");
    }
    Department department = staff.getDepartmentId() == null ? null : departmentMapper.selectById(staff.getDepartmentId());
    StaffServicePlan plan = staffServicePlanMapper.selectOne(
        Wrappers.lambdaQuery(StaffServicePlan.class)
            .eq(StaffServicePlan::getOrgId, orgId)
            .eq(StaffServicePlan::getStaffId, request.getStaffId())
            .eq(StaffServicePlan::getIsDeleted, 0)
            .last("LIMIT 1"));
    if (plan == null) {
      plan = new StaffServicePlan();
      plan.setOrgId(orgId);
      plan.setStaffId(staff.getId());
    }
    plan.setStaffNo(normalizeBlank(staff.getStaffNo()));
    plan.setStaffName(normalizeBlank(staff.getRealName()));
    plan.setDepartmentId(staff.getDepartmentId());
    plan.setDepartmentName(department == null ? null : normalizeBlank(department.getDeptName()));
    if (request.getBreakfastEnabled() != null || plan.getId() == null) {
      plan.setBreakfastEnabled(normalizeFlag(request.getBreakfastEnabled()));
    }
    if (request.getBreakfastUnitPrice() != null || plan.getId() == null) {
      plan.setBreakfastUnitPrice(request.getBreakfastUnitPrice() == null ? null : normalizeMoney(request.getBreakfastUnitPrice()));
    }
    if (request.getBreakfastDaysPerMonth() != null || plan.getId() == null) {
      plan.setBreakfastDaysPerMonth(normalizeDays(request.getBreakfastDaysPerMonth()));
    }
    if (request.getLunchEnabled() != null || plan.getId() == null) {
      plan.setLunchEnabled(normalizeFlag(request.getLunchEnabled()));
    }
    if (request.getLunchUnitPrice() != null || plan.getId() == null) {
      plan.setLunchUnitPrice(request.getLunchUnitPrice() == null ? null : normalizeMoney(request.getLunchUnitPrice()));
    }
    if (request.getLunchDaysPerMonth() != null || plan.getId() == null) {
      plan.setLunchDaysPerMonth(normalizeDays(request.getLunchDaysPerMonth()));
    }
    if (request.getDinnerEnabled() != null || plan.getId() == null) {
      plan.setDinnerEnabled(normalizeFlag(request.getDinnerEnabled()));
    }
    if (request.getDinnerUnitPrice() != null || plan.getId() == null) {
      plan.setDinnerUnitPrice(request.getDinnerUnitPrice() == null ? null : normalizeMoney(request.getDinnerUnitPrice()));
    }
    if (request.getDinnerDaysPerMonth() != null || plan.getId() == null) {
      plan.setDinnerDaysPerMonth(normalizeDays(request.getDinnerDaysPerMonth()));
    }
    if (request.getLiveInDormitory() != null || plan.getId() == null) {
      plan.setLiveInDormitory(normalizeFlag(request.getLiveInDormitory()));
    }
    if (request.getDormitoryBuilding() != null || plan.getId() == null) {
      plan.setDormitoryBuilding(normalizeBlank(request.getDormitoryBuilding()));
    }
    if (request.getDormitoryRoomNo() != null || plan.getId() == null) {
      plan.setDormitoryRoomNo(normalizeBlank(request.getDormitoryRoomNo()));
    }
    if (request.getDormitoryBedNo() != null || plan.getId() == null) {
      plan.setDormitoryBedNo(normalizeBlank(request.getDormitoryBedNo()));
    }
    if (request.getMeterNo() != null || plan.getId() == null) {
      plan.setMeterNo(normalizeBlank(request.getMeterNo()));
    }
    if (request.getStatus() != null || plan.getId() == null) {
      plan.setStatus(normalizeBlank(request.getStatus()) == null ? "ENABLED" : normalizeBlank(request.getStatus()).toUpperCase(Locale.ROOT));
    }
    if (request.getRemark() != null || plan.getId() == null) {
      plan.setRemark(normalizeBlank(request.getRemark()));
    }
    if (normalizeFlag(plan.getLiveInDormitory()) != 1) {
      plan.setDormitoryBuilding(null);
      plan.setDormitoryRoomNo(null);
      plan.setDormitoryBedNo(null);
      plan.setMeterNo(null);
    }
    String occupancyKey = dormitoryOccupancyKey(plan);
    if (occupancyKey != null) {
      StaffServicePlan occupied = staffServicePlanMapper.selectOne(
          Wrappers.lambdaQuery(StaffServicePlan.class)
              .eq(StaffServicePlan::getOrgId, orgId)
              .eq(StaffServicePlan::getIsDeleted, 0)
              .eq(StaffServicePlan::getLiveInDormitory, 1)
              .eq(StaffServicePlan::getDormitoryBuilding, plan.getDormitoryBuilding())
              .eq(StaffServicePlan::getDormitoryRoomNo, plan.getDormitoryRoomNo())
              .eq(StaffServicePlan::getDormitoryBedNo, plan.getDormitoryBedNo())
              .ne(plan.getStaffId() != null, StaffServicePlan::getStaffId, plan.getStaffId())
              .last("LIMIT 1"));
      if (occupied != null) {
        String occupiedBy = defaultText(occupied.getStaffName(), defaultText(occupied.getStaffNo(), "其他员工"));
        return Result.error(400, "该宿舍床位已分配给" + occupiedBy);
      }
    }
    saveServicePlan(plan);
    return Result.ok(toServicePlanResponse(plan, staff, department));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/expense/meal-fee/generate")
  public Result<HrBatchActionSummaryResponse> generateMealFee(@RequestBody HrStaffFeeGenerateRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    YearMonth feeMonth = resolveFeeMonth(request == null ? null : request.getMonth());
    List<StaffAccount> activeStaff = listActiveStaff(orgId);
    Map<Long, Department> departmentMap = loadDepartmentMap(activeStaff);
    Map<Long, StaffServicePlan> planMap = loadServicePlanMap(orgId, activeStaff.stream().map(StaffAccount::getId).toList());
    int successCount = 0;
    int skippedCount = 0;
    java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
    for (StaffAccount staff : activeStaff) {
      StaffServicePlan plan = planMap.get(staff.getId());
      if (!isPlanEnabled(plan) || mealEnabledCount(plan) == 0) {
        skippedCount++;
        continue;
      }
      Department department = departmentMap.get(staff.getDepartmentId());
      StaffMonthlyFeeBill bill = findMonthlyFeeBill(orgId, staff.getId(), feeMonth.toString(), "MEAL");
      if (bill == null) {
        bill = new StaffMonthlyFeeBill();
        bill.setOrgId(orgId);
        bill.setStaffId(staff.getId());
        bill.setFeeMonth(feeMonth.toString());
        bill.setFeeType("MEAL");
        bill.setCreatedBy(operatorId);
      }
      java.math.BigDecimal amount = mealBillAmount(plan);
      bill.setStaffNo(normalizeBlank(staff.getStaffNo()));
      bill.setStaffName(normalizeBlank(staff.getRealName()));
      bill.setDepartmentId(staff.getDepartmentId());
      bill.setDepartmentName(department == null ? null : normalizeBlank(department.getDeptName()));
      bill.setTitle("员工餐费 " + feeMonth + " " + defaultText(staff.getRealName(), defaultText(staff.getUsername(), "员工")));
      bill.setQuantity(java.math.BigDecimal.valueOf(totalMealDays(plan)));
      bill.setUnitPrice(resolveAverageUnitPrice(amount, totalMealDays(plan)));
      bill.setAmount(amount);
      bill.setStatus("GENERATED");
      markFinancePending(bill);
      bill.setDormitoryBuilding(normalizeBlank(plan.getDormitoryBuilding()));
      bill.setDormitoryRoomNo(normalizeBlank(plan.getDormitoryRoomNo()));
      bill.setDormitoryBedNo(normalizeBlank(plan.getDormitoryBedNo()));
      bill.setMeterNo(normalizeBlank(plan.getMeterNo()));
      bill.setDetailJson(writeJson(buildMealFeeDetail(plan)));
      bill.setRemark(normalizeBlank(plan.getRemark()));
      saveMonthlyFeeBill(bill);
      successCount++;
      totalAmount = totalAmount.add(zeroIfNull(amount));
    }
    HrBatchActionSummaryResponse response = new HrBatchActionSummaryResponse();
    response.setTotalCount(activeStaff.size());
    response.setSuccessCount(successCount);
    response.setSkippedCount(skippedCount);
    response.setTotalAmount(totalAmount);
    response.setMessage("员工餐费已按 " + feeMonth + " 生成");
    return Result.ok(response);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/expense/electricity-fee/import")
  public Result<HrBatchActionSummaryResponse> importElectricityFee(@RequestBody HrStaffElectricityImportRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    if (request == null || request.getRows() == null || request.getRows().isEmpty()) {
      return Result.error(400, "rows required");
    }
    YearMonth feeMonth = resolveFeeMonth(request.getMonth());
    List<StaffAccount> activeStaff = listActiveStaff(orgId);
    Map<Long, StaffAccount> staffById = activeStaff.stream()
        .filter(item -> item.getId() != null)
        .collect(Collectors.toMap(StaffAccount::getId, item -> item, (a, b) -> a));
    Map<String, StaffAccount> staffByNo = activeStaff.stream()
        .filter(item -> normalizeBlank(item.getStaffNo()) != null)
        .collect(Collectors.toMap(item -> item.getStaffNo().trim().toLowerCase(Locale.ROOT), item -> item, (a, b) -> a));
    Map<String, StaffAccount> staffByName = activeStaff.stream()
        .filter(item -> normalizeBlank(item.getRealName()) != null)
        .collect(Collectors.toMap(item -> item.getRealName().trim().toLowerCase(Locale.ROOT), item -> item, (a, b) -> a));
    Map<Long, Department> departmentMap = loadDepartmentMap(activeStaff);
    Map<Long, StaffServicePlan> planMap = loadServicePlanMap(orgId, activeStaff.stream().map(StaffAccount::getId).toList());
    int successCount = 0;
    int skippedCount = 0;
    java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
    for (HrStaffElectricityImportRowRequest row : request.getRows()) {
      StaffAccount staff = resolveImportStaff(row, staffById, staffByNo, staffByName);
      if (staff == null || zeroIfNull(row.getAmount()).signum() <= 0) {
        skippedCount++;
        continue;
      }
      Department department = departmentMap.get(staff.getDepartmentId());
      StaffServicePlan plan = planMap.get(staff.getId());
      if (plan == null) {
        plan = new StaffServicePlan();
        plan.setOrgId(orgId);
        plan.setStaffId(staff.getId());
      }
      plan.setStaffNo(normalizeBlank(staff.getStaffNo()));
      plan.setStaffName(normalizeBlank(staff.getRealName()));
      plan.setDepartmentId(staff.getDepartmentId());
      plan.setDepartmentName(department == null ? null : normalizeBlank(department.getDeptName()));
      if (normalizeBlank(row.getDormitoryBuilding()) != null) {
        plan.setDormitoryBuilding(normalizeBlank(row.getDormitoryBuilding()));
      }
      if (normalizeBlank(row.getDormitoryRoomNo()) != null) {
        plan.setDormitoryRoomNo(normalizeBlank(row.getDormitoryRoomNo()));
      }
      if (normalizeBlank(row.getDormitoryBedNo()) != null) {
        plan.setDormitoryBedNo(normalizeBlank(row.getDormitoryBedNo()));
      }
      if (normalizeBlank(row.getMeterNo()) != null) {
        plan.setMeterNo(normalizeBlank(row.getMeterNo()));
      }
      if (normalizeFlag(plan.getLiveInDormitory()) == 0 && (
          normalizeBlank(plan.getDormitoryBuilding()) != null
              || normalizeBlank(plan.getDormitoryRoomNo()) != null
              || normalizeBlank(plan.getDormitoryBedNo()) != null
              || normalizeBlank(plan.getMeterNo()) != null)) {
        plan.setLiveInDormitory(1);
      }
      if (normalizeBlank(plan.getStatus()) == null) {
        plan.setStatus("ENABLED");
      }
      saveServicePlan(plan);
      planMap.put(staff.getId(), plan);

      StaffMonthlyFeeBill bill = findMonthlyFeeBill(orgId, staff.getId(), feeMonth.toString(), "ELECTRICITY");
      if (bill == null) {
        bill = new StaffMonthlyFeeBill();
        bill.setOrgId(orgId);
        bill.setStaffId(staff.getId());
        bill.setFeeMonth(feeMonth.toString());
        bill.setFeeType("ELECTRICITY");
        bill.setCreatedBy(operatorId);
      }
      bill.setStaffNo(normalizeBlank(staff.getStaffNo()));
      bill.setStaffName(normalizeBlank(staff.getRealName()));
      bill.setDepartmentId(staff.getDepartmentId());
      bill.setDepartmentName(department == null ? null : normalizeBlank(department.getDeptName()));
      bill.setTitle("员工电费 " + feeMonth + " " + defaultText(staff.getRealName(), defaultText(staff.getUsername(), "员工")));
      bill.setQuantity(java.math.BigDecimal.ONE);
      bill.setUnitPrice(normalizeMoney(row.getAmount()));
      bill.setAmount(normalizeMoney(row.getAmount()));
      bill.setStatus("IMPORTED");
      markFinancePending(bill);
      bill.setDormitoryBuilding(firstNonBlank(normalizeBlank(row.getDormitoryBuilding()), normalizeBlank(plan.getDormitoryBuilding())));
      bill.setDormitoryRoomNo(firstNonBlank(normalizeBlank(row.getDormitoryRoomNo()), normalizeBlank(plan.getDormitoryRoomNo())));
      bill.setDormitoryBedNo(firstNonBlank(normalizeBlank(row.getDormitoryBedNo()), normalizeBlank(plan.getDormitoryBedNo())));
      bill.setMeterNo(firstNonBlank(normalizeBlank(row.getMeterNo()), normalizeBlank(plan.getMeterNo())));
      bill.setDetailJson(writeJson(buildElectricityFeeDetail(row, plan)));
      bill.setRemark(normalizeBlank(row.getRemark()));
      saveMonthlyFeeBill(bill);
      successCount++;
      totalAmount = totalAmount.add(zeroIfNull(row.getAmount()));
    }
    HrBatchActionSummaryResponse response = new HrBatchActionSummaryResponse();
    response.setTotalCount(request.getRows().size());
    response.setSuccessCount(successCount);
    response.setSkippedCount(skippedCount);
    response.setTotalAmount(totalAmount);
    response.setMessage("员工电费已导入 " + feeMonth);
    return Result.ok(response);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/expense/monthly-fee/sync-finance")
  public Result<HrBatchActionSummaryResponse> syncMonthlyFeeToFinance(@RequestBody HrStaffFeeSyncRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    if (request == null || normalizeBlank(request.getFeeType()) == null) {
      return Result.error(400, "feeType required");
    }
    String feeType = request.getFeeType().trim().toUpperCase(Locale.ROOT);
    if (!Set.of("MEAL", "ELECTRICITY", "SOCIAL_SECURITY").contains(feeType)) {
      return Result.error(400, "feeType only supports MEAL/ELECTRICITY/SOCIAL_SECURITY");
    }
    YearMonth feeMonth = resolveFeeMonth(request.getMonth());
    List<Long> ids = sanitizeIds(request.getIds());
    List<StaffMonthlyFeeBill> rows = staffMonthlyFeeBillMapper.selectList(
        Wrappers.lambdaQuery(StaffMonthlyFeeBill.class)
            .eq(StaffMonthlyFeeBill::getOrgId, orgId)
            .eq(StaffMonthlyFeeBill::getIsDeleted, 0)
            .eq(StaffMonthlyFeeBill::getFeeType, feeType)
            .eq(StaffMonthlyFeeBill::getFeeMonth, feeMonth.toString())
            .in(!ids.isEmpty(), StaffMonthlyFeeBill::getId, ids)
            .orderByAsc(StaffMonthlyFeeBill::getStaffNo)
            .orderByAsc(StaffMonthlyFeeBill::getStaffName));
    int successCount = 0;
    int skippedCount = 0;
    java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
    for (StaffMonthlyFeeBill bill : rows) {
      if (zeroIfNull(bill.getAmount()).signum() <= 0) {
        skippedCount++;
        continue;
      }
      OaApproval approval = bill.getFinanceSyncId() == null ? null : oaApprovalMapper.selectOne(
          Wrappers.lambdaQuery(OaApproval.class)
              .eq(OaApproval::getId, bill.getFinanceSyncId())
              .eq(orgId != null, OaApproval::getOrgId, orgId)
              .eq(OaApproval::getIsDeleted, 0)
              .last("LIMIT 1"));
      if (approval == null) {
        approval = new OaApproval();
        approval.setTenantId(orgId);
        approval.setOrgId(orgId);
        approval.setApprovalType("REIMBURSE");
        approval.setApplicantName("HR自动同步");
        approval.setCreatedBy(operatorId);
      }
      approval.setTitle(bill.getTitle());
      approval.setAmount(bill.getAmount());
      approval.setStartTime(feeMonth.atDay(1).atStartOfDay());
      approval.setEndTime(feeMonth.atEndOfMonth().atTime(LocalTime.MAX));
      approval.setStatus("PENDING");
      approval.setRemark(buildFinanceSyncRemark(bill));
      approval.setFormData(writeJson(buildFinanceSyncFormData(bill)));
      if (approval.getId() == null) {
        oaApprovalMapper.insert(approval);
      } else {
        oaApprovalMapper.updateById(approval);
      }
      bill.setFinanceSyncStatus("SYNCED");
      bill.setFinanceSyncId(approval.getId());
      bill.setFinanceSyncAt(LocalDateTime.now());
      bill.setFinanceSyncBy(operatorId);
      staffMonthlyFeeBillMapper.updateById(bill);
      successCount++;
      totalAmount = totalAmount.add(zeroIfNull(bill.getAmount()));
    }
    HrBatchActionSummaryResponse response = new HrBatchActionSummaryResponse();
    response.setTotalCount(rows.size());
    response.setSuccessCount(successCount);
    response.setSkippedCount(skippedCount);
    response.setTotalAmount(totalAmount);
    response.setMessage("已同步到财务审批 " + feeMonth);
    return Result.ok(response);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/expense/training-reimburse/page")
  public Result<IPage<HrExpenseItemResponse>> expenseTrainingReimbursePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageExpenseByKeyword(pageNo, pageSize, keyword, status, "培训", "training"));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/expense/subsidy/page")
  public Result<IPage<HrExpenseItemResponse>> expenseSubsidyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageExpenseByKeyword(pageNo, pageSize, keyword, status, "补贴", "subsidy"));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/expense/salary-subsidy/page")
  public Result<IPage<HrExpenseItemResponse>> expenseSalarySubsidyPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageExpenseByKeyword(pageNo, pageSize, keyword, status, "工资补贴", "salary"));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/attendance/leave/page")
  public Result<IPage<HrGenericApprovalResponse>> attendanceLeavePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageGenericApprovals(pageNo, pageSize, keyword, status, "LEAVE", null));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/attendance/shift-change/page")
  public Result<IPage<HrGenericApprovalResponse>> attendanceShiftChangePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageGenericApprovals(pageNo, pageSize, keyword, status, "SHIFT_CHANGE", "shift-change"));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/attendance/overtime/page")
  public Result<IPage<HrGenericApprovalResponse>> attendanceOvertimePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    return Result.ok(pageGenericApprovals(pageNo, pageSize, keyword, status, "OVERTIME", null));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIdsSafe(
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
      row.setReviewed(item.getReviewed());
      row.setReviewRemark(item.getReviewRemark());
      row.setReviewedAt(item.getReviewedAt());
      return row;
    }).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/training")
  public Result<StaffTrainingRecord> createTraining(@RequestBody StaffTrainingRequest request) {
    Long orgId = AuthContext.getOrgId();
    StaffTrainingRecord record = new StaffTrainingRecord();
    record.setOrgId(orgId);
    applyTrainingRequest(record, request, false);
    if (record.getStatus() == null) {
      record.setStatus("PLAN".equals(record.getTrainingScene()) ? 0 : 1);
    }
    trainingRecordMapper.insert(record);
    syncTrainingCertificate(record);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "STAFF_TRAINING", record.getId(), "新增培训记录");
    return Result.ok(record);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PutMapping("/training/{id}")
  public Result<StaffTrainingRecord> updateTraining(@PathVariable Long id,
      @RequestBody StaffTrainingRequest request) {
    StaffTrainingRecord record = trainingRecordMapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (record == null || (orgId != null && !orgId.equals(record.getOrgId()))) {
      return Result.error(404, "Training record not found");
    }
    applyTrainingRequest(record, request, true);
    trainingRecordMapper.updateById(record);
    syncTrainingCertificate(record);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "STAFF_TRAINING", record.getId(), "更新培训记录");
    return Result.ok(record);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/training/page")
  public Result<IPage<StaffTrainingResponse>> trainingPage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String scene,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) Long departmentId,
      @RequestParam(required = false) Integer trainingYear,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    Long orgId = AuthContext.getOrgId();
    String resolvedScene = normalizeTrainingScene(scene, "RECORD");
    var wrapper = Wrappers.lambdaQuery(StaffTrainingRecord.class)
        .eq(StaffTrainingRecord::getIsDeleted, 0)
        .eq(orgId != null, StaffTrainingRecord::getOrgId, orgId)
        .eq(staffId != null, StaffTrainingRecord::getStaffId, staffId)
        .eq(departmentId != null, StaffTrainingRecord::getDepartmentId, departmentId)
        .eq(trainingYear != null, StaffTrainingRecord::getTrainingYear, trainingYear)
        .eq(StaffTrainingRecord::getTrainingScene, resolvedScene);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(StaffTrainingRecord::getTrainingName, keyword)
          .or().like(StaffTrainingRecord::getProvider, keyword)
          .or().like(StaffTrainingRecord::getInstructor, keyword)
          .or().like(StaffTrainingRecord::getStaffNo, keyword)
          .or().like(StaffTrainingRecord::getDepartmentName, keyword));
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

    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIdsSafe(
            page.getRecords().stream().map(StaffTrainingRecord::getStaffId).filter(id -> id != null).distinct().toList())
        .stream()
        .collect(Collectors.toMap(StaffAccount::getId, s -> s));
    Map<Long, Department> departmentMap = departmentMapper.selectBatchIds(
            page.getRecords().stream().map(StaffTrainingRecord::getDepartmentId).filter(id -> id != null).distinct().toList())
        .stream()
        .collect(Collectors.toMap(Department::getId, item -> item, (a, b) -> a));

    IPage<StaffTrainingResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(record -> {
      StaffTrainingResponse item = new StaffTrainingResponse();
      StaffAccount staff = staffMap.get(record.getStaffId());
      Department department = record.getDepartmentId() == null ? null : departmentMap.get(record.getDepartmentId());
      item.setId(record.getId());
      item.setSourceTrainingId(record.getSourceTrainingId());
      item.setTrainingScene(record.getTrainingScene());
      item.setTrainingYear(record.getTrainingYear());
      item.setDepartmentId(record.getDepartmentId());
      item.setDepartmentName(record.getDepartmentName() != null ? record.getDepartmentName() : (department == null ? null : department.getDeptName()));
      item.setStaffId(record.getStaffId());
      item.setStaffNo(record.getStaffNo() != null ? record.getStaffNo() : (staff == null ? null : staff.getStaffNo()));
      item.setStaffName(staff == null ? null : staff.getRealName());
      item.setTrainingName(record.getTrainingName());
      item.setTrainingType(record.getTrainingType());
      item.setProvider(record.getProvider());
      item.setInstructor(record.getInstructor());
      item.setStartDate(record.getStartDate());
      item.setEndDate(record.getEndDate());
      item.setHours(record.getHours());
      item.setScore(record.getScore());
      item.setAttendanceStatus(record.getAttendanceStatus());
      item.setTestResult(record.getTestResult());
      item.setCertificateRequired(record.getCertificateRequired());
      item.setCertificateStatus(record.getCertificateStatus());
      item.setStatus(record.getStatus());
      item.setCertificateNo(record.getCertificateNo());
      item.setAttachments(parseTrainingAttachments(record.getAttachmentsJson()));
      item.setRemark(record.getRemark());
      item.setCreateTime(record.getCreateTime());
      return item;
    }).toList());
    return Result.ok(resp);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/points/rule/page")
  public Result<IPage<StaffPointsRuleResponse>> rulePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize) {
    return Result.ok(staffPointsRuleService.page(AuthContext.getOrgId(), pageNo, pageSize));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/points/rule")
  public Result<StaffPointsRuleResponse> upsertRule(@RequestBody StaffPointsRuleRequest request) {
    return Result.ok(staffPointsRuleService.upsert(AuthContext.getOrgId(), request));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @DeleteMapping("/points/rule/{id}")
  public Result<Void> deleteRule(@PathVariable Long id) {
    staffPointsRuleService.delete(AuthContext.getOrgId(), id);
    return Result.ok(null);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/performance/summary")
  public Result<StaffPerformanceSummaryResponse> performanceSummary(
      @RequestParam Long staffId,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo) {
    return Result.ok(performanceService.summary(AuthContext.getOrgId(), staffId, dateFrom, dateTo));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
    response.setCandidateName(asString(ext.get("candidateName")));
    response.setContactPhone(asString(ext.get("contactPhone")));
    response.setResumeUrl(asString(ext.get("resumeUrl")));
    response.setIntentionStatus(asString(ext.get("intentionStatus")));
    response.setFollowUpDate(parseDate(asString(ext.get("followUpDate"))));
    response.setOfferStatus(asString(ext.get("offerStatus")));
    response.setOnboardDate(parseDate(asString(ext.get("onboardDate"))));
    response.setSalary(asString(ext.get("salary")));
    response.setProbationPeriod(asString(ext.get("probationPeriod")));
    response.setWorkLocation(asString(ext.get("workLocation")));
    response.setShiftType(asString(ext.get("shiftType")));
    response.setChecklistJson(asString(ext.get("checklistJson")));
    response.setSignedFilesJson(asString(ext.get("signedFilesJson")));
    response.setAccountPermissionJson(asString(ext.get("accountPermissionJson")));
    response.setIssuedItemsJson(asString(ext.get("issuedItemsJson")));
    response.setMentorName(asString(ext.get("mentorName")));
    response.setProbationGoal(asString(ext.get("probationGoal")));
    response.setRegularizationStatus(asString(ext.get("regularizationStatus")));
    response.setRecommendationNote(asString(ext.get("recommendationNote")));
    response.setOffboardingType(asString(ext.get("offboardingType")));
    response.setLastWorkDate(parseDate(asString(ext.get("lastWorkDate"))));
    response.setHandoverDeadline(parseDate(asString(ext.get("handoverDeadline"))));
    response.setResignationReason(asString(ext.get("resignationReason")));
    response.setResignationReportUrl(asString(ext.get("resignationReportUrl")));
    response.setHandoverItemsJson(asString(ext.get("handoverItemsJson")));
    response.setAssetRecoveryJson(asString(ext.get("assetRecoveryJson")));
    response.setPermissionRecycleJson(asString(ext.get("permissionRecycleJson")));
    response.setFinanceSettlementNote(asString(ext.get("financeSettlementNote")));
    response.setExitArchiveJson(asString(ext.get("exitArchiveJson")));
    response.setFormData(item == null ? null : item.getFormData());
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

  private HrProfileContractResponse toProfileContractResponse(StaffProfile item, StaffAccount staff, LocalDate today) {
    HrProfileContractResponse response = new HrProfileContractResponse();
    response.setContractId(item == null ? null : item.getId());
    response.setStaffId(staff == null ? null : staff.getId());
    response.setStaffNo(staff == null ? null : staff.getStaffNo());
    response.setStaffName(staff == null ? null : staff.getRealName());
    response.setPhone(staff == null ? null : staff.getPhone());
    response.setJobTitle(item == null ? null : item.getJobTitle());
    response.setEmploymentType(item == null ? null : item.getEmploymentType());
    response.setContractNo(item == null ? null : item.getContractNo());
    response.setContractStatus(item == null ? null : item.getContractStatus());
    response.setContractType(item == null ? null : item.getContractType());
    response.setContractStartDate(item == null ? null : item.getContractStartDate());
    response.setContractEndDate(item == null ? null : item.getContractEndDate());
    if (item != null && item.getContractEndDate() != null && today != null) {
      response.setRemainingDays(ChronoUnit.DAYS.between(today, item.getContractEndDate()));
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
    entity.setUploadedAt(parseDateTime(request == null ? null : request.getUploadedAt(), LocalDateTime.now()));
    entity.setRemark(request == null ? null : request.getRemark());
    entity.setCreatedBy(AuthContext.getStaffId());
    oaDocumentMapper.insert(entity);
    return entity;
  }

  private IPage<HrStaffCertificateResponse> toCertificatePage(IPage<StaffTrainingRecord> source, LocalDate today) {
    List<Long> staffIds = source.getRecords().stream().map(StaffTrainingRecord::getStaffId).distinct().toList();
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIdsSafe(staffIds)
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

  private void applyTrainingRequest(StaffTrainingRecord record, StaffTrainingRequest request, boolean partialUpdate) {
    if (!partialUpdate || request.getSourceTrainingId() != null) {
      record.setSourceTrainingId(request.getSourceTrainingId());
    }
    if (!partialUpdate || request.getTrainingScene() != null) {
      record.setTrainingScene(normalizeTrainingScene(request.getTrainingScene(), record.getTrainingScene()));
    }
    if (!partialUpdate || request.getTrainingYear() != null) {
      record.setTrainingYear(request.getTrainingYear());
    }
    if (!partialUpdate || request.getStaffId() != null) {
      record.setStaffId(request.getStaffId());
    }
    if (!partialUpdate || request.getTrainingName() != null) {
      record.setTrainingName(request.getTrainingName());
    }
    if (!partialUpdate || request.getTrainingType() != null) {
      record.setTrainingType(request.getTrainingType());
    }
    if (!partialUpdate || request.getProvider() != null) {
      record.setProvider(request.getProvider());
    }
    if (!partialUpdate || request.getInstructor() != null) {
      record.setInstructor(request.getInstructor());
    }
    if (!partialUpdate || request.getStartDate() != null) {
      record.setStartDate(request.getStartDate());
    }
    if (!partialUpdate || request.getEndDate() != null) {
      record.setEndDate(request.getEndDate());
    }
    if (!partialUpdate || request.getHours() != null) {
      record.setHours(request.getHours());
    }
    if (!partialUpdate || request.getScore() != null) {
      record.setScore(request.getScore());
    }
    if (!partialUpdate || request.getAttendanceStatus() != null) {
      record.setAttendanceStatus(normalizeOptionalText(request.getAttendanceStatus()));
    }
    if (!partialUpdate || request.getTestResult() != null) {
      record.setTestResult(normalizeOptionalText(request.getTestResult()));
    }
    if (!partialUpdate || request.getCertificateRequired() != null) {
      record.setCertificateRequired(request.getCertificateRequired() == null ? 0 : request.getCertificateRequired());
    }
    if (!partialUpdate || request.getCertificateStatus() != null) {
      record.setCertificateStatus(normalizeCertificateStatus(request.getCertificateStatus(), record.getCertificateStatus()));
    }
    if (!partialUpdate || request.getStatus() != null) {
      record.setStatus(request.getStatus());
    }
    if (!partialUpdate || request.getCertificateNo() != null) {
      record.setCertificateNo(normalizeOptionalText(request.getCertificateNo()));
    }
    if (!partialUpdate || request.getRemark() != null) {
      record.setRemark(request.getRemark());
    }
    if (!partialUpdate || request.getAttachments() != null) {
      record.setAttachmentsJson(toAttachmentsJson(request.getAttachments()));
    }
    fillTrainingSnapshots(
        record,
        record.getStaffId(),
        request.getDepartmentId(),
        request.getDepartmentName(),
        request.getStartDate() != null ? request.getStartDate() : record.getStartDate(),
        partialUpdate);
    if (record.getCertificateRequired() == null) {
      record.setCertificateRequired(0);
    }
    if (record.getCertificateRequired() != null && record.getCertificateRequired() == 1) {
      if (normalizeOptionalText(record.getCertificateStatus()) == null) {
        record.setCertificateStatus("PENDING");
      }
    } else {
      record.setCertificateStatus("NONE");
    }
  }

  private void fillTrainingSnapshots(
      StaffTrainingRecord record,
      Long staffId,
      Long requestedDepartmentId,
      String requestedDepartmentName,
      LocalDate fallbackDate,
      boolean partialUpdate) {
    StaffAccount staff = staffId == null ? null : staffMapper.selectById(staffId);
    Long resolvedDepartmentId = requestedDepartmentId != null
        ? requestedDepartmentId
        : (staff == null ? record.getDepartmentId() : staff.getDepartmentId());
    Department department = resolvedDepartmentId == null ? null : departmentMapper.selectById(resolvedDepartmentId);

    if (!partialUpdate || staffId != null) {
      record.setStaffId(staffId);
      record.setStaffNo(staff == null ? record.getStaffNo() : staff.getStaffNo());
    }
    if (!partialUpdate || requestedDepartmentId != null || staffId != null) {
      record.setDepartmentId(resolvedDepartmentId);
    }
    if (!partialUpdate || requestedDepartmentName != null || requestedDepartmentId != null || staffId != null) {
      String departmentName = requestedDepartmentName != null && !requestedDepartmentName.isBlank()
          ? requestedDepartmentName.trim()
          : (department == null ? record.getDepartmentName() : department.getDeptName());
      record.setDepartmentName(departmentName);
    }
    if (record.getTrainingYear() == null) {
      LocalDate baseDate = fallbackDate != null ? fallbackDate : record.getEndDate();
      record.setTrainingYear(baseDate == null ? LocalDate.now().getYear() : baseDate.getYear());
    }
  }

  private void syncTrainingCertificate(StaffTrainingRecord record) {
    if (record == null
        || !"RECORD".equals(normalizeTrainingScene(record.getTrainingScene(), "RECORD"))
        || record.getStaffId() == null
        || record.getStatus() == null
        || record.getStatus() != 1
        || record.getCertificateRequired() == null
        || record.getCertificateRequired() != 1) {
      return;
    }
    StaffTrainingRecord certificateRecord = trainingRecordMapper.selectOne(Wrappers.lambdaQuery(StaffTrainingRecord.class)
        .eq(StaffTrainingRecord::getIsDeleted, 0)
        .eq(StaffTrainingRecord::getOrgId, record.getOrgId())
        .eq(StaffTrainingRecord::getSourceTrainingId, record.getId())
        .eq(StaffTrainingRecord::getTrainingScene, "CERTIFICATE")
        .last("LIMIT 1"));
    if (certificateRecord == null) {
      certificateRecord = new StaffTrainingRecord();
      certificateRecord.setOrgId(record.getOrgId());
      certificateRecord.setSourceTrainingId(record.getId());
      certificateRecord.setTrainingScene("CERTIFICATE");
      certificateRecord.setTrainingType("CERTIFICATE");
      certificateRecord.setStatus(1);
      certificateRecord.setCertificateRequired(1);
    }
    certificateRecord.setStaffId(record.getStaffId());
    certificateRecord.setStaffNo(record.getStaffNo());
    certificateRecord.setDepartmentId(record.getDepartmentId());
    certificateRecord.setDepartmentName(record.getDepartmentName());
    certificateRecord.setTrainingYear(record.getTrainingYear());
    certificateRecord.setTrainingName(record.getTrainingName());
    certificateRecord.setProvider(record.getInstructor() != null && !record.getInstructor().isBlank() ? record.getInstructor() : record.getProvider());
    certificateRecord.setInstructor(record.getInstructor());
    certificateRecord.setStartDate(record.getEndDate() != null ? record.getEndDate() : record.getStartDate());
    certificateRecord.setEndDate(null);
    certificateRecord.setHours(record.getHours());
    certificateRecord.setScore(record.getScore());
    certificateRecord.setAttendanceStatus(record.getAttendanceStatus());
    certificateRecord.setTestResult(record.getTestResult());
    certificateRecord.setCertificateStatus("ISSUED");
    certificateRecord.setCertificateNo(resolveCertificateNo(record));
    certificateRecord.setRemark(buildAutoCertificateRemark(record));
    if (certificateRecord.getId() == null) {
      trainingRecordMapper.insert(certificateRecord);
    } else {
      trainingRecordMapper.updateById(certificateRecord);
    }
    record.setCertificateStatus("GENERATED");
    record.setCertificateNo(certificateRecord.getCertificateNo());
    trainingRecordMapper.updateById(record);
  }

  private String buildAutoCertificateRemark(StaffTrainingRecord record) {
    String base = normalizeOptionalText(record.getRemark());
    String prefix = "培训完成自动生成证书";
    return base == null ? prefix : prefix + "；" + base;
  }

  private String resolveCertificateNo(StaffTrainingRecord record) {
    String existing = normalizeOptionalText(record.getCertificateNo());
    if (existing != null) {
      return existing;
    }
    String staffNo = normalizeOptionalText(record.getStaffNo());
    String suffix = staffNo != null ? staffNo : String.valueOf(record.getStaffId());
    String date = (record.getEndDate() != null ? record.getEndDate() : LocalDate.now()).format(DateTimeFormatter.BASIC_ISO_DATE);
    return "TRN-" + date + "-" + suffix;
  }

  private String normalizeTrainingScene(String scene, String fallback) {
    String normalized = normalizeOptionalText(scene);
    if (normalized == null) {
      return fallback == null ? "RECORD" : fallback;
    }
    normalized = normalized.toUpperCase(Locale.ROOT);
    if (!List.of("PLAN", "RECORD", "CERTIFICATE").contains(normalized)) {
      return fallback == null ? "RECORD" : fallback;
    }
    return normalized;
  }

  private String normalizeCertificateStatus(String status, String fallback) {
    String normalized = normalizeOptionalText(status);
    if (normalized == null) {
      return fallback;
    }
    return normalized.toUpperCase(Locale.ROOT);
  }

  private String normalizeOptionalText(String value) {
    return normalizeBlank(value);
  }

  private List<Map<String, Object>> parseTrainingAttachments(String attachmentsJson) {
    String normalized = normalizeOptionalText(attachmentsJson);
    if (normalized == null) {
      return List.of();
    }
    try {
      return objectMapper.readValue(normalized, new TypeReference<List<Map<String, Object>>>() {});
    } catch (Exception ex) {
      return List.of();
    }
  }

  private String toAttachmentsJson(List<Map<String, Object>> attachments) {
    if (attachments == null || attachments.isEmpty()) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(attachments);
    } catch (Exception ex) {
      throw new IllegalArgumentException("培训附件数据格式错误");
    }
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
    if (title.contains("社保") || unionText.contains("social_security") || unionText.contains("social security")) {
      return "社保费用";
    }
    if (title.contains("培训") || unionText.contains("training")) {
      return "培训费用";
    }
    return "报销";
  }

  private IPage<HrStaffMonthlyFeeBillResponse> pageStaffMonthlyFee(
      long pageNo, long pageSize, String keyword, String month, String financeSyncStatus, String feeType) {
    Long orgId = AuthContext.getOrgId();
    YearMonth feeMonth = resolveFeeMonth(month);
    String normalizedKeyword = normalizeBlank(keyword);
    String normalizedFinanceSyncStatus = normalizeBlank(financeSyncStatus);
    var wrapper = Wrappers.lambdaQuery(StaffMonthlyFeeBill.class)
        .eq(StaffMonthlyFeeBill::getOrgId, orgId)
        .eq(StaffMonthlyFeeBill::getIsDeleted, 0)
        .eq(StaffMonthlyFeeBill::getFeeMonth, feeMonth.toString())
        .eq(StaffMonthlyFeeBill::getFeeType, feeType)
        .eq(normalizedFinanceSyncStatus != null, StaffMonthlyFeeBill::getFinanceSyncStatus, normalizedFinanceSyncStatus)
        .orderByAsc(StaffMonthlyFeeBill::getDepartmentName)
        .orderByAsc(StaffMonthlyFeeBill::getStaffNo)
        .orderByAsc(StaffMonthlyFeeBill::getStaffName)
        .orderByDesc(StaffMonthlyFeeBill::getUpdateTime);
    if (normalizedKeyword != null) {
      wrapper.and(w -> w.like(StaffMonthlyFeeBill::getStaffNo, normalizedKeyword)
          .or().like(StaffMonthlyFeeBill::getStaffName, normalizedKeyword)
          .or().like(StaffMonthlyFeeBill::getDepartmentName, normalizedKeyword)
          .or().like(StaffMonthlyFeeBill::getTitle, normalizedKeyword)
          .or().like(StaffMonthlyFeeBill::getDormitoryRoomNo, normalizedKeyword)
          .or().like(StaffMonthlyFeeBill::getMeterNo, normalizedKeyword)
          .or().like(StaffMonthlyFeeBill::getRemark, normalizedKeyword));
    }
    IPage<StaffMonthlyFeeBill> page = staffMonthlyFeeBillMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, String> financeSyncStaffNameMap = loadStaffNameMap(page.getRecords().stream()
        .map(StaffMonthlyFeeBill::getFinanceSyncBy)
        .toList());
    IPage<HrStaffMonthlyFeeBillResponse> response = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    response.setRecords(page.getRecords().stream()
        .map(item -> toMonthlyFeeBillResponse(item, financeSyncStaffNameMap))
        .toList());
    return response;
  }

  private HrStaffMonthlyFeeBillResponse toMonthlyFeeBillResponse(
      StaffMonthlyFeeBill item, Map<Long, String> financeSyncStaffNameMap) {
    HrStaffMonthlyFeeBillResponse row = new HrStaffMonthlyFeeBillResponse();
    row.setId(item.getId());
    row.setStaffId(item.getStaffId());
    row.setStaffNo(item.getStaffNo());
    row.setStaffName(item.getStaffName());
    row.setDepartmentId(item.getDepartmentId());
    row.setDepartmentName(item.getDepartmentName());
    row.setFeeMonth(item.getFeeMonth());
    row.setFeeType(item.getFeeType());
    row.setTitle(item.getTitle());
    row.setQuantity(item.getQuantity());
    row.setUnitPrice(item.getUnitPrice());
    row.setAmount(item.getAmount());
    row.setStatus(item.getStatus());
    row.setFinanceSyncStatus(item.getFinanceSyncStatus());
    row.setFinanceSyncId(item.getFinanceSyncId());
    row.setFinanceSyncAt(item.getFinanceSyncAt());
    row.setFinanceSyncByName(financeSyncStaffNameMap.get(item.getFinanceSyncBy()));
    row.setDormitoryBuilding(item.getDormitoryBuilding());
    row.setDormitoryRoomNo(item.getDormitoryRoomNo());
    row.setDormitoryBedNo(item.getDormitoryBedNo());
    row.setMeterNo(item.getMeterNo());
    row.setRemark(item.getRemark());
    row.setCreateTime(item.getCreateTime());
    row.setUpdateTime(item.getUpdateTime());
    Map<String, Object> detail = parseJson(item.getDetailJson());
    row.setMealPlanSummary(asString(detail.get("mealPlanSummary")));
    row.setDetailSummary(asString(detail.get("detailSummary")));
    return row;
  }

  private StaffAccount findStaff(Long orgId, Long staffId) {
    if (staffId == null) {
      return null;
    }
    return staffMapper.selectOne(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getId, staffId)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .eq(StaffAccount::getIsDeleted, 0)
        .last("LIMIT 1"));
  }

  private List<StaffAccount> listActiveStaff(Long orgId) {
    return staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(StaffAccount::getStatus, 1)
        .orderByAsc(StaffAccount::getDepartmentId)
        .orderByAsc(StaffAccount::getStaffNo)
        .orderByAsc(StaffAccount::getRealName));
  }

  private Map<Long, Department> loadDepartmentMap(List<StaffAccount> staffRows) {
    List<Long> departmentIds = sanitizeIds(staffRows.stream()
        .map(StaffAccount::getDepartmentId)
        .toList());
    if (departmentIds.isEmpty()) {
      return Map.of();
    }
    return departmentMapper.selectBatchIds(departmentIds).stream()
        .filter(item -> item.getId() != null)
        .collect(Collectors.toMap(Department::getId, item -> item, (a, b) -> a));
  }

  private Map<Long, StaffServicePlan> loadServicePlanMap(Long orgId, List<Long> staffIds) {
    List<Long> sanitizedStaffIds = sanitizeIds(staffIds);
    if (sanitizedStaffIds.isEmpty()) {
      return Map.of();
    }
    return staffServicePlanMapper.selectList(
            Wrappers.lambdaQuery(StaffServicePlan.class)
                .eq(StaffServicePlan::getOrgId, orgId)
                .eq(StaffServicePlan::getIsDeleted, 0)
                .in(StaffServicePlan::getStaffId, sanitizedStaffIds))
        .stream()
        .filter(item -> item.getStaffId() != null)
        .collect(Collectors.toMap(StaffServicePlan::getStaffId, item -> item, (a, b) -> a));
  }

  private List<HrDormitoryStaffResponse> buildDormitoryRows(Long orgId, Integer staffStatus) {
    List<StaffAccount> staffList = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .ne(StaffAccount::getUsername, HIDDEN_PLATFORM_USERNAME)
        .eq(staffStatus != null, StaffAccount::getStatus, staffStatus));
    if (staffList == null || staffList.isEmpty()) {
      return List.of();
    }
    Map<Long, Department> departmentMap = loadDepartmentMap(staffList);
    Map<Long, StaffServicePlan> planMap = loadServicePlanMap(orgId, staffList.stream().map(StaffAccount::getId).toList());
    Map<String, Long> occupancyCountMap = planMap.values().stream()
        .map(this::dormitoryOccupancyKey)
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(key -> key, Collectors.counting()));
    return staffList.stream()
        .map(staff -> {
          StaffServicePlan plan = planMap.get(staff.getId());
          String occupancyKey = dormitoryOccupancyKey(plan);
          boolean occupancyConflict = occupancyKey != null && occupancyCountMap.getOrDefault(occupancyKey, 0L) > 1;
          return toDormitoryResponse(plan, staff, departmentMap.get(staff.getDepartmentId()), occupancyConflict);
        })
        .sorted(Comparator
            .comparing((HrDormitoryStaffResponse item) -> Boolean.TRUE.equals(item.getOccupancyConflict())).reversed()
            .thenComparing(item -> normalizeFlag(item.getLiveInDormitory()), Comparator.reverseOrder())
            .thenComparing(HrDormitoryStaffResponse::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(item -> defaultText(item.getStaffName(), defaultText(item.getStaffNo(), ""))))
        .toList();
  }

  private List<HrDormitoryRoomConfigResponse> listDormitoryRoomConfigs(
      Long orgId,
      String keyword,
      String building,
      String floorLabel,
      String roomNo,
      String status) {
    String normalizedKeyword = normalizeBlank(keyword);
    return dormitoryRoomConfigMapper.selectList(
            Wrappers.lambdaQuery(HrDormitoryRoomConfig.class)
                .eq(HrDormitoryRoomConfig::getOrgId, orgId)
                .eq(HrDormitoryRoomConfig::getIsDeleted, 0)
                .eq(normalizeBlank(building) != null, HrDormitoryRoomConfig::getBuilding, normalizeBlank(building))
                .eq(normalizeBlank(floorLabel) != null, HrDormitoryRoomConfig::getFloorLabel, normalizeBlank(floorLabel))
                .eq(normalizeBlank(roomNo) != null, HrDormitoryRoomConfig::getRoomNo, normalizeBlank(roomNo))
                .eq(normalizeBlank(status) != null, HrDormitoryRoomConfig::getStatus, normalizeBlank(status).toUpperCase(Locale.ROOT))
                .and(normalizedKeyword != null, wrapper -> wrapper
                    .like(HrDormitoryRoomConfig::getBuilding, normalizedKeyword)
                    .or()
                    .like(HrDormitoryRoomConfig::getFloorLabel, normalizedKeyword)
                    .or()
                    .like(HrDormitoryRoomConfig::getRoomNo, normalizedKeyword)
                    .or()
                    .like(HrDormitoryRoomConfig::getRemark, normalizedKeyword))
                .orderByAsc(HrDormitoryRoomConfig::getBuilding)
                .orderByAsc(HrDormitoryRoomConfig::getSortNo)
                .orderByAsc(HrDormitoryRoomConfig::getRoomNo))
        .stream()
        .map(this::toDormitoryRoomConfigResponse)
        .toList();
  }

  private StaffMonthlyFeeBill findMonthlyFeeBill(Long orgId, Long staffId, String feeMonth, String feeType) {
    return staffMonthlyFeeBillMapper.selectOne(
        Wrappers.lambdaQuery(StaffMonthlyFeeBill.class)
            .eq(StaffMonthlyFeeBill::getOrgId, orgId)
            .eq(StaffMonthlyFeeBill::getStaffId, staffId)
            .eq(StaffMonthlyFeeBill::getFeeMonth, feeMonth)
            .eq(StaffMonthlyFeeBill::getFeeType, feeType)
            .eq(StaffMonthlyFeeBill::getIsDeleted, 0)
            .last("LIMIT 1"));
  }

  private HrStaffServicePlanResponse toServicePlanResponse(
      StaffServicePlan plan, StaffAccount staff, Department department) {
    HrStaffServicePlanResponse response = new HrStaffServicePlanResponse();
    response.setId(plan == null ? null : plan.getId());
    response.setStaffId(staff == null ? (plan == null ? null : plan.getStaffId()) : staff.getId());
    response.setStaffNo(staff == null ? (plan == null ? null : plan.getStaffNo()) : staff.getStaffNo());
    response.setStaffName(staff == null ? (plan == null ? null : plan.getStaffName()) : staff.getRealName());
    response.setDepartmentId(staff == null ? (plan == null ? null : plan.getDepartmentId()) : staff.getDepartmentId());
    response.setDepartmentName(department == null ? (plan == null ? null : plan.getDepartmentName()) : department.getDeptName());
    response.setBreakfastEnabled(plan == null ? 0 : normalizeFlag(plan.getBreakfastEnabled()));
    response.setBreakfastUnitPrice(plan == null ? null : plan.getBreakfastUnitPrice());
    response.setBreakfastDaysPerMonth(plan == null ? null : plan.getBreakfastDaysPerMonth());
    response.setLunchEnabled(plan == null ? 0 : normalizeFlag(plan.getLunchEnabled()));
    response.setLunchUnitPrice(plan == null ? null : plan.getLunchUnitPrice());
    response.setLunchDaysPerMonth(plan == null ? null : plan.getLunchDaysPerMonth());
    response.setDinnerEnabled(plan == null ? 0 : normalizeFlag(plan.getDinnerEnabled()));
    response.setDinnerUnitPrice(plan == null ? null : plan.getDinnerUnitPrice());
    response.setDinnerDaysPerMonth(plan == null ? null : plan.getDinnerDaysPerMonth());
    response.setLiveInDormitory(plan == null ? 0 : normalizeFlag(plan.getLiveInDormitory()));
    response.setDormitoryBuilding(plan == null ? null : plan.getDormitoryBuilding());
    response.setDormitoryRoomNo(plan == null ? null : plan.getDormitoryRoomNo());
    response.setDormitoryBedNo(plan == null ? null : plan.getDormitoryBedNo());
    response.setMeterNo(plan == null ? null : plan.getMeterNo());
    response.setStatus(plan == null ? "ENABLED" : defaultText(plan.getStatus(), "ENABLED"));
    response.setMealPlanSummary(buildMealPlanSummary(plan));
    response.setRemark(plan == null ? null : plan.getRemark());
    response.setUpdateTime(plan == null ? null : plan.getUpdateTime());
    return response;
  }

  private HrDormitoryStaffResponse toDormitoryResponse(
      StaffServicePlan plan, StaffAccount staff, Department department, boolean occupancyConflict) {
    HrDormitoryStaffResponse response = new HrDormitoryStaffResponse();
    response.setStaffId(staff == null ? null : staff.getId());
    response.setStaffNo(staff == null ? null : staff.getStaffNo());
    response.setStaffName(staff == null ? null : staff.getRealName());
    response.setDepartmentId(staff == null ? null : staff.getDepartmentId());
    response.setDepartmentName(department == null ? (plan == null ? null : plan.getDepartmentName()) : department.getDeptName());
    response.setStatus(staff == null ? null : staff.getStatus());
    response.setLiveInDormitory(plan == null ? 0 : normalizeFlag(plan.getLiveInDormitory()));
    response.setDormitoryBuilding(plan == null ? null : normalizeBlank(plan.getDormitoryBuilding()));
    response.setDormitoryRoomNo(plan == null ? null : normalizeBlank(plan.getDormitoryRoomNo()));
    response.setDormitoryBedNo(plan == null ? null : normalizeBlank(plan.getDormitoryBedNo()));
    response.setDormitoryLabel(buildDormitoryLabel(plan));
    response.setMeterNo(plan == null ? null : normalizeBlank(plan.getMeterNo()));
    response.setMealPlanSummary(buildMealPlanSummary(plan));
    response.setRemark(plan == null ? null : normalizeBlank(plan.getRemark()));
    response.setOccupancyConflict(occupancyConflict);
    response.setUpdateTime(plan == null ? null : plan.getUpdateTime());
    return response;
  }

  private HrDormitoryRoomConfigResponse toDormitoryRoomConfigResponse(HrDormitoryRoomConfig entity) {
    HrDormitoryRoomConfigResponse response = new HrDormitoryRoomConfigResponse();
    response.setId(entity == null ? null : entity.getId());
    response.setBuilding(entity == null ? null : normalizeBlank(entity.getBuilding()));
    response.setFloorLabel(entity == null ? null : firstNonBlank(normalizeBlank(entity.getFloorLabel()), resolveDormitoryFloorLabel(entity.getRoomNo())));
    response.setRoomNo(entity == null ? null : normalizeBlank(entity.getRoomNo()));
    response.setBedCapacity(entity == null ? null : entity.getBedCapacity());
    response.setStatus(entity == null ? null : defaultText(normalizeBlank(entity.getStatus()), "ENABLED"));
    response.setSortNo(entity == null ? null : entity.getSortNo());
    response.setRemark(entity == null ? null : normalizeBlank(entity.getRemark()));
    response.setUpdateTime(entity == null ? null : entity.getUpdateTime());
    return response;
  }

  private void saveServicePlan(StaffServicePlan plan) {
    if (plan.getId() == null) {
      staffServicePlanMapper.insert(plan);
    } else {
      staffServicePlanMapper.updateById(plan);
    }
  }

  private void saveDormitoryRoomConfig(HrDormitoryRoomConfig entity) {
    if (entity.getId() == null) {
      dormitoryRoomConfigMapper.insert(entity);
    } else {
      dormitoryRoomConfigMapper.updateById(entity);
    }
  }

  private void saveMonthlyFeeBill(StaffMonthlyFeeBill bill) {
    if (bill.getId() == null) {
      staffMonthlyFeeBillMapper.insert(bill);
    } else {
      staffMonthlyFeeBillMapper.updateById(bill);
    }
  }

  private boolean isPlanEnabled(StaffServicePlan plan) {
    return plan != null && !"DISABLED".equalsIgnoreCase(defaultText(plan.getStatus(), "ENABLED"));
  }

  private int mealEnabledCount(StaffServicePlan plan) {
    int count = 0;
    if (plan != null && normalizeFlag(plan.getBreakfastEnabled()) == 1) count++;
    if (plan != null && normalizeFlag(plan.getLunchEnabled()) == 1) count++;
    if (plan != null && normalizeFlag(plan.getDinnerEnabled()) == 1) count++;
    return count;
  }

  private int totalMealDays(StaffServicePlan plan) {
    if (plan == null) {
      return 0;
    }
    int total = 0;
    if (normalizeFlag(plan.getBreakfastEnabled()) == 1) {
      total += normalizeDays(plan.getBreakfastDaysPerMonth());
    }
    if (normalizeFlag(plan.getLunchEnabled()) == 1) {
      total += normalizeDays(plan.getLunchDaysPerMonth());
    }
    if (normalizeFlag(plan.getDinnerEnabled()) == 1) {
      total += normalizeDays(plan.getDinnerDaysPerMonth());
    }
    return total;
  }

  private java.math.BigDecimal mealBillAmount(StaffServicePlan plan) {
    if (plan == null) {
      return java.math.BigDecimal.ZERO;
    }
    java.math.BigDecimal total = java.math.BigDecimal.ZERO;
    if (normalizeFlag(plan.getBreakfastEnabled()) == 1) {
      total = total.add(zeroIfNull(plan.getBreakfastUnitPrice())
          .multiply(java.math.BigDecimal.valueOf(normalizeDays(plan.getBreakfastDaysPerMonth()))));
    }
    if (normalizeFlag(plan.getLunchEnabled()) == 1) {
      total = total.add(zeroIfNull(plan.getLunchUnitPrice())
          .multiply(java.math.BigDecimal.valueOf(normalizeDays(plan.getLunchDaysPerMonth()))));
    }
    if (normalizeFlag(plan.getDinnerEnabled()) == 1) {
      total = total.add(zeroIfNull(plan.getDinnerUnitPrice())
          .multiply(java.math.BigDecimal.valueOf(normalizeDays(plan.getDinnerDaysPerMonth()))));
    }
    return total;
  }

  private java.math.BigDecimal resolveAverageUnitPrice(java.math.BigDecimal totalAmount, int totalDays) {
    if (totalDays <= 0 || totalAmount == null || totalAmount.signum() <= 0) {
      return java.math.BigDecimal.ZERO;
    }
    return totalAmount.divide(java.math.BigDecimal.valueOf(totalDays), 2, java.math.RoundingMode.HALF_UP);
  }

  private Map<String, Object> buildMealFeeDetail(StaffServicePlan plan) {
    Map<String, Object> detail = new HashMap<>();
    List<String> lines = new ArrayList<>();
    if (normalizeFlag(plan.getBreakfastEnabled()) == 1) {
      lines.add("早餐 " + normalizeDays(plan.getBreakfastDaysPerMonth()) + "天 x "
          + zeroIfNull(plan.getBreakfastUnitPrice()).setScale(2, java.math.RoundingMode.HALF_UP) + "元");
    }
    if (normalizeFlag(plan.getLunchEnabled()) == 1) {
      lines.add("中餐 " + normalizeDays(plan.getLunchDaysPerMonth()) + "天 x "
          + zeroIfNull(plan.getLunchUnitPrice()).setScale(2, java.math.RoundingMode.HALF_UP) + "元");
    }
    if (normalizeFlag(plan.getDinnerEnabled()) == 1) {
      lines.add("晚餐 " + normalizeDays(plan.getDinnerDaysPerMonth()) + "天 x "
          + zeroIfNull(plan.getDinnerUnitPrice()).setScale(2, java.math.RoundingMode.HALF_UP) + "元");
    }
    detail.put("mealPlanSummary", buildMealPlanSummary(plan));
    detail.put("detailSummary", String.join("；", lines));
    detail.put("breakfastEnabled", normalizeFlag(plan.getBreakfastEnabled()));
    detail.put("breakfastUnitPrice", plan.getBreakfastUnitPrice());
    detail.put("breakfastDaysPerMonth", normalizeDays(plan.getBreakfastDaysPerMonth()));
    detail.put("lunchEnabled", normalizeFlag(plan.getLunchEnabled()));
    detail.put("lunchUnitPrice", plan.getLunchUnitPrice());
    detail.put("lunchDaysPerMonth", normalizeDays(plan.getLunchDaysPerMonth()));
    detail.put("dinnerEnabled", normalizeFlag(plan.getDinnerEnabled()));
    detail.put("dinnerUnitPrice", plan.getDinnerUnitPrice());
    detail.put("dinnerDaysPerMonth", normalizeDays(plan.getDinnerDaysPerMonth()));
    return detail;
  }

  private Map<String, Object> buildElectricityFeeDetail(HrStaffElectricityImportRowRequest row, StaffServicePlan plan) {
    Map<String, Object> detail = new HashMap<>();
    detail.put("detailSummary", "电费上传金额 " + zeroIfNull(row.getAmount()).setScale(2, java.math.RoundingMode.HALF_UP) + "元");
    detail.put("mealPlanSummary", buildMealPlanSummary(plan));
    detail.put("dormitoryBuilding", firstNonBlank(normalizeBlank(row.getDormitoryBuilding()), plan == null ? null : normalizeBlank(plan.getDormitoryBuilding())));
    detail.put("dormitoryRoomNo", firstNonBlank(normalizeBlank(row.getDormitoryRoomNo()), plan == null ? null : normalizeBlank(plan.getDormitoryRoomNo())));
    detail.put("dormitoryBedNo", firstNonBlank(normalizeBlank(row.getDormitoryBedNo()), plan == null ? null : normalizeBlank(plan.getDormitoryBedNo())));
    detail.put("meterNo", firstNonBlank(normalizeBlank(row.getMeterNo()), plan == null ? null : normalizeBlank(plan.getMeterNo())));
    return detail;
  }

  private String buildMealPlanSummary(StaffServicePlan plan) {
    if (plan == null) {
      return "未维护";
    }
    List<String> items = new ArrayList<>();
    if (normalizeFlag(plan.getBreakfastEnabled()) == 1) {
      items.add("早餐 " + normalizeDays(plan.getBreakfastDaysPerMonth()) + "天");
    }
    if (normalizeFlag(plan.getLunchEnabled()) == 1) {
      items.add("中餐 " + normalizeDays(plan.getLunchDaysPerMonth()) + "天");
    }
    if (normalizeFlag(plan.getDinnerEnabled()) == 1) {
      items.add("晚餐 " + normalizeDays(plan.getDinnerDaysPerMonth()) + "天");
    }
    return items.isEmpty() ? "未启用餐费方案" : String.join(" / ", items);
  }

  private String buildDormitoryLabel(StaffServicePlan plan) {
    if (plan == null || normalizeFlag(plan.getLiveInDormitory()) != 1) {
      return "未住宿";
    }
    List<String> parts = new ArrayList<>();
    if (normalizeBlank(plan.getDormitoryBuilding()) != null) {
      parts.add(plan.getDormitoryBuilding().trim());
    }
    if (normalizeBlank(plan.getDormitoryRoomNo()) != null) {
      parts.add(plan.getDormitoryRoomNo().trim());
    }
    if (normalizeBlank(plan.getDormitoryBedNo()) != null) {
      parts.add(plan.getDormitoryBedNo().trim());
    }
    return parts.isEmpty() ? "待分配宿舍" : String.join(" / ", parts);
  }

  private boolean hasCompleteDormitoryAssignment(HrDormitoryStaffResponse item) {
    return item != null
        && normalizeFlag(item.getLiveInDormitory()) == 1
        && normalizeBlank(item.getDormitoryBuilding()) != null
        && normalizeBlank(item.getDormitoryRoomNo()) != null
        && normalizeBlank(item.getDormitoryBedNo()) != null;
  }

  private String dormitoryRoomKey(HrDormitoryStaffResponse item) {
    if (item == null || normalizeFlag(item.getLiveInDormitory()) != 1) {
      return null;
    }
    String building = normalizeBlank(item.getDormitoryBuilding());
    String roomNo = normalizeBlank(item.getDormitoryRoomNo());
    if (building == null || roomNo == null) {
      return null;
    }
    return (building + "||" + roomNo).toLowerCase(Locale.ROOT);
  }

  private String dormitoryRoomConfigKey(HrDormitoryRoomConfigResponse item) {
    String building = normalizeBlank(item == null ? null : item.getBuilding());
    String roomNo = normalizeBlank(item == null ? null : item.getRoomNo());
    if (building == null || roomNo == null) {
      return null;
    }
    return (building + "||" + roomNo).toLowerCase(Locale.ROOT);
  }

  private String dormitoryOccupancyKey(StaffServicePlan plan) {
    if (plan == null || normalizeFlag(plan.getLiveInDormitory()) != 1) {
      return null;
    }
    String building = normalizeBlank(plan.getDormitoryBuilding());
    String roomNo = normalizeBlank(plan.getDormitoryRoomNo());
    String bedNo = normalizeBlank(plan.getDormitoryBedNo());
    if (building == null || roomNo == null || bedNo == null) {
      return null;
    }
    return (building + "||" + roomNo + "||" + bedNo).toLowerCase(Locale.ROOT);
  }

  private String resolveDormitoryFloorLabel(String roomNo) {
    String normalized = normalizeBlank(roomNo);
    if (normalized == null) {
      return null;
    }
    java.util.regex.Matcher direct = java.util.regex.Pattern.compile("^([A-Za-z]?\\d+)[-层Ff]").matcher(normalized);
    if (direct.find()) {
      return direct.group(1) + "层";
    }
    java.util.regex.Matcher simple = java.util.regex.Pattern.compile("^(\\d{1,2})").matcher(normalized);
    if (simple.find()) {
      return simple.group(1) + "层";
    }
    return null;
  }

  private boolean matchesDormitoryRow(
      HrDormitoryStaffResponse item,
      String keyword,
      Long departmentId,
      String dormitoryState,
      String building,
      String roomNo) {
    if (item == null) {
      return false;
    }
    if (departmentId != null && !Objects.equals(departmentId, item.getDepartmentId())) {
      return false;
    }
    String normalizedKeyword = normalizeBlank(keyword);
    if (normalizedKeyword != null) {
      String merged = String.join(" ",
          defaultText(item.getStaffName(), ""),
          defaultText(item.getStaffNo(), ""),
          defaultText(item.getDepartmentName(), ""),
          defaultText(item.getDormitoryLabel(), ""),
          defaultText(item.getMeterNo(), ""));
      if (!merged.toLowerCase(Locale.ROOT).contains(normalizedKeyword.toLowerCase(Locale.ROOT))) {
        return false;
      }
    }
    String normalizedBuilding = normalizeBlank(building);
    if (normalizedBuilding != null && !defaultText(item.getDormitoryBuilding(), "").toLowerCase(Locale.ROOT)
        .contains(normalizedBuilding.toLowerCase(Locale.ROOT))) {
      return false;
    }
    String normalizedRoomNo = normalizeBlank(roomNo);
    if (normalizedRoomNo != null && !defaultText(item.getDormitoryRoomNo(), "").toLowerCase(Locale.ROOT)
        .contains(normalizedRoomNo.toLowerCase(Locale.ROOT))) {
      return false;
    }
    String state = normalizeBlank(dormitoryState);
    if (state == null) {
      return true;
    }
    return switch (state.toUpperCase(Locale.ROOT)) {
      case "LIVE_IN" -> normalizeFlag(item.getLiveInDormitory()) == 1;
      case "ASSIGNED" -> hasCompleteDormitoryAssignment(item);
      case "PENDING_ASSIGN" -> normalizeFlag(item.getLiveInDormitory()) == 1 && !hasCompleteDormitoryAssignment(item);
      case "CONFLICT" -> Boolean.TRUE.equals(item.getOccupancyConflict());
      case "NO_DORM" -> normalizeFlag(item.getLiveInDormitory()) != 1;
      default -> true;
    };
  }

  private void markFinancePending(StaffMonthlyFeeBill bill) {
    bill.setFinanceSyncStatus("PENDING");
    bill.setFinanceSyncId(null);
    bill.setFinanceSyncAt(null);
    bill.setFinanceSyncBy(null);
  }

  private StaffAccount resolveImportStaff(
      HrStaffElectricityImportRowRequest row,
      Map<Long, StaffAccount> staffById,
      Map<String, StaffAccount> staffByNo,
      Map<String, StaffAccount> staffByName) {
    if (row == null) {
      return null;
    }
    if (row.getStaffId() != null && staffById.containsKey(row.getStaffId())) {
      return staffById.get(row.getStaffId());
    }
    String staffNo = normalizeBlank(row.getStaffNo());
    if (staffNo != null && staffByNo.containsKey(staffNo.toLowerCase(Locale.ROOT))) {
      return staffByNo.get(staffNo.toLowerCase(Locale.ROOT));
    }
    String staffName = normalizeBlank(row.getStaffName());
    if (staffName != null && staffByName.containsKey(staffName.toLowerCase(Locale.ROOT))) {
      return staffByName.get(staffName.toLowerCase(Locale.ROOT));
    }
    return null;
  }

  private Map<String, Object> buildFinanceSyncFormData(StaffMonthlyFeeBill bill) {
    Map<String, Object> payload = new HashMap<>();
    String feeType = defaultText(bill.getFeeType(), "MEAL").toUpperCase(Locale.ROOT);
    payload.put("scene", switch (feeType) {
      case "ELECTRICITY" -> "staff-electricity-monthly";
      case "SOCIAL_SECURITY" -> "staff-social-security-monthly";
      default -> "staff-meal-monthly";
    });
    payload.put("expenseType", switch (feeType) {
      case "ELECTRICITY" -> "员工电费";
      case "SOCIAL_SECURITY" -> "员工社保";
      default -> "员工餐费";
    });
    payload.put("feeBillId", bill.getId());
    payload.put("staffId", bill.getStaffId());
    payload.put("staffNo", bill.getStaffNo());
    payload.put("staffName", bill.getStaffName());
    payload.put("departmentName", bill.getDepartmentName());
    payload.put("feeMonth", bill.getFeeMonth());
    payload.put("dormitoryRoomNo", bill.getDormitoryRoomNo());
    payload.put("meterNo", bill.getMeterNo());
    payload.put("detailJson", bill.getDetailJson());
    return payload;
  }

  private String buildFinanceSyncRemark(StaffMonthlyFeeBill bill) {
    List<String> lines = new ArrayList<>();
    String feeLabel = switch (defaultText(bill.getFeeType(), "MEAL").toUpperCase(Locale.ROOT)) {
      case "ELECTRICITY" -> "电费";
      case "SOCIAL_SECURITY" -> "社保费";
      default -> "餐费";
    };
    lines.add("月度" + feeLabel + "同步");
    lines.add("员工：" + defaultText(bill.getStaffName(), defaultText(bill.getStaffNo(), "未知")));
    lines.add("月份：" + defaultText(bill.getFeeMonth(), "-"));
    if (normalizeBlank(bill.getDormitoryRoomNo()) != null) {
      lines.add("宿舍：" + bill.getDormitoryRoomNo());
    }
    if (normalizeBlank(bill.getMeterNo()) != null) {
      lines.add("电表：" + bill.getMeterNo());
    }
    if (normalizeBlank(bill.getRemark()) != null) {
      lines.add("备注：" + bill.getRemark().trim());
    }
    return String.join("；", lines);
  }

  private Map<Long, String> loadStaffNameMap(List<Long> staffIds) {
    List<Long> sanitized = sanitizeIds(staffIds);
    if (sanitized.isEmpty()) {
      return Map.of();
    }
    return staffMapper.selectBatchIdsSafe(sanitized).stream()
        .filter(item -> item.getId() != null)
        .collect(Collectors.toMap(StaffAccount::getId, item -> defaultText(item.getRealName(), item.getUsername()), (a, b) -> a));
  }

  private YearMonth resolveFeeMonth(String month) {
    String normalized = normalizeBlank(month);
    return normalized == null ? YearMonth.now() : YearMonth.parse(normalized);
  }

  private Integer normalizeFlag(Integer value) {
    return value != null && value == 1 ? 1 : 0;
  }

  private Integer normalizeDays(Integer value) {
    if (value == null || value < 0) {
      return 0;
    }
    return value;
  }

  private java.math.BigDecimal normalizeMoney(java.math.BigDecimal value) {
    return zeroIfNull(value).setScale(2, java.math.RoundingMode.HALF_UP);
  }

  private java.math.BigDecimal zeroIfNull(java.math.BigDecimal value) {
    return value == null ? java.math.BigDecimal.ZERO : value;
  }

  private String defaultText(String value, String fallback) {
    return normalizeBlank(value) == null ? fallback : value.trim();
  }

  private String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String value : values) {
      String normalized = normalizeBlank(value);
      if (normalized != null) {
        return normalized;
      }
    }
    return null;
  }

  private Map<String, Object> buildRecruitmentExt(HrRecruitmentNeedRequest request) {
    Map<String, Object> ext = new HashMap<>();
    if (request == null) {
      return ext;
    }
    putIfPresent(ext, "positionName", request.getPositionName());
    putIfPresent(ext, "departmentName", request.getDepartmentName());
    putIfPresent(ext, "requiredDate", request.getRequiredDate() == null ? null : request.getRequiredDate().toString());
    putIfPresent(ext, "headcount", request.getHeadcount());
    putIfPresent(ext, "scene", request.getScene());
    putIfPresent(ext, "candidateName", request.getCandidateName());
    putIfPresent(ext, "contactPhone", request.getContactPhone());
    putIfPresent(ext, "resumeUrl", request.getResumeUrl());
    putIfPresent(ext, "intentionStatus", request.getIntentionStatus());
    putIfPresent(ext, "followUpDate", request.getFollowUpDate() == null ? null : request.getFollowUpDate().toString());
    putIfPresent(ext, "offerStatus", request.getOfferStatus());
    putIfPresent(ext, "onboardDate", request.getOnboardDate() == null ? null : request.getOnboardDate().toString());
    putIfPresent(ext, "salary", request.getSalary());
    putIfPresent(ext, "probationPeriod", request.getProbationPeriod());
    putIfPresent(ext, "workLocation", request.getWorkLocation());
    putIfPresent(ext, "shiftType", request.getShiftType());
    putIfPresent(ext, "checklistJson", request.getChecklistJson());
    putIfPresent(ext, "signedFilesJson", request.getSignedFilesJson());
    putIfPresent(ext, "accountPermissionJson", request.getAccountPermissionJson());
    putIfPresent(ext, "issuedItemsJson", request.getIssuedItemsJson());
    putIfPresent(ext, "mentorName", request.getMentorName());
    putIfPresent(ext, "probationGoal", request.getProbationGoal());
    putIfPresent(ext, "regularizationStatus", request.getRegularizationStatus());
    putIfPresent(ext, "recommendationNote", request.getRecommendationNote());
    putIfPresent(ext, "offboardingType", request.getOffboardingType());
    putIfPresent(ext, "lastWorkDate", request.getLastWorkDate() == null ? null : request.getLastWorkDate().toString());
    putIfPresent(ext, "handoverDeadline", request.getHandoverDeadline() == null ? null : request.getHandoverDeadline().toString());
    putIfPresent(ext, "resignationReason", request.getResignationReason());
    putIfPresent(ext, "resignationReportUrl", request.getResignationReportUrl());
    putIfPresent(ext, "handoverItemsJson", request.getHandoverItemsJson());
    putIfPresent(ext, "assetRecoveryJson", request.getAssetRecoveryJson());
    putIfPresent(ext, "permissionRecycleJson", request.getPermissionRecycleJson());
    putIfPresent(ext, "financeSettlementNote", request.getFinanceSettlementNote());
    putIfPresent(ext, "exitArchiveJson", request.getExitArchiveJson());
    return ext;
  }

  private Map<String, Object> buildRecruitmentExtWithDefaults(HrRecruitmentNeedRequest request) {
    Map<String, Object> ext = buildRecruitmentExt(request);
    String scene = request == null ? null : request.getScene();
    if ("onboarding".equalsIgnoreCase(scene) || "materials".equalsIgnoreCase(scene)) {
      if (!ext.containsKey("checklistJson")) {
        List<String> defaults = List.of(
            "身份证", "居住证明", "学历证明", "资格证", "体检/健康证", "无犯罪记录证明",
            "银行卡信息", "紧急联系人", "背调核验", "劳动合同", "保密协议", "员工手册承诺");
        ext.put("checklistJson", writeJsonArray(defaults));
      }
      if (!ext.containsKey("offerStatus")) {
        ext.put("offerStatus", "PENDING");
      }
      if (!ext.containsKey("regularizationStatus")) {
        ext.put("regularizationStatus", "PENDING");
      }
    }
    if ("offboarding".equalsIgnoreCase(scene)) {
      if (!ext.containsKey("offboardingType")) {
        ext.put("offboardingType", "RESIGN");
      }
      if (!ext.containsKey("handoverItemsJson")) {
        List<String> defaults = List.of("在办事项", "重点住养老人情况", "风险事项", "文件资料", "资产与库存");
        ext.put("handoverItemsJson", writeJsonArray(defaults));
      }
      if (!ext.containsKey("assetRecoveryJson")) {
        List<String> defaults = List.of("工牌门禁", "钥匙", "制服", "电脑/手机/对讲机");
        ext.put("assetRecoveryJson", writeJsonArray(defaults));
      }
      if (!ext.containsKey("permissionRecycleJson")) {
        ext.put("permissionRecycleJson", "最后工作日自动禁用账号；重要系统提前下线权限；保留审计日志");
      }
    }
    if ("candidates".equalsIgnoreCase(scene) && !ext.containsKey("intentionStatus")) {
      ext.put("intentionStatus", "CONSIDERING");
    }
    return ext;
  }

  private void putIfPresent(Map<String, Object> target, String key, Object value) {
    if (value == null) {
      return;
    }
    if (value instanceof String text && text.isBlank()) {
      return;
    }
    target.put(key, value);
  }

  private Long ensureFolder(Long orgId, Long parentId, String name, String remark) {
    OaDocumentFolder existed = oaDocumentFolderMapper.selectOne(Wrappers.lambdaQuery(OaDocumentFolder.class)
        .eq(OaDocumentFolder::getIsDeleted, 0)
        .eq(orgId != null, OaDocumentFolder::getOrgId, orgId)
        .eq(OaDocumentFolder::getParentId, parentId == null ? 0L : parentId)
        .eq(OaDocumentFolder::getName, name)
        .last("LIMIT 1"));
    if (existed != null) {
      return existed.getId();
    }
    OaDocumentFolder folder = new OaDocumentFolder();
    folder.setTenantId(orgId);
    folder.setOrgId(orgId);
    folder.setParentId(parentId == null ? 0L : parentId);
    folder.setName(name);
    folder.setSortNo(0);
    folder.setStatus("ENABLED");
    folder.setRemark(remark);
    folder.setCreatedBy(AuthContext.getStaffId());
    oaDocumentFolderMapper.insert(folder);
    return folder.getId();
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

  private String writeJsonArray(List<String> payload) {
    try {
      return objectMapper.writeValueAsString(payload == null ? new ArrayList<>() : payload);
    } catch (Exception ex) {
      return "[]";
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

  private LocalDateTime parseDateTime(String value, LocalDateTime fallback) {
    String normalized = normalizeBlank(value);
    if (normalized == null) {
      return fallback;
    }
    List<DateTimeFormatter> formatters = List.of(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    for (DateTimeFormatter formatter : formatters) {
      try {
        return LocalDateTime.parse(normalized, formatter);
      } catch (Exception ex) {
        // try next formatter
      }
    }
    return fallback;
  }

  private String empty(String value) {
    return value == null ? "" : value;
  }

  private List<Long> sanitizeIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return ids.stream().filter(x -> x != null && x > 0).distinct().toList();
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
      response.setContractNo(profile.getContractNo());
      response.setContractType(profile.getContractType());
      response.setContractStatus(profile.getContractStatus());
      response.setContractStartDate(profile.getContractStartDate());
      response.setContractEndDate(profile.getContractEndDate());
      response.setHireDate(profile.getHireDate());
      response.setQualificationLevel(profile.getQualificationLevel());
      response.setCertificateNo(profile.getCertificateNo());
      response.setEmergencyContactName(profile.getEmergencyContactName());
      response.setEmergencyContactPhone(profile.getEmergencyContactPhone());
      response.setBirthday(profile.getBirthday());
      response.setStatus(profile.getStatus());
      response.setLeaveDate(profile.getLeaveDate());
      response.setLeaveReason(profile.getLeaveReason());
      response.setSocialSecurityStatus(profile.getSocialSecurityStatus());
      response.setSocialSecurityStartDate(profile.getSocialSecurityStartDate());
      response.setSocialSecurityReminderDays(profile.getSocialSecurityReminderDays());
      response.setSocialSecurityCompanyApply(profile.getSocialSecurityCompanyApply());
      response.setSocialSecurityNeedDirectorApproval(profile.getSocialSecurityNeedDirectorApproval());
      response.setSocialSecurityWorkflowStatus(profile.getSocialSecurityWorkflowStatus());
      response.setSocialSecurityMonthlyAmount(profile.getSocialSecurityMonthlyAmount());
      response.setSocialSecurityApplySubmittedAt(profile.getSocialSecurityApplySubmittedAt());
      response.setSocialSecurityDirectorDecisionAt(profile.getSocialSecurityDirectorDecisionAt());
      response.setSocialSecurityCompletedAt(profile.getSocialSecurityCompletedAt());
      response.setSocialSecurityLastBilledMonth(profile.getSocialSecurityLastBilledMonth());
      response.setSocialSecurityRemark(profile.getSocialSecurityRemark());
      response.setRemark(profile.getRemark());
      response.setUpdateTime(profile.getUpdateTime());
    }
    return response;
  }

  private boolean shouldTrackSocialSecurity(StaffProfile profile, StaffAccount staff) {
    if (profile == null || profile.getHireDate() == null) {
      return false;
    }
    if (staff != null && !Integer.valueOf(1).equals(staff.getStatus())) {
      return false;
    }
    String status = normalizeBlank(profile.getSocialSecurityStatus());
    if (status == null) {
      return true;
    }
    return !List.of("COMPLETED", "PAID", "STOPPED").contains(status.toUpperCase(Locale.ROOT));
  }

  private LocalDate resolveSocialSecurityReminderDate(StaffProfile profile) {
    if (profile == null || profile.getHireDate() == null) {
      return null;
    }
    Integer reminderDays = profile.getSocialSecurityReminderDays();
    int days = reminderDays == null || reminderDays < 0 ? 30 : reminderDays;
    return profile.getHireDate().plusDays(days);
  }

  private HrSocialSecurityReminderResponse toSocialSecurityReminderResponse(
      StaffProfile profile, StaffAccount staff, LocalDate today) {
    HrSocialSecurityReminderResponse response = new HrSocialSecurityReminderResponse();
    if (staff != null) {
      response.setStaffId(staff.getId());
      response.setStaffNo(staff.getStaffNo());
      response.setStaffName(staff.getRealName());
      response.setPhone(staff.getPhone());
      response.setDepartmentId(staff.getDepartmentId());
    }
    if (profile != null) {
      response.setStaffId(response.getStaffId() == null ? profile.getStaffId() : response.getStaffId());
      response.setJobTitle(profile.getJobTitle());
      response.setHireDate(profile.getHireDate());
      response.setSocialSecurityStatus(profile.getSocialSecurityStatus());
      response.setSocialSecurityStartDate(profile.getSocialSecurityStartDate());
      response.setSocialSecurityReminderDays(profile.getSocialSecurityReminderDays());
      response.setSocialSecurityCompanyApply(profile.getSocialSecurityCompanyApply());
      response.setSocialSecurityNeedDirectorApproval(profile.getSocialSecurityNeedDirectorApproval());
      response.setSocialSecurityWorkflowStatus(profile.getSocialSecurityWorkflowStatus());
      response.setSocialSecurityMonthlyAmount(profile.getSocialSecurityMonthlyAmount());
      response.setSocialSecurityApplySubmittedAt(profile.getSocialSecurityApplySubmittedAt());
      response.setSocialSecurityDirectorDecisionAt(profile.getSocialSecurityDirectorDecisionAt());
      response.setSocialSecurityCompletedAt(profile.getSocialSecurityCompletedAt());
      response.setSocialSecurityLastBilledMonth(profile.getSocialSecurityLastBilledMonth());
      response.setSocialSecurityRemark(profile.getSocialSecurityRemark());
      LocalDate reminderDate = resolveSocialSecurityReminderDate(profile);
      response.setReminderDate(reminderDate);
      if (today != null && reminderDate != null) {
        response.setRemainingDays(ChronoUnit.DAYS.between(today, reminderDate));
      }
      response.setReminderScope(resolveSocialSecurityScope(response, today, today == null ? null : today.plusDays(7)));
    }
    return response;
  }

  private boolean matchSocialSecurityScope(
      HrSocialSecurityReminderResponse item,
      String scopeCode,
      LocalDate today,
      LocalDate upcomingDeadline) {
    if (item == null) {
      return false;
    }
    String normalized = scopeCode == null ? "ALL" : scopeCode;
    if ("ALL".equals(normalized)) {
      return true;
    }
    String resolvedScope = resolveSocialSecurityScope(item, today, upcomingDeadline);
    return normalized.equals(resolvedScope);
  }

  private String resolveSocialSecurityScope(
      HrSocialSecurityReminderResponse item,
      LocalDate today,
      LocalDate upcomingDeadline) {
    if (item == null) {
      return "ALL";
    }
    String status = normalizeBlank(item.getSocialSecurityStatus());
    if (status != null && List.of("COMPLETED", "PAID").contains(status.toUpperCase(Locale.ROOT))) {
      return "COMPLETED";
    }
    LocalDate reminderDate = item.getReminderDate();
    if (reminderDate == null) {
      return "PENDING";
    }
    if (today != null && !reminderDate.isAfter(today)) {
      return "DUE";
    }
    if (today != null && upcomingDeadline != null && reminderDate.isAfter(today) && !reminderDate.isAfter(upcomingDeadline)) {
      return "UPCOMING";
    }
    return "PENDING";
  }

  private StaffProfile ensureSocialSecurityProfile(Long orgId, StaffAccount staff) {
    StaffProfile profile = staffProfileMapper.selectOne(Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getOrgId, orgId)
        .eq(StaffProfile::getStaffId, staff.getId())
        .eq(StaffProfile::getIsDeleted, 0)
        .last("LIMIT 1"));
    if (profile != null) {
      if (normalizeBlank(profile.getSocialSecurityWorkflowStatus()) == null) {
        profile.setSocialSecurityWorkflowStatus(resolveInitialSocialSecurityWorkflowStatus(profile.getSocialSecurityStatus()));
      }
      if (profile.getSocialSecurityMonthlyAmount() == null) {
        profile.setSocialSecurityMonthlyAmount(java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP));
      }
      return profile;
    }
    profile = new StaffProfile();
    profile.setOrgId(orgId);
    profile.setStaffId(staff.getId());
    profile.setStatus(staff.getStatus());
    profile.setSocialSecurityCompanyApply(0);
    profile.setSocialSecurityNeedDirectorApproval(0);
    profile.setSocialSecurityWorkflowStatus("DRAFT");
    profile.setSocialSecurityMonthlyAmount(java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP));
    staffProfileMapper.insert(profile);
    return profile;
  }

  private String resolveInitialSocialSecurityWorkflowStatus(String socialSecurityStatus) {
    String normalized = normalizeBlank(socialSecurityStatus);
    if (normalized == null) {
      return "DRAFT";
    }
    normalized = normalized.toUpperCase(Locale.ROOT);
    if (List.of("COMPLETED", "PAID").contains(normalized)) {
      return "ACTIVE";
    }
    if ("PROCESSING".equals(normalized)) {
      return "PENDING_FINANCE";
    }
    if ("STOPPED".equals(normalized)) {
      return "STOPPED";
    }
    return "DRAFT";
  }

  private String normalizeSocialSecurityWorkflowStatus(String value) {
    String normalized = normalizeBlank(value);
    if (normalized == null) {
      return null;
    }
    normalized = normalized.toUpperCase(Locale.ROOT);
    if (!Set.of("DRAFT", "PENDING_DIRECTOR", "PENDING_FINANCE", "ACTIVE", "REJECTED", "STOPPED").contains(normalized)) {
      throw new IllegalArgumentException("socialSecurityWorkflowStatus unsupported");
    }
    return normalized;
  }

  private boolean canGenerateSocialSecurityBill(StaffProfile profile, StaffAccount staff, YearMonth feeMonth) {
    if (profile == null || staff == null || staff.getId() == null) {
      return false;
    }
    if (!Integer.valueOf(1).equals(staff.getStatus())) {
      return false;
    }
    if (normalizeFlag(profile.getSocialSecurityCompanyApply()) != 1) {
      return false;
    }
    if (!List.of("ACTIVE").contains(defaultText(profile.getSocialSecurityWorkflowStatus(), "DRAFT").toUpperCase(Locale.ROOT))
        && !List.of("COMPLETED", "PAID").contains(defaultText(profile.getSocialSecurityStatus(), "PENDING").toUpperCase(Locale.ROOT))) {
      return false;
    }
    if (zeroIfNull(profile.getSocialSecurityMonthlyAmount()).signum() <= 0) {
      return false;
    }
    return !feeMonth.toString().equals(normalizeBlank(profile.getSocialSecurityLastBilledMonth()));
  }

  private StaffMonthlyFeeBill upsertSocialSecurityMonthlyBill(
      Long orgId, StaffAccount staff, StaffProfile profile, YearMonth feeMonth, Long operatorId) {
    if (orgId == null || staff == null || profile == null || feeMonth == null) {
      return null;
    }
    Department department = staff.getDepartmentId() == null ? null : departmentMapper.selectById(staff.getDepartmentId());
    StaffMonthlyFeeBill bill = findMonthlyFeeBill(orgId, staff.getId(), feeMonth.toString(), "SOCIAL_SECURITY");
    if (bill == null) {
      bill = new StaffMonthlyFeeBill();
      bill.setOrgId(orgId);
      bill.setStaffId(staff.getId());
      bill.setFeeMonth(feeMonth.toString());
      bill.setFeeType("SOCIAL_SECURITY");
      bill.setCreatedBy(operatorId);
    }
    java.math.BigDecimal amount = normalizeMoney(profile.getSocialSecurityMonthlyAmount());
    bill.setStaffNo(normalizeBlank(staff.getStaffNo()));
    bill.setStaffName(normalizeBlank(staff.getRealName()));
    bill.setDepartmentId(staff.getDepartmentId());
    bill.setDepartmentName(department == null ? null : normalizeBlank(department.getDeptName()));
    bill.setTitle("员工社保 " + feeMonth + " " + defaultText(staff.getRealName(), defaultText(staff.getUsername(), "员工")));
    bill.setQuantity(java.math.BigDecimal.ONE);
    bill.setUnitPrice(amount);
    bill.setAmount(amount);
    bill.setStatus("GENERATED");
    markFinancePending(bill);
    Map<String, Object> detail = new HashMap<>();
    detail.put("detailSummary", "社保月度费用 " + amount.setScale(2, java.math.RoundingMode.HALF_UP) + "元/人");
    detail.put("workflowStatus", profile.getSocialSecurityWorkflowStatus());
    detail.put("socialSecurityStartDate", profile.getSocialSecurityStartDate() == null ? null : profile.getSocialSecurityStartDate().toString());
    detail.put("completedAt", profile.getSocialSecurityCompletedAt() == null ? null : profile.getSocialSecurityCompletedAt().toString());
    bill.setDetailJson(writeJson(detail));
    bill.setRemark(firstNonBlank(normalizeBlank(profile.getSocialSecurityRemark()), "社保自动记账"));
    saveMonthlyFeeBill(bill);
    profile.setSocialSecurityLastBilledMonth(feeMonth.toString());
    staffProfileMapper.updateById(profile);
    return bill;
  }

  private void syncSocialSecurityDirectorTodo(Long orgId, StaffAccount staff, StaffProfile profile) {
    if (staff == null || profile == null) {
      return;
    }
    clearSocialSecurityDirectorTodos(orgId, staff.getId());
    StaffAccount assignee = findFirstActiveStaffByRoleCodes(orgId, List.of("DIRECTOR", "DEAN", "ADMIN", "SYS_ADMIN"));
    OaTodo todo = new OaTodo();
    todo.setTenantId(orgId);
    todo.setOrgId(orgId);
    todo.setTitle("【社保审核】" + defaultText(staff.getRealName(), defaultText(staff.getStaffNo(), "员工")) + " 企业社保申请");
    todo.setContent(socialSecurityDirectorTodoMarker(staff.getId())
        + " 员工工号：" + defaultText(staff.getStaffNo(), "-")
        + "；月费用：" + normalizeMoney(profile.getSocialSecurityMonthlyAmount()).setScale(2, java.math.RoundingMode.HALF_UP) + "元"
        + "；备注：" + defaultText(profile.getSocialSecurityRemark(), "无"));
    todo.setDueTime(LocalDateTime.now().plusHours(4));
    todo.setStatus("OPEN");
    todo.setAssigneeId(assignee == null ? null : assignee.getId());
    todo.setAssigneeName(assignee == null ? "院长" : defaultText(assignee.getRealName(), assignee.getUsername()));
    todo.setCreatedBy(AuthContext.getStaffId());
    oaTodoMapper.insert(todo);
  }

  private void syncSocialSecurityFinanceTodo(Long orgId, StaffAccount staff, StaffProfile profile) {
    if (staff == null || profile == null) {
      return;
    }
    clearSocialSecurityFinanceTodo(orgId, profile);
    StaffAccount assignee = findFirstActiveStaffByRoleCodes(orgId,
        List.of("FINANCE_MINISTER", "FINANCE_EMPLOYEE", "ACCOUNTANT", "CASHIER", "FINANCE"));
    OaTodo todo = new OaTodo();
    todo.setTenantId(orgId);
    todo.setOrgId(orgId);
    todo.setTitle("【社保办理】" + defaultText(staff.getRealName(), defaultText(staff.getStaffNo(), "员工")) + " 新增社保购买任务");
    todo.setContent(socialSecurityFinanceTodoMarker(staff.getId())
        + " 员工工号：" + defaultText(staff.getStaffNo(), "-")
        + "；月费用：" + normalizeMoney(profile.getSocialSecurityMonthlyAmount()).setScale(2, java.math.RoundingMode.HALF_UP) + "元"
        + "；是否院长审批：" + (normalizeFlag(profile.getSocialSecurityNeedDirectorApproval()) == 1 ? "是" : "否")
        + "；备注：" + defaultText(profile.getSocialSecurityRemark(), "无"));
    todo.setDueTime(LocalDateTime.now().plusHours(8));
    todo.setStatus("OPEN");
    todo.setAssigneeId(assignee == null ? null : assignee.getId());
    todo.setAssigneeName(assignee == null ? "财务部" : defaultText(assignee.getRealName(), assignee.getUsername()));
    todo.setCreatedBy(AuthContext.getStaffId());
    oaTodoMapper.insert(todo);
    profile.setSocialSecurityFinanceTodoId(todo.getId());
  }

  private StaffAccount findFirstActiveStaffByRoleCodes(Long orgId, List<String> roleCodes) {
    if (roleCodes == null || roleCodes.isEmpty()) {
      return null;
    }
    for (String roleCode : roleCodes) {
      List<StaffAccount> candidates = staffMapper.selectByRoleCode(orgId, roleCode);
      for (StaffAccount candidate : candidates) {
        if (candidate != null && candidate.getId() != null
            && Integer.valueOf(1).equals(candidate.getStatus())
            && Objects.equals(candidate.getOrgId(), orgId)
            && !Objects.equals(candidate.getIsDeleted(), 1)) {
          return candidate;
        }
      }
    }
    return null;
  }

  private void clearSocialSecurityDirectorTodos(Long orgId, Long staffId) {
    closeSocialSecurityTodos(orgId, socialSecurityDirectorTodoMarker(staffId), "院长审核已处理");
  }

  private void clearSocialSecurityFinanceTodo(Long orgId, StaffProfile profile) {
    if (profile == null || profile.getStaffId() == null) {
      return;
    }
    closeSocialSecurityTodos(orgId, socialSecurityFinanceTodoMarker(profile.getStaffId()), "财务办理已完成");
    profile.setSocialSecurityFinanceTodoId(null);
  }

  private void closeSocialSecurityTodos(Long orgId, String markerPrefix, String message) {
    if (normalizeBlank(markerPrefix) == null) {
      return;
    }
    List<OaTodo> todos = oaTodoMapper.selectList(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(OaTodo::getStatus, "OPEN")
        .like(OaTodo::getContent, markerPrefix));
    for (OaTodo todo : todos) {
      todo.setStatus("DONE");
      todo.setContent(defaultText(todo.getContent(), "") + "；" + defaultText(message, "已处理"));
      oaTodoMapper.updateById(todo);
    }
  }

  private String socialSecurityDirectorTodoMarker(Long staffId) {
    return "[SOCIAL_SECURITY_DIRECTOR:" + staffId + "]";
  }

  private String socialSecurityFinanceTodoMarker(Long staffId) {
    return "[SOCIAL_SECURITY_FINANCE:" + staffId + "]";
  }

  private HrContractReminderResponse toContractReminderResponse(StaffProfile profile, StaffAccount staff, LocalDate today) {
    HrContractReminderResponse item = new HrContractReminderResponse();
    item.setContractId(profile == null ? null : profile.getId());
    item.setStaffId(staff == null ? null : staff.getId());
    item.setStaffNo(staff == null ? null : staff.getStaffNo());
    item.setStaffName(staff == null ? null : staff.getRealName());
    item.setPhone(staff == null ? null : staff.getPhone());
    item.setJobTitle(profile == null ? null : profile.getJobTitle());
    item.setContractNo(profile == null ? null : profile.getContractNo());
    item.setContractStatus(profile == null ? null : profile.getContractStatus());
    item.setContractType(profile == null ? null : profile.getContractType());
    item.setContractStartDate(profile == null ? null : profile.getContractStartDate());
    item.setContractEndDate(profile == null ? null : profile.getContractEndDate());
    if (profile != null && profile.getContractEndDate() != null && today != null) {
      item.setRemainingDays(ChronoUnit.DAYS.between(today, profile.getContractEndDate()));
    }
    return item;
  }

  private Map<Long, StaffAccount> loadStaffMap(List<Long> staffIds) {
    List<Long> sanitizedStaffIds = sanitizeIds(staffIds);
    if (sanitizedStaffIds.isEmpty()) {
      return Map.of();
    }
    return staffMapper.selectBatchIdsSafe(sanitizedStaffIds).stream()
        .filter(staff -> !isHiddenPlatformStaff(staff))
        .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));
  }

  private List<Long> findMatchedStaffIds(Long orgId, String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return List.of();
    }
    return staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
            .eq(StaffAccount::getIsDeleted, 0)
            .eq(orgId != null, StaffAccount::getOrgId, orgId)
            .ne(StaffAccount::getUsername, HIDDEN_PLATFORM_USERNAME)
            .and(w -> w.like(StaffAccount::getRealName, keyword.trim())
                .or().like(StaffAccount::getStaffNo, keyword.trim())
                .or().like(StaffAccount::getPhone, keyword.trim())))
        .stream()
        .map(StaffAccount::getId)
        .filter(id -> id != null && id > 0)
        .distinct()
        .toList();
  }

  private List<Long> findMatchedContractStaffIds(Long orgId, String keyword, String contractStatus) {
    if ((keyword == null || keyword.isBlank()) && (contractStatus == null || contractStatus.isBlank())) {
      return List.of();
    }
    return staffProfileMapper.selectList(Wrappers.lambdaQuery(StaffProfile.class)
            .eq(StaffProfile::getIsDeleted, 0)
            .eq(orgId != null, StaffProfile::getOrgId, orgId)
            .eq(contractStatus != null && !contractStatus.isBlank(), StaffProfile::getContractStatus, contractStatus)
            .like(keyword != null && !keyword.isBlank(), StaffProfile::getContractNo, keyword == null ? null : keyword.trim()))
        .stream()
        .map(StaffProfile::getStaffId)
        .filter(id -> id != null && id > 0)
        .distinct()
        .toList();
  }

  private List<Long> findRoleStaffIds(Long orgId, Long roleId) {
    if (roleId == null) {
      return List.of();
    }
    return staffRoleMapper.selectList(Wrappers.lambdaQuery(StaffRole.class)
            .eq(StaffRole::getIsDeleted, 0)
            .eq(orgId != null, StaffRole::getOrgId, orgId)
            .eq(StaffRole::getRoleId, roleId))
        .stream()
        .map(StaffRole::getStaffId)
        .filter(id -> id != null && id > 0)
        .distinct()
        .toList();
  }

  private boolean isHiddenPlatformStaff(StaffAccount staff) {
    if (staff == null) {
      return true;
    }
    String username = staff.getUsername();
    return username != null && HIDDEN_PLATFORM_USERNAME.equalsIgnoreCase(username.trim());
  }

  private String normalizeBlank(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String generateStaffContractNo(Long orgId, Long staffId) {
    String orgSegment = String.format("%02d", Math.abs(orgId == null ? 0 : orgId.intValue()) % 100);
    String staffSegment = String.format("%06d", Math.abs(staffId == null ? 0 : staffId.intValue()) % 1_000_000);
    String timeSegment = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    return "HT" + orgSegment + timeSegment + staffSegment;
  }

  private HrStaffBirthdayResponse toBirthdayResponse(StaffProfile profile, StaffAccount staff, LocalDate today) {
    HrStaffBirthdayResponse item = new HrStaffBirthdayResponse();
    if (profile != null) {
      item.setStaffId(profile.getStaffId());
      item.setJobTitle(profile.getJobTitle());
      item.setBirthday(profile.getBirthday());
      item.setRemark(profile.getRemark());
      item.setDaysUntil(daysUntilBirthday(profile.getBirthday(), today));
      item.setNextBirthday(resolveNextBirthday(profile.getBirthday(), today));
    }
    if (staff != null) {
      item.setStaffId(staff.getId());
      item.setStaffNo(staff.getStaffNo());
      item.setRealName(staff.getRealName());
      item.setPhone(staff.getPhone());
      item.setDepartmentId(staff.getDepartmentId());
      item.setStatus(staff.getStatus());
    }
    return item;
  }

  private boolean matchBirthdayScope(HrStaffBirthdayResponse item, String scopeCode, int month) {
    if (item == null || item.getBirthday() == null) {
      return false;
    }
    int daysUntil = item.getDaysUntil() == null ? -1 : item.getDaysUntil();
    if ("TODAY".equals(scopeCode)) {
      return daysUntil == 0;
    }
    if ("NEXT7".equals(scopeCode)) {
      return daysUntil >= 0 && daysUntil <= 7;
    }
    if ("NEXT30".equals(scopeCode)) {
      return daysUntil >= 0 && daysUntil <= 30;
    }
    if ("THIS_MONTH".equals(scopeCode)) {
      int currentMonth = LocalDate.now().getMonthValue();
      return item.getBirthday().getMonthValue() == currentMonth;
    }
    if (month >= 1 && month <= 12) {
      return item.getBirthday().getMonthValue() == month;
    }
    return true;
  }

  private Integer daysUntilBirthday(LocalDate birthday, LocalDate today) {
    LocalDate next = resolveNextBirthday(birthday, today);
    if (next == null) {
      return null;
    }
    return (int) ChronoUnit.DAYS.between(today, next);
  }

  private LocalDate resolveNextBirthday(LocalDate birthday, LocalDate today) {
    if (birthday == null || today == null) {
      return null;
    }
    LocalDate next = birthdayForYear(birthday, today.getYear());
    if (next.isBefore(today)) {
      next = birthdayForYear(birthday, today.getYear() + 1);
    }
    return next;
  }

  private LocalDate birthdayForYear(LocalDate birthday, int year) {
    int month = birthday.getMonthValue();
    int maxDay = java.time.Month.of(month).length(java.time.Year.isLeap(year));
    int day = Math.min(birthday.getDayOfMonth(), maxDay);
    return LocalDate.of(year, month, day);
  }

  private void syncBirthdayCalendarTask(Long orgId, StaffAccount staff, StaffProfile profile) {
    if (staff == null || staff.getId() == null) {
      return;
    }
    OaTask task = oaTaskMapper.selectOne(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(OaTask::getPlanCategory, "STAFF_BIRTHDAY")
        .eq(OaTask::getSourceTodoId, staff.getId())
        .last("LIMIT 1"));

    LocalDate birthday = profile == null ? null : profile.getBirthday();
    if (birthday == null) {
      if (task != null) {
        task.setIsDeleted(1);
        oaTaskMapper.updateById(task);
      }
      clearBirthdayReminderTodos(orgId, staff.getId());
      return;
    }

    LocalDate today = LocalDate.now();
    LocalDate nextBirthday = birthdayForYear(birthday, today.getYear());
    if (nextBirthday.isBefore(today)) {
      nextBirthday = birthdayForYear(birthday, today.getYear() + 1);
    }

    if (task == null) {
      task = new OaTask();
      task.setTenantId(orgId);
      task.setOrgId(orgId);
      task.setSourceTodoId(staff.getId());
      task.setCreatedBy(AuthContext.getStaffId());
      task.setPlanCategory("STAFF_BIRTHDAY");
    }
    task.setTitle("员工生日：" + empty(staff.getRealName()));
    task.setDescription("员工生日提醒，员工工号：" + empty(staff.getStaffNo()) + "，联系电话：" + empty(staff.getPhone()));
    task.setStartTime(LocalDateTime.of(nextBirthday, LocalTime.of(9, 0)));
    task.setEndTime(LocalDateTime.of(nextBirthday, LocalTime.of(18, 0)));
    task.setPriority("NORMAL");
    task.setStatus("OPEN");
    task.setAssigneeId(staff.getId());
    task.setAssigneeName(staff.getRealName());
    task.setCalendarType("WORK");
    task.setUrgency("NORMAL");
    task.setEventColor("#eb2f96");
    task.setCollaboratorIds(null);
    task.setCollaboratorNames(null);
    task.setIsRecurring(0);
    task.setRecurrenceRule(null);
    task.setRecurrenceInterval(null);
    task.setRecurrenceCount(null);
    if (task.getId() == null) {
      oaTaskMapper.insert(task);
    } else {
      oaTaskMapper.updateById(task);
    }
    syncBirthdayReminderTodo(orgId, staff, birthday, nextBirthday);
  }

  private void syncBirthdayReminderTodo(Long orgId, StaffAccount staff, LocalDate birthday, LocalDate nextBirthday) {
    if (staff == null || staff.getId() == null) {
      return;
    }
    LocalDate today = LocalDate.now();
    if (!today.equals(nextBirthday)) {
      clearBirthdayReminderTodos(orgId, staff.getId());
      return;
    }
    String marker = birthdayReminderMarker(staff.getId(), today);
    OaTodo existed = oaTodoMapper.selectOne(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(OaTodo::getStatus, "OPEN")
        .like(OaTodo::getContent, marker)
        .last("LIMIT 1"));
    if (existed != null) {
      return;
    }
    OaTodo todo = new OaTodo();
    todo.setTenantId(orgId);
    todo.setOrgId(orgId);
    todo.setTitle("【生日提醒】" + empty(staff.getRealName()) + " 今日生日");
    todo.setContent(marker + " 员工工号：" + empty(staff.getStaffNo()) + "，联系电话：" + empty(staff.getPhone()) + "，生日：" + birthday);
    todo.setDueTime(today.atTime(9, 0));
    todo.setStatus("OPEN");
    todo.setAssigneeId(null);
    todo.setAssigneeName("人事行政");
    todo.setCreatedBy(AuthContext.getStaffId());
    oaTodoMapper.insert(todo);
  }

  private void clearBirthdayReminderTodos(Long orgId, Long staffId) {
    if (staffId == null) {
      return;
    }
    String markerPrefix = "[BIRTHDAY_REMINDER:" + staffId + ":";
    List<OaTodo> todos = oaTodoMapper.selectList(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(OaTodo::getStatus, "OPEN")
        .like(OaTodo::getContent, markerPrefix));
    for (OaTodo item : todos) {
      item.setStatus("DONE");
      oaTodoMapper.updateById(item);
    }
  }

  private String birthdayReminderMarker(Long staffId, LocalDate date) {
    return "[BIRTHDAY_REMINDER:" + staffId + ":" + date + "]";
  }
}
