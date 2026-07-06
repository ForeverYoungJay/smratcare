package com.zhiyangyun.care.medicalcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 慢病随访计划。 */
@Data
@TableName("medical_followup_plan")
public class MedicalFollowupPlan {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private String elderName;
  /** HYPERTENSION 高血压 / DIABETES 糖尿病 / COPD 慢阻肺 / CHD 冠心病 / STROKE 脑卒中 / OTHER。 */
  private String diseaseType;
  private String planName;
  /** 随访频次（间隔天数）。 */
  private Integer frequencyDays;
  /** 目标指标，如 血压<140/90mmHg；空腹血糖<7.0mmol/L。 */
  private String targetIndicators;
  private LocalDate nextFollowupDate;
  private LocalDate lastFollowupDate;
  private Long doctorId;
  private String doctorName;
  /** ACTIVE / PAUSED / CLOSED。 */
  private String status;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
