package com.zhiyangyun.care.medorder.task;

import com.zhiyangyun.care.medorder.service.MedicalOrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 每日为生效长期医嘱生成当日执行单。 */
@Component
public class MedicalOrderExecScheduler {

  private final MedicalOrderService orderService;

  public MedicalOrderExecScheduler(MedicalOrderService orderService) {
    this.orderService = orderService;
  }

  @Scheduled(cron = "0 20 0 * * ?")
  public void generateToday() {
    orderService.generateDailyExecutions();
  }
}
