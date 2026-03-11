package com.zhiyangyun.care.elder.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ElderCreateRequest {
  private Long tenantId;
  private Long orgId;
  private Long createdBy;
  private String elderCode;
  @NotBlank
  @Size(max = 64, message = "姓名长度不能超过64")
  private String fullName;
  @Pattern(regexp = "^(|\\d{15}|\\d{17}[\\dXx])$", message = "身份证号格式不正确")
  private String idCardNo;
  private Integer gender;
  private LocalDate birthDate;
  @Pattern(regexp = "^(|1\\d{10})$", message = "手机号格式不正确")
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
