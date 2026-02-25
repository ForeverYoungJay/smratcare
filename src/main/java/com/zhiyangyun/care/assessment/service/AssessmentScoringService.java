package com.zhiyangyun.care.assessment.service;

import com.zhiyangyun.care.assessment.entity.AssessmentScaleTemplate;
import com.zhiyangyun.care.assessment.model.AssessmentScoreResult;

public interface AssessmentScoringService {
  AssessmentScaleTemplate getActiveTemplate(Long orgId, Long templateId);

  AssessmentScoreResult score(AssessmentScaleTemplate template, String detailJson);
}
