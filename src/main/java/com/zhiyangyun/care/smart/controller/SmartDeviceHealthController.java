package com.zhiyangyun.care.smart.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.smart.entity.SmartDevice;
import com.zhiyangyun.care.smart.mapper.SmartDeviceMapper;
import com.zhiyangyun.care.smart.model.SmartDeviceHealthSummaryResponse;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 设备健康监控：在线率/心跳/电量/信号巡检视图。 */
@RestController
@RequestMapping("/api/smart/devices/health")
public class SmartDeviceHealthController {

  private static final String VIEWER =
      "hasAnyRole('NURSING_EMPLOYEE','NURSING_MINISTER','MEDICAL_MINISTER','LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN','STAFF')";
  private static final int WEAK_SIGNAL_THRESHOLD = 30;

  private final SmartDeviceMapper deviceMapper;
  private final int offlineMinutes;
  private final int lowBatteryPercent;

  public SmartDeviceHealthController(
      SmartDeviceMapper deviceMapper,
      @Value("${zhiyangyun.smart.monitor.offline-minutes:10}") int offlineMinutes,
      @Value("${zhiyangyun.smart.monitor.low-battery-percent:20}") int lowBatteryPercent) {
    this.deviceMapper = deviceMapper;
    this.offlineMinutes = offlineMinutes;
    this.lowBatteryPercent = lowBatteryPercent;
  }

  @GetMapping("/summary")
  @PreAuthorize(VIEWER)
  public Result<SmartDeviceHealthSummaryResponse> summary() {
    Long orgId = AuthContext.getOrgId();
    SmartDeviceHealthSummaryResponse response = new SmartDeviceHealthSummaryResponse();
    response.setTotalCount(count(orgId, w -> { }));
    response.setOnlineCount(count(orgId, w -> w.eq(SmartDevice::getOnlineStatus, "ONLINE")));
    response.setOfflineCount(count(orgId, w -> w.eq(SmartDevice::getOnlineStatus, "OFFLINE")));
    response.setLowBatteryCount(count(orgId, w -> w
        .isNotNull(SmartDevice::getBatteryLevel)
        .le(SmartDevice::getBatteryLevel, lowBatteryPercent)));
    response.setWeakSignalCount(count(orgId, w -> w
        .isNotNull(SmartDevice::getSignalStrength)
        .le(SmartDevice::getSignalStrength, WEAK_SIGNAL_THRESHOLD)));
    LocalDateTime cutoff = LocalDateTime.now().minusMinutes(offlineMinutes);
    response.setStaleHeartbeatCount(count(orgId, w -> w
        .eq(SmartDevice::getOnlineStatus, "ONLINE")
        .and(q -> q.isNull(SmartDevice::getLastHeartbeatAt)
            .or().lt(SmartDevice::getLastHeartbeatAt, cutoff))));
    response.setLowBatteryThreshold(lowBatteryPercent);
    response.setWeakSignalThreshold(WEAK_SIGNAL_THRESHOLD);
    response.setOfflineMinutes(offlineMinutes);
    return Result.ok(response);
  }

  @GetMapping("/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<SmartDevice>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String deviceType,
      @RequestParam(required = false) String onlineStatus,
      @RequestParam(required = false, defaultValue = "false") boolean lowBatteryOnly,
      @RequestParam(required = false, defaultValue = "false") boolean weakSignalOnly) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<SmartDevice> wrapper = Wrappers.lambdaQuery(SmartDevice.class)
        .eq(SmartDevice::getIsDeleted, 0)
        .eq(orgId != null, SmartDevice::getOrgId, orgId)
        .eq(StringUtils.hasText(deviceType), SmartDevice::getDeviceType, deviceType)
        .eq(StringUtils.hasText(onlineStatus), SmartDevice::getOnlineStatus, onlineStatus)
        .and(StringUtils.hasText(keyword), w -> w
            .like(SmartDevice::getDeviceName, keyword)
            .or().like(SmartDevice::getDeviceCode, keyword)
            .or().like(SmartDevice::getLocation, keyword));
    if (lowBatteryOnly) {
      wrapper.isNotNull(SmartDevice::getBatteryLevel).le(SmartDevice::getBatteryLevel, lowBatteryPercent);
    }
    if (weakSignalOnly) {
      wrapper.isNotNull(SmartDevice::getSignalStrength).le(SmartDevice::getSignalStrength, WEAK_SIGNAL_THRESHOLD);
    }
    wrapper.orderByAsc(SmartDevice::getOnlineStatus)
        .orderByAsc(SmartDevice::getBatteryLevel)
        .orderByDesc(SmartDevice::getUpdateTime);
    return Result.ok(deviceMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  private Long count(Long orgId, java.util.function.Consumer<LambdaQueryWrapper<SmartDevice>> extra) {
    LambdaQueryWrapper<SmartDevice> wrapper = Wrappers.lambdaQuery(SmartDevice.class)
        .eq(SmartDevice::getIsDeleted, 0)
        .eq(SmartDevice::getEnabled, 1)
        .eq(orgId != null, SmartDevice::getOrgId, orgId);
    extra.accept(wrapper);
    return deviceMapper.selectCount(wrapper);
  }
}
