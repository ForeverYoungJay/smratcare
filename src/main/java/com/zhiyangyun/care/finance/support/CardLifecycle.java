package com.zhiyangyun.care.finance.support;

import com.zhiyangyun.care.finance.entity.CardAccount;
import java.util.Map;
import java.util.Set;

/**
 * 一卡通卡务状态：申领 → 激活 → 挂失/冻结 → 补卡（旧卡作废）/ 注销。
 * 卡只是身份标识，余额在长者资金账户里，状态变化不动资金。
 */
public final class CardLifecycle {
  public static final Set<String> CLOSED = Set.of("REPLACED", "CANCELLED");

  private static final Map<String, Set<String>> FROM = Map.of(
      "ACTIVATE", Set.of("PENDING"),
      "REPORT_LOSS", Set.of("ACTIVE", "FROZEN"),
      "CANCEL_LOSS", Set.of("LOST"),
      "FREEZE", Set.of("ACTIVE"),
      "UNFREEZE", Set.of("FROZEN"),
      "REISSUE", Set.of("LOST"),
      "CANCEL", Set.of("PENDING", "ACTIVE", "FROZEN", "LOST"));

  private static final Map<String, String> TO = Map.of(
      "ACTIVATE", "ACTIVE",
      "REPORT_LOSS", "LOST",
      "CANCEL_LOSS", "ACTIVE",
      "FREEZE", "FROZEN",
      "UNFREEZE", "ACTIVE",
      "REISSUE", "REPLACED",
      "CANCEL", "CANCELLED");

  private CardLifecycle() {}

  /** 旧数据用 ACTIVE + lossFlag=1 表示挂失 */
  public static String current(CardAccount card) {
    String status = card.getStatus() == null ? "ACTIVE" : card.getStatus();
    return "ACTIVE".equals(status) && Integer.valueOf(1).equals(card.getLossFlag()) ? "LOST" : status;
  }

  /** 返回动作后的状态；不允许的动作抛异常 */
  public static String next(CardAccount card, String action) {
    Set<String> from = FROM.get(action);
    if (from == null) {
      throw new IllegalArgumentException("不支持的卡务操作：" + action);
    }
    String status = current(card);
    if (!from.contains(status)) {
      throw new IllegalArgumentException("当前卡状态不能执行该操作");
    }
    return TO.get(action);
  }
}
