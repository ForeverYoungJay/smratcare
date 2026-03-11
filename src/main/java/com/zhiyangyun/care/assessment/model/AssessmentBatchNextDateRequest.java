package com.zhiyangyun.care.assessment.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class AssessmentBatchNextDateRequest {
  @NotEmpty
  private List<String> ids;

  @NotNull
  private LocalDate nextAssessmentDate;
}
