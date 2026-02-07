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

  public BillServiceImpl(
      BillMonthlyMapper billMonthlyMapper,
      BillItemMapper billItemMapper,
      ElderProfileMapper elderMapper,
      StoreOrderMapper orderMapper,
      BillingConfigService billingConfigService) {
    this.billMonthlyMapper = billMonthlyMapper;
    this.billItemMapper = billItemMapper;
    this.elderMapper = elderMapper;
    this.orderMapper = orderMapper;
    this.billingConfigService = billingConfigService;
  }

  @Override
  @Transactional
  public BillGenerateResponse generateMonthlyBills(String billMonth) {
    YearMonth ym = YearMonth.parse(billMonth);
    LocalDate startDate = ym.atDay(1);
    LocalDate endDate = ym.atEndOfMonth();

    List<ElderProfile> elders = elderMapper.selectList(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getStatus, 1));

    BillGenerateResponse response = new BillGenerateResponse();
    response.setBillMonth(billMonth);

    for (ElderProfile elder : elders) {
      BillMonthly existing = billMonthlyMapper.selectOne(
          Wrappers.lambdaQuery(BillMonthly.class)
              .eq(BillMonthly::getOrgId, elder.getOrgId())
              .eq(BillMonthly::getElderId, elder.getId())
              .eq(BillMonthly::getBillMonth, billMonth));
      if (existing != null) {
        response.setSkippedCount(response.getSkippedCount() + 1);
        response.getBillIds().add(existing.getId());
        continue;
      }

      BillMonthly monthly = new BillMonthly();
      monthly.setOrgId(elder.getOrgId());
      monthly.setElderId(elder.getId());
      monthly.setBillMonth(billMonth);
      monthly.setTotalAmount(BigDecimal.ZERO);
      monthly.setStatus(0);
      billMonthlyMapper.insert(monthly);

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

      monthly.setTotalAmount(total);
      billMonthlyMapper.updateById(monthly);

      response.setGeneratedCount(response.getGeneratedCount() + 1);
      response.getBillIds().add(monthly.getId());
    }

    response.setMessage("Generated monthly bills");
    return response;
  }

  @Override
  public BillDetailResponse getBillDetail(Long elderId, String billMonth) {
    BillDetailResponse response = new BillDetailResponse();
    response.setElderId(elderId);
    response.setBillMonth(billMonth);

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

    List<BillItem> items = billItemMapper.selectList(
        Wrappers.lambdaQuery(BillItem.class)
            .eq(BillItem::getBillMonthlyId, monthly.getId()));

    for (BillItem item : items) {
      BillItemDetail detail = new BillItemDetail();
      detail.setItemType(item.getItemType());
      detail.setItemName(item.getItemName());
      detail.setAmount(item.getAmount());
      detail.setBasis(item.getRemark());
      response.getItems().add(detail);
    }

    response.setMessage("OK");
    return response;
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
}
