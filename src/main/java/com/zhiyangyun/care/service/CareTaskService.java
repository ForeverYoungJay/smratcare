package com.zhiyangyun.care.service;

import com.zhiyangyun.care.model.CareTaskTodayItem;
import com.zhiyangyun.care.model.ExecuteTaskRequest;
import com.zhiyangyun.care.model.ExecuteTaskResponse;
import java.time.LocalDate;
import java.util.List;

public interface CareTaskService {
  List<CareTaskTodayItem> getTodayTasks(Long staffId, LocalDate date);

  ExecuteTaskResponse executeTask(ExecuteTaskRequest request);

  int generateDailyTasks(LocalDate date);

  int generateDailyTasks(LocalDate date, boolean force);
}
