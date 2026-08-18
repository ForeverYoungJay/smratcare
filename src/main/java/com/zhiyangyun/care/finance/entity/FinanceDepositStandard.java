package com.zhiyangyun.care.finance.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 押金标准：按护理等级配置，care_level 为空表示通用默认标准。 */
@Data
@TableName("finance_deposit_standard")
public class FinanceDepositStandard {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String standardName;
  private String careLevel;
  private BigDecimal amount;
  private LocalDate effectiveFrom;
  private String remark;
  private Long createdBy;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
