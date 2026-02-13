package com.zhiyangyun.care.survey.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.survey.entity.SurveySubmission;
import com.zhiyangyun.care.survey.model.SurveyPerformanceItem;
import com.zhiyangyun.care.survey.model.SurveyStatsSummaryResponse;
import java.time.LocalDate;
import java.util.List;

public interface SurveySubmissionService {
  SurveySubmission submit(Long orgId, Long submitterId, String submitterName, String submitterRole,
                          SurveySubmission submission, List<com.zhiyangyun.care.survey.entity.SurveySubmissionItem> items);

  IPage<SurveySubmission> page(Long orgId, long pageNo, long pageSize, Long templateId, String targetType, Long targetId);

  SurveyStatsSummaryResponse summary(Long orgId, Long templateId, LocalDate from, LocalDate to);

  List<SurveyPerformanceItem> performance(Long orgId, Long templateId, LocalDate from, LocalDate to);
}
