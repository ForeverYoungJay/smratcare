package com.zhiyangyun.care.nursing.task;

import com.zhiyangyun.care.nursing.service.ServiceBookingService;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ServicePlanBookingScheduler {
  private final ServiceBookingService serviceBookingService;

  public ServicePlanBookingScheduler(ServiceBookingService serviceBookingService) {
    this.serviceBookingService = serviceBookingService;
  }

  @Scheduled(cron = "0 10 0 * * ?")
  public void generateDailyBookingsFromPlans() {
    serviceBookingService.generateFromPlans(null, LocalDate.now(), false);
  }
}
