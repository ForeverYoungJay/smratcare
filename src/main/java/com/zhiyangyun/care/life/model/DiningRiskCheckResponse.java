package com.zhiyangyun.care.life.model;

import java.util.List;
import lombok.Data;

@Data
public class DiningRiskCheckResponse {
  private boolean allowed;
  private boolean canOverride;
  private Long elderId;
  private String elderName;
  private String dishNames;
  private List<String> blockedDishNames;
  private List<DiningRiskReasonItem> reasons;
  private String message;
}
