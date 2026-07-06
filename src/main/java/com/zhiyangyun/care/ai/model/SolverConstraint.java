package com.zhiyangyun.care.ai.model;

import lombok.Data;

/** 求解器约束参数（硬约束 + 软约束权重）。 */
@Data
public class SolverConstraint {
  /** 每人每周最大工时（硬约束） */
  private double maxWeeklyHours = 40;
  /** 连续上班天数上限（硬约束） */
  private int maxConsecutiveDays = 5;
  /** 夜班后强制休一天（硬约束） */
  private boolean nightRestEnabled = true;
  /** 软约束权重：工时均衡 */
  private double workloadBalanceWeight = 1;
  /** 软约束权重：夜班公平 */
  private double nightFairnessWeight = 1;
}
