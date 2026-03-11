package com.zhiyangyun.care.assessment.model;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class AssessmentBatchAssignRequest {
  @NotEmpty
  private List<String> ids;

  private Long assessorId;

  private String assessorName;
}
