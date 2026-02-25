package com.zhiyangyun.care.life.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dining_risk_override")
public class DiningRiskOverride {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private Long elderId;

  private String elderName;

  private String dishNames;

  private String riskDetail;

  private String applyReason;

  private Long applyStaffId;

  private String reviewStatus;

  private Long reviewStaffId;

  private String reviewRemark;

  private LocalDateTime reviewedAt;

  private LocalDateTime effectiveUntil;

  private Long createdBy;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
