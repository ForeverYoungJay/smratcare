package com.zhiyangyun.care.ltci.task;

import com.zhiyangyun.care.ltci.service.LtciBenefitService;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 每月 1 日为所有机构生成上月长护险结算草稿。 */
@Component
public class LtciSettlementScheduler {

  private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyyMM");

  private final LtciBenefitService benefitService;

  public LtciSettlementScheduler(LtciBenefitService benefitService) {
    this.benefitService = benefitService;
  }

  @Scheduled(cron = "0 30 0 1 * ?")
  public void generateLastMonthDrafts() {
    String month = YearMonth.now().minusMonths(1).format(MONTH_FMT);
    benefitService.generateAllOrgMonthlyDrafts(month);
  }
}
