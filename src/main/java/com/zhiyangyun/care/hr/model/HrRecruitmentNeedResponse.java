package com.zhiyangyun.care.hr.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrRecruitmentNeedResponse {
  private Long id;
  private String title;
  private String positionName;
  private String departmentName;
  private Integer headcount;
  private LocalDate requiredDate;
  private String scene;
  private String status;
  private String remark;
  private String applicantName;
  private LocalDateTime createTime;
}
