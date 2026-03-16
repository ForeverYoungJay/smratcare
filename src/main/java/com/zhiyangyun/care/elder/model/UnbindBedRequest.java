package com.zhiyangyun.care.elder.model;

import java.time.LocalDate;
import lombok.Data;

@Data
public class UnbindBedRequest {
  private LocalDate endDate;
  private String reason;
}
