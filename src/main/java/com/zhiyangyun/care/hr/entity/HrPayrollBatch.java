package com.zhiyangyun.care.hr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("hr_payroll_batch")
public class HrPayrollBatch {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private String period;
  private String status;
  private Integer headcount;
  private BigDecimal totalGross;
  private BigDecimal totalNet;
  private Long preparedBy;
  private String preparedByName;
  private Long approvedBy;
  private String approvedByName;
  private LocalDateTime approvedAt;
  private String rejectReason;
  private String bankSubmittedByName;
  private LocalDateTime bankSubmittedAt;
  private String paymentRef;
  private String paidByName;
  private LocalDateTime paidAt;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Integer isDeleted;
}
