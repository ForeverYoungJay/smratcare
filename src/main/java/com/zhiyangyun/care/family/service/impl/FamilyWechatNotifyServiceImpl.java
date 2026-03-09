package com.zhiyangyun.care.family.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import com.zhiyangyun.care.family.config.FamilyPortalProperties;
import com.zhiyangyun.care.family.entity.FamilyNotifyLog;
import com.zhiyangyun.care.family.entity.FamilyPortalState;
import com.zhiyangyun.care.family.mapper.FamilyNotifyLogMapper;
import com.zhiyangyun.care.family.mapper.FamilyPortalStateMapper;
import com.zhiyangyun.care.family.model.FamilyNotifyCommand;
import com.zhiyangyun.care.family.service.FamilyWechatNotifyService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class FamilyWechatNotifyServiceImpl implements FamilyWechatNotifyService {
  private static final Logger log = LoggerFactory.getLogger(FamilyWechatNotifyServiceImpl.class);
  private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private static final String STATUS_PENDING = "PENDING";
  private static final String STATUS_SUCCESS = "SUCCESS";
  private static final String STATUS_FAILED = "FAILED";
  private static final String STATUS_SKIPPED = "SKIPPED";
  private static final String STATE_NOTIFICATION = "NOTIFICATION";

  private final FamilyNotifyLogMapper familyNotifyLogMapper;
  private final FamilyUserMapper familyUserMapper;
  private final FamilyPortalStateMapper familyPortalStateMapper;
  private final FamilyPortalProperties familyPortalProperties;
  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;

  private volatile String accessToken;
  private volatile LocalDateTime accessTokenExpireAt;

  public FamilyWechatNotifyServiceImpl(FamilyNotifyLogMapper familyNotifyLogMapper,
      FamilyUserMapper familyUserMapper,
      FamilyPortalStateMapper familyPortalStateMapper,
      FamilyPortalProperties familyPortalProperties,
      ObjectMapper objectMapper) {
    this.familyNotifyLogMapper = familyNotifyLogMapper;
    this.familyUserMapper = familyUserMapper;
    this.familyPortalStateMapper = familyPortalStateMapper;
    this.familyPortalProperties = familyPortalProperties;
    this.objectMapper = objectMapper;
    this.restTemplate = new RestTemplate();
  }

  @Override
  @Transactional
  public void notifyFamily(FamilyNotifyCommand command) {
    if (command == null || command.getOrgId() == null || command.getFamilyUserId() == null) {
      return;
    }
    FamilyPortalProperties.WechatNotify notify = familyPortalProperties.getWechatNotify();
    if (notify == null || !notify.isEnabled()) {
      return;
    }

    FamilyNotifyLog logRow = initLog(command, notify);
    try {
      if (!shouldNotifyBySettings(command)) {
        markSkipped(logRow, "家属通知设置已关闭该类型提醒");
        return;
      }

      FamilyUser familyUser = familyUserMapper.selectOne(
          Wrappers.lambdaQuery(FamilyUser.class)
              .eq(FamilyUser::getIsDeleted, 0)
              .eq(FamilyUser::getOrgId, command.getOrgId())
              .eq(FamilyUser::getId, command.getFamilyUserId())
              .last("LIMIT 1"));
      String openId = familyUser == null ? null : defaultText(familyUser.getOpenId(), null);
      if (openId == null) {
        markSkipped(logRow, "家属未绑定微信openId");
        return;
      }
      logRow.setTouserOpenId(openId);
      logRow.setTemplateId(resolveTemplateId(notify));
      logRow.setPagePath(defaultText(command.getPagePath(), defaultText(notify.getPage(), "pages/messages/index")));
      familyNotifyLogMapper.updateById(logRow);

      String payload = buildNotifyPayload(logRow, command, notify);
      logRow.setPayloadJson(payload);
      familyNotifyLogMapper.updateById(logRow);

      deliver(logRow, notify);
    } catch (Exception ex) {
      markFailed(logRow, defaultText(ex.getMessage(), "通知投递失败"), notify);
    }
  }

  @Override
  @Transactional
  public int retryFailedNotifications() {
    FamilyPortalProperties.WechatNotify notify = familyPortalProperties.getWechatNotify();
    if (notify == null || !notify.isEnabled()) {
      return 0;
    }
    int batchSize = Math.max(notify.getRetryBatchSize(), 1);
    LocalDateTime now = LocalDateTime.now();
    List<FamilyNotifyLog> list = familyNotifyLogMapper.selectList(
        Wrappers.lambdaQuery(FamilyNotifyLog.class)
            .eq(FamilyNotifyLog::getIsDeleted, 0)
            .eq(FamilyNotifyLog::getStatus, STATUS_FAILED)
            .le(FamilyNotifyLog::getNextRetryAt, now)
            .orderByAsc(FamilyNotifyLog::getNextRetryAt)
            .last("LIMIT " + batchSize));
    if (list == null || list.isEmpty()) {
      return 0;
    }
    int retried = 0;
    for (FamilyNotifyLog row : list) {
      try {
        deliver(row, notify);
        retried++;
      } catch (Exception ex) {
        markFailed(row, defaultText(ex.getMessage(), "通知重试失败"), notify);
      }
    }
    return retried;
  }

  private FamilyNotifyLog initLog(FamilyNotifyCommand command, FamilyPortalProperties.WechatNotify notify) {
    FamilyNotifyLog row = new FamilyNotifyLog();
    row.setTenantId(command.getOrgId());
    row.setOrgId(command.getOrgId());
    row.setFamilyUserId(command.getFamilyUserId());
    row.setElderId(command.getElderId());
    row.setEventType(defaultText(command.getEventType(), "GENERAL"));
    row.setLevel(defaultText(command.getLevel(), "normal"));
    row.setTitle(defaultText(command.getTitle(), "家属通知"));
    row.setContent(defaultText(command.getContent(), ""));
    row.setStatus(STATUS_PENDING);
    row.setRetryCount(0);
    row.setMaxRetry(Math.max(notify.getMaxRetry(), 0));
    row.setCreatedBy(command.getCreatedBy());
    familyNotifyLogMapper.insert(row);
    return row;
  }

  private boolean shouldNotifyBySettings(FamilyNotifyCommand command) {
    FamilyPortalState setting = familyPortalStateMapper.selectOne(
        Wrappers.lambdaQuery(FamilyPortalState.class)
            .eq(FamilyPortalState::getIsDeleted, 0)
            .eq(FamilyPortalState::getOrgId, command.getOrgId())
            .eq(FamilyPortalState::getFamilyUserId, command.getFamilyUserId())
            .eq(FamilyPortalState::getCategory, STATE_NOTIFICATION)
            .eq(FamilyPortalState::getBizKey, "default")
            .last("LIMIT 1"));
    if (setting == null || setting.getValueJson() == null || setting.getValueJson().isBlank()) {
      return true;
    }
    Map<String, Object> map;
    try {
      map = objectMapper.readValue(setting.getValueJson(), new TypeReference<Map<String, Object>>() {});
    } catch (Exception ex) {
      return true;
    }
    String eventType = defaultText(command.getEventType(), "").toUpperCase(Locale.ROOT);
    String level = defaultText(command.getLevel(), "").toLowerCase(Locale.ROOT);
    if (eventType.startsWith("PAYMENT")) {
      return readBoolean(map, "paymentAlert", true)
          && (!"urgent".equals(level) || readBoolean(map, "urgentAlert", true));
    }
    if (eventType.startsWith("HEALTH")) {
      return readBoolean(map, "healthAlert", true)
          && (!"urgent".equals(level) || readBoolean(map, "urgentAlert", true));
    }
    if (eventType.startsWith("ACTIVITY")) {
      return readBoolean(map, "activityAlert", false);
    }
    if ("urgent".equals(level)) {
      return readBoolean(map, "urgentAlert", true);
    }
    return true;
  }

  private void deliver(FamilyNotifyLog row, FamilyPortalProperties.WechatNotify notify) {
    if (row == null) {
      return;
    }
    String payload = defaultText(row.getPayloadJson(), null);
    if (payload == null) {
      throw new IllegalStateException("通知投递报文为空");
    }
    JsonNode response = sendSubscribeMessage(payload, notify, false);
    int errCode = response.path("errcode").asInt(-1);
    if (errCode == 40001 || errCode == 42001) {
      response = sendSubscribeMessage(payload, notify, true);
      errCode = response.path("errcode").asInt(-1);
    }

    if (errCode == 0) {
      row.setStatus(STATUS_SUCCESS);
      row.setResponseJson(writeJson(response));
      row.setLastError(null);
      row.setNextRetryAt(null);
      familyNotifyLogMapper.updateById(row);
      return;
    }

    String error = "微信通知失败 errcode=" + errCode + ", errmsg=" + defaultText(response.path("errmsg").asText(""), "unknown");
    row.setResponseJson(writeJson(response));
    markFailed(row, error, notify);
  }

  private JsonNode sendSubscribeMessage(String payload, FamilyPortalProperties.WechatNotify notify, boolean forceRefresh) {
    String token = getAccessToken(notify, forceRefresh);
    String sendUrl = UriComponentsBuilder.fromUriString(requireText(notify.getSubscribeSendUrl(), "未配置微信订阅消息接口"))
        .queryParam("access_token", token)
        .toUriString();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    ResponseEntity<String> response = restTemplate.postForEntity(sendUrl, new HttpEntity<>(payload, headers), String.class);
    return readJsonNode(response.getBody());
  }

  private String getAccessToken(FamilyPortalProperties.WechatNotify notify, boolean forceRefresh) {
    LocalDateTime now = LocalDateTime.now();
    if (!forceRefresh && accessToken != null && accessTokenExpireAt != null && accessTokenExpireAt.isAfter(now)) {
      return accessToken;
    }
    String appId = resolveNotifyAppId(notify);
    String appSecret = resolveNotifyAppSecret(notify);
    String url = UriComponentsBuilder
        .fromUriString(requireText(notify.getAccessTokenUrl(), "未配置微信AccessToken接口"))
        .queryParam("grant_type", "client_credential")
        .queryParam("appid", appId)
        .queryParam("secret", appSecret)
        .toUriString();
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    JsonNode node = readJsonNode(response.getBody());
    String token = defaultText(node.path("access_token").asText(""), null);
    if (token == null) {
      throw new IllegalStateException("获取微信access_token失败：" + defaultText(node.path("errmsg").asText(""), "未知错误"));
    }
    int expiresIn = node.path("expires_in").asInt(7200);
    int bufferSeconds = Math.max(notify.getAccessTokenExpireBufferSeconds(), 30);
    accessToken = token;
    accessTokenExpireAt = LocalDateTime.now().plusSeconds(Math.max(expiresIn - bufferSeconds, 60));
    return token;
  }

  private String buildNotifyPayload(FamilyNotifyLog row, FamilyNotifyCommand command, FamilyPortalProperties.WechatNotify notify) {
    Map<String, String> placeholders = new LinkedHashMap<>();
    placeholders.put("title", defaultText(row.getTitle(), ""));
    placeholders.put("content", defaultText(row.getContent(), ""));
    placeholders.put("eventType", defaultText(row.getEventType(), ""));
    placeholders.put("level", defaultText(row.getLevel(), ""));
    placeholders.put("time", LocalDateTime.now().format(DATETIME_FMT));
    placeholders.put("elderId", row.getElderId() == null ? "" : String.valueOf(row.getElderId()));
    if (command.getPlaceholders() != null) {
      command.getPlaceholders().forEach((k, v) -> placeholders.put(k, defaultText(v, "")));
    }

    ObjectNode root = objectMapper.createObjectNode();
    root.put("touser", defaultText(row.getTouserOpenId(), ""));
    root.put("template_id", defaultText(row.getTemplateId(), ""));
    root.put("page", defaultText(row.getPagePath(), defaultText(notify.getPage(), "pages/messages/index")));
    root.put("miniprogram_state", defaultText(notify.getMiniProgramState(), "formal"));
    root.put("lang", defaultText(notify.getLanguage(), "zh_CN"));
    root.set("data", buildTemplateData(notify, placeholders));
    return writeJson(root);
  }

  private JsonNode buildTemplateData(FamilyPortalProperties.WechatNotify notify, Map<String, String> placeholders) {
    String templateJson = defaultText(notify.getDataTemplateJson(), null);
    JsonNode template = readJsonNode(templateJson);
    if (!template.isObject() || template.size() == 0) {
      ObjectNode fallback = objectMapper.createObjectNode();
      fallback.putObject("thing1").put("value", shorten(defaultText(placeholders.get("title"), ""), 20));
      fallback.putObject("thing2").put("value", shorten(defaultText(placeholders.get("content"), ""), 20));
      fallback.putObject("time3").put("value", defaultText(placeholders.get("time"), LocalDateTime.now().format(DATETIME_FMT)));
      return fallback;
    }
    return replacePlaceholderNode(template, placeholders);
  }

  private JsonNode replacePlaceholderNode(JsonNode node, Map<String, String> placeholders) {
    if (node == null || node.isNull()) {
      return node;
    }
    if (node.isTextual()) {
      return TextNode.valueOf(resolvePlaceholders(node.asText(""), placeholders));
    }
    if (node.isObject()) {
      ObjectNode copied = objectMapper.createObjectNode();
      node.fields().forEachRemaining(entry -> copied.set(entry.getKey(), replacePlaceholderNode(entry.getValue(), placeholders)));
      return copied;
    }
    if (node.isArray()) {
      ArrayNode array = objectMapper.createArrayNode();
      node.forEach(item -> array.add(replacePlaceholderNode(item, placeholders)));
      return array;
    }
    return node.deepCopy();
  }

  private String resolvePlaceholders(String text, Map<String, String> placeholders) {
    String result = defaultText(text, "");
    if (placeholders == null || placeholders.isEmpty()) {
      return result;
    }
    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      String key = entry.getKey();
      if (key == null || key.isBlank()) {
        continue;
      }
      result = result.replace("{" + key + "}", defaultText(entry.getValue(), ""));
    }
    return result;
  }

  private String resolveNotifyAppId(FamilyPortalProperties.WechatNotify notify) {
    String appId = defaultText(notify.getAppId(), null);
    if (appId != null) {
      return appId;
    }
    FamilyPortalProperties.WechatPay pay = familyPortalProperties.getWechatPay();
    return requireText(pay == null ? null : pay.getAppId(), "未配置微信通知 appId");
  }

  private String resolveNotifyAppSecret(FamilyPortalProperties.WechatNotify notify) {
    String appSecret = defaultText(notify.getAppSecret(), null);
    if (appSecret != null) {
      return appSecret;
    }
    FamilyPortalProperties.WechatPay pay = familyPortalProperties.getWechatPay();
    return requireText(pay == null ? null : pay.getAppSecret(), "未配置微信通知 appSecret");
  }

  private String resolveTemplateId(FamilyPortalProperties.WechatNotify notify) {
    return requireText(notify.getTemplateId(), "未配置微信通知 templateId");
  }

  private void markSkipped(FamilyNotifyLog row, String reason) {
    if (row == null) {
      return;
    }
    row.setStatus(STATUS_SKIPPED);
    row.setLastError(shorten(reason, 250));
    row.setNextRetryAt(null);
    familyNotifyLogMapper.updateById(row);
  }

  private void markFailed(FamilyNotifyLog row, String reason, FamilyPortalProperties.WechatNotify notify) {
    if (row == null) {
      return;
    }
    int retryCount = row.getRetryCount() == null ? 0 : row.getRetryCount();
    retryCount += 1;
    int maxRetry = row.getMaxRetry() == null ? Math.max(notify.getMaxRetry(), 0) : row.getMaxRetry();
    row.setRetryCount(retryCount);
    row.setStatus(STATUS_FAILED);
    row.setLastError(shorten(reason, 250));
    if (retryCount >= maxRetry) {
      row.setNextRetryAt(null);
    } else {
      int retryMinutes = Math.max(notify.getRetryIntervalMinutes(), 1);
      row.setNextRetryAt(LocalDateTime.now().plusMinutes(retryMinutes));
    }
    familyNotifyLogMapper.updateById(row);
    log.warn("family wechat notify failed, logId={}, retry={}/{}, reason={}",
        row.getId(), retryCount, maxRetry, row.getLastError());
  }

  private JsonNode readJsonNode(String json) {
    if (json == null || json.isBlank()) {
      return objectMapper.createObjectNode();
    }
    try {
      return objectMapper.readTree(json);
    } catch (Exception ex) {
      return objectMapper.createObjectNode();
    }
  }

  private String writeJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception ex) {
      throw new IllegalStateException("序列化通知JSON失败", ex);
    }
  }

  private boolean readBoolean(Map<String, Object> map, String key, boolean fallback) {
    if (map == null || map.isEmpty() || key == null) {
      return fallback;
    }
    Object value = map.get(key);
    if (value instanceof Boolean bool) {
      return bool;
    }
    if (value instanceof Number num) {
      return num.intValue() != 0;
    }
    if (value instanceof String text) {
      if (text.isBlank()) {
        return fallback;
      }
      return "1".equals(text.trim()) || "true".equalsIgnoreCase(text.trim());
    }
    return fallback;
  }

  private String requireText(String text, String error) {
    String value = defaultText(text, null);
    if (value == null) {
      throw new IllegalStateException(error);
    }
    return value;
  }

  private String defaultText(String text, String fallback) {
    if (text == null || text.isBlank()) {
      return fallback;
    }
    return text.trim();
  }

  private String shorten(String text, int maxLength) {
    String value = defaultText(text, "");
    if (maxLength <= 0 || value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, maxLength);
  }
}
