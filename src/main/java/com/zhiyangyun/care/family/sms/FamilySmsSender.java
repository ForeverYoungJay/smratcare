package com.zhiyangyun.care.family.sms;

public interface FamilySmsSender {
  String providerName();

  FamilySmsSendResult send(FamilySmsSendCommand command);
}
