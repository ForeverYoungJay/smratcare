package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class StaffTrainingResponse {
  private Long id;
  private Long sourceTrainingId;
  private String trainingScene;
  private Integer trainingYear;
  private Long departmentId;
  private String departmentName;
  private Long staffId;
  private String staffNo;
  private String staffName;
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
  private LocalDateTime createTime;
}
