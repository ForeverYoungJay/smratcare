package com.zhiyangyun.care.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.finance.model.FinanceReminderSummary;
import com.zhiyangyun.care.finance.model.FinanceReminderView;
import java.time.LocalDate;

/**
 * 财务提醒：生成侧由定时任务按机构调用（显式传 orgId，没有登录上下文），
 * 查询与处理侧走当前登录机构。
 */
public interface FinanceReminderService {

  /** 每月 18 号触发：下月代养费提醒。返回新建条数。 */
  int generateNextMonthCareFeeReminder(Long orgId, LocalDate today);

  /** 押金未缴清提醒（按月去重，每位长者一条）。 */
  int generateDepositShortfallReminders(Long orgId, LocalDate today);

  /** 电费未登记（机构级一条）与未缴（每个房间一条）提醒。 */
  int generateElectricityReminders(Long orgId, LocalDate today);

  IPage<FinanceReminderView> page(long pageNo, long pageSize, String status, String reminderType);

  FinanceReminderSummary summary();

  /** 标记单条已处理。 */
  FinanceReminderView handle(Long reminderId, String remark);

  /** 一键处理：传 reminderType 只处理该类型，否则处理全部未处理项。返回处理条数。 */
  int handleAll(String reminderType, String remark);
}
