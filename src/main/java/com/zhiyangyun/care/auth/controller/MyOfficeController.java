package com.zhiyangyun.care.auth.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.entity.Department;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.DepartmentMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.compliance.entity.AuthLoginLog;
import com.zhiyangyun.care.compliance.mapper.AuthLoginLogMapper;
import com.zhiyangyun.care.hr.entity.HrLeaveQuota;
import com.zhiyangyun.care.hr.entity.StaffPointsAccount;
import com.zhiyangyun.care.hr.entity.StaffProfile;
import com.zhiyangyun.care.hr.mapper.HrLeaveQuotaMapper;
import com.zhiyangyun.care.hr.mapper.StaffPointsAccountMapper;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.hr.entity.StaffTrainingRecord;
import com.zhiyangyun.care.hr.mapper.StaffProfileMapper;
import com.zhiyangyun.care.hr.mapper.StaffTrainingRecordMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 我的办公 · 员工本人：档案查看、修改密码、登录记录。
 * 一律用登录身份确定员工，不接受前端传入的员工 ID；正式档案字段由人事维护，这里只读。
 */
@RestController
@RequestMapping("/api/me")
public class MyOfficeController {
  private static final int MIN_PASSWORD_LENGTH = 8;

  private final StaffMapper staffMapper;
  private final DepartmentMapper departmentMapper;
  private final StaffProfileMapper profileMapper;
  private final StaffTrainingRecordMapper trainingMapper;
  private final AuthLoginLogMapper loginLogMapper;
  private final PasswordEncoder passwordEncoder;
  private final AuditLogService auditLogService;
  private final HrLeaveQuotaMapper quotaMapper;
  private final OaApprovalMapper approvalMapper;
  private final StaffPointsAccountMapper pointsAccountMapper;
  private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

  public MyOfficeController(StaffMapper staffMapper, DepartmentMapper departmentMapper, StaffProfileMapper profileMapper,
      StaffTrainingRecordMapper trainingMapper, AuthLoginLogMapper loginLogMapper, PasswordEncoder passwordEncoder,
      AuditLogService auditLogService, HrLeaveQuotaMapper quotaMapper, OaApprovalMapper approvalMapper,
      StaffPointsAccountMapper pointsAccountMapper, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
    this.staffMapper = staffMapper;
    this.departmentMapper = departmentMapper;
    this.profileMapper = profileMapper;
    this.trainingMapper = trainingMapper;
    this.loginLogMapper = loginLogMapper;
    this.passwordEncoder = passwordEncoder;
    this.auditLogService = auditLogService;
    this.quotaMapper = quotaMapper;
    this.approvalMapper = approvalMapper;
    this.pointsAccountMapper = pointsAccountMapper;
    this.objectMapper = objectMapper;
  }

  /**
   * 本年假期余额：额度由人事维护；已用 = 本年已批准请假中同假别的 leaveDays 之和（只读，不另存）。
   * 没有维护额度的假别不显示余额。
   */
  @GetMapping("/leave-balance")
  public Result<List<Map<String, Object>>> leaveBalance() {
    StaffAccount staff = requireMe();
    int year = java.time.LocalDate.now().getYear();
    List<HrLeaveQuota> quotas = quotaMapper.selectList(Wrappers.lambdaQuery(HrLeaveQuota.class)
        .eq(HrLeaveQuota::getStaffId, staff.getId()).eq(HrLeaveQuota::getQuotaYear, year));
    if (quotas.isEmpty()) {
      return Result.ok(List.of());
    }
    Map<String, java.math.BigDecimal> used = new java.util.HashMap<>();
    for (OaApproval approval : approvalMapper.selectList(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0).eq(OaApproval::getApplicantId, staff.getId())
        .eq(OaApproval::getApprovalType, "LEAVE").eq(OaApproval::getStatus, "APPROVED")
        .ge(OaApproval::getStartTime, java.time.LocalDate.of(year, 1, 1).atStartOfDay())
        .lt(OaApproval::getStartTime, java.time.LocalDate.of(year + 1, 1, 1).atStartOfDay()))) {
      try {
        var form = objectMapper.readTree(approval.getFormData() == null ? "{}" : approval.getFormData());
        String type = form.path("leaveType").asText("");
        java.math.BigDecimal days = form.hasNonNull("leaveDays") ? new java.math.BigDecimal(form.path("leaveDays").asText("0")) : java.math.BigDecimal.ZERO;
        used.merge(type, days, java.math.BigDecimal::add);
      } catch (Exception ignored) {
        // 表单格式异常的旧单据不计入，避免一条坏数据让整页报错
      }
    }
    return Result.ok(quotas.stream().map(q -> {
      java.math.BigDecimal u = used.getOrDefault(q.getLeaveType(), java.math.BigDecimal.ZERO);
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("leaveType", q.getLeaveType());
      row.put("quotaDays", q.getQuotaDays());
      row.put("usedDays", u);
      row.put("remainingDays", q.getQuotaDays().subtract(u).max(java.math.BigDecimal.ZERO));
      return row;
    }).toList());
  }

  /** 我的积分：余额、全院排名、积分兑现申请记录（来自审批 POINTS_CASH） */
  @GetMapping("/points-summary")
  public Result<Map<String, Object>> pointsSummary() {
    StaffAccount staff = requireMe();
    StaffPointsAccount account = pointsAccountMapper.selectOne(Wrappers.lambdaQuery(StaffPointsAccount.class)
        .eq(StaffPointsAccount::getStaffId, staff.getId()).last("LIMIT 1"));
    int balance = account == null || account.getPointsBalance() == null ? 0 : account.getPointsBalance();
    Long ahead = pointsAccountMapper.selectCount(Wrappers.lambdaQuery(StaffPointsAccount.class)
        .eq(StaffPointsAccount::getOrgId, staff.getOrgId()).gt(StaffPointsAccount::getPointsBalance, balance));
    Long total = pointsAccountMapper.selectCount(Wrappers.lambdaQuery(StaffPointsAccount.class)
        .eq(StaffPointsAccount::getOrgId, staff.getOrgId()));
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("balance", balance);
    result.put("rank", account == null ? null : ahead + 1);
    result.put("rankTotal", total);
    result.put("exchanges", approvalMapper.selectList(Wrappers.lambdaQuery(OaApproval.class)
            .eq(OaApproval::getIsDeleted, 0).eq(OaApproval::getApplicantId, staff.getId())
            .eq(OaApproval::getApprovalType, "POINTS_CASH").orderByDesc(OaApproval::getCreateTime).last("LIMIT 10"))
        .stream().map(a -> {
          Map<String, Object> row = new LinkedHashMap<>();
          row.put("title", a.getTitle());
          row.put("amount", a.getAmount());
          row.put("status", a.getStatus());
          row.put("time", a.getCreateTime());
          return row;
        }).toList());
    return Result.ok(result);
  }

  /** 本人档案：基本、岗位、合同、紧急联系人、证书（只读） */
  @GetMapping("/profile")
  public Result<Map<String, Object>> profile() {
    StaffAccount staff = requireMe();
    StaffProfile profile = profileMapper.selectOne(Wrappers.lambdaQuery(StaffProfile.class)
        .eq(StaffProfile::getIsDeleted, 0).eq(StaffProfile::getStaffId, staff.getId()).last("LIMIT 1"));
    Department department = staff.getDepartmentId() == null ? null : departmentMapper.selectById(staff.getDepartmentId());
    List<StaffTrainingRecord> certificates = trainingMapper.selectList(Wrappers.lambdaQuery(StaffTrainingRecord.class)
        .eq(StaffTrainingRecord::getIsDeleted, 0).eq(StaffTrainingRecord::getStaffId, staff.getId())
        .and(w -> w.eq(StaffTrainingRecord::getTrainingScene, "CERTIFICATE").or().eq(StaffTrainingRecord::getTrainingType, "CERTIFICATE"))
        .orderByDesc(StaffTrainingRecord::getEndDate));
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("staffId", String.valueOf(staff.getId()));
    result.put("staffNo", staff.getStaffNo());
    result.put("username", staff.getUsername());
    result.put("realName", staff.getRealName());
    result.put("phone", staff.getPhone());
    result.put("email", staff.getEmail());
    result.put("departmentName", department == null ? null : department.getDeptName());
    result.put("onJob", Integer.valueOf(1).equals(staff.getStatus()));
    result.put("lastLoginTime", staff.getLastLoginTime());
    if (profile != null) {
      result.put("jobTitle", profile.getJobTitle());
      result.put("employmentType", profile.getEmploymentType());
      result.put("hireDate", profile.getHireDate());
      result.put("qualificationLevel", profile.getQualificationLevel());
      result.put("contractType", profile.getContractType());
      result.put("contractStatus", profile.getContractStatus());
      result.put("contractStartDate", profile.getContractStartDate());
      result.put("contractEndDate", profile.getContractEndDate());
      result.put("emergencyContactName", profile.getEmergencyContactName());
      result.put("emergencyContactPhone", profile.getEmergencyContactPhone());
    }
    result.put("certificates", certificates.stream().map(c -> {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("name", c.getTrainingName());
      row.put("certificateNo", maskTail(c.getCertificateNo()));
      row.put("validUntil", c.getEndDate());
      row.put("status", c.getCertificateStatus());
      return row;
    }).toList());
    return Result.ok(result);
  }

  public record ChangePasswordRequest(String oldPassword, String newPassword) {}

  /** 修改本人密码：校验原密码；不保留明文快照（人事不应看到员工自设的密码） */
  @PostMapping("/password")
  public Result<Void> changePassword(@RequestBody ChangePasswordRequest request) {
    StaffAccount staff = requireMe();
    if (request.oldPassword() == null || !passwordEncoder.matches(request.oldPassword(), staff.getPasswordHash())) {
      throw new IllegalArgumentException("原密码不正确");
    }
    String next = request.newPassword() == null ? "" : request.newPassword().trim();
    if (next.length() < MIN_PASSWORD_LENGTH || !next.matches(".*[A-Za-z].*") || !next.matches(".*\\d.*")) {
      throw new IllegalArgumentException("新密码至少 " + MIN_PASSWORD_LENGTH + " 位，且同时包含字母和数字");
    }
    if (passwordEncoder.matches(next, staff.getPasswordHash())) {
      throw new IllegalArgumentException("新密码不能与原密码相同");
    }
    staffMapper.update(null, Wrappers.lambdaUpdate(StaffAccount.class)
        .set(StaffAccount::getPasswordHash, passwordEncoder.encode(next))
        .set(StaffAccount::getPasswordPlaintextSnapshot, null)
        .set(StaffAccount::getPasswordSnapshotUpdatedAt, LocalDateTime.now())
        .eq(StaffAccount::getId, staff.getId()));
    auditLogService.record(staff.getOrgId(), staff.getOrgId(), staff.getId(), staff.getUsername(),
        "CHANGE_PASSWORD", "STAFF", staff.getId(), "员工本人修改密码");
    return Result.ok(null);
  }

  /** 本人最近登录记录（成功与失败），用于发现异常登录 */
  @GetMapping("/logins")
  public Result<List<Map<String, Object>>> logins() {
    StaffAccount staff = requireMe();
    return Result.ok(loginLogMapper.selectList(Wrappers.lambdaQuery(AuthLoginLog.class)
            .eq(AuthLoginLog::getIsDeleted, 0).eq(AuthLoginLog::getStaffId, staff.getId())
            .orderByDesc(AuthLoginLog::getCreateTime).last("LIMIT 10"))
        .stream().map(log -> {
          Map<String, Object> row = new LinkedHashMap<>();
          row.put("time", log.getCreateTime());
          row.put("type", log.getLoginType());
          row.put("result", log.getResult());
          row.put("failReason", log.getFailReason());
          row.put("ip", log.getIp());
          row.put("userAgent", log.getUserAgent());
          return row;
        }).toList());
  }

  private StaffAccount requireMe() {
    Long staffId = AuthContext.getStaffId();
    StaffAccount staff = staffId == null ? null : staffMapper.selectById(staffId);
    if (staff == null || Integer.valueOf(1).equals(staff.getIsDeleted())) {
      throw new IllegalArgumentException("当前账号不是员工账号");
    }
    return staff;
  }

  private static String maskTail(String value) {
    if (value == null || value.length() <= 4) {
      return value;
    }
    return "*".repeat(Math.min(value.length() - 4, 8)) + value.substring(value.length() - 4);
  }
}
