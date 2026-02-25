package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class DiningDishRequest {
  @NotBlank
  private String dishName;

  private String dishCategory;

  private String mealType;

  @NotNull
  private BigDecimal unitPrice;

  private Integer calories;

  private String nutritionInfo;

  private String allergenTags;

  private String tagIds;

  private String status;

  private String remark;
}
