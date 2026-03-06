package com.zhiyangyun.care.assessment.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class AssessmentBatchOperationResult {
  private int successCount;
  private int failedCount;
  private List<Long> successIds = new ArrayList<>();
  private List<AssessmentBatchFailureItem> failures = new ArrayList<>();

  public void addSuccess(Long id) {
    successCount++;
    if (id != null) {
      successIds.add(id);
    }
  }

  public void addFailure(Long id, String reason) {
    failedCount++;
    AssessmentBatchFailureItem item = new AssessmentBatchFailureItem();
    item.setId(id);
    item.setReason(reason);
    failures.add(item);
  }
}
