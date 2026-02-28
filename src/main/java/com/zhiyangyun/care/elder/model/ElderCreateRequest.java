package com.zhiyangyun.care.elder.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ElderCreateRequest {
  private Long tenantId;
  private Long orgId;
  private Long createdBy;
  private String elderCode;
  @NotBlank
  private String fullName;
  private String idCardNo;
  private Integer gender;
  private LocalDate birthDate;
  private String phone;
  private String homeAddress;
  private String medicalInsuranceCopyUrl;
  private String householdCopyUrl;
  private String medicalRecordFileUrl;
  private LocalDate admissionDate;
  private Integer status = 1;
  private String careLevel;
  private String riskPrecommit;
  private String remark;
  private Long bedId;
  private LocalDate bedStartDate;
}
