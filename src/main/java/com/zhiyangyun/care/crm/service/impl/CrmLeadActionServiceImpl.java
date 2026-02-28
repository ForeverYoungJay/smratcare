package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.crm.entity.CrmCallbackPlan;
import com.zhiyangyun.care.crm.entity.CrmContractAttachment;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.entity.CrmSmsTask;
import com.zhiyangyun.care.crm.mapper.CrmCallbackPlanMapper;
import com.zhiyangyun.care.crm.mapper.CrmContractAttachmentMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.mapper.CrmSmsTaskMapper;
import com.zhiyangyun.care.crm.model.CrmLeadResponse;
import com.zhiyangyun.care.crm.model.action.CrmCallbackPlanExecuteRequest;
import com.zhiyangyun.care.crm.model.action.CrmCallbackPlanRequest;
import com.zhiyangyun.care.crm.model.action.CrmCallbackPlanResponse;
import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentRequest;
import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentResponse;
import com.zhiyangyun.care.crm.model.action.CrmLeadBatchDeleteRequest;
import com.zhiyangyun.care.crm.model.action.CrmLeadBatchStatusRequest;
import com.zhiyangyun.care.crm.model.action.CrmSmsTaskCreateRequest;
import com.zhiyangyun.care.crm.model.action.CrmSmsTaskResponse;
import com.zhiyangyun.care.crm.service.CrmLeadActionService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrmLeadActionServiceImpl implements CrmLeadActionService {
  private final CrmLeadMapper leadMapper;
  private final CrmCallbackPlanMapper callbackPlanMapper;
  private final CrmContractAttachmentMapper attachmentMapper;
  private final CrmSmsTaskMapper smsTaskMapper;

  public CrmLeadActionServiceImpl(
      CrmLeadMapper leadMapper,
      CrmCallbackPlanMapper callbackPlanMapper,
      CrmContractAttachmentMapper attachmentMapper,
      CrmSmsTaskMapper smsTaskMapper) {
    this.leadMapper = leadMapper;
    this.callbackPlanMapper = callbackPlanMapper;
    this.attachmentMapper = attachmentMapper;
    this.smsTaskMapper = smsTaskMapper;
  }

  @Override
  @Transactional
  public int batchUpdateStatus(Long tenantId, CrmLeadBatchStatusRequest request) {
    if (request == null || request.getIds() == null || request.getIds().isEmpty() || request.getStatus() == null) {
      return 0;
    }
    int updated = 0;
    for (Long id : request.getIds()) {
      CrmLead lead = leadMapper.selectById(id);
      if (!isOwnedLead(lead, tenantId)) {
        continue;
      }
      lead.setStatus(request.getStatus());
      if (request.getFollowupStatus() != null && !request.getFollowupStatus().isBlank()) {
        lead.setFollowupStatus(request.getFollowupStatus().trim());
      }
      if (request.getStatus() == 3) {
        lead.setInvalidTime(parseDateTime(request.getInvalidTime()));
        if (lead.getInvalidTime() == null) {
          lead.setInvalidTime(LocalDateTime.now());
        }
      }
      if (request.getStatus() == 1) {
        lead.setInvalidTime(null);
      }
      leadMapper.updateById(lead);
      updated++;
    }
    return updated;
  }

  @Override
  @Transactional
  public int batchDelete(Long tenantId, CrmLeadBatchDeleteRequest request) {
    if (request == null) {
      return 0;
    }
    List<Long> ids = request.getIds();
    List<String> contractNos = request.getContractNos();
    boolean hasIds = ids != null && !ids.isEmpty();
    boolean hasContractNos = contractNos != null && contractNos.stream().anyMatch(item -> item != null && !item.isBlank());
    if (!hasIds && !hasContractNos) {
      return 0;
    }
    int updated = 0;
    if (hasIds) {
      for (Long id : ids) {
        CrmLead lead = leadMapper.selectById(id);
        if (!isOwnedLead(lead, tenantId)) {
          continue;
        }
        lead.setIsDeleted(1);
        leadMapper.updateById(lead);
        updated++;
      }
    }
    if (hasContractNos) {
      for (String contractNo : contractNos) {
        if (contractNo == null || contractNo.isBlank()) {
          continue;
        }
        CrmLead lead = leadMapper.selectOne(Wrappers.lambdaQuery(CrmLead.class)
            .eq(CrmLead::getTenantId, tenantId)
            .eq(CrmLead::getIsDeleted, 0)
            .eq(CrmLead::getContractNo, contractNo.trim())
            .orderByDesc(CrmLead::getUpdateTime)
            .last("LIMIT 1"));
        if (lead == null) {
          continue;
        }
        if (Integer.valueOf(1).equals(lead.getIsDeleted())) {
          continue;
        }
        lead.setIsDeleted(1);
        leadMapper.updateById(lead);
        updated++;
      }
    }
    return updated;
  }

  @Override
  @Transactional
  public CrmLeadResponse handoffToAssessment(Long tenantId, String contractNo) {
    if (tenantId == null || contractNo == null || contractNo.isBlank()) {
      return null;
    }
    CrmLead lead = leadMapper.selectOne(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getTenantId, tenantId)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(CrmLead::getContractNo, contractNo.trim())
        .orderByDesc(CrmLead::getUpdateTime)
        .last("LIMIT 1"));
    if (!isOwnedLead(lead, tenantId)) {
      return null;
    }
    lead.setStatus(2);
    lead.setFlowStage("PENDING_ASSESSMENT");
    lead.setCurrentOwnerDept("ASSESSMENT");
    lead.setContractStatus("待评估");
    leadMapper.updateById(lead);
    return toLeadResponse(lead);
  }

  @Override
  @Transactional
  public CrmCallbackPlanResponse createCallbackPlan(
      Long tenantId, Long orgId, Long staffId, Long leadId, CrmCallbackPlanRequest request) {
    CrmLead lead = leadMapper.selectById(leadId);
    if (!isOwnedLead(lead, tenantId)) {
      return null;
    }
    CrmCallbackPlan plan = new CrmCallbackPlan();
    plan.setTenantId(tenantId);
    plan.setOrgId(orgId);
    plan.setLeadId(leadId);
    plan.setTitle(blankToDefault(request == null ? null : request.getTitle(), "回访"));
    plan.setFollowupContent(blankToNull(request == null ? null : request.getFollowupContent()));
    plan.setPlanExecuteTime(parseDateTime(request == null ? null : request.getPlanExecuteTime()));
    if (plan.getPlanExecuteTime() == null) {
      plan.setPlanExecuteTime(LocalDateTime.now());
    }
    plan.setExecutorName(blankToNull(request == null ? null : request.getExecutorName()));
    plan.setStatus("PENDING");
    plan.setCreatedBy(staffId);
    callbackPlanMapper.insert(plan);

    lead.setNextFollowDate(plan.getPlanExecuteTime().toLocalDate());
    lead.setFollowupStatus("未回访");
    leadMapper.updateById(lead);
    return toResponse(plan);
  }

  @Override
  public List<CrmCallbackPlanResponse> listCallbackPlan(Long tenantId, Long leadId) {
    if (leadId == null) {
      return Collections.emptyList();
    }
    List<CrmCallbackPlan> list = callbackPlanMapper.selectList(Wrappers.lambdaQuery(CrmCallbackPlan.class)
        .eq(CrmCallbackPlan::getIsDeleted, 0)
        .eq(CrmCallbackPlan::getTenantId, tenantId)
        .eq(CrmCallbackPlan::getLeadId, leadId)
        .orderByDesc(CrmCallbackPlan::getPlanExecuteTime));
    return list.stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional
  public CrmCallbackPlanResponse executeCallbackPlan(Long tenantId, Long planId, CrmCallbackPlanExecuteRequest request) {
    CrmCallbackPlan plan = callbackPlanMapper.selectById(planId);
    if (plan == null || !tenantId.equals(plan.getTenantId()) || Integer.valueOf(1).equals(plan.getIsDeleted())) {
      return null;
    }
    plan.setStatus("DONE");
    plan.setExecutedTime(LocalDateTime.now());
    plan.setExecuteNote(blankToNull(request == null ? null : request.getExecuteNote()));
    plan.setFollowupResult(blankToNull(request == null ? null : request.getFollowupResult()));
    callbackPlanMapper.updateById(plan);

    CrmLead lead = leadMapper.selectById(plan.getLeadId());
    if (isOwnedLead(lead, tenantId)) {
      lead.setFollowupStatus("已回访");
      LocalDate nextDate = parseDate(request == null ? null : request.getNextFollowDate());
      lead.setNextFollowDate(nextDate);
      leadMapper.updateById(lead);
    }
    return toResponse(plan);
  }

  @Override
  public CrmContractAttachmentResponse createAttachment(
      Long tenantId, Long orgId, Long staffId, Long leadId, CrmContractAttachmentRequest request) {
    CrmLead lead = leadMapper.selectById(leadId);
    if (!isOwnedLead(lead, tenantId)) {
      return null;
    }
    String fileName = blankToDefault(request == null ? null : request.getFileName(), "未命名附件");
    List<CrmContractAttachment> existingAttachments = attachmentMapper.selectList(Wrappers.lambdaQuery(CrmContractAttachment.class)
        .eq(CrmContractAttachment::getIsDeleted, 0)
        .eq(CrmContractAttachment::getTenantId, tenantId)
        .eq(CrmContractAttachment::getLeadId, leadId));
    boolean duplicated = existingAttachments.stream()
        .map(CrmContractAttachment::getFileName)
        .filter(item -> item != null && !item.isBlank())
        .anyMatch(item -> item.trim().equalsIgnoreCase(fileName));
    if (duplicated) {
      throw new IllegalArgumentException("附件重名，请修改文件名后重试");
    }

    CrmContractAttachment attachment = new CrmContractAttachment();
    attachment.setTenantId(tenantId);
    attachment.setOrgId(orgId);
    attachment.setLeadId(leadId);
    attachment.setContractNo(blankToDefault(request == null ? null : request.getContractNo(), lead.getContractNo()));
    attachment.setFileName(fileName);
    attachment.setFileUrl(blankToNull(request == null ? null : request.getFileUrl()));
    attachment.setFileType(blankToNull(request == null ? null : request.getFileType()));
    attachment.setFileSize(request == null ? null : request.getFileSize());
    attachment.setRemark(blankToNull(request == null ? null : request.getRemark()));
    attachment.setCreatedBy(staffId);
    attachmentMapper.insert(attachment);
    return toResponse(attachment);
  }

  @Override
  public List<CrmContractAttachmentResponse> listAttachment(Long tenantId, Long leadId) {
    if (leadId == null) {
      return Collections.emptyList();
    }
    List<CrmContractAttachment> list = attachmentMapper.selectList(Wrappers.lambdaQuery(CrmContractAttachment.class)
        .eq(CrmContractAttachment::getIsDeleted, 0)
        .eq(CrmContractAttachment::getTenantId, tenantId)
        .eq(CrmContractAttachment::getLeadId, leadId)
        .orderByDesc(CrmContractAttachment::getCreateTime));
    return list.stream().map(this::toResponse).toList();
  }

  @Override
  public void deleteAttachment(Long tenantId, Long attachmentId) {
    CrmContractAttachment attachment = attachmentMapper.selectById(attachmentId);
    if (attachment == null || !tenantId.equals(attachment.getTenantId())) {
      return;
    }
    attachment.setIsDeleted(1);
    attachmentMapper.updateById(attachment);
  }

  @Override
  @Transactional
  public List<CrmSmsTaskResponse> createSmsTasks(
      Long tenantId, Long orgId, Long staffId, CrmSmsTaskCreateRequest request) {
    if (request == null || request.getLeadIds() == null || request.getLeadIds().isEmpty()) {
      return Collections.emptyList();
    }
    List<CrmSmsTaskResponse> result = new ArrayList<>();
    for (Long leadId : request.getLeadIds()) {
      CrmLead lead = leadMapper.selectById(leadId);
      if (!isOwnedLead(lead, tenantId)) {
        continue;
      }
      CrmSmsTask task = new CrmSmsTask();
      task.setTenantId(tenantId);
      task.setOrgId(orgId);
      task.setLeadId(leadId);
      task.setPhone(blankToNull(lead.getElderPhone()) == null ? lead.getPhone() : lead.getElderPhone());
      task.setTemplateName(blankToDefault(request.getTemplateName(), "合同到期提醒"));
      task.setContent(blankToDefault(request.getContent(), "请关注合同到期续签。"));
      task.setPlanSendTime(parseDateTime(request.getPlanSendTime()));
      if (task.getPlanSendTime() == null) {
        task.setPlanSendTime(LocalDateTime.now());
      }
      task.setStatus("SCHEDULED");
      task.setCreatedBy(staffId);
      smsTaskMapper.insert(task);
      result.add(toResponse(task));
    }
    return result;
  }

  @Override
  public List<CrmSmsTaskResponse> listSmsTasks(Long tenantId, Long leadId) {
    List<CrmSmsTask> list = smsTaskMapper.selectList(Wrappers.lambdaQuery(CrmSmsTask.class)
        .eq(CrmSmsTask::getIsDeleted, 0)
        .eq(CrmSmsTask::getTenantId, tenantId)
        .eq(leadId != null, CrmSmsTask::getLeadId, leadId)
        .orderByDesc(CrmSmsTask::getCreateTime));
    return list.stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional
  public CrmSmsTaskResponse sendSmsTask(Long tenantId, Long taskId) {
    CrmSmsTask task = smsTaskMapper.selectById(taskId);
    if (task == null || !tenantId.equals(task.getTenantId()) || Integer.valueOf(1).equals(task.getIsDeleted())) {
      return null;
    }
    task.setStatus("SENT");
    task.setSendTime(LocalDateTime.now());
    task.setResultMessage("发送成功");
    smsTaskMapper.updateById(task);

    CrmLead lead = leadMapper.selectById(task.getLeadId());
    if (isOwnedLead(lead, tenantId)) {
      lead.setSmsSendCount(lead.getSmsSendCount() == null ? 1 : lead.getSmsSendCount() + 1);
      leadMapper.updateById(lead);
    }
    return toResponse(task);
  }

  private boolean isOwnedLead(CrmLead lead, Long tenantId) {
    return lead != null && tenantId != null && tenantId.equals(lead.getTenantId()) && !Integer.valueOf(1).equals(lead.getIsDeleted());
  }

  private CrmCallbackPlanResponse toResponse(CrmCallbackPlan plan) {
    CrmCallbackPlanResponse response = new CrmCallbackPlanResponse();
    response.setId(plan.getId());
    response.setLeadId(plan.getLeadId());
    response.setTitle(plan.getTitle());
    response.setFollowupContent(plan.getFollowupContent());
    response.setPlanExecuteTime(plan.getPlanExecuteTime());
    response.setExecutorName(plan.getExecutorName());
    response.setStatus(plan.getStatus());
    response.setExecutedTime(plan.getExecutedTime());
    response.setExecuteNote(plan.getExecuteNote());
    response.setFollowupResult(plan.getFollowupResult());
    response.setCreateTime(plan.getCreateTime());
    return response;
  }

  private CrmContractAttachmentResponse toResponse(CrmContractAttachment attachment) {
    CrmContractAttachmentResponse response = new CrmContractAttachmentResponse();
    response.setId(attachment.getId());
    response.setLeadId(attachment.getLeadId());
    response.setContractNo(attachment.getContractNo());
    response.setFileName(attachment.getFileName());
    response.setFileUrl(attachment.getFileUrl());
    response.setFileType(attachment.getFileType());
    response.setFileSize(attachment.getFileSize());
    response.setRemark(attachment.getRemark());
    response.setCreateTime(attachment.getCreateTime());
    return response;
  }

  private CrmSmsTaskResponse toResponse(CrmSmsTask task) {
    CrmSmsTaskResponse response = new CrmSmsTaskResponse();
    response.setId(task.getId());
    response.setLeadId(task.getLeadId());
    response.setPhone(task.getPhone());
    response.setTemplateName(task.getTemplateName());
    response.setContent(task.getContent());
    response.setPlanSendTime(task.getPlanSendTime());
    response.setStatus(task.getStatus());
    response.setSendTime(task.getSendTime());
    response.setResultMessage(task.getResultMessage());
    response.setCreateTime(task.getCreateTime());
    return response;
  }

  private CrmLeadResponse toLeadResponse(CrmLead lead) {
    CrmLeadResponse response = new CrmLeadResponse();
    response.setId(lead.getId());
    response.setTenantId(lead.getTenantId());
    response.setOrgId(lead.getOrgId());
    response.setName(lead.getName());
    response.setPhone(lead.getPhone());
    response.setElderName(lead.getElderName());
    response.setElderPhone(lead.getElderPhone());
    response.setGender(lead.getGender());
    response.setAge(lead.getAge());
    response.setMarketerName(lead.getMarketerName());
    response.setIdCardNo(lead.getIdCardNo());
    response.setHomeAddress(lead.getHomeAddress());
    response.setOrgName(lead.getOrgName());
    response.setStatus(lead.getStatus());
    response.setContractNo(lead.getContractNo());
    response.setContractStatus(lead.getContractStatus());
    response.setFlowStage(lead.getFlowStage());
    response.setCurrentOwnerDept(lead.getCurrentOwnerDept());
    response.setContractSignedAt(lead.getContractSignedAt());
    response.setContractExpiryDate(lead.getContractExpiryDate() == null ? null : lead.getContractExpiryDate().toString());
    response.setRemark(lead.getRemark());
    response.setCreateTime(lead.getCreateTime());
    response.setUpdateTime(lead.getUpdateTime());
    return response;
  }

  private String blankToDefault(String value, String defaultValue) {
    String cleaned = blankToNull(value);
    return cleaned == null ? defaultValue : cleaned;
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private LocalDate parseDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(value.trim());
    } catch (DateTimeParseException ignored) {
      return null;
    }
  }

  private LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String text = value.trim();
    try {
      return LocalDateTime.parse(text);
    } catch (DateTimeParseException ignored) {
    }
    try {
      return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    } catch (DateTimeParseException ignored) {
    }
    try {
      return LocalDate.parse(text).atStartOfDay();
    } catch (DateTimeParseException ignored) {
      return null;
    }
  }
}
