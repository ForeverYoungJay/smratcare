package com.zhiyangyun.care.fire.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FireSafetyQrResponse {
  private Long recordId;
  private String qrToken;
  private String qrContent;
  private LocalDateTime generatedAt;
}
