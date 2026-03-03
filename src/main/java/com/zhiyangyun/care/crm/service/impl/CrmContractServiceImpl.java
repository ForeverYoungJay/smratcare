package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.model.CrmContractRequest;
import com.zhiyangyun.care.crm.model.CrmContractResponse;
import com.zhiyangyun.care.crm.service.CrmContractService;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrmContractServiceImpl implements CrmContractService {
  private static final Pattern CONTRACT_SEQ_PATTERN = Pattern.compile("^(gfyy\\d{8})(\\d{3,})$");
  private static final String FLOW_PENDING_ASSESSMENT = "PENDING_ASSESSMENT";
  private static final String FLOW_PENDING_BED_SELECT = "PENDING_BED_SELECT";
  private static final String FLOW_PENDING_SIGN = "PENDING_SIGN";
  private static final String FLOW_SIGNED = "SIGNED";
  private static final Map<String, Set<String>> STATUS_TRANSITION_RULES = buildStatusTransitionRules();

  private final CrmContractMapper contractMapper;
  private final CrmLeadMapper leadMapper;
  private final ElderAdmissionMapper admissionMapper;

  public CrmContractServiceImpl(
      CrmContractMapper contractMapper,
      CrmLeadMapper leadMapper,
      ElderAdmissionMapper admissionMapper) {
    this.contractMapper = contractMapper;
    this.leadMapper = leadMapper;
    this.admissionMapper = admissionMapper;
  }

  @Override
  @Transactional
  public CrmContractResponse create(Long tenantId, Long orgId, Long staffId, CrmContractRequest request) {
    final String requestedNo = request == null ? null : request.getContractNo();
    for (int attempt = 0; attempt < 6; attempt++) {
      CrmContract contract = new CrmContract();
      contract.setTenantId(tenantId);
      contract.setOrgId(orgId);
      applyRequest(contract, request, null);
      contract.setContractNo(resolveContractNo(tenantId, requestedNo, null));
      if (contract.getFlowStage() == null) {
        contract.setFlowStage(FLOW_PENDING_ASSESSMENT);
      }
      if (contract.getCurrentOwnerDept() == null) {
        contract.setCurrentOwnerDept(resolveOwnerDept(contract.getFlowStage()));
      }
      if (contract.getStatus() == null) {
        contract.setStatus(resolveStatus(contract));
      }
      if (contract.getContractStatus() == null || contract.getContractStatus().isBlank()) {
        contract.setContractStatus(resolveContractStatus(contract.getFlowStage(), contract.getStatus()));
      }
      contract.setCreatedBy(staffId);
      try {
        contractMapper.insert(contract);
        syncLeadProjection(contract, true);
        return toResponse(contract);
      } catch (DuplicateKeyException | DataIntegrityViolationException ex) {
        if (!isContractNoDuplicate(ex) || (requestedNo != null && !requestedNo.isBlank()) || attempt >= 5) {
          throw ex;
        }
      }
    }
    throw new IllegalStateException("合同编号生成失败，请重试");
  }

  @Override
  @Transactional
  public CrmContractResponse update(Long tenantId, Long orgId, Long id, CrmContractRequest request) {
    CrmContract existing = contractMapper.selectById(id);
    if (!isOwnedContract(existing, tenantId)) {
      return null;
    }
    CrmContract before = cloneContract(existing);
    String previousNo = existing.getContractNo();
    applyRequest(existing, request, existing);
    existing.setContractNo(resolveContractNo(tenantId, request == null ? null : request.getContractNo(), existing));
    if (existing.getCurrentOwnerDept() == null) {
      existing.setCurrentOwnerDept(resolveOwnerDept(existing.getFlowStage()));
    }
    if (existing.getStatus() == null || existing.getStatus().isBlank()) {
      existing.setStatus(resolveStatus(existing));
    }
    if (existing.getContractStatus() == null || existing.getContractStatus().isBlank()) {
      existing.setContractStatus(resolveContractStatus(existing.getFlowStage(), existing.getStatus()));
    }
    validateStatusTransition(before, existing);
    contractMapper.updateById(existing);
    syncLeadProjection(existing, false);
    if (previousNo != null && !previousNo.equals(existing.getContractNo())) {
      updateAdmissionContractNo(tenantId, previousNo, existing.getContractNo());
    }
    return toResponse(existing);
  }

  @Override
  public CrmContractResponse get(Long tenantId, Long id) {
    CrmContract contract = contractMapper.selectById(id);
    return isOwnedContract(contract, tenantId) ? toResponse(contract) : null;
  }

  @Override
  public IPage<CrmContractResponse> page(
      Long tenantId,
      long pageNo,
      long pageSize,
      String keyword,
      String contractNo,
      String elderName,
      String elderPhone,
      String marketerName,
      String flowStage,
      String contractStatus,
      String status,
      Boolean overdueOnly,
      Boolean sortByOverdue,
      String currentOwnerDept) {
    var wrapper = Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getTenantId, tenantId)
        .eq(CrmContract::getIsDeleted, 0);
    if (contractNo != null && !contractNo.isBlank()) {
      wrapper.like(CrmContract::getContractNo, contractNo.trim());
    }
    if (elderName != null && !elderName.isBlank()) {
      wrapper.like(CrmContract::getElderName, elderName.trim());
    }
    if (elderPhone != null && !elderPhone.isBlank()) {
      wrapper.like(CrmContract::getElderPhone, elderPhone.trim());
    }
    if (marketerName != null && !marketerName.isBlank()) {
      wrapper.like(CrmContract::getMarketerName, marketerName.trim());
    }
    if (flowStage != null && !flowStage.isBlank()) {
      wrapper.eq(CrmContract::getFlowStage, flowStage.trim());
    }
    if (contractStatus != null && !contractStatus.isBlank()) {
      wrapper.eq(CrmContract::getContractStatus, contractStatus.trim());
    }
    if (status != null && !status.isBlank()) {
      wrapper.eq(CrmContract::getStatus, status.trim());
    }
    if (currentOwnerDept != null && !currentOwnerDept.isBlank()) {
      wrapper.eq(CrmContract::getCurrentOwnerDept, currentOwnerDept.trim());
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(CrmContract::getContractNo, keyword)
          .or().like(CrmContract::getName, keyword)
          .or().like(CrmContract::getPhone, keyword)
          .or().like(CrmContract::getElderName, keyword)
          .or().like(CrmContract::getElderPhone, keyword)
          .or().like(CrmContract::getMarketerName, keyword));
    }
    if (Boolean.TRUE.equals(overdueOnly)) {
      wrapper.and(w -> w
          .and(v -> v.eq(CrmContract::getFlowStage, FLOW_PENDING_ASSESSMENT)
              .lt(CrmContract::getCreateTime, LocalDateTime.now().minusHours(48)))
          .or(v -> v.eq(CrmContract::getFlowStage, FLOW_PENDING_SIGN)
              .lt(CrmContract::getUpdateTime, LocalDateTime.now().minusHours(24))));
    }
    if (Boolean.TRUE.equals(sortByOverdue)) {
      wrapper.orderByAsc(CrmContract::getFlowStage)
          .orderByAsc(CrmContract::getUpdateTime)
          .orderByAsc(CrmContract::getCreateTime);
    } else {
      wrapper.orderByDesc(CrmContract::getUpdateTime).orderByDesc(CrmContract::getCreateTime);
    }
    IPage<CrmContract> page = contractMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  @Transactional
  public int deleteBatch(Long tenantId, List<Long> ids, List<String> contractNos) {
    int affected = 0;
    if (ids != null) {
      for (Long id : ids) {
        CrmContract contract = contractMapper.selectById(id);
        if (!isOwnedContract(contract, tenantId) || Integer.valueOf(1).equals(contract.getIsDeleted())) {
          continue;
        }
        contract.setIsDeleted(1);
        contractMapper.updateById(contract);
        syncLeadDelete(contract);
        affected++;
      }
    }
    if (contractNos != null) {
      for (String item : contractNos) {
        if (item == null || item.isBlank()) {
          continue;
        }
        CrmContract contract = contractMapper.selectOne(Wrappers.lambdaQuery(CrmContract.class)
            .eq(CrmContract::getTenantId, tenantId)
            .eq(CrmContract::getIsDeleted, 0)
            .eq(CrmContract::getContractNo, item.trim())
            .orderByDesc(CrmContract::getUpdateTime)
            .last("LIMIT 1"));
        if (contract == null) {
          continue;
        }
        contract.setIsDeleted(1);
        contractMapper.updateById(contract);
        syncLeadDelete(contract);
        affected++;
      }
    }
    return affected;
  }

  @Override
  @Transactional
  public CrmContractResponse handoffToAssessment(Long tenantId, Long id) {
    CrmContract contract = contractMapper.selectById(id);
    if (!isOwnedContract(contract, tenantId)) {
      return null;
    }
    CrmContract before = cloneContract(contract);
    contract.setFlowStage(FLOW_PENDING_ASSESSMENT);
    contract.setCurrentOwnerDept("ASSESSMENT");
    contract.setContractStatus("待评估");
    contract.setStatus("PENDING_APPROVAL");
    validateStatusTransition(before, contract);
    contractMapper.updateById(contract);
    syncLeadProjection(contract, false);
    return toResponse(contract);
  }

  @Override
  @Transactional
  public CrmContractResponse approve(Long tenantId, Long id, String remark) {
    CrmContract contract = contractMapper.selectById(id);
    if (!isOwnedContract(contract, tenantId)) {
      return null;
    }
    CrmContract before = cloneContract(contract);
    contract.setStatus("APPROVED");
    if (!FLOW_SIGNED.equals(contract.getFlowStage())) {
      contract.setFlowStage(FLOW_PENDING_SIGN);
    }
    contract.setCurrentOwnerDept("MARKETING");
    contract.setContractStatus("费用预审通过");
    if (remark != null && !remark.isBlank()) {
      contract.setRemark(remark.trim());
    }
    validateStatusTransition(before, contract);
    contractMapper.updateById(contract);
    syncLeadProjection(contract, false);
    return toResponse(contract);
  }

  @Override
  @Transactional
  public CrmContractResponse reject(Long tenantId, Long id, String remark) {
    CrmContract contract = contractMapper.selectById(id);
    if (!isOwnedContract(contract, tenantId)) {
      return null;
    }
    CrmContract before = cloneContract(contract);
    contract.setStatus("REJECTED");
    contract.setFlowStage(FLOW_PENDING_ASSESSMENT);
    contract.setCurrentOwnerDept("ASSESSMENT");
    contract.setContractStatus("已驳回");
    if (remark != null && !remark.isBlank()) {
      contract.setRemark(remark.trim());
    }
    validateStatusTransition(before, contract);
    contractMapper.updateById(contract);
    syncLeadProjection(contract, false);
    return toResponse(contract);
  }

  @Override
  @Transactional
  public CrmContractResponse voidContract(Long tenantId, Long id, String remark) {
    CrmContract contract = contractMapper.selectById(id);
    if (!isOwnedContract(contract, tenantId)) {
      return null;
    }
    CrmContract before = cloneContract(contract);
    contract.setStatus("VOID");
    contract.setContractStatus("作废");
    if (remark != null && !remark.isBlank()) {
      contract.setRemark(remark.trim());
    }
    validateStatusTransition(before, contract);
    contractMapper.updateById(contract);
    syncLeadProjection(contract, false);
    return toResponse(contract);
  }

  @Override
  @Transactional
  public CrmContractResponse finalizeSign(Long tenantId, Long id, String remark) {
    CrmContract contract = contractMapper.selectById(id);
    if (!isOwnedContract(contract, tenantId)) {
      return null;
    }
    CrmContract before = cloneContract(contract);
    if (!hasAdmission(contract)) {
      throw new IllegalStateException("未完成入住办理，不能最终签署");
    }
    if (!FLOW_PENDING_SIGN.equals(contract.getFlowStage()) && !FLOW_PENDING_BED_SELECT.equals(contract.getFlowStage())) {
      throw new IllegalStateException("合同当前阶段不允许最终签署");
    }
    contract.setFlowStage(FLOW_SIGNED);
    contract.setCurrentOwnerDept("MARKETING");
    contract.setStatus("SIGNED");
    contract.setContractStatus("已审批,已通过");
    contract.setSignedAt(LocalDateTime.now());
    if (remark != null && !remark.isBlank()) {
      contract.setRemark(remark.trim());
    }
    validateStatusTransition(before, contract);
    contractMapper.updateById(contract);
    syncLeadProjection(contract, false);
    return toResponse(contract);
  }

  private boolean hasAdmission(CrmContract contract) {
    String contractNo = blankToNull(contract.getContractNo());
    if (contractNo != null) {
      ElderAdmission admission = admissionMapper.selectOne(Wrappers.lambdaQuery(ElderAdmission.class)
          .eq(ElderAdmission::getTenantId, contract.getTenantId())
          .eq(ElderAdmission::getIsDeleted, 0)
          .eq(ElderAdmission::getContractNo, contractNo)
          .orderByDesc(ElderAdmission::getCreateTime)
          .last("LIMIT 1"));
      if (admission != null) {
        return true;
      }
    }
    if (contract.getElderId() != null) {
      ElderAdmission admissionByElder = admissionMapper.selectOne(Wrappers.lambdaQuery(ElderAdmission.class)
          .eq(ElderAdmission::getTenantId, contract.getTenantId())
          .eq(ElderAdmission::getIsDeleted, 0)
          .eq(ElderAdmission::getElderId, contract.getElderId())
          .orderByDesc(ElderAdmission::getCreateTime)
          .last("LIMIT 1"));
      return admissionByElder != null;
    }
    return false;
  }

  private void applyRequest(CrmContract target, CrmContractRequest request, CrmContract current) {
    if (request == null) {
      return;
    }
    target.setLeadId(request.getLeadId() == null ? target.getLeadId() : request.getLeadId());
    target.setElderId(request.getElderId() == null ? target.getElderId() : request.getElderId());
    target.setName(firstNonBlank(blankToNull(request.getName()), current == null ? null : current.getName()));
    target.setPhone(firstNonBlank(blankToNull(request.getPhone()), current == null ? null : current.getPhone()));
    target.setIdCardNo(firstNonBlank(blankToNull(request.getIdCardNo()), current == null ? null : current.getIdCardNo()));
    target.setHomeAddress(firstNonBlank(blankToNull(request.getHomeAddress()), current == null ? null : current.getHomeAddress()));
    target.setReservationRoomNo(firstNonBlank(blankToNull(request.getReservationRoomNo()), current == null ? null : current.getReservationRoomNo()));
    target.setReservationBedId(request.getReservationBedId() == null ? (current == null ? null : current.getReservationBedId()) : request.getReservationBedId());
    target.setElderName(firstNonBlank(blankToNull(request.getElderName()), current == null ? null : current.getElderName()));
    target.setElderPhone(firstNonBlank(blankToNull(request.getElderPhone()), current == null ? null : current.getElderPhone()));
    target.setGender(request.getGender() == null ? (current == null ? null : current.getGender()) : request.getGender());
    target.setAge(request.getAge() == null ? (current == null ? null : current.getAge()) : request.getAge());
    target.setMarketerName(firstNonBlank(blankToNull(request.getMarketerName()), current == null ? null : current.getMarketerName()));
    LocalDateTime signedAt = parseDateTime(request.getContractSignedAt());
    target.setSignedAt(signedAt == null ? (current == null ? null : current.getSignedAt()) : signedAt);
    LocalDate effectiveFrom = parseDate(request.getContractSignedAt());
    target.setEffectiveFrom(effectiveFrom == null ? (current == null ? null : current.getEffectiveFrom()) : effectiveFrom);
    LocalDate effectiveTo = parseDate(request.getContractExpiryDate());
    target.setEffectiveTo(effectiveTo == null ? (current == null ? null : current.getEffectiveTo()) : effectiveTo);
    target.setContractStatus(firstNonBlank(blankToNull(request.getContractStatus()), current == null ? null : current.getContractStatus()));
    target.setFlowStage(firstNonBlank(blankToNull(request.getFlowStage()), current == null ? null : current.getFlowStage()));
    target.setCurrentOwnerDept(firstNonBlank(blankToNull(request.getCurrentOwnerDept()), current == null ? null : current.getCurrentOwnerDept()));
    target.setOrgName(firstNonBlank(blankToNull(request.getOrgName()), current == null ? null : current.getOrgName()));
    target.setStatus(firstNonBlank(blankToNull(request.getStatus()), current == null ? null : current.getStatus()));
    target.setRemark(firstNonBlank(blankToNull(request.getRemark()), current == null ? null : current.getRemark()));
    if (request.getSmsSendCount() != null) {
      target.setSmsSendCount(Math.max(0, request.getSmsSendCount()));
    } else if (current == null || current.getSmsSendCount() == null) {
      target.setSmsSendCount(0);
    }
  }

  private void syncLeadProjection(CrmContract contract, boolean createWhenMissing) {
    if (contract == null || contract.getTenantId() == null) {
      return;
    }
    CrmLead lead = null;
    if (contract.getLeadId() != null) {
      lead = leadMapper.selectById(contract.getLeadId());
      if (lead != null && !contract.getTenantId().equals(lead.getTenantId())) {
        lead = null;
      }
    }
    if (lead == null && contract.getContractNo() != null) {
      lead = leadMapper.selectOne(Wrappers.lambdaQuery(CrmLead.class)
          .eq(CrmLead::getTenantId, contract.getTenantId())
          .eq(CrmLead::getIsDeleted, 0)
          .eq(CrmLead::getContractNo, contract.getContractNo())
          .orderByDesc(CrmLead::getUpdateTime)
          .last("LIMIT 1"));
    }
    if (lead == null && !createWhenMissing) {
      return;
    }
    if (lead == null) {
      lead = new CrmLead();
      lead.setTenantId(contract.getTenantId());
      lead.setOrgId(contract.getOrgId());
      lead.setName(firstNonBlank(contract.getName(), contract.getElderName()));
      lead.setPhone(firstNonBlank(contract.getPhone(), contract.getElderPhone()));
      lead.setStatus(2);
      lead.setCreatedBy(contract.getCreatedBy());
      lead.setSource("contract");
      lead.setConsultDate(LocalDate.now());
      lead.setContractNo(contract.getContractNo());
      lead.setIsDeleted(0);
    }
    lead.setElderName(firstNonBlank(contract.getElderName(), contract.getName()));
    lead.setElderPhone(firstNonBlank(contract.getElderPhone(), contract.getPhone()));
    lead.setName(firstNonBlank(contract.getName(), contract.getElderName()));
    lead.setPhone(firstNonBlank(contract.getPhone(), contract.getElderPhone()));
    lead.setIdCardNo(contract.getIdCardNo());
    lead.setHomeAddress(contract.getHomeAddress());
    lead.setGender(contract.getGender());
    lead.setAge(contract.getAge());
    lead.setMarketerName(contract.getMarketerName());
    lead.setReservationRoomNo(contract.getReservationRoomNo());
    lead.setReservationBedId(contract.getReservationBedId());
    lead.setContractNo(contract.getContractNo());
    lead.setContractStatus(contract.getContractStatus());
    lead.setFlowStage(contract.getFlowStage());
    lead.setCurrentOwnerDept(contract.getCurrentOwnerDept());
    lead.setContractSignedAt(contract.getSignedAt());
    lead.setContractSignedFlag("SIGNED".equals(contract.getStatus()) || "EFFECTIVE".equals(contract.getStatus()) ? 1 : 0);
    lead.setContractExpiryDate(contract.getEffectiveTo());
    lead.setSmsSendCount(contract.getSmsSendCount() == null ? 0 : contract.getSmsSendCount());
    lead.setOrgName(contract.getOrgName());
    lead.setRemark(contract.getRemark());
    lead.setStatus(2);
    if (lead.getId() == null) {
      leadMapper.insert(lead);
      contract.setLeadId(lead.getId());
      contractMapper.updateById(contract);
    } else {
      leadMapper.updateById(lead);
    }
  }

  private void syncLeadDelete(CrmContract contract) {
    if (contract.getLeadId() != null) {
      CrmLead lead = leadMapper.selectById(contract.getLeadId());
      if (lead != null && contract.getTenantId().equals(lead.getTenantId())) {
        lead.setIsDeleted(1);
        leadMapper.updateById(lead);
      }
      return;
    }
    if (contract.getContractNo() == null || contract.getContractNo().isBlank()) {
      return;
    }
    CrmLead lead = leadMapper.selectOne(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getTenantId, contract.getTenantId())
        .eq(CrmLead::getIsDeleted, 0)
        .eq(CrmLead::getContractNo, contract.getContractNo())
        .last("LIMIT 1"));
    if (lead != null) {
      lead.setIsDeleted(1);
      leadMapper.updateById(lead);
    }
  }

  private void updateAdmissionContractNo(Long tenantId, String oldNo, String newNo) {
    if (tenantId == null || oldNo == null || oldNo.isBlank() || newNo == null || newNo.isBlank()) {
      return;
    }
    List<ElderAdmission> admissions = admissionMapper.selectList(Wrappers.lambdaQuery(ElderAdmission.class)
        .eq(ElderAdmission::getTenantId, tenantId)
        .eq(ElderAdmission::getIsDeleted, 0)
        .eq(ElderAdmission::getContractNo, oldNo));
    for (ElderAdmission admission : admissions) {
      admission.setContractNo(newNo);
      admissionMapper.updateById(admission);
    }
  }

  private String resolveContractNo(Long tenantId, String candidate, CrmContract current) {
    if (candidate != null && !candidate.isBlank()) {
      return candidate.trim();
    }
    if (current != null && current.getContractNo() != null && !current.getContractNo().isBlank()) {
      return current.getContractNo();
    }

    String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String prefix = "gfyy" + datePart;
    String nextSeq = "001";

    CrmContract latest = contractMapper.selectOne(Wrappers.lambdaQuery(CrmContract.class)
        .eq(tenantId != null, CrmContract::getTenantId, tenantId)
        .eq(CrmContract::getIsDeleted, 0)
        .likeRight(CrmContract::getContractNo, prefix)
        .orderByDesc(CrmContract::getContractNo)
        .last("LIMIT 1"));
    if (latest != null && latest.getContractNo() != null) {
      Matcher matcher = CONTRACT_SEQ_PATTERN.matcher(latest.getContractNo());
      if (matcher.matches() && prefix.equals(matcher.group(1))) {
        try {
          int seq = Integer.parseInt(matcher.group(2));
          nextSeq = String.format("%03d", seq + 1);
        } catch (NumberFormatException ignored) {
          nextSeq = "001";
        }
      }
    }
    return prefix + nextSeq;
  }

  private boolean isContractNoDuplicate(Throwable ex) {
    if (ex == null) {
      return false;
    }
    String message = ex.getMessage();
    if (message != null && message.contains("uk_crm_contract_tenant_no")) {
      return true;
    }
    Throwable cause = ex.getCause();
    if (cause != null && cause != ex) {
      return isContractNoDuplicate(cause);
    }
    return false;
  }

  private String resolveStatus(CrmContract contract) {
    if (contract == null) {
      return "DRAFT";
    }
    if (FLOW_SIGNED.equals(contract.getFlowStage())) {
      return "SIGNED";
    }
    if (FLOW_PENDING_SIGN.equals(contract.getFlowStage())) {
      return "APPROVED";
    }
    if (FLOW_PENDING_BED_SELECT.equals(contract.getFlowStage())) {
      return "PENDING_APPROVAL";
    }
    return "DRAFT";
  }

  private static Map<String, Set<String>> buildStatusTransitionRules() {
    Map<String, Set<String>> rules = new HashMap<>();
    rules.put("DRAFT", Set.of("DRAFT", "PENDING_APPROVAL", "APPROVED", "VOID"));
    rules.put("PENDING_APPROVAL", Set.of("PENDING_APPROVAL", "APPROVED", "REJECTED", "VOID"));
    rules.put("REJECTED", Set.of("REJECTED", "PENDING_APPROVAL", "VOID"));
    rules.put("APPROVED", Set.of("APPROVED", "SIGNED", "EFFECTIVE", "VOID"));
    rules.put("SIGNED", Set.of("SIGNED", "EFFECTIVE", "VOID"));
    rules.put("EFFECTIVE", Set.of("EFFECTIVE", "VOID"));
    rules.put("VOID", Set.of("VOID"));
    return rules;
  }

  private void validateStatusTransition(CrmContract before, CrmContract after) {
    if (after == null) {
      return;
    }
    String from = normalizeDomainStatus(before == null ? null : before.getStatus());
    String to = normalizeDomainStatus(after.getStatus());
    if (from == null || to == null) {
      return;
    }
    Set<String> allowed = STATUS_TRANSITION_RULES.get(from);
    if (allowed != null && !allowed.contains(to)) {
      throw new IllegalStateException("合同状态不允许从 " + from + " 变更为 " + to);
    }
    if ("SIGNED".equals(to) && after.getSignedAt() == null) {
      after.setSignedAt(LocalDateTime.now());
    }
  }

  private String normalizeDomainStatus(String status) {
    String value = blankToNull(status);
    return value == null ? null : value.toUpperCase(Locale.ROOT);
  }

  private CrmContract cloneContract(CrmContract source) {
    if (source == null) {
      return null;
    }
    CrmContract target = new CrmContract();
    target.setId(source.getId());
    target.setStatus(source.getStatus());
    target.setFlowStage(source.getFlowStage());
    target.setSignedAt(source.getSignedAt());
    return target;
  }

  private String resolveContractStatus(String flowStage, String status) {
    if ("SIGNED".equals(status) || FLOW_SIGNED.equals(flowStage)) {
      return "已审批,已通过";
    }
    if (FLOW_PENDING_SIGN.equals(flowStage)) {
      return "费用预审通过";
    }
    if (FLOW_PENDING_BED_SELECT.equals(flowStage)) {
      return "待办理入住";
    }
    if (FLOW_PENDING_ASSESSMENT.equals(flowStage)) {
      return "待评估";
    }
    return "未提交";
  }

  private String resolveOwnerDept(String flowStage) {
    if (FLOW_PENDING_ASSESSMENT.equals(flowStage)) {
      return "ASSESSMENT";
    }
    if (FLOW_SIGNED.equals(flowStage) || FLOW_PENDING_SIGN.equals(flowStage) || FLOW_PENDING_BED_SELECT.equals(flowStage)) {
      return "MARKETING";
    }
    return "MARKETING";
  }

  private CrmContractResponse toResponse(CrmContract contract) {
    CrmContractResponse response = new CrmContractResponse();
    response.setId(contract.getId());
    response.setTenantId(contract.getTenantId());
    response.setOrgId(contract.getOrgId());
    response.setLeadId(contract.getLeadId());
    response.setElderId(contract.getElderId());
    response.setContractNo(contract.getContractNo());
    response.setName(contract.getName());
    response.setPhone(contract.getPhone());
    response.setIdCardNo(contract.getIdCardNo());
    response.setHomeAddress(contract.getHomeAddress());
    response.setReservationRoomNo(contract.getReservationRoomNo());
    response.setReservationBedId(contract.getReservationBedId());
    response.setElderName(contract.getElderName());
    response.setElderPhone(contract.getElderPhone());
    response.setGender(contract.getGender());
    response.setAge(contract.getAge());
    response.setMarketerName(contract.getMarketerName());
    response.setContractSignedAt(contract.getSignedAt());
    response.setContractExpiryDate(contract.getEffectiveTo());
    response.setContractStatus(contract.getContractStatus());
    response.setFlowStage(contract.getFlowStage());
    response.setCurrentOwnerDept(contract.getCurrentOwnerDept());
    response.setOrgName(contract.getOrgName());
    response.setStatus(contract.getStatus());
    response.setSmsSendCount(contract.getSmsSendCount());
    response.setRemark(contract.getRemark());
    response.setCreateTime(contract.getCreateTime());
    response.setUpdateTime(contract.getUpdateTime());
    return response;
  }

  private boolean isOwnedContract(CrmContract contract, Long tenantId) {
    return contract != null && Integer.valueOf(0).equals(contract.getIsDeleted()) && tenantId.equals(contract.getTenantId());
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
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

  private LocalDate parseDate(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    String value = text.trim();
    if (value.length() >= 10) {
      value = value.substring(0, 10);
    }
    return LocalDate.parse(value);
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
    } catch (DateTimeParseException ex) {
      try {
        return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.CHINA));
      } catch (DateTimeParseException ignore) {
        return null;
      }
    }
  }
}
