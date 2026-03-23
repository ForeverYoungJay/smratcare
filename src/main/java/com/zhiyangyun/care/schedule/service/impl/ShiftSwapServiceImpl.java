package com.zhiyangyun.care.schedule.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.schedule.entity.ShiftSwapRequest;
import com.zhiyangyun.care.schedule.entity.StaffSchedule;
import com.zhiyangyun.care.schedule.mapper.ShiftSwapRequestMapper;
import com.zhiyangyun.care.schedule.mapper.StaffScheduleMapper;
import com.zhiyangyun.care.schedule.model.ShiftSwapRequestCreateRequest;
import com.zhiyangyun.care.schedule.model.ShiftSwapRequestResponse;
import com.zhiyangyun.care.schedule.service.ShiftSwapService;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShiftSwapServiceImpl implements ShiftSwapService {
  private final ShiftSwapRequestMapper shiftSwapRequestMapper;
  private final StaffScheduleMapper staffScheduleMapper;
  private final StaffMapper staffMapper;
  private final OaTodoMapper oaTodoMapper;
  private final OaApprovalMapper oaApprovalMapper;
  private final ObjectMapper objectMapper;

  public ShiftSwapServiceImpl(
      ShiftSwapRequestMapper shiftSwapRequestMapper,
      StaffScheduleMapper staffScheduleMapper,
      StaffMapper staffMapper,
      OaTodoMapper oaTodoMapper,
      OaApprovalMapper oaApprovalMapper,
      ObjectMapper objectMapper) {
    this.shiftSwapRequestMapper = shiftSwapRequestMapper;
    this.staffScheduleMapper = staffScheduleMapper;
    this.staffMapper = staffMapper;
    this.oaTodoMapper = oaTodoMapper;
    this.oaApprovalMapper = oaApprovalMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public IPage<ShiftSwapRequestResponse> page(
      Long orgId, Long currentStaffId, boolean adminView, long pageNo, long pageSize, String status, boolean mineOnly) {
    var wrapper = Wrappers.lambdaQuery(ShiftSwapRequest.class)
        .eq(ShiftSwapRequest::getIsDeleted, 0)
        .eq(orgId != null, ShiftSwapRequest::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), ShiftSwapRequest::getStatus, status)
        .orderByDesc(ShiftSwapRequest::getCreateTime);
    if (!adminView || mineOnly) {
      wrapper.and(w -> w.eq(currentStaffId != null, ShiftSwapRequest::getApplicantStaffId, currentStaffId)
          .or().eq(currentStaffId != null, ShiftSwapRequest::getTargetStaffId, currentStaffId));
    }
    IPage<ShiftSwapRequest> page = shiftSwapRequestMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    IPage<ShiftSwapRequestResponse> response = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    response.setRecords(page.getRecords().stream().map(this::toResponse).toList());
    return response;
  }

  @Override
  @Transactional
  public ShiftSwapRequestResponse create(Long orgId, Long currentStaffId, ShiftSwapRequestCreateRequest request) {
    StaffSchedule applicant = staffScheduleMapper.selectById(request.getApplicantScheduleId());
    StaffSchedule target = staffScheduleMapper.selectById(request.getTargetScheduleId());
    if (applicant == null || target == null) {
      throw new IllegalArgumentException("换班排班记录不存在");
    }
    if (orgId != null && (!orgId.equals(applicant.getOrgId()) || !orgId.equals(target.getOrgId()))) {
      throw new IllegalArgumentException("排班不属于当前机构");
    }
    if (currentStaffId != null && !currentStaffId.equals(applicant.getStaffId())) {
      throw new IllegalArgumentException("只能对自己的排班发起换班");
    }
    if (applicant.getStaffId() == null || target.getStaffId() == null || applicant.getStaffId().equals(target.getStaffId())) {
      throw new IllegalArgumentException("目标排班人员无效");
    }
    StaffAccount applicantStaff = staffMapper.selectById(applicant.getStaffId());
    StaffAccount targetStaff = staffMapper.selectById(target.getStaffId());
    ShiftSwapRequest entity = new ShiftSwapRequest();
    entity.setTenantId(orgId);
    entity.setOrgId(orgId);
    entity.setApplicantStaffId(applicant.getStaffId());
    entity.setApplicantStaffName(applicantStaff == null ? null : applicantStaff.getRealName());
    entity.setApplicantScheduleId(applicant.getId());
    entity.setApplicantDutyDate(applicant.getDutyDate());
    entity.setApplicantShiftCode(applicant.getShiftCode());
    entity.setApplicantStartTime(applicant.getStartTime());
    entity.setApplicantEndTime(applicant.getEndTime());
    entity.setTargetStaffId(target.getStaffId());
    entity.setTargetStaffName(targetStaff == null ? null : targetStaff.getRealName());
    entity.setTargetScheduleId(target.getId());
    entity.setTargetDutyDate(target.getDutyDate());
    entity.setTargetShiftCode(target.getShiftCode());
    entity.setTargetStartTime(target.getStartTime());
    entity.setTargetEndTime(target.getEndTime());
    entity.setStatus("PENDING_TARGET");
    entity.setTargetConfirmStatus("PENDING");
    entity.setApprovalStatus("NOT_SUBMITTED");
    entity.setApplicantRemark(normalizeBlank(request.getApplicantRemark()));
    entity.setCreatedBy(currentStaffId);
    shiftSwapRequestMapper.insert(entity);
    createTargetConfirmTodo(entity);
    return toResponse(entity);
  }

  @Override
  @Transactional
  public ShiftSwapRequestResponse targetConfirm(Long orgId, Long currentStaffId, Long id, boolean agreed, String remark) {
    ShiftSwapRequest entity = shiftSwapRequestMapper.selectById(id);
    if (entity == null || (entity.getIsDeleted() != null && entity.getIsDeleted() == 1)
        || (orgId != null && !orgId.equals(entity.getOrgId()))) {
      throw new IllegalArgumentException("换班申请不存在");
    }
    if (currentStaffId != null && !currentStaffId.equals(entity.getTargetStaffId())) {
      throw new IllegalArgumentException("仅被换班员工可以确认");
    }
    if (!"PENDING_TARGET".equalsIgnoreCase(entity.getStatus())) {
      throw new IllegalArgumentException("当前换班申请已处理");
    }
    entity.setTargetRemark(normalizeBlank(remark));
    entity.setTargetConfirmedAt(LocalDateTime.now());
    closeTargetConfirmTodo(entity.getOrgId(), entity.getId(), agreed ? "被换班员工已同意" : "被换班员工已拒绝");
    if (!agreed) {
      entity.setTargetConfirmStatus("REJECTED");
      entity.setStatus("TARGET_REJECTED");
      entity.setApprovalStatus("CLOSED");
      shiftSwapRequestMapper.updateById(entity);
      return toResponse(entity);
    }
    entity.setTargetConfirmStatus("AGREED");
    entity.setStatus("PENDING_APPROVAL");
    entity.setApprovalSubmittedAt(LocalDateTime.now());
    OaApproval approval = createShiftSwapApproval(entity);
    entity.setApprovalId(approval.getId());
    entity.setApprovalStatus(approval.getStatus());
    shiftSwapRequestMapper.updateById(entity);
    createApprovalTodo(entity, approval);
    return toResponse(entity);
  }

  private OaApproval createShiftSwapApproval(ShiftSwapRequest entity) {
    StaffAccount applicant = entity.getApplicantStaffId() == null ? null : staffMapper.selectById(entity.getApplicantStaffId());
    Map<String, Object> form = new LinkedHashMap<>();
    form.put("scene", "shift-change");
    form.put("swapRequestId", entity.getId());
    form.put("approvalPath", List.of("申请人提交", "被换班员工确认", "人事审批", "部门主管审批", "归档"));
    form.put("currentNodeIndex", 2);
    form.put("currentNodeName", "人事审批");
    form.put("currentStep", "人事审批");
    form.put("currentApproverRole", "HR");
    form.put("currentApproverName", "人事审批人");
    form.put("workflowState", "IN_REVIEW");
    form.put("nextApprovalRequired", true);
    form.put("directLeaderId", applicant == null ? null : applicant.getDirectLeaderId());
    form.put("indirectLeaderId", applicant == null ? null : applicant.getIndirectLeaderId());
    form.put("applicantScheduleId", entity.getApplicantScheduleId());
    form.put("targetScheduleId", entity.getTargetScheduleId());
    form.put("targetStaffId", entity.getTargetStaffId());
    form.put("targetStaffName", entity.getTargetStaffName());
    form.put("applicantShiftCode", entity.getApplicantShiftCode());
    form.put("targetShiftCode", entity.getTargetShiftCode());
    form.put("applicantDutyDate", entity.getApplicantDutyDate() == null ? null : entity.getApplicantDutyDate().toString());
    form.put("targetDutyDate", entity.getTargetDutyDate() == null ? null : entity.getTargetDutyDate().toString());
    OaApproval approval = new OaApproval();
    approval.setTenantId(entity.getOrgId());
    approval.setOrgId(entity.getOrgId());
    approval.setApprovalType("SHIFT_CHANGE");
    approval.setTitle("换班申请：" + safe(entity.getApplicantStaffName()) + " ↔ " + safe(entity.getTargetStaffName()));
    approval.setApplicantId(entity.getApplicantStaffId());
    approval.setApplicantName(entity.getApplicantStaffName());
    approval.setStartTime(entity.getApplicantStartTime());
    approval.setEndTime(entity.getTargetEndTime());
    approval.setStatus("PENDING");
    approval.setRemark("换班申请，待人事/主管审批");
    approval.setFormData(writeJson(form));
    approval.setCreatedBy(entity.getApplicantStaffId());
    oaApprovalMapper.insert(approval);
    return approval;
  }

  private void createTargetConfirmTodo(ShiftSwapRequest entity) {
    OaTodo todo = new OaTodo();
    todo.setTenantId(entity.getOrgId());
    todo.setOrgId(entity.getOrgId());
    todo.setTitle("【换班确认】" + safe(entity.getApplicantStaffName()) + " 请求与你换班");
    todo.setContent("[SHIFT_SWAP_TARGET:" + entity.getId() + "] 发起人：" + safe(entity.getApplicantStaffName())
        + "；发起班次：" + safe(entity.getApplicantDutyDate()) + " " + safe(entity.getApplicantShiftCode())
        + "；目标班次：" + safe(entity.getTargetDutyDate()) + " " + safe(entity.getTargetShiftCode()));
    todo.setDueTime(entity.getTargetStartTime() == null ? LocalDateTime.now().plusHours(4) : entity.getTargetStartTime().minusHours(4));
    todo.setStatus("OPEN");
    todo.setAssigneeId(entity.getTargetStaffId());
    todo.setAssigneeName(entity.getTargetStaffName());
    todo.setCreatedBy(entity.getApplicantStaffId());
    oaTodoMapper.insert(todo);
  }

  private void createApprovalTodo(ShiftSwapRequest entity, OaApproval approval) {
    OaTodo todo = new OaTodo();
    todo.setTenantId(entity.getOrgId());
    todo.setOrgId(entity.getOrgId());
    todo.setTitle("审批待办：" + safe(approval.getTitle()));
    todo.setContent("[APPROVAL_FLOW:" + approval.getId() + "] 类型=SHIFT_CHANGE 申请人=" + safe(approval.getApplicantName())
        + " 当前节点=人事审批 审批角色=HR 审批人ID=- 节点序号=2");
    todo.setDueTime(LocalDateTime.now().plusHours(8));
    todo.setStatus("OPEN");
    todo.setAssigneeId(null);
    todo.setAssigneeName("人事审批人");
    todo.setCreatedBy(entity.getApplicantStaffId());
    oaTodoMapper.insert(todo);
  }

  private void closeTargetConfirmTodo(Long orgId, Long swapRequestId, String message) {
    List<OaTodo> todos = oaTodoMapper.selectList(Wrappers.lambdaQuery(OaTodo.class)
        .eq(OaTodo::getIsDeleted, 0)
        .eq(orgId != null, OaTodo::getOrgId, orgId)
        .eq(OaTodo::getStatus, "OPEN")
        .like(OaTodo::getContent, "[SHIFT_SWAP_TARGET:" + swapRequestId + "]"));
    for (OaTodo todo : todos) {
      todo.setStatus("DONE");
      todo.setContent(safe(todo.getContent()) + "；" + safe(message));
      oaTodoMapper.updateById(todo);
    }
  }

  private ShiftSwapRequestResponse toResponse(ShiftSwapRequest entity) {
    ShiftSwapRequestResponse response = new ShiftSwapRequestResponse();
    response.setId(entity.getId());
    response.setApplicantStaffId(entity.getApplicantStaffId());
    response.setApplicantStaffName(entity.getApplicantStaffName());
    response.setApplicantScheduleId(entity.getApplicantScheduleId());
    response.setApplicantDutyDate(entity.getApplicantDutyDate());
    response.setApplicantShiftCode(entity.getApplicantShiftCode());
    response.setApplicantStartTime(entity.getApplicantStartTime());
    response.setApplicantEndTime(entity.getApplicantEndTime());
    response.setTargetStaffId(entity.getTargetStaffId());
    response.setTargetStaffName(entity.getTargetStaffName());
    response.setTargetScheduleId(entity.getTargetScheduleId());
    response.setTargetDutyDate(entity.getTargetDutyDate());
    response.setTargetShiftCode(entity.getTargetShiftCode());
    response.setTargetStartTime(entity.getTargetStartTime());
    response.setTargetEndTime(entity.getTargetEndTime());
    response.setStatus(entity.getStatus());
    response.setTargetConfirmStatus(entity.getTargetConfirmStatus());
    response.setApprovalId(entity.getApprovalId());
    response.setApprovalStatus(entity.getApprovalStatus());
    response.setApplicantRemark(entity.getApplicantRemark());
    response.setTargetRemark(entity.getTargetRemark());
    response.setTargetConfirmedAt(entity.getTargetConfirmedAt());
    response.setApprovalSubmittedAt(entity.getApprovalSubmittedAt());
    response.setCompletedAt(entity.getCompletedAt());
    response.setCreateTime(entity.getCreateTime());
    return response;
  }

  private String normalizeBlank(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String safe(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private String writeJson(Map<String, Object> payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (Exception ex) {
      return "{}";
    }
  }
}
