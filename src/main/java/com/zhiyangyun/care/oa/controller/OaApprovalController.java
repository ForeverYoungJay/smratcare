package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Role;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.entity.StaffRole;
import com.zhiyangyun.care.auth.mapper.RoleMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.mapper.StaffRoleMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.hr.model.StaffPointsAdjustRequest;
import com.zhiyangyun.care.hr.service.StaffPointsService;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.oa.model.OaApprovalBatchRejectRequest;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.oa.model.OaApprovalSummaryResponse;
import com.zhiyangyun.care.oa.model.OaApprovalRequest;
import com.zhiyangyun.care.schedule.mapper.ShiftSwapRequestMapper;
import com.zhiyangyun.care.schedule.service.ScheduleService;
import com.zhiyangyun.care.store.entity.InventoryOutboundSheet;
import com.zhiyangyun.care.store.entity.InventoryOutboundSheetItem;
import com.zhiyangyun.care.store.entity.MaterialPurchaseOrder;
import com.zhiyangyun.care.store.mapper.InventoryOutboundSheetItemMapper;
import com.zhiyangyun.care.store.mapper.InventoryOutboundSheetMapper;
import com.zhiyangyun.care.store.mapper.MaterialPurchaseOrderMapper;
import com.zhiyangyun.care.store.model.InventoryOutboundRequest;
import com.zhiyangyun.care.store.service.InventoryService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
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
  private final OaTaskMapper taskMapper;
  private final OaTodoMapper todoMapper;
  private final StaffMapper staffMapper;
  private final RoleMapper roleMapper;
  private final StaffRoleMapper staffRoleMapper;
  private final StaffPointsService staffPointsService;
  private final MaterialPurchaseOrderMapper materialPurchaseOrderMapper;
  private final InventoryOutboundSheetMapper inventoryOutboundSheetMapper;
  private final InventoryOutboundSheetItemMapper inventoryOutboundSheetItemMapper;
  private final InventoryService inventoryService;
  private final AuditLogService auditLogService;
  private final ObjectMapper objectMapper;
  private final ScheduleService scheduleService;
  private final ShiftSwapRequestMapper shiftSwapRequestMapper;

  public OaApprovalController(
      OaApprovalMapper approvalMapper,
      OaTaskMapper taskMapper,
      OaTodoMapper todoMapper,
      StaffMapper staffMapper,
      RoleMapper roleMapper,
      StaffRoleMapper staffRoleMapper,
      StaffPointsService staffPointsService,
      MaterialPurchaseOrderMapper materialPurchaseOrderMapper,
      InventoryOutboundSheetMapper inventoryOutboundSheetMapper,
      InventoryOutboundSheetItemMapper inventoryOutboundSheetItemMapper,
      InventoryService inventoryService,
      AuditLogService auditLogService,
      ObjectMapper objectMapper,
      ScheduleService scheduleService,
      ShiftSwapRequestMapper shiftSwapRequestMapper) {
    this.approvalMapper = approvalMapper;
    this.taskMapper = taskMapper;
    this.todoMapper = todoMapper;
    this.staffMapper = staffMapper;
    this.roleMapper = roleMapper;
    this.staffRoleMapper = staffRoleMapper;
    this.staffPointsService = staffPointsService;
    this.materialPurchaseOrderMapper = materialPurchaseOrderMapper;
    this.inventoryOutboundSheetMapper = inventoryOutboundSheetMapper;
    this.inventoryOutboundSheetItemMapper = inventoryOutboundSheetItemMapper;
    this.inventoryService = inventoryService;
    this.auditLogService = auditLogService;
    this.objectMapper = objectMapper;
    this.scheduleService = scheduleService;
    this.shiftSwapRequestMapper = shiftSwapRequestMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaApproval>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String currentApproverRole,
      @RequestParam(required = false) Long applicantId,
      @RequestParam(required = false, defaultValue = "false") boolean pendingMine,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    String normalizedType = normalizeType(type);
    String normalizedApproverRole = normalizeApproverRole(currentApproverRole);
    var wrapper = buildQuery(orgId, normalizedStatus, normalizedType, normalizedApproverRole, applicantId, keyword, pendingMine)
        .orderByDesc(OaApproval::getCreateTime);
    return Result.ok(approvalMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/summary")
  public Result<OaApprovalSummaryResponse> summary(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String currentApproverRole,
      @RequestParam(required = false) Long applicantId,
      @RequestParam(required = false, defaultValue = "false") boolean pendingMine,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    String normalizedType = normalizeType(type);
    String normalizedApproverRole = normalizeApproverRole(currentApproverRole);
    LocalDateTime now = LocalDateTime.now();

    OaApprovalSummaryResponse response = new OaApprovalSummaryResponse();
    response.setTotalCount(count(approvalMapper.selectCount(buildQuery(orgId, normalizedStatus, normalizedType, normalizedApproverRole, applicantId, keyword, pendingMine))));
    response.setPendingCount(count(approvalMapper.selectCount(buildQuery(orgId, "PENDING", normalizedType, normalizedApproverRole, applicantId, keyword, pendingMine))));
    response.setApprovedCount(count(approvalMapper.selectCount(buildQuery(orgId, "APPROVED", normalizedType, normalizedApproverRole, applicantId, keyword, pendingMine))));
    response.setRejectedCount(count(approvalMapper.selectCount(buildQuery(orgId, "REJECTED", normalizedType, normalizedApproverRole, applicantId, keyword, pendingMine))));
    response.setTimeoutPendingCount(approvalMapper.selectList(buildQuery(orgId, "PENDING", normalizedType, normalizedApproverRole, applicantId, keyword, pendingMine)).stream()
        .filter(item -> isApprovalTimeout(item, now))
        .count());
    response.setLeavePendingCount(count(approvalMapper.selectCount(buildQuery(orgId, "PENDING", "LEAVE", normalizedApproverRole, applicantId, keyword, pendingMine))));
    response.setReimbursePendingCount(count(approvalMapper.selectCount(buildQuery(orgId, "PENDING", "REIMBURSE", normalizedApproverRole, applicantId, keyword, pendingMine))));
    response.setPurchasePendingCount(count(approvalMapper.selectCount(buildQuery(orgId, "PENDING", "PURCHASE", normalizedApproverRole, applicantId, keyword, pendingMine))));
    response.setMarketingPlanPendingCount(count(approvalMapper.selectCount(buildQuery(orgId, "PENDING", "MARKETING_PLAN", normalizedApproverRole, applicantId, keyword, pendingMine))));
    return Result.ok(response);
  }

  @PostMapping
  public Result<OaApproval> create(@Valid @RequestBody OaApprovalRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long currentStaffId = AuthContext.getStaffId();
    OaApproval approval = new OaApproval();
    approval.setTenantId(orgId);
    approval.setOrgId(orgId);
    validateDateRange(request.getStartTime(), request.getEndTime());
    approval.setApprovalType(normalizeType(request.getApprovalType()));
    approval.setTitle(request.getTitle());
    approval.setApplicantId(resolveApplicantId(currentStaffId, request.getApplicantId()));
    approval.setApplicantName(resolveApplicantName(request.getApplicantName()));
    approval.setAmount(request.getAmount());
    approval.setStartTime(request.getStartTime());
    approval.setEndTime(request.getEndTime());
    approval.setFormData(enrichWorkflowData(request, approval.getApplicantId()));
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null && !"PENDING".equals(normalizedStatus)) {
      throw new IllegalArgumentException("创建审批单仅支持 PENDING 状态");
    }
    approval.setStatus(normalizedStatus == null ? "PENDING" : normalizedStatus);
    approval.setRemark(request.getRemark());
    approval.setCreatedBy(currentStaffId);
    approvalMapper.insert(approval);
    syncApprovalTodo(approval);
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
    approval.setAmount(request.getAmount());
    approval.setStartTime(request.getStartTime());
    approval.setEndTime(request.getEndTime());
    approval.setFormData(enrichWorkflowData(request, approval.getApplicantId()));
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null && !"PENDING".equals(normalizedStatus)) {
      throw new IllegalArgumentException("编辑审批单仅支持保持 PENDING 状态");
    }
    approval.setStatus("PENDING");
    approval.setRemark(request.getRemark());
    approvalMapper.updateById(approval);
    syncApprovalTodo(approval);
    return Result.ok(approval);
  }

  @PutMapping("/{id}/approve")
  @Transactional
  public Result<OaApproval> approve(@PathVariable Long id, @RequestParam(required = false) String remark) {
    ensureApprovePermission();
    OaApproval approval = findAccessibleApproval(id);
    if (approval == null) {
      return Result.ok(null);
    }
    ensurePending(approval);
    ensureCurrentApproverPermission(approval);
    applyApprovalDecision(approval, "APPROVED", remark);
    approvalMapper.updateById(approval);
    syncApprovalTodo(approval);
    if ("APPROVED".equals(approval.getStatus())) {
      handleAfterApproved(approval);
    }
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
    ensureCurrentApproverPermission(approval);
    applyApprovalDecision(approval, "REJECTED", remark);
    approvalMapper.updateById(approval);
    syncApprovalTodo(approval);
    if ("REJECTED".equals(approval.getStatus())) {
      handleAfterRejected(approval);
    }
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
      if (!canCurrentUserApproveStep(approval)) {
        continue;
      }
      applyApprovalDecision(approval, "APPROVED", "批量审批通过");
      approvalMapper.updateById(approval);
      syncApprovalTodo(approval);
      if ("APPROVED".equals(approval.getStatus())) {
        handleAfterApproved(approval);
      }
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
      if (!canCurrentUserApproveStep(approval)) {
        continue;
      }
      applyApprovalDecision(approval, "REJECTED", remark);
      approvalMapper.updateById(approval);
      syncApprovalTodo(approval);
      if ("REJECTED".equals(approval.getStatus())) {
        handleAfterRejected(approval);
      }
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
      syncApprovalTodo(approval);
      handleAfterDeleted(approval);
    }
    return Result.ok(approvals.size());
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String currentApproverRole,
      @RequestParam(required = false) Long applicantId,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    String normalizedType = normalizeType(type);
    String normalizedApproverRole = normalizeApproverRole(currentApproverRole);
    var wrapper = buildQuery(orgId, normalizedStatus, normalizedType, normalizedApproverRole, applicantId, keyword, false)
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
      syncApprovalTodo(approval);
      handleAfterDeleted(approval);
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
        && !"SHIFT_CHANGE".equals(normalized)
        && !"MARKETING_PLAN".equals(normalized)) {
      throw new IllegalArgumentException(
          "approvalType 仅支持 LEAVE/OVERTIME/REIMBURSE/PURCHASE/INCOME_PROOF/BANK_CARD/POINTS_CASH/MATERIAL_APPLY/OFFICIAL_SEAL/SHIFT_CHANGE/MARKETING_PLAN");
    }
    return normalized;
  }

  private String normalizeApproverRole(String role) {
    if (role == null || role.isBlank()) {
      return null;
    }
    String normalized = role.trim().toUpperCase();
    if (!"DEPT_MANAGER".equals(normalized)
        && !"HR".equals(normalized)
        && !"FINANCE".equals(normalized)
        && !"DEAN".equals(normalized)
        && !"WAREHOUSE".equals(normalized)
        && !"ADMIN_OFFICE".equals(normalized)
        && !"PURCHASING".equals(normalized)
        && !"ARCHIVE".equals(normalized)) {
      throw new IllegalArgumentException("currentApproverRole 仅支持 DEPT_MANAGER/HR/FINANCE/DEAN/WAREHOUSE/ADMIN_OFFICE/PURCHASING/ARCHIVE");
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
      Long orgId,
      String normalizedStatus,
      String normalizedType,
      String normalizedApproverRole,
      Long applicantId,
      String keyword,
      boolean pendingMine) {
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
      Long keywordId = parseLong(keyword);
      wrapper.and(w -> {
        if (keywordId != null) {
          w.eq(OaApproval::getId, keywordId).or();
        }
        w.like(OaApproval::getTitle, keyword)
            .or().like(OaApproval::getApplicantName, keyword)
            .or().like(OaApproval::getRemark, keyword);
      });
    }
    if (normalizedApproverRole != null) {
      String token = "\"currentApproverRole\":\"" + normalizedApproverRole + "\"";
      wrapper.like(OaApproval::getFormData, token);
    }
    if (pendingMine && canApprove && "PENDING".equals(normalizedStatus)) {
      List<String> roles = AuthContext.getRoleCodes();
      List<String> normalizedRoles = (roles == null ? List.<String>of() : roles).stream()
          .map(item -> item == null ? null : item.trim().toUpperCase())
          .filter(item -> item != null && !item.isBlank())
          .toList();
      if (staffId != null || !normalizedRoles.isEmpty()) {
        wrapper.and(w -> {
          boolean first = true;
          if (staffId != null) {
            String numericIdToken = "\"currentApproverId\":" + staffId;
            String stringIdToken = "\"currentApproverId\":\"" + staffId + "\"";
            String arrayHeadToken = "\"currentApproverIds\":[" + staffId;
            String arrayMiddleToken = "," + staffId + ",";
            String arrayTailToken = "," + staffId + "]";
            String arraySingleToken = "\"currentApproverIds\":[" + staffId + "]";
            w.and(x -> x.like(OaApproval::getFormData, numericIdToken)
                .or().like(OaApproval::getFormData, stringIdToken)
                .or().like(OaApproval::getFormData, arrayHeadToken)
                .or().like(OaApproval::getFormData, arrayMiddleToken)
                .or().like(OaApproval::getFormData, arrayTailToken)
                .or().like(OaApproval::getFormData, arraySingleToken));
            first = false;
          }
          for (String role : normalizedRoles) {
            String token = "\"currentApproverRole\":\"" + role + "\"";
            String unassignedToken = "\"currentApproverId\":null";
            if (first) {
              w.and(x -> x.like(OaApproval::getFormData, token).like(OaApproval::getFormData, unassignedToken));
              first = false;
            } else {
              w.or(y -> y.like(OaApproval::getFormData, token).like(OaApproval::getFormData, unassignedToken));
            }
          }
        });
      }
    }
    return wrapper;
  }

  private long count(Long value) {
    return value == null ? 0L : value;
  }

  private String enrichWorkflowData(OaApprovalRequest request, Long applicantId) {
    Map<String, Object> map = readFormMap(request.getFormData());
    String type = normalizeType(request.getApprovalType());
    validateWorkflowRequest(type, request, map);
    map.put("workflowType", type);
    map.put("workflowVersion", "2026-Q1");
    if (parseString(map.get("submittedAt")) == null) {
      map.put("submittedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    bindApplicantSupervisors(map, applicantId);
    List<String> path = approvalPath(type, request);
    map.put("approvalPath", path);
    initializeWorkflowRuntime(map, path, AuthContext.getOrgId());
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

  private void bindApplicantSupervisors(Map<String, Object> map, Long applicantId) {
    if (map == null || applicantId == null) {
      return;
    }
    StaffAccount applicant = staffMapper.selectById(applicantId);
    if (applicant == null) {
      return;
    }
    map.put("applicantId", applicant.getId());
    map.put("applicantName", applicant.getRealName());
    map.put("applicantDepartmentId", applicant.getDepartmentId());
    if (applicant.getDirectLeaderId() != null) {
      map.put("directLeaderId", applicant.getDirectLeaderId());
      StaffAccount direct = staffMapper.selectById(applicant.getDirectLeaderId());
      if (direct != null) {
        map.put("directLeaderName", parseString(direct.getRealName()) != null ? parseString(direct.getRealName()) : parseString(direct.getUsername()));
      }
    } else {
      map.put("directLeaderId", null);
      map.put("directLeaderName", null);
    }
    if (applicant.getIndirectLeaderId() != null) {
      map.put("indirectLeaderId", applicant.getIndirectLeaderId());
      StaffAccount indirect = staffMapper.selectById(applicant.getIndirectLeaderId());
      if (indirect != null) {
        map.put("indirectLeaderName", parseString(indirect.getRealName()) != null ? parseString(indirect.getRealName()) : parseString(indirect.getUsername()));
      }
    } else {
      map.put("indirectLeaderId", null);
      map.put("indirectLeaderName", null);
    }
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
        staffId = AuthContext.getStaffId();
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
    if ("SHIFT_CHANGE".equals(type)) {
      return List.of("申请人提交", "被换班员工确认", "人事审批", "部门主管审批", "归档");
    }
    return List.of("申请人提交", "审批", "归档");
  }

  private void handleAfterApproved(OaApproval approval) {
    if (approval == null || approval.getApprovalType() == null) {
      return;
    }
    if ("LEAVE".equalsIgnoreCase(approval.getApprovalType())) {
      updateLeaveAnnualStats(approval);
      syncLeaveCalendarTask(approval);
    }
    if ("POINTS_CASH".equalsIgnoreCase(approval.getApprovalType())) {
      applyPointsCashRedeem(approval);
    }
    if ("PURCHASE".equalsIgnoreCase(approval.getApprovalType())) {
      applyPurchaseApproval(approval);
    }
    if ("MATERIAL_APPLY".equalsIgnoreCase(approval.getApprovalType())) {
      applyOutboundApproval(approval);
    }
    if ("SHIFT_CHANGE".equalsIgnoreCase(approval.getApprovalType())) {
      applyShiftChangeApproval(approval);
    }
  }

  private void applyPurchaseApproval(OaApproval approval) {
    Map<String, Object> formMap = readFormMap(approval == null ? null : approval.getFormData());
    Long purchaseOrderId = parseLong(formMap.get("purchaseOrderId"));
    if (purchaseOrderId == null) {
      return;
    }
    MaterialPurchaseOrder order = materialPurchaseOrderMapper.selectById(purchaseOrderId);
    if (order == null || (order.getIsDeleted() != null && order.getIsDeleted() == 1)) {
      return;
    }
    if (approval.getOrgId() != null && order.getOrgId() != null && !approval.getOrgId().equals(order.getOrgId())) {
      return;
    }
    String status = parseString(order.getStatus());
    if (status == null) {
      return;
    }
    status = status.toUpperCase();
    if ("APPROVED".equals(status) || "COMPLETED".equals(status) || "CANCELLED".equals(status)) {
      return;
    }
    if (!"DRAFT".equals(status)) {
      return;
    }
    order.setStatus("APPROVED");
    materialPurchaseOrderMapper.updateById(order);
  }

  private void applyOutboundApproval(OaApproval approval) {
    Map<String, Object> formMap = readFormMap(approval == null ? null : approval.getFormData());
    Long outboundSheetId = parseLong(formMap.get("outboundSheetId"));
    if (outboundSheetId == null) {
      return;
    }
    InventoryOutboundSheet sheet = inventoryOutboundSheetMapper.selectById(outboundSheetId);
    if (sheet == null || (sheet.getIsDeleted() != null && sheet.getIsDeleted() == 1)) {
      return;
    }
    if (approval.getOrgId() != null && sheet.getOrgId() != null && !approval.getOrgId().equals(sheet.getOrgId())) {
      return;
    }
    String status = parseString(sheet.getStatus());
    if ("CONFIRMED".equalsIgnoreCase(status)) {
      return;
    }
    if (status != null && !"DRAFT".equalsIgnoreCase(status)) {
      return;
    }
    List<InventoryOutboundSheetItem> items = inventoryOutboundSheetItemMapper.selectList(
        Wrappers.lambdaQuery(InventoryOutboundSheetItem.class)
            .eq(InventoryOutboundSheetItem::getIsDeleted, 0)
            .eq(InventoryOutboundSheetItem::getSheetId, outboundSheetId)
            .orderByAsc(InventoryOutboundSheetItem::getItemSort)
            .orderByAsc(InventoryOutboundSheetItem::getId));
    if (items.isEmpty()) {
      return;
    }
    Long orgId = sheet.getOrgId() != null ? sheet.getOrgId() : approval.getOrgId();
    Long operatorStaffId = approval.getApplicantId() != null ? approval.getApplicantId() : sheet.getOperatorStaffId();
    for (InventoryOutboundSheetItem item : items) {
      InventoryOutboundRequest outbound = new InventoryOutboundRequest();
      outbound.setOrgId(orgId);
      outbound.setOperatorStaffId(operatorStaffId);
      outbound.setProductId(item.getProductId());
      outbound.setWarehouseId(item.getWarehouseId());
      outbound.setBatchId(item.getBatchId());
      outbound.setQuantity(item.getQuantity());
      outbound.setReceiverName(sheet.getReceiverName());
      outbound.setReason(item.getReason());
      outbound.setOutboundNo(sheet.getOutboundNo());
      inventoryService.outbound(outbound);
    }
    sheet.setStatus("CONFIRMED");
    sheet.setConfirmStaffId(AuthContext.getStaffId());
    sheet.setConfirmTime(LocalDateTime.now());
    inventoryOutboundSheetMapper.updateById(sheet);
    auditLogService.record(
        orgId,
        orgId,
        AuthContext.getStaffId(),
        resolveCurrentStaffName(),
        "INVENTORY_OUTBOUND_SHEET_CONFIRM",
        "OUTBOUND_SHEET",
        sheet.getId(),
        "OA审批通过自动确认出库:" + sheet.getOutboundNo());
  }

  private void handleAfterRejected(OaApproval approval) {
    if (approval == null || approval.getApprovalType() == null) {
      return;
    }
    if ("LEAVE".equalsIgnoreCase(approval.getApprovalType())) {
      removeLeaveCalendarTask(approval);
    }
    if ("SHIFT_CHANGE".equalsIgnoreCase(approval.getApprovalType())) {
      markShiftSwapApprovalStatus(approval, "REJECTED");
    }
  }

  private void handleAfterDeleted(OaApproval approval) {
    if (approval == null || approval.getApprovalType() == null) {
      return;
    }
    if ("LEAVE".equalsIgnoreCase(approval.getApprovalType())) {
      removeLeaveCalendarTask(approval);
    }
    if ("SHIFT_CHANGE".equalsIgnoreCase(approval.getApprovalType())) {
      markShiftSwapApprovalStatus(approval, "CANCELLED");
    }
  }

  private void applyShiftChangeApproval(OaApproval approval) {
    Map<String, Object> formMap = readFormMap(approval == null ? null : approval.getFormData());
    Long swapRequestId = parseLong(formMap.get("swapRequestId"));
    if (swapRequestId == null) {
      return;
    }
    scheduleService.applySwapByApproval(
        approval.getOrgId(), swapRequestId, AuthContext.getStaffId(), LocalDateTime.now());
    markShiftSwapApprovalStatus(approval, "APPROVED");
  }

  private void markShiftSwapApprovalStatus(OaApproval approval, String status) {
    if (approval == null || approval.getId() == null) {
      return;
    }
    Map<String, Object> formMap = readFormMap(approval.getFormData());
    Long swapRequestId = parseLong(formMap.get("swapRequestId"));
    if (swapRequestId == null) {
      return;
    }
    var entity = shiftSwapRequestMapper.selectById(swapRequestId);
    if (entity == null) {
      return;
    }
    entity.setApprovalId(approval.getId());
    entity.setApprovalStatus(status);
    if ("REJECTED".equalsIgnoreCase(status)) {
      entity.setStatus("REJECTED");
    } else if ("CANCELLED".equalsIgnoreCase(status)) {
      entity.setStatus("CANCELLED");
    }
    shiftSwapRequestMapper.updateById(entity);
  }

  private void syncApprovalTodo(OaApproval approval) {
    if (approval == null || approval.getId() == null) {
      return;
    }
    OaTodo todo = findApprovalTodo(approval.getOrgId(), approval.getId());
    if (approval.getIsDeleted() != null && approval.getIsDeleted() == 1) {
      closeApprovalTodo(todo, "审批单已删除");
      return;
    }
    if (!"PENDING".equalsIgnoreCase(approval.getStatus())) {
      closeApprovalTodo(todo, "审批流程已结束：" + safe(approval.getStatus()));
      return;
    }
    Map<String, Object> formMap = readFormMap(approval.getFormData());
    String currentNodeName = parseString(formMap.get("currentNodeName"));
    String approverRole = parseString(formMap.get("currentApproverRole"));
    String approverName = parseString(formMap.get("currentApproverName"));
    Integer nodeIndex = parseInteger(formMap.get("currentNodeIndex"));
    Long assigneeId = parseLong(formMap.get("currentApproverId"));
    if (assigneeId == null) {
      assigneeId = resolveApproverAssigneeId(formMap, approverRole);
    }
    String marker = approvalTodoMarker(approval.getId());
    if (todo == null) {
      todo = new OaTodo();
      todo.setTenantId(approval.getOrgId());
      todo.setOrgId(approval.getOrgId());
      todo.setCreatedBy(AuthContext.getStaffId());
    }
    todo.setTitle("审批待办：" + safe(approval.getTitle()));
    todo.setContent(marker
        + " 类型=" + safe(approval.getApprovalType())
        + " 申请人=" + safe(approval.getApplicantName())
        + " 当前节点=" + safe(currentNodeName)
        + " 审批角色=" + safe(approverRole)
        + " 审批人ID=" + (assigneeId == null ? "-" : assigneeId)
        + " 节点序号=" + (nodeIndex == null ? "-" : nodeIndex));
    todo.setStatus("OPEN");
    todo.setDueTime(resolveApprovalTodoDueTime(approval));
    todo.setAssigneeId(assigneeId);
    todo.setAssigneeName(approverName);
    if (todo.getId() == null) {
      todoMapper.insert(todo);
    } else {
      todoMapper.updateById(todo);
    }
  }

  private LocalDateTime resolveApprovalTodoDueTime(OaApproval approval) {
    if (approval == null) {
      return LocalDateTime.now().plusHours(24);
    }
    if (approval.getStartTime() != null) {
      return approval.getStartTime().plusHours(24);
    }
    if (approval.getCreateTime() != null) {
      return approval.getCreateTime().plusHours(24);
    }
    return LocalDateTime.now().plusHours(24);
  }

  private OaTodo findApprovalTodo(Long orgId, Long approvalId) {
    if (approvalId == null) {
      return null;
    }
    String marker = approvalTodoMarker(approvalId);
    return todoMapper.selectOne(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .like(OaTodo::getContent, marker)
        .orderByDesc(OaTodo::getCreateTime)
        .last("LIMIT 1"));
  }

  private String approvalTodoMarker(Long approvalId) {
    return "[APPROVAL_FLOW:" + safe(approvalId) + "]";
  }

  private void closeApprovalTodo(OaTodo todo, String message) {
    if (todo == null) {
      return;
    }
    if ("DONE".equalsIgnoreCase(todo.getStatus())) {
      return;
    }
    todo.setStatus("DONE");
    todo.setDueTime(LocalDateTime.now());
    if (message != null && !message.isBlank()) {
      String content = safe(todo.getContent());
      if (!content.contains("[CLOSED_REASON]")) {
        todo.setContent(content + " [CLOSED_REASON]" + message);
      }
    }
    todoMapper.updateById(todo);
  }

  private Long resolveApproverAssigneeId(Map<String, Object> formMap, String approverRole) {
    Long approverId = parseLong(formMap == null ? null : formMap.get("currentApproverId"));
    if (approverId != null) {
      return approverId;
    }
    List<Long> approverIds = parseLongList(formMap == null ? null : formMap.get("currentApproverIds"));
    if (!approverIds.isEmpty()) {
      return approverIds.get(0);
    }
    String normalizedRole = parseString(approverRole);
    if (normalizedRole == null) {
      return null;
    }
    normalizedRole = normalizedRole.trim().toUpperCase();
    if ("DEPT_MANAGER".equals(normalizedRole)) {
      return parseLong(formMap == null ? null : formMap.get("directLeaderId"));
    }
    if ("DEAN".equals(normalizedRole)) {
      return parseLong(formMap == null ? null : formMap.get("indirectLeaderId"));
    }
    return null;
  }

  private ApproverAssignment resolveApproverAssignment(Long orgId, String roleCode, Map<String, Object> map) {
    String normalizedRole = parseString(roleCode);
    if (normalizedRole == null) {
      return new ApproverAssignment(null, "待分配", List.of(), List.of());
    }
    normalizedRole = normalizedRole.trim().toUpperCase();
    if ("DEPT_MANAGER".equals(normalizedRole)) {
      Long staffId = parseLong(map == null ? null : map.get("directLeaderId"));
      String approverName = resolveApproverName(normalizedRole, map);
      String displayName = formatApproverDisplay(resolveApproverDisplayRoleName(normalizedRole), approverName);
      return new ApproverAssignment(
          staffId,
          displayName,
          staffId == null ? List.of() : List.of(staffId),
          displayName == null ? List.of() : List.of(displayName));
    }
    if ("DEAN".equals(normalizedRole)) {
      Long staffId = parseLong(map == null ? null : map.get("indirectLeaderId"));
      String approverName = resolveApproverName(normalizedRole, map);
      String displayName = formatApproverDisplay(resolveApproverDisplayRoleName(normalizedRole), approverName);
      return new ApproverAssignment(
          staffId,
          displayName,
          staffId == null ? List.of() : List.of(staffId),
          displayName == null ? List.of() : List.of(displayName));
    }
    List<String> candidateRoles = switch (normalizedRole) {
      case "HR" -> List.of("HR_MINISTER", "HR_EMPLOYEE", "HR_MANAGER", "PERSONNEL", "HR");
      case "FINANCE" -> List.of("FINANCE_MINISTER", "FINANCE_EMPLOYEE", "ACCOUNTANT", "CASHIER", "FINANCE");
      case "WAREHOUSE" -> List.of("LOGISTICS_MINISTER", "LOGISTICS_EMPLOYEE", "WAREHOUSE", "STORE_KEEPER");
      case "ADMIN_OFFICE" -> List.of("HR_MINISTER", "HR_EMPLOYEE", "OFFICE", "ADMIN");
      case "PURCHASING" -> List.of("LOGISTICS_MINISTER", "LOGISTICS_EMPLOYEE", "PURCHASE", "PROCUREMENT");
      default -> List.of();
    };
    List<ApproverCandidate> candidates = findActiveApproverCandidatesByRoles(
        orgId,
        candidateRoles,
        resolveApproverDisplayRoleName(normalizedRole));
    if (!candidates.isEmpty()) {
      ApproverCandidate primary = candidates.get(0);
      List<Long> candidateIds = candidates.stream()
          .map(ApproverCandidate::staffId)
          .filter(id -> id != null && id > 0)
          .distinct()
          .toList();
      List<String> candidateDisplayNames = candidates.stream()
          .map(ApproverCandidate::displayName)
          .filter(name -> name != null && !name.isBlank())
          .distinct()
          .toList();
      String joinedDisplayName = candidateDisplayNames.isEmpty()
          ? primary.displayName()
          : String.join(" / ", candidateDisplayNames);
      return new ApproverAssignment(primary.staffId(), joinedDisplayName, candidateIds, candidateDisplayNames);
    }
    String fallbackName = resolveApproverName(normalizedRole, map);
    String fallbackDisplayName = formatApproverDisplay(resolveApproverDisplayRoleName(normalizedRole), fallbackName);
    return new ApproverAssignment(
        null,
        fallbackDisplayName,
        List.of(),
        fallbackDisplayName == null ? List.of() : List.of(fallbackDisplayName));
  }

  private List<ApproverCandidate> findActiveApproverCandidatesByRoles(Long orgId, List<String> roleCodes, String fallbackRoleLabel) {
    if (orgId == null || roleCodes == null || roleCodes.isEmpty()) {
      return List.of();
    }
    List<String> normalizedRoles = roleCodes.stream()
        .map(item -> item == null ? null : item.trim().toUpperCase())
        .filter(item -> item != null && !item.isBlank())
        .distinct()
        .toList();
    if (normalizedRoles.isEmpty()) {
      return List.of();
    }
    Map<String, Integer> priorityMap = new LinkedHashMap<>();
    for (int i = 0; i < normalizedRoles.size(); i++) {
      priorityMap.put(normalizedRoles.get(i), i);
    }
    List<Role> roles = roleMapper.selectList(Wrappers.lambdaQuery(Role.class)
        .eq(Role::getIsDeleted, 0)
        .eq(Role::getOrgId, orgId)
        .eq(Role::getStatus, 1)
        .in(Role::getRoleCode, normalizedRoles));
    if (roles.isEmpty()) {
      return List.of();
    }
    Map<Long, Integer> rolePriority = new LinkedHashMap<>();
    Map<Long, String> roleLabels = new LinkedHashMap<>();
    for (Role role : roles) {
      if (role == null || role.getId() == null) {
        continue;
      }
      Integer priority = priorityMap.get(String.valueOf(role.getRoleCode()).trim().toUpperCase());
      if (priority != null) {
        rolePriority.put(role.getId(), priority);
        roleLabels.put(role.getId(), parseString(role.getRoleName()));
      }
    }
    if (rolePriority.isEmpty()) {
      return List.of();
    }
    List<StaffRole> staffRoles = staffRoleMapper.selectList(Wrappers.lambdaQuery(StaffRole.class)
        .eq(StaffRole::getIsDeleted, 0)
        .eq(StaffRole::getOrgId, orgId)
        .in(StaffRole::getRoleId, rolePriority.keySet()));
    if (staffRoles.isEmpty()) {
      return List.of();
    }
    Map<Long, Integer> bestPriorityByStaff = new LinkedHashMap<>();
    Map<Long, Long> bestRoleIdByStaff = new LinkedHashMap<>();
    for (StaffRole staffRole : staffRoles) {
      if (staffRole == null || staffRole.getStaffId() == null || staffRole.getRoleId() == null) {
        continue;
      }
      Integer priority = rolePriority.get(staffRole.getRoleId());
      if (priority == null) {
        continue;
      }
      Long staffId = staffRole.getStaffId();
      Integer existingPriority = bestPriorityByStaff.get(staffId);
      if (existingPriority == null || priority < existingPriority) {
        bestPriorityByStaff.put(staffId, priority);
        bestRoleIdByStaff.put(staffId, staffRole.getRoleId());
      }
    }
    if (bestPriorityByStaff.isEmpty()) {
      return List.of();
    }
    List<StaffAccount> staffs = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(StaffAccount::getOrgId, orgId)
        .eq(StaffAccount::getStatus, 1)
        .in(StaffAccount::getId, bestPriorityByStaff.keySet()));
    return staffs.stream()
        .sorted((left, right) -> {
          int leftPriority = bestPriorityByStaff.getOrDefault(left.getId(), Integer.MAX_VALUE);
          int rightPriority = bestPriorityByStaff.getOrDefault(right.getId(), Integer.MAX_VALUE);
          if (leftPriority != rightPriority) {
            return Integer.compare(leftPriority, rightPriority);
          }
          return Long.compare(left.getId() == null ? Long.MAX_VALUE : left.getId(), right.getId() == null ? Long.MAX_VALUE : right.getId());
        })
        .map(staff -> {
          Long roleId = bestRoleIdByStaff.get(staff.getId());
          String roleLabel = parseString(roleLabels.get(roleId));
          String staffName = resolveStaffDisplayName(staff);
          String displayName = formatApproverDisplay(roleLabel == null ? fallbackRoleLabel : roleLabel, staffName);
          return new ApproverCandidate(staff.getId(), staffName, displayName);
        })
        .toList();
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

  private void syncLeaveCalendarTask(OaApproval approval) {
    if (approval == null || approval.getId() == null || approval.getApplicantId() == null) {
      return;
    }
    OaTask task = taskMapper.selectOne(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(approval.getOrgId() != null, OaTask::getOrgId, approval.getOrgId())
        .eq(OaTask::getSourceTodoId, approval.getId())
        .eq(OaTask::getAssigneeId, approval.getApplicantId())
        .last("LIMIT 1"));
    if (task == null) {
      task = new OaTask();
      task.setTenantId(approval.getOrgId());
      task.setOrgId(approval.getOrgId());
      task.setSourceTodoId(approval.getId());
      task.setCreatedBy(AuthContext.getStaffId());
    }
    task.setTitle("请假中：" + parseString(approval.getApplicantName()));
    task.setDescription("来源审批单#" + approval.getId() + "，审批通过后自动同步。");
    task.setStartTime(approval.getStartTime());
    task.setEndTime(approval.getEndTime());
    task.setPriority("NORMAL");
    task.setStatus("OPEN");
    task.setAssigneeId(approval.getApplicantId());
    task.setAssigneeName(approval.getApplicantName());
    task.setCalendarType("PERSONAL");
    task.setPlanCategory("请假");
    task.setUrgency("NORMAL");
    task.setEventColor("#1890ff");
    task.setCollaboratorIds(null);
    task.setCollaboratorNames(null);
    task.setIsRecurring(0);
    task.setRecurrenceRule(null);
    task.setRecurrenceInterval(null);
    task.setRecurrenceCount(null);
    if (task.getId() == null) {
      taskMapper.insert(task);
      return;
    }
    taskMapper.updateById(task);
  }

  private void removeLeaveCalendarTask(OaApproval approval) {
    if (approval == null || approval.getId() == null || approval.getApplicantId() == null) {
      return;
    }
    OaTask task = taskMapper.selectOne(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(approval.getOrgId() != null, OaTask::getOrgId, approval.getOrgId())
        .eq(OaTask::getSourceTodoId, approval.getId())
        .eq(OaTask::getAssigneeId, approval.getApplicantId())
        .last("LIMIT 1"));
    if (task == null) {
      return;
    }
    task.setIsDeleted(1);
    taskMapper.updateById(task);
  }

  private void applyApprovalDecision(OaApproval approval, String status, String remark) {
    Map<String, Object> map = readFormMap(approval.getFormData());
    List<String> path = parseStringList(map.get("approvalPath"));
    int currentNodeIndex = resolveCurrentNodeIndex(path, map);
    String currentNodeName = currentNodeIndex >= 0 && currentNodeIndex < path.size() ? path.get(currentNodeIndex) : null;
    appendApprovalRoundHistory(map, currentNodeIndex, currentNodeName, status, remark);
    map.put("lastApprovalStatus", status);
    map.put("lastApprovalRemark", normalizeOpinion(remark));
    map.put("lastApprovalAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    map.put("lastApproverId", AuthContext.getStaffId());
    map.put("lastApproverRoles", AuthContext.getRoleCodes());
    map.put("lastApproverName", resolveApplicantName(null));
    if ("APPROVED".equals(status) && !path.isEmpty()) {
      int lastApprovalNodeIndex = resolveLastApprovalNodeIndex(path);
      if (currentNodeIndex < lastApprovalNodeIndex) {
        int nextIndex = Math.max(currentNodeIndex + 1, 0);
        syncCurrentNodeMeta(map, path, nextIndex, approval.getOrgId());
        map.put("workflowState", "IN_REVIEW");
        map.put("nextApprovalRequired", true);
        String toRole = parseString(map.get("currentApproverRole"));
        String toName = parseString(map.get("currentApproverName"));
        appendNotifyHistory(map, toRole, toName, "上一审批环节已通过，系统已自动同步到你审批");
        approval.setStatus("PENDING");
        approval.setRemark(normalizeOpinion(remark));
        approval.setFormData(writeFormMap(map));
        return;
      }
    }
    if ("REJECTED".equals(status)) {
      map.put("workflowState", "REJECTED");
      map.put("nextApprovalRequired", false);
      map.put("currentNodeName", "已驳回");
      map.put("currentApproverRole", null);
      map.put("currentApproverName", null);
    } else if ("APPROVED".equals(status)) {
      map.put("workflowState", "COMPLETED");
      map.put("nextApprovalRequired", false);
      map.put("currentNodeName", "流程完成");
      map.put("currentApproverRole", null);
      map.put("currentApproverName", null);
    }
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

  private void ensureCurrentApproverPermission(OaApproval approval) {
    Map<String, Object> map = readFormMap(approval == null ? null : approval.getFormData());
    Long requiredApproverId = parseLong(map.get("currentApproverId"));
    List<Long> requiredApproverIds = parseLongList(map.get("currentApproverIds"));
    if (!requiredApproverIds.isEmpty() && !AuthContext.isAdmin()) {
      Long currentStaffId = AuthContext.getStaffId();
      if (currentStaffId == null || !requiredApproverIds.contains(currentStaffId)) {
        throw new AccessDeniedException("当前审批环节仅允许指定审批候选人处理");
      }
    } else if (requiredApproverId != null && !AuthContext.isAdmin()) {
      Long currentStaffId = AuthContext.getStaffId();
      if (currentStaffId == null || !requiredApproverId.equals(currentStaffId)) {
        throw new AccessDeniedException("当前审批环节已指派给指定审批人");
      }
    }
    String requiredRole = parseString(map.get("currentApproverRole"));
    if (requiredRole == null) {
      return;
    }
    if (!canApproveByRole(requiredRole)) {
      throw new AccessDeniedException("当前审批环节要求角色：" + requiredRole);
    }
  }

  private boolean canCurrentUserApproveStep(OaApproval approval) {
    try {
      ensureCurrentApproverPermission(approval);
      return true;
    } catch (Exception ignore) {
      return false;
    }
  }

  private boolean canCurrentUserApprove() {
    return AuthContext.isMinisterOrHigher();
  }

  private Long resolveApplicantId(Long currentStaffId, Long requestApplicantId) {
    if (requestApplicantId != null && currentStaffId != null && !requestApplicantId.equals(currentStaffId)) {
      throw new IllegalArgumentException("申请人只能是当前登录人");
    }
    return currentStaffId;
  }

  private String resolveApplicantName(String requestApplicantName) {
    String staffName = resolveCurrentStaffName();
    if (staffName != null && !staffName.isBlank()) {
      return staffName;
    }
    String currentName = parseString(AuthContext.getUsername());
    if (currentName != null) {
      return currentName;
    }
    String requested = parseString(requestApplicantName);
    return requested == null ? "当前用户" : requested;
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

  private void initializeWorkflowRuntime(Map<String, Object> map, List<String> path, Long orgId) {
    if (map == null || path == null || path.isEmpty()) {
      return;
    }
    if (parseString(map.get("currentNodeName")) != null
        && parseInteger(map.get("currentNodeIndex")) != null
        && parseString(map.get("currentApproverRole")) != null) {
      return;
    }
    int initialNodeIndex = path.size() > 1 ? 1 : 0;
    syncCurrentNodeMeta(map, path, initialNodeIndex, orgId);
    map.put("workflowState", "IN_REVIEW");
    map.put("nextApprovalRequired", true);
    String toRole = parseString(map.get("currentApproverRole"));
    String toName = parseString(map.get("currentApproverName"));
    appendNotifyHistory(map, toRole, toName, "审批单已进入你的待办，请尽快处理");
  }

  private int resolveCurrentNodeIndex(List<String> path, Map<String, Object> map) {
    if (path == null || path.isEmpty()) {
      return 0;
    }
    Integer index = parseInteger(map == null ? null : map.get("currentNodeIndex"));
    if (index == null || index < 0 || index >= path.size()) {
      return path.size() > 1 ? 1 : 0;
    }
    return index;
  }

  private int resolveLastApprovalNodeIndex(List<String> path) {
    if (path == null || path.isEmpty()) {
      return 0;
    }
    int lastIndex = path.size() - 1;
    String lastNode = path.get(lastIndex);
    if (lastNode != null && lastNode.contains("归档") && lastIndex > 0) {
      return lastIndex - 1;
    }
    return lastIndex;
  }

  private void syncCurrentNodeMeta(Map<String, Object> map, List<String> path, int nodeIndex, Long orgId) {
    if (map == null || path == null || path.isEmpty()) {
      return;
    }
    int index = Math.max(0, Math.min(nodeIndex, path.size() - 1));
    String node = path.get(index);
    String role = resolveApproverRole(node);
    ApproverAssignment assignment = resolveApproverAssignment(orgId, role, map);
    map.put("currentNodeIndex", index);
    map.put("currentNodeName", node);
    map.put("currentStep", node);
    map.put("currentApproverRole", role);
    map.put("currentApproverId", assignment.staffId());
    map.put("currentApproverIds", assignment.candidateIds());
    map.put("currentApproverName", assignment.staffName());
    map.put("currentApproverDisplayNames", assignment.candidateDisplayNames());
    map.put("currentNodeEnteredAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
  }

  private void appendApprovalRoundHistory(
      Map<String, Object> map,
      int nodeIndex,
      String nodeName,
      String action,
      String remark) {
    if (map == null) {
      return;
    }
    Object raw = map.get("approvalRounds");
    List<Map<String, Object>> rounds = new ArrayList<>();
    if (raw instanceof List<?> list) {
      for (Object item : list) {
        if (item instanceof Map<?, ?> oldMap) {
          Map<String, Object> next = new LinkedHashMap<>();
          oldMap.forEach((k, v) -> next.put(String.valueOf(k), v));
          rounds.add(next);
        }
      }
    }
    Map<String, Object> round = new LinkedHashMap<>();
    round.put("round", rounds.size() + 1);
    round.put("nodeIndex", nodeIndex);
    round.put("nodeName", nodeName);
    round.put("action", action);
    round.put("remark", normalizeOpinion(remark));
    round.put("approverId", AuthContext.getStaffId());
    round.put("approverName", resolveCurrentStaffName());
    round.put("at", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    rounds.add(round);
    map.put("approvalRounds", rounds);
  }

  private void appendNotifyHistory(Map<String, Object> map, String toRole, String toName, String message) {
    if (map == null) {
      return;
    }
    Object raw = map.get("notifyHistory");
    List<Map<String, Object>> history = new ArrayList<>();
    if (raw instanceof List<?> list) {
      for (Object item : list) {
        if (item instanceof Map<?, ?> oldMap) {
          Map<String, Object> next = new LinkedHashMap<>();
          oldMap.forEach((k, v) -> next.put(String.valueOf(k), v));
          history.add(next);
        }
      }
    }
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("toRole", parseString(toRole));
    row.put("toName", parseString(toName));
    row.put("toId", parseLong(map.get("currentApproverId")));
    row.put("toIds", parseLongList(map.get("currentApproverIds")));
    row.put("message", parseString(message));
    row.put("at", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    row.put("by", resolveCurrentStaffName());
    history.add(row);
    map.put("notifyHistory", history);
  }

  private String resolveCurrentStaffName() {
    Long staffId = AuthContext.getStaffId();
    if (staffId != null) {
      StaffAccount account = staffMapper.selectById(staffId);
      String realName = parseString(account == null ? null : account.getRealName());
      if (realName != null && !realName.isBlank()) {
        return realName;
      }
    }
    String username = parseString(AuthContext.getUsername());
    if (username != null && !username.isBlank()) {
      return username;
    }
    return "当前用户";
  }

  private List<String> parseStringList(Object value) {
    if (!(value instanceof List<?> list) || list.isEmpty()) {
      return List.of();
    }
    return list.stream().map(this::parseString).filter(item -> item != null && !item.isBlank()).toList();
  }

  private String resolveApproverRole(String nodeName) {
    String node = parseString(nodeName);
    if (node == null) {
      return "MANAGER";
    }
    if (node.contains("部门主管")) return "DEPT_MANAGER";
    if (node.contains("人事")) return "HR";
    if (node.contains("财务") || node.contains("出纳")) return "FINANCE";
    if (node.contains("院长")) return "DEAN";
    if (node.contains("仓库")) return "WAREHOUSE";
    if (node.contains("行政")) return "ADMIN_OFFICE";
    if (node.contains("采购")) return "PURCHASING";
    if (node.contains("归档")) return "ARCHIVE";
    return "MANAGER";
  }

  private String resolveApproverName(String roleCode, Map<String, Object> map) {
    if (roleCode == null) return "待分配";
    if ("DEPT_MANAGER".equals(roleCode)) {
      String directLeaderName = parseString(map == null ? null : map.get("directLeaderName"));
      return directLeaderName == null ? "部门主管" : directLeaderName;
    }
    if ("HR".equals(roleCode)) return "人事审批人";
    if ("FINANCE".equals(roleCode)) return "财务审批人";
    if ("DEAN".equals(roleCode)) {
      String indirectLeaderName = parseString(map == null ? null : map.get("indirectLeaderName"));
      return indirectLeaderName == null ? "院长" : indirectLeaderName;
    }
    if ("WAREHOUSE".equals(roleCode)) return "仓库管理员";
    if ("ADMIN_OFFICE".equals(roleCode)) return "行政审批人";
    if ("PURCHASING".equals(roleCode)) return "采购执行人";
    if ("ARCHIVE".equals(roleCode)) return "系统归档";
    return "审批负责人";
  }

  private String resolveApproverDisplayRoleName(String roleCode) {
    if (roleCode == null) return "审批负责人";
    return switch (roleCode) {
      case "DEPT_MANAGER", "MANAGER" -> "部门主管";
      case "HR" -> "人事部长";
      case "FINANCE" -> "财务部长";
      case "DEAN" -> "院长";
      case "WAREHOUSE" -> "仓库负责人";
      case "ADMIN_OFFICE" -> "行政负责人";
      case "PURCHASING" -> "采购负责人";
      case "ARCHIVE" -> "系统归档";
      default -> "审批负责人";
    };
  }

  private String resolveStaffDisplayName(StaffAccount staff) {
    if (staff == null) {
      return null;
    }
    String realName = parseString(staff.getRealName());
    if (realName != null) {
      return realName;
    }
    return parseString(staff.getUsername());
  }

  private String formatApproverDisplay(String roleLabel, String staffName) {
    String normalizedRoleLabel = parseString(roleLabel);
    String normalizedStaffName = parseString(staffName);
    if (normalizedRoleLabel != null && normalizedStaffName != null) {
      return normalizedRoleLabel + "（" + normalizedStaffName + "）";
    }
    return normalizedStaffName == null ? (normalizedRoleLabel == null ? "待分配" : normalizedRoleLabel) : normalizedStaffName;
  }

  private boolean isApprovalTimeout(OaApproval approval, LocalDateTime now) {
    if (approval == null || !"PENDING".equalsIgnoreCase(approval.getStatus())) {
      return false;
    }
    LocalDateTime enteredAt = resolveApprovalActiveSince(approval);
    if (enteredAt == null) {
      return false;
    }
    return enteredAt.isBefore(now.minusHours(48));
  }

  private LocalDateTime resolveApprovalActiveSince(OaApproval approval) {
    Map<String, Object> map = readFormMap(approval == null ? null : approval.getFormData());
    LocalDateTime currentNodeEnteredAt = parseDateTime(map.get("currentNodeEnteredAt"));
    if (currentNodeEnteredAt != null) {
      return currentNodeEnteredAt;
    }
    Object rawNotifyHistory = map.get("notifyHistory");
    if (rawNotifyHistory instanceof List<?> list && !list.isEmpty()) {
      for (int i = list.size() - 1; i >= 0; i--) {
        Object item = list.get(i);
        if (item instanceof Map<?, ?> row) {
          LocalDateTime at = parseDateTime(row.get("at"));
          if (at != null) {
            return at;
          }
        }
      }
    }
    LocalDateTime submittedAt = parseDateTime(map.get("submittedAt"));
    if (submittedAt != null) {
      return submittedAt;
    }
    return approval == null ? null : approval.getCreateTime();
  }

  private LocalDateTime parseDateTime(Object value) {
    String text = parseString(value);
    if (text == null) {
      return null;
    }
    try {
      return LocalDateTime.parse(text.replace(' ', 'T'));
    } catch (Exception ignore) {
      return null;
    }
  }

  private boolean canApproveByRole(String roleCode) {
    if (roleCode == null || roleCode.isBlank()) {
      return canCurrentUserApprove();
    }
    if (AuthContext.isAdmin()) {
      return true;
    }
    String normalized = roleCode.trim().toUpperCase();
    if ("DEPT_MANAGER".equals(normalized) || "MANAGER".equals(normalized)) {
      return AuthContext.isMinisterOrHigher();
    }
    if ("DEAN".equals(normalized)) {
      return AuthContext.hasRole("DEAN") || AuthContext.hasRole("DIRECTOR") || AuthContext.isAdmin();
    }
    if ("HR".equals(normalized)) {
      return AuthContext.hasRole("HR")
          || AuthContext.hasRole("HR_MANAGER")
          || AuthContext.hasRole("PERSONNEL")
          || AuthContext.isMinisterOrHigher();
    }
    if ("FINANCE".equals(normalized)) {
      return AuthContext.hasRole("FINANCE")
          || AuthContext.hasRole("CASHIER")
          || AuthContext.hasRole("ACCOUNTANT")
          || AuthContext.isMinisterOrHigher();
    }
    if ("WAREHOUSE".equals(normalized)) {
      return AuthContext.hasRole("WAREHOUSE")
          || AuthContext.hasRole("STORE_KEEPER")
          || AuthContext.isMinisterOrHigher();
    }
    if ("ADMIN_OFFICE".equals(normalized)) {
      return AuthContext.hasRole("ADMIN")
          || AuthContext.hasRole("OFFICE")
          || AuthContext.isMinisterOrHigher();
    }
    if ("PURCHASING".equals(normalized)) {
      return AuthContext.hasRole("PURCHASE")
          || AuthContext.hasRole("PROCUREMENT")
          || AuthContext.isMinisterOrHigher();
    }
    return canCurrentUserApprove();
  }

  private record ApproverCandidate(Long staffId, String staffName, String displayName) {}

  private record ApproverAssignment(Long staffId, String staffName, List<Long> candidateIds, List<String> candidateDisplayNames) {}

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

  private List<Long> parseLongList(Object value) {
    if (!(value instanceof List<?> list) || list.isEmpty()) {
      return List.of();
    }
    return list.stream()
        .map(this::parseLong)
        .filter(item -> item != null && item > 0)
        .distinct()
        .toList();
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
