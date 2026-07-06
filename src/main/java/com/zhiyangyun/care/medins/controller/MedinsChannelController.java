package com.zhiyangyun.care.medins.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.medins.entity.MedinsChannelConfig;
import com.zhiyangyun.care.medins.model.MedinsChannelSaveRequest;
import com.zhiyangyun.care.medins.service.MedinsChannelService;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 医保对接渠道配置接口。 */
@RestController
@RequestMapping("/api/medins")
public class MedinsChannelController {

  private static final String CHANNEL_ADMIN = "hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String VIEWER =
      "hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','MEDICAL_MINISTER','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";

  private final MedinsChannelService channelService;

  public MedinsChannelController(MedinsChannelService channelService) {
    this.channelService = channelService;
  }

  @GetMapping("/channels")
  @PreAuthorize(CHANNEL_ADMIN)
  public Result<List<MedinsChannelConfig>> channels() {
    return Result.ok(channelService.listChannels(AuthContext.getOrgId()));
  }

  /** 上传/校验操作页可选渠道（仅暴露编码与启用状态，不含 endpoint/密钥引用）。 */
  @GetMapping("/channels/options")
  @PreAuthorize(VIEWER)
  public Result<List<Map<String, Object>>> channelOptions() {
    List<Map<String, Object>> options = channelService.listChannels(AuthContext.getOrgId())
        .stream()
        .filter(c -> Integer.valueOf(1).equals(c.getEnabled()))
        .map(c -> {
          Map<String, Object> item = new LinkedHashMap<String, Object>();
          item.put("channel", c.getChannel());
          item.put("regionCode", c.getRegionCode());
          return item;
        })
        .toList();
    return Result.ok(options);
  }

  @PostMapping("/channels")
  @PreAuthorize(CHANNEL_ADMIN)
  public Result<Long> save(@Valid @RequestBody MedinsChannelSaveRequest request) {
    return Result.ok(channelService.saveChannel(request));
  }

  @PostMapping("/channels/{id}/toggle")
  @PreAuthorize(CHANNEL_ADMIN)
  public Result<Void> toggle(@PathVariable("id") Long id, @RequestParam boolean enabled) {
    channelService.toggleChannel(id, enabled);
    return Result.ok(null);
  }
}
