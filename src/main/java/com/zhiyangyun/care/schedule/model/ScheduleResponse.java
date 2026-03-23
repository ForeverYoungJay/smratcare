package com.zhiyangyun.care.schedule.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ScheduleResponse {
  private Long id;
  private Long staffId;
  private String staffName;
  private Long sourceTemplateId;
  private String sourceTemplateName;
  private LocalDate dutyDate;
  private String shiftCode;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Long calendarTaskId;
  private Long dutyTodoId;
  private Integer status;
}
