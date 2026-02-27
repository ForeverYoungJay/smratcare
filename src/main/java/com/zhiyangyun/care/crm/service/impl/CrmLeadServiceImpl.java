package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.model.CrmLeadRequest;
import com.zhiyangyun.care.crm.model.CrmLeadResponse;
import com.zhiyangyun.care.crm.service.CrmLeadService;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class CrmLeadServiceImpl implements CrmLeadService {
  private static final Pattern CONTRACT_SEQ_PATTERN = Pattern.compile("^(gfyy\\d{8})(\\d{3,})$");
  private static final String FLOW_PENDING_ASSESSMENT = "PENDING_ASSESSMENT";
  private static final String FLOW_PENDING_BED_SELECT = "PENDING_BED_SELECT";
  private static final String FLOW_PENDING_SIGN = "PENDING_SIGN";
  private static final String FLOW_SIGNED = "SIGNED";
  private final CrmLeadMapper leadMapper;
  private final ElderMapper elderMapper;
  private final ElderAdmissionMapper admissionMapper;

  public CrmLeadServiceImpl(CrmLeadMapper leadMapper, ElderMapper elderMapper, ElderAdmissionMapper admissionMapper) {
    this.leadMapper = leadMapper;
    this.elderMapper = elderMapper;
    this.admissionMapper = admissionMapper;
  }

  @Override
  public CrmLeadResponse create(CrmLeadRequest request) {
    CrmLead lead = new CrmLead();
    lead.setTenantId(request.getTenantId());
    lead.setOrgId(request.getOrgId());
    lead.setName(defaultLeadName(request));
    lead.setPhone(defaultLeadPhone(request));
    lead.setConsultantName(blankToNull(request.getConsultantName()));
    lead.setConsultantPhone(blankToNull(request.getConsultantPhone()));
    lead.setElderName(blankToNull(request.getElderName()));
    lead.setElderPhone(blankToNull(request.getElderPhone()));
    lead.setGender(request.getGender());
    lead.setAge(request.getAge());
    lead.setConsultDate(parseDate(request.getConsultDate()));
    lead.setConsultType(blankToNull(request.getConsultType()));
    lead.setMediaChannel(blankToNull(request.getMediaChannel()));
    lead.setInfoSource(blankToNull(request.getInfoSource()));
    lead.setReceptionistName(blankToNull(request.getReceptionistName()));
    lead.setHomeAddress(blankToNull(request.getHomeAddress()));
    lead.setMarketerName(blankToNull(request.getMarketerName()));
    lead.setFollowupStatus(blankToNull(request.getFollowupStatus()));
    lead.setReferralChannel(blankToNull(request.getReferralChannel()));
    lead.setInvalidTime(parseDateTime(request.getInvalidTime()));
    lead.setIdCardNo(blankToNull(request.getIdCardNo()));
    lead.setReservationRoomNo(blankToNull(request.getReservationRoomNo()));
    lead.setReservationBedId(request.getReservationBedId());
    lead.setReservationChannel(blankToNull(request.getReservationChannel()));
    lead.setReservationStatus(blankToNull(request.getReservationStatus()));
    lead.setRefunded(normalizeBooleanFlag(request.getRefunded()));
    lead.setReservationAmount(normalizeAmount(request.getReservationAmount()));
    lead.setPaymentTime(parseDateTime(request.getPaymentTime()));
    lead.setOrgName(blankToNull(request.getOrgName()));
    lead.setSource(normalizeSource(request.getSource()));
    lead.setCustomerTag(request.getCustomerTag());
    lead.setStatus(request.getStatus());
    lead.setContractSignedFlag(normalizeSignedFlag(request.getContractSignedFlag()));
    lead.setContractSignedAt(resolveContractTime(parseDateTime(request.getContractSignedAt()), request.getContractSignedFlag()));
    lead.setContractNo(resolveContractNo(request, null));
    lead.setContractStatus(blankToNull(request.getContractStatus()));
    lead.setFlowStage(resolveFlowStage(request, null));
    lead.setCurrentOwnerDept(resolveOwnerDept(lead.getFlowStage(), blankToNull(request.getCurrentOwnerDept())));
    lead.setContractExpiryDate(parseDate(request.getContractExpiryDate()));
    lead.setSmsSendCount(request.getSmsSendCount() == null ? 0 : Math.max(0, request.getSmsSendCount()));
    lead.setNextFollowDate(parseDate(request.getNextFollowDate()));
    lead.setRemark(request.getRemark());
    lead.setCreatedBy(request.getCreatedBy());
    leadMapper.insert(lead);
    syncContractToAdmission(lead);
    return toResponse(lead);
  }

  @Override
  public CrmLeadResponse update(Long id, CrmLeadRequest request) {
    CrmLead lead = leadMapper.selectById(id);
    if (lead == null || !request.getTenantId().equals(lead.getTenantId())) {
      return null;
    }
    lead.setName(defaultLeadName(request));
    lead.setPhone(defaultLeadPhone(request));
    lead.setConsultantName(blankToNull(request.getConsultantName()));
    lead.setConsultantPhone(blankToNull(request.getConsultantPhone()));
    lead.setElderName(blankToNull(request.getElderName()));
    lead.setElderPhone(blankToNull(request.getElderPhone()));
    lead.setGender(request.getGender());
    lead.setAge(request.getAge());
    lead.setConsultDate(parseDate(request.getConsultDate()));
    lead.setConsultType(blankToNull(request.getConsultType()));
    lead.setMediaChannel(blankToNull(request.getMediaChannel()));
    lead.setInfoSource(blankToNull(request.getInfoSource()));
    lead.setReceptionistName(blankToNull(request.getReceptionistName()));
    lead.setHomeAddress(blankToNull(request.getHomeAddress()));
    lead.setMarketerName(blankToNull(request.getMarketerName()));
    lead.setFollowupStatus(blankToNull(request.getFollowupStatus()));
    lead.setReferralChannel(blankToNull(request.getReferralChannel()));
    lead.setInvalidTime(parseDateTime(request.getInvalidTime()));
    lead.setIdCardNo(blankToNull(request.getIdCardNo()));
    lead.setReservationRoomNo(blankToNull(request.getReservationRoomNo()));
    lead.setReservationBedId(request.getReservationBedId());
    lead.setReservationChannel(blankToNull(request.getReservationChannel()));
    lead.setReservationStatus(blankToNull(request.getReservationStatus()));
    lead.setRefunded(normalizeBooleanFlag(request.getRefunded()));
    lead.setReservationAmount(normalizeAmount(request.getReservationAmount()));
    lead.setPaymentTime(parseDateTime(request.getPaymentTime()));
    lead.setOrgName(blankToNull(request.getOrgName()));
    lead.setSource(normalizeSource(request.getSource()));
    lead.setCustomerTag(request.getCustomerTag());
    lead.setStatus(request.getStatus());
    lead.setContractSignedFlag(normalizeSignedFlag(request.getContractSignedFlag()));
    lead.setContractSignedAt(resolveContractTime(parseDateTime(request.getContractSignedAt()), request.getContractSignedFlag()));
    lead.setContractNo(resolveContractNo(request, lead));
    lead.setContractStatus(blankToNull(request.getContractStatus()));
    lead.setFlowStage(resolveFlowStage(request, lead));
    lead.setCurrentOwnerDept(resolveOwnerDept(lead.getFlowStage(), blankToNull(request.getCurrentOwnerDept())));
    lead.setContractExpiryDate(parseDate(request.getContractExpiryDate()));
    lead.setSmsSendCount(request.getSmsSendCount() == null ? 0 : Math.max(0, request.getSmsSendCount()));
    lead.setNextFollowDate(parseDate(request.getNextFollowDate()));
    lead.setRemark(request.getRemark());
    leadMapper.updateById(lead);
    syncContractToAdmission(lead);
    return toResponse(lead);
  }

  @Override
  public CrmLeadResponse get(Long id, Long tenantId) {
    CrmLead lead = leadMapper.selectById(id);
    if (lead == null || (tenantId != null && !tenantId.equals(lead.getTenantId()))) {
      return null;
    }
    return toResponse(lead);
  }

  @Override
  public IPage<CrmLeadResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer status, String source, String customerTag,
                                     String consultantName, String consultantPhone, String elderName, String elderPhone,
                                     String consultDateFrom, String consultDateTo, String consultType, String mediaChannel,
                                     String infoSource, String marketerName, String followupStatus, String reservationChannel,
                                     String contractNo, String contractStatus, String flowStage, String currentOwnerDept,
                                     String followupDateFrom, String followupDateTo, Boolean followupDueOnly) {
    var wrapper = Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId);
    if (status != null) {
      wrapper.eq(CrmLead::getStatus, status);
    }
    if (source != null && !source.isBlank()) {
      wrapper.eq(CrmLead::getSource, source);
    }
    if (customerTag != null && !customerTag.isBlank()) {
      wrapper.eq(CrmLead::getCustomerTag, customerTag);
    }
    if (consultantName != null && !consultantName.isBlank()) {
      wrapper.like(CrmLead::getConsultantName, consultantName);
    }
    if (consultantPhone != null && !consultantPhone.isBlank()) {
      wrapper.like(CrmLead::getConsultantPhone, consultantPhone);
    }
    if (elderName != null && !elderName.isBlank()) {
      wrapper.like(CrmLead::getElderName, elderName);
    }
    if (elderPhone != null && !elderPhone.isBlank()) {
      wrapper.like(CrmLead::getElderPhone, elderPhone);
    }
    if (consultType != null && !consultType.isBlank()) {
      wrapper.eq(CrmLead::getConsultType, consultType);
    }
    if (mediaChannel != null && !mediaChannel.isBlank()) {
      wrapper.eq(CrmLead::getMediaChannel, mediaChannel);
    }
    if (infoSource != null && !infoSource.isBlank()) {
      wrapper.eq(CrmLead::getInfoSource, infoSource);
    }
    if (marketerName != null && !marketerName.isBlank()) {
      wrapper.like(CrmLead::getMarketerName, marketerName);
    }
    if (followupStatus != null && !followupStatus.isBlank()) {
      wrapper.eq(CrmLead::getFollowupStatus, followupStatus);
    }
    if (reservationChannel != null && !reservationChannel.isBlank()) {
      wrapper.eq(CrmLead::getReservationChannel, reservationChannel);
    }
    if (contractNo != null && !contractNo.isBlank()) {
      wrapper.like(CrmLead::getContractNo, contractNo);
    }
    if (contractStatus != null && !contractStatus.isBlank()) {
      wrapper.eq(CrmLead::getContractStatus, contractStatus);
    }
    if (flowStage != null && !flowStage.isBlank()) {
      wrapper.eq(CrmLead::getFlowStage, flowStage);
    }
    if (currentOwnerDept != null && !currentOwnerDept.isBlank()) {
      wrapper.eq(CrmLead::getCurrentOwnerDept, currentOwnerDept);
    }
    LocalDate consultFrom = parseDate(consultDateFrom);
    LocalDate consultTo = parseDate(consultDateTo);
    LocalDate followupFrom = parseDate(followupDateFrom);
    LocalDate followupTo = parseDate(followupDateTo);
    if (consultFrom != null) {
      wrapper.ge(CrmLead::getConsultDate, consultFrom);
    }
    if (consultTo != null) {
      wrapper.le(CrmLead::getConsultDate, consultTo);
    }
    if (followupFrom != null) {
      wrapper.ge(CrmLead::getNextFollowDate, followupFrom);
    }
    if (followupTo != null) {
      wrapper.le(CrmLead::getNextFollowDate, followupTo);
    }
    if (Boolean.TRUE.equals(followupDueOnly)) {
      wrapper.in(CrmLead::getStatus, List.of(0, 1))
          .isNotNull(CrmLead::getNextFollowDate)
          .le(CrmLead::getNextFollowDate, LocalDate.now());
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(CrmLead::getName, keyword)
          .or().like(CrmLead::getPhone, keyword)
          .or().like(CrmLead::getConsultantName, keyword)
          .or().like(CrmLead::getConsultantPhone, keyword)
          .or().like(CrmLead::getElderName, keyword)
          .or().like(CrmLead::getElderPhone, keyword));
    }
    wrapper.orderByDesc(CrmLead::getConsultDate).orderByDesc(CrmLead::getCreateTime);
    IPage<CrmLead> page = leadMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public void delete(Long id, Long tenantId) {
    CrmLead lead = leadMapper.selectById(id);
    if (lead != null && tenantId != null && tenantId.equals(lead.getTenantId())) {
      lead.setIsDeleted(1);
      leadMapper.updateById(lead);
    }
  }

  private CrmLeadResponse toResponse(CrmLead lead) {
    CrmLeadResponse response = new CrmLeadResponse();
    response.setId(lead.getId());
    response.setTenantId(lead.getTenantId());
    response.setOrgId(lead.getOrgId());
    response.setName(lead.getName());
    response.setPhone(lead.getPhone());
    response.setConsultantName(lead.getConsultantName());
    response.setConsultantPhone(lead.getConsultantPhone());
    response.setElderName(lead.getElderName());
    response.setElderPhone(lead.getElderPhone());
    response.setGender(lead.getGender());
    response.setAge(lead.getAge());
    response.setConsultDate(lead.getConsultDate() == null ? null : lead.getConsultDate().toString());
    response.setConsultType(lead.getConsultType());
    response.setMediaChannel(lead.getMediaChannel());
    response.setInfoSource(lead.getInfoSource());
    response.setReceptionistName(lead.getReceptionistName());
    response.setHomeAddress(lead.getHomeAddress());
    response.setMarketerName(lead.getMarketerName());
    response.setFollowupStatus(lead.getFollowupStatus());
    response.setReferralChannel(lead.getReferralChannel());
    response.setInvalidTime(lead.getInvalidTime());
    response.setIdCardNo(lead.getIdCardNo());
    response.setReservationRoomNo(lead.getReservationRoomNo());
    response.setReservationBedId(lead.getReservationBedId());
    response.setReservationChannel(lead.getReservationChannel());
    response.setReservationStatus(lead.getReservationStatus());
    response.setRefunded(lead.getRefunded());
    response.setReservationAmount(lead.getReservationAmount());
    response.setPaymentTime(lead.getPaymentTime());
    response.setOrgName(lead.getOrgName());
    response.setSource(lead.getSource());
    response.setCustomerTag(lead.getCustomerTag());
    response.setStatus(lead.getStatus());
    response.setContractSignedFlag(lead.getContractSignedFlag());
    response.setContractSignedAt(lead.getContractSignedAt());
    response.setContractNo(lead.getContractNo());
    response.setContractStatus(lead.getContractStatus());
    response.setFlowStage(lead.getFlowStage());
    response.setCurrentOwnerDept(lead.getCurrentOwnerDept());
    response.setContractExpiryDate(lead.getContractExpiryDate() == null ? null : lead.getContractExpiryDate().toString());
    response.setSmsSendCount(lead.getSmsSendCount());
    response.setNextFollowDate(lead.getNextFollowDate() == null ? null : lead.getNextFollowDate().toString());
    response.setRemark(lead.getRemark());
    response.setCreateTime(lead.getCreateTime());
    response.setUpdateTime(lead.getUpdateTime());
    return response;
  }

  private LocalDate parseDate(String date) {
    if (date == null || date.isBlank()) {
      return null;
    }
    return LocalDate.parse(date.trim());
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

  private String normalizeSource(String source) {
    if (source == null || source.isBlank()) {
      return null;
    }
    String value = source.trim().replace("　", " ").replace(" ", "");
    String lower = value.toLowerCase(Locale.ROOT);
    if (lower.contains("抖音") || lower.contains("douyin") || lower.contains("dy")) {
      return "抖音";
    }
    if (lower.contains("微信") || lower.contains("weixin") || lower.contains("wx")) {
      return "微信";
    }
    if (lower.contains("转介绍") || lower.contains("介绍")) {
      return "转介绍";
    }
    if (lower.contains("自然到访") || lower.contains("到访")) {
      return "自然到访";
    }
    if (lower.contains("线上") || lower.contains("官网") || lower.contains("小程序")) {
      return "线上咨询";
    }
    if (lower.contains("社区")) {
      return "社区活动";
    }
    if ("其他".equals(value) || "other".equals(lower)) {
      return "其他";
    }
    return value;
  }

  private Integer normalizeSignedFlag(Integer contractSignedFlag) {
    return contractSignedFlag != null && contractSignedFlag == 1 ? 1 : 0;
  }

  private LocalDateTime resolveContractTime(LocalDateTime signedAt, Integer flag) {
    if (flag == null || flag != 1) {
      return null;
    }
    return signedAt == null ? LocalDateTime.now() : signedAt;
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private BigDecimal normalizeAmount(BigDecimal value) {
    return value == null || value.compareTo(BigDecimal.ZERO) < 0 ? null : value;
  }

  private Integer normalizeBooleanFlag(Integer value) {
    return value != null && value == 1 ? 1 : 0;
  }

  private String defaultLeadName(CrmLeadRequest request) {
    String value = blankToNull(request.getElderName());
    if (value != null) {
      return value;
    }
    value = blankToNull(request.getName());
    if (value != null) {
      return value;
    }
    value = blankToNull(request.getConsultantName());
    return value == null ? "未命名线索" : value;
  }

  private String defaultLeadPhone(CrmLeadRequest request) {
    String value = blankToNull(request.getElderPhone());
    if (value != null) {
      return value;
    }
    value = blankToNull(request.getPhone());
    if (value != null) {
      return value;
    }
    return blankToNull(request.getConsultantPhone());
  }

  private String resolveContractNo(CrmLeadRequest request, CrmLead existingLead) {
    String existingContractNo = existingLead == null ? null : blankToNull(existingLead.getContractNo());
    String requestedContractNo = blankToNull(request.getContractNo());
    if (requestedContractNo != null) {
      return requestedContractNo;
    }
    if (existingContractNo != null) {
      return existingContractNo;
    }
    if (!needGenerateContractNo(request)) {
      return null;
    }
    Long tenantId = request.getTenantId();
    if (tenantId == null) {
      return null;
    }
    return generateContractNo(tenantId);
  }

  private String resolveFlowStage(CrmLeadRequest request, CrmLead existingLead) {
    String requested = blankToNull(request.getFlowStage());
    if (requested != null) {
      return requested;
    }
    Integer signedFlag = normalizeSignedFlag(request.getContractSignedFlag());
    if (signedFlag == 1) {
      return FLOW_SIGNED;
    }
    if (existingLead != null) {
      String existingStage = blankToNull(existingLead.getFlowStage());
      if (existingStage != null) {
        return existingStage;
      }
    }
    return FLOW_PENDING_ASSESSMENT;
  }

  private String resolveOwnerDept(String flowStage, String fallback) {
    String stage = blankToNull(flowStage);
    if (FLOW_PENDING_ASSESSMENT.equals(stage)) {
      return "ASSESSMENT";
    }
    if (FLOW_PENDING_BED_SELECT.equals(stage) || FLOW_PENDING_SIGN.equals(stage) || FLOW_SIGNED.equals(stage)) {
      return "MARKETING";
    }
    return fallback;
  }

  private boolean needGenerateContractNo(CrmLeadRequest request) {
    if (request == null) {
      return false;
    }
    if (request.getContractSignedFlag() != null && request.getContractSignedFlag() == 1) {
      return true;
    }
    if (request.getStatus() != null && request.getStatus() >= 2) {
      return true;
    }
    if (blankToNull(request.getContractStatus()) != null) {
      return true;
    }
    return blankToNull(request.getReservationRoomNo()) != null;
  }

  private String generateContractNo(Long tenantId) {
    String prefix = "gfyy" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    CrmLead maxLead = leadMapper.selectOne(Wrappers.lambdaQuery(CrmLead.class)
        .select(CrmLead::getContractNo)
        .eq(CrmLead::getTenantId, tenantId)
        .eq(CrmLead::getIsDeleted, 0)
        .likeRight(CrmLead::getContractNo, prefix)
        .orderByDesc(CrmLead::getContractNo)
        .last("LIMIT 1"));
    String currentMax = maxLead == null ? null : maxLead.getContractNo();

    int nextSeq = 1;
    if (currentMax != null) {
      Matcher matcher = CONTRACT_SEQ_PATTERN.matcher(currentMax);
      if (matcher.matches() && prefix.equals(matcher.group(1))) {
        nextSeq = Integer.parseInt(matcher.group(2)) + 1;
      }
    }
    return prefix + String.format("%03d", nextSeq);
  }

  private void syncContractToAdmission(CrmLead lead) {
    if (lead == null || lead.getTenantId() == null) {
      return;
    }
    if (lead.getContractSignedFlag() == null || lead.getContractSignedFlag() != 1) {
      return;
    }
    String contractNo = blankToNull(lead.getContractNo());
    if (contractNo == null) {
      return;
    }
    ElderProfile elder = findLinkedElder(lead);
    if (elder == null) {
      return;
    }

    ElderAdmission admission = admissionMapper.selectOne(Wrappers.lambdaQuery(ElderAdmission.class)
        .eq(ElderAdmission::getTenantId, lead.getTenantId())
        .eq(ElderAdmission::getElderId, elder.getId())
        .eq(ElderAdmission::getIsDeleted, 0)
        .orderByDesc(ElderAdmission::getAdmissionDate)
        .orderByDesc(ElderAdmission::getCreateTime)
        .last("LIMIT 1"));

    if (admission == null) {
      admission = new ElderAdmission();
      admission.setTenantId(lead.getTenantId());
      admission.setOrgId(lead.getOrgId());
      admission.setElderId(elder.getId());
      LocalDate admissionDate = lead.getContractSignedAt() == null ? elder.getAdmissionDate() : lead.getContractSignedAt().toLocalDate();
      admission.setAdmissionDate(admissionDate == null ? LocalDate.now() : admissionDate);
      admission.setContractNo(contractNo);
      admission.setDepositAmount(lead.getReservationAmount());
      admission.setRemark("营销签约自动同步");
      admission.setCreatedBy(lead.getCreatedBy());
      admissionMapper.insert(admission);
      return;
    }

    admission.setContractNo(contractNo);
    if (admission.getDepositAmount() == null && lead.getReservationAmount() != null) {
      admission.setDepositAmount(lead.getReservationAmount());
    }
    admissionMapper.updateById(admission);
  }

  private ElderProfile findLinkedElder(CrmLead lead) {
    if (lead.getIdCardNo() != null && !lead.getIdCardNo().isBlank()) {
      ElderProfile byIdCard = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
          .eq(ElderProfile::getTenantId, lead.getTenantId())
          .eq(ElderProfile::getIsDeleted, 0)
          .eq(ElderProfile::getIdCardNo, lead.getIdCardNo())
          .last("LIMIT 1"));
      if (byIdCard != null) {
        return byIdCard;
      }
    }

    String elderName = blankToNull(lead.getElderName());
    String elderPhone = blankToNull(lead.getElderPhone());
    if (elderName == null || elderPhone == null) {
      return null;
    }
    return elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getTenantId, lead.getTenantId())
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(ElderProfile::getFullName, elderName)
        .eq(ElderProfile::getPhone, elderPhone)
        .orderByDesc(ElderProfile::getAdmissionDate)
        .orderByDesc(ElderProfile::getCreateTime)
        .last("LIMIT 1"));
  }
}
