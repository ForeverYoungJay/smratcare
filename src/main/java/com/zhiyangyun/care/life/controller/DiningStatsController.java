package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningMealOrder;
import com.zhiyangyun.care.life.mapper.DiningMealOrderMapper;
import com.zhiyangyun.care.life.model.DiningConstants;
import com.zhiyangyun.care.life.model.DiningStatsMealTypeItem;
import com.zhiyangyun.care.life.model.DiningStatsSummaryResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/life/dining/stats")
public class DiningStatsController {
  private final DiningMealOrderMapper mealOrderMapper;

  public DiningStatsController(DiningMealOrderMapper mealOrderMapper) {
    this.mealOrderMapper = mealOrderMapper;
  }

  @GetMapping("/summary")
  public Result<DiningStatsSummaryResponse> summary(
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo) {
    LocalDate toDate = dateTo == null ? LocalDate.now() : dateTo;
    LocalDate fromDate = dateFrom == null ? toDate.minusDays(6) : dateFrom;
    Long orgId = AuthContext.getOrgId();

    List<DiningMealOrder> orders = mealOrderMapper.selectList(Wrappers.lambdaQuery(DiningMealOrder.class)
        .eq(DiningMealOrder::getIsDeleted, 0)
        .eq(orgId != null, DiningMealOrder::getOrgId, orgId)
        .between(DiningMealOrder::getOrderDate, fromDate, toDate));

    long totalOrders = orders.size();
    BigDecimal totalAmount = BigDecimal.ZERO;
    long deliveredOrders = 0L;
    Map<String, Long> mealTypeCounter = new HashMap<>();

    for (DiningMealOrder order : orders) {
      if (order.getTotalAmount() != null) {
        totalAmount = totalAmount.add(order.getTotalAmount());
      }
      if (DiningConstants.ORDER_STATUS_DELIVERED.equals(order.getStatus())) {
        deliveredOrders++;
      }
      String mealType = order.getMealType() == null ? DiningConstants.MEAL_TYPE_UNKNOWN : order.getMealType();
      mealTypeCounter.put(mealType, mealTypeCounter.getOrDefault(mealType, 0L) + 1L);
    }

    BigDecimal deliveryRate = totalOrders == 0
        ? BigDecimal.ZERO
        : BigDecimal.valueOf(deliveredOrders)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

    List<DiningStatsMealTypeItem> mealTypeStats = new ArrayList<>();
    for (Map.Entry<String, Long> entry : mealTypeCounter.entrySet()) {
      mealTypeStats.add(new DiningStatsMealTypeItem(entry.getKey(), entry.getValue()));
    }
    mealTypeStats.sort(Comparator.comparingLong(DiningStatsMealTypeItem::getOrderCount).reversed());

    DiningStatsSummaryResponse response = new DiningStatsSummaryResponse();
    response.setTotalOrders(totalOrders);
    response.setTotalAmount(totalAmount);
    response.setDeliveredOrders(deliveredOrders);
    response.setDeliveryRate(deliveryRate);
    response.setMealTypeStats(mealTypeStats);
    return Result.ok(response);
  }
}
