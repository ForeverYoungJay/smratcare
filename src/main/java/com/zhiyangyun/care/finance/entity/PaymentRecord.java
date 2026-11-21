package com.zhiyangyun.care.finance.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("payment_record")
public class PaymentRecord {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long billMonthlyId;
  /** 实收现金，不含任何抵扣。 */
  private BigDecimal amount;
  /** 登记时的应收余额快照。 */
  private BigDecimal payableAmount;
  private BigDecimal ltciDeductAmount;
  private BigDecimal discountAmount;
  private String discountReason;
  private BigDecimal voucherAmount;
  /** 本次抵账合计 = 实收 + 长护险 + 折扣 + 消费券。 */
  private BigDecimal settledAmount;
  private String payMethod;
  private String externalTxnId;
  private LocalDateTime paidAt;
  private Long operatorStaffId;
  private String remark;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
