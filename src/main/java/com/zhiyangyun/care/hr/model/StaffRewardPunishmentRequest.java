package com.zhiyangyun.care.hr.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class StaffRewardPunishmentRequest {
  @NotNull
  private Long staffId;

  private String staffName;

  @NotBlank
  private String type;

  private String level;

  @NotBlank
  private String title;

  private String reason;

  private BigDecimal amount;

  private Integer points;

  @NotNull
  private LocalDate occurredDate;

  private String status;

  private String remark;
}
