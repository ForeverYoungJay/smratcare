package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OaNoticeRequest {
  @NotBlank
  private String title;

  @NotBlank
  private String content;

  private String status;

  private LocalDateTime publishTime;
}
