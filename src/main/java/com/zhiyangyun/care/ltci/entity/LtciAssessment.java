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
@TableName("ltci_assessment")
public class LtciAssessment {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long applyId;
  private Long elderId;
  private Long templateId;
  private Long assessorId;
  private LocalDate assessDate;
  private BigDecimal adlScore;
  private BigDecimal cognitiveScore;
  private BigDecimal perceptionScore;
  private BigDecimal totalScore;
  private Integer disabilityLevel;
  private String levelLabel;
  private String answersJson;
  private Integer escalated;
  private LocalDate effectiveStart;
  private LocalDate effectiveEnd;
  private String assessStatus;
  private Long reviewOfId;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
