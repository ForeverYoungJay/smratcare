package com.zhiyangyun.care.hr.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.hr.entity.StaffProfile;
import com.zhiyangyun.care.hr.entity.StaffTrainingRecord;
import com.zhiyangyun.care.hr.mapper.StaffProfileMapper;
import com.zhiyangyun.care.hr.mapper.StaffTrainingRecordMapper;
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
import java.time.LocalDate;
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

  public AdminHrController(StaffProfileMapper staffProfileMapper,
      StaffTrainingRecordMapper trainingRecordMapper,
      StaffMapper staffMapper,
      StaffPointsService staffPointsService,
      StaffPointsRuleService staffPointsRuleService,
      HrPerformanceService performanceService,
      AuditLogService auditLogService) {
    this.staffProfileMapper = staffProfileMapper;
    this.trainingRecordMapper = trainingRecordMapper;
    this.staffMapper = staffMapper;
    this.staffPointsService = staffPointsService;
    this.staffPointsRuleService = staffPointsRuleService;
    this.performanceService = performanceService;
    this.auditLogService = auditLogService;
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
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId);
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
      profile.setLeaveDate(LocalDate.parse(leaveDate));
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
      LocalDate start = LocalDate.parse(dateFrom);
      wrapper.ge(StaffTrainingRecord::getStartDate, start);
    }
    if (dateTo != null && !dateTo.isBlank()) {
      LocalDate end = LocalDate.parse(dateTo);
      wrapper.le(StaffTrainingRecord::getEndDate, end);
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
      @RequestParam(required = false) String dateTo) {
    return Result.ok(staffPointsService.pageLogs(AuthContext.getOrgId(), pageNo, pageSize, staffId, dateFrom, dateTo));
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
      @RequestParam(required = false) String sortBy) {
    return Result.ok(performanceService.ranking(AuthContext.getOrgId(), dateFrom, dateTo, sortBy));
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
