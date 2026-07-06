package com.zhiyangyun.care.ai.model;

import java.time.LocalDate;
import lombok.Data;

/** 求解器输出：某员工某日某班次的一条分配。 */
@Data
public class SolverAssignment {
  private Long staffId;
  private String staffName;
  private LocalDate dutyDate;
  private String shiftCode;
  private boolean night;

  public SolverAssignment() {}

  public SolverAssignment(Long staffId, String staffName, LocalDate dutyDate, String shiftCode, boolean night) {
    this.staffId = staffId;
    this.staffName = staffName;
    this.dutyDate = dutyDate;
    this.shiftCode = shiftCode;
    this.night = night;
  }
}
