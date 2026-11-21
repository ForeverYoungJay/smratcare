package com.zhiyangyun.care.hr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("hr_payroll_line")
public class HrPayrollLine {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long batchId;
  private Long staffId;
  private String staffName;
  private BigDecimal baseAmount;
  private BigDecimal performanceAmount;
  private BigDecimal allowanceAmount;
  private BigDecimal overtimeAmount;
  private BigDecimal deductionAmount;
  private BigDecimal personalAmount;
  private BigDecimal grossAmount;
  private BigDecimal netAmount;
  private String remark;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
