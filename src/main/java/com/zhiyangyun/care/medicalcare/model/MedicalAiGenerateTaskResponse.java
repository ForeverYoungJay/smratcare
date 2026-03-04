package com.zhiyangyun.care.medicalcare.model;

import lombok.Data;

@Data
public class MedicalAiGenerateTaskResponse {
  private String inspectionRoute;
  private String medicalTodoRoute;
  private String nursingTaskRoute;
}
