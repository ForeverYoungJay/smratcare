package com.zhiyangyun.care.vital.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;
import com.fasterxml.jackson.databind.JsonNode;

@Data
public class VitalRecordRequest {
  @NotNull
  private Long elderId;
  @NotBlank
  private String type;
  @NotNull
  private JsonNode valueJson;
  @NotNull
  private LocalDateTime measuredAt;
  private Long recordedByStaffId;
  private String remark;
}
