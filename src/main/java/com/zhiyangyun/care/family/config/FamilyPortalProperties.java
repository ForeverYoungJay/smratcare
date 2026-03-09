package com.zhiyangyun.care.family.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.family")
public class FamilyPortalProperties {
  private boolean legacyApiEnabled = true;
  private String legacyApiSunsetDate = "2026-09-30";

  private WechatPay wechatPay = new WechatPay();
  private WechatNotify wechatNotify = new WechatNotify();
  private Recharge recharge = new Recharge();
  private SmsCode smsCode = new SmsCode();

  @Data
  public static class WechatPay {
    private boolean enabled = false;
    private String appId;
    private String appSecret;
    private String merchantId;
    private String merchantSerialNo;
    private String privateKeyPem;
    private String apiV3Key;
    private String notifyUrl;
    private String prepayUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
    private String queryUrlTemplate = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/{outTradeNo}?mchid={mchId}";
    private String certificatesUrl = "https://api.mch.weixin.qq.com/v3/certificates";
    private String platformSerialNo;
    private String platformCertificatePem;
    private long notifyAllowedSkewSeconds = 300;
    private boolean skipNotifySignatureVerify = true;
  }

  @Data
  public static class Recharge {
    private boolean autoCloseEnabled = true;
    private String autoCloseCron = "0 */5 * * * ?";
    private int prepayTimeoutMinutes = 30;
    private int autoCloseBatchSize = 200;
    private boolean createAbnormalTodo = true;
    private boolean autoPaySettleEnabled = true;
    private String autoPaySettleCron = "0 */30 * * * ?";
    private int autoPaySettleBatchSize = 200;
  }

  @Data
  public static class SmsCode {
    private boolean enabled = true;
    private String provider = "mock";
    private int codeLength = 6;
    private int expireSeconds = 300;
    private int cooldownSeconds = 60;
    private int dailyLimit = 20;
    private int maxAttempts = 5;
    private boolean ipGuardEnabled = true;
    private int ipCooldownSeconds = 10;
    private int ipDailyLimit = 120;
    private int ipBanThreshold = 12;
    private int ipBanMinutes = 30;
    private boolean debugReturnCode = true;
    private String codeSalt = "smartcare-family-sms";
    private String signName;
    private String loginTemplateCode;
    private String securityTemplateCode;
    private String defaultTemplateCode;
    private Aliyun aliyun = new Aliyun();

    @Data
    public static class Aliyun {
      private String regionId = "cn-hangzhou";
      private String endpoint = "dysmsapi.aliyuncs.com";
      private String accessKeyId;
      private String accessKeySecret;
    }
  }

  @Data
  public static class WechatNotify {
    private boolean enabled = false;
    private String appId;
    private String appSecret;
    private String templateId;
    private String page = "pages/messages/index";
    private String miniProgramState = "formal";
    private String language = "zh_CN";
    private String accessTokenUrl = "https://api.weixin.qq.com/cgi-bin/token";
    private String subscribeSendUrl = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";
    private String dataTemplateJson =
        "{\"thing1\":{\"value\":\"{title}\"},\"thing2\":{\"value\":\"{content}\"},\"time3\":{\"value\":\"{time}\"}}";
    private int accessTokenExpireBufferSeconds = 120;
    private int maxRetry = 3;
    private int retryIntervalMinutes = 5;
    private int retryBatchSize = 100;
    private String retryCron = "0 */5 * * * ?";
  }
}
