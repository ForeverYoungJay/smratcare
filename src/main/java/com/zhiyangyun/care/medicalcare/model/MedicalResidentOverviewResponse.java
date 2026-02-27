package com.zhiyangyun.care.medicalcare.model;

import java.util.List;
import lombok.Data;

@Data
public class MedicalResidentOverviewResponse {
  private Long elderId;
  private String elderName;
  private String currentStatus;
  private Boolean hasUnclosedIncident;
  private List<MedicalResidentOverviewCardResponse> cards;
}
