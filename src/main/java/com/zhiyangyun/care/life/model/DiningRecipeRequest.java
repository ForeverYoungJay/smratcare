package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Data;

@Data
public class DiningRecipeRequest {
  @NotBlank
  private String recipeName;

  private String mealType;

  private String dishIds;

  @NotBlank
  private String dishNames;

  private LocalDate planDate;

  private String suitableCrowd;

  private String status;

  private String remark;
}
