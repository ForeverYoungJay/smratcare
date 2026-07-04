package com.zhiyangyun.care.operations.notify;

/**
 * 员工端订阅消息发送接口：用于向一线员工推送任务超时等提醒。
 * 默认实现 {@link LoggingStaffSubscribeMessageSender} 仅记录日志；
 * 接入微信小程序订阅消息（或短信等渠道）时提供新的实现替换即可。
 */
public interface StaffSubscribeMessageSender {

  /**
   * 发送任务超时提醒。
   *
   * @param staffId   负责员工 id
   * @param taskTitle 任务标题
   * @param planTime  计划执行时间（已格式化的展示文本）
   */
  void sendOverdueReminder(Long staffId, String taskTitle, String planTime);
}
