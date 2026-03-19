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
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import com.zhiyangyun.care.store.entity.ElderProfile;
import com.zhiyangyun.care.store.mapper.ElderProfileMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
  private final CrmContractMapper crmContractMapper;
  private final CrmLeadMapper crmLeadMapper;

  public BillServiceImpl(
      BillMonthlyMapper billMonthlyMapper,
      BillItemMapper billItemMapper,
      ElderProfileMapper elderMapper,
      StoreOrderMapper orderMapper,
      BillingConfigService billingConfigService,
      ElderAdmissionMapper elderAdmissionMapper,
      CrmContractMapper crmContractMapper,
      CrmLeadMapper crmLeadMapper) {
    this.billMonthlyMapper = billMonthlyMapper;
    this.billItemMapper = billItemMapper;
    this.elderMapper = elderMapper;
    this.orderMapper = orderMapper;
    this.billingConfigService = billingConfigService;
    this.elderAdmissionMapper = elderAdmissionMapper;
    this.crmContractMapper = crmContractMapper;
    this.crmLeadMapper = crmLeadMapper;
  }

  @Override
  @Transactional
  public BillGenerateResponse generateMonthlyBills(String billMonth) {
    List<ElderProfile> elders = elderMapper.selectList(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getStatus, 1));

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
      response.getBillIds().add(result.billId);
      if (result.generated) {
        response.setGeneratedCount(response.getGeneratedCount() + 1);
      } else {
        response.setSkippedCount(response.getSkippedCount() + 1);
      }
    }

    if (response.getSkippedCount() > 0) {
      response.setMessage("Generated monthly bills; skipped elders without signed contracts: " + response.getSkippedCount());
    } else {
      response.setMessage("Generated monthly bills");
    }
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
    BillMonthly existing = billMonthlyMapper.selectOne(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getOrgId, elder.getOrgId())
            .eq(BillMonthly::getElderId, elder.getId())
            .eq(BillMonthly::getBillMonth, billMonth)
            .last("LIMIT 1"));
    if (existing != null) {
      boolean changed = applyContractSnapshot(existing, contractId, contractNo);
      Long itemCount = billItemMapper.selectCount(
          Wrappers.lambdaQuery(BillItem.class)
              .eq(BillItem::getBillMonthlyId, existing.getId())
              .eq(BillItem::getIsDeleted, 0));
      if (itemCount != null && itemCount > 0) {
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
      BigDecimal total = appendBillItems(existing, elder, startDate, endDate, ym, billMonth);
      existing.setTotalAmount(total);
      if (existing.getPaidAmount() == null) {
        existing.setPaidAmount(BigDecimal.ZERO);
      }
      existing.setOutstandingAmount(total.subtract(existing.getPaidAmount()));
      billMonthlyMapper.updateById(existing);
      return new EnsureBillResult(existing.getId(), true);
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
    billMonthlyMapper.insert(monthly);

    BigDecimal total = appendBillItems(monthly, elder, startDate, endDate, ym, billMonth);
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

  private BigDecimal appendBillItems(BillMonthly monthly, ElderProfile elder, LocalDate startDate,
      LocalDate endDate, YearMonth ym, String billMonth) {
    List<BillItem> items = new ArrayList<>();

    BillItem bedFee = new BillItem();
    bedFee.setOrgId(elder.getOrgId());
    bedFee.setBillMonthlyId(monthly.getId());
    bedFee.setItemType("BED");
    bedFee.setItemName("床位费");
    BigDecimal bedFeeAmount = billingConfigService.getConfigValue(
        elder.getOrgId(), "BED_FEE_MONTHLY", billMonth);
    bedFee.setAmount(bedFeeAmount);
    bedFee.setRemark("config=BED_FEE_MONTHLY");
    items.add(bedFee);

    BigDecimal careFee = resolveCareFee(elder.getOrgId(), elder.getCareLevel(), billMonth);
    BillItem careFeeItem = new BillItem();
    careFeeItem.setOrgId(elder.getOrgId());
    careFeeItem.setBillMonthlyId(monthly.getId());
    careFeeItem.setItemType("NURSING");
    careFeeItem.setItemName("护理费");
    careFeeItem.setAmount(careFee);
    careFeeItem.setRemark("level=" + (elder.getCareLevel() == null ? "UNKNOWN" : elder.getCareLevel()));
    items.add(careFeeItem);

    BigDecimal mealAmount = resolveMealFee(elder.getOrgId(), billMonth, ym.lengthOfMonth());
    BillItem mealItem = new BillItem();
    mealItem.setOrgId(elder.getOrgId());
    mealItem.setBillMonthlyId(monthly.getId());
    mealItem.setItemType("MEAL");
    mealItem.setItemName("餐费");
    mealItem.setAmount(mealAmount);
    BigDecimal monthlyMeal = billingConfigService.getConfigValue(
        elder.getOrgId(), "MEAL_FEE_MONTHLY", billMonth);
    mealItem.setRemark(monthlyMeal.compareTo(BigDecimal.ZERO) > 0
        ? "config=MEAL_FEE_MONTHLY"
        : "config=MEAL_FEE_PER_DAY days=" + ym.lengthOfMonth());
    items.add(mealItem);

    BigDecimal shopAmount = calculateShopAmount(elder.getOrgId(), elder.getId(), startDate, endDate);
    BillItem shopItem = new BillItem();
    shopItem.setOrgId(elder.getOrgId());
    shopItem.setBillMonthlyId(monthly.getId());
    shopItem.setItemType("SHOP");
    shopItem.setItemName("商城消费");
    shopItem.setAmount(shopAmount);
    shopItem.setRemark("orders in month");
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
            .eq(BillMonthly::getBillMonth, billMonth));
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
    BillMonthly monthly = billMonthlyMapper.selectById(billId);
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
            .eq(BillItem::getBillMonthlyId, billId));
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

  private BigDecimal resolveCareFee(Long orgId, String careLevel, String billMonth) {
    if (careLevel == null) {
      return BigDecimal.ZERO;
    }
    Map<String, BigDecimal> map = billingConfigService.getCareLevelFees(orgId, billMonth);
    return map.getOrDefault(careLevel, BigDecimal.ZERO);
  }

  private BigDecimal resolveMealFee(Long orgId, String billMonth, int daysInMonth) {
    BigDecimal monthly = billingConfigService.getConfigValue(orgId, "MEAL_FEE_MONTHLY", billMonth);
    if (monthly.compareTo(BigDecimal.ZERO) > 0) {
      return monthly;
    }
    BigDecimal perDay = billingConfigService.getConfigValue(orgId, "MEAL_FEE_PER_DAY", billMonth);
    if (perDay.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return perDay.multiply(BigDecimal.valueOf(daysInMonth));
  }

  private BigDecimal calculateShopAmount(Long orgId, Long elderId, LocalDate start, LocalDate end) {
    LocalDateTime startTime = start.atStartOfDay();
    LocalDateTime endTime = end.plusDays(1).atStartOfDay();
    List<StoreOrder> orders = orderMapper.selectList(
        Wrappers.lambdaQuery(StoreOrder.class)
            .eq(StoreOrder::getOrgId, orgId)
            .eq(StoreOrder::getElderId, elderId)
            .eq(StoreOrder::getPayStatus, 1)
            .ge(StoreOrder::getCreateTime, startTime)
            .lt(StoreOrder::getCreateTime, endTime));
    BigDecimal total = BigDecimal.ZERO;
    for (StoreOrder order : orders) {
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
}
