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
      log.warn("Family wechat-pay is enabled but missing config: {}", String.join(", ", missing));
    }

    boolean prodProfile = Arrays.stream(environment.getActiveProfiles())
        .anyMatch(profile -> "prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile));
    if (prodProfile && wechatPay.isSkipNotifySignatureVerify()) {
      log.warn("Family wechat-pay skip-notify-signature-verify is TRUE in production profile, this is unsafe");
    }
  }

  private void validateFileStorage() {
    if (!"oss".equalsIgnoreCase(defaultText(fileStorageProperties.getProvider(), "local"))) {
      return;
    }
    FileStorageProperties.Oss oss = fileStorageProperties.getOss();
    List<String> missing = new ArrayList<>();
    collectMissing(missing, "app.file-storage.oss.endpoint", oss == null ? null : oss.getEndpoint());
    collectMissing(missing, "app.file-storage.oss.bucket", oss == null ? null : oss.getBucket());
    collectMissing(missing, "app.file-storage.oss.access-key-id", oss == null ? null : oss.getAccessKeyId());
    collectMissing(missing, "app.file-storage.oss.access-key-secret", oss == null ? null : oss.getAccessKeySecret());
    if (!missing.isEmpty()) {
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
      log.warn("Family wechat-notify is enabled but missing config: {}", String.join(", ", missing));
    }
  }

  private void validateSmsCode() {
    FamilyPortalProperties.SmsCode smsCode = familyPortalProperties.getSmsCode();
    if (smsCode == null || !smsCode.isEnabled()) {
      return;
    }
    String provider = defaultText(smsCode.getProvider(), "mock").toLowerCase();
    boolean prodProfile = Arrays.stream(environment.getActiveProfiles())
        .anyMatch(profile -> "prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile));
    if (prodProfile && "mock".equals(provider)) {
      log.warn("Family sms-code provider is MOCK in production profile, verification is not truly delivered");
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
