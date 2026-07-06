package com.zhiyangyun.care.family.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.family.config.FamilyPortalProperties;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 微信小程序登录辅助：用 wx.login 的 code 换 openId，用 getPhoneNumber 的 code 换手机号。
 *
 * <p>访问令牌自带缓存；未配置或 mock 模式下走离线解析，便于联调 / E2E。
 */
@Service
public class WechatMiniAppAuthService {
  private static final Logger log = LoggerFactory.getLogger(WechatMiniAppAuthService.class);
  private static final String MOCK_OPENID_PREFIX = "MOCK-OPENID:";
  private static final String MOCK_PHONE_PREFIX = "MOCK-PHONE:";

  private final FamilyPortalProperties familyPortalProperties;
  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;

  private volatile String accessToken;
  private volatile LocalDateTime accessTokenExpireAt;

  public WechatMiniAppAuthService(FamilyPortalProperties familyPortalProperties, ObjectMapper objectMapper) {
    this.familyPortalProperties = familyPortalProperties;
    this.objectMapper = objectMapper;
    this.restTemplate = new RestTemplate();
  }

  public boolean isLoginEnabled() {
    FamilyPortalProperties.MiniApp miniApp = familyPortalProperties.getMiniApp();
    return miniApp != null && miniApp.isLoginEnabled();
  }

  /** 用 wx.login 返回的 code 换取 openId。 */
  public String resolveOpenId(String loginCode) {
    String code = trimToNull(loginCode);
    if (code == null) {
      return null;
    }
    FamilyPortalProperties.MiniApp miniApp = familyPortalProperties.getMiniApp();
    if (miniApp != null && miniApp.isMockEnabled() && code.startsWith(MOCK_OPENID_PREFIX)) {
      return code.substring(MOCK_OPENID_PREFIX.length()).trim();
    }
    String url = UriComponentsBuilder
        .fromUriString(requireText(miniApp == null ? null : miniApp.getJscode2sessionUrl(),
            "https://api.weixin.qq.com/sns/jscode2session"))
        .queryParam("appid", resolveAppId())
        .queryParam("secret", resolveAppSecret())
        .queryParam("js_code", code)
        .queryParam("grant_type", "authorization_code")
        .toUriString();
    JsonNode node = readJsonNode(restTemplate.getForEntity(url, String.class).getBody());
    String openId = trimToNull(node.path("openid").asText(""));
    if (openId == null) {
      throw new IllegalStateException("获取微信 openId 失败：" + defaultText(node.path("errmsg").asText(""), "未知错误"));
    }
    return openId;
  }

  /** 用 getPhoneNumber 返回的 code 换取手机号（新版免 session_key 方案）。 */
  public String resolvePhoneNumber(String phoneCode) {
    String code = trimToNull(phoneCode);
    if (code == null) {
      throw new IllegalArgumentException("缺少微信手机号授权 code");
    }
    FamilyPortalProperties.MiniApp miniApp = familyPortalProperties.getMiniApp();
    if (miniApp != null && miniApp.isMockEnabled() && code.startsWith(MOCK_PHONE_PREFIX)) {
      return normalizePhone(code.substring(MOCK_PHONE_PREFIX.length()));
    }
    String url = UriComponentsBuilder
        .fromUriString(requireText(miniApp == null ? null : miniApp.getGetUserPhoneNumberUrl(),
            "https://api.weixin.qq.com/wxa/business/getuserphonenumber"))
        .queryParam("access_token", getAccessToken(false))
        .toUriString();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    String body = writeJson(Map.of("code", code));
    JsonNode node = postForJson(url, new HttpEntity<>(body, headers));
    int errCode = node.path("errcode").asInt(-1);
    if (errCode == 40001 || errCode == 42001) {
      // access_token 过期，强制刷新后重试一次
      url = UriComponentsBuilder
          .fromUriString(requireText(miniApp == null ? null : miniApp.getGetUserPhoneNumberUrl(),
              "https://api.weixin.qq.com/wxa/business/getuserphonenumber"))
          .queryParam("access_token", getAccessToken(true))
          .toUriString();
      node = postForJson(url, new HttpEntity<>(body, headers));
      errCode = node.path("errcode").asInt(-1);
    }
    if (errCode != 0) {
      throw new IllegalStateException("获取微信手机号失败：errcode=" + errCode
          + ", errmsg=" + defaultText(node.path("errmsg").asText(""), "未知错误"));
    }
    String phone = trimToNull(node.path("phone_info").path("purePhoneNumber").asText(""));
    if (phone == null) {
      phone = trimToNull(node.path("phone_info").path("phoneNumber").asText(""));
    }
    if (phone == null) {
      throw new IllegalStateException("微信手机号响应缺少号码字段");
    }
    return normalizePhone(phone);
  }

  private JsonNode postForJson(String url, HttpEntity<String> entity) {
    ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
    return readJsonNode(response.getBody());
  }

  private String getAccessToken(boolean forceRefresh) {
    LocalDateTime now = LocalDateTime.now();
    if (!forceRefresh && accessToken != null && accessTokenExpireAt != null && accessTokenExpireAt.isAfter(now)) {
      return accessToken;
    }
    FamilyPortalProperties.MiniApp miniApp = familyPortalProperties.getMiniApp();
    String url = UriComponentsBuilder
        .fromUriString(requireText(miniApp == null ? null : miniApp.getAccessTokenUrl(),
            "https://api.weixin.qq.com/cgi-bin/token"))
        .queryParam("grant_type", "client_credential")
        .queryParam("appid", resolveAppId())
        .queryParam("secret", resolveAppSecret())
        .toUriString();
    JsonNode node = readJsonNode(restTemplate.getForEntity(url, String.class).getBody());
    String token = trimToNull(node.path("access_token").asText(""));
    if (token == null) {
      throw new IllegalStateException("获取微信 access_token 失败：" + defaultText(node.path("errmsg").asText(""), "未知错误"));
    }
    int expiresIn = node.path("expires_in").asInt(7200);
    int buffer = Math.max(miniApp == null ? 120 : miniApp.getAccessTokenExpireBufferSeconds(), 30);
    accessToken = token;
    accessTokenExpireAt = LocalDateTime.now().plusSeconds(Math.max(expiresIn - buffer, 60));
    return token;
  }

  private String resolveAppId() {
    FamilyPortalProperties.MiniApp miniApp = familyPortalProperties.getMiniApp();
    String appId = miniApp == null ? null : trimToNull(miniApp.getAppId());
    if (appId != null) {
      return appId;
    }
    FamilyPortalProperties.WechatNotify notify = familyPortalProperties.getWechatNotify();
    if (notify != null && trimToNull(notify.getAppId()) != null) {
      return notify.getAppId().trim();
    }
    FamilyPortalProperties.WechatPay pay = familyPortalProperties.getWechatPay();
    String payAppId = pay == null ? null : trimToNull(pay.getAppId());
    if (payAppId == null) {
      throw new IllegalStateException("未配置微信小程序 appId");
    }
    return payAppId;
  }

  private String resolveAppSecret() {
    FamilyPortalProperties.MiniApp miniApp = familyPortalProperties.getMiniApp();
    String appSecret = miniApp == null ? null : trimToNull(miniApp.getAppSecret());
    if (appSecret != null) {
      return appSecret;
    }
    FamilyPortalProperties.WechatNotify notify = familyPortalProperties.getWechatNotify();
    if (notify != null && trimToNull(notify.getAppSecret()) != null) {
      return notify.getAppSecret().trim();
    }
    FamilyPortalProperties.WechatPay pay = familyPortalProperties.getWechatPay();
    String payAppSecret = pay == null ? null : trimToNull(pay.getAppSecret());
    if (payAppSecret == null) {
      throw new IllegalStateException("未配置微信小程序 appSecret");
    }
    return payAppSecret;
  }

  private JsonNode readJsonNode(String json) {
    if (json == null || json.isBlank()) {
      return objectMapper.createObjectNode();
    }
    try {
      return objectMapper.readTree(json);
    } catch (Exception ex) {
      log.warn("解析微信响应失败: {}", ex.getMessage());
      return objectMapper.createObjectNode();
    }
  }

  private String writeJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception ex) {
      throw new IllegalStateException("序列化微信请求体失败", ex);
    }
  }

  private String normalizePhone(String phone) {
    String value = trimToNull(phone);
    if (value == null) {
      throw new IllegalStateException("微信手机号为空");
    }
    // 去掉可能带的区号前缀（如 +86 / 0086）
    value = value.replaceAll("\\s+", "");
    if (value.startsWith("+86")) {
      value = value.substring(3);
    } else if (value.startsWith("0086")) {
      value = value.substring(4);
    }
    return value;
  }

  private String requireText(String text, String fallback) {
    String value = trimToNull(text);
    return value == null ? fallback : value;
  }

  private String defaultText(String text, String fallback) {
    String value = trimToNull(text);
    return value == null ? fallback : value;
  }

  private String trimToNull(String text) {
    if (text == null) {
      return null;
    }
    String trimmed = text.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
