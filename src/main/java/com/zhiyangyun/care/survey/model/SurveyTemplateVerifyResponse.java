package com.zhiyangyun.care.survey.model;

import lombok.Data;

@Data
public class SurveyTemplateVerifyResponse {
  private Long templateId;
  private String templateCode;
  private String templateName;
  private String targetType;
  private Integer status;
  private boolean valid;
  private String message;
  private boolean hasQuestions;
}
