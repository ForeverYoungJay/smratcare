package com.zhiyangyun.care.survey.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class SurveySubmissionRequest {
  @NotNull
  private Long templateId;

  @NotBlank
  private String targetType;

  private Long targetId;

  private Long relatedStaffId;

  private Integer anonymousFlag;

  @NotEmpty
  private List<SurveySubmissionAnswerItem> answers;
}
