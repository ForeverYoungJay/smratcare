package com.zhiyangyun.care.operations.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * {@link StaffSubscribeMessageSender} 的默认实现：仅记录日志。
 * 接入微信订阅消息时，用真实实现（调用 subscribeMessage.send）替换本 Bean，
 * 可参考 family 模块的 wechat-notify 配置与发送流程。
 */
@Component
public class LoggingStaffSubscribeMessageSender implements StaffSubscribeMessageSender {
  private static final Logger log = LoggerFactory.getLogger(LoggingStaffSubscribeMessageSender.class);

  @Override
  public void sendOverdueReminder(Long staffId, String taskTitle, String planTime) {
    log.info("[员工订阅消息-模拟发送] 任务超时提醒 staffId={}, taskTitle={}, planTime={}", staffId, taskTitle, planTime);
  }
}
