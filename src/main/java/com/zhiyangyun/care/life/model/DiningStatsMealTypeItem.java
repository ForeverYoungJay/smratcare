package com.zhiyangyun.care.life.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiningStatsMealTypeItem {
  private String mealType;
  private long orderCount;
}
