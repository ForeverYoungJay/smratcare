package com.zhiyangyun.care.smart.task;

import com.zhiyangyun.care.smart.service.SmartDispatchService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 定时自动派单与超时升级。 */
@Component
public class SmartDispatchScheduler {

  private final SmartDispatchService dispatchService;

  public SmartDispatchScheduler(SmartDispatchService dispatchService) {
    this.dispatchService = dispatchService;
  }

  /** 每分钟为新的高危告警自动派单。 */
  @Scheduled(cron = "0 * * * * ?")
  public void autoDispatch() {
    dispatchService.autoDispatchOpenAlerts();
  }

  /** 每分钟升级超时未响应的派单。 */
  @Scheduled(cron = "30 * * * * ?")
  public void escalate() {
    dispatchService.escalateOverdue();
  }
}
