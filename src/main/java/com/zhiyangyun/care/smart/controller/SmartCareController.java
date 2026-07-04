package com.zhiyangyun.care.smart.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.entity.SmartDevice;
import com.zhiyangyun.care.smart.entity.SmartDeviceEvent;
import com.zhiyangyun.care.smart.model.SmartAlertResolveRequest;
import com.zhiyangyun.care.smart.model.SmartAlertSummaryResponse;
import com.zhiyangyun.care.smart.model.SmartDeviceEventRequest;
import com.zhiyangyun.care.smart.model.SmartDeviceRequest;
import com.zhiyangyun.care.smart.service.SmartCareService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/smart-care")
public class SmartCareController {
  private final SmartCareService smartCareService;

  public SmartCareController(SmartCareService smartCareService) {
    this.smartCareService = smartCareService;
  }

  @GetMapping("/devices/page")
  public Result<IPage<SmartDevice>> pageDevices(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String deviceType,
      @RequestParam(required = false) String onlineStatus) {
    return Result.ok(smartCareService.pageDevices(pageNo, pageSize, keyword, deviceType, onlineStatus));
  }

  @PostMapping("/devices")
  public Result<SmartDevice> createDevice(@Valid @RequestBody SmartDeviceRequest request) {
    return Result.ok(smartCareService.saveDevice(null, request));
  }

  @PutMapping("/devices/{id}")
  public Result<SmartDevice> updateDevice(@PathVariable Long id, @Valid @RequestBody SmartDeviceRequest request) {
    return Result.ok(smartCareService.saveDevice(id, request));
  }

  @PutMapping("/devices/{id}/enabled")
  public Result<SmartDevice> setDeviceEnabled(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean enabled) {
    return Result.ok(smartCareService.setDeviceEnabled(id, enabled));
  }

  @PostMapping("/events")
  public Result<SmartDeviceEvent> reportEvent(@Valid @RequestBody SmartDeviceEventRequest request) {
    return Result.ok(smartCareService.reportEvent(request));
  }

  @GetMapping("/alerts/summary")
  public Result<SmartAlertSummaryResponse> summary() {
    return Result.ok(smartCareService.summary());
  }

  @PostMapping("/alerts/derived-health/refresh")
  public Result<SmartAlertSummaryResponse> refreshDerivedHealthAlerts() {
    return Result.ok(smartCareService.refreshDerivedHealthAlerts());
  }

  @GetMapping("/alerts/page")
  public Result<IPage<SmartAlert>> pageAlerts(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String level,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String keyword) {
    return Result.ok(smartCareService.pageAlerts(pageNo, pageSize, status, level, elderId, keyword));
  }

  @PutMapping("/alerts/{id}/ack")
  public Result<SmartAlert> acknowledgeAlert(@PathVariable Long id) {
    return Result.ok(smartCareService.acknowledgeAlert(id));
  }

  @PutMapping("/alerts/{id}/resolve")
  public Result<SmartAlert> resolveAlert(@PathVariable Long id, @RequestBody(required = false) SmartAlertResolveRequest request) {
    return Result.ok(smartCareService.resolveAlert(id, request));
  }
}
