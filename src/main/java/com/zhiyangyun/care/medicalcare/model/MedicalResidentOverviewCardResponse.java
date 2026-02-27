package com.zhiyangyun.care.medicalcare.model;

import java.util.List;
import lombok.Data;

@Data
public class MedicalResidentOverviewCardResponse {
  private String key;
  private String title;
  private String tag;
  private String tagColor;
  private String description;
  private List<String> lines;
  private List<MedicalResidentOverviewActionResponse> actions;
}
