package com.zhiyangyun.care.standard.service;

import java.time.LocalDate;

public interface TaskGenerateService {
  int generateByElderPackage(Long tenantId, LocalDate date, boolean force);
}
