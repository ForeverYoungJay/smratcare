package com.zhiyangyun.care.nursing.model;

import java.time.LocalTime;
import lombok.Data;

@Data
public class ShiftTemplateResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String name;
  private String shiftCode;
  private LocalTime startTime;
  private LocalTime endTime;
  private Integer crossDay;
  private Integer requiredStaffCount;
  private String recurrenceType;
  private Long executeStaffId;
  private String executeStaffName;
  private Integer attendanceLinked;
  private Integer enabled;
  private String remark;
}
