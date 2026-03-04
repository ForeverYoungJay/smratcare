package com.zhiyangyun.care.hr.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrGenericApprovalResponse {
  private Long id;
  private String title;
  private String approvalType;
  private String scene;
  private String applicantName;
  private String status;
  private BigDecimal amount;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private LocalDateTime createTime;
  private String remark;
}
