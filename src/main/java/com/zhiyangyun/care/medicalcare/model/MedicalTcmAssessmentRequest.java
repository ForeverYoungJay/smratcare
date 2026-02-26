package com.zhiyangyun.care.medicalcare.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class MedicalTcmAssessmentRequest {
  private Long elderId;
  @NotBlank
  private String elderName;
  @NotNull
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
}
