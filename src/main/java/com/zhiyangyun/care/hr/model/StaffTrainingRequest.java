package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class StaffTrainingRequest {
  private Long sourceTrainingId;
  private String trainingScene;
  private Integer trainingYear;
  private Long departmentId;
  private String departmentName;
  private String staffNo;
  private Long staffId;
  private String trainingName;
  private String trainingType;
  private String provider;
  private String instructor;
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal hours;
  private BigDecimal score;
  private String attendanceStatus;
  private String testResult;
  private Integer certificateRequired;
  private String certificateStatus;
  private Integer status;
  private String certificateNo;
  private List<Map<String, Object>> attachments;
  private String remark;
}
