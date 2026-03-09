package com.zhiyangyun.care.family.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockFamilySmsSender implements FamilySmsSender {
  private static final Logger log = LoggerFactory.getLogger(MockFamilySmsSender.class);

  @Override
  public String providerName() {
    return "mock";
  }

  @Override
  public FamilySmsSendResult send(FamilySmsSendCommand command) {
    String payload = "{\"phone\":\"" + command.getPhone() + "\",\"scene\":\"" + command.getScene()
        + "\",\"code\":\"" + command.getCode() + "\"}";
    log.info("Mock SMS send: {}", payload);
    return FamilySmsSendResult.success(providerName(),
        "MOCK-" + System.currentTimeMillis(), payload, "{\"code\":\"OK\",\"msg\":\"mock sent\"}");
  }
}
