package com.zhiyangyun.care.medicalcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("medical_alert_rule_config")
public class MedicalAlertRuleConfig {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Integer medicationHighDosageThreshold;
  private Integer overdueHoursThreshold;
  private Integer abnormalInspectionRequirePhoto;
  private Integer handoverAutoFillConfirmTime;
  private Integer autoCreateNursingLogFromInspection;
  private Integer autoRaiseTaskFromAbnormal;
  private Integer autoCarryResidentContext;
  private String handoverRiskKeywords;
  private Long createdBy;
  private Long updatedBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
