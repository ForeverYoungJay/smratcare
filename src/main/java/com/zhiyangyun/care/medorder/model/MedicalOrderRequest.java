package com.zhiyangyun.care.medorder.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

/** 开立医嘱请求。 */
@Data
public class MedicalOrderRequest {
  @NotNull
  private Long elderId;
  private Long emrId;
  /** LONG_TERM / TEMPORARY，默认 LONG_TERM。 */
  private String orderType;
  /** DRUG / NURSING / EXAM / DIET，默认 DRUG。 */
  private String category;
  @NotBlank
  private String content;
  private Long drugId;
  private String dosage;
  private String frequency;
  private String route;
  private Integer quantityPerTime;
  private LocalDateTime startTime;
  private LocalDateTime stopTime;
  private String priority;
  private String remark;
}
