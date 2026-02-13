package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class MealPlanRequest {
  @NotNull
  private LocalDate planDate;

  @NotBlank
  private String mealType;

  @NotBlank
  private String menu;

  private Integer calories;

  private String remark;
}
