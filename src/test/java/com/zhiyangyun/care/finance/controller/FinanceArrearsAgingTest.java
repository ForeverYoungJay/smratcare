package com.zhiyangyun.care.finance.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.finance.model.FinanceArrearsAgingItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class FinanceArrearsAgingTest {
  private static BillMonthly bill(String month, String outstanding) {
    BillMonthly bill = new BillMonthly();
    bill.setBillMonth(month);
    bill.setOutstandingAmount(new BigDecimal(outstanding));
    return bill;
  }

  @Test
  void bucketsByDaysPastTheEndOfTheBillMonth() {
    LocalDate today = LocalDate.of(2026, 11, 17);
    List<FinanceArrearsAgingItem> result = FinanceReportController.agingBuckets(List.of(
        bill("2026-11", "100"),   // 本月未到期
        bill("2026-10", "200"),   // 10-31 起 17 天
        bill("2026-10", "50"),
        bill("2026-09", "300"),   // 47 天
        bill("2026-08", "400"),   // 78 天
        bill("2026-07", "500"),   // 109 天
        bill("bad", "999")), today);
    assertThat(result).extracting(FinanceArrearsAgingItem::getKey)
        .containsExactly("CURRENT", "D30", "D60", "D90", "D90_PLUS");
    assertThat(result).extracting(item -> item.getAmount().intValue()).containsExactly(100, 250, 300, 400, 500);
    assertThat(result.get(1).getBillCount()).isEqualTo(2);
  }
}
