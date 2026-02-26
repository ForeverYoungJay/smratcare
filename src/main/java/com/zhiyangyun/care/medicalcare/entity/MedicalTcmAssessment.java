package com.zhiyangyun.care.medicalcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("medical_tcm_assessment")
public class MedicalTcmAssessment {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private String elderName;
  private LocalDate assessmentDate;
  private Long assessorId;
  private String assessorName;
  private String assessmentScene;
  private String constitutionPrimary;
  private String constitutionSecondary;
  private BigDecimal score;
  private BigDecimal confidence;
  private String featureSummary;
  private String suggestionDiet;
  private String suggestionRoutine;
  private String suggestionExercise;
  private String suggestionEmotion;
  private String nursingTip;
  private String suggestionPoints;
  private String questionnaireJson;
  private Integer isReassessment;
  private Integer familyVisible;
  private Integer generateNursingTask;
  private String status;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
