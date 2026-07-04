package com.zhiyangyun.care.pharmacy.task;

import com.zhiyangyun.care.pharmacy.service.PharmacyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 每日巡检药品临期与低库存并生成告警。 */
@Component
public class DrugExpiryMonitor {

  private static final int EXPIRY_WARNING_DAYS = 30;

  private final PharmacyService pharmacyService;

  public DrugExpiryMonitor(PharmacyService pharmacyService) {
    this.pharmacyService = pharmacyService;
  }

  @Scheduled(cron = "0 15 1 * * ?")
  public void scan() {
    pharmacyService.scanExpiryAndLowStock(EXPIRY_WARNING_DAYS);
  }
}
