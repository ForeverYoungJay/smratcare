package com.zhiyangyun.care.family.config;

import com.zhiyangyun.care.common.file.FileStorageProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class FamilyPortalStartupValidator implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(FamilyPortalStartupValidator.class);

  private final FamilyPortalProperties familyPortalProperties;
  private final FileStorageProperties fileStorageProperties;
  private final Environment environment;

  public FamilyPortalStartupValidator(FamilyPortalProperties familyPortalProperties,
      FileStorageProperties fileStorageProperties,
      Environment environment) {
    this.familyPortalProperties = familyPortalProperties;
    this.fileStorageProperties = fileStorageProperties;
    this.environment = environment;
  }

  @Override
  public void run(ApplicationArguments args) {
    validateWechatPay();
    validateWechatNotify();
    validateSmsCode();
    validateFileStorage();
  }

  private boolean isProdProfile() {
    return Arrays.stream(environment.getActiveProfiles())
        .anyMatch(profile -> "prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile));
  }

  private void validateWechatPay() {
    FamilyPortalProperties.WechatPay wechatPay = familyPortalProperties.getWechatPay();
    if (wechatPay == null || !wechatPay.isEnabled()) {
      return;
    }
    List<String> missing = new ArrayList<>();
    collectMissing(missing, "app.family.wechat-pay.app-id", wechatPay.getAppId());
    collectMissing(missing, "app.family.wechat-pay.app-secret", wechatPay.getAppSecret());
    collectMissing(missing, "app.family.wechat-pay.merchant-id", wechatPay.getMerchantId());
    collectMissing(missing, "app.family.wechat-pay.merchant-serial-no", wechatPay.getMerchantSerialNo());
    collectMissing(missing, "app.family.wechat-pay.private-key-pem", wechatPay.getPrivateKeyPem());
    collectMissing(missing, "app.family.wechat-pay.api-v3-key", wechatPay.getApiV3Key());
    collectMissing(missing, "app.family.wechat-pay.notify-url", wechatPay.getNotifyUrl());
    if (!missing.isEmpty()) {
      if (isProdProfile()) {
        throw new IllegalStateException("Family wechat-pay is enabled but missing config: " + String.join(", ", missing));
      }
      log.warn("Family wechat-pay is enabled but missing config: {}", String.join(", ", missing));
    }
    String notifyUrl = defaultText(wechatPay.getNotifyUrl(), "");
    if (isProdProfile() && notifyUrl.contains("localhost")) {
      throw new IllegalStateException("Family wechat-pay notify-url cannot point to localhost in production");
    }
    if (isProdProfile() && wechatPay.isSkipNotifySignatureVerify()) {
      throw new IllegalStateException("Family wechat-pay skip-notify-signature-verify must be false in production");
    }
  }

  private void validateFileStorage() {
    String provider = defaultText(fileStorageProperties.getProvider(), "local");
    if (isProdProfile() && !"oss".equalsIgnoreCase(provider)) {
      throw new IllegalStateException("File storage provider must be oss in production");
    }
    if (!"oss".equalsIgnoreCase(provider)) {
      return;
    }
    FileStorageProperties.Oss oss = fileStorageProperties.getOss();
    List<String> missing = new ArrayList<>();
    collectMissing(missing, "app.file-storage.oss.endpoint", oss == null ? null : oss.getEndpoint());
    collectMissing(missing, "app.file-storage.oss.bucket", oss == null ? null : oss.getBucket());
    collectMissing(missing, "app.file-storage.oss.access-key-id", oss == null ? null : oss.getAccessKeyId());
    collectMissing(missing, "app.file-storage.oss.access-key-secret", oss == null ? null : oss.getAccessKeySecret());
    if (!missing.isEmpty()) {
      if (isProdProfile()) {
        throw new IllegalStateException("File storage provider=oss but missing config: " + String.join(", ", missing));
      }
      log.warn("File storage provider=oss but missing config: {}", String.join(", ", missing));
    }
  }

  private void validateWechatNotify() {
    FamilyPortalProperties.WechatNotify notify = familyPortalProperties.getWechatNotify();
    if (notify == null || !notify.isEnabled()) {
      return;
    }
    List<String> missing = new ArrayList<>();
    String appId = defaultText(notify.getAppId(), familyPortalProperties.getWechatPay() == null
        ? null : familyPortalProperties.getWechatPay().getAppId());
    String appSecret = defaultText(notify.getAppSecret(), familyPortalProperties.getWechatPay() == null
        ? null : familyPortalProperties.getWechatPay().getAppSecret());
    collectMissing(missing, "app.family.wechat-notify.app-id", appId);
    collectMissing(missing, "app.family.wechat-notify.app-secret", appSecret);
    collectMissing(missing, "app.family.wechat-notify.template-id", notify.getTemplateId());
    if (!missing.isEmpty()) {
      if (isProdProfile()) {
        throw new IllegalStateException("Family wechat-notify is enabled but missing config: " + String.join(", ", missing));
      }
      log.warn("Family wechat-notify is enabled but missing config: {}", String.join(", ", missing));
    }
  }

  private void validateSmsCode() {
    FamilyPortalProperties.SmsCode smsCode = familyPortalProperties.getSmsCode();
    if (smsCode == null || !smsCode.isEnabled()) {
      return;
    }
    String provider = defaultText(smsCode.getProvider(), "mock").toLowerCase();
    if (isProdProfile() && "mock".equals(provider)) {
      throw new IllegalStateException("Family sms-code provider cannot be mock in production");
    }
    if (isProdProfile() && smsCode.isDebugReturnCode()) {
      throw new IllegalStateException("Family sms-code debug-return-code must be false in production");
    }
    if (!"aliyun".equals(provider)) {
      return;
    }
    List<String> missing = new ArrayList<>();
    collectMissing(missing, "app.family.sms-code.sign-name", smsCode.getSignName());
    collectMissing(missing, "app.family.sms-code.login-template-code", smsCode.getLoginTemplateCode());
    FamilyPortalProperties.SmsCode.Aliyun aliyun = smsCode.getAliyun();
    collectMissing(missing, "app.family.sms-code.aliyun.access-key-id", aliyun == null ? null : aliyun.getAccessKeyId());
    collectMissing(missing, "app.family.sms-code.aliyun.access-key-secret", aliyun == null ? null : aliyun.getAccessKeySecret());
    if (!missing.isEmpty()) {
      if (isProdProfile()) {
        throw new IllegalStateException("Family sms-code provider=aliyun but missing config: " + String.join(", ", missing));
      }
      log.warn("Family sms-code provider=aliyun but missing config: {}", String.join(", ", missing));
    }
  }

  private void collectMissing(List<String> missing, String key, String value) {
    if (value == null || value.isBlank()) {
      missing.add(key);
    }
  }

  private String defaultText(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value.trim();
  }
}
