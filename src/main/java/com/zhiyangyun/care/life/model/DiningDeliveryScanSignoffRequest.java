package com.zhiyangyun.care.life.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class DiningDeliveryScanSignoffRequest {
  @NotBlank
  private String qrToken;

  private String deliveredByName;

  private LocalDateTime signedAt;

  private LocalDateTime qrScanAt;

  private List<String> signoffImageUrls;

  private String remark;
}
