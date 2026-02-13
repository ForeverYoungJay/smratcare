package com.zhiyangyun.care.service;

import com.zhiyangyun.care.model.CareTaskTodayItem;
import com.zhiyangyun.care.model.CareTaskCreateRequest;
import com.zhiyangyun.care.model.ExecuteTaskRequest;
import com.zhiyangyun.care.model.ExecuteTaskResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CareTaskService {
  List<CareTaskTodayItem> getTodayTasks(Long tenantId, Long staffId, LocalDate date);

  IPage<CareTaskTodayItem> page(Long tenantId, long pageNo, long pageSize, LocalDate dateFrom,
      LocalDate dateTo, Long staffId, String roomNo, String careLevel, String status, String keyword);

  void assignTask(Long tenantId, Long taskDailyId, Long staffId, boolean force);

  int assignBatch(Long tenantId, String dateFrom, String dateTo, Long staffId, String roomNo,
      String floorNo, String building, String careLevel, String status, boolean force);

  void reviewTask(Long tenantId, Long taskDailyId, Long staffId, Integer score, String comment,
      String reviewerType, Long reviewerId, String reviewerName, LocalDateTime reviewTime);

  Long createTask(Long tenantId, CareTaskCreateRequest request);

  ExecuteTaskResponse executeTask(Long tenantId, ExecuteTaskRequest request);

  int generateDailyTasks(Long tenantId, LocalDate date);

  int generateDailyTasks(Long tenantId, LocalDate date, boolean force);
}
