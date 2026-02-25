package com.zhiyangyun.care.life.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiningRiskReasonItem {
  private String reasonType;
  private String reasonCode;
  private String reasonDetail;
}
