package com.zhiyangyun.care.hr.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class HrStaffCertificateResponse {
  private Long id;
  private Long staffId;
  private String staffName;
  private String certificateName;
  private String certificateNo;
  private String issuingAuthority;
  private LocalDate issueDate;
  private LocalDate expiryDate;
  private Long remainingDays;
  private Integer status;
  private String remark;
}
