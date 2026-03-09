package com.zhiyangyun.care.family.task;

import com.zhiyangyun.care.family.config.FamilyPortalProperties;
import com.zhiyangyun.care.family.service.FamilyPortalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FamilyRechargeOrderScheduler {
  private static final Logger log = LoggerFactory.getLogger(FamilyRechargeOrderScheduler.class);

  private final FamilyPortalService familyPortalService;
  private final FamilyPortalProperties familyPortalProperties;

  public FamilyRechargeOrderScheduler(FamilyPortalService familyPortalService,
      FamilyPortalProperties familyPortalProperties) {
    this.familyPortalService = familyPortalService;
    this.familyPortalProperties = familyPortalProperties;
  }

  @Scheduled(cron = "${app.family.recharge.auto-close-cron:0 */5 * * * ?}")
  public void closeExpiredRechargeOrders() {
    FamilyPortalProperties.Recharge recharge = familyPortalProperties.getRecharge();
    if (recharge == null || !recharge.isAutoCloseEnabled()) {
      return;
    }
    try {
      int affected = familyPortalService.closeExpiredRechargeOrders();
      if (affected > 0) {
        log.info("family-recharge auto-close completed, closed={}", affected);
      }
    } catch (Exception ex) {
      log.error("family-recharge auto-close failed", ex);
    }
  }

  @Scheduled(cron = "${app.family.recharge.auto-pay-settle-cron:0 */30 * * * ?}")
  public void executeAutoPaySettlements() {
    FamilyPortalProperties.Recharge recharge = familyPortalProperties.getRecharge();
    if (recharge == null || !recharge.isAutoPaySettleEnabled()) {
      return;
    }
    try {
      int settled = familyPortalService.executeAutoPaySettlements();
      if (settled > 0) {
        log.info("family-recharge auto-pay settlement completed, settled={}", settled);
      }
    } catch (Exception ex) {
      log.error("family-recharge auto-pay settlement failed", ex);
    }
  }
}
