package com.zhiyangyun.care.life.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DiningDeliverySignoffQrResponse {
  private Long recordId;
  private String qrToken;
  private String qrContent;
  private LocalDateTime generatedAt;
}
