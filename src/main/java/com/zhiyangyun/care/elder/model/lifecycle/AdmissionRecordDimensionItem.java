package com.zhiyangyun.care.elder.model.lifecycle;

import lombok.Data;

@Data
public class AdmissionRecordDimensionItem {
  private String dimensionKey;
  private String dimensionLabel;
  private long count;
  private double ratio;
}
