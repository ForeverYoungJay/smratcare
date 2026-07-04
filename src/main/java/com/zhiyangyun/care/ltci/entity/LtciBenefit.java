package com.zhiyangyun.care.ltci.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ltci_benefit")
public class LtciBenefit {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long insuredId;
  private Long elderId;
  private Long assessmentId;
  private Integer disabilityLevel;
  private String benefitType;
  /** 日限额，单位：分。 */
  private Long dailyQuota;
  /** 统筹支付比例，0~1。 */
  private BigDecimal payRatio;
  private String benefitStatus;
  private LocalDate validStart;
  private LocalDate validEnd;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
