package com.zhiyangyun.care.medins.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 医保结算清单。状态机：DRAFT → UPLOADED → RECEIPTED → RECONCILED；REJECTED 驳回后可重传。 */
@Data
@TableName("medins_settlement_sheet")
public class MedinsSettlementSheet {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long ltciSettlementId;
  private Long elderId;
  private String sheetNo;
  private String settleMonth;
  /** 费用合计（分）。 */
  private Long totalFee;
  /** 统筹支付（分）。 */
  private Long fundPay;
  /** 个人自付（分）。 */
  private Long selfPay;
  private String sheetStatus;
  private String channel;
  private String receiptCode;
  private String rejectReason;
  private LocalDateTime uploadedAt;
  private LocalDateTime receiptedAt;
  private LocalDateTime reconciledAt;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
