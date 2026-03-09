package com.zhiyangyun.care.medicalcare.model;

import java.util.List;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MedicalResidentOverviewResponse {
  private Long elderId;
  private String elderName;
  private String currentStatus;
  private Boolean hasUnclosedIncident;
  private Integer alertCardCount;
  private Integer alertTotalCount;
  private LocalDateTime generatedAt;
  private List<MedicalResidentOverviewCardResponse> cards;
}
