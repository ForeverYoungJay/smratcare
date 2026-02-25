package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.model.CrmLeadRequest;
import com.zhiyangyun.care.crm.model.CrmLeadResponse;
import com.zhiyangyun.care.crm.service.CrmLeadService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class CrmLeadServiceImpl implements CrmLeadService {
  private final CrmLeadMapper leadMapper;

  public CrmLeadServiceImpl(CrmLeadMapper leadMapper) {
    this.leadMapper = leadMapper;
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
    lead.setContractSignedAt(resolveContractTime(request.getContractSignedAt(), request.getContractSignedFlag()));
    lead.setContractNo(blankToNull(request.getContractNo()));
    lead.setContractStatus(blankToNull(request.getContractStatus()));
    lead.setContractExpiryDate(parseDate(request.getContractExpiryDate()));
    lead.setSmsSendCount(request.getSmsSendCount() == null ? 0 : Math.max(0, request.getSmsSendCount()));
    lead.setNextFollowDate(parseDate(request.getNextFollowDate()));
    lead.setRemark(request.getRemark());
    lead.setCreatedBy(request.getCreatedBy());
    leadMapper.insert(lead);
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
    lead.setContractSignedAt(resolveContractTime(request.getContractSignedAt(), request.getContractSignedFlag()));
    lead.setContractNo(blankToNull(request.getContractNo()));
    lead.setContractStatus(blankToNull(request.getContractStatus()));
    lead.setContractExpiryDate(parseDate(request.getContractExpiryDate()));
    lead.setSmsSendCount(request.getSmsSendCount() == null ? 0 : Math.max(0, request.getSmsSendCount()));
    lead.setNextFollowDate(parseDate(request.getNextFollowDate()));
    lead.setRemark(request.getRemark());
    leadMapper.updateById(lead);
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
                                     String contractNo, String contractStatus) {
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
    LocalDate consultFrom = parseDate(consultDateFrom);
    LocalDate consultTo = parseDate(consultDateTo);
    if (consultFrom != null) {
      wrapper.ge(CrmLead::getConsultDate, consultFrom);
    }
    if (consultTo != null) {
      wrapper.le(CrmLead::getConsultDate, consultTo);
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
    response.setContractExpiryDate(lead.getContractExpiryDate() == null ? null : lead.getContractExpiryDate().toString());
    response.setSmsSendCount(lead.getSmsSendCount());
    response.setNextFollowDate(lead.getNextFollowDate() == null ? null : lead.getNextFollowDate().toString());
    response.setRemark(lead.getRemark());
    response.setCreateTime(lead.getCreateTime());
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
}
