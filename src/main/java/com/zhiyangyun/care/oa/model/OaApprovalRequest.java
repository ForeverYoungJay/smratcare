package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OaApprovalRequest {
  @NotBlank
  private String approvalType;

  @NotBlank
  private String title;

  private Long applicantId;

  @NotBlank
  private String applicantName;

  private BigDecimal amount;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private String formData;

  private String status;

  private String remark;
}
