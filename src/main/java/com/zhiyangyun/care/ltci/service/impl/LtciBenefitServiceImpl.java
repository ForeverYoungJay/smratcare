package com.zhiyangyun.care.ltci.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.ltci.entity.LtciBenefit;
import com.zhiyangyun.care.ltci.entity.LtciInsured;
import com.zhiyangyun.care.ltci.entity.LtciServicePackage;
import com.zhiyangyun.care.ltci.entity.LtciServiceRecord;
import com.zhiyangyun.care.ltci.entity.LtciSettlement;
import com.zhiyangyun.care.ltci.mapper.LtciBenefitMapper;
import com.zhiyangyun.care.ltci.mapper.LtciInsuredMapper;
import com.zhiyangyun.care.ltci.mapper.LtciServicePackageMapper;
import com.zhiyangyun.care.ltci.mapper.LtciServiceRecordMapper;
import com.zhiyangyun.care.ltci.mapper.LtciSettlementMapper;
import com.zhiyangyun.care.ltci.model.LtciBenefitRequest;
import com.zhiyangyun.care.ltci.model.LtciInsuredRequest;
import com.zhiyangyun.care.ltci.model.LtciServiceRecordRequest;
import com.zhiyangyun.care.ltci.model.LtciSettleResult;
import com.zhiyangyun.care.ltci.service.LtciBenefitService;
import com.zhiyangyun.care.ltci.service.LtciSettlementCalculator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class LtciBenefitServiceImpl implements LtciBenefitService {

  private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyyMM");

  private final LtciInsuredMapper insuredMapper;
  private final LtciBenefitMapper benefitMapper;
  private final LtciServicePackageMapper packageMapper;
  private final LtciServiceRecordMapper serviceRecordMapper;
  private final LtciSettlementMapper settlementMapper;
  private final ObjectMapper objectMapper;

  public LtciBenefitServiceImpl(LtciInsuredMapper insuredMapper, LtciBenefitMapper benefitMapper,
      LtciServicePackageMapper packageMapper, LtciServiceRecordMapper serviceRecordMapper,
      LtciSettlementMapper settlementMapper, ObjectMapper objectMapper) {
    this.insuredMapper = insuredMapper;
    this.benefitMapper = benefitMapper;
    this.packageMapper = packageMapper;
    this.serviceRecordMapper = serviceRecordMapper;
    this.settlementMapper = settlementMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public Long registerInsured(LtciInsuredRequest request) {
    LtciInsured insured = new LtciInsured();
    insured.setOrgId(AuthContext.getOrgId());
    insured.setElderId(request.getElderId());
    insured.setInsuredNo(request.getInsuredNo());
    insured.setIdCard(request.getIdCard());
    insured.setCityCode(request.getCityCode());
    insured.setInsureStatus("ACTIVE");
    insured.setStartDate(request.getStartDate());
    insured.setEndDate(request.getEndDate());
    insured.setRemark(request.getRemark());
    insured.setCreatedBy(AuthContext.getStaffId());
    insuredMapper.insert(insured);
    return insured.getId();
  }

  @Override
  public Long grantBenefit(LtciBenefitRequest request) {
    LtciBenefit benefit = new LtciBenefit();
    benefit.setOrgId(AuthContext.getOrgId());
    benefit.setInsuredId(request.getInsuredId());
    benefit.setElderId(request.getElderId());
    benefit.setAssessmentId(request.getAssessmentId());
    benefit.setDisabilityLevel(request.getDisabilityLevel());
    benefit.setBenefitType(StringUtils.hasText(request.getBenefitType())
        ? request.getBenefitType() : "INSTITUTION");
    benefit.setDailyQuota(request.getDailyQuota() == null ? 0L : request.getDailyQuota());
    benefit.setPayRatio(request.getPayRatio());
    benefit.setBenefitStatus("ACTIVE");
    benefit.setValidStart(request.getValidStart());
    benefit.setValidEnd(request.getValidEnd());
    benefit.setRemark(request.getRemark());
    benefit.setCreatedBy(AuthContext.getStaffId());
    benefitMapper.insert(benefit);
    return benefit.getId();
  }

  @Override
  public Long addServiceRecord(LtciServiceRecordRequest request) {
    LtciServiceRecord record = new LtciServiceRecord();
    record.setOrgId(AuthContext.getOrgId());
    record.setElderId(request.getElderId());
    record.setBenefitId(request.getBenefitId());
    record.setPackageId(request.getPackageId());
    record.setServiceDate(request.getServiceDate());
    record.setItemCode(request.getItemCode());
    record.setItemName(request.getItemName());
    record.setQuantity(request.getQuantity() == null ? 1 : request.getQuantity());
    record.setFee(request.getFee() == null ? 0L : request.getFee());
    record.setOperatorId(request.getOperatorId() != null
        ? request.getOperatorId() : AuthContext.getStaffId());
    record.setSignUrl(request.getSignUrl());
    record.setSettled(0);
    record.setRemark(request.getRemark());
    record.setCreatedBy(AuthContext.getStaffId());
    serviceRecordMapper.insert(record);
    return record.getId();
  }

  @Override
  public List<LtciServicePackage> listPackages(Long orgId) {
    return packageMapper.selectList(Wrappers.lambdaQuery(LtciServicePackage.class)
        .eq(LtciServicePackage::getIsDeleted, 0)
        .eq(LtciServicePackage::getStatus, 1)
        .and(w -> w.isNull(LtciServicePackage::getOrgId)
            .or(orgId != null, q -> q.eq(LtciServicePackage::getOrgId, orgId)))
        .orderByAsc(LtciServicePackage::getPackageCode));
  }

  @Override
  @Transactional
  public LtciSettlement generateSettlement(Long orgId, Long elderId, String month) {
    YearMonth ym = parseMonth(month);
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();

    List<LtciServiceRecord> records = serviceRecordMapper.selectList(
        Wrappers.lambdaQuery(LtciServiceRecord.class)
            .eq(LtciServiceRecord::getIsDeleted, 0)
            .eq(LtciServiceRecord::getOrgId, orgId)
            .eq(LtciServiceRecord::getElderId, elderId)
            .ge(LtciServiceRecord::getServiceDate, start)
            .le(LtciServiceRecord::getServiceDate, end));

    long totalFee = 0L;
    Set<LocalDate> days = new HashSet<>();
    for (LtciServiceRecord r : records) {
      totalFee += r.getFee() == null ? 0L : r.getFee();
      if (r.getServiceDate() != null) {
        days.add(r.getServiceDate());
      }
    }
    int serviceDays = days.size();

    LtciBenefit benefit = activeBenefit(orgId, elderId, start, end);
    long dailyQuota = benefit != null && benefit.getDailyQuota() != null ? benefit.getDailyQuota() : 0L;
    var payRatio = benefit != null ? benefit.getPayRatio() : null;
    LtciSettleResult calc = LtciSettlementCalculator.calculate(totalFee, dailyQuota, serviceDays, payRatio);

    LtciSettlement existing = settlementMapper.selectOne(Wrappers.lambdaQuery(LtciSettlement.class)
        .eq(LtciSettlement::getIsDeleted, 0)
        .eq(LtciSettlement::getOrgId, orgId)
        .eq(LtciSettlement::getElderId, elderId)
        .eq(LtciSettlement::getSettleMonth, ym.format(MONTH_FMT))
        .last("limit 1"));
    if (existing != null && !"DRAFT".equals(existing.getSettleStatus())) {
      throw new IllegalStateException("该月结算已提交，不能重复生成: " + ym.format(MONTH_FMT));
    }

    LtciSettlement settlement = existing != null ? existing : new LtciSettlement();
    settlement.setOrgId(orgId);
    settlement.setElderId(elderId);
    settlement.setBenefitId(benefit != null ? benefit.getId() : null);
    settlement.setSettleMonth(ym.format(MONTH_FMT));
    settlement.setServiceDays(serviceDays);
    settlement.setTotalFee(calc.getTotalFee());
    settlement.setFundPay(calc.getFundPay());
    settlement.setSelfPay(calc.getSelfPay());
    settlement.setOverQuota(calc.getOverQuota());
    settlement.setDailyQuota(dailyQuota);
    settlement.setPayRatio(payRatio);
    settlement.setSettleStatus("DRAFT");
    settlement.setDetailJson(buildDetail(records.size(), serviceDays, calc));
    if (existing == null) {
      settlement.setCreatedBy(AuthContext.getStaffId());
      settlementMapper.insert(settlement);
    } else {
      settlementMapper.updateById(settlement);
    }
    return settlement;
  }

  @Override
  public int generateMonthlyDrafts(Long orgId, String month) {
    YearMonth ym = parseMonth(month);
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();
    List<LtciBenefit> benefits = benefitMapper.selectList(Wrappers.lambdaQuery(LtciBenefit.class)
        .eq(LtciBenefit::getIsDeleted, 0)
        .eq(LtciBenefit::getOrgId, orgId)
        .eq(LtciBenefit::getBenefitStatus, "ACTIVE")
        .le(LtciBenefit::getValidStart, end));
    Set<Long> elderIds = new HashSet<>();
    for (LtciBenefit b : benefits) {
      if (b.getValidEnd() == null || !b.getValidEnd().isBefore(start)) {
        elderIds.add(b.getElderId());
      }
    }
    int count = 0;
    for (Long elderId : elderIds) {
      generateSettlement(orgId, elderId, month);
      count++;
    }
    return count;
  }

  @Override
  public int generateAllOrgMonthlyDrafts(String month) {
    List<LtciBenefit> benefits = benefitMapper.selectList(Wrappers.lambdaQuery(LtciBenefit.class)
        .eq(LtciBenefit::getIsDeleted, 0)
        .eq(LtciBenefit::getBenefitStatus, "ACTIVE")
        .select(LtciBenefit::getOrgId));
    Set<Long> orgIds = new HashSet<>();
    for (LtciBenefit b : benefits) {
      if (b.getOrgId() != null) {
        orgIds.add(b.getOrgId());
      }
    }
    int count = 0;
    for (Long orgId : orgIds) {
      count += generateMonthlyDrafts(orgId, month);
    }
    return count;
  }

  @Override
  @Transactional
  public void submitSettlement(Long settlementId) {
    LtciSettlement settlement = settlementMapper.selectById(settlementId);
    if (settlement == null || Integer.valueOf(1).equals(settlement.getIsDeleted())) {
      throw new IllegalArgumentException("结算单不存在: " + settlementId);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(settlement.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的结算单");
    }
    if (!"DRAFT".equals(settlement.getSettleStatus())) {
      throw new IllegalStateException("仅草稿(DRAFT)结算单可提交，当前=" + settlement.getSettleStatus());
    }
    settlement.setSettleStatus("SUBMITTED");
    settlement.setSettleNo("LTCI" + settlement.getSettleMonth() + "-" + settlement.getElderId());
    settlement.setSubmittedBy(AuthContext.getStaffId());
    settlement.setSubmittedAt(LocalDateTime.now());
    settlementMapper.updateById(settlement);

    YearMonth ym = parseMonth(settlement.getSettleMonth());
    LtciServiceRecord patch = new LtciServiceRecord();
    patch.setSettled(1);
    patch.setSettleMonth(settlement.getSettleMonth());
    serviceRecordMapper.update(patch, Wrappers.lambdaUpdate(LtciServiceRecord.class)
        .eq(LtciServiceRecord::getIsDeleted, 0)
        .eq(LtciServiceRecord::getOrgId, settlement.getOrgId())
        .eq(LtciServiceRecord::getElderId, settlement.getElderId())
        .ge(LtciServiceRecord::getServiceDate, ym.atDay(1))
        .le(LtciServiceRecord::getServiceDate, ym.atEndOfMonth()));
  }

  @Override
  public IPage<LtciSettlement> pageSettlements(long pageNo, long pageSize, Long elderId,
      String month, String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(LtciSettlement.class)
        .eq(LtciSettlement::getIsDeleted, 0)
        .eq(orgId != null, LtciSettlement::getOrgId, orgId)
        .eq(elderId != null, LtciSettlement::getElderId, elderId)
        .eq(StringUtils.hasText(month), LtciSettlement::getSettleMonth, month)
        .eq(StringUtils.hasText(status), LtciSettlement::getSettleStatus, status)
        .orderByDesc(LtciSettlement::getSettleMonth)
        .orderByDesc(LtciSettlement::getId);
    return settlementMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  private LtciBenefit activeBenefit(Long orgId, Long elderId, LocalDate start, LocalDate end) {
    return benefitMapper.selectOne(Wrappers.lambdaQuery(LtciBenefit.class)
        .eq(LtciBenefit::getIsDeleted, 0)
        .eq(LtciBenefit::getOrgId, orgId)
        .eq(LtciBenefit::getElderId, elderId)
        .eq(LtciBenefit::getBenefitStatus, "ACTIVE")
        .le(LtciBenefit::getValidStart, end)
        .and(w -> w.isNull(LtciBenefit::getValidEnd).or().ge(LtciBenefit::getValidEnd, start))
        .orderByDesc(LtciBenefit::getValidStart)
        .orderByDesc(LtciBenefit::getId)
        .last("limit 1"));
  }

  private YearMonth parseMonth(String month) {
    if (!StringUtils.hasText(month) || month.length() != 6) {
      throw new IllegalArgumentException("月份格式应为 YYYYMM: " + month);
    }
    try {
      return YearMonth.parse(month, MONTH_FMT);
    } catch (Exception ex) {
      throw new IllegalArgumentException("非法月份: " + month, ex);
    }
  }

  private String buildDetail(int recordCount, int serviceDays, LtciSettleResult calc) {
    Map<String, Object> detail = new LinkedHashMap<>();
    detail.put("recordCount", recordCount);
    detail.put("serviceDays", serviceDays);
    detail.put("totalFee", calc.getTotalFee());
    detail.put("quotaCap", calc.getQuotaCap());
    detail.put("fundPay", calc.getFundPay());
    detail.put("selfPay", calc.getSelfPay());
    detail.put("overQuota", calc.getOverQuota());
    try {
      return objectMapper.writeValueAsString(detail);
    } catch (Exception ex) {
      return null;
    }
  }
}
