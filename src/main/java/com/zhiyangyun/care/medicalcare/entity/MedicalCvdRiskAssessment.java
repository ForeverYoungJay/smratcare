package com.zhiyangyun.care.medicalcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("medical_cvd_risk_assessment")
public class MedicalCvdRiskAssessment {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private String elderName;
  private LocalDate assessmentDate;
  private Long assessorId;
  private String assessorName;
  private String riskLevel;
  private String keyRiskFactors;
  private String lifestyleJson;
  private String factorJson;
  private String conclusion;
  private String medicalAdvice;
  private String nursingAdvice;
  private Integer followupDays;
  private Integer needFollowup;
  private Integer generateInspectionPlan;
  private Integer generateFollowupTask;
  private Integer suggestMedicalOrder;
  private String status;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
