package com.zhiyangyun.care.ai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.ai.entity.AiScheduleConstraint;
import com.zhiyangyun.care.ai.entity.AiScheduleProposal;
import com.zhiyangyun.care.ai.entity.AiScheduleProposalItem;
import com.zhiyangyun.care.ai.mapper.AiScheduleConstraintMapper;
import com.zhiyangyun.care.ai.mapper.AiScheduleProposalItemMapper;
import com.zhiyangyun.care.ai.mapper.AiScheduleProposalMapper;
import com.zhiyangyun.care.ai.model.AiScheduleAdoptResponse;
import com.zhiyangyun.care.ai.model.AiScheduleConstraintRequest;
import com.zhiyangyun.care.ai.model.AiScheduleGenerateRequest;
import com.zhiyangyun.care.ai.model.AiScheduleItemUpdateRequest;
import com.zhiyangyun.care.ai.model.AiScheduleProposalItemResponse;
import com.zhiyangyun.care.ai.model.AiScheduleProposalResponse;
import com.zhiyangyun.care.ai.model.SolverAssignment;
import com.zhiyangyun.care.ai.model.SolverConstraint;
import com.zhiyangyun.care.ai.model.SolverResult;
import com.zhiyangyun.care.ai.model.SolverShift;
import com.zhiyangyun.care.ai.model.SolverStaff;
import com.zhiyangyun.care.ai.service.AiScheduleService;
import com.zhiyangyun.care.ai.service.AiScheduleSolver;
import com.zhiyangyun.care.auth.entity.Role;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.entity.StaffRole;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.mapper.StaffRoleMapper;
import com.zhiyangyun.care.nursing.entity.ShiftTemplate;
import com.zhiyangyun.care.nursing.mapper.ShiftTemplateMapper;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.schedule.entity.StaffSchedule;
import com.zhiyangyun.care.schedule.mapper.StaffScheduleMapper;
import com.zhiyangyun.care.schedule.model.ScheduleRequest;
import com.zhiyangyun.care.schedule.model.ScheduleResponse;
import com.zhiyangyun.care.schedule.service.ScheduleService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiScheduleServiceImpl implements AiScheduleService {
  private static final int MAX_PERIOD_DAYS = 31;

  private final AiScheduleProposalMapper proposalMapper;
  private final AiScheduleProposalItemMapper itemMapper;
  private final AiScheduleConstraintMapper constraintMapper;
  private final AiScheduleSolver solver;
  private final StaffMapper staffMapper;
  private final StaffRoleMapper staffRoleMapper;
  private final RoleMapper roleMapper;
  private final ShiftTemplateMapper shiftTemplateMapper;
  private final OaApprovalMapper approvalMapper;
  private final StaffScheduleMapper staffScheduleMapper;
  private final ScheduleService scheduleService;
  private final ObjectMapper objectMapper;

  public AiScheduleServiceImpl(
      AiScheduleProposalMapper proposalMapper,
      AiScheduleProposalItemMapper itemMapper,
      AiScheduleConstraintMapper constraintMapper,
      AiScheduleSolver solver,
      StaffMapper staffMapper,
      StaffRoleMapper staffRoleMapper,
      RoleMapper roleMapper,
      ShiftTemplateMapper shiftTemplateMapper,
      OaApprovalMapper approvalMapper,
      StaffScheduleMapper staffScheduleMapper,
      ScheduleService scheduleService,
      ObjectMapper objectMapper) {
    this.proposalMapper = proposalMapper;
    this.itemMapper = itemMapper;
    this.constraintMapper = constraintMapper;
    this.solver = solver;
    this.staffMapper = staffMapper;
    this.staffRoleMapper = staffRoleMapper;
    this.roleMapper = roleMapper;
    this.shiftTemplateMapper = shiftTemplateMapper;
    this.approvalMapper = approvalMapper;
    this.staffScheduleMapper = staffScheduleMapper;
    this.scheduleService = scheduleService;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public AiScheduleProposalResponse generate(Long orgId, Long staffId, AiScheduleGenerateRequest request) {
    LocalDate from = request.getDateFrom();
    LocalDate to = request.getDateTo();
    if (from == null || to == null || to.isBefore(from)) {
      throw new IllegalArgumentException("排班周期不合法");
    }
    if (from.plusDays(MAX_PERIOD_DAYS).isBefore(to)) {
      throw new IllegalArgumentException("排班周期最长 " + MAX_PERIOD_DAYS + " 天");
    }
    List<LocalDate> days = new ArrayList<>();
    for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
      days.add(d);
    }

    List<StaffAccount> staffAccounts = loadStaff(orgId, request);
    if (staffAccounts.isEmpty()) {
      throw new IllegalArgumentException("没有可参与排班的员工");
    }
    Map<Long, List<String>> roleMap = loadRoleCodes(orgId, staffAccounts);
    List<SolverStaff> solverStaff = staffAccounts.stream().map(account -> {
      SolverStaff s = new SolverStaff();
      s.setId(account.getId());
      s.setName(account.getRealName());
      s.setRoleCodes(roleMap.getOrDefault(account.getId(), List.of()));
      return s;
    }).toList();

    AiScheduleConstraint constraintConfig = getConstraint(orgId);
    Map<String, ShiftTemplate> templates = loadShiftTemplates(orgId, request.getShiftCodes());
    if (templates.isEmpty()) {
      throw new IllegalArgumentException("没有可用的班次模板，请先在护理排班配置班次");
    }
    Map<String, List<String>> qualification = parseQualification(constraintConfig.getQualificationJson());
    List<SolverShift> shifts = templates.values().stream().map(tpl -> {
      SolverShift shift = new SolverShift();
      shift.setShiftCode(tpl.getShiftCode());
      shift.setShiftName(tpl.getName());
      shift.setStartTime(tpl.getStartTime());
      shift.setEndTime(tpl.getEndTime());
      shift.setCrossDay(tpl.getCrossDay());
      shift.setRequiredCount(tpl.getRequiredStaffCount() == null || tpl.getRequiredStaffCount() <= 0
          ? 1 : tpl.getRequiredStaffCount());
      shift.setNight(isNightShift(tpl));
      shift.setRequiredRoles(qualification.get(tpl.getShiftCode()));
      return shift;
    }).toList();

    Set<String> blocked = new HashSet<>();
    List<Long> staffIds = staffAccounts.stream().map(StaffAccount::getId).toList();
    if (constraintConfig.getRespectLeave() == null || constraintConfig.getRespectLeave() == 1) {
      blocked.addAll(loadLeaveBlocked(orgId, staffIds, from, to));
    }
    blocked.addAll(loadExistingScheduleBlocked(orgId, staffIds, from, to));

    SolverConstraint solverConstraint = toSolverConstraint(constraintConfig);
    SolverResult result = solver.solve(days, shifts, solverStaff, blocked, solverConstraint);

    AiScheduleProposal proposal = new AiScheduleProposal();
    proposal.setTenantId(orgId);
    proposal.setOrgId(orgId);
    proposal.setTitle(request.getTitle() == null || request.getTitle().isBlank()
        ? "智能排班 " + from + "~" + to : request.getTitle());
    proposal.setDeptId(request.getDeptId());
    proposal.setDeptName(request.getDeptName());
    proposal.setDateFrom(from);
    proposal.setDateTo(to);
    proposal.setShiftCodes(String.join(",", templates.keySet()));
    proposal.setStaffIds(staffIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
    proposal.setStatus("GENERATED");
    proposal.setConstraintSnapshotJson(writeJson(solverConstraint));
    proposal.setScoreJson(writeJson(buildScoreSummary(result)));
    proposal.setUnfilledCount(result.getUnfilledSlots().size());
    proposal.setGeneratedAt(LocalDateTime.now());
    proposal.setRemark(request.getRemark());
    proposal.setCreatedBy(staffId);
    proposalMapper.insert(proposal);

    for (SolverAssignment assignment : result.getAssignments()) {
      AiScheduleProposalItem item = new AiScheduleProposalItem();
      item.setTenantId(orgId);
      item.setOrgId(orgId);
      item.setProposalId(proposal.getId());
      item.setStaffId(assignment.getStaffId());
      item.setStaffName(assignment.getStaffName());
      item.setDutyDate(assignment.getDutyDate());
      item.setShiftCode(assignment.getShiftCode());
      ShiftTemplate tpl = templates.get(assignment.getShiftCode());
      item.setStartTime(buildStartTime(assignment.getDutyDate(), tpl));
      item.setEndTime(buildEndTime(assignment.getDutyDate(), tpl));
      item.setNightShift(assignment.isNight() ? 1 : 0);
      item.setManualAdjusted(0);
      item.setCreatedBy(staffId);
      itemMapper.insert(item);
    }
    return detail(orgId, proposal.getId());
  }

  @Override
  public IPage<AiScheduleProposalResponse> page(Long orgId, long pageNo, long pageSize, String status) {
    IPage<AiScheduleProposal> page = proposalMapper.selectPage(new Page<>(pageNo, pageSize),
        Wrappers.lambdaQuery(AiScheduleProposal.class)
            .eq(AiScheduleProposal::getIsDeleted, 0)
            .eq(orgId != null, AiScheduleProposal::getOrgId, orgId)
            .eq(status != null && !status.isBlank(), AiScheduleProposal::getStatus, status)
            .orderByDesc(AiScheduleProposal::getCreateTime));
    IPage<AiScheduleProposalResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(p -> toResponse(p, false)).toList());
    return resp;
  }

  @Override
  public AiScheduleProposalResponse detail(Long orgId, Long id) {
    AiScheduleProposal proposal = selectProposal(orgId, id);
    if (proposal == null) {
      return null;
    }
    return toResponse(proposal, true);
  }

  @Override
  public AiScheduleProposalItemResponse updateItem(
      Long orgId, Long staffId, Long proposalId, Long itemId, AiScheduleItemUpdateRequest request) {
    AiScheduleProposal proposal = selectProposal(orgId, proposalId);
    AiScheduleProposalItem item = itemMapper.selectById(itemId);
    if (proposal == null || item == null || !proposalId.equals(item.getProposalId())
        || (item.getIsDeleted() != null && item.getIsDeleted() == 1)) {
      return null;
    }
    if ("ADOPTED".equals(proposal.getStatus())) {
      throw new IllegalStateException("方案已采纳，不能再调整");
    }
    if (request.getStaffId() != null && !request.getStaffId().equals(item.getStaffId())) {
      StaffAccount account = staffMapper.selectById(request.getStaffId());
      if (account == null || (orgId != null && !orgId.equals(account.getOrgId()))) {
        throw new IllegalArgumentException("目标员工不存在");
      }
      item.setStaffId(account.getId());
      item.setStaffName(account.getRealName());
    }
    if (request.getShiftCode() != null && !request.getShiftCode().isBlank()
        && !request.getShiftCode().equals(item.getShiftCode())) {
      Map<String, ShiftTemplate> templates = loadShiftTemplates(orgId, List.of(request.getShiftCode()));
      ShiftTemplate tpl = templates.get(request.getShiftCode());
      if (tpl == null) {
        throw new IllegalArgumentException("班次模板不存在：" + request.getShiftCode());
      }
      item.setShiftCode(tpl.getShiftCode());
      item.setStartTime(buildStartTime(item.getDutyDate(), tpl));
      item.setEndTime(buildEndTime(item.getDutyDate(), tpl));
      item.setNightShift(isNightShift(tpl) ? 1 : 0);
    }
    item.setManualAdjusted(1);
    item.setViolationNote(checkItemViolation(orgId, item));
    itemMapper.updateById(item);
    return toItemResponse(item);
  }

  @Override
  public void deleteItem(Long orgId, Long proposalId, Long itemId) {
    AiScheduleProposal proposal = selectProposal(orgId, proposalId);
    AiScheduleProposalItem item = itemMapper.selectById(itemId);
    if (proposal == null || item == null || !proposalId.equals(item.getProposalId())) {
      return;
    }
    if ("ADOPTED".equals(proposal.getStatus())) {
      throw new IllegalStateException("方案已采纳，不能再调整");
    }
    item.setIsDeleted(1);
    itemMapper.updateById(item);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public AiScheduleAdoptResponse adopt(Long orgId, Long staffId, Long proposalId) {
    AiScheduleProposal proposal = selectProposal(orgId, proposalId);
    if (proposal == null) {
      return null;
    }
    if ("ADOPTED".equals(proposal.getStatus())) {
      throw new IllegalStateException("方案已采纳，请勿重复操作");
    }
    List<AiScheduleProposalItem> items = itemMapper.selectList(
        Wrappers.lambdaQuery(AiScheduleProposalItem.class)
            .eq(AiScheduleProposalItem::getIsDeleted, 0)
            .eq(AiScheduleProposalItem::getProposalId, proposalId));
    int created = 0;
    int skipped = 0;
    for (AiScheduleProposalItem item : items) {
      if (item.getScheduleId() != null) {
        skipped++;
        continue;
      }
      Long existing = findExistingScheduleId(orgId, item);
      if (existing != null) {
        item.setScheduleId(existing);
        itemMapper.updateById(item);
        skipped++;
        continue;
      }
      // 复用 schedule 模块的写入逻辑（含值班日历/待办联动）
      ScheduleRequest scheduleRequest = new ScheduleRequest();
      scheduleRequest.setStaffId(item.getStaffId());
      scheduleRequest.setDutyDate(item.getDutyDate());
      scheduleRequest.setShiftCode(item.getShiftCode());
      scheduleRequest.setStartTime(item.getStartTime());
      scheduleRequest.setEndTime(item.getEndTime());
      scheduleRequest.setSourceTemplateName("AI排班·" + proposal.getTitle());
      scheduleRequest.setStatus(1);
      ScheduleResponse response = scheduleService.create(orgId, staffId, scheduleRequest);
      if (response != null && response.getId() != null) {
        item.setScheduleId(response.getId());
        itemMapper.updateById(item);
        created++;
      }
    }
    proposal.setStatus("ADOPTED");
    proposal.setAdoptedAt(LocalDateTime.now());
    proposal.setAdoptedBy(staffId);
    proposalMapper.updateById(proposal);

    AiScheduleAdoptResponse response = new AiScheduleAdoptResponse();
    response.setProposalId(proposalId);
    response.setCreatedCount(created);
    response.setSkippedCount(skipped);
    return response;
  }

  @Override
  public void delete(Long orgId, Long id) {
    AiScheduleProposal proposal = selectProposal(orgId, id);
    if (proposal == null) {
      return;
    }
    proposal.setIsDeleted(1);
    proposalMapper.updateById(proposal);
  }

  @Override
  public AiScheduleConstraint getConstraint(Long orgId) {
    AiScheduleConstraint config = constraintMapper.selectOne(Wrappers.lambdaQuery(AiScheduleConstraint.class)
        .eq(AiScheduleConstraint::getIsDeleted, 0)
        .eq(orgId != null, AiScheduleConstraint::getOrgId, orgId)
        .orderByDesc(AiScheduleConstraint::getId)
        .last("LIMIT 1"));
    if (config != null) {
      return config;
    }
    AiScheduleConstraint defaults = new AiScheduleConstraint();
    defaults.setOrgId(orgId);
    defaults.setMaxWeeklyHours(BigDecimal.valueOf(40));
    defaults.setMaxConsecutiveDays(5);
    defaults.setNightRestEnabled(1);
    defaults.setRespectLeave(1);
    defaults.setWorkloadBalanceWeight(BigDecimal.ONE);
    defaults.setNightFairnessWeight(BigDecimal.ONE);
    defaults.setEnabled(1);
    return defaults;
  }

  @Override
  public AiScheduleConstraint saveConstraint(Long orgId, Long staffId, AiScheduleConstraintRequest request) {
    AiScheduleConstraint config = constraintMapper.selectOne(Wrappers.lambdaQuery(AiScheduleConstraint.class)
        .eq(AiScheduleConstraint::getIsDeleted, 0)
        .eq(orgId != null, AiScheduleConstraint::getOrgId, orgId)
        .orderByDesc(AiScheduleConstraint::getId)
        .last("LIMIT 1"));
    boolean isNew = config == null;
    if (isNew) {
      config = new AiScheduleConstraint();
      config.setTenantId(orgId);
      config.setOrgId(orgId);
      config.setCreatedBy(staffId);
      config.setEnabled(1);
    }
    if (request.getMaxWeeklyHours() != null) {
      config.setMaxWeeklyHours(request.getMaxWeeklyHours());
    }
    if (request.getMaxConsecutiveDays() != null) {
      config.setMaxConsecutiveDays(request.getMaxConsecutiveDays());
    }
    if (request.getNightRestEnabled() != null) {
      config.setNightRestEnabled(request.getNightRestEnabled());
    }
    if (request.getRespectLeave() != null) {
      config.setRespectLeave(request.getRespectLeave());
    }
    if (request.getQualificationJson() != null) {
      config.setQualificationJson(request.getQualificationJson());
    }
    if (request.getWorkloadBalanceWeight() != null) {
      config.setWorkloadBalanceWeight(request.getWorkloadBalanceWeight());
    }
    if (request.getNightFairnessWeight() != null) {
      config.setNightFairnessWeight(request.getNightFairnessWeight());
    }
    if (request.getRemark() != null) {
      config.setRemark(request.getRemark());
    }
    if (isNew) {
      if (config.getMaxWeeklyHours() == null) {
        config.setMaxWeeklyHours(BigDecimal.valueOf(40));
      }
      if (config.getMaxConsecutiveDays() == null) {
        config.setMaxConsecutiveDays(5);
      }
      if (config.getNightRestEnabled() == null) {
        config.setNightRestEnabled(1);
      }
      if (config.getRespectLeave() == null) {
        config.setRespectLeave(1);
      }
      if (config.getWorkloadBalanceWeight() == null) {
        config.setWorkloadBalanceWeight(BigDecimal.ONE);
      }
      if (config.getNightFairnessWeight() == null) {
        config.setNightFairnessWeight(BigDecimal.ONE);
      }
      constraintMapper.insert(config);
    } else {
      constraintMapper.updateById(config);
    }
    return config;
  }

  private AiScheduleProposal selectProposal(Long orgId, Long id) {
    AiScheduleProposal proposal = proposalMapper.selectById(id);
    if (proposal == null || (proposal.getIsDeleted() != null && proposal.getIsDeleted() == 1)
        || (orgId != null && !orgId.equals(proposal.getOrgId()))) {
      return null;
    }
    return proposal;
  }

  private List<StaffAccount> loadStaff(Long orgId, AiScheduleGenerateRequest request) {
    if (request.getStaffIds() != null && !request.getStaffIds().isEmpty()) {
      return staffMapper.selectBatchIdsSafe(request.getStaffIds()).stream()
          .filter(s -> s.getIsDeleted() == null || s.getIsDeleted() == 0)
          .filter(s -> orgId == null || orgId.equals(s.getOrgId()))
          .toList();
    }
    return staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .eq(request.getDeptId() != null, StaffAccount::getDepartmentId, request.getDeptId())
        .eq(StaffAccount::getStatus, 1));
  }

  private Map<Long, List<String>> loadRoleCodes(Long orgId, List<StaffAccount> staffAccounts) {
    List<Long> staffIds = staffAccounts.stream().map(StaffAccount::getId).toList();
    if (staffIds.isEmpty()) {
      return Map.of();
    }
    List<StaffRole> relations = staffRoleMapper.selectList(Wrappers.lambdaQuery(StaffRole.class)
        .eq(StaffRole::getIsDeleted, 0)
        .eq(orgId != null, StaffRole::getOrgId, orgId)
        .in(StaffRole::getStaffId, staffIds));
    if (relations.isEmpty()) {
      return Map.of();
    }
    List<Long> roleIds = relations.stream().map(StaffRole::getRoleId).distinct().toList();
    Map<Long, String> roleCodeById = roleMapper.selectList(Wrappers.lambdaQuery(Role.class)
            .eq(Role::getIsDeleted, 0)
            .in(Role::getId, roleIds)).stream()
        .collect(Collectors.toMap(Role::getId, r -> r.getRoleCode() == null ? "" : r.getRoleCode(), (a, b) -> a));
    Map<Long, List<String>> result = new HashMap<>();
    for (StaffRole relation : relations) {
      String code = roleCodeById.get(relation.getRoleId());
      if (code == null || code.isBlank()) {
        continue;
      }
      result.computeIfAbsent(relation.getStaffId(), k -> new ArrayList<>()).add(code);
    }
    return result;
  }

  private Map<String, ShiftTemplate> loadShiftTemplates(Long orgId, List<String> shiftCodes) {
    List<ShiftTemplate> templates = shiftTemplateMapper.selectList(Wrappers.lambdaQuery(ShiftTemplate.class)
        .eq(ShiftTemplate::getIsDeleted, 0)
        .eq(orgId != null, ShiftTemplate::getOrgId, orgId)
        .eq(ShiftTemplate::getEnabled, 1)
        .in(shiftCodes != null && !shiftCodes.isEmpty(), ShiftTemplate::getShiftCode, shiftCodes)
        .orderByAsc(ShiftTemplate::getRuleSort)
        .orderByAsc(ShiftTemplate::getId));
    Map<String, ShiftTemplate> result = new LinkedHashMap<>();
    for (ShiftTemplate template : templates) {
      if (template.getShiftCode() != null && !template.getShiftCode().isBlank()) {
        result.putIfAbsent(template.getShiftCode(), template);
      }
    }
    return result;
  }

  private Set<String> loadLeaveBlocked(Long orgId, List<Long> staffIds, LocalDate from, LocalDate to) {
    Set<String> blocked = new HashSet<>();
    List<OaApproval> leaves = approvalMapper.selectList(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .in(OaApproval::getApplicantId, staffIds)
        .eq(OaApproval::getApprovalType, "LEAVE")
        .eq(OaApproval::getStatus, "APPROVED")
        .isNotNull(OaApproval::getStartTime)
        .isNotNull(OaApproval::getEndTime)
        .le(OaApproval::getStartTime, to.plusDays(1).atStartOfDay())
        .ge(OaApproval::getEndTime, from.atStartOfDay()));
    for (OaApproval leave : leaves) {
      LocalDate start = leave.getStartTime().toLocalDate();
      LocalDate end = leave.getEndTime().toLocalDate();
      for (LocalDate d = start.isBefore(from) ? from : start; !d.isAfter(end) && !d.isAfter(to); d = d.plusDays(1)) {
        blocked.add(leave.getApplicantId() + "@" + d);
      }
    }
    return blocked;
  }

  private Set<String> loadExistingScheduleBlocked(Long orgId, List<Long> staffIds, LocalDate from, LocalDate to) {
    Set<String> blocked = new HashSet<>();
    List<StaffSchedule> schedules = staffScheduleMapper.selectList(Wrappers.lambdaQuery(StaffSchedule.class)
        .eq(StaffSchedule::getIsDeleted, 0)
        .eq(orgId != null, StaffSchedule::getOrgId, orgId)
        .in(StaffSchedule::getStaffId, staffIds)
        .ge(StaffSchedule::getDutyDate, from)
        .le(StaffSchedule::getDutyDate, to)
        .eq(StaffSchedule::getStatus, 1));
    for (StaffSchedule schedule : schedules) {
      blocked.add(schedule.getStaffId() + "@" + schedule.getDutyDate());
    }
    return blocked;
  }

  private SolverConstraint toSolverConstraint(AiScheduleConstraint config) {
    SolverConstraint constraint = new SolverConstraint();
    if (config.getMaxWeeklyHours() != null) {
      constraint.setMaxWeeklyHours(config.getMaxWeeklyHours().doubleValue());
    }
    if (config.getMaxConsecutiveDays() != null) {
      constraint.setMaxConsecutiveDays(config.getMaxConsecutiveDays());
    }
    constraint.setNightRestEnabled(config.getNightRestEnabled() == null || config.getNightRestEnabled() == 1);
    if (config.getWorkloadBalanceWeight() != null) {
      constraint.setWorkloadBalanceWeight(config.getWorkloadBalanceWeight().doubleValue());
    }
    if (config.getNightFairnessWeight() != null) {
      constraint.setNightFairnessWeight(config.getNightFairnessWeight().doubleValue());
    }
    return constraint;
  }

  private Map<String, List<String>> parseQualification(String json) {
    if (json == null || json.isBlank()) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<Map<String, List<String>>>() {});
    } catch (Exception ex) {
      return Map.of();
    }
  }

  private boolean isNightShift(ShiftTemplate template) {
    if (template.getCrossDay() != null && template.getCrossDay() == 1) {
      return true;
    }
    if (template.getStartTime() != null && !template.getStartTime().isBefore(LocalTime.of(20, 0))) {
      return true;
    }
    String text = (template.getShiftCode() == null ? "" : template.getShiftCode())
        + (template.getName() == null ? "" : template.getName());
    return text.toUpperCase(Locale.ROOT).contains("NIGHT") || text.contains("夜");
  }

  private LocalDateTime buildStartTime(LocalDate date, ShiftTemplate template) {
    if (template == null || template.getStartTime() == null) {
      return null;
    }
    return LocalDateTime.of(date, template.getStartTime());
  }

  private LocalDateTime buildEndTime(LocalDate date, ShiftTemplate template) {
    if (template == null || template.getEndTime() == null) {
      return null;
    }
    LocalDate endDate = date;
    if ((template.getCrossDay() != null && template.getCrossDay() == 1)
        || (template.getStartTime() != null && template.getEndTime().isBefore(template.getStartTime()))) {
      endDate = date.plusDays(1);
    }
    return LocalDateTime.of(endDate, template.getEndTime());
  }

  private Long findExistingScheduleId(Long orgId, AiScheduleProposalItem item) {
    StaffSchedule existing = staffScheduleMapper.selectOne(Wrappers.lambdaQuery(StaffSchedule.class)
        .eq(StaffSchedule::getIsDeleted, 0)
        .eq(orgId != null, StaffSchedule::getOrgId, orgId)
        .eq(StaffSchedule::getStaffId, item.getStaffId())
        .eq(StaffSchedule::getDutyDate, item.getDutyDate())
        .eq(StaffSchedule::getShiftCode, item.getShiftCode())
        .orderByDesc(StaffSchedule::getId)
        .last("LIMIT 1"));
    return existing == null ? null : existing.getId();
  }

  /** 人工调整后的即时校验：同人同日重复班次提示（软提示，不阻断）。 */
  private String checkItemViolation(Long orgId, AiScheduleProposalItem item) {
    Long count = itemMapper.selectCount(Wrappers.lambdaQuery(AiScheduleProposalItem.class)
        .eq(AiScheduleProposalItem::getIsDeleted, 0)
        .eq(AiScheduleProposalItem::getProposalId, item.getProposalId())
        .eq(AiScheduleProposalItem::getStaffId, item.getStaffId())
        .eq(AiScheduleProposalItem::getDutyDate, item.getDutyDate())
        .ne(item.getId() != null, AiScheduleProposalItem::getId, item.getId()));
    if (count != null && count > 0) {
      return "该员工当日已有其他班次";
    }
    StaffSchedule existing = staffScheduleMapper.selectOne(Wrappers.lambdaQuery(StaffSchedule.class)
        .eq(StaffSchedule::getIsDeleted, 0)
        .eq(orgId != null, StaffSchedule::getOrgId, orgId)
        .eq(StaffSchedule::getStaffId, item.getStaffId())
        .eq(StaffSchedule::getDutyDate, item.getDutyDate())
        .eq(StaffSchedule::getStatus, 1)
        .last("LIMIT 1"));
    if (existing != null) {
      return "该员工当日在正式排班中已有班次";
    }
    return null;
  }

  private Map<String, Object> buildScoreSummary(SolverResult result) {
    Map<String, Object> summary = new LinkedHashMap<>();
    summary.put("softScore", result.getSoftScore());
    summary.put("assignmentCount", result.getAssignments().size());
    summary.put("unfilledSlots", result.getUnfilledSlots());
    List<Double> hours = new ArrayList<>(result.getHoursByStaff().values());
    summary.put("maxHours", hours.stream().mapToDouble(Double::doubleValue).max().orElse(0));
    summary.put("minHours", hours.stream().mapToDouble(Double::doubleValue).min().orElse(0));
    summary.put("avgHours", Math.round(hours.stream().mapToDouble(Double::doubleValue).average().orElse(0) * 10) / 10.0);
    summary.put("maxNights", result.getNightsByStaff().values().stream().mapToInt(Integer::intValue).max().orElse(0));
    summary.put("minNights", result.getNightsByStaff().values().stream().mapToInt(Integer::intValue).min().orElse(0));
    return summary;
  }

  private AiScheduleProposalResponse toResponse(AiScheduleProposal proposal, boolean withItems) {
    AiScheduleProposalResponse response = new AiScheduleProposalResponse();
    response.setId(proposal.getId());
    response.setTitle(proposal.getTitle());
    response.setDeptId(proposal.getDeptId());
    response.setDeptName(proposal.getDeptName());
    response.setDateFrom(proposal.getDateFrom());
    response.setDateTo(proposal.getDateTo());
    response.setShiftCodes(proposal.getShiftCodes());
    response.setStatus(proposal.getStatus());
    response.setScoreJson(proposal.getScoreJson());
    response.setUnfilledCount(proposal.getUnfilledCount());
    response.setUnfilledSlots(readUnfilledSlots(proposal.getScoreJson()));
    response.setGeneratedAt(proposal.getGeneratedAt());
    response.setAdoptedAt(proposal.getAdoptedAt());
    response.setRemark(proposal.getRemark());
    response.setCreateTime(proposal.getCreateTime());
    if (withItems) {
      List<AiScheduleProposalItem> items = itemMapper.selectList(
          Wrappers.lambdaQuery(AiScheduleProposalItem.class)
              .eq(AiScheduleProposalItem::getIsDeleted, 0)
              .eq(AiScheduleProposalItem::getProposalId, proposal.getId())
              .orderByAsc(AiScheduleProposalItem::getDutyDate)
              .orderByAsc(AiScheduleProposalItem::getShiftCode)
              .orderByAsc(AiScheduleProposalItem::getStaffId));
      response.setItems(items.stream().map(this::toItemResponse).toList());
    }
    return response;
  }

  private List<String> readUnfilledSlots(String scoreJson) {
    if (scoreJson == null || scoreJson.isBlank()) {
      return List.of();
    }
    try {
      Map<String, Object> map = objectMapper.readValue(scoreJson, new TypeReference<Map<String, Object>>() {});
      Object slots = map.get("unfilledSlots");
      if (slots instanceof List<?> list) {
        return list.stream().map(String::valueOf).toList();
      }
    } catch (Exception ignored) {
      // scoreJson 解析失败时忽略缺口明细
    }
    return List.of();
  }

  private AiScheduleProposalItemResponse toItemResponse(AiScheduleProposalItem item) {
    AiScheduleProposalItemResponse response = new AiScheduleProposalItemResponse();
    response.setId(item.getId());
    response.setProposalId(item.getProposalId());
    response.setStaffId(item.getStaffId());
    response.setStaffName(item.getStaffName());
    response.setDutyDate(item.getDutyDate());
    response.setShiftCode(item.getShiftCode());
    response.setStartTime(item.getStartTime());
    response.setEndTime(item.getEndTime());
    response.setNightShift(item.getNightShift());
    response.setManualAdjusted(item.getManualAdjusted());
    response.setViolationNote(item.getViolationNote());
    response.setScheduleId(item.getScheduleId());
    return response;
  }

  private String writeJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception ex) {
      return "{}";
    }
  }
}
