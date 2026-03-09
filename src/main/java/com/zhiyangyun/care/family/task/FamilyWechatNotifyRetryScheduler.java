package com.zhiyangyun.care.family.task;

import com.zhiyangyun.care.family.config.FamilyPortalProperties;
import com.zhiyangyun.care.family.service.FamilyWechatNotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FamilyWechatNotifyRetryScheduler {
  private static final Logger log = LoggerFactory.getLogger(FamilyWechatNotifyRetryScheduler.class);

  private final FamilyWechatNotifyService familyWechatNotifyService;
  private final FamilyPortalProperties familyPortalProperties;

  public FamilyWechatNotifyRetryScheduler(FamilyWechatNotifyService familyWechatNotifyService,
      FamilyPortalProperties familyPortalProperties) {
    this.familyWechatNotifyService = familyWechatNotifyService;
    this.familyPortalProperties = familyPortalProperties;
  }

  @Scheduled(cron = "${app.family.wechat-notify.retry-cron:0 */5 * * * ?}")
  public void retryFailedNotifications() {
    FamilyPortalProperties.WechatNotify notify = familyPortalProperties.getWechatNotify();
    if (notify == null || !notify.isEnabled()) {
      return;
    }
    try {
      int retried = familyWechatNotifyService.retryFailedNotifications();
      if (retried > 0) {
        log.info("family-wechat-notify retry completed, retried={}", retried);
      }
    } catch (Exception ex) {
      log.error("family-wechat-notify retry failed", ex);
    }
  }
}
