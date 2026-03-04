package com.zhiyangyun.care.logistics.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LogisticsEquipmentArchiveRequest {
  @NotBlank
  private String equipmentCode;

  @NotBlank
  private String equipmentName;

  private String category;

  private String brand;

  private String modelNo;

  private String serialNo;

  private LocalDate purchaseDate;

  private LocalDate warrantyUntil;

  private String location;

  private String maintainerName;

  private LocalDateTime lastMaintainedAt;

  private LocalDateTime nextMaintainedAt;

  private String status;

  private String remark;
}
