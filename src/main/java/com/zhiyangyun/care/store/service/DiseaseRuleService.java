package com.zhiyangyun.care.store.service;

import com.zhiyangyun.care.store.model.admin.ForbiddenTagsResponse;
import java.util.List;

public interface DiseaseRuleService {
  void saveForbiddenTags(Long orgId, Long diseaseId, List<Long> tagIds);

  ForbiddenTagsResponse getForbiddenTags(Long orgId, Long diseaseId);

  void saveElderDiseases(Long orgId, Long elderId, List<Long> diseaseIds);

  List<Long> getElderDiseaseIds(Long orgId, Long elderId);
}
