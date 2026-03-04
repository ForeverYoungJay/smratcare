package com.zhiyangyun.care.hr.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class HrStaffCertificateRequest {
  private Long staffId;
  private String certificateName;
  private String certificateNo;
  private String issuingAuthority;
  private LocalDate issueDate;
  private LocalDate expiryDate;
  private String remark;
}
