package com.zhiyangyun.care.elder.model.lifecycle;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class TrialStayRequest {
  @NotNull
  private Long elderId;
  @NotNull
  private LocalDate trialStartDate;
  @NotNull
  private LocalDate trialEndDate;
  private String channel;
  private String trialPackage;
  private String intentLevel;
  private String status;
  private String careLevel;
  private String remark;
}
