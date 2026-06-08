package com.zhiyangyun.care.smart.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.entity.SmartDevice;
import com.zhiyangyun.care.smart.entity.SmartDeviceEvent;
import com.zhiyangyun.care.smart.model.SmartAlertResolveRequest;
import com.zhiyangyun.care.smart.model.SmartAlertSummaryResponse;
import com.zhiyangyun.care.smart.model.SmartDeviceEventRequest;
import com.zhiyangyun.care.smart.model.SmartDeviceRequest;

public interface SmartCareService {
  IPage<SmartDevice> pageDevices(int pageNo, int pageSize, String keyword, String deviceType, String onlineStatus);

  SmartDevice saveDevice(Long id, SmartDeviceRequest request);

  SmartDevice setDeviceEnabled(Long id, boolean enabled);

  SmartDeviceEvent reportEvent(SmartDeviceEventRequest request);

  SmartAlertSummaryResponse summary();

  IPage<SmartAlert> pageAlerts(int pageNo, int pageSize, String status, String level, Long elderId, String keyword);

  SmartAlert acknowledgeAlert(Long id);

  SmartAlert resolveAlert(Long id, SmartAlertResolveRequest request);
}
