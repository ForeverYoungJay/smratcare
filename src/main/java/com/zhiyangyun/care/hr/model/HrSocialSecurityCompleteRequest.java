package com.zhiyangyun.care.hr.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class HrSocialSecurityCompleteRequest {
  private Long staffId;
  private LocalDate socialSecurityStartDate;
  private String socialSecurityRemark;
}
