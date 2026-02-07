package com.zhiyangyun.care.model;

import lombok.Data;

@Data
public class ExecuteTaskResponse {
  private Long taskDailyId;
  private String status;
  private boolean abnormal;
  private boolean suspicious;
  private String message;
}
