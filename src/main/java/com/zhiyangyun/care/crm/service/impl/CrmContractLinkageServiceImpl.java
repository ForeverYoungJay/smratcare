package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.assessment.entity.AssessmentRecord;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.crm.entity.CrmContractAttachment;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmContractAttachmentMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.model.CrmContractAssessmentContractItem;
import com.zhiyangyun.care.crm.model.CrmContractAssessmentOverviewResponse;
import com.zhiyangyun.care.crm.model.CrmContractAssessmentReportItem;
import com.zhiyangyun.care.crm.model.CrmContractLinkageResponse;
import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentResponse;
import com.zhiyangyun.care.crm.service.CrmContractLinkageService;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CrmContractLinkageServiceImpl implements CrmContractLinkageService {
  private final CrmLeadMapper leadMapper;
  private final CrmContractAttachmentMapper attachmentMapper;
  private final ElderMapper elderMapper;
  private final ElderAdmissionMapper admissionMapper;
  private final BillMonthlyMapper billMonthlyMapper;
  private final AssessmentRecordMapper assessmentRecordMapper;

  public CrmContractLinkageServiceImpl(
      CrmLeadMapper leadMapper,
      CrmContractAttachmentMapper attachmentMapper,
      ElderMapper elderMapper,
      ElderAdmissionMapper admissionMapper,
      BillMonthlyMapper billMonthlyMapper,
      AssessmentRecordMapper assessmentRecordMapper) {
    this.leadMapper = leadMapper;
    this.attachmentMapper = attachmentMapper;
    this.elderMapper = elderMapper;
    this.admissionMapper = admissionMapper;
    this.billMonthlyMapper = billMonthlyMapper;
    this.assessmentRecordMapper = assessmentRecordMapper;
  }

  @Override
  public CrmContractLinkageResponse getByElderId(Long tenantId, Long elderId) {
    if (tenantId == null || elderId == null) {
      return null;
    }

    ElderProfile elder = elderMapper.selectById(elderId);
    if (elder == null || Integer.valueOf(1).equals(elder.getIsDeleted()) || !tenantId.equals(elder.getTenantId())) {
      return null;
    }

    ElderAdmission admission = loadLatestAdmission(tenantId, elderId);
    CrmLead lead = loadLinkedLead(tenantId, elder, admission == null ? null : admission.getContractNo());

    CrmContractLinkageResponse response = new CrmContractLinkageResponse();
    response.setElderId(elderId);
    response.setElderName(elder.getFullName());
    response.setElderPhone(elder.getPhone());

    if (admission != null) {
      response.setContractNo(admission.getContractNo());
      response.setAdmissionDate(admission.getAdmissionDate());
      response.setDepositAmount(admission.getDepositAmount());
    }
    if (lead != null) {
      response.setLeadId(lead.getId());
      response.setOrgName(lead.getOrgName());
      response.setMarketerName(lead.getMarketerName());
      response.setContractNo(firstNonBlank(response.getContractNo(), lead.getContractNo()));
      response.setContractStatus(lead.getContractStatus());
      response.setContractSignedAt(lead.getContractSignedAt());
      response.setContractExpiryDate(lead.getContractExpiryDate());
    }

    fillBillingSummary(response, elderId, tenantId);
    fillAttachments(response, tenantId, lead == null ? null : lead.getId(), response.getContractNo());
    return response;
  }

  @Override
  public CrmContractLinkageResponse getByLeadId(Long tenantId, Long leadId) {
    if (tenantId == null || leadId == null) {
      return null;
    }
    CrmLead lead = leadMapper.selectById(leadId);
    if (lead == null || Integer.valueOf(1).equals(lead.getIsDeleted()) || !tenantId.equals(lead.getTenantId())) {
      return null;
    }

    ElderProfile elder = findElderByLead(tenantId, lead);
    ElderAdmission admission = elder == null ? null : loadLatestAdmission(tenantId, elder.getId());

    CrmContractLinkageResponse response = new CrmContractLinkageResponse();
    response.setLeadId(lead.getId());
    response.setContractNo(lead.getContractNo());
    response.setContractStatus(lead.getContractStatus());
    response.setContractSignedAt(lead.getContractSignedAt());
    response.setContractExpiryDate(lead.getContractExpiryDate());
    response.setOrgName(lead.getOrgName());
    response.setMarketerName(lead.getMarketerName());
    response.setElderName(firstNonBlank(lead.getElderName(), lead.getName()));
    response.setElderPhone(firstNonBlank(lead.getElderPhone(), lead.getPhone()));

    if (elder != null) {
      response.setElderId(elder.getId());
      response.setElderName(elder.getFullName());
      response.setElderPhone(elder.getPhone());
      fillBillingSummary(response, elder.getId(), tenantId);
    }
    if (admission != null) {
      response.setAdmissionDate(admission.getAdmissionDate());
      response.setDepositAmount(admission.getDepositAmount());
      response.setContractNo(firstNonBlank(response.getContractNo(), admission.getContractNo()));
    }

    fillAttachments(response, tenantId, leadId, response.getContractNo());
    return response;
  }

  @Override
  public CrmContractAssessmentOverviewResponse getAssessmentOverview(Long tenantId, Long elderId, Long leadId) {
    if (tenantId == null) {
      return null;
    }
    ElderProfile elder = resolveElderForOverview(tenantId, elderId, leadId);
    CrmContractAssessmentOverviewResponse response = new CrmContractAssessmentOverviewResponse();
    if (elder == null) {
      return response;
    }
    response.setElderId(elder.getId());
    response.setElderName(elder.getFullName());
    response.setElderPhone(elder.getPhone());

    List<CrmLead> contracts = listContractsByElder(tenantId, elder);
    List<CrmContractAssessmentContractItem> contractItems = contracts.stream()
        .map(this::toContractItem)
        .toList();
    response.setContracts(contractItems);
    response.setTotalContractCount(contractItems.size());

    List<AssessmentRecord> records = assessmentRecordMapper.selectList(Wrappers.lambdaQuery(AssessmentRecord.class)
        .eq(AssessmentRecord::getIsDeleted, 0)
        .eq(AssessmentRecord::getOrgId, tenantId)
        .eq(AssessmentRecord::getElderId, elder.getId())
        .eq(AssessmentRecord::getAssessmentType, "ADMISSION")
        .orderByDesc(AssessmentRecord::getAssessmentDate)
        .orderByDesc(AssessmentRecord::getUpdateTime));
    response.setTotalReportCount(records.size());
    attachReportsByContract(contractItems, records, response);
    return response;
  }

  private ElderAdmission loadLatestAdmission(Long tenantId, Long elderId) {
    return admissionMapper.selectOne(Wrappers.lambdaQuery(ElderAdmission.class)
        .eq(ElderAdmission::getTenantId, tenantId)
        .eq(ElderAdmission::getElderId, elderId)
        .eq(ElderAdmission::getIsDeleted, 0)
        .orderByDesc(ElderAdmission::getAdmissionDate)
        .orderByDesc(ElderAdmission::getCreateTime)
        .last("LIMIT 1"));
  }

  private CrmLead loadLinkedLead(Long tenantId, ElderProfile elder, String contractNo) {
    if (contractNo != null && !contractNo.isBlank()) {
      CrmLead byContract = leadMapper.selectOne(Wrappers.lambdaQuery(CrmLead.class)
          .eq(CrmLead::getTenantId, tenantId)
          .eq(CrmLead::getIsDeleted, 0)
          .eq(CrmLead::getContractNo, contractNo)
          .orderByDesc(CrmLead::getContractSignedAt)
          .orderByDesc(CrmLead::getCreateTime)
          .last("LIMIT 1"));
      if (byContract != null) {
        return byContract;
      }
    }
    return findElderLeadFallback(tenantId, elder);
  }

  private CrmLead findElderLeadFallback(Long tenantId, ElderProfile elder) {
    if (elder == null) {
      return null;
    }
    if (elder.getIdCardNo() != null && !elder.getIdCardNo().isBlank()) {
      CrmLead byIdCard = leadMapper.selectOne(Wrappers.lambdaQuery(CrmLead.class)
          .eq(CrmLead::getTenantId, tenantId)
          .eq(CrmLead::getIsDeleted, 0)
          .eq(CrmLead::getIdCardNo, elder.getIdCardNo())
          .orderByDesc(CrmLead::getContractSignedAt)
          .orderByDesc(CrmLead::getCreateTime)
          .last("LIMIT 1"));
      if (byIdCard != null) {
        return byIdCard;
      }
    }
    if (elder.getFullName() == null || elder.getPhone() == null) {
      return null;
    }
    return leadMapper.selectOne(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getTenantId, tenantId)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(CrmLead::getElderName, elder.getFullName())
        .eq(CrmLead::getElderPhone, elder.getPhone())
        .orderByDesc(CrmLead::getContractSignedAt)
        .orderByDesc(CrmLead::getCreateTime)
        .last("LIMIT 1"));
  }

  private ElderProfile findElderByLead(Long tenantId, CrmLead lead) {
    if (lead == null) {
      return null;
    }
    if (lead.getIdCardNo() != null && !lead.getIdCardNo().isBlank()) {
      ElderProfile byIdCard = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
          .eq(ElderProfile::getTenantId, tenantId)
          .eq(ElderProfile::getIsDeleted, 0)
          .eq(ElderProfile::getIdCardNo, lead.getIdCardNo())
          .last("LIMIT 1"));
      if (byIdCard != null) {
        return byIdCard;
      }
    }
    String elderName = firstNonBlank(lead.getElderName(), lead.getName());
    String elderPhone = firstNonBlank(lead.getElderPhone(), lead.getPhone());
    if (elderName == null || elderPhone == null) {
      return null;
    }
    return elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getTenantId, tenantId)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(ElderProfile::getFullName, elderName)
        .eq(ElderProfile::getPhone, elderPhone)
        .orderByDesc(ElderProfile::getAdmissionDate)
        .orderByDesc(ElderProfile::getCreateTime)
        .last("LIMIT 1"));
  }

  private ElderProfile resolveElderForOverview(Long tenantId, Long elderId, Long leadId) {
    if (elderId != null) {
      ElderProfile elder = elderMapper.selectById(elderId);
      if (elder != null && Integer.valueOf(0).equals(elder.getIsDeleted()) && tenantId.equals(elder.getTenantId())) {
        return elder;
      }
    }
    if (leadId != null) {
      CrmLead lead = leadMapper.selectById(leadId);
      if (!isOwnedLead(lead, tenantId)) {
        return null;
      }
      return findElderByLead(tenantId, lead);
    }
    return null;
  }

  private boolean isOwnedLead(CrmLead lead, Long tenantId) {
    return lead != null && Integer.valueOf(0).equals(lead.getIsDeleted()) && tenantId.equals(lead.getTenantId());
  }

  private List<CrmLead> listContractsByElder(Long tenantId, ElderProfile elder) {
    if (elder == null) {
      return Collections.emptyList();
    }
    var wrapper = Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getTenantId, tenantId)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(CrmLead::getStatus, 2)
        .orderByDesc(CrmLead::getContractSignedAt)
        .orderByDesc(CrmLead::getCreateTime);
    if (elder.getIdCardNo() != null && !elder.getIdCardNo().isBlank()) {
      wrapper.and(w -> w.eq(CrmLead::getIdCardNo, elder.getIdCardNo())
          .or(p -> p.eq(CrmLead::getElderName, elder.getFullName())
              .eq(CrmLead::getElderPhone, elder.getPhone())));
    } else {
      wrapper.eq(CrmLead::getElderName, elder.getFullName())
          .eq(CrmLead::getElderPhone, elder.getPhone());
    }
    List<CrmLead> list = leadMapper.selectList(wrapper);
    if (list == null || list.isEmpty()) {
      return Collections.emptyList();
    }
    LinkedHashMap<String, CrmLead> byContract = new LinkedHashMap<>();
    for (CrmLead lead : list) {
      if (lead.getContractNo() == null || lead.getContractNo().isBlank()) {
        continue;
      }
      byContract.putIfAbsent(lead.getContractNo(), lead);
    }
    return byContract.values().stream().toList();
  }

  private CrmContractAssessmentContractItem toContractItem(CrmLead lead) {
    CrmContractAssessmentContractItem item = new CrmContractAssessmentContractItem();
    item.setLeadId(lead.getId());
    item.setContractNo(lead.getContractNo());
    item.setContractStatus(lead.getContractStatus());
    item.setFlowStage(lead.getFlowStage());
    item.setCurrentOwnerDept(lead.getCurrentOwnerDept());
    item.setMarketerName(lead.getMarketerName());
    item.setOrgName(lead.getOrgName());
    item.setContractSignedAt(lead.getContractSignedAt());
    item.setContractExpiryDate(lead.getContractExpiryDate());
    return item;
  }

  private void attachReportsByContract(
      List<CrmContractAssessmentContractItem> contracts,
      List<AssessmentRecord> records,
      CrmContractAssessmentOverviewResponse response) {
    if (records == null || records.isEmpty()) {
      return;
    }
    if (contracts == null || contracts.isEmpty()) {
      response.setUnassignedReports(records.stream().map(this::toReportItem).toList());
      return;
    }
    List<CrmContractAssessmentContractItem> sortedContracts = contracts.stream()
        .sorted(Comparator.comparing(this::contractSignedDateSafe))
        .toList();
    for (AssessmentRecord record : records) {
      CrmContractAssessmentContractItem matched = findMatchingContract(sortedContracts, record.getAssessmentDate());
      if (matched == null) {
        response.getUnassignedReports().add(toReportItem(record));
        continue;
      }
      matched.getReports().add(toReportItem(record));
    }
    for (CrmContractAssessmentContractItem contract : contracts) {
      contract.getReports().sort((a, b) -> {
        LocalDate d1 = a.getAssessmentDate() == null ? LocalDate.MIN : a.getAssessmentDate();
        LocalDate d2 = b.getAssessmentDate() == null ? LocalDate.MIN : b.getAssessmentDate();
        return d2.compareTo(d1);
      });
    }
  }

  private LocalDate contractSignedDateSafe(CrmContractAssessmentContractItem item) {
    if (item == null || item.getContractSignedAt() == null) {
      return LocalDate.MIN;
    }
    return item.getContractSignedAt().toLocalDate();
  }

  private CrmContractAssessmentContractItem findMatchingContract(
      List<CrmContractAssessmentContractItem> contracts, LocalDate assessmentDate) {
    if (contracts == null || contracts.isEmpty()) {
      return null;
    }
    if (assessmentDate == null) {
      return contracts.get(contracts.size() - 1);
    }
    CrmContractAssessmentContractItem target = null;
    for (CrmContractAssessmentContractItem item : contracts) {
      LocalDate signedDate = contractSignedDateSafe(item);
      if (signedDate.isAfter(assessmentDate)) {
        continue;
      }
      target = item;
    }
    if (target != null) {
      return target;
    }
    return contracts.get(0);
  }

  private CrmContractAssessmentReportItem toReportItem(AssessmentRecord record) {
    CrmContractAssessmentReportItem item = new CrmContractAssessmentReportItem();
    item.setRecordId(record.getId());
    item.setAssessmentType(record.getAssessmentType());
    item.setAssessmentDate(record.getAssessmentDate());
    item.setStatus(record.getStatus());
    item.setScore(record.getScore());
    item.setLevelCode(record.getLevelCode());
    item.setResultSummary(record.getResultSummary());
    item.setNextAssessmentDate(record.getNextAssessmentDate());
    return item;
  }

  private void fillBillingSummary(CrmContractLinkageResponse response, Long elderId, Long tenantId) {
    List<BillMonthly> bills = billMonthlyMapper.selectList(Wrappers.lambdaQuery(BillMonthly.class)
        .eq(BillMonthly::getOrgId, tenantId)
        .eq(BillMonthly::getElderId, elderId)
        .eq(BillMonthly::getIsDeleted, 0));
    response.setBillCount(bills.size());
    BigDecimal total = BigDecimal.ZERO;
    BigDecimal paid = BigDecimal.ZERO;
    BigDecimal outstanding = BigDecimal.ZERO;
    for (BillMonthly bill : bills) {
      total = total.add(defaultAmount(bill.getTotalAmount()));
      paid = paid.add(defaultAmount(bill.getPaidAmount()));
      outstanding = outstanding.add(defaultAmount(bill.getOutstandingAmount()));
    }
    response.setBillTotalAmount(total);
    response.setBillPaidAmount(paid);
    response.setBillOutstandingAmount(outstanding);
  }

  private void fillAttachments(CrmContractLinkageResponse response, Long tenantId, Long leadId, String contractNo) {
    List<CrmContractAttachment> attachments;
    if (leadId != null) {
      attachments = attachmentMapper.selectList(Wrappers.lambdaQuery(CrmContractAttachment.class)
          .eq(CrmContractAttachment::getTenantId, tenantId)
          .eq(CrmContractAttachment::getLeadId, leadId)
          .eq(CrmContractAttachment::getIsDeleted, 0)
          .orderByDesc(CrmContractAttachment::getCreateTime));
    } else if (contractNo != null && !contractNo.isBlank()) {
      attachments = attachmentMapper.selectList(Wrappers.lambdaQuery(CrmContractAttachment.class)
          .eq(CrmContractAttachment::getTenantId, tenantId)
          .eq(CrmContractAttachment::getContractNo, contractNo)
          .eq(CrmContractAttachment::getIsDeleted, 0)
          .orderByDesc(CrmContractAttachment::getCreateTime));
    } else {
      attachments = Collections.emptyList();
    }

    List<CrmContractAttachmentResponse> records = attachments.stream().map(item -> {
      CrmContractAttachmentResponse resp = new CrmContractAttachmentResponse();
      resp.setId(item.getId());
      resp.setLeadId(item.getLeadId());
      resp.setContractNo(item.getContractNo());
      resp.setFileName(item.getFileName());
      resp.setFileUrl(item.getFileUrl());
      resp.setFileType(item.getFileType());
      resp.setFileSize(item.getFileSize());
      resp.setRemark(item.getRemark());
      resp.setCreateTime(item.getCreateTime());
      return resp;
    }).toList();
    response.setAttachments(records);
    response.setAttachmentCount(records.size());
  }

  private BigDecimal defaultAmount(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private String firstNonBlank(String first, String second) {
    if (first != null && !first.isBlank()) {
      return first;
    }
    if (second != null && !second.isBlank()) {
      return second;
    }
    return null;
  }
}
