package com.zhiyangyun.care.smart.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.smart.model.SmartEventIngestRequest;
import com.zhiyangyun.care.smart.model.SmartEventIngestResponse;
import com.zhiyangyun.care.smart.service.SmartEventIngestService;
import jakarta.validation.Valid;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 厂商网关标准设备事件接入（匿名 + API-Key 校验，安全做法同 FamilyPaymentWebhookController：
 * 在 SecurityConfig 放行路径，由控制器自行校验凭证）。
 *
 * <p>配置项 {@code zhiyangyun.smart.ingest.api-key} 未配置时接口整体关闭。</p>
 */
@RestController
@RequestMapping("/api/smart/events")
public class SmartEventIngestController {

  public static final String API_KEY_HEADER = "X-Api-Key";

  private final SmartEventIngestService ingestService;
  private final String apiKey;

  public SmartEventIngestController(SmartEventIngestService ingestService,
      @Value("${zhiyangyun.smart.ingest.api-key:}") String apiKey) {
    this.ingestService = ingestService;
    this.apiKey = apiKey == null ? "" : apiKey.trim();
  }

  @PostMapping("/ingest")
  public Result<SmartEventIngestResponse> ingest(
      @RequestHeader(value = API_KEY_HEADER, required = false) String requestKey,
      @Valid @RequestBody SmartEventIngestRequest request) {
    if (!StringUtils.hasText(apiKey)) {
      return Result.error(403, "设备事件接入未启用（未配置 zhiyangyun.smart.ingest.api-key）");
    }
    if (!StringUtils.hasText(requestKey) || !constantTimeEquals(apiKey, requestKey.trim())) {
      return Result.error(401, "API Key 无效");
    }
    return Result.ok(ingestService.ingest(request));
  }

  private boolean constantTimeEquals(String expected, String actual) {
    return MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
  }
}
