package com.zhiyangyun.care.smart.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.smart.entity.SmartAlertDispatch;
import com.zhiyangyun.care.smart.model.SmartDispatchActionRequest;

/** 告警派单闭环：触发→受理→响应→到场→处置→复盘，超时未响应自动升级。 */
public interface SmartDispatchService {

  /** 为开放的高危告警自动创建派单（供定时任务调用），返回新建条数。 */
  int autoDispatchOpenAlerts();

  /** 受理并指派处置人。 */
  SmartAlertDispatch assign(SmartDispatchActionRequest request);

  /** 已响应。 */
  SmartAlertDispatch respond(Long dispatchId);

  /** 已到场。 */
  SmartAlertDispatch onsite(Long dispatchId);

  /** 处置完成（记录处置说明，可关联不良事件）。 */
  SmartAlertDispatch handle(SmartDispatchActionRequest request);

  /** 复盘并关闭。 */
  SmartAlertDispatch review(SmartDispatchActionRequest request);

  /** 分页查询派单。 */
  IPage<SmartAlertDispatch> page(int pageNo, int pageSize, String status, String level, Long assigneeId);

  /** 升级超时未响应的派单（供定时任务调用），返回升级条数。 */
  int escalateOverdue();
}
