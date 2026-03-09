package com.zhiyangyun.care.family.sms;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.zhiyangyun.care.family.config.FamilyPortalProperties;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.family.sms-code", name = "provider", havingValue = "aliyun")
public class AliyunFamilySmsSender implements FamilySmsSender {
  private final FamilyPortalProperties familyPortalProperties;

  public AliyunFamilySmsSender(FamilyPortalProperties familyPortalProperties) {
    this.familyPortalProperties = familyPortalProperties;
  }

  @Override
  public String providerName() {
    return "aliyun";
  }

  @Override
  public FamilySmsSendResult send(FamilySmsSendCommand command) {
    FamilyPortalProperties.SmsCode smsCode = familyPortalProperties.getSmsCode();
    FamilyPortalProperties.SmsCode.Aliyun aliyun = smsCode == null ? null : smsCode.getAliyun();
    if (smsCode == null || aliyun == null) {
      return FamilySmsSendResult.failed(providerName(), null, null, "短信配置缺失");
    }

    String signName = requireText(smsCode.getSignName(), "未配置短信签名");
    String templateCode = resolveTemplateCode(smsCode, command.getScene());
    String accessKeyId = requireText(aliyun.getAccessKeyId(), "未配置阿里云短信 accessKeyId");
    String accessKeySecret = requireText(aliyun.getAccessKeySecret(), "未配置阿里云短信 accessKeySecret");
    String regionId = requireText(aliyun.getRegionId(), "未配置阿里云短信 regionId");
    String endpoint = requireText(aliyun.getEndpoint(), "未配置阿里云短信 endpoint");

    String requestPayload = "{\"phone\":\"" + command.getPhone() + "\",\"scene\":\"" + command.getScene()
        + "\",\"signName\":\"" + signName + "\",\"templateCode\":\"" + templateCode + "\"}";
    try {
      DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
      DefaultProfile.addEndpoint(regionId, regionId, "Dysmsapi", endpoint);
      IAcsClient client = new DefaultAcsClient(profile);

      SendSmsRequest request = new SendSmsRequest();
      request.setSysMethod(MethodType.POST);
      request.setPhoneNumbers(command.getPhone());
      request.setSignName(signName);
      request.setTemplateCode(templateCode);
      request.setTemplateParam("{\"code\":\"" + command.getCode() + "\"}");
      request.setOutId("FAMILY-" + command.getScene() + "-" + System.currentTimeMillis());

      SendSmsResponse response = client.getAcsResponse(request);
      String responsePayload = "{\"code\":\"" + defaultText(response.getCode(), "") + "\",\"message\":\""
          + defaultText(response.getMessage(), "") + "\",\"bizId\":\"" + defaultText(response.getBizId(), "")
          + "\",\"requestId\":\"" + defaultText(response.getRequestId(), "") + "\"}";
      if (!"OK".equalsIgnoreCase(defaultText(response.getCode(), ""))) {
        return FamilySmsSendResult.failed(providerName(), requestPayload, responsePayload,
            "短信发送失败:" + defaultText(response.getMessage(), "未知错误"));
      }
      return FamilySmsSendResult.success(providerName(), response.getBizId(), requestPayload, responsePayload);
    } catch (Exception ex) {
      return FamilySmsSendResult.failed(providerName(), requestPayload, null,
          "短信发送异常:" + defaultText(ex.getMessage(), ex.getClass().getSimpleName()));
    }
  }

  private String resolveTemplateCode(FamilyPortalProperties.SmsCode smsCode, String scene) {
    String normalized = defaultText(scene, "LOGIN").toUpperCase(Locale.ROOT);
    if ("SECURITY".equals(normalized) && hasText(smsCode.getSecurityTemplateCode())) {
      return smsCode.getSecurityTemplateCode().trim();
    }
    if ("LOGIN".equals(normalized) && hasText(smsCode.getLoginTemplateCode())) {
      return smsCode.getLoginTemplateCode().trim();
    }
    if (hasText(smsCode.getDefaultTemplateCode())) {
      return smsCode.getDefaultTemplateCode().trim();
    }
    if (hasText(smsCode.getLoginTemplateCode())) {
      return smsCode.getLoginTemplateCode().trim();
    }
    throw new IllegalStateException("未配置短信模板编码");
  }

  private String requireText(String value, String message) {
    if (!hasText(value)) {
      throw new IllegalStateException(message);
    }
    return value.trim();
  }

  private String defaultText(String value, String fallback) {
    if (!hasText(value)) {
      return fallback;
    }
    return value.trim();
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
