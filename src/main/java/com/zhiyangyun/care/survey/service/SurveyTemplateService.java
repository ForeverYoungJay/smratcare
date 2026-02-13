package com.zhiyangyun.care.survey.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.survey.entity.SurveyTemplate;
import com.zhiyangyun.care.survey.model.SurveyTemplateDetailResponse;
import com.zhiyangyun.care.survey.model.SurveyTemplateQuestionItem;
import java.util.List;

public interface SurveyTemplateService {
  SurveyTemplate create(Long orgId, SurveyTemplate template);

  SurveyTemplate update(Long orgId, Long id, SurveyTemplate template);

  SurveyTemplateDetailResponse getDetail(Long orgId, Long id);

  IPage<SurveyTemplate> page(Long orgId, long pageNo, long pageSize, String keyword, Integer status, String targetType);

  void setQuestions(Long orgId, Long templateId, List<SurveyTemplateQuestionItem> items);

  void delete(Long orgId, Long id);
}
