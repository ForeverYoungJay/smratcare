package com.zhiyangyun.care.assessment.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AssessmentRecordResponse {
  private Long id;

  private Long tenantId;

  private Long orgId;

  private String orgName;

  private Long elderId;

  private String elderName;

  private Integer gender;

  private String genderLabel;

  private Integer age;

  private String phone;

  private String address;

  private String assessmentType;

  private String assessmentTypeLabel;

  private Long templateId;

  private String levelCode;

  private BigDecimal score;

  private LocalDate assessmentDate;

  private LocalDate nextAssessmentDate;

  private Long assessorId;

  private String assessorName;

  private String status;

  private String statusLabel;

  private String resultSummary;

  private String suggestion;

  private String detailJson;

  private Integer scoreAuto;

  private String archiveNo;

  private String source;

  private Long createdBy;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;
}
