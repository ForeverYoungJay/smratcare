package com.zhiyangyun.care.survey.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SurveyTemplateQuestionDetail {
  private Long questionId;
  private String questionCode;
  private String title;
  private String questionType;
  private String optionsJson;
  private Integer requiredFlag;
  private Integer scoreEnabled;
  private Integer maxScore;
  private Integer status;
  private Integer sortNo;
  private BigDecimal weight;
}
