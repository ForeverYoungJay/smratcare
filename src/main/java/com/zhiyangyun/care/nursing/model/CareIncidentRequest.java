package com.zhiyangyun.care.nursing.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CareIncidentRequest {
  private Long elderId;
  @Size(max = 64, message = "elderName too long")
  private String elderName;
  @Size(max = 128, message = "location too long")
  private String location;
  @NotBlank
  @Pattern(regexp = "^(FALL|SKIN|CHOKING|WANDER|DELAY|ENVIRONMENT|OTHER)$", message = "invalid incidentType")
  private String incidentType;
  @NotBlank
  @Pattern(regexp = "^(LOW|MEDIUM|HIGH)$", message = "invalid severity")
  private String severity;
  @NotNull
  private LocalDateTime occurredAt;
  @NotBlank
  private String description;
  private String measures;
  @Size(max = 255, message = "notifyTargets too long")
  private String notifyTargets;
  private Long ownerId;
  private String ownerName;
  private Long relatedTaskId;
}
