package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.hr.model.StaffPointsAdjustRequest;
import com.zhiyangyun.care.hr.service.StaffPointsService;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.oa.model.OaApprovalBatchRejectRequest;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.oa.model.OaApprovalSummaryResponse;
import com.zhiyangyun.care.oa.model.OaApprovalRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
@RequestMapping("/api/oa/approval")
public class OaApprovalController {
  private final OaApprovalMapper approvalMapper;
  private final StaffPointsService staffPointsService;
  private final ObjectMapper objectMapper;

  public OaApprovalController(
      OaApprovalMapper approvalMapper,
      StaffPointsService staffPointsService,
      ObjectMapper objectMapper) {
    this.approvalMapper = approvalMapper;
    this.staffPointsService = staffPointsService;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaApproval>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Long applicantId,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    String normalizedType = normalizeType(type);
    var wrapper = buildQuery(orgId, normalizedStatus, normalizedType, applicantId, keyword).orderByDesc(OaApproval::getCreateTime);
    return Result.ok(approvalMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/summary")
  public Result<OaApprovalSummaryResponse> summary(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Long applicantId,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    String normalizedType = normalizeType(type);
    LocalDateTime now = LocalDateTime.now();

    OaApprovalSummaryResponse response = new OaApprovalSummaryResponse();
    response.setTotalCount(count(approvalMapper.selectCount(buildQuery(orgId, normalizedStatus, normalizedType, applicantId, keyword))));
    response.setPendingCount(count(approvalMapper.selectCount(buildQuery(orgId, "PENDING", normalizedType, applicantId, keyword))));
    response.setApprovedCount(count(approvalMapper.selectCount(buildQuery(orgId, "APPROVED", normalizedType, applicantId, keyword))));
    response.setRejectedCount(count(approvalMapper.selectCount(buildQuery(orgId, "REJECTED", normalizedType, applicantId, keyword))));
    response.setTimeoutPendingCount(count(approvalMapper.selectCount(buildQuery(orgId, "PENDING", normalizedType, applicantId, keyword)
        .lt(OaApproval::getCreateTime, now.minusHours(48)))));
    response.setLeavePendingCount(count(approvalMapper.selectCount(buildQuery(orgId, "PENDING", "LEAVE", applicantId, keyword))));
    response.setReimbursePendingCount(count(approvalMapper.selectCount(buildQuery(orgId, "PENDING", "REIMBURSE", applicantId, keyword))));
    response.setPurchasePendingCount(count(approvalMapper.selectCount(buildQuery(orgId, "PENDING", "PURCHASE", applicantId, keyword))));
    response.setMarketingPlanPendingCount(count(approvalMapper.selectCount(buildQuery(orgId, "PENDING", "MARKETING_PLAN", applicantId, keyword))));
    return Result.ok(response);
  }

  @PostMapping
  public Result<OaApproval> create(@Valid @RequestBody OaApprovalRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long currentStaffId = AuthContext.getStaffId();
    boolean canApprove = canCurrentUserApprove();
    OaApproval approval = new OaApproval();
    approval.setTenantId(orgId);
    approval.setOrgId(orgId);
    validateDateRange(request.getStartTime(), request.getEndTime());
    approval.setApprovalType(normalizeType(request.getApprovalType()));
    approval.setTitle(request.getTitle());
    approval.setApplicantId(resolveApplicantId(canApprove, currentStaffId, request.getApplicantId()));
    approval.setApplicantName(request.getApplicantName());
    approval.setAmount(request.getAmount());
    approval.setStartTime(request.getStartTime());
    approval.setEndTime(request.getEndTime());
    approval.setFormData(enrichWorkflowData(request));
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null && !"PENDING".equals(normalizedStatus)) {
      throw new IllegalArgumentException("创建审批单仅支持 PENDING 状态");
    }
    approval.setStatus(normalizedStatus == null ? "PENDING" : normalizedStatus);
    approval.setRemark(request.getRemark());
    approval.setCreatedBy(currentStaffId);
    approvalMapper.insert(approval);
    return Result.ok(approval);
  }

  @PutMapping("/{id}")
  public Result<OaApproval> update(@PathVariable Long id, @Valid @RequestBody OaApprovalRequest request) {
    OaApproval approval = findAccessibleApproval(id);
    if (approval == null) {
      return Result.ok(null);
    }
    if (!"PENDING".equals(approval.getStatus())) {
      throw new IllegalArgumentException("仅待审批状态可编辑");
    }
    validateDateRange(request.getStartTime(), request.getEndTime());
    approval.setApprovalType(normalizeType(request.getApprovalType()));
    approval.setTitle(request.getTitle());
    approval.setApplicantId(resolveApplicantId(canCurrentUserApprove(), AuthContext.getStaffId(), request.getApplicantId()));
    approval.setApplicantName(request.getApplicantName());
    approval.setAmount(request.getAmount());
    approval.setStartTime(request.getStartTime());
    approval.setEndTime(request.getEndTime());
    approval.setFormData(enrichWorkflowData(request));
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null && !"PENDING".equals(normalizedStatus)) {
      throw new IllegalArgumentException("编辑审批单仅支持保持 PENDING 状态");
    }
    approval.setStatus("PENDING");
    approval.setRemark(request.getRemark());
    approvalMapper.updateById(approval);
    return Result.ok(approval);
  }

  @PutMapping("/{id}/approve")
  public Result<OaApproval> approve(@PathVariable Long id, @RequestParam(required = false) String remark) {
    ensureApprovePermission();
    OaApproval approval = findAccessibleApproval(id);
    if (approval == null) {
      return Result.ok(null);
    }
    ensurePending(approval);
    applyApprovalDecision(approval, "APPROVED", remark);
    approvalMapper.updateById(approval);
    handleAfterApproved(approval);
    return Result.ok(approval);
  }

  @PutMapping("/{id}/reject")
  public Result<OaApproval> reject(@PathVariable Long id, @RequestParam(required = false) String remark) {
    ensureApprovePermission();
    OaApproval approval = findAccessibleApproval(id);
    if (approval == null) {
      return Result.ok(null);
    }
    ensurePending(approval);
    applyApprovalDecision(approval, "REJECTED", remark);
    approvalMapper.updateById(approval);
    return Result.ok(approval);
  }

  @PutMapping("/batch/approve")
  public Result<Integer> batchApprove(@RequestBody OaBatchIdsRequest request) {
    ensureApprovePermission();
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaApproval> approvals = approvalMapper.selectList(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .in(OaApproval::getId, ids));
    int count = 0;
    for (OaApproval approval : approvals) {
      if (!"PENDING".equals(approval.getStatus())) {
        continue;
      }
      applyApprovalDecision(approval, "APPROVED", "批量审批通过");
      approvalMapper.updateById(approval);
      handleAfterApproved(approval);
      count++;
    }
    return Result.ok(count);
  }

  @PutMapping("/batch/reject")
  public Result<Integer> batchReject(@RequestBody OaApprovalBatchRejectRequest request) {
    ensureApprovePermission();
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    String remark = request == null ? null : request.getRemark();
    Long orgId = AuthContext.getOrgId();
    List<OaApproval> approvals = approvalMapper.selectList(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .in(OaApproval::getId, ids));
    int count = 0;
    for (OaApproval approval : approvals) {
      if (!"PENDING".equals(approval.getStatus())) {
        continue;
      }
      applyApprovalDecision(approval, "REJECTED", remark);
      approvalMapper.updateById(approval);
      count++;
    }
    return Result.ok(count);
  }

  @DeleteMapping("/batch")
  public Result<Integer> batchDelete(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaApproval> approvals = approvalMapper.selectList(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .in(OaApproval::getId, ids));
    for (OaApproval approval : approvals) {
      approval.setIsDeleted(1);
      approvalMapper.updateById(approval);
    }
    return Result.ok(approvals.size());
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Long applicantId,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    String normalizedType = normalizeType(type);
    var wrapper = buildQuery(orgId, normalizedStatus, normalizedType, applicantId, keyword)
        .orderByDesc(OaApproval::getCreateTime);
    List<OaApproval> approvals = approvalMapper.selectList(wrapper);
    List<String> headers = List.of("ID", "类型", "标题", "申请人", "金额", "状态", "开始时间", "结束时间", "备注");
    List<List<String>> rows = approvals.stream()
        .map(item -> List.of(
            safe(item.getId()),
            safe(item.getApprovalType()),
            safe(item.getTitle()),
            safe(item.getApplicantName()),
            safe(item.getAmount()),
            safe(item.getStatus()),
            formatDateTime(item.getStartTime()),
            formatDateTime(item.getEndTime()),
            safe(item.getRemark())))
        .toList();
    return csvResponse("oa-approval", headers, rows);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaApproval approval = findAccessibleApproval(id);
    if (approval != null) {
      approval.setIsDeleted(1);
      approvalMapper.updateById(approval);
    }
    return Result.ok(null);
  }

  private OaApproval findAccessibleApproval(Long id) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    boolean canApprove = canCurrentUserApprove();
    return approvalMapper.selectOne(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getId, id)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(!canApprove && staffId != null, OaApproval::getApplicantId, staffId)
        .last("LIMIT 1"));
  }

  private void ensurePending(OaApproval approval) {
    if (!"PENDING".equals(approval.getStatus())) {
      throw new IllegalArgumentException("仅待审批状态可执行审批操作");
    }
  }

  private void validateDateRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
      throw new IllegalArgumentException("开始时间不能晚于结束时间");
    }
  }

  private String normalizeType(String type) {
    if (type == null || type.isBlank()) {
      return null;
    }
    String normalized = type.trim().toUpperCase();
    if (!"LEAVE".equals(normalized)
        && !"REIMBURSE".equals(normalized)
        && !"PURCHASE".equals(normalized)
        && !"OVERTIME".equals(normalized)
        && !"INCOME_PROOF".equals(normalized)
        && !"BANK_CARD".equals(normalized)
        && !"POINTS_CASH".equals(normalized)
        && !"MATERIAL_APPLY".equals(normalized)
        && !"OFFICIAL_SEAL".equals(normalized)
        && !"MARKETING_PLAN".equals(normalized)) {
      throw new IllegalArgumentException(
          "approvalType 仅支持 LEAVE/OVERTIME/REIMBURSE/PURCHASE/INCOME_PROOF/BANK_CARD/POINTS_CASH/MATERIAL_APPLY/OFFICIAL_SEAL/MARKETING_PLAN");
    }
    return normalized;
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if (!"PENDING".equals(normalized) && !"APPROVED".equals(normalized) && !"REJECTED".equals(normalized)) {
      throw new IllegalArgumentException("status 仅支持 PENDING/APPROVED/REJECTED");
    }
    return normalized;
  }

  private List<Long> sanitizeIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return ids.stream().filter(id -> id != null && id > 0).distinct().collect(Collectors.toList());
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

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OaApproval> buildQuery(
      Long orgId, String normalizedStatus, String normalizedType, Long applicantId, String keyword) {
    Long staffId = AuthContext.getStaffId();
    boolean canApprove = canCurrentUserApprove();
    Long queryApplicantId = canApprove ? applicantId : staffId;
    var wrapper = Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(normalizedStatus != null, OaApproval::getStatus, normalizedStatus)
        .eq(normalizedType != null, OaApproval::getApprovalType, normalizedType)
        .eq(queryApplicantId != null, OaApproval::getApplicantId, queryApplicantId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaApproval::getTitle, keyword)
          .or().like(OaApproval::getApplicantName, keyword)
          .or().like(OaApproval::getRemark, keyword));
    }
    return wrapper;
  }

  private long count(Long value) {
    return value == null ? 0L : value;
  }

  private String enrichWorkflowData(OaApprovalRequest request) {
    Map<String, Object> map = readFormMap(request.getFormData());
    String type = normalizeType(request.getApprovalType());
    validateWorkflowRequest(type, request, map);
    map.put("workflowType", type);
    map.put("workflowVersion", "2026-Q1");
    map.put("submittedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    map.put("approvalPath", approvalPath(type, request));
    if ("LEAVE".equals(type) && request.getStartTime() != null && request.getEndTime() != null) {
      long leaveHours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();
      long leaveDays = (long) Math.ceil(Math.max(leaveHours, 0) / 24.0);
      map.put("leaveDays", leaveDays);
      map.put("needDeanApproval", leaveDays > 3);
      if (leaveDays > 3) {
        map.put("flowHint", "超过3天自动加院长审批，流程较长请提前规划");
      }
    }
    return writeFormMap(map);
  }

  private void validateWorkflowRequest(String type, OaApprovalRequest request, Map<String, Object> map) {
    if ("LEAVE".equals(type)) {
      if (request.getStartTime() == null || request.getEndTime() == null) {
        throw new IllegalArgumentException("请假审批需填写开始时间和结束时间");
      }
      if (!parseBoolean(map.get("policyAcknowledged"))) {
        throw new IllegalArgumentException("请先阅读并确认请假制度");
      }
      String leaveType = parseString(map.get("leaveType"));
      map.put("leaveType", leaveType == null ? "ANNUAL" : leaveType);
      return;
    }
    if ("POINTS_CASH".equals(type)) {
      Long staffId = parseLong(map.get("staffId"));
      if (staffId == null) {
        staffId = request.getApplicantId();
      }
      Integer redeemPoints = parseInteger(map.get("redeemPoints"));
      if (staffId == null || redeemPoints == null || redeemPoints <= 0 || redeemPoints % 300 != 0) {
        throw new IllegalArgumentException("积分兑现金需提供员工和300整数倍积分");
      }
      map.put("staffId", staffId);
      map.put("redeemPoints", redeemPoints);
      map.put("redeemCash", (redeemPoints / 300) * 10);
      map.put("exchangeRate", "300:10");
      return;
    }
    if ("PURCHASE".equals(type)) {
      String purchaseCategory = parseString(map.get("purchaseCategory"));
      map.put("purchaseCategory", purchaseCategory == null ? "FOOD" : purchaseCategory);
    } else if ("MATERIAL_APPLY".equals(type)) {
      String materialCategory = parseString(map.get("materialCategory"));
      map.put("materialCategory", materialCategory == null ? "CARE" : materialCategory);
    } else if ("INCOME_PROOF".equals(type) || "BANK_CARD".equals(type)) {
      String proofNo = parseString(map.get("proofNo"));
      if (proofNo == null) {
        proofNo = "PF-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
      }
      map.put("proofNo", proofNo);
    }
    if (request.getAmount() != null && request.getAmount().signum() < 0) {
      throw new IllegalArgumentException("审批金额不能为负数");
    }
  }

  private List<String> approvalPath(String type, OaApprovalRequest request) {
    if ("LEAVE".equals(type)) {
      boolean needDean = false;
      if (request.getStartTime() != null && request.getEndTime() != null) {
        long leaveHours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();
        long leaveDays = (long) Math.ceil(Math.max(leaveHours, 0) / 24.0);
        needDean = leaveDays > 3;
      }
      List<String> path = new ArrayList<>();
      path.add("员工发起");
      path.add("部门主管审批");
      path.add("人事确认");
      if (needDean) {
        path.add("院长审批");
      }
      path.add("归档");
      return path;
    }
    if ("REIMBURSE".equals(type) || "OVERTIME".equals(type)) {
      return List.of("申请人提交", "部门主管审批", "财务审核", "院长审批", "出纳付款", "归档");
    }
    if ("PURCHASE".equals(type)) {
      return List.of("申请人提交", "部门主管审批", "仓库确认库存", "财务预算确认", "院长审批", "采购执行", "入库验收");
    }
    if ("MATERIAL_APPLY".equals(type)) {
      return List.of("申请人提交", "部门主管审批", "仓库审核", "出库确认", "归档");
    }
    if ("INCOME_PROOF".equals(type) || "BANK_CARD".equals(type)) {
      return List.of("员工申请", "人事审核", "财务确认", "行政盖章", "归档");
    }
    if ("POINTS_CASH".equals(type)) {
      return List.of("员工申请", "院长审批", "积分扣减", "财务发放", "归档");
    }
    return List.of("申请人提交", "审批", "归档");
  }

  private void handleAfterApproved(OaApproval approval) {
    if (approval == null || approval.getApprovalType() == null) {
      return;
    }
    if ("LEAVE".equalsIgnoreCase(approval.getApprovalType())) {
      updateLeaveAnnualStats(approval);
    }
    if ("POINTS_CASH".equalsIgnoreCase(approval.getApprovalType())) {
      applyPointsCashRedeem(approval);
    }
  }

  private void updateLeaveAnnualStats(OaApproval approval) {
    if (approval.getApplicantId() == null) {
      return;
    }
    int targetYear = approval.getStartTime() == null ? LocalDateTime.now().getYear() : approval.getStartTime().getYear();
    LocalDateTime yearStart = LocalDateTime.of(targetYear, 1, 1, 0, 0, 0);
    LocalDateTime yearEnd = yearStart.plusYears(1);
    long annualCount = count(approvalMapper.selectCount(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(approval.getOrgId() != null, OaApproval::getOrgId, approval.getOrgId())
        .eq(OaApproval::getApprovalType, "LEAVE")
        .eq(OaApproval::getApplicantId, approval.getApplicantId())
        .eq(OaApproval::getStatus, "APPROVED")
        .ge(OaApproval::getStartTime, yearStart)
        .lt(OaApproval::getStartTime, yearEnd)));
    Map<String, Object> map = readFormMap(approval.getFormData());
    map.put("annualLeaveApprovedYear", targetYear);
    map.put("annualLeaveApprovedCount", annualCount);
    approval.setFormData(writeFormMap(map));
    approvalMapper.updateById(approval);
  }

  private void applyApprovalDecision(OaApproval approval, String status, String remark) {
    Map<String, Object> map = readFormMap(approval.getFormData());
    map.put("lastApprovalStatus", status);
    map.put("lastApprovalRemark", normalizeOpinion(remark));
    map.put("lastApprovalAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    map.put("lastApproverId", AuthContext.getStaffId());
    map.put("lastApproverRoles", AuthContext.getRoleCodes());
    approval.setFormData(writeFormMap(map));
    approval.setStatus(status);
    approval.setRemark(normalizeOpinion(remark));
  }

  private String normalizeOpinion(String remark) {
    if (remark == null) {
      return null;
    }
    String trimmed = remark.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private void ensureApprovePermission() {
    if (!canCurrentUserApprove()) {
      throw new AccessDeniedException("仅管理者或部门负责人可审批");
    }
  }

  private boolean canCurrentUserApprove() {
    return AuthContext.isMinisterOrHigher();
  }

  private Long resolveApplicantId(boolean canApprove, Long currentStaffId, Long requestApplicantId) {
    if (!canApprove) {
      return currentStaffId;
    }
    if (requestApplicantId != null) {
      return requestApplicantId;
    }
    return currentStaffId;
  }

  private void applyPointsCashRedeem(OaApproval approval) {
    Map<String, Object> map = readFormMap(approval.getFormData());
    Long staffId = parseLong(map.get("staffId"));
    Integer redeemPoints = parseInteger(map.get("redeemPoints"));
    if (staffId == null || redeemPoints == null || redeemPoints <= 0) {
      return;
    }
    StaffPointsAdjustRequest adjustRequest = new StaffPointsAdjustRequest();
    adjustRequest.setStaffId(staffId);
    adjustRequest.setChangeType("DEDUCT");
    adjustRequest.setChangePoints(redeemPoints);
    adjustRequest.setSourceType("POINTS_CASH");
    adjustRequest.setSourceId(approval.getId());
    adjustRequest.setRemark("积分兑现金审批通过，审批单#" + approval.getId());
    var adjusted = staffPointsService.adjust(approval.getOrgId(), AuthContext.getStaffId(), adjustRequest);
    if (adjusted == null) {
      throw new IllegalArgumentException("积分余额不足，无法完成兑现金审批");
    }
  }

  private Map<String, Object> readFormMap(String formData) {
    if (formData == null || formData.isBlank()) {
      return new java.util.LinkedHashMap<>();
    }
    try {
      return objectMapper.readValue(formData, new TypeReference<Map<String, Object>>() {});
    } catch (Exception ignore) {
      Map<String, Object> map = new java.util.LinkedHashMap<>();
      map.put("rawText", formData);
      return map;
    }
  }

  private String writeFormMap(Map<String, Object> map) {
    try {
      return objectMapper.writeValueAsString(map);
    } catch (Exception ignore) {
      return null;
    }
  }

  private Long parseLong(Object value) {
    if (value == null) return null;
    if (value instanceof Number number) {
      return number.longValue();
    }
    try {
      return Long.parseLong(String.valueOf(value));
    } catch (Exception ignore) {
      return null;
    }
  }

  private Integer parseInteger(Object value) {
    if (value == null) return null;
    if (value instanceof Number number) {
      return number.intValue();
    }
    try {
      return Integer.parseInt(String.valueOf(value));
    } catch (Exception ignore) {
      return null;
    }
  }

  private boolean parseBoolean(Object value) {
    if (value == null) return false;
    if (value instanceof Boolean bool) {
      return bool;
    }
    String text = String.valueOf(value).trim().toLowerCase();
    return "true".equals(text) || "1".equals(text) || "yes".equals(text) || "y".equals(text);
  }

  private String parseString(Object value) {
    if (value == null) return null;
    String text = String.valueOf(value).trim();
    return text.isEmpty() ? null : text;
  }
}
