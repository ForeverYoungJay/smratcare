package com.zhiyangyun.care.survey.model;

import java.util.List;
import lombok.Data;

@Data
public class SurveyTemplateDetailResponse {
  private Long id;
  private String templateCode;
  private String templateName;
  private String description;
  private String targetType;
  private Integer status;
  private String startDate;
  private String endDate;
  private Integer anonymousFlag;
  private Integer scoreEnabled;
  private Integer totalScore;
  private List<SurveyTemplateQuestionDetail> questions;
}
