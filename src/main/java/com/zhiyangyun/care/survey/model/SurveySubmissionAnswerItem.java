package com.zhiyangyun.care.survey.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SurveySubmissionAnswerItem {
  @NotNull
  private Long questionId;

  private String answerJson;

  private String answerText;

  private Integer score;
}
