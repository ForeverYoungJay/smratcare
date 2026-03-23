package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.Data;

@Data
public class OaActivityPlanExecutionRequest {
  @Min(0)
  private Integer actualCount;

  private List<String> signInRecords;

  private List<String> photoUrls;

  private String executionRemark;
}
