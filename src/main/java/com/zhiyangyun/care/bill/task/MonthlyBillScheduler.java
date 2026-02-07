package com.zhiyangyun.care.bill.task;

import com.zhiyangyun.care.bill.service.BillService;
import java.time.YearMonth;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MonthlyBillScheduler {
  private final BillService billService;

  public MonthlyBillScheduler(BillService billService) {
    this.billService = billService;
  }

  @Scheduled(cron = "0 5 0 1 * ?")
  public void generateLastMonthBills() {
    YearMonth lastMonth = YearMonth.now().minusMonths(1);
    billService.generateMonthlyBills(lastMonth.toString());
  }
}
