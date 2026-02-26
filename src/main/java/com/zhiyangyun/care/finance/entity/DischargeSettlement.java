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
@TableName("finance_discharge_settlement")
public class DischargeSettlement {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private String detailNo;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private String elderName;
  private Long dischargeApplyId;
  private BigDecimal payableAmount;
  private String feeItem;
  private String dischargeFeeConfig;
  private BigDecimal fromDepositAmount;
  private BigDecimal refundAmount;
  private BigDecimal supplementAmount;
  private String status;
  private Integer frontdeskApproved;
  private String frontdeskSignerName;
  private LocalDateTime frontdeskSignedTime;
  private Integer nursingApproved;
  private String nursingSignerName;
  private LocalDateTime nursingSignedTime;
  private Integer financeRefunded;
  private String financeRefundOperatorName;
  private LocalDateTime financeRefundTime;
  private String remark;
  private Long settledBy;
  private LocalDateTime settledTime;
  private Long createdBy;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
