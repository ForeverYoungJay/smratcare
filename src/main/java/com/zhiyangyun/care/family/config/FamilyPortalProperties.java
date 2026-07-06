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
  private MiniApp miniApp = new MiniApp();
  private Support support = new Support();
  private Recharge recharge = new Recharge();
  private SmsCode smsCode = new SmsCode();

  @Data
  public static class MiniApp {
    /** 是否允许微信手机号一键登录（getPhoneNumber + jscode2session）。 */
    private boolean loginEnabled = false;
    /** 小程序 appId / appSecret，留空时回退到 wechat-notify、wechat-pay 的配置。 */
    private String appId;
    private String appSecret;
    /** 一键登录时若手机号无家属账号，是否自动创建（无密码，可后续补设或直接绑定长者）。 */
    private boolean autoCreateAccount = true;
    private String jscode2sessionUrl = "https://api.weixin.qq.com/sns/jscode2session";
    private String accessTokenUrl = "https://api.weixin.qq.com/cgi-bin/token";
    private String getUserPhoneNumberUrl = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";
    private int accessTokenExpireBufferSeconds = 120;
    /**
     * 联调/E2E 专用：为 true 时，loginCode 形如 "MOCK-OPENID:xxx"、phoneCode 形如 "MOCK-PHONE:138..."
     * 会被直接解析，无需真实微信服务器。生产环境必须为 false。
     */
    private boolean mockEnabled = false;
  }

  @Data
  public static class Support {
    /** 客服信息，供家属端「帮助与反馈」等页面展示。 */
    private String organizationName = "";
    private String servicePhone = "";
    private String serviceEmail = "";
    private String serviceHours = "工作日 09:00-18:00";
    private String wechatOfficialAccount = "";
    private String address = "";
  }

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
