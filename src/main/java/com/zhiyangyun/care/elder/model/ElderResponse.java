package com.zhiyangyun.care.elder.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ElderResponse {
  private Long id;
  private Long orgId;
  private String elderCode;
  private String elderQrCode;
  private String fullName;
  private String idCardNo;
  private Integer gender;
  private LocalDate birthDate;
  private String phone;
  private LocalDate admissionDate;
  private Integer status;
  private Long bedId;
  private String careLevel;
  private String remark;
  private BedResponse currentBed;
}
