package com.zhiyangyun.care.survey.model;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class SurveyTemplateQuestionUpdateRequest {
  @NotEmpty
  private List<SurveyTemplateQuestionItem> questions;
}
