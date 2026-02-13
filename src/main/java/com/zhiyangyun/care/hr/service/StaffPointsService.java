package com.zhiyangyun.care.hr.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.hr.model.StaffPointsAccountResponse;
import com.zhiyangyun.care.hr.model.StaffPointsAdjustRequest;
import com.zhiyangyun.care.hr.model.StaffPointsLogResponse;

public interface StaffPointsService {
  StaffPointsAccountResponse adjust(Long orgId, Long operatorId, StaffPointsAdjustRequest request);

  StaffPointsAccountResponse awardForTask(Long orgId, Long staffId, Long taskDailyId, int points, String remark);

  StaffPointsAccountResponse awardForReview(Long orgId, Long staffId, Long taskDailyId, int points, String remark);

  IPage<StaffPointsLogResponse> pageLogs(Long orgId, long pageNo, long pageSize, Long staffId,
      String dateFrom, String dateTo);

  void awardSurveyPoints(Long orgId, Long staffId, Long submissionId, Integer points, String remark);
}
