package com.zhiyangyun.care.ltci.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ltci_settlement")
public class LtciSettlement {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private Long benefitId;
  private String settleMonth;
  private Integer serviceDays;
  private Long totalFee;
  private Long fundPay;
  private Long selfPay;
  private Long overQuota;
  private Long dailyQuota;
  private BigDecimal payRatio;
  private String settleStatus;
  private String settleNo;
  private String detailJson;
  private Long submittedBy;
  private LocalDateTime submittedAt;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
