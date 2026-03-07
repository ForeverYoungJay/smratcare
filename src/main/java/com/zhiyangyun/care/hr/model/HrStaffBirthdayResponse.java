package com.zhiyangyun.care.hr.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class HrStaffBirthdayResponse {
  private Long staffId;
  private String staffNo;
  private String realName;
  private String phone;
  private Long departmentId;
  private String jobTitle;
  private Integer status;
  private LocalDate birthday;
  private LocalDate nextBirthday;
  private Integer daysUntil;
  private String remark;
}
