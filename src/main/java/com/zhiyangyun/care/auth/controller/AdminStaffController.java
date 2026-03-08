package com.zhiyangyun.care.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.model.StaffOptionResponse;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.auth.security.SupervisorRuleHelper;
import com.zhiyangyun.care.auth.model.StaffCreateRequest;
import com.zhiyangyun.care.auth.model.StaffUpdateRequest;
import jakarta.validation.Valid;
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
  private final StaffMapper staffMapper;
  private final RoleMapper roleMapper;
  private final AuditLogService auditLogService;
  private final PasswordEncoder passwordEncoder;

  public AdminStaffController(
      StaffMapper staffMapper,
      RoleMapper roleMapper,
      AuditLogService auditLogService,
      PasswordEncoder passwordEncoder) {
    this.staffMapper = staffMapper;
    this.roleMapper = roleMapper;
    this.auditLogService = auditLogService;
    this.passwordEncoder = passwordEncoder;
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping
  public Result<StaffAccount> create(@Valid @RequestBody StaffCreateRequest request) {
    StaffAccount staff = new StaffAccount();
    staff.setOrgId(AuthContext.getOrgId());
    staff.setDepartmentId(request.getDepartmentId());
    staff.setStaffNo(request.getStaffNo());
    staff.setUsername(request.getUsername());
    staff.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    staff.setRealName(request.getRealName());
    staff.setPhone(request.getPhone());
    staff.setEmail(request.getEmail());
    staff.setDirectLeaderId(request.getDirectLeaderId() == null || request.getDirectLeaderId() <= 0 ? null : request.getDirectLeaderId());
    staff.setIndirectLeaderId(request.getIndirectLeaderId() == null || request.getIndirectLeaderId() <= 0 ? null : request.getIndirectLeaderId());
    staff.setGender(request.getGender());
    staff.setStatus(request.getStatus());
    validateSupervisorChain(staff, null, staff.getDirectLeaderId(), staff.getIndirectLeaderId());
    staffMapper.insert(staff);
    fillRoleCodes(staff);
    recordSupervisorAudit(staff, null, null);
    return Result.ok(staff);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PutMapping
  public Result<StaffAccount> update(@Valid @RequestBody StaffUpdateRequest request) {
    StaffAccount staff = staffMapper.selectById(request.getId());
    if (staff == null) {
      return Result.error(404, "Staff not found");
    }
    Long beforeDirectLeaderId = staff.getDirectLeaderId();
    Long beforeIndirectLeaderId = staff.getIndirectLeaderId();
    if (request.getDepartmentId() != null) {
      staff.setDepartmentId(request.getDepartmentId());
    }
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      staff.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    }
    if (request.getRealName() != null) {
      staff.setRealName(request.getRealName());
    }
    if (request.getPhone() != null) {
      staff.setPhone(request.getPhone());
    }
    if (request.getEmail() != null) {
      staff.setEmail(request.getEmail());
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
    validateSupervisorChain(staff, request.getId(), staff.getDirectLeaderId(), staff.getIndirectLeaderId());
    staffMapper.updateById(staff);
    fillRoleCodes(staff);
    recordSupervisorAudit(staff, beforeDirectLeaderId, beforeIndirectLeaderId);
    return Result.ok(staff);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    StaffAccount staff = staffMapper.selectById(id);
    if (staff == null) {
      return Result.error(404, "Staff not found");
    }
    staff.setIsDeleted(1);
    staffMapper.updateById(staff);
    return Result.ok(null);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/{id}")
  public Result<StaffAccount> get(@PathVariable Long id) {
    StaffAccount staff = staffMapper.selectById(id);
    fillRoleCodes(staff);
    return Result.ok(staff);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping
  public Result<IPage<StaffAccount>> list(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sortBy,
      @RequestParam(defaultValue = "desc") String order) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId);
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
    result.getRecords().forEach(this::fillRoleCodes);
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
        .eq(orgId != null, StaffAccount::getOrgId, orgId));
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
