package com.zhiyangyun.care.elder.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ElderUpdateRequest {
  private Long tenantId;
  private Long updatedBy;
  private String fullName;
  private String idCardNo;
  private Integer gender;
  private LocalDate birthDate;
  private String phone;
  private LocalDate admissionDate;
  private Integer status;
  private String careLevel;
  private String remark;
  private Long bedId;
  private LocalDate bedStartDate;
}
