package com.zhiyangyun.care.assessment.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class AssessmentBatchStatusRequest {
  @NotEmpty
  private List<String> ids;

  @NotBlank
  private String status;
}
