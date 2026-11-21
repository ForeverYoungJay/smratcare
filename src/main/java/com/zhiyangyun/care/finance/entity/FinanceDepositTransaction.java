package com.zhiyangyun.care.finance.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 押金流水：PAY 缴纳 / DEDUCT 扣款 / REFUND 退还。 */
@Data
@TableName("finance_deposit_transaction")
public class FinanceDepositTransaction {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long depositAccountId;
  private Long elderId;
  private String txnType;
  private BigDecimal amount;
  private BigDecimal balanceAfter;
  private String payMethod;
  private LocalDateTime occurredAt;
  private String reason;
  private String remark;
  private Long operatorStaffId;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
