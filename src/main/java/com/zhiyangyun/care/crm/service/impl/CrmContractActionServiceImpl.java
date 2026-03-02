package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.entity.CrmContractAttachment;
import com.zhiyangyun.care.crm.entity.CrmSmsTask;
import com.zhiyangyun.care.crm.mapper.CrmContractAttachmentMapper;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.mapper.CrmSmsTaskMapper;
import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentRequest;
import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentResponse;
import com.zhiyangyun.care.crm.model.action.CrmSmsTaskCreateRequest;
import com.zhiyangyun.care.crm.model.action.CrmSmsTaskResponse;
import com.zhiyangyun.care.crm.service.CrmContractActionService;
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
public class CrmContractActionServiceImpl implements CrmContractActionService {
  private final CrmContractMapper contractMapper;
  private final CrmContractAttachmentMapper attachmentMapper;
  private final CrmSmsTaskMapper smsTaskMapper;
  private final CrmLeadMapper leadMapper;

  public CrmContractActionServiceImpl(
      CrmContractMapper contractMapper,
      CrmContractAttachmentMapper attachmentMapper,
      CrmSmsTaskMapper smsTaskMapper,
      CrmLeadMapper leadMapper) {
    this.contractMapper = contractMapper;
    this.attachmentMapper = attachmentMapper;
    this.smsTaskMapper = smsTaskMapper;
    this.leadMapper = leadMapper;
  }

  @Override
  @Transactional
  public CrmContractAttachmentResponse createAttachment(
      Long tenantId, Long orgId, Long staffId, Long contractId, CrmContractAttachmentRequest request) {
    CrmContract contract = contractMapper.selectById(contractId);
    if (!isOwnedContract(contract, tenantId)) {
      return null;
    }
    String fileName = blankToDefault(request == null ? null : request.getFileName(), "未命名附件");
    List<CrmContractAttachment> existing = attachmentMapper.selectList(Wrappers.lambdaQuery(CrmContractAttachment.class)
        .eq(CrmContractAttachment::getIsDeleted, 0)
        .eq(CrmContractAttachment::getTenantId, tenantId)
        .eq(CrmContractAttachment::getContractId, contractId));
    boolean duplicated = existing.stream()
        .map(CrmContractAttachment::getFileName)
        .filter(item -> item != null && !item.isBlank())
        .anyMatch(item -> item.trim().equalsIgnoreCase(fileName));
    if (duplicated) {
      throw new IllegalArgumentException("附件重名，请修改文件名后重试");
    }

    CrmContractAttachment attachment = new CrmContractAttachment();
    attachment.setTenantId(tenantId);
    attachment.setOrgId(orgId);
    attachment.setLeadId(contract.getLeadId());
    attachment.setContractId(contract.getId());
    attachment.setContractNo(contract.getContractNo());
    attachment.setAttachmentType(blankToDefault(request == null ? null : request.getAttachmentType(), "CONTRACT"));
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
  public List<CrmContractAttachmentResponse> listAttachment(Long tenantId, Long contractId) {
    if (contractId == null) {
      return Collections.emptyList();
    }
    List<CrmContractAttachment> list = attachmentMapper.selectList(Wrappers.lambdaQuery(CrmContractAttachment.class)
        .eq(CrmContractAttachment::getIsDeleted, 0)
        .eq(CrmContractAttachment::getTenantId, tenantId)
        .eq(CrmContractAttachment::getContractId, contractId)
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
  public List<CrmSmsTaskResponse> createSmsTasks(Long tenantId, Long orgId, Long staffId, CrmSmsTaskCreateRequest request) {
    if (request == null) {
      return Collections.emptyList();
    }
    List<Long> contractIds = request.getContractIds();
    if ((contractIds == null || contractIds.isEmpty()) && request.getLeadIds() != null && !request.getLeadIds().isEmpty()) {
      contractIds = request.getLeadIds().stream().map(leadId -> {
        CrmContract contract = contractMapper.selectOne(Wrappers.lambdaQuery(CrmContract.class)
            .eq(CrmContract::getTenantId, tenantId)
            .eq(CrmContract::getIsDeleted, 0)
            .eq(CrmContract::getLeadId, leadId)
            .orderByDesc(CrmContract::getUpdateTime)
            .last("LIMIT 1"));
        return contract == null ? null : contract.getId();
      }).filter(item -> item != null).toList();
    }
    if (contractIds == null || contractIds.isEmpty()) {
      return Collections.emptyList();
    }
    List<CrmSmsTaskResponse> result = new ArrayList<>();
    for (Long contractId : contractIds) {
      CrmContract contract = contractMapper.selectById(contractId);
      if (!isOwnedContract(contract, tenantId)) {
        continue;
      }
      CrmSmsTask task = new CrmSmsTask();
      task.setTenantId(tenantId);
      task.setOrgId(orgId);
      task.setLeadId(contract.getLeadId());
      task.setContractId(contractId);
      task.setPhone(contract.getElderPhone());
      task.setTemplateName(blankToDefault(request.getTemplateName(), "合同到期提醒"));
      task.setContent(blankToDefault(request.getContent(), "请关注合同到期续签。"));
      task.setPlanSendTime(parseDateTime(request.getPlanSendTime()));
      if (task.getPlanSendTime() == null) {
        task.setPlanSendTime(LocalDateTime.now());
      }
      task.setStatus("SCHEDULED");
      task.setCreatedBy(staffId);
      smsTaskMapper.insert(task);

      contract.setSmsSendCount(contract.getSmsSendCount() == null ? 1 : contract.getSmsSendCount() + 1);
      contractMapper.updateById(contract);
      if (contract.getLeadId() != null) {
        leadMapper.update(null, Wrappers.lambdaUpdate(com.zhiyangyun.care.crm.entity.CrmLead.class)
            .eq(com.zhiyangyun.care.crm.entity.CrmLead::getId, contract.getLeadId())
            .set(com.zhiyangyun.care.crm.entity.CrmLead::getSmsSendCount, contract.getSmsSendCount()));
      }
      result.add(toResponse(task));
    }
    return result;
  }

  @Override
  public List<CrmSmsTaskResponse> listSmsTasks(Long tenantId, Long contractId) {
    List<CrmSmsTask> list = smsTaskMapper.selectList(Wrappers.lambdaQuery(CrmSmsTask.class)
        .eq(CrmSmsTask::getIsDeleted, 0)
        .eq(CrmSmsTask::getTenantId, tenantId)
        .eq(contractId != null, CrmSmsTask::getContractId, contractId)
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
    task.setResultMessage("mock:发送成功");
    smsTaskMapper.updateById(task);
    return toResponse(task);
  }

  private boolean isOwnedContract(CrmContract contract, Long tenantId) {
    return contract != null && Integer.valueOf(0).equals(contract.getIsDeleted()) && tenantId.equals(contract.getTenantId());
  }

  private CrmContractAttachmentResponse toResponse(CrmContractAttachment attachment) {
    CrmContractAttachmentResponse response = new CrmContractAttachmentResponse();
    response.setId(attachment.getId());
    response.setLeadId(attachment.getLeadId());
    response.setContractId(attachment.getContractId());
    response.setContractNo(attachment.getContractNo());
    response.setAttachmentType(attachment.getAttachmentType());
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
    response.setContractId(task.getContractId());
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

  private LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String text = value.trim();
    try {
      if (text.length() == 10) {
        return LocalDate.parse(text).atStartOfDay();
      }
      return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private String blankToDefault(String value, String defaultValue) {
    String trimmed = blankToNull(value);
    return trimmed == null ? defaultValue : trimmed;
  }
}
