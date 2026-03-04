package com.zhiyangyun.care.hr.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class HrRecruitmentNeedRequest {
  private String title;
  private String positionName;
  private String departmentName;
  private Integer headcount;
  private LocalDate requiredDate;
  private String scene;
  private String status;
  private String remark;
}
