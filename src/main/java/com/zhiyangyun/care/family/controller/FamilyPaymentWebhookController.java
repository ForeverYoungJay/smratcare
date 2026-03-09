package com.zhiyangyun.care.family.controller;

import com.zhiyangyun.care.family.service.FamilyPortalService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/family/payment/wechat")
public class FamilyPaymentWebhookController {
  private final FamilyPortalService familyPortalService;

  public FamilyPaymentWebhookController(FamilyPortalService familyPortalService) {
    this.familyPortalService = familyPortalService;
  }

  @PostMapping("/notify")
  public ResponseEntity<Map<String, String>> wechatNotify(@RequestBody String payload,
      @RequestHeader Map<String, String> headers) {
    boolean success = familyPortalService.handleWechatPayNotify(payload, headers);
    if (success) {
      return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "成功"));
    }
    return ResponseEntity.ok(Map.of("code", "FAIL", "message", "处理失败"));
  }
}
