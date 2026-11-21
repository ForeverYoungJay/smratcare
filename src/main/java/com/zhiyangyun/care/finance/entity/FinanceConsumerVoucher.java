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

/** 消费券：可在账单收款时抵扣应收，支持绑定长者、有效期与剩余额度。 */
@Data
@TableName("finance_consumer_voucher")
public class FinanceConsumerVoucher {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  /** 绑定长者，null 表示机构通用券。 */
  private Long elderId;
  private String voucherNo;
  private String voucherName;
  private BigDecimal faceAmount;
  private BigDecimal balanceAmount;
  private BigDecimal minBillAmount;
  private Integer allowSplit;
  private LocalDate validFrom;
  private LocalDate validTo;
  /** ACTIVE / USED / EXPIRED / REVOKED。 */
  private String status;
  private String source;
  private Long issuedBy;
  private LocalDateTime issuedAt;
  private Long revokedBy;
  private LocalDateTime revokedAt;
  private String revokeReason;
  private String remark;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
