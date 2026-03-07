package com.zhiyangyun.care.schedule.model;

import lombok.Data;

@Data
public class AttendanceReviewRequest {
  private Integer reviewed;
  private String reviewRemark;
}
