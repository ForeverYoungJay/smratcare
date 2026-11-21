package com.zhiyangyun.care.finance.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 长者押金账户：在押余额 = 已缴 − 已扣 − 已退。 */
@Data
@TableName("finance_deposit_account")
public class FinanceDepositAccount {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private BigDecimal standardAmount;
  private BigDecimal paidAmount;
  private BigDecimal deductedAmount;
  private BigDecimal refundedAmount;
  private BigDecimal balanceAmount;
  /** UNPAID / PARTIAL / PAID / CLOSED。 */
  private String status;
  private LocalDateTime lastOpAt;
  private String remark;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
