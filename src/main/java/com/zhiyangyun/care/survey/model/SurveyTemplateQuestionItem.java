package com.zhiyangyun.care.survey.model;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SurveyTemplateQuestionItem {
  @NotNull
  private Long questionId;

  private Integer sortNo;

  private Integer requiredFlag;

  private BigDecimal weight;
}
