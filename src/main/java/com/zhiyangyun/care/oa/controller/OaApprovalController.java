package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.oa.model.OaApprovalBatchRejectRequest;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.oa.model.OaApprovalRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
@RequestMapping("/api/oa/approval")
public class OaApprovalController {
  private final OaApprovalMapper approvalMapper;

  public OaApprovalController(OaApprovalMapper approvalMapper) {
    this.approvalMapper = approvalMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaApproval>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    String normalizedType = normalizeType(type);
    var wrapper = Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(normalizedStatus != null, OaApproval::getStatus, normalizedStatus)
        .eq(normalizedType != null, OaApproval::getApprovalType, normalizedType);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaApproval::getTitle, keyword)
          .or()
          .like(OaApproval::getApplicantName, keyword)
          .or()
          .like(OaApproval::getRemark, keyword));
    }
    wrapper
        .orderByDesc(OaApproval::getCreateTime);
    return Result.ok(approvalMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<OaApproval> create(@Valid @RequestBody OaApprovalRequest request) {
    Long orgId = AuthContext.getOrgId();
    OaApproval approval = new OaApproval();
    approval.setTenantId(orgId);
    approval.setOrgId(orgId);
    validateDateRange(request.getStartTime(), request.getEndTime());
    approval.setApprovalType(normalizeType(request.getApprovalType()));
    approval.setTitle(request.getTitle());
    approval.setApplicantId(request.getApplicantId());
    approval.setApplicantName(request.getApplicantName());
    approval.setAmount(request.getAmount());
    approval.setStartTime(request.getStartTime());
    approval.setEndTime(request.getEndTime());
    approval.setFormData(request.getFormData());
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null && !"PENDING".equals(normalizedStatus)) {
      throw new IllegalArgumentException("创建审批单仅支持 PENDING 状态");
    }
    approval.setStatus(normalizedStatus == null ? "PENDING" : normalizedStatus);
    approval.setRemark(request.getRemark());
    approval.setCreatedBy(AuthContext.getStaffId());
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
    approval.setApplicantId(request.getApplicantId());
    approval.setApplicantName(request.getApplicantName());
    approval.setAmount(request.getAmount());
    approval.setStartTime(request.getStartTime());
    approval.setEndTime(request.getEndTime());
    approval.setFormData(request.getFormData());
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
  public Result<OaApproval> approve(@PathVariable Long id) {
    OaApproval approval = findAccessibleApproval(id);
    if (approval == null) {
      return Result.ok(null);
    }
    ensurePending(approval);
    approval.setStatus("APPROVED");
    approvalMapper.updateById(approval);
    return Result.ok(approval);
  }

  @PutMapping("/{id}/reject")
  public Result<OaApproval> reject(@PathVariable Long id, @RequestParam(required = false) String remark) {
    OaApproval approval = findAccessibleApproval(id);
    if (approval == null) {
      return Result.ok(null);
    }
    ensurePending(approval);
    approval.setStatus("REJECTED");
    approval.setRemark(remark);
    approvalMapper.updateById(approval);
    return Result.ok(approval);
  }

  @PutMapping("/batch/approve")
  public Result<Integer> batchApprove(@RequestBody OaBatchIdsRequest request) {
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
      approval.setStatus("APPROVED");
      approvalMapper.updateById(approval);
      count++;
    }
    return Result.ok(count);
  }

  @PutMapping("/batch/reject")
  public Result<Integer> batchReject(@RequestBody OaApprovalBatchRejectRequest request) {
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
      approval.setStatus("REJECTED");
      approval.setRemark(remark);
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
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    String normalizedType = normalizeType(type);
    var wrapper = Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(normalizedStatus != null, OaApproval::getStatus, normalizedStatus)
        .eq(normalizedType != null, OaApproval::getApprovalType, normalizedType)
        .orderByDesc(OaApproval::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaApproval::getTitle, keyword)
          .or()
          .like(OaApproval::getApplicantName, keyword)
          .or()
          .like(OaApproval::getRemark, keyword));
    }
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
    return approvalMapper.selectOne(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getId, id)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
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
        && !"MATERIAL_APPLY".equals(normalized)
        && !"OFFICIAL_SEAL".equals(normalized)) {
      throw new IllegalArgumentException(
          "approvalType 仅支持 LEAVE/OVERTIME/REIMBURSE/PURCHASE/INCOME_PROOF/MATERIAL_APPLY/OFFICIAL_SEAL");
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
}
