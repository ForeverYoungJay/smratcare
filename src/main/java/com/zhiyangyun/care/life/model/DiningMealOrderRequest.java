package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class DiningMealOrderRequest {
  @NotNull
  private Long elderId;

  private String elderName;

  @NotNull
  private LocalDate orderDate;

  @NotBlank
  private String mealType;

  private String dishIds;

  @NotBlank
  private String dishNames;

  @NotNull
  private BigDecimal totalAmount;

  private Long prepZoneId;

  private String prepZoneName;

  private Long deliveryAreaId;

  private String deliveryAreaName;

  private Long overrideId;

  private String status;

  private String remark;
}
