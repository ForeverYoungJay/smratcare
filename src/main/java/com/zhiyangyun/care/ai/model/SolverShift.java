package com.zhiyangyun.care.ai.model;

import java.time.LocalTime;
import java.util.List;
import lombok.Data;

/** 求解器输入：一个班次槽位定义（每天需求 requiredCount 人）。 */
@Data
public class SolverShift {
  private String shiftCode;
  private String shiftName;
  private LocalTime startTime;
  private LocalTime endTime;
  /** 结束时间跨天（夜班常见） */
  private Integer crossDay;
  private int requiredCount = 1;
  /** 是否夜班（夜班后强制休、夜班公平性打分用） */
  private boolean night;
  /** 资质要求：可上此班次的角色编码，空表示不限 */
  private List<String> requiredRoles;

  public double durationHours() {
    if (startTime == null || endTime == null) {
      return 8;
    }
    int startMin = startTime.getHour() * 60 + startTime.getMinute();
    int endMin = endTime.getHour() * 60 + endTime.getMinute();
    if (crossDay != null && crossDay == 1) {
      endMin += 24 * 60;
    }
    if (endMin <= startMin) {
      endMin += 24 * 60;
    }
    return (endMin - startMin) / 60.0;
  }
}
