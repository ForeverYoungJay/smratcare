package com.zhiyangyun.care.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.entity.Department;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.entity.StaffRole;
import com.zhiyangyun.care.auth.mapper.DepartmentMapper;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.mapper.StaffRoleMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.model.StaffCredentialResponse;
import com.zhiyangyun.care.auth.model.StaffOptionResponse;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.auth.security.SupervisorRuleHelper;
import com.zhiyangyun.care.auth.model.StaffCreateRequest;
import com.zhiyangyun.care.auth.model.StaffUpdateRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@RequestMapping("/api/admin/staff")
public class AdminStaffController {
  private static final String DEFAULT_INITIAL_PASSWORD = "123456";
  private static final String RESERVED_SYS_ADMIN_CODE = "SYS_ADMIN";
  private static final String HIDDEN_PLATFORM_USERNAME = "sysadmin_root";
  private final StaffMapper staffMapper;
  private final DepartmentMapper departmentMapper;
  private final RoleMapper roleMapper;
  private final StaffRoleMapper staffRoleMapper;
  private final AuditLogService auditLogService;
  private final PasswordEncoder passwordEncoder;

  public AdminStaffController(
      StaffMapper staffMapper,
      DepartmentMapper departmentMapper,
      RoleMapper roleMapper,
      StaffRoleMapper staffRoleMapper,
      AuditLogService auditLogService,
      PasswordEncoder passwordEncoder) {
    this.staffMapper = staffMapper;
    this.departmentMapper = departmentMapper;
    this.roleMapper = roleMapper;
    this.staffRoleMapper = staffRoleMapper;
    this.auditLogService = auditLogService;
    this.passwordEncoder = passwordEncoder;
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping
  public Result<StaffAccount> create(@Valid @RequestBody StaffCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    Department department = requireDepartment(orgId, request.getDepartmentId(), "所属部门");
    String staffNo = resolveStaffNo(orgId, request.getStaffNo());
    String username = resolveUsername(request.getUsername(), staffNo);
    String password = resolvePassword(request.getPassword());
    StaffAccount staff = new StaffAccount();
    staff.setOrgId(orgId);
    staff.setDepartmentId(department.getId());
    staff.setStaffNo(staffNo);
    staff.setUsername(username);
    staff.setPasswordHash(passwordEncoder.encode(password));
    staff.setPasswordPlaintextSnapshot(password);
    staff.setPasswordSnapshotUpdatedAt(LocalDateTime.now());
    staff.setRealName(normalizeText(request.getRealName()));
    staff.setPhone(normalizeText(request.getPhone()));
    staff.setEmail(normalizeText(request.getEmail()));
    staff.setDirectLeaderId(request.getDirectLeaderId() == null || request.getDirectLeaderId() <= 0 ? null : request.getDirectLeaderId());
    staff.setIndirectLeaderId(request.getIndirectLeaderId() == null || request.getIndirectLeaderId() <= 0 ? null : request.getIndirectLeaderId());
    staff.setGender(request.getGender());
    staff.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    ensureUniqueStaffIdentity(orgId, username, staffNo, null);
    validateSupervisorChain(staff, null, staff.getDirectLeaderId(), staff.getIndirectLeaderId());
    staffMapper.insert(staff);
    fillRoleCodes(staff);
    recordSupervisorAudit(staff, null, null);
    return Result.ok(sanitizeStaffResponse(staff));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PutMapping
  public Result<StaffAccount> update(@Valid @RequestBody StaffUpdateRequest request) {
    StaffAccount staff = requireStaff(request.getId());
    rejectProtectedSysAdminStaff(staff, request.getStatus(), "系统超管账号为超级保护账号，不支持修改状态或删除");
    Long beforeDirectLeaderId = staff.getDirectLeaderId();
    Long beforeIndirectLeaderId = staff.getIndirectLeaderId();
    if (request.getDepartmentId() != null) {
      staff.setDepartmentId(requireDepartment(staff.getOrgId(), request.getDepartmentId(), "所属部门").getId());
    }
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      String normalizedPassword = request.getPassword().trim();
      staff.setPasswordHash(passwordEncoder.encode(normalizedPassword));
      staff.setPasswordPlaintextSnapshot(normalizedPassword);
      staff.setPasswordSnapshotUpdatedAt(LocalDateTime.now());
    }
    if (request.getRealName() != null) {
      staff.setRealName(normalizeText(request.getRealName()));
    }
    if (request.getPhone() != null) {
      staff.setPhone(normalizeText(request.getPhone()));
    }
    if (request.getEmail() != null) {
      staff.setEmail(normalizeText(request.getEmail()));
    }
    if (request.getDirectLeaderId() != null) {
      staff.setDirectLeaderId(request.getDirectLeaderId() <= 0 ? null : request.getDirectLeaderId());
    }
    if (request.getIndirectLeaderId() != null) {
      staff.setIndirectLeaderId(request.getIndirectLeaderId() <= 0 ? null : request.getIndirectLeaderId());
    }
    if (request.getGender() != null) {
      staff.setGender(request.getGender());
    }
    if (request.getStatus() != null) {
      staff.setStatus(request.getStatus());
    }
    ensureUniqueStaffIdentity(staff.getOrgId(), staff.getUsername(), staff.getStaffNo(), staff.getId());
    validateSupervisorChain(staff, request.getId(), staff.getDirectLeaderId(), staff.getIndirectLeaderId());
    staffMapper.updateById(staff);
    fillRoleCodes(staff);
    recordSupervisorAudit(staff, beforeDirectLeaderId, beforeIndirectLeaderId);
    return Result.ok(sanitizeStaffResponse(staff));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    StaffAccount staff = requireStaff(id);
    rejectProtectedSysAdminStaff(staff, null, "系统超管账号为超级保护账号，不支持删除");
    staff.setIsDeleted(1);
    staffMapper.updateById(staff);
    return Result.ok(null);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/{id}")
  public Result<StaffAccount> get(@PathVariable Long id) {
    StaffAccount staff = requireStaff(id);
    fillRoleCodes(staff);
    return Result.ok(sanitizeStaffResponse(staff));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/{id}/credentials")
  public Result<StaffCredentialResponse> getCredentials(@PathVariable Long id) {
    StaffAccount staff = requireStaff(id);
    return Result.ok(toCredentialResponse(staff));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PutMapping("/{id}/lock")
  public Result<StaffAccount> lock(@PathVariable Long id) {
    StaffAccount staff = requireStaff(id);
    rejectProtectedSysAdminStaff(staff, 0, "系统超管账号为超级保护账号，不支持锁定");
    staff.setStatus(0);
    staffMapper.updateById(staff);
    fillRoleCodes(staff);
    return Result.ok(sanitizeStaffResponse(staff));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PutMapping("/{id}/unlock")
  public Result<StaffAccount> unlock(@PathVariable Long id) {
    StaffAccount staff = requireStaff(id);
    rejectProtectedSysAdminStaff(staff, 1, "系统超管账号为超级保护账号，无需通过此入口解锁");
    staff.setStatus(1);
    staffMapper.updateById(staff);
    fillRoleCodes(staff);
    return Result.ok(sanitizeStaffResponse(staff));
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping
  public Result<IPage<StaffAccount>> list(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long departmentId,
      @RequestParam(required = false) Long roleId,
      @RequestParam(required = false) String sortBy,
      @RequestParam(defaultValue = "desc") String order) {
    Long orgId = AuthContext.getOrgId();
    List<Long> matchedRoleStaffIds = roleId == null ? List.of() : staffRoleMapper.selectList(Wrappers.lambdaQuery(StaffRole.class)
            .eq(StaffRole::getIsDeleted, 0)
            .eq(orgId != null, StaffRole::getOrgId, orgId)
            .eq(StaffRole::getRoleId, roleId))
        .stream()
        .map(StaffRole::getStaffId)
        .distinct()
        .toList();
    var wrapper = Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .ne(StaffAccount::getUsername, HIDDEN_PLATFORM_USERNAME)
        .eq(departmentId != null, StaffAccount::getDepartmentId, departmentId);
    if (roleId != null) {
      if (matchedRoleStaffIds.isEmpty()) {
        wrapper.eq(StaffAccount::getId, -1L);
      } else {
        wrapper.in(StaffAccount::getId, matchedRoleStaffIds);
      }
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(StaffAccount::getUsername, keyword)
          .or().like(StaffAccount::getRealName, keyword)
          .or().like(StaffAccount::getPhone, keyword)
          .or().like(StaffAccount::getStaffNo, keyword));
    }
    if (sortBy != null && !sortBy.isBlank()) {
      boolean asc = "asc".equalsIgnoreCase(order);
      if ("username".equals(sortBy)) {
        wrapper.orderBy(true, asc, StaffAccount::getUsername);
      } else if ("realName".equals(sortBy)) {
        wrapper.orderBy(true, asc, StaffAccount::getRealName);
      } else if ("createTime".equals(sortBy)) {
        wrapper.orderBy(true, asc, StaffAccount::getCreateTime);
      }
    }
    IPage<StaffAccount> result = staffMapper.selectPage(new Page<>(page, size), wrapper);
    result.getRecords().forEach(item -> {
      fillRoleCodes(item);
      sanitizeStaffResponse(item);
    });
    return Result.ok(result);
  }

  @PreAuthorize("isAuthenticated() and !hasRole('FAMILY')")
  @GetMapping("/options")
  public Result<IPage<StaffOptionResponse>> listOptions(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "200") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .ne(StaffAccount::getUsername, HIDDEN_PLATFORM_USERNAME)
        .eq(activeOnly, StaffAccount::getStatus, 1);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(StaffAccount::getUsername, keyword)
          .or().like(StaffAccount::getRealName, keyword)
          .or().like(StaffAccount::getPhone, keyword)
          .or().like(StaffAccount::getStaffNo, keyword));
    }
    wrapper.orderByAsc(StaffAccount::getRealName).orderByAsc(StaffAccount::getId);
    IPage<StaffAccount> sourcePage = staffMapper.selectPage(new Page<>(page, size), wrapper);
    sourcePage.getRecords().forEach(this::fillRoleCodes);
    Page<StaffOptionResponse> responsePage = new Page<>(sourcePage.getCurrent(), sourcePage.getSize(), sourcePage.getTotal());
    responsePage.setRecords(sourcePage.getRecords().stream().map(this::toStaffOptionResponse).toList());
    return Result.ok(responsePage);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/supervisor-anomalies")
  public Result<List<Map<String, Object>>> supervisorAnomalies() {
    Long orgId = AuthContext.getOrgId();
    List<StaffAccount> staffs = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .ne(StaffAccount::getUsername, HIDDEN_PLATFORM_USERNAME));
    Map<Long, StaffAccount> staffMap = staffs.stream()
        .filter(item -> item != null && item.getId() != null)
        .collect(Collectors.toMap(StaffAccount::getId, item -> item, (a, b) -> a, LinkedHashMap::new));
    List<Map<String, Object>> rows = staffs.stream()
        .map(item -> buildSupervisorAnomaly(item, staffMap))
        .filter(item -> item != null)
        .toList();
    return Result.ok(rows);
  }

  private void fillRoleCodes(StaffAccount staff) {
    if (staff == null || staff.getId() == null || staff.getOrgId() == null) {
      return;
    }
    staff.setRoleCodes(roleMapper.selectRoleCodesByStaff(staff.getId(), staff.getOrgId()));
  }

  private void rejectProtectedSysAdminStaff(StaffAccount staff, Integer targetStatus, String message) {
    if (staff == null || staff.getId() == null || staff.getOrgId() == null) {
      return;
    }
    List<String> roleCodes = staff.getRoleCodes();
    if (roleCodes == null || roleCodes.isEmpty()) {
      roleCodes = roleMapper.selectRoleCodesByStaff(staff.getId(), staff.getOrgId());
      staff.setRoleCodes(roleCodes);
    }
    boolean isProtected = roleCodes != null && roleCodes.stream()
        .map(code -> code == null ? null : code.trim().toUpperCase())
        .anyMatch(RESERVED_SYS_ADMIN_CODE::equals);
    if (!isProtected) {
      return;
    }
    if (targetStatus == null || targetStatus.intValue() != 1) {
      throw new IllegalArgumentException(message);
    }
  }

  private StaffAccount sanitizeStaffResponse(StaffAccount staff) {
    if (staff == null) {
      return null;
    }
    staff.setPasswordHash(null);
    staff.setPasswordPlaintextSnapshot(null);
    return staff;
  }

  private StaffCredentialResponse toCredentialResponse(StaffAccount staff) {
    StaffCredentialResponse response = new StaffCredentialResponse();
    response.setStaffId(staff.getId());
    response.setStaffNo(staff.getStaffNo());
    response.setUsername(staff.getUsername());
    response.setRealName(staff.getRealName());
    response.setStatus(staff.getStatus());
    response.setPasswordPlaintextSnapshot(staff.getPasswordPlaintextSnapshot());
    response.setPasswordSnapshotUpdatedAt(staff.getPasswordSnapshotUpdatedAt());
    return response;
  }

  private StaffAccount requireStaff(Long id) {
    StaffAccount staff = staffMapper.selectById(id);
    if (staff == null || staff.getIsDeleted() != null && staff.getIsDeleted() == 1) {
      throw new IllegalArgumentException("员工不存在");
    }
    AuthContext.requireOrgAccess(staff.getOrgId());
    return staff;
  }

  private Department requireDepartment(Long orgId, Long departmentId, String label) {
    Department department = departmentMapper.selectById(departmentId);
    if (department == null || department.getIsDeleted() != null && department.getIsDeleted() == 1) {
      throw new IllegalArgumentException(label + "不存在");
    }
    if (orgId == null || !orgId.equals(department.getOrgId())) {
      throw new IllegalArgumentException(label + "不属于当前机构");
    }
    return department;
  }

  private void ensureUniqueStaffIdentity(Long orgId, String username, String staffNo, Long excludeId) {
    if (username != null) {
      StaffAccount existing = staffMapper.selectOne(Wrappers.lambdaQuery(StaffAccount.class)
          .eq(StaffAccount::getIsDeleted, 0)
          .eq(orgId != null, StaffAccount::getOrgId, orgId)
          .eq(StaffAccount::getUsername, username)
          .ne(excludeId != null, StaffAccount::getId, excludeId)
          .last("LIMIT 1"));
      if (existing != null) {
        throw new IllegalArgumentException("登录账号已存在，请调整后重试");
      }
    }
    if (staffNo != null) {
      StaffAccount existing = staffMapper.selectOne(Wrappers.lambdaQuery(StaffAccount.class)
          .eq(StaffAccount::getIsDeleted, 0)
          .eq(orgId != null, StaffAccount::getOrgId, orgId)
          .eq(StaffAccount::getStaffNo, staffNo)
          .ne(excludeId != null, StaffAccount::getId, excludeId)
          .last("LIMIT 1"));
      if (existing != null) {
        throw new IllegalArgumentException("职工号已存在，请调整后重试");
      }
    }
  }

  private String resolveStaffNo(Long orgId, String requestStaffNo) {
    String normalized = normalizeText(requestStaffNo);
    if (normalized != null) {
      return normalized;
    }
    String orgSegment = String.format("%02d", Math.abs(orgId == null ? 0 : orgId.intValue()) % 100);
    for (int index = 1; index <= 99; index++) {
      String candidate = "YG" + orgSegment + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmm"))
          + String.format("%02d", index);
      Long count = staffMapper.selectCount(Wrappers.lambdaQuery(StaffAccount.class)
          .eq(StaffAccount::getIsDeleted, 0)
          .eq(orgId != null, StaffAccount::getOrgId, orgId)
          .eq(StaffAccount::getStaffNo, candidate));
      if (count == null || count == 0) {
        return candidate;
      }
    }
    return "YG" + orgSegment + System.currentTimeMillis();
  }

  private String resolveUsername(String requestUsername, String staffNo) {
    String normalized = normalizeText(requestUsername);
    return normalized == null ? staffNo : normalized;
  }

  private String resolvePassword(String requestPassword) {
    String normalized = normalizeText(requestPassword);
    return normalized == null ? DEFAULT_INITIAL_PASSWORD : normalized;
  }

  private String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private StaffOptionResponse toStaffOptionResponse(StaffAccount staff) {
    StaffOptionResponse row = new StaffOptionResponse();
    row.setId(staff.getId());
    row.setUsername(staff.getUsername());
    row.setRealName(staff.getRealName());
    row.setStatus(staff.getStatus());
    row.setDepartmentId(staff.getDepartmentId());
    row.setOrgId(staff.getOrgId());
    row.setStaffNo(staff.getStaffNo());
    row.setPhone(staff.getPhone());
    row.setRoleCodes(staff.getRoleCodes());
    return row;
  }

  private void validateSupervisorChain(
      StaffAccount target,
      Long targetId,
      Long directLeaderId,
      Long indirectLeaderId) {
    if (target == null) {
      return;
    }
    if (targetId != null) {
      if (directLeaderId != null && targetId.equals(directLeaderId)) {
        throw new IllegalArgumentException("直接领导不能设置为本人");
      }
      if (indirectLeaderId != null && targetId.equals(indirectLeaderId)) {
        throw new IllegalArgumentException("间接领导不能设置为本人");
      }
    }
    if (directLeaderId != null && indirectLeaderId != null && directLeaderId.equals(indirectLeaderId)) {
      throw new IllegalArgumentException("直接领导与间接领导不能为同一人");
    }
    List<String> targetRoleCodes = targetId == null || target.getOrgId() == null
        ? List.of()
        : SupervisorRuleHelper.normalizeRoleCodes(roleMapper.selectRoleCodesByStaff(targetId, target.getOrgId()));
    SupervisorRuleHelper.Level targetLevel = SupervisorRuleHelper.resolveLevel(targetRoleCodes);
    if (directLeaderId != null) {
      StaffAccount directLeader = requireLeader(target.getOrgId(), directLeaderId, "直接领导");
      List<String> directRoles = SupervisorRuleHelper.normalizeRoleCodes(roleMapper.selectRoleCodesByStaff(directLeader.getId(), directLeader.getOrgId()));
      if (!SupervisorRuleHelper.canBeDirectLeader(
          targetLevel,
          target.getDepartmentId(),
          directLeader.getDepartmentId(),
          directRoles)) {
        throw new IllegalArgumentException("当前组织规则下，直接领导应为本部门部长（员工）或院长/SYS_ADMIN（部长）");
      }
    }
    if (indirectLeaderId != null) {
      StaffAccount indirectLeader = requireLeader(target.getOrgId(), indirectLeaderId, "间接领导");
      List<String> indirectRoles = SupervisorRuleHelper.normalizeRoleCodes(roleMapper.selectRoleCodesByStaff(indirectLeader.getId(), indirectLeader.getOrgId()));
      if (!SupervisorRuleHelper.canBeIndirectLeader(targetLevel, indirectRoles)) {
        throw new IllegalArgumentException("当前组织规则下，间接领导需为院长或SYS_ADMIN");
      }
    }
  }

  private StaffAccount requireLeader(Long orgId, Long staffId, String label) {
    StaffAccount staff = staffMapper.selectOne(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getId, staffId)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .last("LIMIT 1"));
    if (staff == null) {
      throw new IllegalArgumentException(label + "不存在或不可用");
    }
    return staff;
  }

  private void recordSupervisorAudit(StaffAccount target, Long beforeDirectLeaderId, Long beforeIndirectLeaderId) {
    if (target == null || target.getId() == null || target.getOrgId() == null) {
      return;
    }
    if (equalsNullable(beforeDirectLeaderId, target.getDirectLeaderId())
        && equalsNullable(beforeIndirectLeaderId, target.getIndirectLeaderId())) {
      return;
    }
    Map<String, Object> detail = new LinkedHashMap<>();
    detail.put("staffId", target.getId());
    detail.put("staffName", target.getRealName());
    detail.put("beforeDirectLeaderId", beforeDirectLeaderId);
    detail.put("afterDirectLeaderId", target.getDirectLeaderId());
    detail.put("beforeIndirectLeaderId", beforeIndirectLeaderId);
    detail.put("afterIndirectLeaderId", target.getIndirectLeaderId());
    auditLogService.record(
        target.getOrgId(),
        target.getOrgId(),
        AuthContext.getStaffId(),
        AuthContext.getUsername(),
        "SUPERVISOR_CHAIN_UPDATE",
        "STAFF",
        target.getId(),
        detail.toString());
  }

  private boolean equalsNullable(Long a, Long b) {
    if (a == null && b == null) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }
    return a.equals(b);
  }

  private Map<String, Object> buildSupervisorAnomaly(StaffAccount target, Map<Long, StaffAccount> staffMap) {
    if (target == null || target.getId() == null) {
      return null;
    }
    String issue = detectSupervisorIssue(target, staffMap);
    if (issue == null) {
      return null;
    }
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("staffId", target.getId());
    row.put("staffNo", target.getStaffNo());
    row.put("staffName", target.getRealName());
    row.put("departmentId", target.getDepartmentId());
    row.put("directLeaderId", target.getDirectLeaderId());
    row.put("indirectLeaderId", target.getIndirectLeaderId());
    row.put("issue", issue);
    return row;
  }

  private String detectSupervisorIssue(StaffAccount target, Map<Long, StaffAccount> staffMap) {
    Long targetId = target.getId();
    Long directLeaderId = target.getDirectLeaderId();
    Long indirectLeaderId = target.getIndirectLeaderId();
    if (targetId != null && targetId.equals(directLeaderId)) {
      return "直接领导不能为本人";
    }
    if (targetId != null && targetId.equals(indirectLeaderId)) {
      return "间接领导不能为本人";
    }
    if (directLeaderId != null && directLeaderId.equals(indirectLeaderId)) {
      return "直接领导与间接领导不能为同一人";
    }
    List<String> targetRoles = SupervisorRuleHelper.normalizeRoleCodes(
        roleMapper.selectRoleCodesByStaff(targetId, target.getOrgId()));
    SupervisorRuleHelper.Level targetLevel = SupervisorRuleHelper.resolveLevel(targetRoles);
    if (directLeaderId != null) {
      StaffAccount directLeader = staffMap.get(directLeaderId);
      if (directLeader == null) {
        return "直接领导不存在";
      }
      List<String> directRoles = SupervisorRuleHelper.normalizeRoleCodes(
          roleMapper.selectRoleCodesByStaff(directLeaderId, target.getOrgId()));
      if (!SupervisorRuleHelper.canBeDirectLeader(targetLevel, target.getDepartmentId(), directLeader.getDepartmentId(), directRoles)) {
        return "直接领导不符合层级规则";
      }
    }
    if (indirectLeaderId != null) {
      StaffAccount indirectLeader = staffMap.get(indirectLeaderId);
      if (indirectLeader == null) {
        return "间接领导不存在";
      }
      List<String> indirectRoles = SupervisorRuleHelper.normalizeRoleCodes(
          roleMapper.selectRoleCodesByStaff(indirectLeaderId, target.getOrgId()));
      if (!SupervisorRuleHelper.canBeIndirectLeader(targetLevel, indirectRoles)) {
        return "间接领导不符合层级规则";
      }
    }
    return null;
  }
}
