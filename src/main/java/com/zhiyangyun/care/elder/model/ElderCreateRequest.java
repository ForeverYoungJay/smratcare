package com.zhiyangyun.care.elder.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ElderCreateRequest {
  private Long orgId;
  @NotBlank
  private String elderCode;
  @NotBlank
  private String fullName;
  private String idCardNo;
  private Integer gender;
  private LocalDate birthDate;
  private String phone;
  private LocalDate admissionDate;
  private Integer status = 1;
  private String careLevel;
  private String remark;
}
