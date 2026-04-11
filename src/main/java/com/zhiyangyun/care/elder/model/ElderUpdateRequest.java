package com.zhiyangyun.care.elder.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ElderUpdateRequest {
  private Long tenantId;
  private Long updatedBy;
  private String elderCode;
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
  private Integer status;
  private String lifecycleStatus;
  private String departureType;
  private String careLevel;
  private String riskPrecommit;
  private String remark;
  private String sourceType;
  private String historicalContractFileUrl;
  private Long bedId;
  private LocalDate bedStartDate;
  private Boolean clearBed;
}
