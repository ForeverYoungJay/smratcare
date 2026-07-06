package com.zhiyangyun.care.ai.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/** 求解器输出：分配集合 + 缺口 + 软约束打分。 */
@Data
public class SolverResult {
  private List<SolverAssignment> assignments = new ArrayList<>();
  /** 无法满足硬约束的缺口："2026-07-06 NIGHT 缺1人" */
  private List<String> unfilledSlots = new ArrayList<>();
  private Map<Long, Double> hoursByStaff = new HashMap<>();
  private Map<Long, Integer> nightsByStaff = new HashMap<>();
  /** 软约束目标值（越小越好）：工时方差*权重 + 夜班方差*权重 */
  private double softScore;

  public boolean hasSlot(Long staffId, LocalDate date) {
    return assignments.stream().anyMatch(a -> a.getStaffId().equals(staffId) && a.getDutyDate().equals(date));
  }
}
