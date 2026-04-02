package com.zhiyangyun.care.bill.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.bill.entity.BillItem;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillItemMapper;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.bill.model.BillDetailResponse;
import com.zhiyangyun.care.bill.model.BillGenerateResponse;
import com.zhiyangyun.care.bill.model.BillItemDetail;
import com.zhiyangyun.care.bill.service.BillService;
import com.zhiyangyun.care.bill.service.BillingConfigService;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischargeApply;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderDischargeApplyMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import com.zhiyangyun.care.store.entity.ElderProfile;
import com.zhiyangyun.care.store.mapper.ElderProfileMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillServiceImpl implements BillService {
  private final BillMonthlyMapper billMonthlyMapper;
  private final BillItemMapper billItemMapper;
  private final ElderProfileMapper elderMapper;
  private final StoreOrderMapper orderMapper;
  private final BillingConfigService billingConfigService;
  private final ElderAdmissionMapper elderAdmissionMapper;
  private final ElderDischargeApplyMapper elderDischargeApplyMapper;
  private final CrmContractMapper crmContractMapper;
  private final CrmLeadMapper crmLeadMapper;

  public BillServiceImpl(
      BillMonthlyMapper billMonthlyMapper,
      BillItemMapper billItemMapper,
      ElderProfileMapper elderMapper,
      StoreOrderMapper orderMapper,
      BillingConfigService billingConfigService,
      ElderAdmissionMapper elderAdmissionMapper,
      ElderDischargeApplyMapper elderDischargeApplyMapper,
      CrmContractMapper crmContractMapper,
      CrmLeadMapper crmLeadMapper) {
    this.billMonthlyMapper = billMonthlyMapper;
    this.billItemMapper = billItemMapper;
    this.elderMapper = elderMapper;
    this.orderMapper = orderMapper;
    this.billingConfigService = billingConfigService;
    this.elderAdmissionMapper = elderAdmissionMapper;
    this.elderDischargeApplyMapper = elderDischargeApplyMapper;
    this.crmContractMapper = crmContractMapper;
    this.crmLeadMapper = crmLeadMapper;
  }

  @Override
  @Transactional
  public BillGenerateResponse generateMonthlyBills(String billMonth) {
    List<ElderProfile> elders = loadBillingCandidates(billMonth);

    BillGenerateResponse response = new BillGenerateResponse();
    response.setBillMonth(billMonth);

    for (ElderProfile elder : elders) {
      CrmContract signedContract = resolveSignedContract(elder.getOrgId(), elder.getId());
      if (signedContract == null || signedContract.getContractNo() == null) {
        response.setSkippedCount(response.getSkippedCount() + 1);
        continue;
      }
      EnsureBillResult result = ensureMonthlyBillInternal(
          elder,
          billMonth,
          signedContract.getId(),
          signedContract.getContractNo());
      if (result.billId != null) {
        response.getBillIds().add(result.billId);
      }
      if (result.generated) {
        response.setGeneratedCount(response.getGeneratedCount() + 1);
      } else {
        response.setSkippedCount(response.getSkippedCount() + 1);
      }
    }

    response.setMessage(response.getSkippedCount() > 0
        ? "Generated monthly bills; not generated " + response.getSkippedCount() + " records"
        : "Generated monthly bills");
    return response;
  }

  @Override
  @Transactional
  public BillDetailResponse ensureMonthlyBillForElder(
      Long orgId,
      Long elderId,
      String billMonth,
      Long contractId,
      String contractNo) {
    if (elderId == null || billMonth == null || billMonth.isBlank()) {
      BillDetailResponse response = new BillDetailResponse();
      response.setElderId(elderId);
      response.setBillMonth(billMonth);
      response.setMessage("Bill not found");
      return response;
    }
    ElderProfile elder = elderMapper.selectById(elderId);
    if (elder == null) {
      BillDetailResponse response = new BillDetailResponse();
      response.setElderId(elderId);
      response.setBillMonth(billMonth);
      response.setMessage("Bill not found");
      return response;
    }
    if (orgId != null && elder.getOrgId() != null && !orgId.equals(elder.getOrgId())) {
      orgId = elder.getOrgId();
    }
    EnsureBillResult result = ensureMonthlyBillInternal(
        elder,
        billMonth,
        contractId,
        contractNo);
    if (result.billId == null) {
      BillDetailResponse response = new BillDetailResponse();
      response.setElderId(elderId);
      response.setBillMonth(billMonth);
      response.setElderName(elder.getFullName());
      response.setMessage("No billable service days in month");
      return response;
    }
    return getBillDetailById(result.billId);
  }

  private CrmContract resolveSignedContract(Long orgId, Long elderId) {
    ElderAdmission admission = elderAdmissionMapper.selectOne(
        Wrappers.lambdaQuery(ElderAdmission.class)
            .eq(ElderAdmission::getOrgId, orgId)
            .eq(ElderAdmission::getElderId, elderId)
            .eq(ElderAdmission::getIsDeleted, 0)
            .isNotNull(ElderAdmission::getContractNo)
            .orderByDesc(ElderAdmission::getAdmissionDate)
            .orderByDesc(ElderAdmission::getCreateTime)
            .last("LIMIT 1"));
    if (admission == null || admission.getContractNo() == null || admission.getContractNo().isBlank()) {
      return null;
    }
    CrmContract contract = crmContractMapper.selectOne(Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getOrgId, orgId)
        .eq(CrmContract::getIsDeleted, 0)
        .eq(CrmContract::getContractNo, admission.getContractNo())
        .in(CrmContract::getStatus, List.of("SIGNED", "EFFECTIVE"))
        .orderByDesc(CrmContract::getSignedAt)
        .orderByDesc(CrmContract::getUpdateTime)
        .last("LIMIT 1"));
    if (contract != null) {
      return contract;
    }
    CrmLead lead = crmLeadMapper.selectOne(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getOrgId, orgId)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(CrmLead::getContractNo, admission.getContractNo())
        .eq(CrmLead::getContractSignedFlag, 1)
        .in(CrmLead::getContractStatus, List.of("已审批,已通过", "费用预审通过", "SIGNED", "EFFECTIVE"))
        .orderByDesc(CrmLead::getContractSignedAt)
        .orderByDesc(CrmLead::getCreateTime)
        .last("LIMIT 1"));
    if (lead == null) {
      return null;
    }
    CrmContract fallback = new CrmContract();
    fallback.setContractNo(admission.getContractNo());
    return fallback;
  }

  private EnsureBillResult ensureMonthlyBillInternal(
      ElderProfile elder,
      String billMonth,
      Long contractId,
      String contractNo) {
    YearMonth ym = YearMonth.parse(billMonth);
    LocalDate startDate = ym.atDay(1);
    LocalDate endDate = ym.atEndOfMonth();
    CrmContract contractSnapshot = loadContractSnapshot(elder.getOrgId(), contractId, contractNo);
    BillingWindow billingWindow = calculateBillingWindow(elder.getOrgId(), elder.getId(), startDate, endDate, contractSnapshot);
    if (!billingWindow.billable()) {
      return new EnsureBillResult(null, false);
    }
    BillMonthly existing = findMonthlyBillForUpdate(elder.getOrgId(), elder.getId(), billMonth);
    if (existing != null) {
      return refreshExistingMonthlyBill(existing, elder, ym, billMonth, contractId, contractNo, billingWindow);
    }

    BillMonthly monthly = new BillMonthly();
    monthly.setOrgId(elder.getOrgId());
    monthly.setElderId(elder.getId());
    monthly.setBillMonth(billMonth);
    monthly.setTotalAmount(BigDecimal.ZERO);
    monthly.setPaidAmount(BigDecimal.ZERO);
    monthly.setOutstandingAmount(BigDecimal.ZERO);
    monthly.setStatus(0);
    monthly.setElderContractId(contractId);
    monthly.setContractNoSnapshot(contractNo);
    try {
      billMonthlyMapper.insert(monthly);
    } catch (DuplicateKeyException ex) {
      BillMonthly concurrentExisting = findMonthlyBillForUpdate(elder.getOrgId(), elder.getId(), billMonth);
      if (concurrentExisting == null) {
        throw ex;
      }
      return refreshExistingMonthlyBill(concurrentExisting, elder, ym, billMonth, contractId, contractNo, billingWindow);
    }

    BigDecimal total = appendBillItems(monthly, elder, ym, billMonth, billingWindow);
    monthly.setTotalAmount(total);
    monthly.setOutstandingAmount(total);
    billMonthlyMapper.updateById(monthly);
    return new EnsureBillResult(monthly.getId(), true);
  }

  private boolean applyContractSnapshot(BillMonthly monthly, Long contractId, String contractNo) {
    boolean changed = false;
    if (monthly.getElderContractId() == null && contractId != null) {
      monthly.setElderContractId(contractId);
      changed = true;
    }
    if ((monthly.getContractNoSnapshot() == null || monthly.getContractNoSnapshot().isBlank())
        && contractNo != null && !contractNo.isBlank()) {
      monthly.setContractNoSnapshot(contractNo);
      changed = true;
    }
    return changed;
  }

  private EnsureBillResult refreshExistingMonthlyBill(BillMonthly existing, ElderProfile elder,
      YearMonth ym, String billMonth, Long contractId, String contractNo, BillingWindow billingWindow) {
    boolean changed = applyContractSnapshot(existing, contractId, contractNo);
    Long itemCount = billItemMapper.selectCount(
        Wrappers.lambdaQuery(BillItem.class)
            .eq(BillItem::getBillMonthlyId, existing.getId())
            .eq(BillItem::getIsDeleted, 0));
    if (itemCount != null && itemCount > 0 && !canRebuildBill(existing)) {
      if (existing.getTotalAmount() == null) {
        existing.setTotalAmount(BigDecimal.ZERO);
        changed = true;
      }
      if (existing.getPaidAmount() == null) {
        existing.setPaidAmount(BigDecimal.ZERO);
        changed = true;
      }
      if (existing.getOutstandingAmount() == null && existing.getTotalAmount() != null && existing.getPaidAmount() != null) {
        existing.setOutstandingAmount(existing.getTotalAmount().subtract(existing.getPaidAmount()));
        changed = true;
      }
      if (changed) {
        billMonthlyMapper.updateById(existing);
      }
      return new EnsureBillResult(existing.getId(), false);
    }
    if (itemCount != null && itemCount > 0) {
      markBillItemsDeleted(existing.getId());
    }
    BigDecimal total = appendBillItems(existing, elder, ym, billMonth, billingWindow);
    existing.setTotalAmount(total);
    if (existing.getPaidAmount() == null) {
      existing.setPaidAmount(BigDecimal.ZERO);
    }
    existing.setOutstandingAmount(total.subtract(existing.getPaidAmount()));
    if (!Integer.valueOf(9).equals(existing.getStatus())) {
      if (total.compareTo(BigDecimal.ZERO) <= 0) {
        existing.setStatus(0);
      } else if (existing.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
        existing.setStatus(0);
      } else {
        existing.setStatus(existing.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0 ? 2 : 1);
      }
    }
    billMonthlyMapper.updateById(existing);
    return new EnsureBillResult(existing.getId(), true);
  }

  private BigDecimal appendBillItems(
      BillMonthly monthly,
      ElderProfile elder,
      YearMonth ym,
      String billMonth,
      BillingWindow billingWindow) {
    List<BillItem> items = new ArrayList<>();
    BigDecimal daysRatio = billingWindow.daysRatio();

    BillItem bedFee = new BillItem();
    bedFee.setOrgId(elder.getOrgId());
    bedFee.setBillMonthlyId(monthly.getId());
    bedFee.setItemType("BED");
    bedFee.setItemName("床位费");
    BigDecimal bedFeeAmount = billingConfigService.getConfigValue(
        elder.getOrgId(), "BED_FEE_MONTHLY", billMonth);
    bedFee.setAmount(prorateAmount(bedFeeAmount, daysRatio));
    bedFee.setRemark(billingWindow.remark("config=BED_FEE_MONTHLY"));
    items.add(bedFee);

    BigDecimal careFee = resolveCareFee(elder.getOrgId(), elder.getCareLevel(), billMonth);
    BillItem careFeeItem = new BillItem();
    careFeeItem.setOrgId(elder.getOrgId());
    careFeeItem.setBillMonthlyId(monthly.getId());
    careFeeItem.setItemType("NURSING");
    careFeeItem.setItemName("护理费");
    careFeeItem.setAmount(prorateAmount(careFee, daysRatio));
    careFeeItem.setRemark(billingWindow.remark("level=" + (elder.getCareLevel() == null ? "UNKNOWN" : elder.getCareLevel())));
    items.add(careFeeItem);

    BigDecimal mealAmount = resolveMealFee(elder.getOrgId(), billMonth, billingWindow.chargeableDays(), ym.lengthOfMonth());
    BillItem mealItem = new BillItem();
    mealItem.setOrgId(elder.getOrgId());
    mealItem.setBillMonthlyId(monthly.getId());
    mealItem.setItemType("MEAL");
    mealItem.setItemName("餐费");
    mealItem.setAmount(mealAmount);
    BigDecimal monthlyMeal = billingConfigService.getConfigValue(
        elder.getOrgId(), "MEAL_FEE_MONTHLY", billMonth);
    mealItem.setRemark(monthlyMeal.compareTo(BigDecimal.ZERO) > 0
        ? billingWindow.remark("config=MEAL_FEE_MONTHLY")
        : billingWindow.remark("config=MEAL_FEE_PER_DAY"));
    items.add(mealItem);

    BigDecimal shopAmount = calculateShopAmount(elder.getOrgId(), elder.getId(), billingWindow.startDate(), billingWindow.endDate());
    BillItem shopItem = new BillItem();
    shopItem.setOrgId(elder.getOrgId());
    shopItem.setBillMonthlyId(monthly.getId());
    shopItem.setItemType("SHOP");
    shopItem.setItemName("商城消费");
    shopItem.setAmount(shopAmount);
    shopItem.setRemark("orders in billing window " + billingWindow.startDate() + "~" + billingWindow.endDate());
    items.add(shopItem);

    BigDecimal total = BigDecimal.ZERO;
    for (BillItem item : items) {
      billItemMapper.insert(item);
      total = total.add(item.getAmount());
    }
    return total;
  }

  @Override
  public BillDetailResponse getBillDetail(Long elderId, String billMonth) {
    BillDetailResponse response = new BillDetailResponse();
    response.setElderId(elderId);
    response.setBillMonth(billMonth);
    ElderProfile elder = elderMapper.selectById(elderId);
    response.setElderName(elder == null ? null : elder.getFullName());

    BillMonthly monthly = billMonthlyMapper.selectOne(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getElderId, elderId)
            .eq(BillMonthly::getBillMonth, billMonth)
            .eq(BillMonthly::getIsDeleted, 0));
    if (monthly == null) {
      response.setMessage("Bill not found");
      return response;
    }

    response.setBillId(monthly.getId());
    response.setTotalAmount(monthly.getTotalAmount());

    response.getItems().addAll(buildItemDetails(monthly.getId()));

    response.setMessage("OK");
    return response;
  }

  @Override
  public BillDetailResponse getBillDetailById(Long billId) {
    BillDetailResponse response = new BillDetailResponse();
    response.setBillId(billId);
    BillMonthly monthly = billMonthlyMapper.selectOne(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getId, billId)
            .eq(BillMonthly::getIsDeleted, 0)
            .last("LIMIT 1"));
    if (monthly == null) {
      response.setMessage("Bill not found");
      return response;
    }
    response.setElderId(monthly.getElderId());
    ElderProfile elder = elderMapper.selectById(monthly.getElderId());
    response.setElderName(elder == null ? null : elder.getFullName());
    response.setBillMonth(monthly.getBillMonth());
    response.setTotalAmount(monthly.getTotalAmount());
    response.getItems().addAll(buildItemDetails(billId));
    response.setMessage("OK");
    return response;
  }

  private List<BillItemDetail> buildItemDetails(Long billId) {
    List<BillItem> items = billItemMapper.selectList(
        Wrappers.lambdaQuery(BillItem.class)
            .eq(BillItem::getBillMonthlyId, billId)
            .eq(BillItem::getIsDeleted, 0));
    List<BillItemDetail> details = new ArrayList<>();
    for (BillItem item : items) {
      BillItemDetail detail = new BillItemDetail();
      detail.setItemType(item.getItemType());
      detail.setItemName(item.getItemName());
      detail.setAmount(item.getAmount());
      detail.setBasis(item.getRemark());
      details.add(detail);
    }
    return details;
  }

  private BillMonthly findMonthlyBillForUpdate(Long orgId, Long elderId, String billMonth) {
    return billMonthlyMapper.selectOne(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getElderId, elderId)
            .eq(BillMonthly::getBillMonth, billMonth)
            .eq(BillMonthly::getIsDeleted, 0)
            .last("LIMIT 1 FOR UPDATE"));
  }

  private BigDecimal resolveCareFee(Long orgId, String careLevel, String billMonth) {
    if (careLevel == null) {
      return BigDecimal.ZERO;
    }
    Map<String, BigDecimal> map = billingConfigService.getCareLevelFees(orgId, billMonth);
    return map.getOrDefault(careLevel, BigDecimal.ZERO);
  }

  private BigDecimal resolveMealFee(Long orgId, String billMonth, int chargeableDays, int daysInMonth) {
    BigDecimal monthly = billingConfigService.getConfigValue(orgId, "MEAL_FEE_MONTHLY", billMonth);
    if (monthly.compareTo(BigDecimal.ZERO) > 0) {
      return prorateAmount(monthly, BigDecimal.valueOf(chargeableDays)
          .divide(BigDecimal.valueOf(daysInMonth), 8, RoundingMode.HALF_UP));
    }
    BigDecimal perDay = billingConfigService.getConfigValue(orgId, "MEAL_FEE_PER_DAY", billMonth);
    if (perDay.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return perDay.multiply(BigDecimal.valueOf(chargeableDays)).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateShopAmount(Long orgId, Long elderId, LocalDate start, LocalDate end) {
    LocalDateTime startTime = start.atStartOfDay();
    LocalDateTime endTime = end.plusDays(1).atStartOfDay();
    List<StoreOrder> orders = orderMapper.selectList(
        Wrappers.lambdaQuery(StoreOrder.class)
            .eq(StoreOrder::getIsDeleted, 0)
            .eq(StoreOrder::getOrgId, orgId)
            .eq(StoreOrder::getElderId, elderId)
            .eq(StoreOrder::getPayStatus, 1));
    BigDecimal total = BigDecimal.ZERO;
    for (StoreOrder order : orders) {
      LocalDateTime effectiveTime = order.getPayTime() == null ? order.getCreateTime() : order.getPayTime();
      if (effectiveTime == null || effectiveTime.isBefore(startTime) || !effectiveTime.isBefore(endTime)) {
        continue;
      }
      if (order.getTotalAmount() != null) {
        total = total.add(order.getTotalAmount());
      }
    }
    return total;
  }

  private static final class EnsureBillResult {
    private final Long billId;
    private final boolean generated;

    private EnsureBillResult(Long billId, boolean generated) {
      this.billId = billId;
      this.generated = generated;
    }
  }

  private List<ElderProfile> loadBillingCandidates(String billMonth) {
    YearMonth ym = YearMonth.parse(billMonth);
    LocalDate monthEnd = ym.atEndOfMonth();
    Set<Long> elderIds = new HashSet<>();
    elderMapper.selectList(
            Wrappers.lambdaQuery(ElderProfile.class)
                .eq(ElderProfile::getIsDeleted, 0)
                .eq(ElderProfile::getStatus, 1))
        .stream()
        .map(ElderProfile::getId)
        .filter(id -> id != null && id > 0)
        .forEach(elderIds::add);
    elderAdmissionMapper.selectList(
            Wrappers.lambdaQuery(ElderAdmission.class)
                .eq(ElderAdmission::getIsDeleted, 0)
                .le(ElderAdmission::getAdmissionDate, monthEnd))
        .stream()
        .map(ElderAdmission::getElderId)
        .filter(id -> id != null && id > 0)
        .forEach(elderIds::add);
    if (elderIds.isEmpty()) {
      return List.of();
    }
    return elderMapper.selectList(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .in(ElderProfile::getId, elderIds));
  }

  private CrmContract loadContractSnapshot(Long orgId, Long contractId, String contractNo) {
    if (contractId != null) {
      CrmContract contract = crmContractMapper.selectOne(
          Wrappers.lambdaQuery(CrmContract.class)
              .eq(CrmContract::getId, contractId)
              .eq(CrmContract::getIsDeleted, 0)
              .eq(orgId != null, CrmContract::getOrgId, orgId)
              .last("LIMIT 1"));
      if (contract != null) {
        return contract;
      }
    }
    if (contractNo != null && !contractNo.isBlank()) {
      CrmContract contract = crmContractMapper.selectOne(
          Wrappers.lambdaQuery(CrmContract.class)
              .eq(CrmContract::getIsDeleted, 0)
              .eq(orgId != null, CrmContract::getOrgId, orgId)
              .eq(CrmContract::getContractNo, contractNo)
              .orderByDesc(CrmContract::getUpdateTime)
              .last("LIMIT 1"));
      if (contract != null) {
        return contract;
      }
      CrmContract fallback = new CrmContract();
      fallback.setContractNo(contractNo);
      return fallback;
    }
    return null;
  }

  private BillingWindow calculateBillingWindow(
      Long orgId,
      Long elderId,
      LocalDate monthStart,
      LocalDate monthEnd,
      CrmContract contract) {
    LocalDate effectiveStart = monthStart;
    LocalDate effectiveEnd = monthEnd;
    ElderAdmission admission = elderAdmissionMapper.selectOne(
        Wrappers.lambdaQuery(ElderAdmission.class)
            .eq(ElderAdmission::getIsDeleted, 0)
            .eq(orgId != null, ElderAdmission::getOrgId, orgId)
            .eq(ElderAdmission::getElderId, elderId)
            .le(ElderAdmission::getAdmissionDate, monthEnd)
            .orderByDesc(ElderAdmission::getAdmissionDate)
            .orderByDesc(ElderAdmission::getCreateTime)
            .last("LIMIT 1"));
    if (admission != null && admission.getAdmissionDate() != null && admission.getAdmissionDate().isAfter(effectiveStart)) {
      effectiveStart = admission.getAdmissionDate();
    }
    if (contract != null && contract.getEffectiveFrom() != null && contract.getEffectiveFrom().isAfter(effectiveStart)) {
      effectiveStart = contract.getEffectiveFrom();
    }
    if (contract != null && contract.getEffectiveTo() != null && contract.getEffectiveTo().isBefore(effectiveEnd)) {
      effectiveEnd = contract.getEffectiveTo();
    }
    ElderDischargeApply dischargeApply = elderDischargeApplyMapper.selectOne(
        Wrappers.lambdaQuery(ElderDischargeApply.class)
            .eq(ElderDischargeApply::getIsDeleted, 0)
            .eq(orgId != null, ElderDischargeApply::getOrgId, orgId)
            .eq(ElderDischargeApply::getElderId, elderId)
            .eq(ElderDischargeApply::getStatus, "APPROVED")
            .isNotNull(ElderDischargeApply::getPlannedDischargeDate)
            .le(ElderDischargeApply::getPlannedDischargeDate, monthEnd)
            .orderByDesc(ElderDischargeApply::getPlannedDischargeDate)
            .orderByDesc(ElderDischargeApply::getReviewedTime)
            .last("LIMIT 1"));
    if (dischargeApply != null
        && dischargeApply.getPlannedDischargeDate() != null
        && dischargeApply.getPlannedDischargeDate().isBefore(effectiveEnd)) {
      effectiveEnd = dischargeApply.getPlannedDischargeDate();
    }
    if (effectiveEnd.isBefore(effectiveStart)) {
      return BillingWindow.empty(monthStart, monthEnd);
    }
    int chargeableDays = (int) ChronoUnit.DAYS.between(effectiveStart, effectiveEnd) + 1;
    return new BillingWindow(effectiveStart, effectiveEnd, chargeableDays, monthStart.lengthOfMonth());
  }

  private BigDecimal prorateAmount(BigDecimal monthlyAmount, BigDecimal ratio) {
    if (monthlyAmount == null || ratio == null || monthlyAmount.compareTo(BigDecimal.ZERO) <= 0 || ratio.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return monthlyAmount.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
  }

  private boolean canRebuildBill(BillMonthly monthly) {
    if (monthly == null || Integer.valueOf(9).equals(monthly.getStatus())) {
      return false;
    }
    return monthly.getPaidAmount() == null || monthly.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0;
  }

  private void markBillItemsDeleted(Long billId) {
    if (billId == null) {
      return;
    }
    billItemMapper.update(
        null,
        Wrappers.lambdaUpdate(BillItem.class)
            .set(BillItem::getIsDeleted, 1)
            .eq(BillItem::getBillMonthlyId, billId)
            .eq(BillItem::getIsDeleted, 0));
  }

  private static final class BillingWindow {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int chargeableDays;
    private final int daysInMonth;

    private BillingWindow(LocalDate startDate, LocalDate endDate, int chargeableDays, int daysInMonth) {
      this.startDate = startDate;
      this.endDate = endDate;
      this.chargeableDays = chargeableDays;
      this.daysInMonth = daysInMonth;
    }

    private static BillingWindow empty(LocalDate monthStart, LocalDate monthEnd) {
      return new BillingWindow(monthStart, monthEnd, 0, monthStart.lengthOfMonth());
    }

    private boolean billable() {
      return chargeableDays > 0;
    }

    private BigDecimal daysRatio() {
      if (!billable()) {
        return BigDecimal.ZERO;
      }
      return BigDecimal.valueOf(chargeableDays)
          .divide(BigDecimal.valueOf(daysInMonth), 8, java.math.RoundingMode.HALF_UP);
    }

    private String remark(String base) {
      return base + " window=" + startDate + "~" + endDate + " days=" + chargeableDays + "/" + daysInMonth;
    }

    private LocalDate startDate() {
      return startDate;
    }

    private LocalDate endDate() {
      return endDate;
    }

    private int chargeableDays() {
      return chargeableDays;
    }
  }
}
