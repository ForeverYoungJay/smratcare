package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaActivityPlan;
import com.zhiyangyun.care.oa.mapper.OaActivityPlanMapper;
import com.zhiyangyun.care.oa.model.OaActivityPlanApprovalRequest;
import com.zhiyangyun.care.oa.model.OaActivityPlanExecutionRequest;
import com.zhiyangyun.care.oa.model.OaActivityPlanRequest;
import com.zhiyangyun.care.oa.model.OaActivityPlanReviewRequest;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/oa/activity-plan")
public class OaActivityPlanController {
  private static final String STATUS_DRAFT = "DRAFT";
  private static final String STATUS_PLAN_PENDING = "PLAN_PENDING";
  private static final String STATUS_APPROVING = "APPROVING";
  private static final String STATUS_APPROVED = "APPROVED";
  private static final String STATUS_EXECUTING = "EXECUTING";
  private static final String STATUS_COMPLETED = "COMPLETED";
  private static final String STATUS_REJECTED = "REJECTED";
  private static final String STATUS_CANCELLED = "CANCELLED";

  private static final String STEP_PENDING = "PENDING";
  private static final String STEP_WAITING = "WAITING";
  private static final String STEP_APPROVED = "APPROVED";
  private static final String STEP_REJECTED = "REJECTED";

  private static final String ROLE_NURSE_MANAGER = "NURSE_MANAGER";
  private static final String ROLE_FINANCE = "FINANCE";
  private static final String ROLE_DEAN = "DEAN";

  private final OaActivityPlanMapper activityPlanMapper;
  private final StaffMapper staffMapper;
  private final ObjectMapper objectMapper;

  public OaActivityPlanController(
      OaActivityPlanMapper activityPlanMapper,
      StaffMapper staffMapper,
      ObjectMapper objectMapper) {
    this.activityPlanMapper = activityPlanMapper;
    this.staffMapper = staffMapper;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaActivityPlan>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String organizer,
      @RequestParam(required = false) String location,
      @RequestParam(required = false) String participantTarget,
      @RequestParam(required = false) String activityType) {
    if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
      throw new IllegalArgumentException("开始日期不能晚于结束日期");
    }
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaActivityPlan.class)
        .eq(OaActivityPlan::getIsDeleted, 0)
        .eq(orgId != null, OaActivityPlan::getOrgId, orgId)
        .eq(normalizedStatus != null, OaActivityPlan::getStatus, normalizedStatus)
        .ge(dateFrom != null, OaActivityPlan::getPlanDate, dateFrom)
        .le(dateTo != null, OaActivityPlan::getPlanDate, dateTo)
        .eq(activityType != null && !activityType.isBlank(), OaActivityPlan::getActivityType, activityType)
        .like(organizer != null && !organizer.isBlank(), OaActivityPlan::getOrganizer, organizer)
        .like(location != null && !location.isBlank(), OaActivityPlan::getLocation, location)
        .like(participantTarget != null && !participantTarget.isBlank(), OaActivityPlan::getParticipantTarget, participantTarget);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaActivityPlan::getTitle, keyword)
          .or().like(OaActivityPlan::getOrganizer, keyword)
          .or().like(OaActivityPlan::getLocation, keyword)
          .or().like(OaActivityPlan::getActivityType, keyword));
    }
    wrapper.orderByDesc(OaActivityPlan::getPlanDate).orderByDesc(OaActivityPlan::getCreateTime);
    return Result.ok(activityPlanMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/{id}")
  public Result<OaActivityPlan> get(@PathVariable Long id) {
    return Result.ok(findAccessiblePlan(id));
  }

  @PostMapping
  public Result<OaActivityPlan> create(@Valid @RequestBody OaActivityPlanRequest request) {
    Long orgId = AuthContext.getOrgId();
    validateBaseRequest(request);
    OaActivityPlan plan = new OaActivityPlan();
    plan.setTenantId(orgId);
    plan.setOrgId(orgId);
    applyBaseFields(plan, request);
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null && !STATUS_DRAFT.equals(normalizedStatus) && !STATUS_PLAN_PENDING.equals(normalizedStatus)) {
      throw new IllegalArgumentException("创建活动仅支持草稿或待提交方案状态");
    }
    plan.setStatus(normalizedStatus == null ? STATUS_PLAN_PENDING : normalizedStatus);
    plan.setCreatedBy(AuthContext.getStaffId());
    clearApprovalState(plan);
    activityPlanMapper.insert(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}")
  public Result<OaActivityPlan> update(@PathVariable Long id, @Valid @RequestBody OaActivityPlanRequest request) {
    OaActivityPlan plan = requirePlan(id);
    ensureCanEdit(plan);
    validateBaseRequest(request);
    if (STATUS_APPROVING.equals(plan.getStatus())
        || STATUS_APPROVED.equals(plan.getStatus())
        || STATUS_EXECUTING.equals(plan.getStatus())
        || STATUS_COMPLETED.equals(plan.getStatus())) {
      throw new IllegalArgumentException("当前状态下不可编辑基础信息");
    }
    applyBaseFields(plan, request);
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null) {
      if (!STATUS_DRAFT.equals(normalizedStatus)
          && !STATUS_PLAN_PENDING.equals(normalizedStatus)
          && !STATUS_REJECTED.equals(normalizedStatus)) {
        throw new IllegalArgumentException("编辑仅支持保存为草稿、待提交方案或已驳回状态");
      }
      plan.setStatus(normalizedStatus);
    }
    clearApprovalState(plan);
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}/submit-scheme")
  public Result<OaActivityPlan> submitScheme(@PathVariable Long id) {
    OaActivityPlan plan = requirePlan(id);
    ensureCanEdit(plan);
    if (!STATUS_DRAFT.equals(plan.getStatus())
        && !STATUS_PLAN_PENDING.equals(plan.getStatus())
        && !STATUS_REJECTED.equals(plan.getStatus())) {
      throw new IllegalArgumentException("仅草稿、待提交方案或已驳回状态支持重新提交");
    }
    validateScheme(plan);
    List<Map<String, Object>> steps = buildApprovalSteps(plan);
    if (steps.isEmpty()) {
      throw new IllegalArgumentException("未生成审批流程，请检查配置");
    }
    plan.setApprovalLogsJson(writeJson(steps));
    plan.setCurrentApprovalStep(1);
    plan.setStatus(STATUS_APPROVING);
    applyCurrentApprover(plan, steps.get(0));
    plan.setSubmittedAt(LocalDateTime.now());
    plan.setApprovedAt(null);
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}/approve")
  public Result<OaActivityPlan> approve(
      @PathVariable Long id,
      @RequestBody(required = false) OaActivityPlanApprovalRequest request) {
    OaActivityPlan plan = requirePlan(id);
    if (!STATUS_APPROVING.equals(plan.getStatus())) {
      throw new IllegalArgumentException("当前计划不在审批中");
    }
    List<Map<String, Object>> steps = readApprovalSteps(plan.getApprovalLogsJson());
    Map<String, Object> currentStep = findCurrentPendingStep(steps, plan.getCurrentApprovalStep());
    if (currentStep == null) {
      throw new IllegalArgumentException("未找到当前审批步骤");
    }
    ensureCanApprove(currentStep);
    LocalDateTime now = LocalDateTime.now();
    currentStep.put("status", STEP_APPROVED);
    currentStep.put("actionTime", formatDateTime(now));
    currentStep.put("opinion", request == null ? null : trimToNull(request.getOpinion()));
    currentStep.put("approverId", AuthContext.getStaffId());
    currentStep.put("approverName", currentOperatorName());
    Map<String, Object> nextStep = findNextWaitingStep(steps, currentStep);
    if (nextStep == null) {
      plan.setStatus(STATUS_APPROVED);
      plan.setApprovedAt(now);
      plan.setCurrentApprovalStep(null);
      plan.setCurrentApproverRole(null);
      plan.setCurrentApproverId(null);
      plan.setCurrentApproverName(null);
    } else {
      nextStep.put("status", STEP_PENDING);
      plan.setCurrentApprovalStep(parseInteger(nextStep.get("stepNo")));
      applyCurrentApprover(plan, nextStep);
    }
    plan.setApprovalLogsJson(writeJson(steps));
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}/reject")
  public Result<OaActivityPlan> reject(
      @PathVariable Long id,
      @RequestBody(required = false) OaActivityPlanApprovalRequest request) {
    OaActivityPlan plan = requirePlan(id);
    if (!STATUS_APPROVING.equals(plan.getStatus())) {
      throw new IllegalArgumentException("当前计划不在审批中");
    }
    List<Map<String, Object>> steps = readApprovalSteps(plan.getApprovalLogsJson());
    Map<String, Object> currentStep = findCurrentPendingStep(steps, plan.getCurrentApprovalStep());
    if (currentStep == null) {
      throw new IllegalArgumentException("未找到当前审批步骤");
    }
    ensureCanApprove(currentStep);
    currentStep.put("status", STEP_REJECTED);
    currentStep.put("actionTime", formatDateTime(LocalDateTime.now()));
    currentStep.put("opinion", request == null ? null : trimToNull(request.getOpinion()));
    currentStep.put("approverId", AuthContext.getStaffId());
    currentStep.put("approverName", currentOperatorName());
    plan.setStatus(STATUS_REJECTED);
    plan.setCurrentApprovalStep(null);
    plan.setCurrentApproverRole(null);
    plan.setCurrentApproverId(null);
    plan.setCurrentApproverName(null);
    plan.setApprovalLogsJson(writeJson(steps));
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}/execution")
  public Result<OaActivityPlan> saveExecution(
      @PathVariable Long id,
      @Valid @RequestBody OaActivityPlanExecutionRequest request) {
    OaActivityPlan plan = requirePlan(id);
    ensureCanManageLifecycle(plan);
    if (!STATUS_APPROVED.equals(plan.getStatus())
        && !STATUS_EXECUTING.equals(plan.getStatus())
        && !STATUS_COMPLETED.equals(plan.getStatus())) {
      throw new IllegalArgumentException("当前状态不可维护执行信息");
    }
    plan.setActualCount(request.getActualCount());
    plan.setSignInRecordsJson(writeJson(request.getSignInRecords() == null ? List.of() : request.getSignInRecords()));
    plan.setPhotoUrlsJson(writeJson(request.getPhotoUrls() == null ? List.of() : request.getPhotoUrls()));
    plan.setExecutionRemark(trimToNull(request.getExecutionRemark()));
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}/review")
  public Result<OaActivityPlan> saveReview(
      @PathVariable Long id,
      @Valid @RequestBody OaActivityPlanReviewRequest request) {
    OaActivityPlan plan = requirePlan(id);
    ensureCanManageLifecycle(plan);
    if (!STATUS_EXECUTING.equals(plan.getStatus()) && !STATUS_COMPLETED.equals(plan.getStatus())) {
      throw new IllegalArgumentException("仅执行中或已完成活动支持复盘归档");
    }
    applyReviewFields(plan, request);
    if (plan.getReportContent() == null || plan.getReportContent().isBlank()) {
      plan.setReportContent(generateActivityReport(plan));
    }
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}/start")
  public Result<OaActivityPlan> start(@PathVariable Long id) {
    OaActivityPlan plan = requirePlan(id);
    ensureCanManageLifecycle(plan);
    if (!STATUS_APPROVED.equals(plan.getStatus())) {
      throw new IllegalArgumentException("仅已批准活动可开始执行");
    }
    plan.setStatus(STATUS_EXECUTING);
    plan.setStartedAt(LocalDateTime.now());
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}/done")
  public Result<OaActivityPlan> done(@PathVariable Long id) {
    OaActivityPlan plan = requirePlan(id);
    ensureCanManageLifecycle(plan);
    if (!STATUS_EXECUTING.equals(plan.getStatus())) {
      throw new IllegalArgumentException("仅执行中活动可完成");
    }
    plan.setStatus(STATUS_COMPLETED);
    plan.setCompletedAt(LocalDateTime.now());
    if (plan.getReportContent() == null || plan.getReportContent().isBlank()) {
      plan.setReportContent(generateActivityReport(plan));
    }
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @PutMapping("/{id}/cancel")
  public Result<OaActivityPlan> cancel(@PathVariable Long id) {
    OaActivityPlan plan = requirePlan(id);
    ensureCanManageLifecycle(plan);
    if (STATUS_COMPLETED.equals(plan.getStatus())) {
      throw new IllegalArgumentException("已完成活动不可取消");
    }
    plan.setStatus(STATUS_CANCELLED);
    activityPlanMapper.updateById(plan);
    return Result.ok(plan);
  }

  @PutMapping("/batch/start")
  public Result<Integer> batchStart(@RequestBody OaBatchIdsRequest request) {
    return batchStatusUpdate(request, STATUS_EXECUTING);
  }

  @PutMapping("/batch/done")
  public Result<Integer> batchDone(@RequestBody OaBatchIdsRequest request) {
    return batchStatusUpdate(request, STATUS_COMPLETED);
  }

  @PutMapping("/batch/cancel")
  public Result<Integer> batchCancel(@RequestBody OaBatchIdsRequest request) {
    return batchStatusUpdate(request, STATUS_CANCELLED);
  }

  @DeleteMapping("/batch")
  public Result<Integer> batchDelete(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaActivityPlan> list = activityPlanMapper.selectList(Wrappers.lambdaQuery(OaActivityPlan.class)
        .eq(OaActivityPlan::getIsDeleted, 0)
        .eq(orgId != null, OaActivityPlan::getOrgId, orgId)
        .in(OaActivityPlan::getId, ids));
    int count = 0;
    for (OaActivityPlan item : list) {
      if (!canDelete(item)) {
        continue;
      }
      item.setIsDeleted(1);
      activityPlanMapper.updateById(item);
      count++;
    }
    return Result.ok(count);
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String organizer,
      @RequestParam(required = false) String location,
      @RequestParam(required = false) String participantTarget) {
    if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
      throw new IllegalArgumentException("开始日期不能晚于结束日期");
    }
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaActivityPlan.class)
        .eq(OaActivityPlan::getIsDeleted, 0)
        .eq(orgId != null, OaActivityPlan::getOrgId, orgId)
        .eq(normalizedStatus != null, OaActivityPlan::getStatus, normalizedStatus)
        .ge(dateFrom != null, OaActivityPlan::getPlanDate, dateFrom)
        .le(dateTo != null, OaActivityPlan::getPlanDate, dateTo)
        .like(organizer != null && !organizer.isBlank(), OaActivityPlan::getOrganizer, organizer)
        .like(location != null && !location.isBlank(), OaActivityPlan::getLocation, location)
        .like(participantTarget != null && !participantTarget.isBlank(), OaActivityPlan::getParticipantTarget, participantTarget)
        .orderByDesc(OaActivityPlan::getPlanDate)
        .orderByDesc(OaActivityPlan::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaActivityPlan::getTitle, keyword)
          .or().like(OaActivityPlan::getOrganizer, keyword)
          .or().like(OaActivityPlan::getLocation, keyword)
          .or().like(OaActivityPlan::getActivityType, keyword));
    }
    List<OaActivityPlan> list = activityPlanMapper.selectList(wrapper);
    List<String> headers = List.of("ID", "活动名称", "活动类型", "计划日期", "地点", "组织人", "参与对象", "风险等级", "预算金额", "状态", "当前审批人");
    List<List<String>> rows = list.stream()
        .map(item -> List.of(
            safe(item.getId()),
            safe(item.getTitle()),
            safe(item.getActivityType()),
            safe(item.getPlanDate()),
            safe(item.getLocation()),
            safe(item.getOrganizer()),
            safe(item.getParticipantTarget()),
            safe(item.getRiskLevel()),
            safe(item.getBudgetAmount()),
            safe(item.getStatus()),
            safe(item.getCurrentApproverName())))
        .toList();
    return csvResponse("oa-activity-plan", headers, rows);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaActivityPlan plan = requirePlan(id);
    if (!canDelete(plan)) {
      throw new IllegalArgumentException("当前状态不可删除");
    }
    plan.setIsDeleted(1);
    activityPlanMapper.updateById(plan);
    return Result.ok(null);
  }

  private void applyBaseFields(OaActivityPlan plan, OaActivityPlanRequest request) {
    plan.setTitle(trimToNull(request.getTitle()));
    plan.setActivityType(trimToNull(request.getActivityType()));
    plan.setPlanDate(request.getPlanDate());
    plan.setStartTime(request.getStartTime());
    plan.setEndTime(request.getEndTime());
    plan.setLocation(trimToNull(request.getLocation()));
    plan.setOrganizer(trimToNull(request.getOrganizer()));
    plan.setParticipantTarget(trimToNull(request.getParticipantTarget()));
    plan.setParticipantTags(trimToNull(request.getParticipantTags()));
    plan.setParticipantElderIds(trimToNull(request.getParticipantElderIds()));
    plan.setEstimatedCount(request.getEstimatedCount());
    plan.setNeedMedicalStaff(Boolean.TRUE.equals(request.getNeedMedicalStaff()));
    plan.setRiskLevel(normalizeRiskLevel(request.getRiskLevel()));
    plan.setNeedBudget(Boolean.TRUE.equals(request.getNeedBudget()));
    plan.setBudgetAmount(request.getBudgetAmount());
    plan.setBudgetDescription(trimToNull(request.getBudgetDescription()));
    plan.setContent(trimToNull(request.getContent()));
    plan.setSchemeAttachmentName(trimToNull(request.getSchemeAttachmentName()));
    plan.setSchemeAttachmentUrl(trimToNull(request.getSchemeAttachmentUrl()));
    plan.setRemark(trimToNull(request.getRemark()));
  }

  private void applyReviewFields(OaActivityPlan plan, OaActivityPlanReviewRequest request) {
    plan.setActualExpense(request.getActualExpense());
    plan.setEffectEvaluation(normalizeEffectEvaluation(request.getEffectEvaluation()));
    plan.setElderFeedback(trimToNull(request.getElderFeedback()));
    plan.setRiskSituation(trimToNull(request.getRiskSituation()));
    plan.setReportContent(trimToNull(request.getReportContent()));
  }

  private void validateBaseRequest(OaActivityPlanRequest request) {
    validateDateRange(request.getStartTime(), request.getEndTime());
    if (request.getEstimatedCount() != null && request.getEstimatedCount() < 0) {
      throw new IllegalArgumentException("预估人数不能小于 0");
    }
    if (Boolean.TRUE.equals(request.getNeedBudget())) {
      if (request.getBudgetAmount() == null || request.getBudgetAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("需要费用时必须填写预算金额");
      }
    }
  }

  private void validateScheme(OaActivityPlan plan) {
    if ((plan.getContent() == null || plan.getContent().isBlank())
        && (plan.getSchemeAttachmentUrl() == null || plan.getSchemeAttachmentUrl().isBlank())) {
      throw new IllegalArgumentException("提交方案前请填写方案详情或上传方案附件");
    }
  }

  private void clearApprovalState(OaActivityPlan plan) {
    plan.setApprovalLogsJson(null);
    plan.setCurrentApprovalStep(null);
    plan.setCurrentApproverRole(null);
    plan.setCurrentApproverId(null);
    plan.setCurrentApproverName(null);
    plan.setSubmittedAt(null);
    plan.setApprovedAt(null);
  }

  private OaActivityPlan requirePlan(Long id) {
    OaActivityPlan plan = findAccessiblePlan(id);
    if (plan == null) {
      throw new IllegalArgumentException("活动计划不存在");
    }
    return plan;
  }

  private OaActivityPlan findAccessiblePlan(Long id) {
    Long orgId = AuthContext.getOrgId();
    return activityPlanMapper.selectOne(Wrappers.lambdaQuery(OaActivityPlan.class)
        .eq(OaActivityPlan::getId, id)
        .eq(OaActivityPlan::getIsDeleted, 0)
        .eq(orgId != null, OaActivityPlan::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private List<Map<String, Object>> buildApprovalSteps(OaActivityPlan plan) {
    List<Map<String, Object>> steps = new ArrayList<>();
    steps.add(createStep(1, ROLE_NURSE_MANAGER, "护士长审核", STEP_PENDING, plan.getOrgId()));
    if (Boolean.TRUE.equals(plan.getNeedBudget())) {
      steps.add(createStep(2, ROLE_FINANCE, "财务审批", STEP_WAITING, plan.getOrgId()));
      steps.add(createStep(3, ROLE_DEAN, "院长审批", STEP_WAITING, plan.getOrgId()));
    } else {
      steps.add(createStep(2, ROLE_DEAN, "院长审批", STEP_WAITING, plan.getOrgId()));
    }
    return steps;
  }

  private Map<String, Object> createStep(int stepNo, String roleCode, String label, String status, Long orgId) {
    LinkedHashMap<String, Object> step = new LinkedHashMap<>();
    step.put("stepNo", stepNo);
    step.put("roleCode", roleCode);
    step.put("label", label);
    step.put("status", status);
    StaffAccount assignee = findFirstActiveStaffByRole(orgId, roleCode);
    if (assignee != null) {
      step.put("expectedApproverId", assignee.getId());
      step.put("expectedApproverName", trimToNull(assignee.getRealName()) != null ? assignee.getRealName() : assignee.getUsername());
    } else {
      step.put("expectedApproverName", expectedApproverName(roleCode));
    }
    step.put("approverId", null);
    step.put("approverName", null);
    step.put("actionTime", null);
    step.put("opinion", null);
    return step;
  }

  private void applyCurrentApprover(OaActivityPlan plan, Map<String, Object> step) {
    plan.setCurrentApproverRole(parseString(step.get("roleCode")));
    plan.setCurrentApproverId(parseLong(step.get("expectedApproverId")));
    String expectedName = parseString(step.get("expectedApproverName"));
    plan.setCurrentApproverName(expectedName == null ? expectedApproverName(plan.getCurrentApproverRole()) : expectedName);
  }

  private Map<String, Object> findCurrentPendingStep(List<Map<String, Object>> steps, Integer currentApprovalStep) {
    if (steps == null || steps.isEmpty()) {
      return null;
    }
    for (Map<String, Object> item : steps) {
      Integer stepNo = parseInteger(item.get("stepNo"));
      if (Objects.equals(stepNo, currentApprovalStep) && STEP_PENDING.equals(parseString(item.get("status")))) {
        return item;
      }
    }
    return steps.stream()
        .filter(item -> STEP_PENDING.equals(parseString(item.get("status"))))
        .findFirst()
        .orElse(null);
  }

  private Map<String, Object> findNextWaitingStep(List<Map<String, Object>> steps, Map<String, Object> currentStep) {
    Integer currentNo = parseInteger(currentStep.get("stepNo"));
    for (Map<String, Object> item : steps) {
      Integer stepNo = parseInteger(item.get("stepNo"));
      if (stepNo != null && currentNo != null && stepNo > currentNo && STEP_WAITING.equals(parseString(item.get("status")))) {
        return item;
      }
    }
    return null;
  }

  private void ensureCanApprove(Map<String, Object> step) {
    String roleCode = parseString(step.get("roleCode"));
    Long expectedApproverId = parseLong(step.get("expectedApproverId"));
    Long currentStaffId = AuthContext.getStaffId();
    if (expectedApproverId != null && currentStaffId != null && expectedApproverId.equals(currentStaffId)) {
      return;
    }
    if (!canApproveByRole(roleCode)) {
      throw new IllegalArgumentException("当前账号无权审批该步骤");
    }
  }

  private boolean canApproveByRole(String roleCode) {
    if (AuthContext.isAdmin()) {
      return true;
    }
    String normalized = roleCode == null ? "" : roleCode.trim().toUpperCase(Locale.ROOT);
    if (ROLE_NURSE_MANAGER.equals(normalized)) {
      return AuthContext.hasRole("NURSING_MINISTER")
          || AuthContext.hasRole("NURSING_MANAGER")
          || AuthContext.hasRole("SUPERVISOR")
          || AuthContext.hasRole("MANAGER");
    }
    if (ROLE_FINANCE.equals(normalized)) {
      return AuthContext.hasRole("FINANCE_MINISTER")
          || AuthContext.hasRole("FINANCE_EMPLOYEE")
          || AuthContext.hasRole("ACCOUNTANT")
          || AuthContext.hasRole("CASHIER");
    }
    if (ROLE_DEAN.equals(normalized)) {
      return AuthContext.hasRole("DIRECTOR")
          || AuthContext.hasRole("DEAN")
          || AuthContext.hasRole("SYS_ADMIN")
          || AuthContext.hasRole("ADMIN");
    }
    return false;
  }

  private StaffAccount findFirstActiveStaffByRole(Long orgId, String roleCode) {
    if (orgId == null || roleCode == null || roleCode.isBlank()) {
      return null;
    }
    for (String candidate : resolveRoleCandidates(roleCode)) {
      List<StaffAccount> staffList = staffMapper.selectByRoleCode(orgId, candidate);
      if (staffList == null || staffList.isEmpty()) {
        continue;
      }
      for (StaffAccount account : staffList) {
        if (account == null || account.getIsDeleted() != null && account.getIsDeleted() != 0) {
          continue;
        }
        if (account.getStatus() != null && account.getStatus() == 0) {
          continue;
        }
        return account;
      }
    }
    return null;
  }

  private List<String> resolveRoleCandidates(String roleCode) {
    if (ROLE_NURSE_MANAGER.equals(roleCode)) {
      return List.of("NURSING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN");
    }
    if (ROLE_FINANCE.equals(roleCode)) {
      return List.of("FINANCE_MINISTER", "FINANCE_EMPLOYEE", "ACCOUNTANT", "CASHIER", "DIRECTOR", "ADMIN");
    }
    if (ROLE_DEAN.equals(roleCode)) {
      return List.of("DIRECTOR", "SYS_ADMIN", "ADMIN");
    }
    return List.of();
  }

  private String expectedApproverName(String roleCode) {
    if (ROLE_NURSE_MANAGER.equals(roleCode)) {
      return "护士长";
    }
    if (ROLE_FINANCE.equals(roleCode)) {
      return "财务审批人";
    }
    if (ROLE_DEAN.equals(roleCode)) {
      return "院长";
    }
    return "待分配";
  }

  private void ensureCanEdit(OaActivityPlan plan) {
    if (canManageLifecycle(plan)) {
      return;
    }
    Long staffId = AuthContext.getStaffId();
    if (staffId != null && staffId.equals(plan.getCreatedBy())) {
      return;
    }
    throw new IllegalArgumentException("当前账号无权编辑该活动计划");
  }

  private void ensureCanManageLifecycle(OaActivityPlan plan) {
    if (!canManageLifecycle(plan)) {
      throw new IllegalArgumentException("当前账号无权操作该活动计划");
    }
  }

  private boolean canManageLifecycle(OaActivityPlan plan) {
    if (AuthContext.isAdmin() || AuthContext.isMinisterOrHigher()) {
      return true;
    }
    Long staffId = AuthContext.getStaffId();
    return staffId != null && staffId.equals(plan.getCreatedBy());
  }

  private boolean canDelete(OaActivityPlan plan) {
    return canManageLifecycle(plan)
        && !STATUS_APPROVING.equals(plan.getStatus())
        && !STATUS_EXECUTING.equals(plan.getStatus())
        && !STATUS_COMPLETED.equals(plan.getStatus());
  }

  private Result<Integer> batchStatusUpdate(OaBatchIdsRequest request, String targetStatus) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaActivityPlan> list = activityPlanMapper.selectList(Wrappers.lambdaQuery(OaActivityPlan.class)
        .eq(OaActivityPlan::getIsDeleted, 0)
        .eq(orgId != null, OaActivityPlan::getOrgId, orgId)
        .in(OaActivityPlan::getId, ids));
    int count = 0;
    for (OaActivityPlan item : list) {
      if (!canManageLifecycle(item)) {
        continue;
      }
      try {
        ensureStatusTransition(item.getStatus(), targetStatus);
      } catch (IllegalArgumentException ignored) {
        continue;
      }
      item.setStatus(targetStatus);
      if (STATUS_EXECUTING.equals(targetStatus)) {
        item.setStartedAt(LocalDateTime.now());
      } else if (STATUS_COMPLETED.equals(targetStatus)) {
        item.setCompletedAt(LocalDateTime.now());
      }
      activityPlanMapper.updateById(item);
      count++;
    }
    return Result.ok(count);
  }

  private void validateDateRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
      throw new IllegalArgumentException("开始时间不能晚于结束时间");
    }
  }

  private void ensureStatusTransition(String fromStatus, String toStatus) {
    String from = normalizeStatus(fromStatus);
    String to = normalizeStatus(toStatus);
    if (to == null || from == null || to.equals(from)) {
      return;
    }
    if (STATUS_APPROVED.equals(from) && STATUS_EXECUTING.equals(to)) {
      return;
    }
    if (STATUS_EXECUTING.equals(from) && STATUS_COMPLETED.equals(to)) {
      return;
    }
    if (!STATUS_COMPLETED.equals(from) && STATUS_CANCELLED.equals(to)) {
      return;
    }
    throw new IllegalArgumentException("活动计划状态不允许从 " + from + " 变更为 " + to);
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase(Locale.ROOT);
    if ("PLANNED".equals(normalized)) return STATUS_PLAN_PENDING;
    if ("IN_PROGRESS".equals(normalized)) return STATUS_EXECUTING;
    if ("DONE".equals(normalized)) return STATUS_COMPLETED;
    if ("DRAFT".equals(normalized)
        || "PLAN_PENDING".equals(normalized)
        || "APPROVING".equals(normalized)
        || "APPROVED".equals(normalized)
        || "EXECUTING".equals(normalized)
        || "COMPLETED".equals(normalized)
        || "REJECTED".equals(normalized)
        || "CANCELLED".equals(normalized)) {
      return normalized;
    }
    throw new IllegalArgumentException("status 仅支持 DRAFT/PLAN_PENDING/APPROVING/APPROVED/EXECUTING/COMPLETED/REJECTED/CANCELLED");
  }

  private String normalizeRiskLevel(String riskLevel) {
    if (riskLevel == null || riskLevel.isBlank()) {
      return "LOW";
    }
    String normalized = riskLevel.trim().toUpperCase(Locale.ROOT);
    if (!"LOW".equals(normalized) && !"MEDIUM".equals(normalized) && !"HIGH".equals(normalized)) {
      throw new IllegalArgumentException("风险等级仅支持 LOW/MEDIUM/HIGH");
    }
    return normalized;
  }

  private String normalizeEffectEvaluation(String effectEvaluation) {
    if (effectEvaluation == null || effectEvaluation.isBlank()) {
      return null;
    }
    String normalized = effectEvaluation.trim().toUpperCase(Locale.ROOT);
    if (!"GOOD".equals(normalized) && !"NORMAL".equals(normalized) && !"BAD".equals(normalized)) {
      throw new IllegalArgumentException("效果评价仅支持 GOOD/NORMAL/BAD");
    }
    return normalized;
  }

  private List<Long> sanitizeIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return ids.stream().filter(id -> id != null && id > 0).distinct().collect(Collectors.toList());
  }

  private List<Map<String, Object>> readApprovalSteps(String json) {
    if (json == null || json.isBlank()) {
      return new ArrayList<>();
    }
    try {
      List<LinkedHashMap<String, Object>> parsed =
          objectMapper.readValue(json, new TypeReference<List<LinkedHashMap<String, Object>>>() {});
      return new ArrayList<>(parsed);
    } catch (Exception ex) {
      throw new IllegalArgumentException("审批流程数据格式错误");
    }
  }

  private String writeJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception ex) {
      throw new IllegalArgumentException("活动计划数据写入失败");
    }
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String currentOperatorName() {
    String username = trimToNull(AuthContext.getUsername());
    return username == null ? "当前审批人" : username;
  }

  private String safe(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private String formatDateTime(LocalDateTime value) {
    if (value == null) {
      return "";
    }
    return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  private String parseString(Object value) {
    if (value == null) {
      return null;
    }
    String text = String.valueOf(value).trim();
    return text.isEmpty() ? null : text;
  }

  private Integer parseInteger(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.intValue();
    }
    try {
      return Integer.parseInt(String.valueOf(value));
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private Long parseLong(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    try {
      return Long.parseLong(String.valueOf(value));
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private String generateActivityReport(OaActivityPlan plan) {
    StringBuilder builder = new StringBuilder();
    builder.append("活动名称：").append(safe(plan.getTitle())).append('\n');
    builder.append("活动类型：").append(safe(plan.getActivityType())).append('\n');
    builder.append("活动时间：").append(safe(plan.getPlanDate())).append(' ')
        .append(plan.getStartTime() == null ? "" : plan.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
        .append(" - ")
        .append(plan.getEndTime() == null ? "" : plan.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))).append('\n');
    builder.append("组织人：").append(safe(plan.getOrganizer())).append('\n');
    builder.append("地点：").append(safe(plan.getLocation())).append('\n');
    builder.append("实际人数：").append(safe(plan.getActualCount())).append('\n');
    builder.append("实际花费：").append(safe(plan.getActualExpense())).append('\n');
    builder.append("效果评价：").append(safe(plan.getEffectEvaluation())).append('\n');
    builder.append("老人反馈：").append(safe(plan.getElderFeedback())).append('\n');
    builder.append("风险情况：").append(safe(plan.getRiskSituation())).append('\n');
    builder.append("执行备注：").append(safe(plan.getExecutionRemark())).append('\n');
    return builder.toString();
  }

  private ResponseEntity<byte[]> csvResponse(String filenameBase, List<String> headers, List<List<String>> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append('\uFEFF');
    sb.append(toCsvLine(headers)).append('\n');
    for (List<String> row : rows) {
      sb.append(toCsvLine(row)).append('\n');
    }
    byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    String filename = filenameBase + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    HttpHeaders headersObj = new HttpHeaders();
    headersObj.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename + ".csv");
    headersObj.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
    headersObj.setContentLength(bytes.length);
    return ResponseEntity.ok().headers(headersObj).body(bytes);
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
}
