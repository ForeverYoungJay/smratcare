package com.zhiyangyun.care.family.service;

import com.zhiyangyun.care.family.model.FamilyNotifyCommand;

public interface FamilyWechatNotifyService {
  void notifyFamily(FamilyNotifyCommand command);

  int retryFailedNotifications();
}
