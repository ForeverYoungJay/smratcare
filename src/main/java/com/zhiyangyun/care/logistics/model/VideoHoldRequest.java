package com.zhiyangyun.care.logistics.model;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VideoHoldRequest {
  @NotNull
  private Boolean held;
  @NotBlank @Size(max = 255)
  private String reason;
}
