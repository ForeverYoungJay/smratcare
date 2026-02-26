package com.zhiyangyun.care.medicalcare.model;

import lombok.Data;

@Data
public class CvdPublishActionRequest {
  private Integer generateInspectionPlan;
  private Integer generateFollowupTask;
  private Integer suggestMedicalOrder;
}
